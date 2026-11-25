package studio.cardia.core

import kotlin.math.*

/** A mesh in normalized scene units (scene fits a unit sphere-ish box, centred at the origin). */
class ScenePart(
    val structure: String, val node: String, val path: List<String>,
    val positions: FloatArray, val normals: FloatArray, val indices: IntArray, val color: FloatArray,
    val min: FloatArray, val max: FloatArray, val centroid: FloatArray
)

class Bounds(val min: FloatArray, val max: FloatArray) {
    val center get() = FloatArray(3) { (min[it] + max[it]) / 2 }
    val radius get() = (0..2).maxOf { (max[it] - min[it]) / 2 }.coerceAtLeast(1e-4f)
}

/**
 * All meshes for one organ in a shared normalized frame. HRA coordinates are metres with
 * +Y superior, +Z anterior and +X toward the patient's left; normalization only translates and
 * uniformly scales, so anatomical relationships between files are preserved.
 */
class AnatomyModel(val organ: Organ, val parts: List<ScenePart>, val unmapped: List<String>, val metresPerUnit: Float) {
    val triangles get() = parts.sumOf { it.indices.size / 3 }

    /** Bounds of the given structure ids or glTF node names (a node matches every mesh beneath it). All parts when empty. */
    fun bounds(targets: Collection<String>? = null): Bounds? {
        val chosen = if (targets.isNullOrEmpty()) parts else parts.filter { p -> p.structure in targets || targets.any { it in p.path } }
        if (chosen.isEmpty()) return null
        return Bounds(FloatArray(3) { i -> chosen.minOf { it.min[i] } }, FloatArray(3) { i -> chosen.maxOf { it.max[i] } })
    }

    /** Resolves an anchor against the node or any mesh beneath it. */
    fun anchor(anchor: Anchor): FloatArray? {
        val meshes = parts.filter { anchor.node in it.path }
        if (meshes.isEmpty()) return null
        return when (anchor.at) {
            AnchorAt.Centroid -> {
                var n = 0; val sum = FloatArray(3)
                meshes.forEach { m -> for (i in m.positions.indices step 3) { sum[0] += m.positions[i]; sum[1] += m.positions[i + 1]; sum[2] += m.positions[i + 2]; n++ } }
                FloatArray(3) { sum[it] / n }
            }
            AnchorAt.Top, AnchorAt.Bottom -> {
                var best: FloatArray? = null
                meshes.forEach { m -> for (i in m.positions.indices step 3) {
                    val y = m.positions[i + 1]
                    if (best == null || (anchor.at == AnchorAt.Top && y > best!![1]) || (anchor.at == AnchorAt.Bottom && y < best!![1]))
                        best = floatArrayOf(m.positions[i], y, m.positions[i + 2])
                } }
                best
            }
        }
    }

    /** Plane (normal xyz, offset w) that keeps points where dot(p, n) <= w. */
    fun plane(cut: Cut): FloatArray {
        val axis = when (cut.axis) { Axis.Coronal -> 2; Axis.Sagittal -> 0; Axis.Transverse -> 1 }
        val base = cut.through?.let { bounds(listOf(it))?.center?.get(axis) } ?: 0f
        val at = base + cut.shift
        return when (cut.axis) {
            Axis.Coronal -> floatArrayOf(0f, 0f, 1f, at)       // remove anterior
            Axis.Sagittal -> floatArrayOf(-1f, 0f, 0f, -at)    // remove patient's right (−X)
            Axis.Transverse -> floatArrayOf(0f, 1f, 0f, at)    // remove superior
        }
    }

