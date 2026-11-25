# 3D model attribution

All anatomical meshes in this folder are unmodified 3D Reference Organs from the
**Human Reference Atlas (HRA)**, HuBMAP consortium, derived from the NLM Visible Human
Male dataset. Licensed under **CC BY 4.0** — https://creativecommons.org/licenses/by/4.0/

Creators: Kristen Browne, Heidi Schlehlein. Publisher: HuBMAP.
Catalogue: https://humanatlas.io/3d-reference-library

| File | HRA dataset | Source URL | SHA-256 |
|---|---|---|---|
| heart.glb | heart-male v1.3 | https://cdn.humanatlas.io/digital-objects/ref-organ/heart-male/v1.3/assets/3d-vh-m-heart.glb | 689df039c8bc264ea06a44cd926762976f0666c0ff2dc532d046409b185e6b0c |
| kidney-l.glb | kidney-male-left v1.3 | https://cdn.humanatlas.io/digital-objects/ref-organ/kidney-male-left/v1.3/assets/3d-vh-m-kidney-l.glb | 1e9f54281652d0b05c9ae54a06380a41a8d9c62c1036829180750995b1730fac |
| kidney-r.glb | kidney-male-right v1.3 | https://cdn.humanatlas.io/digital-objects/ref-organ/kidney-male-right/v1.3/assets/3d-vh-m-kidney-r.glb | f23bac3f62fd050b689bdee86bb8a20bff6997e830cb508c847f74c03c6a964b |
| ureter-l.glb | ureter-male-left v1.2 | https://cdn.humanatlas.io/digital-objects/ref-organ/ureter-male-left/v1.2/assets/3d-vh-m-ureter-l.glb | ebdd493c2e8095ccb6e795f49bf26c94e8d205c94097a7e1f9a9d7b168a1173c |
| ureter-r.glb | ureter-male-right v1.2 | https://cdn.humanatlas.io/digital-objects/ref-organ/ureter-male-right/v1.2/assets/3d-vh-m-ureter-r.glb | 215978b67cf4d3876249a8dafb8c83a13471b5e1eb601559dca25a61a7c9d091 |
| urinary-bladder.glb | urinary-bladder-male v1.2 | https://cdn.humanatlas.io/digital-objects/ref-organ/urinary-bladder-male/v1.2/assets/3d-vh-m-urinary-bladder.glb | 070893aea9139d9a2c4f8da3aa66ca309a3c683a599d93a37dd7031c7d71021d |
| liver.glb | liver-male v1.2 | https://cdn.humanatlas.io/digital-objects/ref-organ/liver-male/v1.2/assets/3d-vh-m-liver.glb | 8e4c38293cfdb6b4f47ef2930c7f4af9e37b9144d1cf26d07c8f12c5045eb404 |
| lung.glb | lung-male v1.4 | https://cdn.humanatlas.io/digital-objects/ref-organ/lung-male/v1.4/assets/3d-vh-m-lung.glb | bba95516fa993ac45c3b1c53f32b58d97232ffd78ae56397e5a2589c6ce4903d |
| trachea.glb | trachea-male v1.1 | https://cdn.humanatlas.io/digital-objects/ref-organ/trachea-male/v1.1/assets/3d-vh-m-trachea.glb | bca5616e505baff61dce1bbc31d6602eb72c112bcbe4531931e37e0c254ddd8d |
| main-bronchus.glb | main-bronchus-male v1.1 | https://cdn.humanatlas.io/digital-objects/ref-organ/main-bronchus-male/v1.1/assets/3d-vh-m-main-bronchus.glb | 1f1f50659daba678a5738af67d4fc8add5a113a744c93427280a19ce9e0c5755 |
| pancreas.glb | pancreas-male v1.3 | https://cdn.humanatlas.io/digital-objects/ref-organ/pancreas-male/v1.3/assets/3d-vh-m-pancreas.glb | a89d7acbc9448ca1e6c09f7f6d6a18b2bd8a2e94cd4a0b51709915a8b7bea84e |
| spleen.glb | spleen-male v1.3 | https://cdn.humanatlas.io/digital-objects/ref-organ/spleen-male/v1.3/assets/3d-vh-m-spleen.glb | 826012b9dc6b69064b76c3aabbce32a2baa32d1f97fde3e88a015f399c9040b8 |

All organs share the HRA whole-body coordinate frame (metres, +Y superior, +Z anterior,
+X patient's left), so kidneys, ureters and bladder align without manual placement.
The app only re-centres and scales the scene at load time; geometry is not edited.
Stone markers in the kidney-stone lesson are schematic spheres positioned at points
computed from these meshes, not part of the HRA models.
