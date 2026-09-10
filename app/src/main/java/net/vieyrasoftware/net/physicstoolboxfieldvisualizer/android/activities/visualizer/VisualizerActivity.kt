package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.BuildConfig
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ui.dialog.appDialogBuilder
import com.google.android.material.button.MaterialButton
import com.google.android.filament.Scene
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.collision.HitResult
import io.github.sceneview.node.Node
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.settings.SettingsActivity
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.actors.stablemagarrow.model.StaticArrowModel
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.FieldGroup
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.ArrowTextOverlay
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.HeatmapGradientDrawable
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.Field
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.render.Filter
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.CommonUtils
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.CompactNotificationHelper
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.DisplayRotationHelper
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.PermissionHelper
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers.TrackingStateHelper
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import java.io.File
import java.io.FileOutputStream
import java.util.EnumSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sqrt

// SceneView imports - migrated from Sceneform
// Note: uppercase AR
// Filament Scene, not SceneView Scene
// import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.filters.gridarrow.GridFilter; // Removed - GridFilter not migrated
class VisualizerActivity : AppCompatActivity() {
    var high: TextView? = null
    var low: TextView? = null

    var heatmapImageView: ImageView? = null
    var isAddingArrows: Int = 0
    private val automaticArrowHandler = Handler(Looper.getMainLooper())
    private val automaticArrowLoop = RepeatingActionScheduler(
        intervalMillis = AUTOMATIC_ARROW_INTERVAL_MILLIS,
        postDelayed = automaticArrowHandler::postDelayed,
        removeCallbacks = automaticArrowHandler::removeCallbacks,
        action = ::createTempArrow
    )

    var counterTap: Int = 0
    var readout: Boolean = true
    var heatmap: Boolean = false

    private val mDisplayRotationHelper: DisplayRotationHelper? = null

    private val accuracy = 3

    // Handles
    private var mSensorManager: SensorManager? = null
    private val toggleNumbers: OnSharedPreferenceChangeListener? = null

    // Data

    // Data groups
    private var recorder: MagnetometerData? = null
    private var fields: FieldGroup? = null
    private var scene: Scene? = null
    private val stableArrows = ArrayList<StaticArrowModel>()

    // Views & Fragments
    private val fieldTextViews: Array<TextView?>? = arrayOfNulls<TextView>(4)
    private var arSceneView: ARSceneView? = null // Migrated from ArFragment
    private var arrowTextOverlay: ArrowTextOverlay? = null  // Text overlay for arrows
    private var infoButton: MaterialButton? = null
    private var settingsButton: MaterialButton? = null
    private var restartButton: MaterialButton? = null
    private var compassButton: MaterialButton? = null
    private var snapshotButton: MaterialButton? = null
    private var onboardingOverlay: View? = null
    private var statusIndicatorCard: View? = null
    private var statusText: TextView? = null
    private var statusDot: View? = null

    private var toolbar: Toolbar? = null

    private var blurlayout: View? = null
    private var redlayout: View? = null
    private var greenlayout: View? = null
    private var bluelayout: View? = null

    private var xtextview: TextView? = null
    private var ytextview: TextView? = null
    private var ztextview: TextView? = null

    private var xyzComponentsContainer: View? = null

    var globalReference: ConstraintLayout? = null

