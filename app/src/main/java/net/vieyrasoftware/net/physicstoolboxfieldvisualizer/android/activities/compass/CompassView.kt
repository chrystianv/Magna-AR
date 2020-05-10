package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.compass

import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import kotlin.math.cos
import kotlin.math.sin

class CompassView : View {
    // Represented data and relevant animations.
    private var mDegree = 0f
    private var mRoll = 0f
    private var mPitch = 0f
    private var headingAnimator: AnimatorSet? = null
    // Padding, stroke width, line length, and other distance constants
    private var divisionRadius = 0
    private val defaultPadding = dp2px(5f)
    private val divisionStrokeWidth = dp2px(2f)
    private val gradientLineLength = dp2px(20f)
    private val divisionLineLength = dp2px(20f)
    private val crossStrokeWidth = dp2px(1f)
    private val gradientStrokeWidth = dp2px(1f)
    private val labelStrokeWidth = dp2px(1f)
    private val labelWidth = dp2px(40f)
    private val labelNSWEWidth = dp2px(25f)
    private val labelNSWEFontSize = sp2px(22f)
    private val labelFontSize = sp2px(16f)
    // Cardinal label text
    private var east = context.getString(R.string.compass_east)
    private var west = context.getString(R.string.compass_west)
    private var south = context.getString(R.string.compass_south)
    private var north = context.getString(R.string.compass_north)
    // Paint objects
    private val mPaintDivision = {
        // Minor divider line style
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.strokeWidth = divisionStrokeWidth.toFloat()
        p.color = Color.parseColor("#979797")
        p
    }()
    private val mPaintDivisionBold = {
        // Major divider line style
        val p = Paint(mPaintDivision)
        p.color = Color.parseColor("#FFFFFF")
        p.strokeWidth = (divisionStrokeWidth * 1.2).toFloat()
        p
    }()
    private val mPaintTriangle = {
        // North divider line
        val p = Paint(mPaintDivisionBold)
        p.color = Color.parseColor("#FF0000")
        p
    }()
    private val mPaintLabel = {
        // Degree text markers
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.parseColor("#AAFFFFFF")
        p.textSize = labelFontSize.toFloat()
        p.textAlign = Paint.Align.CENTER
        p
    }()
    private val mPaintCross = {
        // Central cross
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.parseColor("#88979797")
        p.style = Paint.Style.STROKE
        p.strokeWidth = crossStrokeWidth.toFloat()
        p
    }()
    // Inclination cross
    private val mPaintGradientLine = {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.parseColor("#FFFFFF")
        p.style = Paint.Style.STROKE
        p.strokeWidth = gradientStrokeWidth.toFloat()
        p
    }()
    private val mPaintNSWE = {
        // Cardinal text markers
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.parseColor("#FFFFFF")
        p.textSize = labelNSWEFontSize.toFloat()
        p.textAlign = Paint.Align.CENTER
        p
    }()
    private val mPaintGradientCircle = {
        // Inclination marker circle
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.FILL
        p.color = Color.parseColor("#388E3C")
        p.alpha = 50
        p
    }()

    private var centerX = 0
    private var centerY = 0
    var mPathDivision = Path()

    private val drawMemoryBuffer = FloatArray(4)

    /* ****** Constructors/Construction ****** */
    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
    private fun init() {
        // Assignments happen at instance field declaration
    }

    /* ****** Measurement ****** */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(parentWidth, parentHeight)
        centerX = parentWidth / 2
        centerY = parentHeight / 2
        divisionRadius = centerX - labelWidth - paddingLeft - paddingRight
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /* ****** Draw Auxiliary Methods ****** */
    /**
     * Get the sine and cosine such that North lies at -90 degrees.
     *
     * @return The first value of the pair is sine. The second value is cosine.
     */
    private fun bearingXY(degree: Float): Pair<Float, Float> {
        val cos = cos(Math.toRadians(degree - 90 + mDegree.toDouble())).toFloat()
        val sin = sin(Math.toRadians(degree - 90 + mDegree.toDouble())).toFloat()
        return Pair(cos, sin)
    }
    /**
     * Find the endpoints of the divider's line, provided the sine and cosine
     * desired.
     *
     * @return Each member of the Pair is an endpoint. Each endpoint is in the
     * order (x, y).
     */
    private fun dividerEndpoints(bearing: Pair<Float, Float>): Pair<Pair<Float, Float>, Pair<Float, Float>> {
        val (cos, sin) = bearing
        val fromX = (divisionRadius * cos + centerX)
        val fromY = (divisionRadius * sin + centerY)
        val toX = ((divisionRadius - divisionLineLength) * cos + centerX)
        val toY = ((divisionRadius - divisionLineLength) * sin + centerY)
        return Pair(Pair(fromX, fromY), Pair(toX, toY))
    }

