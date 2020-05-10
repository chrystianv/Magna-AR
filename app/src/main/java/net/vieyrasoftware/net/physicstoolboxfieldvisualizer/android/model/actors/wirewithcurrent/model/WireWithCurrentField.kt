package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.actors.wirewithcurrent.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field
import java.util.*

class WireWithCurrentField @JvmOverloads constructor(private val parent: WireWithCurrentActor, i: Float = 1f, mu: Float = 1f) : Field {
    private val i = MutableLiveData<Float>()
    private val mu = MutableLiveData<Float>()
    private val k = MutableLiveData<Float>()
    private fun updateK() {
        k.postValue((getI() * getMu() / 2 / Math.PI).toFloat())
    }

    private fun getK(): Float {
        return Optional.ofNullable(k.value).orElse(0f)
    }

    private val kData: LiveData<Float>
        get() = k

    private fun getI(): Float {
        return Optional.ofNullable(i.value).orElse(0f)
    }

    val iData: LiveData<Float>
        get() = i

    private fun getMu(): Float {
        return Optional.ofNullable(mu.value).orElse(0f)
    }

    val muData: LiveData<Float>
        get() = mu

    override fun sample(world: Vector3, root: Node): Vector3 {
        val local = parent.asNode()!!.worldToLocalPoint(world)
        return root.worldToLocalDirection(parent.asNode()!!.localToWorldDirection(dir(local).scaled(getK() / r(local))))
    }

    private fun dir(local: Vector3): Vector3 {
        val dir = Vector3(local)
        val temp = dir.x
        dir.x = -dir.z
        dir.y = 0f
        dir.z = temp
        return dir.normalized()
    }

    private fun r(local: Vector3): Float {
        val dir = Vector3(local)
        dir.y = 0f
        return dir.length()
    }

    companion object {
        private const val TAG = "PhysicsVisualizerWireWithCurrentField"
    }

    init {
        this.i.postValue(i)
        this.mu.postValue(mu)
        this.i.observeForever { ignored: Float? -> updateK() }
        this.mu.observeForever { ignored: Float? -> updateK() }
    }
}