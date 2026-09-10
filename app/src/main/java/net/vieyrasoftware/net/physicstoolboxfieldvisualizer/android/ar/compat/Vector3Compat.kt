package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat

import dev.romainguy.kotlin.math.Float3

/**
 * Compatibility class to ease migration from Sceneform Vector3 to SceneView Float3.
 * Provides similar API to com.google.ar.sceneform.math.Vector3
 */
data class Vector3Compat(@JvmField var x: Float, @JvmField var y: Float, @JvmField var z: Float) {

    constructor() : this(0f, 0f, 0f)

    constructor(vector: Float3) : this(vector.x, vector.y, vector.z)

    /**
     * Convert to SceneView Float3
     */
    fun toFloat3(): Float3 = Float3(x, y, z)

    /**
     * Set from Float3
     */
    fun set(vector: Float3) {
        x = vector.x
        y = vector.y
        z = vector.z
    }

    /**
     * Set individual components
     */
    fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Vector addition
     */
    operator fun plus(other: Vector3Compat): Vector3Compat {
        return Vector3Compat(x + other.x, y + other.y, z + other.z)
    }

    /**
     * Vector subtraction
     */
    operator fun minus(other: Vector3Compat): Vector3Compat {
        return Vector3Compat(x - other.x, y - other.y, z - other.z)
    }

    /**
     * Scalar multiplication
     */
    operator fun times(scalar: Float): Vector3Compat {
        return Vector3Compat(x * scalar, y * scalar, z * scalar)
    }

    /**
     * Scalar division
     */
    operator fun div(scalar: Float): Vector3Compat {
        return Vector3Compat(x / scalar, y / scalar, z / scalar)
    }

    /**
     * Dot product
     */
    fun dot(other: Vector3Compat): Float {
        return x * other.x + y * other.y + z * other.z
    }

    /**
     * Cross product
     */
    fun cross(other: Vector3Compat): Vector3Compat {
        return Vector3Compat(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    /**
     * Vector length (magnitude)
     */
    fun length(): Float {
        return Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
    }

    /**
     * Squared length (faster than length())
     */
    fun lengthSquared(): Float {
        return x * x + y * y + z * z
    }

    /**
     * Normalize the vector (unit length)
     */
    fun normalized(): Vector3Compat {
        val len = length()
        return if (len > 0.0001f) {
            Vector3Compat(x / len, y / len, z / len)
        } else {
            Vector3Compat(0f, 0f, 0f)
        }
    }

    /**
     * Negate the vector
     */
    fun negated(): Vector3Compat {
        return Vector3Compat(-x, -y, -z)
    }

    /**
     * Linear interpolation
     */
    fun lerp(end: Vector3Compat, t: Float): Vector3Compat {
        return Vector3Compat(
            x + (end.x - x) * t,
            y + (end.y - y) * t,
            z + (end.z - z) * t
        )
    }

    companion object {
        @JvmField
        val zero = Vector3Compat(0f, 0f, 0f)

        @JvmField
        val one = Vector3Compat(1f, 1f, 1f)

        @JvmField
        val up = Vector3Compat(0f, 1f, 0f)

        @JvmField
        val down = Vector3Compat(0f, -1f, 0f)

        @JvmField
        val left = Vector3Compat(-1f, 0f, 0f)

        @JvmField
        val right = Vector3Compat(1f, 0f, 0f)

        @JvmField
        val forward = Vector3Compat(0f, 0f, -1f)

        @JvmField
        val back = Vector3Compat(0f, 0f, 1f)

        /**
         * Create from Float3
         */
        @JvmStatic
        fun fromFloat3(vector: Float3): Vector3Compat {
            return Vector3Compat(vector.x, vector.y, vector.z)
        }

        /**
         * Linear interpolation between two vectors
         */
        @JvmStatic
        fun lerp(start: Vector3Compat, end: Vector3Compat, t: Float): Vector3Compat {
            return start.lerp(end, t)
        }

        /**
         * Distance between two points
         */
        @JvmStatic
        fun distance(a: Vector3Compat, b: Vector3Compat): Float {
            val dx = b.x - a.x
            val dy = b.y - a.y
            val dz = b.z - a.z
            return Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
        }

        /**
         * Dot product
         */
        @JvmStatic
        fun dot(a: Vector3Compat, b: Vector3Compat): Float {
            return a.dot(b)
        }

        /**
         * Cross product
         */
        @JvmStatic
        fun cross(a: Vector3Compat, b: Vector3Compat): Vector3Compat {
            return a.cross(b)
        }
    }
}
