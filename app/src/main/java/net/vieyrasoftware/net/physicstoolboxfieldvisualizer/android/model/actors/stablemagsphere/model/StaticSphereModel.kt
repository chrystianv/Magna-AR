package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.actors.stablemagsphere.model

import android.content.Context
import android.graphics.Color
import android.widget.TextView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.Model
import java.util.*
import kotlin.math.exp
import kotlin.math.sqrt

class StaticSphereModel(pos: Vector3?, field: Vector3, showNumbers: Boolean) : Model {
    var node = Node()
    var viewNode = Node()
    var intensity: Float
    var strength: Float
    fun setNumberVisibility(isVisible: Boolean) {
        if (isVisible) {
            node.addChild(viewNode)
            viewNode.worldScale = Vector3.one().scaled(0.1f)
        } else {
            node.removeChild(viewNode)
        }
    }

    override fun node(): Node {
        return node
    }

    private fun colForIntensity(): com.google.ar.sceneform.rendering.Color {
        val Pr = 0.299
        val Pb = 0.587
        val Pg = 0.114
        val P = sqrt(Pr)
        // want range from
        return Color(
                (P + (1 - P) * intensity).toFloat(),
                (P - P * intensity).toFloat(),
                (P - P * intensity).toFloat(),
                1f
        )
    }

    override fun loadAsset(ctx: Context?) {
        MaterialFactory
                .makeOpaqueWithColor(ctx, colForIntensity())
                .thenApply { material: Material? -> ShapeFactory.makeSphere(0.01f, Vector3.zero(), material) }.thenAccept { renderable: ModelRenderable? -> node().renderable = renderable }
        ViewRenderable.builder()
                .setView(ctx, R.layout.view_3d_number_text)
                .build()
                .thenApply { v: ViewRenderable ->
                    val numbers = v.view.findViewById<TextView>(R.id.number_text)
                    numbers.setTextColor(Color.BLACK)
                    numbers.text = String.format(Locale.getDefault(), "%.0f", strength)
                    v
                }
                .thenAccept { renderable: ViewRenderable? -> viewNode.renderable = renderable }
    }

    companion object {
        // TODO adjust to typical magnet ranges
        private fun asIntensity(field: Vector3): Float {
            val ranged = field.length() - 250 // changes focused about 500
            val sigmoidal = (1 / (1 + exp(-(ranged / 100).toDouble()))).toFloat() // TODO adjust to fit usual magnets
            return sigmoidal.coerceAtMost(1f).coerceAtLeast(0f)
        }
    }

    init {
        node.worldRotation = Quaternion.rotationBetweenVectors(
                Vector3(0f, -1.0f, 0f),
                field.normalized()
        )
        node.localPosition = Vector3(0f, -0.5f, 0f)
        node.worldPosition = pos
        // Scale to mag field strength
        val scaling = Vector3.one()
        // TODO adjust scaling so as to make the arrow more comfortable
        strength = field.length()
        intensity = asIntensity(field)
        node.localScale = scaling.scaled(intensity * 9 + 1)
        node.localScale = Vector3(0.25f, 0.25f, 0.25f)
        viewNode.localRotation = Quaternion.rotationBetweenVectors(
                Vector3(0f, 1f, 0f),
                node.worldToLocalDirection(Vector3(0f, 1f, 0f))
        )
        viewNode.localPosition = node.worldToLocalDirection(Vector3(0f, 0.01f, 0f))
        setNumberVisibility(showNumbers)
    }
}