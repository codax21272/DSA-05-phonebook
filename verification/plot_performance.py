"""Plot the unmodified instructor benchmarks; preserve measured zero milliseconds."""
import csv
import hashlib
import json
from pathlib import Path
import xml.etree.ElementTree as ET

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "performance"
OUTPUT.mkdir(exist_ok=True)


def measurements(filename):
    rows = []
    structure = None
    for row in csv.reader((ROOT / filename).open(encoding="utf-8")):
        if row and row[0] in ("Binary search tree:", "Hashtable:"):
            structure = "BST" if row[0].startswith("Binary") else "HashTable"
        elif len(row) == 4 and row[0].endswith(".txt"):
            rows.append(dict(structure=structure, file=row[0], bytes=int(row[1]),
                             elements=int(row[2]), milliseconds=int(row[3])))
    return rows


data = {"add": measurements("compare-add.csv"),
        "to_sorted_array": measurements("compare-toarray.csv")}
assert len(data["add"]) == 18 and len(data["to_sorted_array"]) == 16
data["archive_sha256"] = hashlib.file_digest((ROOT / "towns.zip").open("rb"), "sha256").hexdigest()
data["note"] = "Single instructor benchmark run; insertion includes parsing and I/O; export includes sorted-order validation."
data["status_output"] = {}
for name in ("PerformanceTestsAdd", "PerformanceTestsToArray"):
    report = ET.parse(ROOT / "target" / "surefire-reports" / f"TEST-oy.tol.tra.{name}.xml").getroot()
    assert report.get("failures") == "0" and report.get("errors") == "0"
    data["status_output"][name] = "\n".join(node.text or "" for node in report.iter("system-out"))
(OUTPUT / "measurements.json").write_text(json.dumps(data, indent=2), encoding="utf-8")

plt.rcParams.update({"font.size": 10, "axes.spines.top": False, "axes.spines.right": False})
fig, axes = plt.subplots(1, 2, figsize=(12, 4.8), layout="constrained")
for ax, operation, title in zip(axes, ("add", "to_sorted_array"),
                                ("Read and insert", "Sorted export and validation")):
    for structure, color in (("BST", "#2563eb"), ("HashTable", "#c2410c")):
        rows = [row for row in data[operation] if row["structure"] == structure]
        ax.plot([row["elements"] for row in rows], [row["milliseconds"] for row in rows],
                marker="o", markersize=4, color=color, label=structure)
    ax.set_xscale("log")
    # A symmetric log scale retains true zero-ms measurements instead of inventing values.
    ax.set_yscale("symlog", linthresh=1)
    ax.set_ylim(bottom=0)
    ax.set(title=title, xlabel="Entries (log scale)", ylabel="Elapsed time (ms; symlog, linear below 1)")
    ax.grid(True, alpha=0.2)
    ax.legend(frameon=False)
fig.suptitle("Assignment 5 — towns.zip benchmark results", fontsize=15)
fig.savefig(OUTPUT / "performance.png", dpi=180)
plt.close(fig)
print("Wrote performance/measurements.json and performance/performance.png")
for operation in ("add", "to_sorted_array"):
    print(operation)
    for row in data[operation]:
        print(row["structure"], row["file"], row["elements"], row["milliseconds"])
