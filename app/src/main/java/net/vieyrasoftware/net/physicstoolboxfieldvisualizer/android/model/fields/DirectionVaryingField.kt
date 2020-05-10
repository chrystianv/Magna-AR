package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.fields

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field
import kotlin.math.cos
import kotlin.math.sin

class DirectionVaryingField : Field {
    override fun sample(world: Vector3, root: Node): Vector3 {
        val curr = System.currentTimeMillis() * 0.0001
        return Vector3(sin(curr).toFloat(), cos(curr).toFloat(), 0f)
    }
}