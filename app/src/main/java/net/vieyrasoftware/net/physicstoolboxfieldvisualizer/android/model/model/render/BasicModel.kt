package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem

abstract class BasicModel(ts: TransformationSystem?) : Model {
    private val mine: TransformableNode = TransformableNode(ts)
    override fun node(): Node {
        return mine
    }

    protected fun setRenderable(r: Renderable?) {
        mine.renderable = r
    }

}