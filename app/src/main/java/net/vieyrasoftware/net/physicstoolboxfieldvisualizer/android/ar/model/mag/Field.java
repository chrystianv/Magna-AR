package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag;

import io.github.sceneview.node.Node;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat;

public interface Field {
    Vector3Compat sample(Vector3Compat world, Node root);
}
