"""Dependency-free structural checks; explicitly NOT a Kotlin compiler or UI test.
Content-versus-model integrity is covered by shared/src/jvmTest (ModelAssetTest), which parses the real GLBs."""
from pathlib import Path
import hashlib
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
passed = 0

def check(condition, label):
    global passed
    if not condition:
        raise AssertionError(label)
    passed += 1
    print(f"PASS {label}")

required = ["settings.gradle.kts", "build.gradle.kts", "gradlew", "gradlew.bat", "setup.sh", "setup.ps1",
            "shared/build.gradle.kts", "androidApp/build.gradle.kts", "README.md", "LICENSE",
            "gradle/wrapper/gradle-wrapper.properties", "androidApp/src/main/assets/models/NOTICE.md"]
check(all((root / name).is_file() for name in required), "project and bootstrap files present")
xmls = [p for p in root.rglob("*.xml") if "build" not in p.parts]
for path in xmls:
    ET.parse(path)
check(len(xmls) >= 3, "Android manifest and resources parse as XML")

models = root / "androidApp/src/main/assets/models"
rows = re.findall(r"^\| ([a-z-]+\.glb) \|[^|]+\| (https://\S+) \| ([0-9a-f]{64}) \|", (models / "NOTICE.md").read_text(), re.M)
check(len(rows) >= 10, "every bundled model is listed in NOTICE.md")
check({name for name, _, _ in rows} == {p.name for p in models.glob("*.glb")}, "NOTICE.md lists exactly the bundled GLBs")
for name, url, digest in rows:
    check(hashlib.sha256((models / name).read_bytes()).hexdigest() == digest, f"{name} matches pinned SHA-256")
    check(url.startswith("https://cdn.humanatlas.io/"), f"{name} sourced from the Human Reference Atlas CDN")
check("CC BY 4.0" in (models / "NOTICE.md").read_text(), "model licence stated")

content = "".join(p.read_text() for p in (root / "shared/src/commonMain").rglob("*Content.kt"))
check(len(re.findall(r'\bOrgan\(\s*\n?\s*id = "', content)) == 6, "six organs authored")
assets = set(re.findall(r'"([a-z-]+\.glb)"', content))
check(assets <= {p.name for p in models.glob("*.glb")}, "every asset referenced by content is bundled")
common = list((root / "shared/src/commonMain").rglob("*.kt"))
check(all(not re.search(r'^import (android\.|java\.)', p.read_text(), re.M) for p in common), "shared production code has no Android or Java imports")
manifest = (root / "androidApp/src/main/AndroidManifest.xml").read_text()
check("uses-permission" not in manifest, "app requests no permissions")
check('android:exported="true"' in manifest and 'android:glEsVersion="0x00020000"' in manifest, "launcher and GLES capability declared")
wrapper = (root / "gradle/wrapper/gradle-wrapper.properties").read_text()
check("f397b287023acdba1e9f6fc5ea72d22dd63669d59ed4a289a29b1a76eee151c6" in wrapper, "distribution checksum pinned")
check(all("2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046" in (root / p).read_text() for p in ["setup.sh", "setup.ps1"]), "both bootstrap scripts pin official wrapper checksum")
check(not any(p.name.endswith((".keystore", ".jks")) for p in root.rglob("*")), "no signing credentials included")
print(f"\n{passed} structural checks passed. Run ./gradlew :shared:jvmTest for model/content integrity.")
