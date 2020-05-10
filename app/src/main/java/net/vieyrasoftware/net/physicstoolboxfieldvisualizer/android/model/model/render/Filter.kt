package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render

import android.content.Context
import com.google.ar.sceneform.Node
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field

interface Filter {
    fun update(fields: List<Field>, root: Node) // only call if mag sources changed
    fun node(): Node
    fun loadAsset(ctx: Context?)
}