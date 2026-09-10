package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RepeatingActionSchedulerTest {
    @Test
    fun startSchedulesOneActionAndRepeatsAfterItRuns() {
        val pending = mutableListOf<Runnable>()
        val delays = mutableListOf<Long>()
        var actionCount = 0
        val scheduler = RepeatingActionScheduler(
            intervalMillis = 500L,
            postDelayed = { runnable, delay ->
                pending += runnable
                delays += delay
            },
            removeCallbacks = pending::remove,
            action = { actionCount++ }
        )

        scheduler.start()
        scheduler.start()

        assertEquals(1, pending.size)
        assertEquals(listOf(500L), delays)

        pending.removeAt(0).run()

        assertEquals(1, actionCount)
        assertEquals(1, pending.size)
        assertEquals(listOf(500L, 500L), delays)
    }

    @Test
    fun stopCancelsPendingActionAndAllowsRestart() {
        val pending = mutableListOf<Runnable>()
        var actionCount = 0
        val scheduler = RepeatingActionScheduler(
            intervalMillis = 500L,
            postDelayed = { runnable, _ -> pending += runnable },
            removeCallbacks = pending::remove,
            action = { actionCount++ }
        )

        scheduler.start()
        val cancelledAction = pending.single()
        scheduler.stop()

        assertTrue(pending.isEmpty())
        cancelledAction.run()
        assertEquals(0, actionCount)

        scheduler.start()
        pending.removeAt(0).run()

        assertEquals(1, actionCount)
    }
}
