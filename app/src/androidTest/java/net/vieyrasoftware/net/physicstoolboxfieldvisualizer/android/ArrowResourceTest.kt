package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer.MagnaArSceneView
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.actors.stablemagarrow.model.StaticArrowModel
import net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.compat.Vector3Compat

@RunWith(AndroidJUnit4::class)
class ArrowResourceTest {
    @Test fun repeatedArrowDisposalReturnsNativeResourcesToBaseline() = runBlocking {
        withContext(Dispatchers.Main) {
            val scene = MagnaArSceneView(InstrumentationRegistry.getInstrumentation().targetContext)
            try {
                val lights = scene.engine.lightManager.componentCount
                repeat(20) {
                    val arrow = StaticArrowModel(Vector3Compat.zero, Vector3Compat(20f, 30f, 40f), true)
                    scene.beforeDestroy = { arrow.destroy() }
                    withTimeout(10_000) { arrow.loadModel(scene, this).join() }
                    val asset = arrow.node().childNodes.filterIsInstance<io.github.sceneview.node.ModelNode>().single().model
                    val entities = asset.entities.copyOf()
                    assertTrue("Arrow asset must actually load", entities.any { scene.engine.renderableManager.hasComponent(it) })
                    assertTrue(scene.engine.lightManager.componentCount > lights)
                    arrow.destroy()
                    arrow.destroy() // Repeated lifecycle cleanup must be harmless.
                    assertTrue("Disposed asset must have no renderables", entities.none { scene.engine.renderableManager.hasComponent(it) })
                    assertEquals(lights, scene.engine.lightManager.componentCount)
                    scene.beforeDestroy = null
                }
                val pending = StaticArrowModel(Vector3Compat.zero, Vector3Compat(20f, 30f, 40f), false)
                val job = pending.loadModel(scene, this)
                var releasedBeforeEngine = false
                scene.beforeDestroy = { pending.destroy(); releasedBeforeEngine = true }
                scene.destroy()
                job.join()
                assertTrue(releasedBeforeEngine)
            } finally { scene.destroy() }
        }
    }
}
