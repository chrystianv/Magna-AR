package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

/**
 * Helper to detect taps using Android GestureDetector, and pass the taps between UI thread and
 * render thread.
 */
class TapHelper(context: Context?) : View.OnTouchListener {
    private val gestureDetector: GestureDetector
    private val queuedSingleTaps: BlockingQueue<MotionEvent> = ArrayBlockingQueue(16)
    /**
     * Polls for a tap.
     *
     * @return if a tap was queued, a MotionEvent for the tap. Otherwise null if no taps are queued.
     */
    fun poll(): MotionEvent? {
        return queuedSingleTaps.poll()
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    /**
     * Creates the tap helper.
     *
     * @param context the application's context.
     */
    init {
        gestureDetector = GestureDetector(
                context,
                object : SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent): Boolean { // Queue tap if there is space. Tap is lost if queue is full.
                        queuedSingleTaps.offer(e)
                        return true
                    }

                    override fun onDown(e: MotionEvent): Boolean {
                        return true
                    }
                })
    }
}