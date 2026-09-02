import json
import os
import sys

BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"


def vid(index):
    """A short deterministic id per vertex, in Blockbench's alphabet."""
    s = ""
    n = index + 1
    while n:
        n, r = divmod(n, 62)
        s = BASE62[r] + s
    return "v" + s


def convert(obj_path, scale=16.0):
    verts = []
    faces = []
    with open(obj_path, "r", encoding="utf-8") as handle:
        for line in handle:
            if line.startswith("v "):
                p = line.split()
                verts.append([float(p[1]) * scale, float(p[2]) * scale, float(p[3]) * scale])
            elif line.startswith("f "):
                idx = []
                for token in line.split()[1:]:
                    n = int(token.split("/")[0])
                    idx.append(n - 1 if n > 0 else len(verts) + n)
                if len(idx) >= 3:
                    faces.append(idx)
    if not faces:
        raise ValueError("No faces in " + obj_path)

    vertices = {vid(i): verts[i] for i in range(len(verts))}
    face_map = {}
    for fi, idx in enumerate(faces):
        ids = [vid(i) for i in idx]
        face_map["f%d" % fi] = {"vertices": ids, "uv": {vi: [0, 0] for vi in ids}, "texture": 0}

    element = {
        "name": os.path.splitext(os.path.basename(obj_path))[0],
        "origin": [0, 0, 0],
        "rotation": [0, 0, 0],
        "vertices": vertices,
        "faces": face_map,
        "type": "mesh",
        "uuid": "00000000-0000-0000-0000-000000000000",
    }
    model = {
        "meta": {"format_version": "5.0", "model_format": "free", "box_uv": False},
        "name": element["name"],
        "resolution": {"width": 16, "height": 16},
        "elements": [element],
        "outliner": [element["uuid"]],
        "textures": [],
    }

    base, _ = os.path.splitext(obj_path)
    out = base + ".bbmodel"
    with open(out, "w") as handle:
        json.dump(model, handle, separators=(",", ":"))
    print("bbmodel written to " + out)
    return out


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python obj_to_bbmodel.py input.obj")
        sys.exit(1)
    convert(sys.argv[1])
