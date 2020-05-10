package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.fields.constantfield

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field

class ConstantField(private val strength: Vector3) : Field {
    override fun sample(world: Vector3, root: Node): Vector3 {
        return Vector3(strength)
    }

}