"""Мини-библиотека для рисования детских раскрасок в SVG (холст 1024x1024).
Порядок рисования = порядок наложения: фигуры с белой заливкой перекрывают предыдущие."""
import math, io, os
import cairosvg
import numpy as np
from PIL import Image
from scipy import ndimage

W = 1024
SW = 10  # толщина линии


def f(v):
    return f"{v:.1f}".rstrip("0").rstrip(".")


class Pic:
    def __init__(self):
        self.items = []

    # --- базовые элементы ---
    def shape(self, d, fill="#fff", sw=SW):
        self.items.append(f'<path d="{d}" fill="{fill}" stroke="#000" stroke-width="{sw}" '
                          f'stroke-linejoin="round" stroke-linecap="round"/>')

    def fillonly(self, d, fill="#fff"):
        self.items.append(f'<path d="{d}" fill="{fill}" stroke="none"/>')

    def line(self, d, sw=SW):
        self.items.append(f'<path d="{d}" fill="none" stroke="#000" stroke-width="{sw}" '
                          f'stroke-linejoin="round" stroke-linecap="round"/>')

    def part(self, fill_d, stroke_d, sw=SW):
        """Часть тела, сливающаяся с туловищем: заливка без обводки + обводка только по краю."""
        self.fillonly(fill_d)
        self.line(stroke_d, sw)

    def ellipse(self, cx, cy, rx, ry, rot=0, fill="#fff", sw=SW):
        t = f' transform="rotate({f(rot)} {f(cx)} {f(cy)})"' if rot else ""
        self.items.append(f'<ellipse cx="{f(cx)}" cy="{f(cy)}" rx="{f(rx)}" ry="{f(ry)}" fill="{fill}" '
                          f'stroke="#000" stroke-width="{sw}"{t}/>')

    def circle(self, cx, cy, r, fill="#fff", sw=SW):
        self.ellipse(cx, cy, r, r, 0, fill, sw)

    def dot(self, cx, cy, r):
        self.items.append(f'<circle cx="{f(cx)}" cy="{f(cy)}" r="{f(r)}" fill="#000"/>')

    def wdot(self, cx, cy, r):
        self.items.append(f'<circle cx="{f(cx)}" cy="{f(cy)}" r="{f(r)}" fill="#fff"/>')

    def rect(self, x, y, w, h, r=0, fill="#fff", sw=SW, rot=0, cx=None, cy=None):
        t = ""
        if rot:
            t = f' transform="rotate({f(rot)} {f(cx if cx is not None else x + w / 2)} {f(cy if cy is not None else y + h / 2)})"'
        self.items.append(f'<rect x="{f(x)}" y="{f(y)}" width="{f(w)}" height="{f(h)}" rx="{f(r)}" fill="{fill}" '
                          f'stroke="#000" stroke-width="{sw}" stroke-linejoin="round"{t}/>')

    def poly(self, pts, fill="#fff", sw=SW):
        d = "M" + " L".join(f"{f(x)} {f(y)}" for x, y in pts) + " Z"
        self.shape(d, fill, sw)

    def raw(self, s):
        self.items.append(s)

    def group(self, transform):
        """Контекст: всё внутри рисуется с трансформацией."""
        pic = self

        class G:
            def __enter__(self_):
                pic.items.append(f'<g transform="{transform}">')

            def __exit__(self_, *a):
                pic.items.append('</g>')
        return G()

    # --- готовые детали ---
    def eye(self, cx, cy, r, lash=False, side=1):
        """Большой мультяшный глаз: чёрный зрачок с бликами."""
        self.ellipse(cx, cy, r * 0.82, r, fill="#000", sw=SW)
        self.wdot(cx - r * 0.28, cy - r * 0.38, r * 0.34)
        self.wdot(cx + r * 0.3, cy + r * 0.38, r * 0.14)
        if lash:
            for i, a in enumerate((-60, -35)):
                a = math.radians(a)
                x0 = cx + side * r * 0.82 * math.cos(a) * 0.95
                y0 = cy + r * math.sin(a) * 0.95
                self.line(f"M{f(x0)} {f(y0)} l{f(side * r * 0.35)} {f(-r * 0.2 + i * r * 0.12)}", sw=7)

    def closed_eye(self, cx, cy, r):
        self.line(f"M{f(cx - r)} {f(cy)} Q{f(cx)} {f(cy + r * 0.9)} {f(cx + r)} {f(cy)}")

    def cheek(self, cx, cy, r=24):
        self.ellipse(cx, cy, r * 1.3, r * 0.8, sw=7)

    def smile(self, cx, cy, w=40, h=25, sw=SW):
        self.line(f"M{f(cx - w)} {f(cy)} Q{f(cx)} {f(cy + h * 2)} {f(cx + w)} {f(cy)}", sw)

    def open_mouth(self, cx, cy, w=36, h=40):
        self.shape(f"M{f(cx - w)} {f(cy)} Q{f(cx)} {f(cy + 4)} {f(cx + w)} {f(cy)} "
                   f"Q{f(cx + w * 0.9)} {f(cy + h)} {f(cx)} {f(cy + h)} Q{f(cx - w * 0.9)} {f(cy + h)} {f(cx - w)} {f(cy)} Z")
        # язычок
        self.shape(f"M{f(cx - w * 0.5)} {f(cy + h * 0.78)} Q{f(cx)} {f(cy + h * 0.3)} {f(cx + w * 0.5)} {f(cy + h * 0.78)} "
                   f"Q{f(cx)} {f(cy + h * 1.02)} {f(cx - w * 0.5)} {f(cy + h * 0.78)} Z", sw=7)

    def cat_mouth(self, cx, cy, w=26):
        self.line(f"M{f(cx)} {f(cy)} q{f(-w * 0.2)} {f(w * 0.9)} {f(-w)} {f(w * 0.45)}")
        self.line(f"M{f(cx)} {f(cy)} q{f(w * 0.2)} {f(w * 0.9)} {f(w)} {f(w * 0.45)}")

    def nose(self, cx, cy, w=30, h=20):
        self.shape(f"M{f(cx - w)} {f(cy - h * 0.4)} Q{f(cx)} {f(cy - h * 1.1)} {f(cx + w)} {f(cy - h * 0.4)} "
                   f"Q{f(cx + w * 0.6)} {f(cy + h)} {f(cx)} {f(cy + h)} Q{f(cx - w * 0.6)} {f(cy + h)} {f(cx - w)} {f(cy - h * 0.4)} Z",
                   fill="#000")
        self.wdot(cx - w * 0.3, cy - h * 0.25, w * 0.2)

    def star(self, cx, cy, r, inner=0.48, rot=0, sw=SW):
        pts = []
        for i in range(10):
            a = math.radians(-90 + rot + i * 36)
            rr = r if i % 2 == 0 else r * inner
            pts.append((cx + rr * math.cos(a), cy + rr * math.sin(a)))
        self.poly(pts, sw=sw)

    def sparkle(self, cx, cy, r, sw=8):
        self.shape(f"M{f(cx)} {f(cy - r)} Q{f(cx + r * 0.15)} {f(cy - r * 0.15)} {f(cx + r)} {f(cy)} "
                   f"Q{f(cx + r * 0.15)} {f(cy + r * 0.15)} {f(cx)} {f(cy + r)} Q{f(cx - r * 0.15)} {f(cy + r * 0.15)} {f(cx - r)} {f(cy)} "
                   f"Q{f(cx - r * 0.15)} {f(cy - r * 0.15)} {f(cx)} {f(cy - r)} Z", sw=sw)

    def heart(self, cx, cy, s, sw=SW):
        self.shape(f"M{f(cx)} {f(cy + s * 0.9)} C{f(cx - s * 1.5)} {f(cy - s * 0.1)} {f(cx - s * 0.8)} {f(cy - s * 1.2)} {f(cx)} {f(cy - s * 0.45)} "
                   f"C{f(cx + s * 0.8)} {f(cy - s * 1.2)} {f(cx + s * 1.5)} {f(cy - s * 0.1)} {f(cx)} {f(cy + s * 0.9)} Z", sw=sw)

    def cloud(self, cx, cy, s=1.0, sw=SW):
        # силуэт облака из дуг
        d = (f"M{f(cx - 110 * s)} {f(cy + 35 * s)} "
             f"Q{f(cx - 150 * s)} {f(cy + 35 * s)} {f(cx - 145 * s)} {f(cy)} "
             f"Q{f(cx - 140 * s)} {f(cy - 40 * s)} {f(cx - 90 * s)} {f(cy - 30 * s)} "
             f"Q{f(cx - 80 * s)} {f(cy - 85 * s)} {f(cx - 20 * s)} {f(cy - 75 * s)} "
             f"Q{f(cx + 30 * s)} {f(cy - 110 * s)} {f(cx + 70 * s)} {f(cy - 55 * s)} "
             f"Q{f(cx + 140 * s)} {f(cy - 60 * s)} {f(cx + 135 * s)} {f(cy)} "
             f"Q{f(cx + 160 * s)} {f(cy + 35 * s)} {f(cx + 110 * s)} {f(cy + 35 * s)} Z")
        self.shape(d, sw=sw)

    def sun(self, cx, cy, r, face=True):
        for i in range(10):
            a = math.radians(i * 36)
            self.line(f"M{f(cx + (r + 18) * math.cos(a))} {f(cy + (r + 18) * math.sin(a))} "
                      f"L{f(cx + (r + 50) * math.cos(a))} {f(cy + (r + 50) * math.sin(a))}")
        self.circle(cx, cy, r)
        if face:
            self.dot(cx - r * 0.33, cy - r * 0.12, r * 0.1)
            self.dot(cx + r * 0.33, cy - r * 0.12, r * 0.1)
            self.smile(cx, cy + r * 0.2, r * 0.32, r * 0.15, sw=8)

    def flower(self, cx, cy, r, petals=5, sw=SW, rot=0):
        for i in range(petals):
            a = math.radians(rot + i * 360 / petals - 90)
            self.ellipse(cx + r * 0.75 * math.cos(a), cy + r * 0.75 * math.sin(a), r * 0.52, r * 0.62,
                         rot=math.degrees(a) + 90, sw=sw)
        self.circle(cx, cy, r * 0.45, sw=sw)

    def daisy(self, cx, cy, r, sw=8):
        self.flower(cx, cy, r, petals=8, sw=sw)

    def leaf(self, x, y, ang, L=90, wdt=40, sw=SW):
        a = math.radians(ang)
        ex, ey = x + L * math.cos(a), y + L * math.sin(a)
        nx, ny = -math.sin(a) * wdt, math.cos(a) * wdt
        mx, my = (x + ex) / 2, (y + ey) / 2
        self.shape(f"M{f(x)} {f(y)} Q{f(mx + nx)} {f(my + ny)} {f(ex)} {f(ey)} Q{f(mx - nx)} {f(my - ny)} {f(x)} {f(y)} Z", sw=sw)
        self.line(f"M{f(x + (ex - x) * 0.15)} {f(y + (ey - y) * 0.15)} L{f(x + (ex - x) * 0.7)} {f(y + (ey - y) * 0.7)}", sw=6)

    def grass(self, x, y, n=3, h=50, sw=8):
        d = f"M{f(x)} {f(y)}"
        for i in range(n):
            d += f" Q{f(x + i * 26 + 5)} {f(y - h * 0.5)} {f(x + i * 26 + 13)} {f(y - h)} Q{f(x + i * 26 + 16)} {f(y - h * 0.4)} {f(x + i * 26 + 26)} {f(y)}"
        self.line(d, sw)

    def ground(self, cx=512, cy=885, rx=360, ry=58):
        self.ellipse(cx, cy, rx, ry)

    def hill(self, y=860, amp=40):
        self.line(f"M0 {f(y)} Q256 {f(y - amp)} 512 {f(y)} Q768 {f(y + amp)} 1024 {f(y - 10)}")

    def clipped(self, d):
        """Контекст: всё внутри обрезается по контуру d."""
        pic = self
        cid = f"c{len(self.items)}"

        class G:
            def __enter__(self_):
                pic.items.append(f'<clipPath id="{cid}"><path d="{d}"/></clipPath><g clip-path="url(#{cid})">')

            def __exit__(self_, *a):
                pic.items.append('</g>')
        return G()

    def scale_all(self, s, cx=512, cy=512, start=0):
        """Увеличить рисунок (начиная с элемента start) вокруг точки."""
        self.items = (self.items[:start] + [f'<g transform="translate({cx} {cy}) scale({s}) translate({-cx} {-cy})">']
                      + self.items[start:] + ['</g>'])

    def svg(self):
        body = "\n".join(self.items)
        return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{W}" viewBox="0 0 {W} {W}">'
                f'<rect width="{W}" height="{W}" fill="#fff"/>{body}</svg>')


