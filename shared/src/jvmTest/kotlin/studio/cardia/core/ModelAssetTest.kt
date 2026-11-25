package studio.cardia.core

import java.io.File
import kotlin.math.abs
import kotlin.test.*

/** Parses the bundled HRA models and checks content against the real geometry. */
class ModelAssetTest {
    private val dir = File(System.getProperty("cardia.models") ?: "../androidApp/src/main/assets/models")
    private val parsed = mutableMapOf<String, List<GlbMesh>>()
    private fun model(organ: Organ) = AnatomyModel.build(organ, organ.assets.map { name -> parsed.getOrPut(name) { Glb.parse(File(dir, name).readBytes()) } })

    @Test fun everyAssetParsesWithFiniteGeometry() {
        for (organ in Atlas.organs) for (name in organ.assets) {
            val meshes = parsed.getOrPut(name) { Glb.parse(File(dir, name).readBytes()) }
            assertTrue(meshes.isNotEmpty(), name)
            for (m in meshes) {
                assertTrue(m.positions.all { it.isFinite() } && m.normals.all { it.isFinite() }, "${m.node} finite")
                assertEquals(0, m.indices.size % 3, m.node)
                assertTrue(m.indices.all { it in 0 until m.positions.size / 3 }, "${m.node} index range")
            }
        }
    }

    @Test fun everyMeshMapsToAStructureAndEveryStructureHasGeometry() {
        for (organ in Atlas.organs) {
            val m = model(organ)
            assertEquals(emptyList(), m.unmapped, "${organ.id}: meshes without a structure")
            for (s in organ.structures) assertTrue(m.parts.any { it.structure == s.id }, "${organ.id}/${s.id} has no mesh")
        }
    }

    @Test fun framesCutsAndMarkersResolve() {
        for (organ in Atlas.organs) {
            val m = model(organ)
            for (lesson in organ.lessons) for (step in lesson.steps) {
                val v = step.view
                if (v.frame.isNotEmpty()) assertNotNull(m.bounds(v.frame), "${organ.id}/${lesson.id}: frame ${v.frame}")
                v.frame.forEach { assertNotNull(m.bounds(listOf(it)), "${organ.id}: frame target $it") }
                v.cut?.through?.let { assertNotNull(m.bounds(listOf(it)), "${organ.id}: cut target $it") }
                v.markers.forEach { assertNotNull(m.anchor(it.anchor), "${organ.id}: marker ${it.label}") }
            }
        }
    }

    @Test fun sceneIsNormalizedToUnitHalfExtent() {
        for (organ in Atlas.organs) {
            val b = model(organ).bounds()!!
            assertEquals(1f, b.radius, 1e-3f, organ.id)
            (0..2).forEach { assertTrue(abs(b.center[it]) < 1e-3f, "${organ.id} centred") }
        }
    }

    /** The stone lesson's claims depend on these spatial relationships in the real meshes. */
    @Test fun urinaryTractAnchorsAreAnatomicallyConsistent() {
        val m = model(KidneyContent.organ)
        val mm = m.metresPerUnit * 1000f
        fun dist(a: FloatArray, b: FloatArray) = kotlin.math.sqrt((0..2).sumOf { ((a[it] - b[it]) * (a[it] - b[it])).toDouble() }).toFloat() * mm
        val puj = m.anchor(Anchor("VH_M_ureter_L", AnchorAt.Top))!!
        val pelvis = m.bounds(listOf("VH_M_renal_pelvis_L"))!!
        assertTrue(puj[1] <= pelvis.max[1] && puj[1] >= pelvis.min[1] - 10f / mm, "PUJ sits at the lower end of the pelvis")
        val vuj = m.anchor(Anchor("VH_M_ureter_L", AnchorAt.Bottom))!!
        val orifice = m.anchor(Anchor("VH_M_ureteral_orifice_L"))!!
        assertTrue(dist(vuj, orifice) < 10f, "VUJ within 10 mm of the left ureteric orifice, was ${dist(vuj, orifice)}")
        val papilla = m.anchor(Anchor("VH_M_renal_papilla_L_i"))!!
        val calyx = m.anchor(Anchor("VH_M_minor_calyx_L_i"))!!
        assertTrue(dist(papilla, calyx) < 6f, "lower-pole papilla drains into its minor calyx")
        val left = m.bounds(listOf("VH_M_left_kidney"))!!
        assertTrue(left.center[0] > 0f, "left kidney is on +X (patient's left)")
        assertTrue(m.bounds(listOf("dome"))!!.max[1] < left.min[1], "bladder lies below the kidney")
    }

    @Test fun kidneyCountsMatchContentClaims() {
        val m = model(KidneyContent.organ)
        fun count(group: String) = m.parts.filter { group in it.path && it.node != group }.map { it.node }.toSet().size
        assertEquals(9, count("VH_M_renal_pyramid_L")); assertEquals(10, count("VH_M_renal_pyramid_R"))
        assertEquals(3, count("VH_M_major_calyx_L")); assertEquals(2, count("VH_M_major_calyx_R"))
    }

    @Test fun lungHasTenSegmentsPerSide() {
        val nodes = model(LungContent.organ).parts.map { it.node }.filter { it.endsWith("bronchopulmonary_segment") }.toSet()
        assertEquals(10, nodes.count { it.startsWith("VH_M_left_") }); assertEquals(10, nodes.count { it.startsWith("VH_M_right_") })
    }
}
