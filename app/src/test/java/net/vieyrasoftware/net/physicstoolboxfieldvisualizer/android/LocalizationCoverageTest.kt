package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.*
import org.junit.Test
import org.w3c.dom.Element

class LocalizationCoverageTest {
    private val resources = File("src/main/res")
    private fun strings(folder: String): Map<String, Element> {
        val found = linkedMapOf<String, Element>()
        File(resources, folder).listFiles()!!.filter { it.extension == "xml" }.forEach { file ->
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            val nodes = document.getElementsByTagName("string")
            for (index in 0 until nodes.length) {
                val element = nodes.item(index) as Element
                val name = element.getAttribute("name")
                assertNull("Duplicate string $name in $folder", found.put(name, element))
            }
        }
        return found
    }

    @Test fun everyUserFacingStringHasSpanishTranslation() {
        val base = strings("values").filterValues { it.getAttribute("translatable") != "false" }
        val spanish = strings("values-es")
        assertEquals("Missing Spanish strings", emptySet<String>(), base.keys - spanish.keys)
        base.keys.forEach { key -> assertTrue("Empty Spanish string $key", spanish.getValue(key).textContent.isNotBlank()) }
    }

    @Test fun spanishFormattingArgumentsMatchEnglish() {
        val base = strings("values")
        val spanish = strings("values-es")
        // Ignore argument indexes/width/precision; compare conversion types for the existing templates.
        val format = Regex("%(?:[0-9]+\\$)?[-#+ 0,(]*[0-9]*(?:\\.[0-9]+)?([a-zA-Z])")
        for ((name, translated) in spanish) {
            val original = base[name] ?: continue
            fun types(text: String) = format.findAll(text).map { it.groupValues[1] }.sorted().toList()
            assertEquals("Format argument mismatch: $name", types(original.textContent), types(translated.textContent))
        }
    }
}
