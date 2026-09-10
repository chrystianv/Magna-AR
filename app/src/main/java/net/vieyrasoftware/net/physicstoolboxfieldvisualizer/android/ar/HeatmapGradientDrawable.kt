package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar

import android.graphics.*
import android.graphics.drawable.Drawable

/**
 * Procedurally generated heatmap gradient drawable.
 * Creates a smooth, visually pleasing gradient that matches the color scheme
 * used in StaticArrowModel.getColorForIntensity().
 *
 * Color progression (top to bottom):
 * - Red (high intensity, hue 0°)
 * - Orange (hue ~30°)
 * - Yellow (hue 60°)
 * - Green (hue 120°)
 * - Cyan (hue 180°)
 * - Blue (low intensity, hue 237°)
 */
class HeatmapGradientDrawable : Drawable() {

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.argb(60, 255, 255, 255) // Subtle white border
    }
    private var gradientShader: LinearGradient? = null
    private val cornerRadius = 24f // Rounded corners for a modern look

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        if (bounds.isEmpty) return

        // Create gradient if not already created or if bounds changed
        if (gradientShader == null || bounds.width() > 0 && bounds.height() > 0) {
            createGradient(bounds)
        }

        // Draw rounded rectangle with gradient
        gradientPaint.shader = gradientShader
        canvas.drawRoundRect(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.bottom.toFloat(),
            cornerRadius,
            cornerRadius,
            gradientPaint
        )

        // Draw subtle border for a polished look
        canvas.drawRoundRect(
            bounds.left + 1f,
            bounds.top + 1f,
            bounds.right - 1f,
            bounds.bottom - 1f,
            cornerRadius,
            cornerRadius,
            borderPaint
        )
    }

    private fun createGradient(bounds: Rect) {
        // Define color stops for a smooth HSV-based gradient
        // This matches the formula: hue = 0.66f - (normalized * 0.66f)
        // where normalized goes from 0 (low) to 1 (high)

        val colors = intArrayOf(
            Color.HSVToColor(floatArrayOf(0f, 1f, 1f)),        // Red (high)
            Color.HSVToColor(floatArrayOf(30f, 1f, 1f)),       // Orange
            Color.HSVToColor(floatArrayOf(60f, 1f, 1f)),       // Yellow
            Color.HSVToColor(floatArrayOf(120f, 1f, 1f)),      // Green
            Color.HSVToColor(floatArrayOf(180f, 1f, 1f)),      // Cyan
            Color.HSVToColor(floatArrayOf(237f, 1f, 1f))       // Blue (low)
        )

        // Evenly distributed positions
        val positions = floatArrayOf(0f, 0.2f, 0.4f, 0.6f, 0.8f, 1f)

        gradientShader = LinearGradient(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.left.toFloat(),
            bounds.bottom.toFloat(),
            colors,
            positions,
            Shader.TileMode.CLAMP
        )
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        // Recreate gradient when bounds change
        gradientShader = null
    }

    override fun setAlpha(alpha: Int) {
        gradientPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        gradientPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
