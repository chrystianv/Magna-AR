package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.fields

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field
import java.util.*

class CombinedField(children: List<Field>?) : Field {
    private val children: MutableList<Field>
    fun addChild(child: Field) {
        children.add(child)
    }

    fun removeChild(child: Field?) {
        children.remove(child)
    }

    fun clearChildren() {
        children.clear()
    }

    override fun sample(world: Vector3, root: Node): Vector3 {
        var sum = Vector3.zero()
        for (child in children) {
            sum = Vector3.add(sum, child.sample(world, root))
        }
        return sum
    }

    init {
        this.children = ArrayList(children!!)
    }
}