package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat

import dev.romainguy.kotlin.math.Quaternion
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.acos

/**
 * Compatibility class to ease migration from Sceneform Quaternion to SceneView Quaternion.
 * Provides similar API to com.google.ar.sceneform.math.Quaternion
 */
data class QuaternionCompat(var x: Float, var y: Float, var z: Float, var w: Float) {

    constructor() : this(0f, 0f, 0f, 1f)

    constructor(quat: Quaternion) : this(quat.x, quat.y, quat.z, quat.w)

    /**
     * Convert to SceneView Quaternion
     */
    fun toQuaternion(): Quaternion = Quaternion(x, y, z, w)

    /**
     * Set from Quaternion
     */
    fun set(quat: Quaternion) {
        x = quat.x
        y = quat.y
        z = quat.z
        w = quat.w
    }

    /**
     * Set individual components
     */
    fun set(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    /**
     * Quaternion multiplication
     */
    operator fun times(other: QuaternionCompat): QuaternionCompat {
        return QuaternionCompat(
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w,
            w * other.w - x * other.x - y * other.y - z * other.z
        )
    }

    /**
     * Rotate a vector by this quaternion
     */
    fun rotateVector(vec: Vector3Compat): Vector3Compat {
        val qx = x.toDouble()
        val qy = y.toDouble()
        val qz = z.toDouble()
        val qw = w.toDouble()

        val vx = vec.x.toDouble()
        val vy = vec.y.toDouble()
        val vz = vec.z.toDouble()

        // v + 2 * cross(q.xyz, cross(q.xyz, v) + q.w * v)
        val uvx = qy * vz - qz * vy
        val uvy = qz * vx - qx * vz
        val uvz = qx * vy - qy * vx

        val uuvx = qy * uvz - qz * uvy
        val uuvy = qz * uvx - qx * uvz
        val uuvz = qx * uvy - qy * uvx

        val w2 = qw * 2.0
        val ux = uvx * w2
        val uy = uvy * w2
        val uz = uvz * w2

        return Vector3Compat(
            (vx + (uuvx + ux) * 2.0).toFloat(),
            (vy + (uuvy + uy) * 2.0).toFloat(),
            (vz + (uuvz + uz) * 2.0).toFloat()
        )
    }

    /**
     * Get the conjugate of this quaternion
     */
    fun conjugate(): QuaternionCompat {
        return QuaternionCompat(-x, -y, -z, w)
    }

    /**
     * Get the inverse of this quaternion
     */
    fun inverted(): QuaternionCompat {
        val norm = x * x + y * y + z * z + w * w
        if (norm > 0.0001f) {
            val invNorm = 1.0f / norm
            return QuaternionCompat(-x * invNorm, -y * invNorm, -z * invNorm, w * invNorm)
        }
        return identity
    }

    /**
     * Normalize the quaternion
     */
    fun normalized(): QuaternionCompat {
        val norm = sqrt(x * x + y * y + z * z + w * w)
        if (norm > 0.0001f) {
            return QuaternionCompat(x / norm, y / norm, z / norm, w / norm)
        }
        return identity
    }

    /**
     * Spherical linear interpolation
     */
    fun slerp(end: QuaternionCompat, t: Float): QuaternionCompat {
        var cosom = x * end.x + y * end.y + z * end.z + w * end.w
        var endCopy = end

        // Adjust signs if necessary
        if (cosom < 0.0f) {
            cosom = -cosom
            endCopy = QuaternionCompat(-end.x, -end.y, -end.z, -end.w)
        }

        val scale0: Float
        val scale1: Float

        if (1.0f - cosom > 0.0001f) {
            // Standard case (slerp)
            val omega = acos(cosom)
            val sinom = sin(omega)
            scale0 = sin((1.0f - t) * omega) / sinom
            scale1 = sin(t * omega) / sinom
        } else {
            // Very close, just linear interpolate
            scale0 = 1.0f - t
            scale1 = t
        }

        return QuaternionCompat(
            scale0 * x + scale1 * endCopy.x,
            scale0 * y + scale1 * endCopy.y,
            scale0 * z + scale1 * endCopy.z,
            scale0 * w + scale1 * endCopy.w
        )
    }

    companion object {
        @JvmField
        val identity = QuaternionCompat(0f, 0f, 0f, 1f)

        /**
         * Create from Quaternion
         */
        @JvmStatic
        fun fromQuaternion(quat: Quaternion): QuaternionCompat {
            return QuaternionCompat(quat.x, quat.y, quat.z, quat.w)
        }

        /**
         * Create quaternion from axis and angle
         */
        @JvmStatic
        fun axisAngle(axis: Vector3Compat, angleDegrees: Float): QuaternionCompat {
            val angleRadians = Math.toRadians(angleDegrees.toDouble()).toFloat()
            val halfAngle = angleRadians * 0.5f
            val sinHalfAngle = sin(halfAngle)

            val normalized = axis.normalized()

            return QuaternionCompat(
                normalized.x * sinHalfAngle,
                normalized.y * sinHalfAngle,
                normalized.z * sinHalfAngle,
                cos(halfAngle)
            )
        }

        /**
         * Create quaternion from Euler angles (degrees)
         */
        @JvmStatic
        fun euler(pitch: Float, yaw: Float, roll: Float): QuaternionCompat {
            val pitchRad = Math.toRadians(pitch.toDouble()).toFloat()
            val yawRad = Math.toRadians(yaw.toDouble()).toFloat()
            val rollRad = Math.toRadians(roll.toDouble()).toFloat()

            val cy = cos(yawRad * 0.5f)
            val sy = sin(yawRad * 0.5f)
            val cp = cos(pitchRad * 0.5f)
            val sp = sin(pitchRad * 0.5f)
            val cr = cos(rollRad * 0.5f)
            val sr = sin(rollRad * 0.5f)

            return QuaternionCompat(
                cy * sp * cr + sy * cp * sr,
                sy * cp * cr - cy * sp * sr,
                cy * cp * sr - sy * sp * cr,
                cy * cp * cr + sy * sp * sr
            )
        }

        /**
         * Look at rotation
         */
        @JvmStatic
        fun lookRotation(forward: Vector3Compat, up: Vector3Compat = Vector3Compat.up): QuaternionCompat {
            val f = forward.normalized()
            val r = up.cross(f).normalized()
            val u = f.cross(r)

            val trace = r.x + u.y + f.z

            return if (trace > 0.0f) {
                val s = sqrt(trace + 1.0f) * 2.0f
                QuaternionCompat(
                    (u.z - f.y) / s,
                    (f.x - r.z) / s,
                    (r.y - u.x) / s,
                    0.25f * s
                )
            } else if (r.x > u.y && r.x > f.z) {
                val s = sqrt(1.0f + r.x - u.y - f.z) * 2.0f
                QuaternionCompat(
                    0.25f * s,
                    (r.y + u.x) / s,
                    (f.x + r.z) / s,
                    (u.z - f.y) / s
                )
            } else if (u.y > f.z) {
                val s = sqrt(1.0f + u.y - r.x - f.z) * 2.0f
                QuaternionCompat(
                    (r.y + u.x) / s,
                    0.25f * s,
                    (u.z + f.y) / s,
                    (f.x - r.z) / s
                )
            } else {
                val s = sqrt(1.0f + f.z - r.x - u.y) * 2.0f
                QuaternionCompat(
                    (f.x + r.z) / s,
                    (u.z + f.y) / s,
                    0.25f * s,
                    (r.y - u.x) / s
                )
            }
        }

        /**
         * Spherical linear interpolation
         */
        @JvmStatic
        fun slerp(start: QuaternionCompat, end: QuaternionCompat, t: Float): QuaternionCompat {
            return start.slerp(end, t)
        }
    }
}