    /* ****** Draw View ****** */
    private fun drawInclinationMarker(canvas: Canvas) {
        val bigCrossLineWidth = divisionRadius - labelNSWEWidth * 2.toFloat()
        val gradientCircleRadius = bigCrossLineWidth / 2
        mPitch = if (mPitch > 90) 180 - mPitch else mPitch
        mPitch = if (mPitch < -90) (180 + mPitch) * -1 else mPitch
        val gradientCenterY = (centerY + gradientCircleRadius * mPitch / 90).toInt().toFloat()
        val gradientCenterX = (centerX + gradientCircleRadius * mRoll / 90).toInt().toFloat()
        canvas.drawCircle(gradientCenterX, gradientCenterY, gradientCircleRadius, mPaintGradientCircle)
        canvas.drawLine(gradientCenterX - gradientLineLength.toFloat(), gradientCenterY, gradientCenterX + gradientLineLength.toFloat(), gradientCenterY.toFloat(), mPaintGradientLine)
        canvas.drawLine(gradientCenterX, gradientCenterY - gradientLineLength.toFloat(), gradientCenterX, gradientCenterY + gradientLineLength.toFloat(), mPaintGradientLine)
        mPathDivision.addCircle(centerX.toFloat(), centerY.toFloat(), divisionRadius.toFloat(), Path.Direction.CCW)
    }
    private fun drawCompassBackground(canvas: Canvas) {
        // Draw Top Line
        drawMemoryBuffer[0] = centerX.toFloat()
        drawMemoryBuffer[1] = (centerY - height / 2).toFloat()
        drawMemoryBuffer[2] = centerX.toFloat()
        drawMemoryBuffer[3] = (centerY - divisionRadius).toFloat()
        canvas.drawLines(drawMemoryBuffer, mPaintDivision)
    }
    private fun drawBearings(canvas: Canvas) {
        drawMinorDividers(canvas)
        drawMajorDividersAndNumbers(canvas)
        drawCardinalLabels(canvas)
        drawNorthLine(canvas)
    }
    // Every 3 degrees except for the tenth.
    private fun drawMinorDividers(canvas: Canvas) {
        for (subdivisionStartingDegree in 0 until 360 step 30) {
            for (degree in (subdivisionStartingDegree + 3) until (subdivisionStartingDegree + 30) step 3) {
                val endpoints = dividerEndpoints(bearingXY(degree.toFloat()))
                drawMemoryBuffer[0] = endpoints.first.first
                drawMemoryBuffer[1] = endpoints.first.second
                drawMemoryBuffer[2] = endpoints.second.first
                drawMemoryBuffer[3] = endpoints.second.second
                canvas.drawLines(drawMemoryBuffer, mPaintDivision)
            }
        }
    }
    // Every 30 degrees.
    private fun drawMajorDividersAndNumbers(canvas: Canvas) {
        for (degree in 0 until 360 step 30) {
            val (cos, sin) = bearingXY(degree.toFloat())
            val labelX = ((divisionRadius + labelWidth / 2) * cos + centerX)
            var labelY = ((divisionRadius + labelWidth / 2) * sin + centerY)
            val startY = centerY - (divisionRadius + labelWidth / 2).toFloat()
            val totalYHeight = (divisionRadius + labelWidth / 2) * 2.toFloat()
            val gapY = labelY - startY
            labelY += labelFontSize * (gapY / totalYHeight)
            canvas.drawText(degree.toString(), labelX, labelY, mPaintLabel)

            val dividerEndpoints = dividerEndpoints(bearingXY(degree.toFloat()))
            drawMemoryBuffer[0] = dividerEndpoints.first.first
            drawMemoryBuffer[1] = dividerEndpoints.first.second
            drawMemoryBuffer[2] = dividerEndpoints.second.first
            drawMemoryBuffer[3] = dividerEndpoints.second.second
            canvas.drawLines(drawMemoryBuffer, mPaintDivisionBold)
        }
    }
    // N -> 0, E -> 1, S -> 2, W -> 3
    private fun drawCardinalLabels(canvas: Canvas) {
        for (degree in 0 until 360 step 90) {
            val (cos, sin) = bearingXY(degree.toFloat())
            val targetX = ((divisionRadius - divisionLineLength - labelFontSize) * cos + centerX)
            var targetY = ((divisionRadius - divisionLineLength - labelFontSize) * sin + centerY)
            val startY = centerY - (divisionRadius - divisionLineLength - labelFontSize).toFloat()
            val totalYHeight = (divisionRadius - divisionLineLength - labelFontSize) * 2f
            val gapY = targetY - startY
            targetY += labelFontSize * (gapY / totalYHeight)
            val label = when (degree) {
                0 -> north
                90 -> east
                180 -> south
                270 -> west
                else -> throw Exception("There are only 4 cardinals. This line should never be reached.")
            }
            canvas.drawText(label, targetX, targetY, mPaintNSWE)
        }
    }
    // Draw red line for line pointing to North.
    private fun drawNorthLine(canvas: Canvas) {
        // TODO: 12/23/15  draw red triangle
        // Draw red line for north
        val dividerEndpoints = dividerEndpoints(bearingXY(0f))
        drawMemoryBuffer[0] = dividerEndpoints.first.first
        drawMemoryBuffer[1] = dividerEndpoints.first.second
        drawMemoryBuffer[2] = dividerEndpoints.second.first
        drawMemoryBuffer[3] = dividerEndpoints.second.second
        canvas.drawLines(drawMemoryBuffer, mPaintTriangle)
    }
    private fun drawCompassForeground(canvas: Canvas) {
        val bigCrossLineWidth = divisionRadius - labelNSWEWidth * 2.toFloat()
        // draw big cross
        canvas.drawLine(centerX - bigCrossLineWidth, centerY.toFloat(), centerX + bigCrossLineWidth, centerY.toFloat(), mPaintCross)
        canvas.drawLine(centerX.toFloat(), centerY - bigCrossLineWidth, centerX.toFloat(), centerY + bigCrossLineWidth, mPaintCross)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        // Draw all the things
        drawInclinationMarker(canvas)
        drawCompassBackground(canvas)
        drawBearings(canvas)
        drawCompassForeground(canvas)

        canvas.restore()
    }

    /* ****** Animate Compass Movement ****** */
    // TODO Move animation from Fragment to here.
    fun updateDegree(degree: Float, pitch: Float, roll: Float) {
        mDegree = degree
        mRoll = roll
        mPitch = pitch
        invalidate()
    }

    /* ****** Unit Conversion ****** */
    private fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.resources.displayMetrics
        ).toInt()
    }
    private fun sp2px(sp: Float): Int {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context.resources.displayMetrics
        ).toInt()
    }
}