package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.render;

import android.content.Context;

import io.github.sceneview.node.Node;

public interface Model {
    Node node();

    void loadAsset(Context ctx);
}
