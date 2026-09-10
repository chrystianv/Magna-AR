package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag;

import io.github.sceneview.node.Node;

import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.render.Model;

public interface Actor {
    Node asNode();

    boolean hasField();
    boolean hasModel();

    Field field();
    Model model();
}
