package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag

interface DynamicActor : Actor {
    fun apply(f: List<Field>): Boolean
}