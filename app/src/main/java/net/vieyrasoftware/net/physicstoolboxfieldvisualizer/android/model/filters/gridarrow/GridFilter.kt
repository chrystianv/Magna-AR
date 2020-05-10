package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.filters.gridarrow

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.MathHelper
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.Filter
import java.util.*

class GridFilter(dimLocal: Vector3, subdiv: Vector3) : Filter {
    private val parent: Node = Node()
    private val arrowNodes: MutableList<Node> = ArrayList()
    private var arrow: Renderable? = null
    private val dimLocal = MutableLiveData<Vector3>()
    private val subdiv = MutableLiveData<Vector3>()
    override fun loadAsset(ctx: Context?) {
        ModelRenderable.builder()
                .setSource(ctx, Uri.parse("arrow.sfb"))
                .build()
                .thenAccept { arrow: ModelRenderable -> setAsset(arrow) }
    }

    private fun setAsset(arrow: ModelRenderable) {
        arrow.collisionShape = null
        this.arrow = arrow
        for (n in arrowNodes) {
            n.renderable = arrow
        }
    }

    private fun getDimLocal(): Vector3 {
        return Optional.ofNullable(dimLocal.value).orElse(Vector3())
    }

    private val subDiv: Vector3
        get() = Optional.ofNullable(subdiv.value).orElse(Vector3())

    private fun refitArrows() { // Potentially more things to be done...?
        initArrows()
    }

    private fun initArrows() {
        val dimLocal = getDimLocal()
        var subdiv = subDiv
        val min = dimLocal.scaled(-0.5f)
        min.y = 0f
        val step = componentDivide(dimLocal, subdiv)
        subdiv = Vector3.add(subdiv, Vector3.one())
        // Reuse existing nodes.
        run {
            var i = 0
            while (i < subdiv.x * subdiv.y * subdiv.z) {
                val pos = position(min, step, gridIdx(subdiv, i))
                var n: Node
                if (i < arrowNodes.size) {
                    n = arrowNodes[i]
                } else {
                    n = Node()
                    arrowNodes.add(n)
                    this.parent.addChild(n)
                    n.renderable = this.arrow
                }
                n.localScale = Vector3.zero()
                n.localPosition = pos
                ++i
            }
        }
        // Clear unused nodes.
        for (i in (subdiv.x * subdiv.y * subdiv.z).toInt() until arrowNodes.size) {
            arrowNodes.removeAt(arrowNodes.size - 1)
        }
    }

    private fun position(min: Vector3, step: Vector3, pos: Vector3): Vector3 {
        return Vector3.add(min, componentMultiply(step, pos))
    }

    private fun gridIdx(div: Vector3, i: Int): Vector3 {
        return Vector3(x(div, i).toFloat(), y(div, i).toFloat(), z(div, i).toFloat())
    }

    override fun update(fields: List<Field>, root: Node) {
        for (i in arrowNodes.indices) {
            val point = arrowNodes[i].worldPosition
            var runningSum = Vector3()
            for (source in fields) {
                runningSum = Vector3.add(runningSum, source.sample(point, root))
            }
            val arrowNode = arrowNodes[i]
            arrowNode.localScale = toScale(runningSum.length())
            arrowNode.localRotation = Quaternion.rotationBetweenVectors(Vector3(0f, -1f, 0f), runningSum.normalized())
        }
    }

    private fun toScale(length: Float): Vector3 { // TODO: We're probably gonna want to scale + cap this at some point (with something like clamp).
        val str = Vector3.one()
        str.y = MathHelper.clamp(length, 0.1f, 1000f)
        return str.scaled(0.1f)
    }

    override fun node(): Node {
        return parent
    }

    companion object {
        private const val TAG = "PhysicsVisualizerGridFilter"
        private fun componentMultiply(a: Vector3, b: Vector3): Vector3 {
            return Vector3(
                    a.x * b.x,
                    a.y * b.y,
                    a.z * b.z
            )
        }

        private fun componentInverse(a: Vector3): Vector3 {
            return Vector3(
                    1 / a.x,
                    1 / a.y,
                    1 / a.z
            )
        }

        private fun componentDivide(a: Vector3, b: Vector3): Vector3 {
            return componentMultiply(a, componentInverse(b))
        }

        private fun x(div: Vector3, idx: Int): Int {
            return idx / (div.y * div.z).toInt()
        }

        private fun y(div: Vector3, idx: Int): Int {
            return idx % (div.y * div.z).toInt() / div.x.toInt()
        }

        private fun z(div: Vector3, idx: Int): Int {
            return idx % div.y.toInt()
        }
    }

    init {
        this.dimLocal.observeForever { refitArrows() }
        this.subdiv.observeForever { refitArrows() }
        this.dimLocal.postValue(dimLocal)
        this.subdiv.postValue(subdiv)
    }
}