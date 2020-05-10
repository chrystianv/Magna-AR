package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.compass

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import java.util.*
import kotlin.math.abs

class CompassFragment : Fragment() {
    companion object {
        fun newInstance() = CompassFragment()
        private const val MAX_ROTA_DEGREE = 1.0f
    }

    private lateinit var headingTextView: TextView
    private lateinit var compassView: CompassView
    private lateinit var viewModel: CompassViewModel
    private val mInterpolator = AccelerateInterpolator()

    private var mDirection = 0f
    private var mRoll = 0f
    private var mPitch = 0f

    private var mTargetDirection = 0f
    private var mTargetRoll = 0f
    private var mTargetPitch = 0f

    private var mHandler: Handler? = null
    private var mStopDrawing = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_compass, container, false)
        compassView = view.findViewById(R.id.compass)
        headingTextView = view.findViewById(R.id.headingTextView);
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CompassViewModel::class.java)
        viewModel.getOrientationLiveData().observe(this, Observer<FloatArray> { heading ->
            mTargetDirection = heading[0]
            mTargetPitch = heading[1]
            mTargetRoll = heading[2]
            Log.i("CompassFragment", "Target: $heading")
        })
    }

    override fun onResume() {
        super.onResume()
        val handler = Handler()
        handler.postDelayed(mCompassViewUpdater, 1000)
        mHandler = handler
        mStopDrawing = false
    }

    override fun onPause() {
        super.onPause()
        mStopDrawing = true
        mHandler?.removeCallbacksAndMessages(null)
        mHandler = null
    }

    private fun normalizeDegree(degree: Float): Float {
        return (degree + 720) % 360
    }

    // TODO move this into the CompassView itself
    private var mCompassViewUpdater: Runnable = object : Runnable {
        override fun run() {
            if (!mStopDrawing) {
                if (mDirection != mTargetDirection || mTargetRoll != mRoll || mTargetPitch != mTargetPitch) { // calculate the short routine
                    var to: Float = mTargetDirection
                    if (to - mDirection > 180) {
                        to -= 360f
                    } else if (to - mDirection < -180) {
                        to += 360f
                    }
                    // limit the max speed to MAX_ROTATE_DEGREE
                    var distance: Float = to - mDirection
                    if (abs(distance) > MAX_ROTA_DEGREE) {
                        distance = if (distance > 0) MAX_ROTA_DEGREE else -1.0f * MAX_ROTA_DEGREE
                    }
                    // need to slow down if the distance is short
                    mDirection = normalizeDegree(mDirection + (to - mDirection) * mInterpolator.getInterpolation(if (abs(distance) > MAX_ROTA_DEGREE) 0.4f else 0.3f))
                    to = mTargetPitch
                    distance = to - mPitch
                    if (abs(distance) > MAX_ROTA_DEGREE) {
                        distance = if (distance > 0) MAX_ROTA_DEGREE else -1.0f * MAX_ROTA_DEGREE
                    }
                    mPitch += (to - mPitch) * mInterpolator.getInterpolation(if (abs(distance) > MAX_ROTA_DEGREE) 0.4f else 0.3f)
                    to = mTargetRoll
                    distance = to - mRoll
                    if (abs(distance) > MAX_ROTA_DEGREE) {
                        distance = if (distance > 0) MAX_ROTA_DEGREE else -1.0f * MAX_ROTA_DEGREE
                    }
                    mRoll += (to - mRoll) * mInterpolator.getInterpolation(if (abs(distance) > MAX_ROTA_DEGREE) 0.4f else 0.3f)
                    /*
                     * A circle has 360 degrees. Each of the cardinals and intercardinals occupies
                     * 45 degrees. Furthermore, the subdivisions do not start from the 0 degree
                     * location, but rather a 22.5 degree offset from the center. Thus, we first
                     * rotate by 22.5 degrees to the right, to align the boundary between North and
                     * Northwest at 0 degrees. To do this, we add 22.5 to shift the values. At this
                     * point, we can ignore everything less than one degree, so we can cast to int.
                     * However, half of the values for the North section are now too high, so we mod
                     * by 360 to bring the range [360, 382] back down to [0, 22]. Integer division
                     * by 45 then gives which bucket each reading falls under.
                     */
                    val value: Double = abs(mDirection - 360).toDouble()
                    val cardinals = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
                    val bucket = (value + 22.5).toInt() % 360 / 45
                    headingTextView.text = String.format(
                            Locale.getDefault(),
                            "%d° %s",
                            value.toInt(),
                            cardinals[bucket]
                    )
                    compassView.updateDegree(mDirection, mPitch, mRoll)
                }
                mHandler!!.postDelayed(this, 20)
            }
        }
    }

}