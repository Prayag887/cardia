package studio.cardia.core

object HeartContent {
    private val coronal = Cut(Axis.Coronal)
    val organ = Organ(
        id = "heart", name = "Heart", system = "Cardiovascular", tagline = "Four chambers, four valves, one rhythm.",
        assets = listOf("heart.glb"),
        layers = listOf("Chambers", "Valves & papillary muscles"),
        note = "The HRA heart contains chambers, septum, valves and papillary muscles. Great vessels and coronary arteries are not part of this model.",
        structures = listOf(
            Structure("ra", "Right atrium", "Systemic venous return", "Receives blood from the superior and inferior venae cavae and the coronary sinus. Blood passes through the tricuspid valve into the right ventricle.", "The sinoatrial node lies in the wall of the right atrium near the opening of the superior vena cava.", listOf("VH_M_right_cardiac_atrium"), tint = 0x5E80BC, tag = "Lower-oxygen side · blue teaching convention"),
            Structure("rv", "Right ventricle", "Pulmonary pump", "Ejects blood through the pulmonary valve into the pulmonary trunk and on to the lungs. It forms most of the anterior (sternocostal) surface of the heart.", "Its wall is thinner than the left ventricle's because the pulmonary circulation has lower resistance.", listOf("VH_M_heart_right_ventricle"), tint = 0x4B6FAE, tag = "Lower-oxygen side · blue teaching convention"),
            Structure("la", "Left atrium", "Pulmonary venous return", "Receives oxygenated blood from the pulmonary veins. Blood passes through the mitral valve into the left ventricle. It forms most of the base (posterior surface) of the heart.", "Pulmonary veins carry oxygenated blood: vessels are named by direction relative to the heart, not by oxygen content.", listOf("VH_M_left_cardiac_atrium"), tint = 0xC4505F, tag = "Oxygenated side · red teaching convention"),
            Structure("lv", "Left ventricle", "Systemic pump", "Ejects blood through the aortic valve into the aorta to supply the systemic circulation. It forms the apex of the heart.", "Its thick muscular wall generates the pressure required for systemic circulation.", listOf("VH_M_heart_left_ventricle"), tint = 0xB23B4D, tag = "Oxygenated side · red teaching convention"),
            Structure("septum", "Interventricular septum", "Wall between the ventricles", "A mostly muscular wall, with a small thin membranous part superiorly, separating the right and left ventricles. It contracts with the left ventricle.", "Ventricular septal defects are the most common congenital heart defects; many involve the membranous part.", listOf("VH_M_interventricular_septum"), tint = 0xC98C8C),
            Structure("tricuspid", "Tricuspid valve", "Right atrioventricular valve", "Three leaflets between the right atrium and right ventricle, tethered by chordae tendineae to papillary muscles. Open in ventricular filling, closed during ventricular systole.", "Closure of the atrioventricular valves contributes to the first heart sound (S1).", listOf("VH_M_tricuspid_valve"), depth = 1, tint = 0xE8D5A8),
            Structure("pulmonary", "Pulmonary valve", "Right semilunar valve", "Three semilunar cusps at the exit of the right ventricle into the pulmonary trunk. Open during ejection, closed during ventricular relaxation.", "Closure of the semilunar valves contributes to the second heart sound (S2).", listOf("VH_M_pulmonary_valve"), depth = 1, tint = 0xD9C79A),
            Structure("mitral", "Mitral valve", "Left atrioventricular valve", "Two leaflets (anterior and posterior) between the left atrium and left ventricle, tethered by chordae tendineae to papillary muscles.", "It is the only heart valve with two leaflets, hence the older name ‘bicuspid valve’.", listOf("VH_M_mitral_valve"), depth = 1, tint = 0xEBD9B0),
            Structure("aortic", "Aortic valve", "Left semilunar valve", "Three semilunar cusps at the exit of the left ventricle into the aorta. The coronary arteries arise from the aortic sinuses just above two of the cusps.", "The aortic valve prevents blood returning from the aorta into the left ventricle during diastole.", listOf("VH_M_aortic_valve"), depth = 1, tint = 0xDCC89C),
            Structure("papillary", "Papillary muscles", "Valve anchors", "Cone-shaped muscles projecting from the ventricular walls. Through chordae tendineae they keep atrioventricular valve leaflets from everting into the atria during systole.", "They contract with the ventricle; they do not open the valves — pressure differences do.", listOf("VH_M_papillary_muscle_of_heart"), depth = 1, tint = 0xCF6F6F)
        ),
        lessons = listOf(
            Lesson("chambers", "Four chambers. One rhythm.", "Build your anatomical foundation", 4, listOf(
                LessonStep("Receive: the right atrium", "Start with the venous side. Systemic blood returns to the right atrium through the venae cavae. The coronary sinus returns blood from the heart muscle itself.", "Anterior view: the patient's right is on your left.", StudyView(listOf("ra"))),
                LessonStep("Send blood to the lungs", "Blood passes through the tricuspid valve to the right ventricle. Ventricular contraction sends it through the pulmonary valve toward the lungs.", "The right ventricle forms most of the front of the heart. Rotate to confirm.", StudyView(listOf("rv"))),
                LessonStep("Receive oxygenated blood", "Gas exchange takes place in the lungs. Pulmonary veins then bring oxygenated blood to the left atrium, the most posterior chamber.", "Turned to show the posterior surface: the left atrium forms most of the base of the heart.", StudyView(listOf("la"), yaw = 180f)),
                LessonStep("Power systemic circulation", "Blood crosses the mitral valve into the left ventricle and exits through the aortic valve. The left ventricle is the high-pressure systemic pump and forms the apex.", "The section shows the ventricles from the front. Compare their shapes.", StudyView(listOf("lv", "septum"), frame = listOf("lv", "rv"), cut = coronal))
            )),
            Lesson("flow", "Four valves, one direction", "How the valves keep blood moving forward", 4, listOf(
                LessonStep("Tricuspid valve", "Between the right atrium and right ventricle. Three leaflets, held by chordae tendineae and papillary muscles so they do not evert into the atrium when the ventricle contracts.", "The chambers have been peeled away to show the valves.", StudyView(listOf("tricuspid", "papillary"), frame = listOf("tricuspid", "pulmonary", "mitral", "aortic"), peel = 1)),
                LessonStep("Pulmonary valve", "Right ventricle → pulmonary valve → pulmonary trunk → lungs. Three semilunar cusps with no chordae.", "Semilunar cusps close when arterial pressure exceeds ventricular pressure.", StudyView(listOf("pulmonary"), frame = listOf("tricuspid", "pulmonary", "mitral", "aortic"), peel = 1)),
                LessonStep("Mitral valve", "Pulmonary veins → left atrium → mitral valve → left ventricle. Two leaflets, anchored by papillary muscles.", "Compare the two atrioventricular valves.", StudyView(listOf("mitral", "papillary"), frame = listOf("tricuspid", "pulmonary", "mitral", "aortic"), peel = 1)),
                LessonStep("Aortic valve", "Left ventricle → aortic valve → aorta → systemic arteries. The coronary arteries arise just above this valve.", "The aorta itself is not in this model; the valve marks its root.", StudyView(listOf("aortic"), frame = listOf("tricuspid", "pulmonary", "mitral", "aortic"), peel = 1))
            )),
            Lesson("cycle", "Understand the cardiac cycle", "Pressure, valves and coordinated contraction", 5, listOf(
                LessonStep("Ventricular filling", "During ventricular diastole, ventricular pressure falls. The atrioventricular valves open when atrial pressure exceeds ventricular pressure, and the ventricles fill. Atrial contraction adds the final portion.", "Scrub the timeline. Timing is schematic, not a clinical measurement.", StudyView(listOf("mitral", "tricuspid"), frame = listOf("tricuspid", "pulmonary", "mitral", "aortic"), peel = 1)),
                LessonStep("Isovolumetric contraction", "Ventricular pressure rises. The mitral and tricuspid valves close; for a short interval all four valves are closed and ventricular volume stays constant.", "S1 is associated with closure of the atrioventricular valves.", StudyView(listOf("lv", "rv"))),
                LessonStep("Ejection", "When ventricular pressure exceeds arterial pressure, the semilunar valves open and blood is ejected into the pulmonary trunk and aorta.", "Electrical depolarization precedes mechanical contraction; an ECG is not a pressure tracing.", StudyView(listOf("aortic", "pulmonary"), frame = listOf("tricuspid", "pulmonary", "mitral", "aortic"), peel = 1)),
                LessonStep("Relaxation", "The aortic and pulmonary valves close as the ventricles relax. All valves are briefly closed again, then the atrioventricular valves reopen and filling begins.", "S2 is associated with semilunar valve closure. Real phase durations vary with heart rate.", StudyView(listOf("aortic", "pulmonary"), frame = listOf("tricuspid", "pulmonary", "mitral", "aortic"), peel = 1))
            ))
        ),
        questions = listOf(
            Question("Which chamber receives systemic venous return?", listOf("Left atrium", "Right atrium", "Left ventricle", "Right ventricle"), 1, "The venae cavae return systemic venous blood to the right atrium."),
            Question("Which valve lies between the left atrium and left ventricle?", listOf("Tricuspid", "Pulmonary", "Mitral", "Aortic"), 2, "The mitral valve is the left atrioventricular valve."),
            Question("Which valve does blood cross when leaving the right ventricle?", listOf("Aortic", "Pulmonary", "Mitral", "Tricuspid"), 1, "The right ventricle ejects through the pulmonary valve into the pulmonary trunk."),
            Question("Which chamber forms the cardiac apex?", listOf("Right atrium", "Left atrium", "Right ventricle", "Left ventricle"), 3, "The apex is formed by the left ventricle."),
            Question("Why is the left ventricular wall thicker?", listOf("It pumps to the lungs", "It receives more veins", "It pumps against higher systemic resistance", "It has no valves"), 2, "Systemic circulation requires higher pressure than the pulmonary circulation."),
            Question("What do papillary muscles do?", listOf("Open the semilunar valves", "Prevent AV valve leaflets from everting in systole", "Generate the heartbeat", "Drain the coronary sinus"), 1, "Via chordae tendineae they tether the tricuspid and mitral leaflets during ventricular contraction."),
            Question("What is true during isovolumetric contraction?", listOf("All valves are open", "All valves are closed", "Only the mitral valve is open", "Ventricular pressure falls"), 1, "All four valves are briefly closed while ventricular pressure rises before ejection."),
            Question("Which event is associated with S2?", listOf("Atrial contraction", "Mitral and tricuspid closure", "Aortic and pulmonary valve closure", "Opening of the venae cavae"), 2, "S2 is associated with closure of the semilunar valves at the end of ventricular ejection.")
        ),
        references = listOf(
            "NHLBI · How the Heart Works" to "https://www.nhlbi.nih.gov/health/heart",
            "NHLBI · Blood flow" to "https://www.nhlbi.nih.gov/health/heart/blood-flow",
            "OpenStax · Heart anatomy" to "https://openstax.org/books/anatomy-and-physiology-2e/pages/19-1-heart-anatomy",
            "OpenStax · Cardiac cycle" to "https://openstax.org/books/anatomy-and-physiology-2e/pages/19-3-cardiac-cycle"
        ),
        yaw = 0f, pitch = 4f
    )
}

