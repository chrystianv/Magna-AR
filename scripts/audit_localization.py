#!/usr/bin/env python3
"""List real translation gaps without counting invariant names or filling English into locales."""
from pathlib import Path
import json
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1] / "app/src/main/res"
def strings(folder):
    result = {}
    for path in sorted(folder.glob("*.xml")):
        for node in ET.parse(path).getroot():
            if node.tag == "string":
                name = node.attrib["name"]
                if name in result:
                    raise ValueError(f"Duplicate string {name} in {folder}")
                result[name] = node
    return result

base = strings(root / "values")
required = {name for name, node in base.items() if node.get("translatable") != "false"}
report = {}
for folder in sorted(root.glob("values-*")):
    locale = folder.name.removeprefix("values-")
    # Resource directories in this port use language tags or Android configuration qualifiers.
    if len(locale.split("-")[0]) not in (2, 3) or locale.split("-")[0] in {"land", "night"} or locale.startswith("v") and locale[1:].isdigit():
        continue
    actual = strings(folder)
    if actual:
        report[locale] = sorted(required - actual.keys())
print(json.dumps(report, ensure_ascii=False, indent=2))
