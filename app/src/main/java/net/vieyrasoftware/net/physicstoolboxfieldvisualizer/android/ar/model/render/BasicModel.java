package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.render;

import io.github.sceneview.node.Node;
import io.github.sceneview.node.ModelNode;

/**
 * BasicModel migrated to SceneView.
 *
 * Note: SceneView doesn't have TransformableNode/TransformationSystem.
 * For transformable behavior, you'll need to implement touch gestures manually
 * or use SceneView's gesture detection on the node.
 */
public abstract class BasicModel implements Model {
    private Node mine;

    public BasicModel() {
        // Create a placeholder node using the same pattern as StaticArrowModel
        try {
            mine = Node.class.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // Fallback - will be replaced when added to scene
            mine = null;
        }
    }

    @Override
    public Node node() {
        return mine;
    }

    protected final Node getNode() {
        return mine;
    }

    protected final ModelNode getModelNode() {
        if (mine instanceof ModelNode) {
            return (ModelNode) mine;
        }
        return null;
    }
}