    companion object {
        fun build(organ: Organ, files: List<List<GlbMesh>>): AnatomyModel {
            val meshes = files.flatten()
            require(meshes.isNotEmpty()) { "No meshes for ${organ.id}" }
            val min = FloatArray(3) { Float.POSITIVE_INFINITY }; val max = FloatArray(3) { Float.NEGATIVE_INFINITY }
            meshes.forEach { m -> for (i in m.positions.indices) { val a = i % 3; min[a] = min(min[a], m.positions[i]); max[a] = max(max[a], m.positions[i]) } }
            val center = FloatArray(3) { (min[it] + max[it]) / 2 }
            val scale = 1f / (0..2).maxOf { (max[it] - min[it]) / 2 }
            val unmapped = mutableListOf<String>()
            val parts = meshes.map { m ->
                val structure = organ.structureFor(m.path) ?: "".also { unmapped += m.node }
                val p = FloatArray(m.positions.size) { (m.positions[it] - center[it % 3]) * scale }
                val lo = FloatArray(3) { Float.POSITIVE_INFINITY }; val hi = FloatArray(3) { Float.NEGATIVE_INFINITY }
                for (i in p.indices) { val a = i % 3; lo[a] = min(lo[a], p[i]); hi[a] = max(hi[a], p[i]) }
                val tint = organ.structure(structure)?.tint
                val color = tint?.let { floatArrayOf((it shr 16 and 0xFF) / 255f, (it shr 8 and 0xFF) / 255f, (it and 0xFF) / 255f) } ?: m.color
                ScenePart(structure, m.node, m.path, p, m.normals, m.indices, color, lo, hi, FloatArray(3) { (lo[it] + hi[it]) / 2 })
            }
            return AnatomyModel(organ, parts, unmapped, 1f / scale)
        }
    }
}

/** Möller–Trumbore ray/triangle test. Returns the ray parameter of the nearest hit ≥ 0, or +∞. */
object Ray {
    fun nearest(part: ScenePart, o: FloatArray, d: FloatArray, keep: ((Float, Float, Float) -> Boolean)? = null): Float {
        if (!hitsBox(part.min, part.max, o, d)) return Float.POSITIVE_INFINITY
        val p = part.positions; val idx = part.indices; var best = Float.POSITIVE_INFINITY
        for (t in idx.indices step 3) {
            val a = idx[t] * 3; val b = idx[t + 1] * 3; val c = idx[t + 2] * 3
            val e1x = p[b] - p[a]; val e1y = p[b + 1] - p[a + 1]; val e1z = p[b + 2] - p[a + 2]
            val e2x = p[c] - p[a]; val e2y = p[c + 1] - p[a + 1]; val e2z = p[c + 2] - p[a + 2]
            val hx = d[1] * e2z - d[2] * e2y; val hy = d[2] * e2x - d[0] * e2z; val hz = d[0] * e2y - d[1] * e2x
            val det = e1x * hx + e1y * hy + e1z * hz
            if (abs(det) < 1e-12f) continue
            val f = 1f / det
            val sx = o[0] - p[a]; val sy = o[1] - p[a + 1]; val sz = o[2] - p[a + 2]
            val u = f * (sx * hx + sy * hy + sz * hz); if (u < 0f || u > 1f) continue
            val qx = sy * e1z - sz * e1y; val qy = sz * e1x - sx * e1z; val qz = sx * e1y - sy * e1x
            val v = f * (d[0] * qx + d[1] * qy + d[2] * qz); if (v < 0f || u + v > 1f) continue
            val hit = f * (e2x * qx + e2y * qy + e2z * qz)
            if (hit >= 0f && hit < best && (keep == null || keep(o[0] + d[0] * hit, o[1] + d[1] * hit, o[2] + d[2] * hit))) best = hit
        }
        return best
    }
    private fun hitsBox(min: FloatArray, max: FloatArray, o: FloatArray, d: FloatArray): Boolean {
        var lo = Float.NEGATIVE_INFINITY; var hi = Float.POSITIVE_INFINITY
        for (i in 0..2) {
            if (abs(d[i]) < 1e-12f) { if (o[i] < min[i] || o[i] > max[i]) return false; continue }
            val a = (min[i] - o[i]) / d[i]; val b = (max[i] - o[i]) / d[i]
            lo = max(lo, min(a, b)); hi = min(hi, max(a, b))
        }
        return hi >= max(lo, 0f)
    }
}

object Primitives {
    /** Unit sphere, interleaved xyz + normal xyz, triangle list. Used for schematic markers. */
    fun sphere(slices: Int = 24, stacks: Int = 16): FloatArray {
        require(slices >= 3 && stacks >= 2)
        val result = FloatArray(slices * stacks * 6 * 6); var offset = 0
        fun point(u: Int, v: Int) {
            val phi = PI * v / stacks; val theta = 2 * PI * u / slices
            val x = (sin(phi) * cos(theta)).toFloat(); val y = cos(phi).toFloat(); val z = (sin(phi) * sin(theta)).toFloat()
            repeat(2) { result[offset++] = x; result[offset++] = y; result[offset++] = z }
        }
        for (v in 0 until stacks) for (u in 0 until slices) { point(u, v); point(u, v + 1); point(u + 1, v); point(u + 1, v); point(u, v + 1); point(u + 1, v + 1) }
        return result
    }
}
