package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag

import com.google.ar.sceneform.Node
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.Model

interface Actor {
    fun asNode(): Node
    fun hasField(): Boolean
    fun hasModel(): Boolean
    fun field(): Field
    fun model(): Model?
}