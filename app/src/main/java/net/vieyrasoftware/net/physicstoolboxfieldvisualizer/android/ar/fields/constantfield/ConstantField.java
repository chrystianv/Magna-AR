package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.fields.constantfield;

import io.github.sceneview.node.Node;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.Field;

public class ConstantField implements Field {
    private final Vector3Compat strength;

    public ConstantField(Vector3Compat strength) {
        this.strength = strength;
    }

    @Override
    public Vector3Compat sample(Vector3Compat world, Node root) {
        return new Vector3Compat(strength.x, strength.y, strength.z);
    }
}
