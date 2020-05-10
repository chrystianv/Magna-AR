package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3

interface Field {
    fun sample(world: Vector3, root: Node): Vector3
}