package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.google.ar.core.Camera
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ARSceneView

/**
 * Helper class to render 2D text overlays for 3D AR objects
 */
class TextOverlayRenderer(private val arSceneView: ARSceneView) {

    private val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 42f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        style = Paint.Style.FILL
        setShadowLayer(8f, 0f, 0f, android.graphics.Color.BLACK)
    }

    private val backgroundPaint = Paint().apply {
        color = android.graphics.Color.argb(180, 0, 0, 0)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textBounds = Rect()

    /**
     * Project a 3D world position to 2D screen coordinates
     */
    fun worldToScreen(worldPosition: Float3, camera: Camera): android.graphics.PointF? {
        try {
            val viewMatrix = FloatArray(16)
            val projectionMatrix = FloatArray(16)
            camera.getViewMatrix(viewMatrix, 0)
            camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100f)

            // Transform world coordinates to view space
            val viewX: Float = viewMatrix[0] * worldPosition.x + viewMatrix[4] * worldPosition.y + viewMatrix[8] * worldPosition.z + viewMatrix[12]
            val viewY: Float = viewMatrix[1] * worldPosition.x + viewMatrix[5] * worldPosition.y + viewMatrix[9] * worldPosition.z + viewMatrix[13]
            val viewZ: Float = viewMatrix[2] * worldPosition.x + viewMatrix[6] * worldPosition.y + viewMatrix[10] * worldPosition.z + viewMatrix[14]
            val viewW: Float = viewMatrix[3] * worldPosition.x + viewMatrix[7] * worldPosition.y + viewMatrix[11] * worldPosition.z + viewMatrix[15]

            // Check if point is behind camera
            if (viewZ > 0f) {
                return null
            }

            // Transform view space to clip space
            val clipX: Float = projectionMatrix[0] * viewX + projectionMatrix[4] * viewY + projectionMatrix[8] * viewZ + projectionMatrix[12] * viewW
            val clipY: Float = projectionMatrix[1] * viewX + projectionMatrix[5] * viewY + projectionMatrix[9] * viewZ + projectionMatrix[13] * viewW
            val clipW: Float = projectionMatrix[3] * viewX + projectionMatrix[7] * viewY + projectionMatrix[11] * viewZ + projectionMatrix[15] * viewW

            // Check for division by zero
            if (kotlin.math.abs(clipW) < 0.00001f) {
                return null
            }

            // Normalize to NDC (Normalized Device Coordinates)
            val ndcX: Float = clipX / clipW
            val ndcY: Float = clipY / clipW

            // Check if point is within view frustum
            if (ndcX < -1f || ndcX > 1f || ndcY < -1f || ndcY > 1f) {
                return null
            }

            // Convert NDC to screen coordinates
            val screenX: Float = (ndcX + 1f) * 0.5f * arSceneView.width.toFloat()
            val screenY: Float = (1f - ndcY) * 0.5f * arSceneView.height.toFloat()

            return android.graphics.PointF(screenX, screenY)
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Draw text at a screen position with background
     */
    fun drawTextWithBackground(canvas: Canvas, text: String, x: Float, y: Float) {
        // Measure text bounds
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        // Draw background rectangle
        val padding = 16f
        val left = x - textBounds.width() / 2f - padding
        val right = x + textBounds.width() / 2f + padding
        val top = y + textBounds.top - padding
        val bottom = y + textBounds.bottom + padding

        val cornerRadius = 8f
        canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, backgroundPaint)

        // Draw text
        canvas.drawText(text, x, y, textPaint)
    }
}
