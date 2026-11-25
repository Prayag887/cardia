package studio.cardia.core

object LungContent {
    private val lobes = listOf("rul", "rml", "rll", "lul", "lingula", "lll")
    private val airways = listOf("trachea", "main", "lobar", "segmental")

    val organ = Organ(
        id = "lungs", name = "Lungs & airways", system = "Respiratory", tagline = "Five lobes, twenty segments, one tree.",
        assets = listOf("lung.glb", "trachea.glb", "main-bronchus.glb"),
        layers = listOf("Lobes", "Airways"),
        note = "Lobes (built from bronchopulmonary segments), hila, trachea and bronchial tree. Pulmonary vessels, pleura and fissure surfaces are not modelled; fissures appear as the gaps between lobes.",
        structures = listOf(
            Structure("rul", "Right upper lobe", "Apical, posterior, anterior segments", "The superior lobe of the right lung, separated from the middle lobe by the horizontal fissure and from the lower lobe by the oblique fissure.", "The right lung has three lobes and, in this model, ten bronchopulmonary segments.", listOf("VH_M_lungs_R_upper_lobe"), tint = 0xE7A2AA),
            Structure("rml", "Right middle lobe", "Lateral and medial segments", "A wedge-shaped lobe between the horizontal and oblique fissures, lying against the right heart border.", "Middle lobe pneumonia blurs the right heart border on a frontal chest X-ray.", listOf("VH_M_lungs_R_middle_lobe"), tint = 0xD5879A),
            Structure("rll", "Right lower lobe", "Superior and four basal segments", "The largest right lobe, below and behind the oblique fissure, resting on the diaphragm.", "Its superior segment is a common site for aspiration when lying on the back.", listOf("VH_M_lungs_R_lower_lobe"), tint = 0xC76F84),
            Structure("lul", "Left upper lobe", "Apical, posterior, anterior segments", "The superior lobe of the left lung, above and in front of the oblique fissure. The left lung has no horizontal fissure.", "The left lung is smaller than the right: the heart occupies the cardiac notch on its medial side.", listOf("VH_M_lungs_L_upper_lobe"), tint = 0xE3A8B6),
            Structure("lingula", "Lingula", "Superior and inferior lingular segments", "A tongue-like part of the left upper lobe below the cardiac notch. It is the left-sided counterpart of the right middle lobe.", "The lingula belongs to the upper lobe: it is supplied by the lingular branch of the left upper lobe bronchus.", listOf("VH_M_left_lingula_superior_bronchopulmonary_segment", "VH_M_left_lingula_inferior_bronchopulmonary_segment"), tint = 0xF0C3CB),
            Structure("lll", "Left lower lobe", "Superior and four basal segments", "The lobe below and behind the oblique fissure of the left lung.", "This model shows five left lower lobe segments; many texts combine the anterior and medial basal segments.", listOf("VH_M_lungs_L_lower_lobe"), tint = 0xC57890),
            Structure("hilum", "Hila", "Root of each lung", "The region on the medial surface where the main bronchus, pulmonary artery, pulmonary veins, bronchial vessels, nerves and lymphatics enter or leave the lung.", "Hilar lymph node enlargement is an important sign on chest imaging.", listOf("VH_M_hilum_L", "VH_M_hilum_upper_L", "VH_M_hilum_lower_L", "VH_M_hilum_R", "VH_M_hilum_upper_R", "VH_M_hilum_middle_R", "VH_M_hilum_lower_R"), tint = 0xA84B63),
            Structure("trachea", "Trachea & carina", "The windpipe", "A tube about 10–12 cm long, held open by C-shaped cartilage rings open posteriorly, where the trachealis muscle lies. It divides at the carina, around the level of the sternal angle.", "The carina is highly sensitive; touching it triggers a strong cough reflex.", listOf("VH_M_trachea", "VH_M_carina"), depth = 1, tint = 0xE9DCC6),
            Structure("main", "Main bronchi", "Right and left", "The first divisions of the trachea. The right main bronchus is shorter, wider and more vertical than the left.", "That is why inhaled foreign bodies more often enter the right main bronchus.", listOf("VH_M_right_main_bronchus", "VH_M_left_main_bronchus"), depth = 1, tint = 0xE3D0B2),
            Structure("lobar", "Lobar bronchi", "One per lobe", "Secondary bronchi: three on the right (upper, middle, lower, with the intermediate bronchus between) and two on the left (upper and lower).", "The right upper lobe bronchus arises very close to the carina.", listOf("VH_M_lobar_bronchus_L", "VH_M_lobar_bronchi_R"), depth = 1, tint = 0xDCC39F),
            Structure("segmental", "Segmental bronchi", "One per bronchopulmonary segment", "Tertiary bronchi, each supplying one bronchopulmonary segment together with a branch of the pulmonary artery. Segments are the units of surgical resection.", "Each segment is a pyramid with its apex toward the hilum and base at the lung surface.", listOf("VH_M_tertiary_bronchi_L", "VH_M_tertiary_bronchi_R"), depth = 1, tint = 0xD4B58C),
            Structure("cartilage", "Airway cartilage", "Keeps airways open", "C-shaped rings in the trachea, and irregular plates in the walls of the bronchi. Cartilage becomes sparser as airways narrow and is absent in bronchioles.", "Without cartilage, bronchioles depend on surrounding lung elastic recoil to stay open.", listOf("VH_M_tracheal_cartilage", "VH_M_cartilage_of_the_main_bronchus_L", "VH_M_cartilage_of_the_main_bronchus_R", "VH_M_left_bronchial_cartilage", "VH_M_right_bronchial_cartilage"), depth = 1, tint = 0xBCD3DE)
        ),
        lessons = listOf(
            Lesson("lobes", "Lobes and fissures", "Why the right and left lungs differ", 4, listOf(
                LessonStep("Two unequal lungs", "The right lung has three lobes, the left two. The left is smaller because the heart bulges into it at the cardiac notch.", "Anterior view: the patient's right lung is on your left.", StudyView(lobes, frame = emptyList())),
                LessonStep("The right lung", "The oblique fissure separates the lower lobe; the horizontal fissure separates the upper lobe from the middle lobe.", "Gaps between coloured lobes mark the fissures.", StudyView(listOf("rul", "rml", "rll"), frame = listOf("rul", "rml", "rll"))),
                LessonStep("The left lung and lingula", "The left lung has only an oblique fissure. The lingula, part of the upper lobe, is the counterpart of the right middle lobe.", "Find the tongue-shaped lingula below the cardiac notch.", StudyView(listOf("lul", "lingula", "lll"), frame = listOf("lul", "lingula", "lll"))),
                LessonStep("The hila", "Each lung's root enters at its hilum on the medial surface.", "Right lung hidden and view turned to the medial surface of the left lung.", StudyView(listOf("hilum"), frame = listOf("lul", "lingula", "lll"), hide = listOf("rul", "rml", "rll"), yaw = 90f))
            )),
            Lesson("airways", "The bronchial tree", "From trachea to segments", 5, listOf(
                LessonStep("Trachea and carina", "The trachea divides at the carina into the right and left main bronchi.", "Lung tissue peeled away to show the airways.", StudyView(listOf("trachea"), frame = airways, peel = 1)),
                LessonStep("Right is steeper", "The right main bronchus is shorter, wider and more vertical, so aspirated objects and fluid more often go right.", "Compare the angles of the two main bronchi.", StudyView(listOf("main"), frame = listOf("trachea", "main"), peel = 1)),
                LessonStep("Lobar bronchi", "Each lobe has its own bronchus: three on the right, two on the left.", "Count them.", StudyView(listOf("lobar"), frame = airways, peel = 1)),
                LessonStep("Bronchopulmonary segments", "Segmental bronchi each supply one bronchopulmonary segment — a functionally independent unit that can be resected on its own.", "Lobes ghosted over the tree. Each lobe here is built from separate segment meshes.", StudyView(listOf("segmental"), frame = emptyList(), ghost = lobes)),
                LessonStep("Cartilage", "C-shaped rings support the trachea; irregular plates support the bronchi. Cartilage fades out as airways become bronchioles.", "Cartilage highlighted over the airways.", StudyView(listOf("cartilage"), frame = airways, peel = 1))
            ))
        ),
        questions = listOf(
            Question("How many lobes does the right lung have?", listOf("Two", "Three", "Four", "Five"), 1, "Upper, middle and lower lobes, separated by the horizontal and oblique fissures."),
            Question("The lingula belongs to which lobe?", listOf("Right middle", "Left lower", "Left upper", "Right upper"), 2, "The lingula is part of the left upper lobe, the counterpart of the right middle lobe."),
            Question("Why do aspirated objects more often enter the right main bronchus?", listOf("It is narrower", "It is shorter, wider and more vertical", "It has no cartilage", "It is posterior"), 1, "Its steeper, wider course continues more directly from the trachea."),
            Question("What is the surgical unit of lung resection below a lobe?", listOf("Alveolus", "Bronchopulmonary segment", "Acinus", "Hilum"), 1, "Each segment has its own segmental bronchus and artery branch."),
            Question("Which fissure is found only in the right lung?", listOf("Oblique", "Horizontal", "Cardiac", "Azygos"), 1, "The horizontal fissure separates the right upper and middle lobes."),
            Question("The trachea bifurcates at the…", listOf("Hilum", "Carina", "Lingula", "Cardiac notch"), 1, "The carina lies at about the level of the sternal angle.")
        ),
        references = listOf(
            "OpenStax · Organs and structures of the respiratory system" to "https://openstax.org/books/anatomy-and-physiology-2e/pages/22-1-organs-and-structures-of-the-respiratory-system",
            "OpenStax · The lungs" to "https://openstax.org/books/anatomy-and-physiology-2e/pages/22-2-the-lungs",
            "NHLBI · How the lungs work" to "https://www.nhlbi.nih.gov/health/lungs"
        ),
        yaw = 0f, pitch = 4f
    )
}
