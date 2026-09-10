package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

internal class RepeatingActionScheduler(
    private val intervalMillis: Long,
    private val postDelayed: (Runnable, Long) -> Unit,
    private val removeCallbacks: (Runnable) -> Unit,
    private val action: () -> Unit
) {
    private var running = false

    private val runnable = object : Runnable {
        override fun run() {
            if (!running) return

            action()
            if (running) {
                postDelayed(this, intervalMillis)
            }
        }
    }

    fun start() {
        if (running) return

        running = true
        postDelayed(runnable, intervalMillis)
    }

    fun stop() {
        if (!running) return

        running = false
        removeCallbacks(runnable)
    }
}