/** Stable, deterministic common code; no Android types or clock dependencies. */
object Cycle {
    fun phaseAfter(phase: Float, seconds: Float, bpm: Float): Float = ((phase + seconds.coerceAtLeast(0f) * bpm.coerceIn(40f, 140f) / 60f) % 1f + 1f) % 1f
    fun contraction(phase: Float): Float {
        val x = ((phase % 1f + 1f) % 1f - 0.24f) / 0.34f
        return if (x in 0f..1f) kotlin.math.sin(x * kotlin.math.PI).toFloat() else 0f
    }
    fun label(phase: Float) = when {
        phase < .18f -> "Atrial contraction"
        phase < .28f -> "Isovolumetric contraction"
        phase < .58f -> "Ventricular ejection"
        phase < .68f -> "Isovolumetric relaxation"
        else -> "Ventricular filling"
    }
    /** Structures emphasised in each phase: contracting chambers, or the valves that are open. */
    fun active(phase: Float): Map<String, Float> = when {
        phase < .18f -> mapOf("ra" to 1f, "la" to 1f, "mitral" to .6f, "tricuspid" to .6f)
        phase < .28f -> mapOf("rv" to .6f, "lv" to .6f)
        phase < .58f -> mapOf("rv" to contraction(phase), "lv" to contraction(phase), "aortic" to .6f, "pulmonary" to .6f)
        phase < .68f -> emptyMap()
        else -> mapOf("mitral" to .6f, "tricuspid" to .6f)
    }
    fun ecg(phase: Float): Float {
        fun peak(at: Float, width: Float, height: Float): Float {
            val x = (phase - at) / width
            return height * kotlin.math.exp(-x * x)
        }
        return peak(.12f, .03f, .15f) - peak(.225f, .013f, .14f) + peak(.25f, .012f, 1f) - peak(.28f, .015f, .26f) + peak(.49f, .065f, .28f)
    }
}
