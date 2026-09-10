package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.fields;

import io.github.sceneview.node.Node;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.Field;

import java.util.ArrayList;
import java.util.List;

public class CombinedField implements Field {
    private List<Field> children;

    public CombinedField(List<Field> children) {
        this.children = new ArrayList<>(children);
    }

    public void addChild(Field child) {
        children.add(child);
    }

    public void removeChild(Field child) {
        children.remove(child);
    }

    public void clearChildren() {
        children.clear();
    }

    @Override
    public Vector3Compat sample(Vector3Compat world, Node root) {
        Vector3Compat sum = Vector3Compat.zero;
        for (Field child : children) {
            sum = sum.plus(child.sample(world, root));
        }
        return sum;
    }
}
