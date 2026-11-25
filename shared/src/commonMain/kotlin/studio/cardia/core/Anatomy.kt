package studio.cardia.core

/**
 * A selectable anatomical structure. [nodes] are glTF node names from the bundled HRA models;
 * a mesh belongs to the structure whose node is its nearest named ancestor (or itself).
 * [depth] is the dissection layer: peeling to layer n hides every structure with depth < n.
 * [tint] optionally replaces the model's material colour (0xRRGGBB) to separate regions that
 * share one material, such as Couinaud segments. [decal] marks surface patches drawn on top of
 * a coincident shell.
 */
data class Structure(
    val id: String, val name: String, val subtitle: String, val description: String, val fact: String,
    val nodes: List<String>, val depth: Int = 0, val tint: Int? = null, val decal: Boolean = false, val tag: String = ""
)

enum class Axis(val label: String) { Coronal("Coronal"), Sagittal("Sagittal"), Transverse("Transverse") }

/** Section plane. Coronal removes tissue anterior to the plane, sagittal removes the patient's right, transverse removes superior. */
data class Cut(val axis: Axis, val through: String? = null, val shift: Float = 0f)

enum class AnchorAt { Centroid, Top, Bottom }

/** A point computed from real mesh geometry: centroid, highest or lowest vertex of a glTF node. */
data class Anchor(val node: String, val at: AnchorAt = AnchorAt.Centroid)

/** Schematic marker (for example a stone) placed at an [Anchor]. Not part of the anatomical model. */
data class Marker(val label: String, val anchor: Anchor)

/**
 * How the 3D scene is staged for one lesson chapter. [focus], [ghost] and [hide] are structure ids;
 * [frame] and [Cut.through] also accept glTF node names, to frame one side of a paired organ.
 */
data class StudyView(
    val focus: List<String> = emptyList(),
    val frame: List<String> = focus,
    val peel: Int = 0,
    val cut: Cut? = null,
    val ghost: List<String> = emptyList(),
    val hide: List<String> = emptyList(),
    val markers: List<Marker> = emptyList(),
    val explode: Float = 0f,
    /** Camera angles in degrees; null keeps the organ default. Yaw 180 shows the posterior aspect. */
    val yaw: Float? = null,
    val pitch: Float? = null
)

data class LessonStep(val title: String, val body: String, val prompt: String, val view: StudyView)
data class Lesson(val id: String, val title: String, val subtitle: String, val minutes: Int, val steps: List<LessonStep>)
data class Question(val prompt: String, val options: List<String>, val correct: Int, val explanation: String)

data class Organ(
    val id: String, val name: String, val system: String, val tagline: String,
    val assets: List<String>, val structures: List<Structure>, val layers: List<String>,
    val lessons: List<Lesson>, val questions: List<Question>, val references: List<Pair<String, String>>,
    val yaw: Float = 0f, val pitch: Float = 6f, val note: String = ""
) {
    fun structure(id: String) = structures.firstOrNull { it.id == id }
    private val byNode: Map<String, String> = buildMap { structures.forEach { s -> s.nodes.forEach { put(it, s.id) } } }
    /** Maps a mesh's node path (root first) to the structure owning its nearest named node. */
    fun structureFor(path: List<String>): String? = path.asReversed().firstNotNullOfOrNull { byNode[it] }
}

object Atlas {
    const val disclaimer = "Anatomy from the Human Reference Atlas (Visible Human male, CC BY 4.0). One reference body, not every body. A study aid, not a diagnostic tool."
    const val credit = "3D models: Human Reference Atlas 3D Reference Organs, HuBMAP (Kristen Browne, Heidi Schlehlein), derived from the NLM Visible Human Male. CC BY 4.0. Geometry is unmodified; the app only centres and scales it."
    val organs: List<Organ> by lazy { listOf(HeartContent.organ, KidneyContent.organ, LiverContent.organ, LungContent.organ, PancreasContent.organ, SpleenContent.organ) }
    fun organ(id: String) = organs.firstOrNull { it.id == id } ?: organs.first()
}

/** Lesson ids are namespaced "organ/lesson"; best scores are per organ. */
data class Progress(val completed: Set<String> = emptySet(), val best: Map<String, Int> = emptyMap()) {
    fun complete(organ: String, lesson: String) = copy(completed = completed + "$organ/$lesson")
    fun isComplete(organ: String, lesson: String) = "$organ/$lesson" in completed
    fun completedIn(organ: Organ) = organ.lessons.count { isComplete(organ.id, it.id) }
    fun bestFor(organ: String) = best[organ] ?: 0
    fun record(organ: Organ, score: Int) = copy(best = best + (organ.id to maxOf(bestFor(organ.id), score.coerceIn(0, organ.questions.size))))

    companion object {
        private val legacyHeartLessons = setOf("chambers", "flow", "cycle")
        /** Reads stored sets, migrating the single-organ format ("chambers", "best" int) to the heart. */
        fun restore(completed: Set<String>, best: Set<String>, legacyBest: Int): Progress = Progress(
            completed.map { if ('/' !in it && it in legacyHeartLessons) "heart/$it" else it }.filter { '/' in it }.toSet(),
            best.mapNotNull { e -> e.split('=').takeIf { it.size == 2 }?.let { (k, v) -> v.toIntOrNull()?.let { k to it } } }.toMap()
                .let { if (legacyBest > 0 && "heart" !in it) it + ("heart" to legacyBest) else it }
        )
    }
    fun bestEntries(): Set<String> = best.map { (k, v) -> "$k=$v" }.toSet()
}
