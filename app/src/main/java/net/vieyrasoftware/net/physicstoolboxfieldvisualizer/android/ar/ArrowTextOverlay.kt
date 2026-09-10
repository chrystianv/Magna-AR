package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import io.github.sceneview.ar.ARSceneView
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.actors.stablemagarrow.model.StaticArrowModel

/**
 * Overlay view that renders text labels for AR arrows
 */
class ArrowTextOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var arSceneView: ARSceneView? = null
    private var textRenderer: TextOverlayRenderer? = null
    private val arrowModels = mutableListOf<StaticArrowModel>()
    private var showText = false

    fun setARSceneView(sceneView: ARSceneView) {
        this.arSceneView = sceneView
        this.textRenderer = TextOverlayRenderer(sceneView)
    }

    fun addArrowModel(model: StaticArrowModel) {
        if (!arrowModels.contains(model)) {
            arrowModels.add(model)
            invalidate()
        }
    }

    fun removeArrowModel(model: StaticArrowModel) {
        arrowModels.remove(model)
        invalidate()
    }

    fun clearArrows() {
        arrowModels.clear()
        invalidate()
    }

    fun setTextVisibility(visible: Boolean) {
        showText = visible
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!showText) {
            return
        }

        val sceneView = arSceneView ?: return
        val renderer = textRenderer ?: return
        val frame = sceneView.frame ?: return
        val camera = frame.camera

        // Draw text for each arrow
        for (model in arrowModels) {
            val worldPos = model.getTextNodeWorldPosition() ?: continue
            val screenPos = renderer.worldToScreen(worldPos, camera) ?: continue

            // Draw the text
            renderer.drawTextWithBackground(
                canvas,
                model.getDisplayText(),
                screenPos.x,
                screenPos.y
            )
        }
    }
}
