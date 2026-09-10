package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.fields;

import io.github.sceneview.node.Node;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.Field;

public class DirectionVaryingField implements Field {
    @Override
    public Vector3Compat sample(Vector3Compat world, Node root) {
        double curr = System.currentTimeMillis() * 0.0001;
        return new Vector3Compat((float) Math.sin(curr), (float) Math.cos(curr), 0f);
    }
}
