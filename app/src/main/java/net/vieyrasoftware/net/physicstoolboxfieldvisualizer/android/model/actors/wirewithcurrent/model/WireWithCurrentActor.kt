package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.actors.wirewithcurrent.model

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.ux.TransformationSystem
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Actor
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.BasicModel
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.Model

class WireWithCurrentActor(ts: TransformationSystem?) : Actor {
    private val field: WireWithCurrentField = WireWithCurrentField(this)
    private val model: BasicModel?
    override fun asNode(): Node {
        return model!!.node()
    }

    override fun hasField(): Boolean {
        return true
    }

    override fun hasModel(): Boolean {
        return model != null
    }

    override fun field(): Field {
        return field
    }

    override fun model(): Model? {
        return model
    }

    init {
        model = WireWithCurrentModel(ts)
    }
}