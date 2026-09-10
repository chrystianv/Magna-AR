package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import org.junit.Assert.*
import org.junit.Test

class RetainedResourcesTest {
    @Test fun sustainedPlacementNeverAllocatesBeyondTheSessionBudget() {
        var allocations = 0
        val released = mutableListOf<Int>()
        val resources = RetainedResources<Int>(128, released::add)
        repeat(10_000) { resources.tryAdd { ++allocations } }
        assertEquals(128, allocations)
        assertEquals(128, resources.size)
        assertTrue(resources.isFull)
        assertEquals((1..128).toList(), resources.toList())
        resources.clear()
        resources.clear()
        assertEquals((1..128).toList(), released)
        assertEquals(0, resources.size)
        assertFalse(resources.isFull)
        assertTrue(resources.tryAdd { ++allocations })
    }

    @Test fun failedCreationDoesNotConsumeCapacity() {
        val resources = RetainedResources<Int>(1) { }
        try { resources.tryAdd { error("asset unavailable") }; fail("Expected failure") } catch (_: IllegalStateException) { }
        assertEquals(0, resources.size)
        assertTrue(resources.tryAdd { 1 })
        assertFalse(resources.tryAdd { error("Must not allocate when full") })
    }
}