    var launch: Int = 0

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this@VisualizerActivity,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@VisualizerActivity,
                arrayOf<String>(Manifest.permission.CAMERA),
                PackageManager.PERMISSION_GRANTED
            )
        }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Log screen view

        // Check if device is compatible with ARCore and SceneKit
        if (!CommonUtils.checkIsSupportedDeviceOrFinish(this@VisualizerActivity)) {
            return
        }
        // ARCore requires camera permissions to operate.
        if (!PermissionHelper.hasCameraPermission(this)) {
            startActivity(Intent(this, LaunchScreenActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_visualizer)
        bindViews()
        setupToolbar()
        mSensorManager = getApplicationContext().getSystemService(SENSOR_SERVICE) as SensorManager?
        recorder = MagnetometerData()
        bindRecorderData()
        setListeners()
        showTopLesson()
    }

    /**
     * Bind MutableLiveData elements to various ui elements.
     */
    private fun bindRecorderData() {
        val fieldStringIds = intArrayOf(
            R.string.x_mag_readout,
            R.string.y_mag_readout,
            R.string.z_mag_readout,
            R.string.total_mag_readout
        )

        if (fieldTextViews != null) {
            recorder!!.field.observe(this, Observer { f: FloatArray? ->
                if (f!!.size >= 3) {
                    recorder!!.x = f[0]
                    recorder!!.y = f[1]
                    recorder!!.z = f[2]
                }
                var i = 0
                while (i < fieldTextViews.size && i < fieldStringIds.size) {
                    val fieldTextView = fieldTextViews[i]

                    if (fieldTextView != null && i < f.size) {
                        fieldTextView.setText(getResources().getString(fieldStringIds[i], f[i]))
                    }
                    ++i
                }
            })
        }
    }


    /**
     * Grab handles for Views.
     */
    private fun bindViews() {
        val magneticFieldIds = intArrayOf(
            R.id.xTextView,
            R.id.yTextView,
            R.id.zTextView,
            R.id.TotalMagneticTextView,
        )
        for (i in magneticFieldIds.indices) {
            fieldTextViews!![i] = findViewById<TextView?>(magneticFieldIds[i])
        }
        infoButton = findViewById<MaterialButton>(R.id.infoButton)
        settingsButton = findViewById<MaterialButton>(R.id.settingsButton)
        restartButton = findViewById<MaterialButton>(R.id.restartButton)
        toolbar = findViewById<Toolbar?>(R.id.toolbar)
        blurlayout = findViewById<View>(R.id.blurLayout)
        compassButton = findViewById<MaterialButton>(R.id.compassButton)
        snapshotButton = findViewById<MaterialButton>(R.id.snapshotButton)
        onboardingOverlay = findViewById<View>(R.id.magnaOnboardingOverlay)
        redlayout = findViewById<View>(R.id.redblur)
        greenlayout = findViewById<View>(R.id.greenblur)
        bluelayout = findViewById<View>(R.id.blueblur)
        xtextview = findViewById<TextView>(R.id.xtextview)
        ytextview = findViewById<TextView>(R.id.ytextview)
        ztextview = findViewById<TextView>(R.id.ztextview)
        xyzComponentsContainer = findViewById<View>(R.id.xyzComponentsContainer)
        heatmapImageView = findViewById<ImageView>(R.id.heatmapImage)
        high = findViewById<TextView>(R.id.hightTextViewField)
        low = findViewById<TextView>(R.id.lowTextViewField)

        // Set procedurally generated gradient instead of static image
        heatmapImageView?.setImageDrawable(HeatmapGradientDrawable())
        globalReference = findViewById<View?>(R.id.constraintLayoutMain) as ConstraintLayout


        // Status indicator views
        statusIndicatorCard = findViewById<View?>(R.id.statusIndicatorCard)
        statusText = findViewById<TextView?>(R.id.statusText)
        statusDot = findViewById<View?>(R.id.statusDot)


        // 3D Views
        bindAr()
    }

    /**
     * Grab handles for Renderables.
     */
    private fun bindAr() {
        arSceneView = findViewById<ARSceneView?>(R.id.vr_fragment)
        if (arSceneView != null) {
            arSceneView!!.lifecycle = lifecycle
        }

        // Setup arrow text overlay
        arrowTextOverlay = findViewById<ArrowTextOverlay?>(R.id.arrowTextOverlay)
        if (arrowTextOverlay != null && arSceneView != null) {
            arrowTextOverlay!!.setARSceneView(arSceneView!!)
            arrowTextOverlay!!.setTextVisibility(shouldHaveNumbers())

            // Set up frame update callback to redraw overlay
            arSceneView!!.onFrame = { frameTime ->
                arrowTextOverlay?.invalidate()
            }
        }
    }

    /**
     * Setup toolbar with back navigation
     */
    private fun setupToolbar() {
        toolbar?.apply {
            title = getString(R.string.magna_ar)
            setNavigationIcon(R.drawable.ic_arrow_back_24dp)
            navigationContentDescription = getString(R.string.back)
            setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }


    /**
     * Set listeners for arSceneView.
     */
    private fun setListeners() {
        // TODO: create actual interface to avoid these commenting shenanigans
        //     Handle plane tap events differently with SceneView
        if (arSceneView != null) {
            arSceneView!!.onTouchEvent = { motionEvent: MotionEvent?, hitResult: HitResult? ->
                handleSceneTouch(
                    motionEvent,
                    hitResult
                )
            }
        }

        restartButton!!.setOnClickListener(View.OnClickListener { v: View? -> restartActivity() })
        settingsButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(
                    getApplicationContext(),
                    SettingsActivity::class.java
                )
            )
        })

        infoButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            isAddingArrows = 1 - isAddingArrows
            // Update checkable state for Material 3 button - this automatically changes the background color
            infoButton!!.isChecked = (isAddingArrows == 1)

            if (isAddingArrows == 1) {
                // Only fires when toggling ON — preserved intentionally, not a per-toggle event.

                automaticArrowLoop.start()
                showStatusIndicator(getString(R.string.adding_vectors_status))
                CompactNotificationHelper.showSuccess(
                    this@VisualizerActivity,
                    getString(R.string.tap_vectors)
                )
            } else {
                automaticArrowLoop.stop()
                hideStatusIndicator()
                CompactNotificationHelper.showShort(
                    this@VisualizerActivity,
                    getString(R.string.stopped_adding_vectors)
                )
            }
        })

        compassButton!!.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(
                    getApplicationContext(),
                    ARCompassActivity::class.java
                )
            )
        })

        snapshotButton!!.setOnClickListener {
            captureAndShareSnapshot()
        }

        blurlayout!!.setOnClickListener(View.OnClickListener { v: View? ->
            counterTap++
            val visibility = if (counterTap % 2 == 1) View.VISIBLE else View.GONE

            // Toggle XYZ components container visibility
            xyzComponentsContainer?.setVisibility(visibility)

            if (visibility == View.GONE) {
                counterTap = 0
            }
        })
    }

    private fun captureAndShareSnapshot() {
        val sceneView = arSceneView ?: return
        val root = globalReference ?: return
        if (sceneView.width <= 0 || sceneView.height <= 0) {
            showSnapshotError()
            return
        }

        val chrome = listOfNotNull(
            settingsButton,
            restartButton,
            compassButton,
            snapshotButton,
            infoButton,
            onboardingOverlay,
        )
        val previousVisibilities = chrome.associateWith { it.visibility }
        chrome.forEach { it.visibility = View.INVISIBLE }
        snapshotButton?.isEnabled = false

        val restoreSnapshotChrome = {
            previousVisibilities.forEach { (view, visibility) ->
                view.visibility = visibility
            }
            snapshotButton?.isEnabled = true
        }

        // ARSceneView renders on a SurfaceView. Copy that surface directly so the camera and
        // Filament content are present, then draw the Android measurement overlays on top.
        root.postOnAnimation {
            val bitmap = Bitmap.createBitmap(
                sceneView.width,
                sceneView.height,
                Bitmap.Config.ARGB_8888,
            )

            try {
                PixelCopy.request(
                    sceneView,
                    bitmap,
                    { result ->
                        if (result == PixelCopy.SUCCESS) {
                            try {
                                drawSnapshotOverlays(root, sceneView, bitmap)
                            } catch (error: Exception) {
                                restoreSnapshotChrome()
                                bitmap.recycle()
                                Log.e(TAG, "Unable to draw Magna-AR snapshot overlays", error)
                                showSnapshotError()
                                return@request
                            }
                        }

                        restoreSnapshotChrome()

                        if (result == PixelCopy.SUCCESS) {
                            lifecycleScope.launch {
                                try {
                                    val snapshot = withContext(Dispatchers.IO) {
                                        writeSnapshot(bitmap)
                                    }
                                    bitmap.recycle()
                                    // The legacy raw "snapshot captured" analytics event that
                                    // used to guard this success path was deleted (Task 8,
                                    // analytics phase 4): confirmed live duplicate of the
                                    // call inside shareSnapshot(), which already fires on this
                                    // identical success path immediately before the same
                                    // startActivity(...) chooser call.
                                    shareSnapshot(snapshot)
                                } catch (error: Exception) {
                                    if (!bitmap.isRecycled) bitmap.recycle()
                                    Log.e(TAG, "Unable to write Magna-AR snapshot", error)
                                    showSnapshotError()
                                }
                            }
                        } else {
                            bitmap.recycle()
                            Log.e(TAG, "PixelCopy failed with result $result")
                            showSnapshotError()
                        }
                    },
                    Handler(Looper.getMainLooper()),
                )
            } catch (error: IllegalArgumentException) {
                restoreSnapshotChrome()
                bitmap.recycle()
                Log.e(TAG, "Unable to capture Magna-AR surface", error)
                showSnapshotError()
            }
        }
    }

    private fun drawSnapshotOverlays(
        root: ConstraintLayout,
        sceneView: ARSceneView,
        snapshot: Bitmap,
    ) {
        val overlays = Bitmap.createBitmap(
            sceneView.width,
            sceneView.height,
            Bitmap.Config.ARGB_8888,
        )
        val previousBackground = root.background

        try {
            root.background = null
            Canvas(overlays).apply {
                translate(-sceneView.left.toFloat(), -sceneView.top.toFloat())
                root.draw(this)
            }
            Canvas(snapshot).drawBitmap(overlays, 0f, 0f, null)
        } finally {
            root.background = previousBackground
            overlays.recycle()
        }
    }

    private fun writeSnapshot(bitmap: Bitmap): File {
        val snapshotDirectory = File(cacheDir, SNAPSHOT_DIRECTORY).apply { mkdirs() }
        snapshotDirectory.listFiles()?.forEach { it.delete() }
        val snapshot = File(snapshotDirectory, "Magna-AR-${System.currentTimeMillis()}.png")
        FileOutputStream(snapshot).use { output ->
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                "Bitmap encoder returned no snapshot data"
            }
        }
        return snapshot
    }

    // Returns whether the share intent actually launched. false means resolving the
    // FileProvider URI failed: showSnapshotError(reason = "file_provider") already reported it
    // (toast + the one accurate app_error), so the caller skips its "captured" bookkeeping for
    // this outcome without also needing to catch an exception here — that would have routed
    // through the caller's generic catch too, which calls showSnapshotError() with its default
    // reason, double-reporting the same single failure. A thrown exception from building the
    // intent or from startActivity below is NOT caught here, matching CsvSharing's shape: it
    // still propagates to the caller's own catch, which still calls showSnapshotError() with
    // the default "file_missing" reason — unchanged from before this fix.
    private fun shareSnapshot(snapshot: File): Boolean {
        val uri = try {
            FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.provider",
                snapshot,
            )
        } catch (e: IllegalArgumentException) {
            showSnapshotError(reason = "file_provider")
            return false
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.magna_ar_share_snapshot))
            clipData = ClipData.newRawUri(getString(R.string.magna_ar_share_snapshot), uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        // Emitted once the intent is validly built for a real file, before startActivity —
        // matching CsvSharing's ordering, so file_shared means the same thing (a valid share
        // intent was handed to the OS for a file that exists) at every call site in the app.
        startActivity(
            Intent.createChooser(
                shareIntent,
                getString(R.string.magna_ar_share_snapshot),
            ),
        )
        return true
    }

    // reason defaults to "file_missing", accurate for every caller except shareSnapshot's own
    // FileProvider catch, which knows better and passes "file_provider" explicitly. This keeps
    // showSnapshotError() the single place that reports a snapshot failure — never two callers
    // emitting two app_error events for what is, from the user's perspective, one failure.
    private fun showSnapshotError(reason: String = "file_missing") {
        CompactNotificationHelper.showWarning(
            this,
            getString(R.string.magna_ar_snapshot_failed),
        )
    }

    /* ****************************** Get settings ******************************** */
    private fun shouldHaveNumbers(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("numerical", false)
    }

    private fun handleSceneTouch(motionEvent: MotionEvent?, hitResult: HitResult?): Boolean {
        if (motionEvent == null || arSceneView == null) {
            return false
        }
        val action = motionEvent.getActionMasked()
        Log.d(TAG, "handleSceneTouch action=" + action)
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_POINTER_DOWN || action == MotionEvent.ACTION_POINTER_UP) {
            return true
        }
        if (action == MotionEvent.ACTION_UP) {
            createTempArrow(motionEvent.getX(), motionEvent.getY())
            return true
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            return true
        }
        return false
    }

    private fun createTempArrow(xPx: Float, yPx: Float) {
        if (arSceneView == null) {
            Log.w(TAG, "ARSceneView not ready yet, ignoring tap.")
            return
        }

        val ses: Session? = arSceneView!!.session
        if (ses == null) {
            Log.w(TAG, "AR session not ready yet, ignoring tap.")
            return
        }

        val frame = arSceneView!!.frame
        if (frame == null) {
            Log.w(TAG, "AR frame not available yet, ignoring tap.")
            return
        }
        val cam = frame.getCamera()
        if (cam.getTrackingState() == TrackingState.TRACKING) {
            Log.d(TAG, "Camera tracking; attempting to place model")
            val desiredDistance = 0.05f // 5 cm from camera
            var planeHit: com.google.ar.core.HitResult? = null
            try {
                val planeTypes: Set<Plane.Type> = EnumSet.of(
                    Plane.Type.HORIZONTAL_UPWARD_FACING,
                    Plane.Type.HORIZONTAL_DOWNWARD_FACING,
                    Plane.Type.VERTICAL
                )
                val trackingStates: Set<TrackingState> = EnumSet.of(
                    TrackingState.TRACKING
                )
                val orientationModes: Set<Point.OrientationMode> =
                    EnumSet.of(
                        Point.OrientationMode.ESTIMATED_SURFACE_NORMAL
                    )
                planeHit = arSceneView!!.hitTestAR(
                    xPx,
                    yPx,
                    planeTypes,
                    false,
                    false,
                    false,
                    trackingStates,
                    orientationModes,
                    true,
                    null,
                    null
                )
            } catch (e: Exception) {
                Log.w(TAG, "hitTestAR failed, falling back to camera-relative placement", e)
            }
            val cameraPose = cam.getDisplayOrientedPose()
            // init empty fields + scene if null
            if (scene == null) {
                checkNotNull(ses)
                // Create AnchorNode with SceneView
                val anchorNode = AnchorNode(
                    arSceneView!!.engine,
                    ses.createAnchor(Pose.IDENTITY),
                    null,  // onTrackingStateChanged
                    null,  // onPoseChanged
                    null,  // onAnchorChanged
                    null // onUpdated
                )
                scene = arSceneView!!.scene
                fields = FieldGroup(anchorNode, object : Filter {
                    // Create Node with SceneView
                    var n: Node = Node(arSceneView!!.engine, -1)

                    override fun update(fields: MutableList<Field?>, root: Node) {}

                    override fun node(): Node {
                        return n
                    }

                    override fun loadAsset(ctx: Context?) {}
                })
                // Add anchor node to the SceneView's child nodes
                anchorNode.parent = null // Root level node
                arSceneView!!.addChildNode(anchorNode)
            }
            var currentMag = recorder!!.field.getValue()
            if (currentMag != null) {
                currentMag = cameraPose.rotateVector(currentMag)
                val mag = Vector3Compat(currentMag[0], currentMag[1], currentMag[2])
                if (mag.length() > 1e-9) {
                    val translation: FloatArray
                    val desiredTranslation = cameraPose
                        .compose(
                            Pose.makeTranslation(0f, 0f, -desiredDistance)
                        ).extractTranslation().getTranslation()
                    if (planeHit != null) {
                        val hitPose = planeHit.getHitPose()
                        val hitTranslation = hitPose.getTranslation()
                        val cameraTranslation = cameraPose.getTranslation()
                        val dx = (hitTranslation[0] - cameraTranslation[0]).toDouble()
                        val dy = (hitTranslation[1] - cameraTranslation[1]).toDouble()
                        val dz = (hitTranslation[2] - cameraTranslation[2]).toDouble()
                        val distance = sqrt(dx * dx + dy * dy + dz * dz)
                        if (distance > desiredDistance * 1.5 || distance < desiredDistance * 0.5) {
                            translation = desiredTranslation
                            Log.d(
                                TAG,
                                "Plane hit too far (" + distance + "m), clamping to desired distance."
                            )
                        } else {
                            translation = hitTranslation
                            Log.d(
                                TAG,
                                "Plane hit at (" + translation[0] + ", " + translation[1] + ", " + translation[2] + ")"
                            )
                        }
                    } else {
                        translation = desiredTranslation
                        Log.d(
                            TAG,
                            "Fallback placement at (" + translation[0] + ", " + translation[1] + ", " + translation[2] + ")"
                        )
                    }
                    val arrowModel = StaticArrowModel(
                        Vector3Compat(translation[0], translation[1], translation[2]),
                        mag, shouldHaveNumbers()
                    )
                    stableArrows.add(arrowModel)
                    arrowTextOverlay?.addArrowModel(arrowModel)


                    Log.d(
                        TAG,
                        "Loading arrow model assets at position: " + translation[0] + "," + translation[1] + "," + translation[2]
                    )
                    arrowModel.loadModel(arSceneView!!, lifecycleScope)

                    try {
                        arrowModel.loadAsset(getApplicationContext())
                        fields!!.addModel(arrowModel)
                        hideTopLesson()
                        Log.d(
                            TAG,
                            "Models tracked: arrows=" + stableArrows.size
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load/add model", e)
                    }
                } else {
                    Log.d(TAG, "Magnetometer magnitude too small: " + mag.length())
                }
            } else {
                Log.d(TAG, "Magnetometer data not yet available")
            }
        }

        // If not tracking, don't draw 3D objects, show tracking failure reason instead.
        if (cam.getTrackingState() == TrackingState.PAUSED) {
            println("Not running because tracking is paused.")
            CompactNotificationHelper.showWarning(
                this@VisualizerActivity,
                TrackingStateHelper.getTrackingFailureReasonString(this, cam)
            )
        }
    }

    private fun createTempArrow() {
        if (arSceneView == null) {
            Log.w(TAG, "ARSceneView not ready yet, ignoring tap.")
            return
        }
        createTempArrow(arSceneView!!.getWidth() / 2.0f, arSceneView!!.getHeight() / 2.0f)
    }

    private fun showTopLesson() {
        onboardingOverlay?.apply {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = -12f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .start()
        }
        // Deliberately toolAction, not tutorialStarted — this hint fires on every onCreate with
        // no first-run gate, unlike the app's other (first-run-gated) tutorial pairs.
    }

    private fun hideTopLesson() {
        val overlay = onboardingOverlay ?: return
        if (overlay.visibility != View.VISIBLE) return

        overlay.animate()
            .alpha(0f)
            .translationY(-12f)
            .setDuration(350)
            .withEndAction {
                overlay.visibility = View.GONE
                overlay.translationY = 0f
            }
            .start()
        // Deliberately toolAction, not tutorialCompleted — see showTopLesson()'s note above.
    }

    /* **************************** Scene Interaction ***************************** */
    /**
     * Update per frame things here.
     * TODO: SceneView doesn't use FrameTime - need to implement alternative update mechanism
     */
    // private void onUpdate() {
    //     // TODO: if nothing else is needed here, delete this.
    //     fields.step(null);
    //     View contentView = findViewById(android.R.id.content);
    // }
    private fun toggleRecord(v: View?, event: MotionEvent): Boolean {
        if (event.getActionButton() == MotionEvent.ACTION_BUTTON_RELEASE) {
            recorder!!.toggleState()
            return true
        }
        return false
    }

    /* *********************** Additional lifetime methods ************************ */
    public override fun onResume() {
        super.onResume()
        if (isFinishing || recorder == null) return

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // The AR camera feed is live for as long as this screen is
            // resumed with permission granted, so hold the screen awake —
            // mirrors KeepScreenOnEffect's Compose equivalent for the
            // rest of the app's camera/AR tools.
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            try {
                bindViews()
                if (mSensorManager == null) {
                    mSensorManager =
                        getApplicationContext().getSystemService(SENSOR_SERVICE) as SensorManager?
                }

                mSensorManager!!.unregisterListener(recorder)

                mSensorManager!!.registerListener(
                    recorder,
                    mSensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    SensorManager.SENSOR_DELAY_NORMAL
                )

                if (arSceneView != null) {
                    // TODO: hiding the plane discovery with SceneView
                    // SceneView handles plane rendering differently
                    // arSceneView.planeRenderer.isEnabled = false
                }

                // Update number visibility for all arrows
                val showNumbers = shouldHaveNumbers()
                for (sam in this.stableArrows) {
                    sam.setNumberVisibility(showNumbers)
                }

                // Update text overlay visibility
                arrowTextOverlay?.setTextVisibility(showNumbers)

                val appPreferences = PreferenceManager
                    .getDefaultSharedPreferences(this)

                readout = appPreferences.getBoolean("readout", true)

                heatmap = appPreferences.getBoolean("heatmap_switch", false)

                if (!readout) {
                    blurlayout!!.setVisibility(View.INVISIBLE)
                    for (i in 0..3) {
                        fieldTextViews!![i]!!.setVisibility(View.INVISIBLE)
                    }
                } else {
                    blurlayout!!.setVisibility(View.VISIBLE)
                }

                val heatmapVisibility = if (heatmap) View.VISIBLE else View.INVISIBLE

                heatmapImageView!!.setVisibility(heatmapVisibility)
                low!!.setVisibility(heatmapVisibility)
                high!!.setVisibility(heatmapVisibility)

                if (isAddingArrows == 1) {
                    automaticArrowLoop.start()
                }
            } catch (e: com.google.ar.core.exceptions.FatalException) {
                // ARCore encountered a fatal error during resume
                Log.e(TAG, "ARCore FatalException in onResume - this may be device-specific", e)

                // Show a user-friendly error dialog
                showArCoreErrorDialog()
            } catch (e: Exception) {
                // Catch any other unexpected exceptions
                Log.e(TAG, "Unexpected exception in onResume", e)

                // Show a generic error dialog
                showGenericErrorDialog()
            }
        } else {
            requestPermissions() // permissions are not granted yet, request them
        }
    }

    public override fun onPause() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        automaticArrowLoop.stop()
        mSensorManager?.unregisterListener(recorder)
        recorder?.setState(false)
        super.onPause()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun restartActivity() {

        // Clear the text overlay
        arrowTextOverlay?.clearArrows()

        val intent = getIntent()
        finish()
        startActivity(intent)
    }


    /**
     * Show the status indicator with a message
     */
    private fun showStatusIndicator(message: String?) {
        if (statusIndicatorCard != null && statusText != null) {
            statusText!!.setText(message)
            statusIndicatorCard!!.setVisibility(View.VISIBLE)
            statusIndicatorCard!!.setAlpha(0f)
            statusIndicatorCard!!.setTranslationY(20f) // Start from below
            statusIndicatorCard!!.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start()


            // Animate the pulsing dot
            if (statusDot != null) {
                statusDot!!.animate()
                    .alpha(0.3f)
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(600)
                    .withEndAction(object : Runnable {
                        override fun run() {
                            if (statusDot != null && statusIndicatorCard!!.getVisibility() == View.VISIBLE) {
                                statusDot!!.animate()
                                    .alpha(1f)
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(600)
                                    .withEndAction(this)
                                    .start()
                            }
                        }
                    })
                    .start()
            }
        }
    }

    /**
     * Hide the status indicator
     */
    private fun hideStatusIndicator() {
        if (statusIndicatorCard != null) {
            statusIndicatorCard!!.animate()
                .alpha(0f)
                .translationY(20f) // Slide down when hiding
                .setDuration(300)
                .withEndAction(object : Runnable {
                    override fun run() {
                        if (statusIndicatorCard != null) {
                            statusIndicatorCard!!.setVisibility(View.GONE)
                        }
                    }
                })
                .start()
        }
    }

    /**
     * Show error dialog when ARCore encounters a FatalException
     */
    private fun showArCoreErrorDialog() {
        appDialogBuilder(this)
            .setTitle(R.string.arcore_error_title)
            .setMessage(R.string.arcore_error_message)
            .setPositiveButton(R.string.retry) { dialog, _ ->
                dialog.dismiss()
                restartActivity()
            }
            .setNegativeButton(R.string.close) { dialog, _ ->
                dialog.dismiss()
                // Navigate back to main activity instead of just finishing
                navigateBackToMainActivity()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show generic error dialog for unexpected exceptions
     */
    private fun showGenericErrorDialog() {
        appDialogBuilder(this)
            .setTitle(R.string.error_title)
            .setMessage(R.string.error_message)
            .setPositiveButton(R.string.retry) { dialog, _ ->
                dialog.dismiss()
                restartActivity()
            }
            .setNegativeButton(R.string.close) { dialog, _ ->
                dialog.dismiss()
                // Navigate back to main activity instead of just finishing
                navigateBackToMainActivity()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Navigate back to the main activity (ToolsNavigationFragment)
     */
    private fun navigateBackToMainActivity() {
        try {
            // Finish this activity and return to the main activity
            finish()

            // Start the main activity if needed (in case it was destroyed)
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.let {
                it.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating back to main activity", e)
            // Fallback: just finish the activity
            finish()
        }
    }

    companion object {
        private const val TAG = "VisualizerActivity"
        private const val SNAPSHOT_DIRECTORY = "magna_ar_snapshots"
        private const val AUTOMATIC_ARROW_INTERVAL_MILLIS = 500L

        // sites per spec §9.
        private const val ACTION_AUTOMATIC_ARROWS_TOGGLED = "automatic_arrows_toggled"
        private const val ACTION_VECTOR_ADDED = "vector_added"
        private const val ACTION_TOP_LESSON_SHOWN = "top_lesson_shown"
        private const val ACTION_TOP_LESSON_HIDDEN = "top_lesson_hidden"
        private const val ACTION_RESET = "reset"

        // automatic_arrows_toggled's detail value (only fires when toggling ON, see call site).
        private const val DETAIL_ON = "on"
    }
}
