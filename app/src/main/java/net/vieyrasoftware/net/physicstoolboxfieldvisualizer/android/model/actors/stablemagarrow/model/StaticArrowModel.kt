package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.actors.stablemagarrow.model

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.widget.TextView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.R
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.Model
import java.util.*
import kotlin.math.exp

class StaticArrowModel(pos: Vector3?, field: Vector3, showNumbers: Boolean) : Model {
    var node = Node()
    var figureNode = Node()
    var viewNode = Node()
    var intensity: Float
    var strength: Float
    fun setNumberVisibility(isVisible: Boolean) {
        if (isVisible) {
            node.addChild(viewNode)
            viewNode.worldScale = Vector3.one().scaled(0.1f)
            viewNode.localPosition = Vector3(0f, 0.02f, 0f)
        } else {
            node.removeChild(viewNode)
        }
    }

    override fun node(): Node {
        return node
    }

    private fun colForIntensity(): com.google.ar.sceneform.rendering.Color {
        val colors = arrayOf(
                // The trailing comment is "color, strength range, index"
                arrayOf(0, 0, 0), // black, MIN..0, 0
                arrayOf(13, 71, 161), // blue 900, 1..55, 1
                // +10 to get to next range
                arrayOf(21, 101, 192), // blue 800, 56..65, 2
                arrayOf(25, 118, 210), // blue 700, 66..75, 3
                arrayOf(30, 136, 229), // blue 600, 76..85, 4
                arrayOf(33, 150, 243), // blue 500, 86..95, 5
                arrayOf(66, 165, 245), // blue 400, 96..105, 6
                arrayOf(100, 181, 246), // blue 300, 106..115, 7
                // +20 to get to next range
                arrayOf(255, 241, 118), // yellow 300, 116..135, 8
                arrayOf(255, 238, 88), // yellow 400, 136..155, 9
                arrayOf(255, 235, 59), // yellow 500, 156..175, 10
                arrayOf(253, 216, 53), // yellow 600, 176..195, 11
                // +40 to get to next range
                arrayOf(251, 192, 45), // yellow 700, 196..235, 12
                arrayOf(255, 179, 0), // orange 600, 236..275, 13
                arrayOf(255, 160, 0), // orange 700, 276..315, 14
                arrayOf(255, 111, 0), // orange 900, 316..355, 15
                arrayOf(255, 87, 34), // deep orange 500, 356..395, 16
                arrayOf(244, 81, 30), // deep orange 600, 396..435, 17
                arrayOf(230, 74, 25), // deep orange 700, 436..475, 18
                arrayOf(244, 67, 54), // red 500, 476..515, 19
                arrayOf(229, 57, 53), // red 600, 516..555, 20
                arrayOf(211, 47, 47), // red 700, 556..595, 21
                arrayOf(198, 40, 40) // red 800, 596..MAX, 22
        )
        // 0, 55, +10 up to 115, +20 up to 195, +40 up to 595
        val bucket = when (val flooredStrength = strength.toInt()) {
            in Int.MIN_VALUE..0 -> 0
            in 0..55 -> 1
            in 56..115 -> 2 + ((flooredStrength - 56) / 10)
            in 116..195 -> 8 + ((flooredStrength - 116) / 20)
            in 196..595 -> 12 + ((flooredStrength - 196) / 40)
            else -> 22
        }
        val colorFloats = colors[bucket].map { it / 255f }
        return Color(colorFloats[0], colorFloats[1], colorFloats[2], 1f)
    }

    override fun loadAsset(ctx: Context?) {
        ModelRenderable.builder()
                .setSource(ctx, Uri.parse("arrow.sfb"))
                .build()
                .thenCombine(MaterialFactory.makeOpaqueWithColor(ctx, colForIntensity())) { m: ModelRenderable, mat: Material? ->
                    m.material = mat
                    m
                }
                .thenAccept { renderable: ModelRenderable? -> figureNode.renderable = renderable }
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
            val ranged = field.length() - 150
            val sigmoidal = (1 / (1 + exp(-(ranged / 100).toDouble()))).toFloat() // TODO adjust to fit usual magnets
            return sigmoidal.coerceAtMost(1f).coerceAtLeast(0f)
        }
    }

    init {
        node.worldPosition = pos
        node.worldRotation = Quaternion.identity()
        node.worldScale = Vector3.one()
        // process figure
        node.addChild(figureNode)
        figureNode.localRotation = Quaternion.rotationBetweenVectors(
                Vector3(0f, -1.0f, 0f),
                field.normalized()
        )
        // Scale to mag field strength
        val scaling = Vector3.one()
        // TODO adjust scaling so as to make the arrow more comfortable
        strength = field.length()
        intensity = asIntensity(field)
        scaling.y = intensity * 5 + 1
        figureNode.localPosition = field.normalized().scaled(scaling.y * 0.05f * 0.05f)
        // figureNode.setLocalPosition(new Vector3(0, 0.5f, 0));
        figureNode.localScale = scaling.scaled(0.005f)
        // process view
        setNumberVisibility(showNumbers)
    }
}