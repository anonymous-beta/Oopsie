#!/usr/bin/env python3
"""
Oopsie — icon factory.
Reads Assets/logo.png and emits every launcher asset Android needs.
Made by Anonymous-beta.
"""
from __future__ import annotations

import sys
from pathlib import Path

try:
    from PIL import Image, ImageDraw
except ImportError:
    sys.exit("Pillow is missing.  pip install pillow")

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "Assets"
RES = ROOT / "app" / "src" / "main" / "res"

DENSITIES = {
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}

LEGACY_DP = 48       # legacy launcher icon
FOREGROUND_DP = 108  # adaptive foreground canvas
SAFE_DP = 66         # guaranteed-visible centre of that canvas

BG = (255, 143, 163)  # bubblegum — matches @color/ic_launcher_background
INK = (58, 46, 57)
BUTTER = (255, 209, 102)


def fallback_mascot(size: int = 1024) -> Image.Image:
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    pad = int(size * 0.06)
    d.ellipse([pad, pad, size - pad, size - pad], fill=BG + (255,))

    ew, eh = size * 0.13, size * 0.16
    for cx in (size * 0.35, size * 0.65):
        d.ellipse([cx - ew / 2, size * 0.38 - eh / 2,
                   cx + ew / 2, size * 0.38 + eh / 2], fill=(255, 255, 255, 255))
        d.ellipse([cx - ew * 0.24, size * 0.38 - eh * 0.24,
                   cx + ew * 0.24, size * 0.38 + eh * 0.24], fill=INK + (255,))

    for cx in (size * 0.22, size * 0.78):
        d.ellipse([cx - size * 0.08, size * 0.52,
                   cx + size * 0.08, size * 0.59], fill=BUTTER + (150,))

    d.arc([size * 0.34, size * 0.46, size * 0.66, size * 0.70],
          start=20, end=160, fill=INK + (255,), width=max(2, int(size * 0.028)))
    return img


def load_logo() -> Image.Image:
    p = ASSETS / "logo.png"
    if p.is_file():
        raw = Image.open(p).convert("RGBA")
        side = max(raw.size)
        canvas = Image.new("RGBA", (side, side), (0, 0, 0, 0))
        canvas.paste(raw, ((side - raw.width) // 2, (side - raw.height) // 2), raw)
        print(f"[oopsie] loaded {p.name} -> normalised {side}x{side}")
        return canvas
    print("[oopsie] Assets/logo.png not found — drawing the fallback mascot")
    return fallback_mascot()


def rounded_mask(size: int, ratio: float = 0.22) -> Image.Image:
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).rounded_rectangle([0, 0, size - 1, size - 1],
                                        radius=int(size * ratio), fill=255)
    return m


def circle_mask(size: int) -> Image.Image:
    m = Image.new("L", (size, size), 0)
    ImageDraw.Draw(m).ellipse([0, 0, size - 1, size - 1], fill=255)
    return m


def save(img: Image.Image, bucket: str, name: str) -> None:
    out_dir = RES / bucket
    out_dir.mkdir(parents=True, exist_ok=True)
    path = out_dir / f"{name}.png"
    img.save(path, "PNG", optimize=True)
    print(f"  {path.relative_to(ROOT)}  {img.width}x{img.height}")


def main() -> None:
    logo = load_logo()

    for density, scale in DENSITIES.items():
        legacy = int(LEGACY_DP * scale)
        fg = int(FOREGROUND_DP * scale)
        safe = int(SAFE_DP * scale)

        art = logo.resize((int(legacy * 0.78), int(legacy * 0.78)), Image.LANCZOS)
        offset = ((legacy - art.width) // 2, (legacy - art.height) // 2)

        square = Image.new("RGBA", (legacy, legacy), BG + (255,))
        square.paste(art, offset, art)
        square.putalpha(rounded_mask(legacy))
        save(square, f"mipmap-{density}", "ic_launcher")

        round_icon = Image.new("RGBA", (legacy, legacy), BG + (255,))
        round_icon.paste(art, offset, art)
        round_icon.putalpha(circle_mask(legacy))
        save(round_icon, f"mipmap-{density}", "ic_launcher_round")

        foreground = Image.new("RGBA", (fg, fg), (0, 0, 0, 0))
        art2 = logo.resize((safe, safe), Image.LANCZOS)
        foreground.paste(art2, ((fg - safe) // 2, (fg - safe) // 2), art2)
        save(foreground, f"mipmap-{density}", "ic_launcher_foreground")

    inapp = RES / "drawable-nodpi"
    inapp.mkdir(parents=True, exist_ok=True)
    logo.resize((512, 512), Image.LANCZOS).save(
        inapp / "logo_inapp.png", "PNG", optimize=True)
    print(f"  {(inapp / 'logo_inapp.png').relative_to(ROOT)}  512x512")

    print("[oopsie] icon factory done.")


if __name__ == "__main__":
    main()
