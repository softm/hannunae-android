#!/usr/bin/env python3
"""Sync this app's own pure HTML web release; no JavaScript build step is used."""
from pathlib import Path
import hashlib
import json
import shutil
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
source = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else root.parent / "hannunae-web"
target = root / "web-assets"
if not (source / "index.html").is_file():
    raise SystemExit(f"Missing web source: {source / 'index.html'}")
allowed = {".html", ".css", ".js", ".json", ".webmanifest", ".svg", ".png", ".ico", ".jpg", ".woff2"}
files = [p for p in source.iterdir() if p.is_file() and p.suffix in allowed
         and not p.name.startswith(".")]
if target.exists():
    shutil.rmtree(target)
target.mkdir()
ledger = {}
for file in sorted(files):
    relative = file.relative_to(source)
    destination = target / relative
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(file, destination)
    ledger[str(relative)] = hashlib.sha256(destination.read_bytes()).hexdigest()
try:
    source_commit = subprocess.check_output(
        ["git", "-C", str(source), "rev-parse", "HEAD"], text=True, stderr=subprocess.DEVNULL
    ).strip()
except (subprocess.CalledProcessError, FileNotFoundError):
    source_commit = None
(root / "web-assets.lock.json").write_text(json.dumps({
    "source": "https://github.com/softm/hannunae-web",
    "source_commit": source_commit,
    "files": ledger
}, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print(f"Synced {len(files)} pure HTML assets from {source.name}")
