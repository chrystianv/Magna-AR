package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model

import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.node.Node
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.Actor
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.DynamicActor
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.Field
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.render.Filter
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.render.Model

import java.util.*
import kotlin.math.abs

class FieldGroup(// Root node for anchoring the scene.
    private val root: Node, private var active: Filter
) {
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
        if (m != null) {
            m.node().parent = null
            models.remove(m)
        }
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
        if (m != null) {
            m.node().parent = root
            models.add(m)
        }
    }

    fun changeFilter(active: Filter) {
        this.active.node().parent = null
        this.active = active
        this.active.node().parent = root
    }

    fun step(frameTime: Long?) {
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

    // Ray intersection with horizontal plane at root position
    fun intersect(rayOrigin: Vector3Compat, rayDirection: Vector3Compat): Vector3Compat {
        val n = Vector3Compat.up
        val denominator = Vector3Compat.dot(rayDirection, n)
        if (abs(denominator) > 1e-10) {
            val center = Vector3Compat.fromFloat3(root.worldPosition)
            val d = center.minus(rayOrigin)
            val numerator = Vector3Compat.dot(d, n)
            val t = numerator / denominator
            val hitPoint = rayOrigin.plus(rayDirection.times(t))
            return worldToRootPoint(hitPoint)
        }
        return Vector3Compat.zero
    }

    fun worldToRootDirection(world: Vector3Compat?): Vector3Compat {
        world ?: return Vector3Compat.zero
        // Simplified transformation
        return world
    }

    fun rootToWorldDirection(world: Vector3Compat?): Vector3Compat {
        world ?: return Vector3Compat.zero
        return world
    }

    fun worldToRootPoint(world: Vector3Compat): Vector3Compat {
        val worldF3 = world.toFloat3()
        val rootWorldPos = root.worldPosition
        // Simple subtraction for point transformation
        return Vector3Compat(
            worldF3.x - rootWorldPos.x,
            worldF3.y - rootWorldPos.y,
            worldF3.z - rootWorldPos.z
        )
    }

    fun rootToWorldPoint(world: Vector3Compat): Vector3Compat {
        val worldF3 = world.toFloat3()
        val rootWorldPos = root.worldPosition
        return Vector3Compat(
            worldF3.x + rootWorldPos.x,
            worldF3.y + rootWorldPos.y,
            worldF3.z + rootWorldPos.z
        )
    }

    fun localToRootDirection(point: Vector3Compat, local: Node): Vector3Compat {
        return point
    }

    fun rootToLocalDirection(point: Vector3Compat, local: Node): Vector3Compat {
        return point
    }

    fun localToRootPoint(point: Vector3Compat, local: Node): Vector3Compat {
        val localWorldPos = Vector3Compat.fromFloat3(local.worldPosition)
        val rootWorldPos = Vector3Compat.fromFloat3(root.worldPosition)
        val worldPoint = point.plus(localWorldPos)
        return worldPoint.minus(rootWorldPos)
    }

    fun rootToLocalPoint(point: Vector3Compat, local: Node): Vector3Compat {
        val rootWorldPos = Vector3Compat.fromFloat3(root.worldPosition)
        val localWorldPos = Vector3Compat.fromFloat3(local.worldPosition)
        val worldPoint = point.plus(rootWorldPos)
        return worldPoint.minus(localWorldPos)
    }

    init {
        active.node().parent = root
        active.node().position = Float3(0f, 0f, 0f)
    }
}
