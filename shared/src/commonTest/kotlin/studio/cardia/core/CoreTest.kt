package studio.cardia.core
import kotlin.test.*

class CoreTest {
    @Test fun organIdsAndStructureIdsAreUnique() {
        assertEquals(Atlas.organs.size, Atlas.organs.map { it.id }.toSet().size)
        for (o in Atlas.organs) assertEquals(o.structures.size, o.structures.map { it.id }.toSet().size, o.id)
    }
    @Test fun lessonViewsReferenceKnownStructures() {
        for (o in Atlas.organs) for (l in o.lessons) for (s in l.steps) {
            val ids = s.view.focus + s.view.ghost + s.view.hide
            ids.forEach { assertNotNull(o.structure(it), "${o.id}/${l.id}: unknown structure $it") }
            assertTrue(s.view.peel in o.layers.indices, "${o.id}/${l.id}: peel out of range")
        }
    }
    @Test fun layersCoverStructureDepths() { for (o in Atlas.organs) o.structures.forEach { assertTrue(it.depth in o.layers.indices, "${o.id}/${it.id}") } }
    @Test fun quizAnswersAreValid() { Atlas.organs.flatMap { it.questions }.forEach { assertTrue(it.correct in it.options.indices); assertTrue(it.explanation.isNotBlank()) } }
    @Test fun progressIsIdempotent() { assertEquals(1, Progress().complete("heart", "flow").complete("heart", "flow").completed.size) }
    @Test fun bestScoreNeverDecreasesAndIsBounded() {
        val heart = Atlas.organ("heart")
        assertEquals(7, Progress().record(heart, 7).record(heart, 3).bestFor("heart"))
        assertEquals(heart.questions.size, Progress().record(heart, 100).bestFor("heart"))
    }
    @Test fun legacyProgressMigratesToHeart() {
        val p = Progress.restore(setOf("chambers", "kidney/stones"), emptySet(), 6)
        assertTrue(p.isComplete("heart", "chambers")); assertTrue(p.isComplete("kidney", "stones")); assertEquals(6, p.bestFor("heart"))
        assertEquals(p, Progress.restore(p.completed, p.bestEntries(), 0))
    }
    @Test fun nearestNamedNodeWins() {
        val liver = Atlas.organ("liver")
        assertEquals("quadrate", liver.structureFor(listOf("VH_M_liver", "VH_M_left_inferomedial_segment", "VH_M_quadrate_lobe_of_liver")))
        assertEquals("s4b", liver.structureFor(listOf("VH_M_liver", "VH_M_left_inferomedial_segment")))
        assertNull(liver.structureFor(listOf("VH_M_liver")))
    }
    @Test fun phaseWraps() { assertTrue(kotlin.math.abs(Cycle.phaseAfter(.8f, .5f, 60f) - .3f) < .0001f) }
    @Test fun contractionIsBounded() { (0..1000).forEach { assertTrue(Cycle.contraction(it / 1000f) in 0f..1f) } }
    @Test fun sphereHasFiniteUnitNormals() {
        val mesh = Primitives.sphere(16, 12)
        assertEquals(16 * 12 * 6 * 6, mesh.size)
        for (i in mesh.indices step 6) {
            val length = kotlin.math.sqrt(mesh[i+3]*mesh[i+3] + mesh[i+4]*mesh[i+4] + mesh[i+5]*mesh[i+5])
            assertTrue(kotlin.math.abs(length - 1f) < .001f)
        }
    }
    @Test fun rayHitsTriangleAndRespectsClip() {
        val tri = ScenePart("s", "n", listOf("n"), floatArrayOf(-1f,-1f,0f, 1f,-1f,0f, 0f,1f,0f), FloatArray(9), intArrayOf(0,1,2), FloatArray(3), floatArrayOf(-1f,-1f,0f), floatArrayOf(1f,1f,0f), FloatArray(3))
        assertEquals(2f, Ray.nearest(tri, floatArrayOf(0f,0f,2f), floatArrayOf(0f,0f,-1f)), 1e-5f)
        assertTrue(Ray.nearest(tri, floatArrayOf(0f,0f,2f), floatArrayOf(0f,0f,-1f)) { _, _, z -> z > 1f }.isInfinite())
        assertTrue(Ray.nearest(tri, floatArrayOf(3f,0f,2f), floatArrayOf(0f,0f,-1f)).isInfinite())
    }
}
