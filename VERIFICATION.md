# Verification and release checklist

## Performed (2026-09-22)

- `./gradlew :shared:jvmTest` — 19 tests pass, including `ModelAssetTest`, which parses all
  12 bundled GLBs and checks: finite geometry and valid indices; every mesh maps to a
  structure; every structure has geometry; every lesson frame, section target and marker
  resolves; urinary-tract anchor relationships (PUJ at the pelvis, VUJ within 10 mm of the
  left ureteric orifice, lower-pole papilla within 6 mm of its calyx, bladder below kidney,
  left kidney on +X); pyramid, calyx and bronchopulmonary segment counts quoted in the text.
- `./gradlew :androidApp:assembleDebug :androidApp:lintDebug` — succeed.
- `./gradlew :androidApp:connectedDebugAndroidTest` — 3 tests pass on a Pixel Tablet
  emulator (API 35): studio open/organ switch/back, heart lesson completion, and the
  kidney-stone lesson including its stone marker.
- Manual emulator screenshots of every organ in the studio, layer peeling (heart valves,
  liver segments, bronchial tree), coronal section, and all nine kidney-stone chapters.
- `python3 tools/check_project.py` — structural checks and model SHA-256 checksums.
- Reference URLs and the Evan et al. DOI resolved.

## Not performed

- Physical-device GPU performance and thermal profiling (the lung scene is ~275k triangles).
- Formal medical/anatomist review of the lesson text.
- Accessibility audit with TalkBack beyond semantic labels.
