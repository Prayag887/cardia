package studio.cardia.core

import kotlinx.serialization.json.*
import kotlin.math.sqrt

/** One triangle primitive of a glTF node, flattened into world space. [path] lists node names root→self. */
class GlbMesh(val node: String, val path: List<String>, val positions: FloatArray, val normals: FloatArray, val indices: IntArray, val color: FloatArray)

/**
 * Minimal binary glTF 2.0 reader: triangle primitives, interleaved or tightly packed float
 * attributes, u8/u16/u32 indices, node TRS/matrix transforms and base colour factors.
 * No textures, skins, morph targets, sparse accessors or compression extensions.
 */
object Glb {
    fun parse(bytes: ByteArray): List<GlbMesh> {
        require(bytes.size >= 28 && u32(bytes, 0) == 0x46546C67) { "Not a binary glTF file" }
        require(u32(bytes, 4) == 2) { "Only glTF 2.0 is supported" }
        val jsonLength = u32(bytes, 12)
        require(u32(bytes, 16) == 0x4E4F534A) { "First GLB chunk must be JSON" }
        val gltf = Json.parseToJsonElement(bytes.decodeToString(20, 20 + jsonLength)).jsonObject
        val binHeader = 20 + jsonLength
        require(u32(bytes, binHeader + 4) == 0x004E4942) { "Missing BIN chunk" }
        val bin = binHeader + 8
        val required = gltf["extensionsRequired"]?.jsonArray.orEmpty()
        require(required.isEmpty()) { "Unsupported required extensions: $required" }

        val nodes = gltf.array("nodes"); val meshes = gltf.array("meshes")
        val accessors = gltf.array("accessors"); val views = gltf.array("bufferViews")
        val materials = gltf.array("materials")

        fun read(index: Int): Pair<FloatArray, Int> {
            val a = accessors[index].jsonObject
            require("sparse" !in a) { "Sparse accessors are not supported" }
            val count = a.int("count"); val type = a.str("type"); val component = a.int("componentType")
            val width = when (type) { "SCALAR" -> 1; "VEC2" -> 2; "VEC3" -> 3; "VEC4" -> 4; else -> error("Unsupported accessor type $type") }
            val size = when (component) { 5126, 5125 -> 4; 5123 -> 2; 5121 -> 1; else -> error("Unsupported component type $component") }
            val view = views[a.int("bufferView")].jsonObject
            val stride = view["byteStride"]?.jsonPrimitive?.int ?: (width * size)
            val start = bin + (view["byteOffset"]?.jsonPrimitive?.int ?: 0) + (a["byteOffset"]?.jsonPrimitive?.int ?: 0)
            val out = FloatArray(count * width)
            for (i in 0 until count) for (c in 0 until width) {
                val p = start + i * stride + c * size
                out[i * width + c] = when (component) {
                    5126 -> Float.fromBits(u32(bytes, p))
                    5125 -> u32(bytes, p).toUInt().toFloat()
                    5123 -> ((bytes[p].toInt() and 0xFF) or ((bytes[p + 1].toInt() and 0xFF) shl 8)).toFloat()
                    else -> (bytes[p].toInt() and 0xFF).toFloat()
                }
            }
            return out to width
        }
        fun readIndices(index: Int): IntArray {
            val a = accessors[index].jsonObject
            val view = views[a.int("bufferView")].jsonObject
            val start = bin + (view["byteOffset"]?.jsonPrimitive?.int ?: 0) + (a["byteOffset"]?.jsonPrimitive?.int ?: 0)
            val count = a.int("count")
            return when (a.int("componentType")) {
                5125 -> IntArray(count) { u32(bytes, start + it * 4) }
                5123 -> IntArray(count) { (bytes[start + it * 2].toInt() and 0xFF) or ((bytes[start + it * 2 + 1].toInt() and 0xFF) shl 8) }
                5121 -> IntArray(count) { bytes[start + it].toInt() and 0xFF }
                else -> error("Unsupported index type")
            }
        }

        val result = mutableListOf<GlbMesh>()
        fun visit(index: Int, parent: FloatArray, path: List<String>) {
            val node = nodes[index].jsonObject
            val name = node["name"]?.jsonPrimitive?.content ?: "node$index"
            val world = Mat4.multiply(parent, localMatrix(node))
            val here = path + name
            node["mesh"]?.jsonPrimitive?.int?.let { meshIndex ->
                for (primitive in meshes[meshIndex].jsonObject.array("primitives")) {
                    val p = primitive.jsonObject
                    if ((p["mode"]?.jsonPrimitive?.int ?: 4) != 4) continue
                    val attributes = p["attributes"]!!.jsonObject
                    val positions = read(attributes.int("POSITION")).first
                    val vertexCount = positions.size / 3
                    val indices = p["indices"]?.jsonPrimitive?.int?.let(::readIndices) ?: IntArray(vertexCount) { it }
                    val normals = attributes["NORMAL"]?.jsonPrimitive?.int?.let { read(it).first } ?: computeNormals(positions, indices)
                    transform(world, positions, normals)
                    val factor = p["material"]?.jsonPrimitive?.int?.let { materials.getOrNull(it)?.jsonObject }
                        ?.get("pbrMetallicRoughness")?.jsonObject?.get("baseColorFactor")?.jsonArray?.map { it.jsonPrimitive.float }
                    // glTF base colours are linear; the renderer works in display (sRGB-like) space.
                    val color = FloatArray(3) { c -> factor?.getOrNull(c)?.let { srgb(it) } ?: .75f }
                    result += GlbMesh(name, here, positions, normals, indices, color)
                }
            }
            node["children"]?.jsonArray?.forEach { visit(it.jsonPrimitive.int, world, here) }
        }
        val scene = gltf["scene"]?.jsonPrimitive?.int ?: 0
        val roots = gltf["scenes"]?.jsonArray?.getOrNull(scene)?.jsonObject?.array("nodes")?.map { it.jsonPrimitive.int }
            ?: nodes.indices.toList()
        roots.forEach { visit(it, Mat4.identity(), emptyList()) }
        return result
    }

