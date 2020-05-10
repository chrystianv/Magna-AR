package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.helpers

import androidx.lifecycle.MutableLiveData
import java.util.*

object LiveDataUtil {
    fun <T> fromOrDefault(mut: MutableLiveData<T>, d: T): T {
        return Optional.ofNullable(mut.value).orElse(d)
    }
}