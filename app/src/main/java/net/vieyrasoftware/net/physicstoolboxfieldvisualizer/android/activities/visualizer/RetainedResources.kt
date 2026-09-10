package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

/** Main-thread session ownership. Refuse work before allocating once the budget is full. */
internal class RetainedResources<T>(private val limit: Int, private val release: (T) -> Unit) : Iterable<T> {
    init { require(limit > 0) }
    private val items = mutableListOf<T>()
    val size: Int get() = items.size
    val isFull: Boolean get() = size >= limit

    fun tryAdd(create: () -> T): Boolean {
        if (isFull) return false
        items.add(create())
        return true
    }

    fun clear() {
        val owned = items.toList()
        items.clear()
        owned.forEach(release)
    }

    override fun iterator(): Iterator<T> = items.iterator()
}
