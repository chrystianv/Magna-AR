package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.fields;

import io.github.sceneview.node.Node;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.Field;

import java.util.ArrayList;

public class DataBackedField implements Field {
    public DataBackedField(ArrayList<Vector3Compat> magData, ArrayList<Vector3Compat> locData) {
    }

    @Override
    public Vector3Compat sample(Vector3Compat world, Node root) {
        // TODO: Interpolate
        return null;
    }
}
