# Cardia — an anatomy study studio

An Android-first Kotlin Multiplatform app. Real reference anatomy in 3D, dissection tools,
guided lessons and active recall. Fully offline at runtime, no API key or account.

Organs: **heart**, **kidneys & urinary tract**, **liver**, **lungs & airways**, **pancreas**, **spleen**.

## Quick start — Android Studio

1. Open the `Cardia` folder (containing `settings.gradle.kts`) in Android Studio.
2. First time only, fetch the official Gradle wrapper: `./setup.sh` (Windows: `setup.ps1`).
3. Let Gradle sync. Android SDK 35 is required. Any JDK 17+ works as the Gradle JDK
   (the bundled Android Studio JDK is fine).
4. Run the `androidApp` configuration on an API 26+ device or emulator (hardware graphics).

### Command line

```sh
./gradlew :shared:jvmTest :androidApp:assembleDebug     # unit + model-integrity tests, debug APK
./gradlew :androidApp:connectedDebugAndroidTest          # UI tests on a connected device/emulator
python3 tools/check_project.py                           # structural + asset checksum checks
./tools/fetch_models.sh                                  # re-download models, verify checksums
```

## 3D models

All anatomy comes from the **Human Reference Atlas (HRA) 3D Reference Organs**
(HuBMAP; creators Kristen Browne and Heidi Schlehlein), derived from the NLM Visible Human
Male and licensed **CC BY 4.0**. Files, source URLs and SHA-256 checksums are listed in
`androidApp/src/main/assets/models/NOTICE.md`. The meshes are unmodified; the app only
centres and scales them at load time. Credit is shown in the app (Your space, Settings).

The HRA organs share one whole-body coordinate frame, so the kidneys, calyces, ureters
and bladder sit in their true relative positions without manual placement. Each organ is
segmented into named sub-structures (for example renal pyramids, papillae, minor and major
calyces; Couinaud liver segments; 20 bronchopulmonary segments; heart valves).

Model limits, stated in the app per organ: the heart has no great vessels or coronaries;
the liver has no internal vessels or bile ducts; the lungs have no pulmonary vessels or
pleura. This is one reference body (adult male), not a population average.

## Dissection tools

- **Section**: coronal, sagittal or transverse clipping plane with a position slider.
  The inner surface of cut shells is drawn flat as the cut face.
- **Layers**: peel outer structures away (e.g. kidney capsule → cortex → medulla →
  collecting system; liver surface → Couinaud segments; lungs → airways; heart chambers → valves).
- **Separate**: explode structures outward from the framed centre.
- **Isolate / Focus**: show one structure or frame the camera on it.
- Tap-to-identify uses per-triangle ray picking and respects the section plane.

## Lessons

Lessons stage the scene per chapter (focus, framing, section, peel, ghosting, camera angle).
The kidney-stone class dissects the left kidney: supersaturation → coronal section →
Randall's plaque on a papilla → calyceal stone → staghorn → pelvi-ureteric junction →
vesico-ureteric junction → obstruction and colic → diagnosis, treatment and prevention.

Stone positions are **schematic markers** placed at points computed from the real meshes
(papilla centroid, lower-pole minor calyx, top and bottom of the ureter). No stone geometry
is invented. `ModelAssetTest` checks these anatomical relationships against the meshes
(for example, the lowest point of the left ureter lies within 10 mm of its bladder orifice).

## Architecture

| Module | Responsibility |
|---|---|
| `shared/commonMain` | GLB parser, scene builder (structure mapping, anchors, framing, section planes), ray picking, organ content, progress, cardiac cycle maths |
| `shared/commonTest` | Content integrity, progress, picking maths |
| `shared/jvmTest` | Parses bundled GLBs: every mesh mapped, every structure/frame/marker resolved, anatomical sanity checks |
| `androidApp` | Compose Material 3 screens, persistence, OpenGL ES 2 renderer (`AnatomyView`) |

Rendering is on demand. Models are parsed off the main thread and the two most recent
organs are cached. 32-bit indices use `OES_element_index_uint` when available, with a
non-indexed fallback.

## Content and accuracy

Explanatory text is original summarization checked against the references linked in the app
(OpenStax Anatomy & Physiology 2e, NIDDK, NHLBI, EAU Urolithiasis Guidelines, Evan et al.
2003 J Clin Invest). Counts quoted in the text (pyramids, calyces, segments) are taken from
the bundled models and verified by tests. This is a study aid; it has **not** had formal
medical review and is not a diagnostic tool.

## Data and privacy

All runtime data lives in app-private SharedPreferences. No network permission, login,
tracking or backend. Progress from the earlier heart-only version is migrated.

Original source is MIT licensed; the 3D models are CC BY 4.0; dependencies keep their own licenses.
