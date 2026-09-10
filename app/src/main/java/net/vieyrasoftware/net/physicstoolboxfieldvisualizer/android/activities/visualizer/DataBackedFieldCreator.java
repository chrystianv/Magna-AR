package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer;

import androidx.lifecycle.Observer;

import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.fields.DataBackedField;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.FieldGroup;
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag.Field;

import java.util.ArrayList;

class DataBackedFieldCreator implements Observer<float[]> {
    private FieldGroup fields;
    private ArrayList<Vector3Compat> magData;
    private ArrayList<Vector3Compat> locData;

    public DataBackedFieldCreator(FieldGroup fields) {
        this.fields = fields;
    }

    public boolean validState() {
        return false; // TODO: return true if enough data, else false.
    }

    public Field create() {
        return new DataBackedField(magData, locData);
    }

    @Override
    public void onChanged(float[] field) {
        magData.add(fields.worldToRootDirection(new Vector3Compat(field[0], field[1], field[2])));
        // TODO: record location data as well, somehow.
    }
}
