package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.actors.wirewithcurrent.model

import android.content.Context
import android.graphics.Color
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.TransformationSystem
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.BasicModel

class WireWithCurrentModel(ts: TransformationSystem?) : BasicModel(ts) {
    override fun loadAsset(ctx: Context?) {
        MaterialFactory
                .makeOpaqueWithColor(ctx, Color(Color.GREEN))
                .thenApply { material: Material? ->
                    ShapeFactory.makeCylinder(
                            0.01f, 100f,
                            Vector3.zero(),
                            material
                    )
                }.thenAccept { r: ModelRenderable? -> setRenderable(r) }
    }
}