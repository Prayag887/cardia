package studio.cardia.core

object PancreasContent {
    private val parts = listOf("head", "uncinate", "neck", "body", "tail")
    val organ = Organ(
        id = "pancreas", name = "Pancreas", system = "Digestive & endocrine", tagline = "Head in the duodenum, tail at the spleen.",
        assets = listOf("pancreas.glb"),
        layers = listOf("Pancreas"),
        note = "The five parts of the HRA pancreas. Ducts, islets and the surrounding duodenum and vessels are not modelled. Part colours are teaching colours.",
        structures = listOf(
            Structure("head", "Head", "Inside the duodenal C-loop", "The widest part, lying in the concavity of the duodenum to the right of the superior mesenteric vessels. The common bile duct runs through or behind it.", "Tumours of the head can compress the bile duct and cause painless jaundice.", listOf("VH_M_head_of_pancreas"), tint = 0xE7B46A),
            Structure("uncinate", "Uncinate process", "Hook behind the vessels", "A projection from the lower head that hooks behind the superior mesenteric vein and artery.", "Because of this position, uncinate tumours may involve the superior mesenteric vessels early.", listOf("VH_M_uncinate_process_of_the_pancreas"), tint = 0xC9784A),
            Structure("neck", "Neck", "Over the portal vein", "A short narrowed part joining head and body. Behind it, the superior mesenteric vein and splenic vein unite to form the portal vein.", "The neck is the usual line of transection in a Whipple operation.", listOf("VH_M_neck_of_pancreas"), tint = 0xF2E0B5),
            Structure("body", "Body", "Across the spine", "Runs to the left across the aorta and the first lumbar vertebra, behind the stomach and lesser sac. The splenic artery runs along its upper border.", "Its position against the spine makes it vulnerable to compression in blunt abdominal trauma.", listOf("VH_M_body_of_pancreas"), tint = 0xD4BE62),
            Structure("tail", "Tail", "Toward the spleen", "The narrow left end, running in the splenorenal ligament toward the hilum of the spleen.", "The tail has the highest density of islets of Langerhans, the endocrine cells that make insulin and glucagon.", listOf("VH_M_tail_of_pancreas"), tint = 0xB08A58)
        ),
        lessons = listOf(
            Lesson("parts", "Parts of the pancreas", "Position and relations", 3, listOf(
                LessonStep("A gland with two jobs", "Most of the pancreas is exocrine acinar tissue making digestive enzymes, drained through ducts to the duodenum. Scattered islets of Langerhans, only a few percent of its mass, release insulin and glucagon into the blood.", "Anterior view: the head lies on the patient's right, on your left.", StudyView(parts, frame = emptyList())),
                LessonStep("Head and uncinate process", "The head sits in the C-loop of the duodenum; the uncinate process hooks behind the superior mesenteric vessels.", "Turned to see the uncinate process from below.", StudyView(listOf("head", "uncinate"), frame = listOf("head", "uncinate", "neck"), pitch = -45f)),
                LessonStep("Neck, body and tail", "The neck overlies the formation of the portal vein; the body crosses the spine; the tail reaches the splenic hilum.", "Separated to show each part.", StudyView(listOf("neck", "body", "tail"), frame = emptyList(), explode = .3f))
            ))
        ),
        questions = listOf(
            Question("Which part of the pancreas lies within the duodenal C-loop?", listOf("Tail", "Body", "Head", "Neck"), 2, "The head sits in the concavity of the duodenum."),
            Question("The uncinate process hooks behind which vessels?", listOf("Splenic artery and vein", "Superior mesenteric vein and artery", "Renal vessels", "Coeliac trunk"), 1, "It lies posterior to the superior mesenteric vein and artery."),
            Question("Where do islets of Langerhans have their highest density?", listOf("Head", "Uncinate process", "Tail", "Neck"), 2, "Islet density is highest in the tail."),
            Question("Behind the pancreatic neck, the portal vein is formed by…", listOf("Splenic and superior mesenteric veins", "Hepatic veins", "Renal and gonadal veins", "Inferior vena cava"), 0, "The splenic vein joins the superior mesenteric vein behind the neck.")
        ),
        references = listOf(
            "OpenStax · Accessory organs in digestion" to "https://openstax.org/books/anatomy-and-physiology-2e/pages/23-6-accessory-organs-in-digestion-the-liver-pancreas-and-gallbladder",
            "OpenStax · The endocrine pancreas" to "pages/17-9-the-endocrine-pancreas"
        )
    )
}