    private fun srgb(linear: Float): Float = if (linear <= .0031308f) linear * 12.92f else (1.055 * kotlin.math.exp(kotlin.math.ln(linear.toDouble()) / 2.4) - .055).toFloat()

    private fun localMatrix(node: JsonObject): FloatArray {
        node["matrix"]?.jsonArray?.let { m -> return FloatArray(16) { m[it].jsonPrimitive.float } }
        val t = node["translation"]?.jsonArray?.map { it.jsonPrimitive.float } ?: listOf(0f, 0f, 0f)
        val r = node["rotation"]?.jsonArray?.map { it.jsonPrimitive.float } ?: listOf(0f, 0f, 0f, 1f)
        val s = node["scale"]?.jsonArray?.map { it.jsonPrimitive.float } ?: listOf(1f, 1f, 1f)
        val (x, y, z, w) = r
        // Column-major T * R * S.
        return floatArrayOf(
            (1 - 2 * (y * y + z * z)) * s[0], (2 * (x * y + z * w)) * s[0], (2 * (x * z - y * w)) * s[0], 0f,
            (2 * (x * y - z * w)) * s[1], (1 - 2 * (x * x + z * z)) * s[1], (2 * (y * z + x * w)) * s[1], 0f,
            (2 * (x * z + y * w)) * s[2], (2 * (y * z - x * w)) * s[2], (1 - 2 * (x * x + y * y)) * s[2], 0f,
            t[0], t[1], t[2], 1f
        )
    }

    /** Applies [m] to positions and its 3x3 part to normals (exact for rotation + uniform scale, which HRA uses). */
    private fun transform(m: FloatArray, positions: FloatArray, normals: FloatArray) {
        if (Mat4.isIdentity(m)) return
        for (i in positions.indices step 3) {
            val x = positions[i]; val y = positions[i + 1]; val z = positions[i + 2]
            positions[i] = m[0] * x + m[4] * y + m[8] * z + m[12]
            positions[i + 1] = m[1] * x + m[5] * y + m[9] * z + m[13]
            positions[i + 2] = m[2] * x + m[6] * y + m[10] * z + m[14]
            val nx = normals[i]; val ny = normals[i + 1]; val nz = normals[i + 2]
            val a = m[0] * nx + m[4] * ny + m[8] * nz; val b = m[1] * nx + m[5] * ny + m[9] * nz; val c = m[2] * nx + m[6] * ny + m[10] * nz
            val l = sqrt(a * a + b * b + c * c).takeIf { it > 0f } ?: 1f
            normals[i] = a / l; normals[i + 1] = b / l; normals[i + 2] = c / l
        }
    }

    fun computeNormals(positions: FloatArray, indices: IntArray): FloatArray {
        val n = FloatArray(positions.size)
        for (t in indices.indices step 3) {
            val a = indices[t] * 3; val b = indices[t + 1] * 3; val c = indices[t + 2] * 3
            val ux = positions[b] - positions[a]; val uy = positions[b + 1] - positions[a + 1]; val uz = positions[b + 2] - positions[a + 2]
            val vx = positions[c] - positions[a]; val vy = positions[c + 1] - positions[a + 1]; val vz = positions[c + 2] - positions[a + 2]
            val x = uy * vz - uz * vy; val y = uz * vx - ux * vz; val z = ux * vy - uy * vx
            for (v in intArrayOf(a, b, c)) { n[v] += x; n[v + 1] += y; n[v + 2] += z }
        }
        for (i in n.indices step 3) { val l = sqrt(n[i] * n[i] + n[i + 1] * n[i + 1] + n[i + 2] * n[i + 2]).takeIf { it > 0f } ?: 1f; n[i] /= l; n[i + 1] /= l; n[i + 2] /= l }
        return n
    }

    private fun u32(b: ByteArray, p: Int) = (b[p].toInt() and 0xFF) or ((b[p + 1].toInt() and 0xFF) shl 8) or ((b[p + 2].toInt() and 0xFF) shl 16) or ((b[p + 3].toInt() and 0xFF) shl 24)
    private fun JsonObject.array(key: String): JsonArray = this[key]?.jsonArray ?: JsonArray(emptyList())
    private fun JsonObject.int(key: String): Int = this[key]!!.jsonPrimitive.int
    private fun JsonObject.str(key: String): String = this[key]!!.jsonPrimitive.content
}

/** Column-major 4x4 helpers, matching glTF and android.opengl.Matrix layout. */
object Mat4 {
    fun identity() = FloatArray(16).also { it[0] = 1f; it[5] = 1f; it[10] = 1f; it[15] = 1f }
    fun isIdentity(m: FloatArray) = m.contentEquals(identity())
    fun multiply(a: FloatArray, b: FloatArray) = FloatArray(16) { i ->
        val col = i / 4; val row = i % 4
        (0..3).fold(0f) { acc, k -> acc + a[k * 4 + row] * b[col * 4 + k] }
    }
}
