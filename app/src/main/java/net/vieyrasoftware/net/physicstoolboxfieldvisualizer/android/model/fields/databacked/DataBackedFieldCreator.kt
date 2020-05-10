package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.fields.databacked

import androidx.lifecycle.Observer
import com.google.ar.sceneform.math.Vector3
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.fields.databacked.DataBackedField
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.FieldGroup
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.mag.Field
import java.util.*

internal class DataBackedFieldCreator(private val fields: FieldGroup) : Observer<FloatArray> {
    private val magData: ArrayList<Vector3> = arrayListOf()
    private val locData: ArrayList<Vector3> = arrayListOf()
    fun validState(): Boolean {
        return false // TODO: return true if enough data, else false.
    }

    fun create(): Field {
        return DataBackedField(magData, locData)
    }

    override fun onChanged(field: FloatArray) {
        magData.add(fields.worldToRootDirection(Vector3(field[0], field[1], field[2])))
        // TODO: record location data as well, somehow.
    }
}