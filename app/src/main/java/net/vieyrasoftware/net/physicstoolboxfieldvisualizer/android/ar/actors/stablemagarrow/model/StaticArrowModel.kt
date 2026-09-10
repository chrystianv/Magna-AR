package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.actors.stablemagarrow.model

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.google.android.filament.LightManager
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.node.LightNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.JvmOverloads
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.QuaternionCompat
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.render.Model
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/**
 * StaticArrowModel migrated to SceneView.
 * Renders colored arrows showing magnetic field direction and strength.
 */
class StaticArrowModel @JvmOverloads constructor(
    private val pos: Vector3Compat,
    private val field: Vector3Compat,
    private val showNumbers: Boolean,
    private val scaleWithIntensity: Boolean = true
) : Model {

    // Nodes - created lazily when needed
    private var rootNode: Node? = null
    private var figureNode: ModelNode? = null
    private var textNode: Node? = null  // Node for text positioning
    private var lightNode: LightNode? = null

    private var intensity: Float = 0f
    private var strength: Float = 0f
    private var sceneView: ARSceneView? = null
    private var coloredMaterialInstance: com.google.android.filament.MaterialInstance? = null

    init {
        // Calculate strength and intensity
        strength = field.length()
        intensity = asIntensity(field)
    }

    private fun normalizedStrength(): Float {
        val normalized = (strength - MIN_FIELD_STRENGTH) / (MAX_FIELD_STRENGTH - MIN_FIELD_STRENGTH)
        return max(0f, min(normalized, 1f))
    }

    /**
     * Calculate intensity from field strength (sigmoid function)
     */
    private fun asIntensity(field: Vector3Compat): Float {
        val ranged = field.length() - 250f
        val sigmoidal = 1f / (1f + exp(-(ranged / 100f)))
        return max(min(sigmoidal, 1f), 0f)
    }

    /**
     * Calculate rotation between two vectors
     * Mimics Sceneform's Quaternion.rotationBetweenVectors
     */
    private fun calculateRotationBetweenVectors(from: Vector3Compat, to: Vector3Compat): QuaternionCompat {
        val fromNorm = from.normalized()
        val toNorm = to.normalized()

        val dot = fromNorm.dot(toNorm)

        // Check if vectors are parallel
        if (dot >= 0.999999f) {
            return QuaternionCompat.identity
        }

        // Check if vectors are opposite
        if (dot <= -0.999999f) {
            // Find any perpendicular vector
            val axis = if (kotlin.math.abs(fromNorm.x) < 0.9f) {
                Vector3Compat(1f, 0f, 0f).cross(fromNorm)
            } else {
                Vector3Compat(0f, 1f, 0f).cross(fromNorm)
            }.normalized()
            return QuaternionCompat.axisAngle(axis, 180f)
        }

        // Standard case
        val axis = fromNorm.cross(toNorm)
        val angle = kotlin.math.acos(dot) * 180f / kotlin.math.PI.toFloat()

        return QuaternionCompat.axisAngle(axis, angle)
    }

    fun setNumberVisibility(isVisible: Boolean) {
        textNode?.let { tNode ->
            if (isVisible) {
                if (tNode.parent == null) {
                    tNode.parent = rootNode
                }
            } else {
                if (tNode.parent != null) {
                    tNode.parent = null
                }
            }
        }
    }

    /**
     * Get the text node position in world space for overlay rendering
     */
    fun getTextNodeWorldPosition(): Float3? {
        return textNode?.worldPosition
    }

    /**
     * Get the text to display
     */
    fun getDisplayText(): String {
        return "${strength.toInt()} µT"
    }

    override fun node(): Node {
        // Return existing root node, or throw an error if loadModel hasn't been called yet
        return rootNode ?: throw IllegalStateException(
            "Node not initialized. Call loadModel(arSceneView) first. Position: $pos"
        )
    }

    /**
     * Get color based on intensity/strength
     * Uses a heat map gradient: blue (low) → yellow → orange → red (high)
     */
    private fun getColorForIntensity(): Int {
        val normalized = normalizedStrength()
        val hue = 0.66f - (normalized * 0.66f)
        val hsv = floatArrayOf(hue * 360f, 1f, 1f)
        return Color.HSVToColor(255, hsv)
    }

    /**
     * Load the 3D model and apply materials.
     *
     * This is a placeholder for compatibility with the Model interface.
     * Actual model loading happens in loadModel() which requires ARSceneView.
     *
     * @param ctx Context for loading assets
     */
    override fun loadAsset(ctx: Context) {
        // Ensure root node exists
        node()
        Log.d("StaticArrowModel", "loadAsset called - node created at $pos")
    }

    /**
     * Load the actual GLB model from an ARSceneView context
     * This should be called BEFORE loadAsset() to create the nodes first
     */
    fun loadModel(arSceneView: ARSceneView, scope: CoroutineScope) {
        this.sceneView = arSceneView

        // Create the root node synchronously so it's available immediately
        if (rootNode == null) {
            rootNode = Node(arSceneView.engine)
            rootNode?.position = pos.toFloat3()
            rootNode?.quaternion = QuaternionCompat.identity.toQuaternion()
            rootNode?.scale = Float3(1f, 1f, 1f)
        }

        if (lightNode == null) {
            lightNode = LightNode(
                engine = arSceneView.engine,
                type = LightManager.Type.POINT
            ) {
                intensity(3800f)
                falloff(0.25f)
                color(1.0f, 0.97f, 0.92f)
            }.also { node ->
                node.parent = rootNode
                node.position = Float3(0f, 0.015f, 0f)
                node.isShadowCaster = false
            }
        }

        // Create a node to mark where text should be displayed
        if (textNode == null) {
            createTextPositionNode(arSceneView)
        }

        // Load the actual model asynchronously
        scope.launch {
            try {
                // Load the GLB model
                val loadedModel = arSceneView.modelLoader.loadModelInstance("arrow.glb")

                if (loadedModel != null) {
                    // Create model node with the loaded instance
                    figureNode = ModelNode(modelInstance = loadedModel, autoAnimate = false)
                    figureNode?.parent = rootNode

                    // Calculate rotation to align with field direction
                    val fieldNormalized = field.normalized()
                    val upVector = Vector3Compat(0f, -1.0f, 0f)
                    val rotation = calculateRotationBetweenVectors(upVector, fieldNormalized)
                    figureNode?.quaternion = rotation.toQuaternion()

                    // Scale based on field strength
                    val normalized = normalizedStrength()
                    val intensityMultiplier = if (scaleWithIntensity) {
                        1f + (normalized * 2f)
                    } else {
                        STATIC_SCALE_MULTIPLIER
                    }
                    val baseScale = BASE_ARROW_SCALE
                    val targetScale = baseScale * intensityMultiplier
                    figureNode?.position = Float3(
                        fieldNormalized.x * targetScale * 0.5f,
                        fieldNormalized.y * targetScale * 0.5f,
                        fieldNormalized.z * targetScale * 0.5f
                    )
                    figureNode?.scale = Float3(
                        targetScale,
                        targetScale,
                        targetScale
                    )

                    // Apply color based on field strength using an opaque material instance
                    val color = getColorForIntensity()
                    coloredMaterialInstance = sceneView?.materialLoader?.createColorInstance(color, metallic = 0f, roughness = 0.15f, reflectance = 0.7f)
                    coloredMaterialInstance?.let { instance ->
                        figureNode?.setMaterialInstance(instance)
                    }

                    Log.d("StaticArrowModel", "Model loaded successfully with color strength: $strength")
                } else {
                    Log.e("StaticArrowModel", "Failed to load arrow.glb model")
                }
            } catch (e: Exception) {
                Log.e("StaticArrowModel", "Error loading GLB model", e)
            }
        }
    }

    /**
     * Create a simple node to mark where text should be displayed (position marker)
     * The actual text rendering will be done as a 2D overlay in the activity
     */
    private fun createTextPositionNode(arSceneView: ARSceneView) {
        try {
            val fieldNormalized = field.normalized()
            val normalized = normalizedStrength()
            val intensityMultiplier = if (scaleWithIntensity) {
                1f + (normalized * 2f)
            } else {
                STATIC_SCALE_MULTIPLIER
            }
            val baseScale = BASE_ARROW_SCALE
            val targetScale = baseScale * intensityMultiplier

            // Create a marker node positioned at the tip of the arrow
            textNode = Node(arSceneView.engine).apply {
                // Position at the arrow tip (slightly beyond)
                val offset = 1.15f  // 15% beyond the arrow tip
                position = Float3(
                    fieldNormalized.x * targetScale * offset,
                    fieldNormalized.y * targetScale * offset,
                    fieldNormalized.z * targetScale * offset
                )

                // Initially set visibility based on showNumbers
                if (showNumbers) {
                    parent = rootNode
                } else {
                    parent = null
                }
            }

            Log.d("StaticArrowModel", "Created text position node at arrow tip")
        } catch (e: Exception) {
            Log.e("StaticArrowModel", "Error creating text position node", e)
        }
    }

    companion object {
        private const val MIN_FIELD_STRENGTH = 25f
        private const val MAX_FIELD_STRENGTH = 500f
        private const val BASE_ARROW_SCALE = 0.008f
        private const val STATIC_SCALE_MULTIPLIER = 1.5f
    }

}
