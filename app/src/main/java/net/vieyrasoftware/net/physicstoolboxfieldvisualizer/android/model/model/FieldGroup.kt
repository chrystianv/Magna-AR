package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model

import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Ray
import com.google.ar.sceneform.math.Vector3
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Actor
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.DynamicActor
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.Filter
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render.Model
import java.util.*
import kotlin.math.abs

class FieldGroup(// Root node for anchoring the scene.
        private val root: AnchorNode, private var active: Filter) {
    // Components in the scene.
    private val dyns: MutableList<DynamicActor> = ArrayList()
    private val actors: MutableList<Actor> = ArrayList()
    private val fields: MutableList<Field> = ArrayList()
    private val models: MutableList<Model> = ArrayList()
    // Variables for final interpretation of the magnetic field.
    private var changed = false // Optimization = false

    fun removeDynActor(d: DynamicActor) {
        removeActor(d)
        dyns.remove(d)
    }

    fun removeActor(a: Actor) {
        if (a.hasField()) {
            removeField(a.field())
        }
        if (a.hasModel()) {
            removeModel(a.model())
        }
        actors.remove(a)
    }

    fun removeField(f: Field?) {
        fields.remove(f)
        changed = true // Optimization
    }

    fun removeModel(m: Model?) {
        root.removeChild(m!!.node())
        models.remove(m)
    }

    fun addDynActor(d: DynamicActor) {
        addActor(d)
        dyns.add(d)
    }

    fun addActor(a: Actor) {
        if (a.hasField()) {
            addField(a.field())
            changed = true // Optimization
        }
        if (a.hasModel()) {
            addModel(a.model())
        }
        actors.add(a)
    }

    fun addField(f: Field) {
        fields.add(f)
        changed = true
    }

    fun addModel(m: Model?) {
        root.addChild(m!!.node())
        models.add(m)
    }

    fun changeFilter(active: Filter) {
        root.removeChild(this.active.node())
        this.active = active
        root.addChild(this.active.node())
    }

    fun step(frameTime: FrameTime?) {
        for (a in dyns) {
            changed = changed or a.apply(fields)
        }
        // Which one is better? who knows? Optimize afterwards
/* if (changed) {
            ArrayList<Field> tempCache = new ArrayList<>(fields);
            changed = false;
            active.update(tempCache);
        } */
// active.update(fields);
        if (changed) {
            active.update(fields, root)
        }
    }

    fun filter(): Filter {
        return active
    }

    fun intersect(r: Ray): Vector3 {
        val n = Vector3.up()
        val denominator = Vector3.dot(r.direction, n)
        if (abs(denominator) > 1e-10) {
            val center = root.worldPosition
            val d = Vector3.subtract(center, r.origin)
            val numerator = Vector3.dot(d, n)
            return root.worldToLocalPoint(r.getPoint(numerator / denominator))
        }
        return Vector3.zero()
    }

    fun worldToRootDirection(world: Vector3?): Vector3 {
        return root.worldToLocalDirection(world)
    }

    fun rootToWorldDirection(world: Vector3?): Vector3 {
        return root.localToWorldDirection(world)
    }

    fun worldToRootPoint(world: Vector3): Vector3 {
        return root.worldToLocalPoint(world)
    }

    fun rootToWorldPoint(world: Vector3): Vector3 {
        return root.localToWorldPoint(world)
    }

    fun localToRootDirection(point: Vector3, local: Node): Vector3 {
        return root.worldToLocalDirection(local.localToWorldDirection(point))
    }

    fun rootToLocalDirection(point: Vector3, local: Node): Vector3 {
        return local.worldToLocalDirection(root.localToWorldDirection(point))
    }

    fun localToRootPoint(point: Vector3, local: Node): Vector3 {
        return root.worldToLocalPoint(local.localToWorldPoint(point))
    }

    fun rootToLocalPoint(point: Vector3, local: Node): Vector3 {
        return local.worldToLocalPoint(root.localToWorldPoint(point))
    }

    init {
        root.addChild(active.node())
        active.node().localPosition = Vector3.zero()
    }
}