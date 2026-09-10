package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Size
import io.github.sceneview.node.ViewNode2
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.CommonUtils
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.PermissionHelper
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R

class ARCompassActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var arSceneView: ARSceneView
    private lateinit var headingValue: TextView
    private var toolbar: Toolbar? = null
    private var tiltHintText: TextView? = null
    private var onboardingOverlay: View? = null
    private var onboardingShowing = false
    private var tiltHintShowing = false

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private var gravityValues = FloatArray(3)
    private var geomagneticValues = FloatArray(3)
    private var hasGravity = false
    private var hasMagnetic = false

    private val filteredGravity = FloatArray(3)
    private var hasFilteredGravity = false

    private val rotationMatrix = FloatArray(9)
    private val displayRotationMatrix = FloatArray(9)
    private val inclinationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val compassNodes = mutableListOf<CompassPointNode>()
    private var compassAnchor: AnchorNode? = null
    private var compassPlaced = false
    private var pendingBaselineHeading: Float? = null
    private var currentHeading = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!CommonUtils.checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        if (!PermissionHelper.hasCameraPermission(this)) {
            startActivity(android.content.Intent(this, LaunchScreenActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_ar_compass)
        headingValue = findViewById(R.id.headingValue)
        toolbar = findViewById(R.id.arCompassToolbar)
        tiltHintText = findViewById(R.id.tiltHintText)
        onboardingOverlay = findViewById(R.id.onboardingOverlay)
        setupToolbar()
        setupOnboarding()
        bindArSceneView()
        setupArCallbacks()
        initSensors()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != PermissionHelper.CAMERA_PERMISSION_CODE) return

        val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

    }

    override fun onResume() {
        super.onResume()
        if (isFinishing || !::sensorManager.isInitialized) return
        // Reachable only from VisualizerActivity, which already holds camera
        // permission by the time this launches — no permission check needed
        // here. The AR camera feed is live for this activity's whole
        // resumed life, so hold the screen awake.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        registerSensorListeners()
    }

    override fun onPause() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (isFinishing) {
            releaseCompassNodes()
        }
        super.onPause()
        if (::sensorManager.isInitialized) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onDestroy() {
        releaseCompassNodes()
        super.onDestroy()
    }

    private fun releaseCompassNodes() {
        if (compassNodes.isEmpty() && compassAnchor == null) return
        compassNodes.forEach {
            it.markerNode.parent = null
            it.markerNode.destroy()
        }
        compassAnchor?.let { anchor ->
            anchor.parent = null
            anchor.destroy()
            compassAnchor = null
        }
        compassNodes.clear()
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                gravityValues = event.values.clone()
                hasGravity = true
                updateFilteredGravity(event.values)
                updateTiltHint()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagneticValues = event.values.clone()
                hasMagnetic = true
            }
            else -> return
        }

        if (hasGravity && hasMagnetic) {
            val success = SensorManager.getRotationMatrix(
                rotationMatrix,
                inclinationMatrix,
                gravityValues,
                geomagneticValues
            )
            if (success) {
                val (xAxis, yAxis) = ArCompassDisplayAxes.forRotation(
                    arSceneView.display?.rotation ?: Surface.ROTATION_0
                )
                SensorManager.remapCoordinateSystem(
                    rotationMatrix,
                    xAxis,
                    yAxis,
                    displayRotationMatrix
                )
                SensorManager.getOrientation(displayRotationMatrix, orientationAngles)
                val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                val heading = (azimuth + 360f) % 360f
                handleHeadingUpdate(heading)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private fun setupOnboarding() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (!preferences.getBoolean(PREF_AR_COMPASS_FIRST_RUN, true)) {
            return
        }
        onboardingShowing = true
        onboardingOverlay?.visibility = View.VISIBLE
        findViewById<View>(R.id.onboardingGotItButton).setOnClickListener {
            preferences.edit().putBoolean(PREF_AR_COMPASS_FIRST_RUN, false).apply()
            onboardingShowing = false
            onboardingOverlay?.visibility = View.GONE
            updateTiltHint()
        }
    }

    /**
     * The compass markers are anchored at eye level, so they sit outside the camera
     * view whenever the phone is tilted well above or below the horizon. Nudge the
     * user to level the phone in that case, with hysteresis so the hint doesn't
     * flicker at the threshold.
     */
    private fun updateTiltHint() {
        if (!hasFilteredGravity) return
        val hint = tiltHintText ?: return

        if (onboardingShowing) {
            if (tiltHintShowing) {
                tiltHintShowing = false
                hint.visibility = View.GONE
            }
            return
        }

        val gx = filteredGravity[0]
        val gy = filteredGravity[1]
        val gz = filteredGravity[2]
        val magnitude = sqrt(gx * gx + gy * gy + gz * gz)
        if (magnitude < 0.1f) return

        // The rear camera looks along the device's -Z axis, so the camera's angle
        // above the horizon follows from the gravity component on that axis.
        val elevationDegrees = Math.toDegrees(
            asin((-gz / magnitude).coerceIn(-1f, 1f).toDouble())
        )

        if (!tiltHintShowing && abs(elevationDegrees) > TILT_HINT_SHOW_DEGREES) {
            tiltHintShowing = true
            hint.alpha = 0f
            hint.visibility = View.VISIBLE
            hint.animate().alpha(1f).setDuration(250).start()
        } else if (tiltHintShowing && abs(elevationDegrees) < TILT_HINT_HIDE_DEGREES) {
            tiltHintShowing = false
            hint.animate().alpha(0f).setDuration(250).withEndAction {
                hint.visibility = View.GONE
            }.start()
        }
    }

    private fun updateFilteredGravity(values: FloatArray) {
        if (!hasFilteredGravity) {
            values.copyInto(filteredGravity)
            hasFilteredGravity = true
            return
        }
        for (i in filteredGravity.indices) {
            filteredGravity[i] += GRAVITY_FILTER_ALPHA * (values[i] - filteredGravity[i])
        }
    }

    private fun setupToolbar() {
        toolbar?.apply {
            title = getString(R.string.ar_compass_title)
            setNavigationIcon(R.drawable.ic_arrow_back_24dp)
            navigationContentDescription = getString(R.string.back)
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun bindArSceneView() {
        arSceneView = findViewById(R.id.arCompassView)
        arSceneView.lifecycle = lifecycle
        if (arSceneView.viewNodeWindowManager == null) {
            arSceneView.viewNodeWindowManager = SceneView.createViewNodeManager(this)
        }
        arSceneView.onFrame = {
            updateMarkerFacing()
        }
    }

    private fun setupArCallbacks() {
        arSceneView.onSessionUpdated = { session, frame ->
            if (compassAnchor == null && frame.camera.trackingState == TrackingState.TRACKING) {
                gravityAlignedAnchorPose(frame.camera.displayOrientedPose)?.let { pose ->
                    compassAnchor = AnchorNode(
                        engine = arSceneView.engine,
                        anchor = session.createAnchor(pose)
                    ).also { anchorNode ->
                        arSceneView.addChildNode(anchorNode)
                    }
                    pendingBaselineHeading?.let { placeCompassPoints(it) }
                }
            }
        }
    }

    /**
     * The camera pose includes pitch and roll, so anchoring the ring to it directly tilts
     * the whole marker circle whenever the phone isn't level (e.g. pointing down at a desk
     * when tracking starts). Keep only the camera's position and horizontal yaw so the ring
     * stays level at the height where tracking began. Returns null while the camera points
     * too close to vertical for its yaw to be meaningful — the anchor then waits for a
     * later frame in which the phone is held more upright.
     */
    private fun gravityAlignedAnchorPose(cameraPose: Pose): Pose? {
        val zAxis = cameraPose.zAxis
        val rotation = ArCompassAnchorMath.gravityAlignedRotation(-zAxis[0], -zAxis[2])
            ?: return null
        return Pose(
            floatArrayOf(cameraPose.tx(), cameraPose.ty(), cameraPose.tz()),
            rotation
        )
    }

    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    private fun registerSensorListeners() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun placeCompassPoints(baselineHeading: Float) {
        if (compassPlaced) return
        val anchorNode = compassAnchor ?: return
        val windowManager = arSceneView.viewNodeWindowManager
            ?: SceneView.createViewNodeManager(this).also {
                arSceneView.viewNodeWindowManager = it
            }

        CompassPointDefinition.defaults.forEach { definition ->
            val markerNode = ViewNode2(
                engine = arSceneView.engine,
                windowManager = windowManager,
                materialLoader = arSceneView.materialLoader,
                viewLayoutRes = R.layout.view_ar_compass_marker,
                unlit = true
            ).apply {
                parent = anchorNode
                pxPerUnits = 900f
                viewSize = Size(340f, 380f)
            }

            val isNorth = definition.angle == 0f
            val accent = ContextCompat.getColor(
                this,
                when {
                    isNorth -> R.color.colorAccent
                    definition.isCardinal -> R.color.ar_compass_marker_cardinal
                    else -> R.color.ar_compass_marker_intercardinal
                }
            )
            markerNode.layout.findViewById<TextView>(R.id.markerLetter).apply {
                text = getString(definition.abbreviationRes)
                setTextColor(accent)
                contentDescription = getString(definition.labelRes)
            }
            markerNode.layout.findViewById<TextView>(R.id.markerDegrees).text = getString(
                R.string.ar_compass_heading_value,
                definition.angle.roundToInt()
            )
            markerNode.layout.findViewById<View>(R.id.markerDot).backgroundTintList =
                ColorStateList.valueOf(accent)

            val tierScale = when {
                isNorth -> 1.15f
                definition.isCardinal -> 1f
                else -> 0.8f
            }
            markerNode.scale = Float3(tierScale, tierScale, tierScale)

            val offsetRadians = Math.toRadians((pointAngle(definition, baselineHeading)).toDouble())
            markerNode.position = Float3(
                (sin(offsetRadians) * MARKER_DISTANCE_METERS).toFloat(),
                0f,
                (-cos(offsetRadians) * MARKER_DISTANCE_METERS).toFloat()
            )

            compassNodes += CompassPointNode(definition, markerNode)
        }

        compassPlaced = true
    }

    private fun handleHeadingUpdate(heading: Float) {
        currentHeading = heading
        headingValue.text = getString(
            R.string.ar_compass_heading_value,
            heading.roundToInt()
        )
        if (!compassPlaced) {
            pendingBaselineHeading = heading
            placeCompassPoints(heading)
        }
    }

    private fun updateMarkerFacing() {
        if (compassNodes.isEmpty()) return
        val cameraNode = arSceneView.cameraNode
        compassNodes.forEach { point ->
            point.markerNode.lookAt(cameraNode)
        }
    }

    private fun pointAngle(definition: CompassPointDefinition, baselineHeading: Float): Float {
        return definition.angle - baselineHeading
    }

    private data class CompassPointNode(
        val definition: CompassPointDefinition,
        val markerNode: ViewNode2
    )

    private data class CompassPointDefinition(
        val angle: Float,
        @StringRes val labelRes: Int,
        @StringRes val abbreviationRes: Int,
        val isCardinal: Boolean
    ) {
        companion object {
            val defaults = listOf(
                CompassPointDefinition(0f, R.string.ar_compass_north, R.string.compass_north, true),
                CompassPointDefinition(45f, R.string.ar_compass_northeast, R.string.compass_northeast, false),
                CompassPointDefinition(90f, R.string.ar_compass_east, R.string.compass_east, true),
                CompassPointDefinition(135f, R.string.ar_compass_southeast, R.string.compass_southeast, false),
                CompassPointDefinition(180f, R.string.ar_compass_south, R.string.compass_south, true),
                CompassPointDefinition(225f, R.string.ar_compass_southwest, R.string.compass_southwest, false),
                CompassPointDefinition(270f, R.string.ar_compass_west, R.string.compass_west, true),
                CompassPointDefinition(315f, R.string.ar_compass_northwest, R.string.compass_northwest, false)
            )
        }
    }

    companion object {
        private const val MARKER_DISTANCE_METERS = 1.35f

        private const val PREF_AR_COMPASS_FIRST_RUN = "AR_COMPASS_FIRST_RUN"
        private const val TILT_HINT_SHOW_DEGREES = 40.0
        private const val TILT_HINT_HIDE_DEGREES = 28.0
        private const val GRAVITY_FILTER_ALPHA = 0.15f
    }
}

internal object ArCompassDisplayAxes {
    fun forRotation(rotation: Int): Pair<Int, Int> = when (rotation) {
        Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
        Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
        Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
        else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
    }
}

internal object ArCompassAnchorMath {

    /**
     * Minimum length of the forward vector's horizontal projection (cos of the elevation
     * angle) for the yaw to be trusted; 0.5 admits cameras within 60° of the horizon.
     */
    private const val MIN_HORIZONTAL_FORWARD = 0.5f

    /**
     * Yaw-only rotation quaternion {x, y, z, w} that turns world -Z onto the horizontal
     * projection of the given camera-forward direction, or null when the camera points too
     * close to straight up or down for its yaw to be meaningful.
     */
    fun gravityAlignedRotation(forwardX: Float, forwardZ: Float): FloatArray? {
        val horizontal = sqrt(forwardX * forwardX + forwardZ * forwardZ)
        if (horizontal < MIN_HORIZONTAL_FORWARD) return null
        val yaw = atan2(-forwardX.toDouble(), -forwardZ.toDouble())
        return floatArrayOf(0f, sin(yaw / 2.0).toFloat(), 0f, cos(yaw / 2.0).toFloat())
    }
}
