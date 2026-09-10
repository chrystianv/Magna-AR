package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.fields;

import io.github.sceneview.node.Node;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.Field;

public class StrengthVaryingField implements Field {
    @Override
    public Vector3Compat sample(Vector3Compat world, Node root) {
        return new Vector3Compat(0, 0, (float) (System.currentTimeMillis() / 100 % 10) / 5);
    }
}