def render(pic, path):
    png = cairosvg.svg2png(bytestring=pic.svg().encode(), output_width=W, output_height=W)
    img = Image.open(io.BytesIO(png)).convert("L")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, optimize=True)
    return img


def regions_preview(img):
    """Имитирует заливку приложения: раскрашивает каждую замкнутую область своим цветом."""
    a = np.array(img).astype(int)
    alpha = np.clip((255 - a - 40) * 255 // 140, 0, 255)
    bar = alpha >= 100
    bar = ndimage.binary_dilation(bar, structure=np.ones((3, 3)))
    lab, n = ndimage.label(~bar)
    rng = np.random.default_rng(3)
    pal = rng.integers(90, 255, size=(n + 1, 3))
    pal[0] = 0
    out = pal[lab].astype(np.uint8)
    # контур сверху
    line = np.clip(alpha, 0, 255)[..., None] / 255.0
    out = (out * (1 - line)).astype(np.uint8)
    return Image.fromarray(out), n


def sheet(paths, out, cols=4, size=380, regions=False):
    ims = []
    for p in paths:
        im = Image.open(p).convert("L")
        if regions:
            im, _ = regions_preview(im)
        ims.append(im.convert("RGB").resize((size, size), Image.LANCZOS))
    rows = (len(ims) + cols - 1) // cols
    S = Image.new("RGB", (cols * size + (cols + 1) * 6, rows * size + (rows + 1) * 6), (120, 120, 120))
    for i, im in enumerate(ims):
        S.paste(im, (6 + (i % cols) * (size + 6), 6 + (i // cols) * (size + 6)))
    S.save(out)
