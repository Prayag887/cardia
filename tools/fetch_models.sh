#!/bin/sh
# Re-download the bundled HRA reference organs and verify pinned checksums.
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)/androidApp/src/main/assets/models"
grep -E '^\| [a-z-]+\.glb' NOTICE.md | while IFS='|' read -r _ file _ url sha _; do
  file=$(echo "$file" | xargs); url=$(echo "$url" | xargs); sha=$(echo "$sha" | xargs)
  curl --fail --location --silent --show-error "$url" --output "$file.tmp"
  actual=$(shasum -a 256 "$file.tmp" | cut -d ' ' -f 1)
  if [ "$actual" != "$sha" ]; then rm -f "$file.tmp"; echo "Checksum mismatch: $file" >&2; exit 1; fi
  mv "$file.tmp" "$file"; echo "OK $file"
done
