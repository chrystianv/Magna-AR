package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.model.model.render

import android.content.Context
import com.google.ar.sceneform.Node

interface Model {
    fun node(): Node
    fun loadAsset(ctx: Context?)
}