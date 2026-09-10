package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.geometry

import android.graphics.Color
import com.google.android.filament.Box
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.IndexBuffer
import com.google.android.filament.RenderableManager
import com.google.android.filament.VertexBuffer
import dev.romainguy.kotlin.math.Float3
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * Programmatically creates a 3D arrow geometry for SceneView without needing model files.
 *
 * Arrow structure:
 * - Shaft: Cylinder (rectangular prism for simplicity)
 * - Head: Cone (pyramid)
 */
class ArrowGeometry {

    companion object {
        /**
         * Creates a simple arrow mesh programmatically.
         *
         * @param engine Filament engine
         * @param color Arrow color
         * @return Renderable entity ID
         */
        fun createArrow(engine: Engine, color: Int = Color.RED): Int {
            // Arrow dimensions
            val shaftRadius = 0.02f
            val shaftLength = 0.8f
            val headRadius = 0.06f
            val headLength = 0.2f

            // Create vertices for the arrow
            val vertices = createArrowVertices(shaftRadius, shaftLength, headRadius, headLength)
            val indices = createArrowIndices()

            // Create vertex buffer
            val vertexBuffer = VertexBuffer.Builder()
                .vertexCount(vertices.size / 6) // 3 for position, 3 for normal
                .bufferCount(1)
                .attribute(
                    VertexBuffer.VertexAttribute.POSITION,
                    0,
                    VertexBuffer.AttributeType.FLOAT3,
                    0,
                    24
                ) // stride = 6 floats * 4 bytes = 24
                .attribute(
                    VertexBuffer.VertexAttribute.TANGENTS,
                    0,
                    VertexBuffer.AttributeType.FLOAT3,
                    12,
                    24
                ) // offset = 3 floats * 4 bytes = 12
                .build(engine)

            val vertexData = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices)
            vertexData.flip()

            vertexBuffer.setBufferAt(engine, 0, vertexData)

            // Create index buffer
            val indexBuffer = IndexBuffer.Builder()
                .indexCount(indices.size)
                .bufferType(IndexBuffer.Builder.IndexType.USHORT)
                .build(engine)

            val indexData = ByteBuffer.allocateDirect(indices.size * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(indices)
            indexData.flip()

            indexBuffer.setBuffer(engine, indexData)

            // Create entity and renderable
            val entity = EntityManager.get().create()

            RenderableManager.Builder(1)
                .boundingBox(Box(0f, 0f, 0f, 1f, 1f, 1f))
                .geometry(
                    0,
                    RenderableManager.PrimitiveType.TRIANGLES,
                    vertexBuffer,
                    indexBuffer,
                    0,
                    indices.size
                )
                .culling(false)
                .receiveShadows(false)
                .castShadows(false)
                .build(engine, entity)

            return entity
        }

        /**
         * Creates vertices for arrow (position + normal interleaved).
         * Format: [x, y, z, nx, ny, nz, x, y, z, nx, ny, nz, ...]
         */
        private fun createArrowVertices(
            shaftRadius: Float,
            shaftLength: Float,
            headRadius: Float,
            headLength: Float
        ): FloatArray {
            val vertices = mutableListOf<Float>()

            // Simplified arrow as two rectangular prisms (shaft and head)
            // This is a basic implementation - can be enhanced with cylinders later

            // Shaft (rectangular prism from 0 to shaftLength)
            addBox(
                vertices,
                Float3(0f, 0f, 0f),
                Float3(shaftRadius * 2, shaftRadius * 2, shaftLength)
            )

            // Head (pyramid at the tip)
            addPyramid(
                vertices,
                Float3(0f, 0f, shaftLength),
                headRadius,
                headLength
            )

            return vertices.toFloatArray()
        }

        /**
         * Add a box to the vertex list
         */
        private fun addBox(vertices: MutableList<Float>, center: Float3, size: Float3) {
            val halfSize = size * 0.5f

            // 8 corners of the box
            val corners = listOf(
                Float3(-halfSize.x, -halfSize.y, -halfSize.z),
                Float3(halfSize.x, -halfSize.y, -halfSize.z),
                Float3(halfSize.x, halfSize.y, -halfSize.z),
                Float3(-halfSize.x, halfSize.y, -halfSize.z),
                Float3(-halfSize.x, -halfSize.y, halfSize.z),
                Float3(halfSize.x, -halfSize.y, halfSize.z),
                Float3(halfSize.x, halfSize.y, halfSize.z),
                Float3(-halfSize.x, halfSize.y, halfSize.z)
            ).map { it + center }

            // Add faces with normals
            // Front face (normal = +Z)
            addVertex(vertices, corners[4], Float3(0f, 0f, 1f))
            addVertex(vertices, corners[5], Float3(0f, 0f, 1f))
            addVertex(vertices, corners[6], Float3(0f, 0f, 1f))
            addVertex(vertices, corners[7], Float3(0f, 0f, 1f))

            // Back face (normal = -Z)
            addVertex(vertices, corners[0], Float3(0f, 0f, -1f))
            addVertex(vertices, corners[3], Float3(0f, 0f, -1f))
            addVertex(vertices, corners[2], Float3(0f, 0f, -1f))
            addVertex(vertices, corners[1], Float3(0f, 0f, -1f))

            // Other faces...
            // (Simplified - add remaining 4 faces for complete box)
        }

        /**
         * Add a pyramid (cone) to the vertex list
         */
        private fun addPyramid(
            vertices: MutableList<Float>,
            base: Float3,
            radius: Float,
            height: Float
        ) {
            val tip = Float3(base.x, base.y, base.z + height)

            // 4 corners of square base
            val baseCorners = listOf(
                Float3(base.x - radius, base.y - radius, base.z),
                Float3(base.x + radius, base.y - radius, base.z),
                Float3(base.x + radius, base.y + radius, base.z),
                Float3(base.x - radius, base.y + radius, base.z)
            )

            // Add pyramid faces
            for (i in 0 until 4) {
                val j = (i + 1) % 4
                val normal = calculateNormal(baseCorners[i], baseCorners[j], tip)
                addVertex(vertices, baseCorners[i], normal)
                addVertex(vertices, baseCorners[j], normal)
                addVertex(vertices, tip, normal)
            }
        }

        private fun addVertex(vertices: MutableList<Float>, position: Float3, normal: Float3) {
            vertices.add(position.x)
            vertices.add(position.y)
            vertices.add(position.z)
            vertices.add(normal.x)
            vertices.add(normal.y)
            vertices.add(normal.z)
        }

        private fun calculateNormal(v1: Float3, v2: Float3, v3: Float3): Float3 {
            val edge1 = v2 - v1
            val edge2 = v3 - v1
            val crossed = cross(edge1, edge2)
            val length = kotlin.math.sqrt(crossed.x * crossed.x + crossed.y * crossed.y + crossed.z * crossed.z)
            return if (length > 0.0001f) crossed / length else Float3(0f, 1f, 0f)
        }

        private fun cross(a: Float3, b: Float3): Float3 {
            return Float3(
                a.y * b.z - a.z * b.y,
                a.z * b.x - a.x * b.z,
                a.x * b.y - a.y * b.x
            )
        }

        /**
         * Creates triangle indices for the arrow mesh
         */
        private fun createArrowIndices(): ShortArray {
            // Simplified indices for the basic geometry
            // This matches the vertex order from createArrowVertices
            val indices = mutableListOf<Short>()

            // Add indices for shaft faces
            // Front face
            indices.addAll(listOf(0, 1, 2, 0, 2, 3))
            // Back face
            indices.addAll(listOf(4, 5, 6, 4, 6, 7))

            // Add indices for pyramid faces (4 triangles)
            var offset: Short = 8
            for (i in 0 until 4) {
                indices.add(offset++)
                indices.add(offset++)
                indices.add(offset++)
            }

            return indices.toShortArray()
        }

        /**
         * Simple alternative: Create arrow using basic shapes
         * This is even simpler and doesn't require complex geometry
         */
        fun createSimpleArrow(color: Int = Color.RED): SimpleArrowData {
            return SimpleArrowData(
                shaftLength = 0.8f,
                shaftRadius = 0.02f,
                headLength = 0.2f,
                headRadius = 0.06f,
                color = color
            )
        }
    }

    data class SimpleArrowData(
        val shaftLength: Float,
        val shaftRadius: Float,
        val headLength: Float,
        val headRadius: Float,
        val color: Int
    )
}
