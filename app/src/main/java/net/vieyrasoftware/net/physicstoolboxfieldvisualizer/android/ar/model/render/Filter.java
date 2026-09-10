package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.render;

import android.content.Context;

import io.github.sceneview.node.Node;

import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.Field;

import java.util.List;

public interface Filter {
    void update(List<Field> fields, Node root); // only call if mag sources changed

    Node node();

    void loadAsset(Context ctx);
}
