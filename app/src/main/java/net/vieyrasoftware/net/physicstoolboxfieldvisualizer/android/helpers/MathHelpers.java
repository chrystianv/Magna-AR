package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers;

import com.google.ar.core.Pose;
import java.util.Arrays;

/**
 * Created by inio on 2/28/18.
 */

public class MathHelpers {
    protected MathHelpers() {}

    /**
     * Returns a pose rotating about the origin so that the point {@code from} is rotated to be
     * colinear with the origin and {@code to}.  Rotation takes the shortest path.
     */
    public static Pose rotateBetween(float[] fromRaw, float[] toRaw) {
        float[] from = Arrays.copyOf(fromRaw, 3);
        normalize(from);
        float[] to = Arrays.copyOf(toRaw, 3);
        normalize(to);

        float[] cross = new float[3];
        cross[0] = from[1]*to[2] - from[2]*to[1];
        cross[1] = from[2]*to[0] - from[0]*to[2];
        cross[2] = from[0]*to[1] - from[1]*to[0];
        float dot = from[0]*to[0] + from[1]*to[1] + from[2]*to[2];
        float angle = (float)Math.atan2(norm(cross), dot);
        normalize(cross);

        float sinhalf = (float)Math.sin(angle/2.0f);
        float coshalf = (float)Math.cos(angle/2.0f);

        return Pose.makeRotation(cross[0]*sinhalf, cross[1]*sinhalf, cross[2]*sinhalf, coshalf);
    }

    public static Pose axisRotation(int axis, float angleRad) {
        float sinHalf = (float)Math.sin(angleRad/2);
        float cosHalf = (float)Math.cos(angleRad/2);

        switch(axis) {
            case 0:
                return Pose.makeRotation(sinHalf, 0, 0, cosHalf);
            case 1:
                return Pose.makeRotation(0, sinHalf, 0, cosHalf);
            case 2:
                return Pose.makeRotation(0, 0, sinHalf, cosHalf);
            default:
                throw new IllegalArgumentException("invalid axis "+axis);
        }
    }

    /** Returns the 2-norm of the input array. */
    public static float norm(float[] in) {
        float sum = 0;
        for (float f : in) {
            sum += f * f;
        }
        return (float)Math.sqrt(sum);
    }

    /** Normalizes the input array in-place. */
    public static void normalize(float[] in) {
        float scale = 1/norm(in);
        for (int i=0; i<in.length; ++i) {
            in[i] *= scale;
        }
    }
}