object SpleenContent {
    val organ = Organ(
        id = "spleen", name = "Spleen", system = "Lymphatic", tagline = "A blood filter tucked under the ribs.",
        assets = listOf("spleen.glb"),
        layers = listOf("Surfaces"),
        note = "The HRA spleen divides its surface into diaphragmatic, gastric, renal and colic surfaces and the hilum. Surface colours are teaching colours.",
        structures = listOf(
            Structure("diaphragmatic", "Diaphragmatic surface", "Convex outer face", "The smooth convex surface facing up, back and to the left against the diaphragm, which separates it from the pleura and lower ribs.", "The spleen lies under the left 9th to 11th ribs; a normal spleen is not usually palpable.", listOf("VH_M_diaphragmatic_surface_of_spleen"), tint = 0x8E4652),
            Structure("gastric", "Gastric surface", "Faces the stomach", "Part of the concave visceral surface, facing forward and medially toward the stomach.", "The gastrosplenic ligament, carrying short gastric vessels, connects the spleen to the stomach.", listOf("VH_M_gastric_surface_of_spleen"), tint = 0xB86470),
            Structure("renal", "Renal surface", "Faces the left kidney", "Part of the visceral surface, facing medially and backward toward the left kidney and adrenal gland.", "The splenorenal ligament, carrying the splenic vessels and pancreatic tail, runs between spleen and kidney.", listOf("VH_M_renal_surface_of_spleen"), tint = 0xA25563),
            Structure("colic", "Colic surface", "Rests on the splenic flexure", "The lower anterior part of the visceral surface, resting on the left colic (splenic) flexure.", "An enlarging spleen grows down and toward the right iliac fossa.", listOf("VH_M_colic_surface_of_spleen"), tint = 0xC98590),
            Structure("hilum", "Splenic hilum", "Vessels in, vessels out", "A slit on the visceral surface where branches of the splenic artery enter and the splenic vein leaves; the tail of the pancreas often reaches it.", "The splenic vein drains into the portal system, so portal hypertension enlarges the spleen.", listOf("VH_M_hilum_of_spleen"), tint = 0x6E2F3A)
        ),
        lessons = listOf(
            Lesson("surfaces", "Surfaces of the spleen", "Reading its neighbours", 3, listOf(
                LessonStep("Under the left ribs", "The spleen filters blood: it removes old red cells and mounts immune responses to blood-borne pathogens. Its smooth diaphragmatic surface lies against the diaphragm.", "Anterior view: the spleen sits on the patient's left, on your right.", StudyView(listOf("diaphragmatic"), frame = emptyList())),
                LessonStep("The visceral surface", "Its inner face is moulded by neighbours: stomach in front, kidney behind, colon below.", "Turned to face the visceral surface.", StudyView(listOf("gastric", "renal", "colic"), frame = emptyList(), yaw = 60f)),
                LessonStep("The hilum", "Splenic vessels enter and leave at the hilum between the gastric and renal surfaces.", "Look for the dark slit.", StudyView(listOf("hilum"), frame = emptyList(), yaw = 60f))
            ))
        ),
        questions = listOf(
            Question("Beneath which ribs does the spleen lie?", listOf("Right 5th–7th", "Left 9th–11th", "Left 5th–7th", "Right 11th–12th"), 1, "It lies under the left 9th to 11th ribs, in the left upper quadrant."),
            Question("Which surface faces the left kidney?", listOf("Colic", "Gastric", "Renal", "Diaphragmatic"), 2, "The renal surface faces the left kidney and adrenal gland."),
            Question("Why does portal hypertension enlarge the spleen?", listOf("The splenic artery dilates", "The splenic vein drains into the portal system", "The spleen makes more red cells", "Lymph flow reverses"), 1, "Raised portal pressure backs up into the splenic vein."),
            Question("What passes through the splenic hilum?", listOf("Splenic vessels", "Left ureter", "Common bile duct", "Left renal vein"), 0, "Splenic artery branches enter and the splenic vein leaves at the hilum.")
        ),
        references = listOf(
            "OpenStax · Anatomy of the lymphatic and immune systems" to "https://openstax.org/books/anatomy-and-physiology-2e/pages/21-1-anatomy-of-the-lymphatic-and-immune-systems"
        ),
        yaw = 0f, pitch = 4f
    )
}
