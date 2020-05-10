package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.fields

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field

class StrengthVaryingField : Field {
    override fun sample(world: Vector3, root: Node): Vector3 {
        return Vector3(0f, 0f, (System.currentTimeMillis() / 100 % 10).toFloat() / 5)
    }
}