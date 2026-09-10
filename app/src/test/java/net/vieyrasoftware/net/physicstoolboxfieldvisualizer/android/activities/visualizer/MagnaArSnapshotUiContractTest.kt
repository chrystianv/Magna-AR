package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.activities.visualizer

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element

class MagnaArSnapshotUiContractTest {

    @Test
    fun `snapshot shutter remains centered between compass and auto add`() {
        val layout = sequenceOf(
            File("src/main/res/layout/activity_visualizer.xml"),
            File("app/src/main/res/layout/activity_visualizer.xml"),
        ).first { it.isFile }
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(layout)
        val elements = (0 until document.getElementsByTagName("*").length)
            .mapNotNull { document.getElementsByTagName("*").item(it) as? Element }
        val shutter = elements.single {
            it.getAttributeNS(ANDROID_NAMESPACE, "id") == "@+id/snapshotButton"
        }

        assertEquals("70dp", shutter.androidAttribute("layout_width"))
        assertEquals("70dp", shutter.androidAttribute("layout_height"))
        assertEquals("25dp", shutter.androidAttribute("layout_marginBottom"))
        assertEquals("@string/magna_ar_capture_snapshot", shutter.androidAttribute("contentDescription"))
        assertEquals("parent", shutter.appAttribute("layout_constraintStart_toStartOf"))
        assertEquals("parent", shutter.appAttribute("layout_constraintEnd_toEndOf"))
        assertEquals("parent", shutter.appAttribute("layout_constraintBottom_toBottomOf"))
        assertEquals("@drawable/ic_camera_shutter", shutter.appAttribute("icon"))
        assertEquals("4dp", shutter.appAttribute("strokeWidth"))
    }

    private fun Element.androidAttribute(name: String) = getAttributeNS(ANDROID_NAMESPACE, name)

    private fun Element.appAttribute(name: String) = getAttributeNS(APP_NAMESPACE, name)

    private companion object {
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
        const val APP_NAMESPACE = "http://schemas.android.com/apk/res-auto"
    }
}
