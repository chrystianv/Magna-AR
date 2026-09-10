package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import android.content.Context
import android.util.AttributeSet
import io.github.sceneview.ar.ARSceneView

/** Release application-owned nodes before SceneView's lifecycle callback destroys its engine. */
class MagnaArSceneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ARSceneView(context, attrs, defStyleAttr) {
    var beforeDestroy: (() -> Unit)? = null
    override fun destroy() {
        val cleanup = beforeDestroy
        beforeDestroy = null
        try { cleanup?.invoke() } finally { super.destroy() }
    }
}
