# -*- coding: utf-8 -*-
"""Veteran uygulamasi icin .ico simge uretir (paket kutusu temasi)."""
import os
from PIL import Image, ImageDraw

HERE = os.path.dirname(os.path.abspath(__file__))

SIZE = 256
BRAND = (31, 58, 95)
BRAND2 = (39, 75, 120)
BOX = (242, 201, 76)      # altin sari koli
BOX_DARK = (214, 170, 50)
TAPE = (255, 255, 255)
ACCENT = (46, 134, 222)

img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# Yuvarlatilmis marka arka plan
r = 52
d.rounded_rectangle([8, 8, SIZE - 8, SIZE - 8], radius=r, fill=BRAND)
# ust yari hafif acik (derinlik hissi)
d.rounded_rectangle([8, 8, SIZE - 8, SIZE // 2], radius=r, fill=BRAND2)
d.rounded_rectangle([8, 40, SIZE - 8, SIZE - 8], radius=r, fill=BRAND)

# Rota cizgisi (kesik) ve iki nokta
d.line([60, 196, 120, 150, 170, 188, 205, 120], fill=ACCENT, width=7, joint="curve")
for (px, py) in [(60, 196), (205, 120)]:
    d.ellipse([px - 9, py - 9, px + 9, py + 9], fill=(255, 255, 255))
    d.ellipse([px - 5, py - 5, px + 5, py + 5], fill=ACCENT)

# Koli kutusu (on yuz)
bx0, by0, bx1, by1 = 78, 96, 178, 196
d.rectangle([bx0, by0, bx1, by1], fill=BOX)
# kapak ust seridi
d.rectangle([bx0, by0, bx1, by0 + 22], fill=BOX_DARK)
# bant (tape) - dikey ve yatay
cx = (bx0 + bx1) // 2
d.rectangle([cx - 9, by0, cx + 9, by1], fill=TAPE)
d.rectangle([bx0, by0 + 30, bx1, by0 + 46], fill=TAPE)

out = os.path.join(HERE, "veteran.ico")
img.save(out, sizes=[(16, 16), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)])
print("ICON OK ->", out)
