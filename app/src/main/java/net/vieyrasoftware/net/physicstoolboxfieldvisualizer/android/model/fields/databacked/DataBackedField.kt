package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.fields.databacked

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field
import java.util.*

class DataBackedField(private val magData: ArrayList<Vector3>, private val locData: ArrayList<Vector3>) : Field {
    override fun sample(world: Vector3, root: Node): Vector3 {
        // TODO: Interpolate
        return Vector3(0f, 0f, 0f)
    }
}