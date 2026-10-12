from lib import *
from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen

FD = "/tmp/claude-0/-home-claude/09bff0dc-504d-5bc8-bfd6-fce1dc80cccb/scratchpad/font/package/files/"
FONTS = [TTFont(FD + "nunito-cyrillic-900-normal.woff2"), TTFont(FD + "nunito-latin-900-normal.woff2")]
CAP = 705


def glyph(ch):
    for ft in FONTS:
        cmap = ft.getBestCmap()
        if ord(ch) in cmap:
            return ft, cmap[ord(ch)]
    raise KeyError(ch)


def text_d(text, cx, base, height, spacing=0.02):
    s = height / CAP
    parts = []
    for ch in text:
        ft, g = glyph(ch)
        parts.append((ft, g, ft["hmtx"][g][0]))
    total = sum(a for _, _, a in parts) * s + spacing * height * (len(parts) - 1)
    x = cx - total / 2
    d = ""
    for ft, g, adv in parts:
        gs = ft.getGlyphSet()
        pen = SVGPathPen(gs)
        tp = TransformPen(pen, (s, 0, 0, -s, x, base))
        gs[g].draw(tp)
        d += pen.getCommands() + " "
        x += adv * s + spacing * height
    return d


def text(p, t, cx, base, height, sw=SW):
    # Контуры шрифта перекрываются: рисуем двойную обводку и закрываем её заливкой,
    # чтобы остался только внешний контур буквы.
    d = text_d(t, cx, base, height)
    p.shape(d, sw=sw * 2)
    p.fillonly(d)


# --- простые предметы (cx, cy — центр, s — масштаб, ~200px при s=1) ---

def o_watermelon(p, cx, cy, s):
    p.shape(f"M{cx - 170 * s} {cy - 60 * s} L{cx + 170 * s} {cy - 60 * s} C{cx + 170 * s} {cy + 60 * s} {cx + 90 * s} {cy + 140 * s} {cx} {cy + 140 * s} "
            f"C{cx - 90 * s} {cy + 140 * s} {cx - 170 * s} {cy + 60 * s} {cx - 170 * s} {cy - 60 * s} Z")
    p.shape(f"M{cx - 140 * s} {cy - 60 * s} L{cx + 140 * s} {cy - 60 * s} C{cx + 140 * s} {cy + 40 * s} {cx + 75 * s} {cy + 110 * s} {cx} {cy + 110 * s} "
            f"C{cx - 75 * s} {cy + 110 * s} {cx - 140 * s} {cy + 40 * s} {cx - 140 * s} {cy - 60 * s} Z")
    for dx, dy in ((-80, -20), (-30, 20), (30, 20), (80, -20), (0, 70), (-60, 50), (60, 50)):
        p.ellipse(cx + dx * s, cy + dy * s, 7 * s, 12 * s, fill="#000", sw=3)


def o_butterfly(p, cx, cy, s):
    for sx in (-1, 1):
        p.ellipse(cx + sx * 75 * s, cy + 70 * s, 60 * s, 70 * s, rot=sx * -20)
        p.ellipse(cx + sx * 90 * s, cy - 50 * s, 85 * s, 100 * s, rot=sx * 25)
        p.circle(cx + sx * 95 * s, cy - 55 * s, 35 * s, sw=8)
        p.circle(cx + sx * 75 * s, cy + 75 * s, 22 * s, sw=8)
        p.line(f"M{cx + sx * 8 * s} {cy - 125 * s} Q{cx + sx * 30 * s} {cy - 190 * s} {cx + sx * 70 * s} {cy - 195 * s}", sw=8)
        p.circle(cx + sx * 72 * s, cy - 195 * s, 12 * s, sw=7)
    p.ellipse(cx, cy, 24 * s, 130 * s)
    p.circle(cx, cy - 135 * s, 32 * s)


def o_cherry(p, cx, cy, s):
    p.line(f"M{cx - 70 * s} {cy + 50 * s} Q{cx - 40 * s} {cy - 100 * s} {cx + 20 * s} {cy - 150 * s}", sw=9)
    p.line(f"M{cx + 80 * s} {cy + 40 * s} Q{cx + 60 * s} {cy - 80 * s} {cx + 20 * s} {cy - 150 * s}", sw=9)
    p.leaf(cx + 20 * s, cy - 150 * s, -20, 110 * s, 38 * s)
    for x, y in ((-70, 50), (80, 40)):
        p.circle(cx + x * s, cy + (y + 55) * s, 70 * s)
        p.line(f"M{cx + (x - 35) * s} {cy + (y + 35) * s} Q{cx + (x - 30) * s} {cy + (y + 10) * s} {cx + (x - 10) * s} {cy + (y + 5) * s}", sw=6)


def o_mushroom(p, cx, cy, s):
    p.shape(f"M{cx - 55 * s} {cy} Q{cx - 70 * s} {cy + 110 * s} {cx - 60 * s} {cy + 160 * s} L{cx + 60 * s} {cy + 160 * s} "
            f"Q{cx + 70 * s} {cy + 110 * s} {cx + 55 * s} {cy} Z")
    p.shape(f"M{cx - 180 * s} {cy + 10 * s} C{cx - 180 * s} {cy - 190 * s} {cx + 180 * s} {cy - 190 * s} {cx + 180 * s} {cy + 10 * s} "
            f"Q{cx} {cy + 50 * s} {cx - 180 * s} {cy + 10 * s} Z")
    for x, y, r in ((-90, -40, 26), (0, -100, 30), (90, -40, 24), (20, -20, 18)):
        p.circle(cx + x * s, cy + y * s, r * s, sw=8)


def o_house(p, cx, cy, s):
    p.rect(cx + 60 * s, cy - 170 * s, 40 * s, 80 * s)
    p.rect(cx - 140 * s, cy - 40 * s, 280 * s, 210 * s)
    p.poly([(cx - 180 * s, cy - 30 * s), (cx, cy - 190 * s), (cx + 180 * s, cy - 30 * s)])
    p.rect(cx - 35 * s, cy + 60 * s, 70 * s, 110 * s)
    p.rect(cx - 115 * s, cy + 10 * s, 60 * s, 60 * s)
    p.rect(cx + 55 * s, cy + 10 * s, 60 * s, 60 * s)
    p.circle(cx, cy - 90 * s, 25 * s, sw=8)


def o_fir(p, cx, cy, s):
    p.rect(cx - 25 * s, cy + 150 * s, 50 * s, 50 * s)
    p.shape(f"M{cx} {cy - 200 * s} L{cx + 80 * s} {cy - 80 * s} L{cx + 45 * s} {cy - 80 * s} L{cx + 130 * s} {cy + 40 * s} "
            f"L{cx + 85 * s} {cy + 40 * s} L{cx + 170 * s} {cy + 155 * s} L{cx - 170 * s} {cy + 155 * s} L{cx - 85 * s} {cy + 40 * s} "
            f"L{cx - 130 * s} {cy + 40 * s} L{cx - 45 * s} {cy - 80 * s} L{cx - 80 * s} {cy - 80 * s} Z")
    for x, y in ((-40, -20), (50, 60), (-70, 110), (20, -110), (80, 120)):
        p.circle(cx + x * s, cy + y * s, 16 * s, sw=7)
    p.star(cx, cy - 205 * s, 40 * s)


def o_sun(p, cx, cy, s):
    p.sun(cx, cy, 110 * s)


def o_heart(p, cx, cy, s):
    p.heart(cx, cy, 70 * s)


def o_star(p, cx, cy, s):
    p.star(cx, cy, 80 * s)


def o_balloon(p, cx, cy, s):
    p.line(f"M{cx} {cy + 75 * s} Q{cx + 20 * s} {cy + 120 * s} {cx} {cy + 160 * s}", sw=7)
    p.poly([(cx, cy + 68 * s), (cx - 14 * s, cy + 88 * s), (cx + 14 * s, cy + 88 * s)], sw=7)
    p.ellipse(cx, cy, 58 * s, 72 * s)
    p.line(f"M{cx - 30 * s} {cy - 25 * s} Q{cx - 28 * s} {cy - 50 * s} {cx - 5 * s} {cy - 55 * s}", sw=6)


def o_flower(p, cx, cy, s):
    p.line(f"M{cx} {cy + 50 * s} L{cx} {cy + 150 * s}", sw=8)
    p.leaf(cx, cy + 120 * s, -30, 60 * s, 20 * s, sw=7)
    p.flower(cx, cy, 65 * s)


def o_beetle(p, cx, cy, s):
    for sx in (-1, 1):
        for dy in (-40, 20, 80):
            p.line(f"M{cx + sx * 100 * s} {cy + dy * s} L{cx + sx * 165 * s} {cy + (dy + 25) * s}", sw=9)
        p.line(f"M{cx + sx * 30 * s} {cy - 160 * s} Q{cx + sx * 50 * s} {cy - 220 * s} {cx + sx * 90 * s} {cy - 225 * s}", sw=8)
        p.circle(cx + sx * 92 * s, cy - 225 * s, 14 * s, sw=7)
    p.circle(cx, cy + 20 * s, 135 * s)
    p.line(f"M{cx} {cy - 110 * s} L{cx} {cy + 155 * s}", sw=9)
    for x, y, r in ((-60, -20, 26), (60, -20, 26), (-65, 70, 22), (65, 70, 22)):
        p.circle(cx + x * s, cy + y * s, r * s, sw=8)
    p.shape(f"M{cx - 85 * s} {cy - 95 * s} C{cx - 85 * s} {cy - 170 * s} {cx + 85 * s} {cy - 170 * s} {cx + 85 * s} {cy - 95 * s} Z")
    p.dot(cx - 30 * s, cy - 125 * s, 10 * s)
    p.dot(cx + 30 * s, cy - 125 * s, 10 * s)


def o_candy(p, cx, cy, s):
    p.poly([(cx - 90 * s, cy), (cx - 170 * s, cy - 70 * s), (cx - 160 * s, cy), (cx - 170 * s, cy + 70 * s)])
    p.poly([(cx + 90 * s, cy), (cx + 170 * s, cy - 70 * s), (cx + 160 * s, cy), (cx + 170 * s, cy + 70 * s)])
    p.ellipse(cx, cy, 105 * s, 75 * s)
    p.line(f"M{cx - 40 * s} {cy - 68 * s} Q{cx - 10 * s} {cy} {cx - 40 * s} {cy + 68 * s}", sw=7)
    p.line(f"M{cx + 40 * s} {cy - 68 * s} Q{cx + 70 * s} {cy} {cx + 40 * s} {cy + 68 * s}", sw=7)


def o_yogurt(p, cx, cy, s):
    p.line(f"M{cx + 40 * s} {cy - 60 * s} L{cx + 120 * s} {cy - 220 * s}", sw=12)
    p.ellipse(cx + 125 * s, cy - 235 * s, 28 * s, 38 * s, rot=25)
    p.shape(f"M{cx - 120 * s} {cy - 80 * s} L{cx + 120 * s} {cy - 80 * s} L{cx + 90 * s} {cy + 180 * s} L{cx - 90 * s} {cy + 180 * s} Z")
    p.ellipse(cx, cy - 80 * s, 125 * s, 30 * s)
    p.line(f"M{cx - 110 * s} {cy - 10 * s} L{cx + 110 * s} {cy - 10 * s}", sw=7)
    p.heart(cx, cy + 70 * s, 45 * s, sw=8)


def o_whale(p, cx, cy, s):
    p.shape(f"M{cx + 140 * s} {cy - 10 * s} C{cx + 180 * s} {cy - 60 * s} {cx + 190 * s} {cy - 110 * s} {cx + 175 * s} {cy - 140 * s} "
            f"C{cx + 210 * s} {cy - 120 * s} {cx + 230 * s} {cy - 140 * s} {cx + 240 * s} {cy - 170 * s} C{cx + 250 * s} {cy - 90 * s} {cx + 200 * s} {cy - 30 * s} {cx + 150 * s} {cy + 20 * s} Z")
    p.shape(f"M{cx - 190 * s} {cy + 20 * s} C{cx - 190 * s} {cy - 90 * s} {cx - 90 * s} {cy - 140 * s} {cx} {cy - 140 * s} "
            f"C{cx + 100 * s} {cy - 140 * s} {cx + 170 * s} {cy - 60 * s} {cx + 170 * s} {cy + 40 * s} C{cx + 170 * s} {cy + 110 * s} {cx + 80 * s} {cy + 140 * s} {cx - 20 * s} {cy + 140 * s} "
            f"C{cx - 130 * s} {cy + 140 * s} {cx - 190 * s} {cy + 100 * s} {cx - 190 * s} {cy + 20 * s} Z")
    p.eye(cx - 110 * s, cy - 10 * s, 24 * s)
    p.smile(cx - 135 * s, cy + 60 * s, 30 * s, 8 * s, sw=8)
    p.line(f"M{cx - 10 * s} {cy - 145 * s} Q{cx - 30 * s} {cy - 220 * s} {cx - 80 * s} {cy - 230 * s}", sw=8)
    p.line(f"M{cx} {cy - 145 * s} Q{cx + 20 * s} {cy - 220 * s} {cx + 70 * s} {cy - 230 * s}", sw=8)


def o_lemon(p, cx, cy, s):
    p.leaf(cx + 60 * s, cy - 120 * s, -40, 100 * s, 35 * s)
    p.shape(f"M{cx - 190 * s} {cy} Q{cx - 160 * s} {cy - 130 * s} {cx} {cy - 130 * s} Q{cx + 160 * s} {cy - 130 * s} {cx + 190 * s} {cy} "
            f"Q{cx + 160 * s} {cy + 130 * s} {cx} {cy + 130 * s} Q{cx - 160 * s} {cy + 130 * s} {cx - 190 * s} {cy} Z")
    p.line(f"M{cx - 90 * s} {cy - 60 * s} Q{cx - 50 * s} {cy - 90 * s} {cx} {cy - 90 * s}", sw=7)


def o_ball(p, cx, cy, s):
    ball = f"M{cx} {cy} m{-160 * s} 0 a{160 * s} {160 * s} 0 1 0 {320 * s} 0 a{160 * s} {160 * s} 0 1 0 {-320 * s} 0 Z"
    p.shape(ball)
    with p.clipped(ball):
        p.line(f"M{cx - 200 * s} {cy - 60 * s} Q{cx} {cy + 20 * s} {cx + 200 * s} {cy - 60 * s}", sw=9)
        p.line(f"M{cx - 200 * s} {cy + 70 * s} Q{cx} {cy + 150 * s} {cx + 200 * s} {cy + 70 * s}", sw=9)
        p.line(f"M{cx} {cy - 200 * s} Q{cx - 70 * s} {cy} {cx} {cy + 200 * s}", sw=9)
    p.shape(ball, fill="none")


def o_sock(p, cx, cy, s):
    sock = (f"M{cx - 60 * s} {cy - 190 * s} L{cx + 70 * s} {cy - 190 * s} L{cx + 70 * s} {cy + 60 * s} "
            f"C{cx + 70 * s} {cy + 170 * s} {cx - 10 * s} {cy + 200 * s} {cx - 120 * s} {cy + 200 * s} "
            f"C{cx - 200 * s} {cy + 200 * s} {cx - 210 * s} {cy + 110 * s} {cx - 140 * s} {cy + 90 * s} L{cx - 60 * s} {cy + 50 * s} Z")
    p.shape(sock)
    with p.clipped(sock):
        for y in (-120, -50, 20):
            p.rect(cx - 220 * s, cy + y * s, 440 * s, 34 * s, sw=7)
        p.circle(cx - 150 * s, cy + 160 * s, 60 * s, sw=7)
    p.shape(sock, fill="none")
    p.rect(cx - 75 * s, cy - 220 * s, 160 * s, 50 * s, r=10 * s)


def o_cloud(p, cx, cy, s):
    p.cloud(cx, cy + 30 * s, 1.35 * s)
    p.dot(cx - 40 * s, cy + 10 * s, 10 * s)
    p.dot(cx + 40 * s, cy + 10 * s, 10 * s)
    p.smile(cx, cy + 35 * s, 22 * s, 8 * s, sw=7)
    for x in (-60, 0, 60):
        p.line(f"M{cx + x * s} {cy + 110 * s} L{cx + (x - 15) * s} {cy + 170 * s}", sw=8)


def o_bee(p, cx, cy, s):
    p.ellipse(cx - 30 * s, cy - 100 * s, 55 * s, 75 * s, rot=-20)
    p.ellipse(cx + 40 * s, cy - 100 * s, 55 * s, 75 * s, rot=20)
    body = f"M{cx - 170 * s} {cy} C{cx - 170 * s} {cy - 100 * s} {cx + 140 * s} {cy - 100 * s} {cx + 170 * s} {cy} C{cx + 140 * s} {cy + 100 * s} {cx - 170 * s} {cy + 100 * s} {cx - 170 * s} {cy} Z"
    p.shape(body)
    with p.clipped(body):
        for x in (-40, 50):
            p.rect(cx + x * s, cy - 120 * s, 45 * s, 240 * s, sw=8)
    p.shape(body, fill="none")
    p.eye(cx - 115 * s, cy - 10 * s, 22 * s)
    p.smile(cx - 110 * s, cy + 40 * s, 18 * s, 6 * s, sw=7)


def o_fish(p, cx, cy, s):
    p.poly([(cx + 120 * s, cy), (cx + 200 * s, cy - 80 * s), (cx + 200 * s, cy + 80 * s)])
    body = f"M{cx - 170 * s} {cy} Q{cx - 20 * s} {cy - 150 * s} {cx + 140 * s} {cy} Q{cx - 20 * s} {cy + 150 * s} {cx - 170 * s} {cy} Z"
    p.shape(body)
    with p.clipped(body):
        p.line(f"M{cx} {cy - 120 * s} Q{cx + 30 * s} {cy} {cx} {cy + 120 * s}", sw=8)
        p.line(f"M{cx + 60 * s} {cy - 120 * s} Q{cx + 90 * s} {cy} {cx + 60 * s} {cy + 120 * s}", sw=8)
    p.shape(body, fill="none")
    p.eye(cx - 90 * s, cy - 15 * s, 22 * s)


def o_cake(p, cx, cy, s):
    p.ellipse(cx, cy + 170 * s, 200 * s, 30 * s)
    p.rect(cx - 170 * s, cy, 340 * s, 165 * s, r=20 * s)
    p.shape(f"M{cx - 170 * s} {cy + 40 * s} Q{cx - 128 * s} {cy + 80 * s} {cx - 85 * s} {cy + 40 * s} Q{cx - 43 * s} {cy + 80 * s} {cx} {cy + 40 * s} "
            f"Q{cx + 43 * s} {cy + 80 * s} {cx + 85 * s} {cy + 40 * s} Q{cx + 128 * s} {cy + 80 * s} {cx + 170 * s} {cy + 40 * s} L{cx + 170 * s} {cy + 20 * s} Q{cx + 170 * s} {cy} {cx + 150 * s} {cy} L{cx - 150 * s} {cy} Q{cx - 170 * s} {cy} {cx - 170 * s} {cy + 20 * s} Z", sw=8)
    p.rect(cx - 20 * s, cy - 130 * s, 40 * s, 130 * s, r=8 * s)
    p.shape(f"M{cx} {cy - 210 * s} Q{cx + 35 * s} {cy - 170 * s} {cx + 18 * s} {cy - 145 * s} Q{cx} {cy - 130 * s} {cx - 18 * s} {cy - 145 * s} Q{cx - 35 * s} {cy - 170 * s} {cx} {cy - 210 * s} Z", sw=8)


def o_snail(p, cx, cy, s):
    p.shape(f"M{cx - 200 * s} {cy + 150 * s} C{cx - 200 * s} {cy + 110 * s} {cx - 170 * s} {cy + 100 * s} {cx - 150 * s} {cy + 100 * s} "
            f"C{cx - 160 * s} {cy + 20 * s} {cx - 150 * s} {cy - 60 * s} {cx - 110 * s} {cy - 70 * s} C{cx - 70 * s} {cy - 80 * s} {cx - 50 * s} {cy - 40 * s} {cx - 55 * s} {cy + 100 * s} "
            f"L{cx + 190 * s} {cy + 110 * s} C{cx + 220 * s} {cy + 115 * s} {cx + 220 * s} {cy + 160 * s} {cx + 190 * s} {cy + 165 * s} L{cx - 180 * s} {cy + 170 * s} Q{cx - 200 * s} {cy + 165 * s} {cx - 200 * s} {cy + 150 * s} Z")
    p.line(f"M{cx - 120 * s} {cy - 65 * s} L{cx - 140 * s} {cy - 140 * s} M{cx - 80 * s} {cy - 65 * s} L{cx - 60 * s} {cy - 140 * s}", sw=8)
    p.dot(cx - 140 * s, cy - 145 * s, 12 * s)
    p.dot(cx - 60 * s, cy - 145 * s, 12 * s)
    p.eye(cx - 105 * s, cy - 10 * s, 18 * s)
    p.circle(cx + 70 * s, cy - 20 * s, 130 * s)
    p.line(f"M{cx + 70 * s} {cy - 20 * s} C{cx + 70 * s} {cy - 50 * s} {cx + 110 * s} {cy - 50 * s} {cx + 110 * s} {cy - 20 * s} "
           f"C{cx + 110 * s} {cy + 30 * s} {cx + 30 * s} {cy + 30 * s} {cx + 30 * s} {cy - 20 * s} C{cx + 30 * s} {cy - 90 * s} {cx + 150 * s} {cy - 90 * s} {cx + 150 * s} {cy - 20 * s}", sw=8)


def o_flag(p, cx, cy, s):
    p.rect(cx - 140 * s, cy - 200 * s, 24 * s, 400 * s, r=10 * s)
    p.circle(cx - 128 * s, cy - 210 * s, 20 * s)
    flag = f"M{cx - 116 * s} {cy - 180 * s} Q{cx} {cy - 230 * s} {cx + 170 * s} {cy - 170 * s} L{cx + 170 * s} {cy + 10 * s} Q{cx} {cy - 50 * s} {cx - 116 * s} {cy} Z"
    p.shape(flag)
    with p.clipped(flag):
        p.line(f"M{cx - 130 * s} {cy - 110 * s} Q{cx} {cy - 160 * s} {cx + 190 * s} {cy - 105 * s}", sw=8)
    p.shape(flag, fill="none")
    p.star(cx + 30 * s, cy - 65 * s, 30 * s, sw=7)


def o_bread(p, cx, cy, s):
    p.shape(f"M{cx - 190 * s} {cy + 60 * s} C{cx - 200 * s} {cy - 70 * s} {cx - 120 * s} {cy - 120 * s} {cx} {cy - 120 * s} "
            f"C{cx + 120 * s} {cy - 120 * s} {cx + 200 * s} {cy - 70 * s} {cx + 190 * s} {cy + 60 * s} C{cx + 180 * s} {cy + 110 * s} {cx - 180 * s} {cy + 110 * s} {cx - 190 * s} {cy + 60 * s} Z")
    for x in (-100, -20, 60):
        p.line(f"M{cx + x * s} {cy - 70 * s} Q{cx + (x + 30) * s} {cy - 20 * s} {cx + (x + 50) * s} {cy + 30 * s}", sw=8)
    p.ellipse(cx, cy + 130 * s, 230 * s, 30 * s)


def o_cup(p, cx, cy, s):
    p.ellipse(cx - 10 * s, cy + 170 * s, 210 * s, 35 * s)
    p.shape(f"M{cx + 130 * s} {cy - 40 * s} C{cx + 230 * s} {cy - 50 * s} {cx + 230 * s} {cy + 80 * s} {cx + 110 * s} {cy + 90 * s} L{cx + 115 * s} {cy + 50 * s} "
            f"C{cx + 180 * s} {cy + 45 * s} {cx + 180 * s} {cy - 10 * s} {cx + 130 * s} {cy - 5 * s} Z")
    p.shape(f"M{cx - 160 * s} {cy - 80 * s} L{cx + 140 * s} {cy - 80 * s} Q{cx + 140 * s} {cy + 150 * s} {cx - 10 * s} {cy + 160 * s} Q{cx - 160 * s} {cy + 150 * s} {cx - 160 * s} {cy - 80 * s} Z")
    p.heart(cx - 10 * s, cy + 30 * s, 45 * s, sw=8)
    for x in (-80, -10, 60):
        p.line(f"M{cx + x * s} {cy - 110 * s} Q{cx + (x - 25) * s} {cy - 150 * s} {cx + x * s} {cy - 190 * s}", sw=7)


def o_puppy(p, cx, cy, s):
    p.ellipse(cx, cy, 150 * s, 135 * s)
    p.shape(f"M{cx - 120 * s} {cy - 100 * s} C{cx - 200 * s} {cy - 100 * s} {cx - 220 * s} {cy + 20 * s} {cx - 190 * s} {cy + 90 * s} C{cx - 170 * s} {cy + 120 * s} {cx - 120 * s} {cy + 100 * s} {cx - 115 * s} {cy + 50 * s} Z")
    p.shape(f"M{cx + 120 * s} {cy - 100 * s} C{cx + 200 * s} {cy - 100 * s} {cx + 220 * s} {cy + 20 * s} {cx + 190 * s} {cy + 90 * s} C{cx + 170 * s} {cy + 120 * s} {cx + 120 * s} {cy + 100 * s} {cx + 115 * s} {cy + 50 * s} Z")
    p.eye(cx - 55 * s, cy - 15 * s, 30 * s)
    p.eye(cx + 55 * s, cy - 15 * s, 30 * s)
    p.ellipse(cx, cy + 65 * s, 65 * s, 45 * s)
    p.nose(cx, cy + 50 * s, 24 * s, 15 * s)
    p.cat_mouth(cx, cy + 65 * s, 18 * s)


def o_eskimo(p, cx, cy, s):
    p.rect(cx - 25 * s, cy + 100 * s, 50 * s, 120 * s, r=14 * s)
    body = (f"M{cx - 110 * s} {cy + 110 * s} L{cx - 110 * s} {cy - 120 * s} C{cx - 110 * s} {cy - 220 * s} {cx + 110 * s} {cy - 220 * s} {cx + 110 * s} {cy - 120 * s} "
            f"L{cx + 110 * s} {cy - 60 * s} Q{cx + 70 * s} {cy - 70 * s} {cx + 70 * s} {cy - 30 * s} Q{cx + 70 * s} {cy + 10 * s} {cx + 110 * s} {cy} L{cx + 110 * s} {cy + 110 * s} Z")
    p.shape(body)
    with p.clipped(body):
        p.shape(f"M{cx - 130 * s} {cy - 100 * s} Q{cx - 80 * s} {cy - 40 * s} {cx - 40 * s} {cy - 100 * s} Q{cx} {cy - 30 * s} {cx + 40 * s} {cy - 100 * s} Q{cx + 80 * s} {cy - 40 * s} {cx + 130 * s} {cy - 100 * s} L{cx + 130 * s} {cy - 260 * s} L{cx - 130 * s} {cy - 260 * s} Z", sw=8)
    p.shape(body, fill="none")
    p.dot(cx - 40 * s, cy + 20 * s, 10 * s)
    p.dot(cx + 30 * s, cy + 20 * s, 10 * s)
    p.smile(cx - 5 * s, cy + 50 * s, 22 * s, 8 * s, sw=7)


def o_top(p, cx, cy, s):
    p.rect(cx - 15 * s, cy - 230 * s, 30 * s, 90 * s, r=10 * s)
    top = f"M{cx - 180 * s} {cy - 20 * s} C{cx - 180 * s} {cy - 120 * s} {cx - 100 * s} {cy - 150 * s} {cx} {cy - 150 * s} C{cx + 100 * s} {cy - 150 * s} {cx + 180 * s} {cy - 120 * s} {cx + 180 * s} {cy - 20 * s} L{cx} {cy + 200 * s} Z"
    p.shape(top)
    with p.clipped(top):
        p.line(f"M{cx - 200 * s} {cy - 70 * s} Q{cx} {cy - 30 * s} {cx + 200 * s} {cy - 70 * s}", sw=8)
        p.line(f"M{cx - 200 * s} {cy + 10 * s} Q{cx} {cy + 50 * s} {cx + 200 * s} {cy + 10 * s}", sw=8)
    p.shape(top, fill="none")
    for x in (-110, -40, 40, 110):
        p.circle(cx + x * s, cy - 32 * s, 14 * s, sw=6)


def o_apple(p, cx, cy, s):
    p.line(f"M{cx} {cy - 120 * s} Q{cx + 5 * s} {cy - 170 * s} {cx + 25 * s} {cy - 190 * s}", sw=10)
    p.leaf(cx + 20 * s, cy - 165 * s, -20, 90 * s, 32 * s)
    p.shape(f"M{cx} {cy - 110 * s} C{cx + 60 * s} {cy - 160 * s} {cx + 180 * s} {cy - 130 * s} {cx + 170 * s} {cy + 10 * s} "
            f"C{cx + 160 * s} {cy + 130 * s} {cx + 70 * s} {cy + 180 * s} {cx} {cy + 150 * s} C{cx - 70 * s} {cy + 180 * s} {cx - 160 * s} {cy + 130 * s} {cx - 170 * s} {cy + 10 * s} "
            f"C{cx - 180 * s} {cy - 130 * s} {cx - 60 * s} {cy - 160 * s} {cx} {cy - 110 * s} Z")
    p.line(f"M{cx - 110 * s} {cy - 30 * s} Q{cx - 100 * s} {cy - 80 * s} {cx - 60 * s} {cy - 95 * s}", sw=7)


def letter_page(letter, word, obj):
    p = Pic()
    for x, y, r in ((90, 90, 30), (940, 90, 26), (950, 640, 22), (70, 700, 22)):
        p.star(x, y, r)
    text(p, letter, 285, 620, 420)
    obj(p, 740, 420, LETTER_SCALE.get(obj.__name__, 1.15))
    text(p, word, 512, 900, 130 if len(word) <= 7 else 115, sw=9)
    return p


# примерный размер предмета при масштабе 1 (для раскладки по клеткам)
NAT = {}


def number_page(digit, word, obj, n):
    p = Pic()
    p.sparkle(90, 90, 30)
    if n <= 5:
        p.sparkle(940, 110, 26)
    slots = {
        1: [(720, 400)],
        2: [(630, 290), (830, 520)],
        3: [(615, 250), (840, 250), (728, 510)],
        4: [(615, 230), (840, 230), (615, 520), (840, 520)],
        5: [(600, 220), (860, 220), (730, 400), (600, 580), (860, 580)],
    }.get(n)
    sc = {1: 1.4, 2: 1.35, 3: 1.15, 4: 1.0, 5: 0.8}.get(n)
    if slots is not None:
        text(p, digit, 250, 650, 500)
    else:
        text(p, digit, 215 if len(digit) == 1 else 195, 600, 400 if len(digit) == 1 else 250)
        x0, x1, y0, y1 = 430, 1000, 100, 770
        nat = NAT.get(obj.__name__, 360)
        # выбираем число столбцов, при котором предметы получаются крупнее всего
        cols = max((2, 3, 4), key=lambda c: min((x1 - x0) / c, (y1 - y0) / ((n + c - 1) // c)))
        rows = (n + cols - 1) // cols
        cw, ch = (x1 - x0) / cols, (y1 - y0) / rows
        sc = min(cw, ch) * 0.8 / nat
        slots = []
        for i in range(n):
            r, c = divmod(i, cols)
            in_row = min(cols, n - r * cols)
            left = (x0 + x1) / 2 - in_row * cw / 2
            slots.append((left + (c + 0.5) * cw, y0 + (r + 0.5) * ch))
    for x, y in slots:
        obj(p, x, y, sc)
    text(p, word, 512, 905, 130, sw=9)
    return p


NAT.update({"o_apple": 350, "o_fish": 380, "o_ball": 330, "o_candy": 345, "o_heart": 180})
LETTER_SCALE = {"o_star": 2.1, "o_balloon": 2.0, "o_flower": 2.0, "o_sun": 1.45, "o_heart": 2.0}


LETTERS = [
    ("А", "АРБУЗ", o_watermelon), ("Б", "БАБОЧКА", o_butterfly), ("В", "ВИШНЯ", o_cherry),
    ("Г", "ГРИБ", o_mushroom), ("Д", "ДОМ", o_house), ("Е", "ЕЛЬ", o_fir), ("Ё", "ЁЛКА", o_fir),
    ("Ж", "ЖУК", o_beetle), ("З", "ЗВЕЗДА", o_star), ("И", "ИРИСКА", o_candy), ("Й", "ЙОГУРТ", o_yogurt),
    ("К", "КИТ", o_whale), ("Л", "ЛИМОН", o_lemon), ("М", "МЯЧ", o_ball), ("Н", "НОСОК", o_sock),
    ("О", "ОБЛАКО", o_cloud), ("П", "ПЧЕЛА", o_bee), ("Р", "РЫБКА", o_fish), ("С", "СОЛНЦЕ", o_sun),
    ("Т", "ТОРТ", o_cake), ("У", "УЛИТКА", o_snail), ("Ф", "ФЛАЖОК", o_flag), ("Х", "ХЛЕБ", o_bread),
    ("Ц", "ЦВЕТОК", o_flower), ("Ч", "ЧАШКА", o_cup), ("Ш", "ШАРИК", o_balloon), ("Щ", "ЩЕНОК", o_puppy),
    ("Э", "ЭСКИМО", o_eskimo), ("Ю", "ЮЛА", o_top), ("Я", "ЯБЛОКО", o_apple),
]
NUMBERS = [
    ("1", "ОДИН", o_sun, 1), ("2", "ДВА", o_heart, 2), ("3", "ТРИ", o_star, 3), ("4", "ЧЕТЫРЕ", o_balloon, 4),
    ("5", "ПЯТЬ", o_flower, 5), ("6", "ШЕСТЬ", o_apple, 6), ("7", "СЕМЬ", o_fish, 7), ("8", "ВОСЕМЬ", o_ball, 8),
    ("9", "ДЕВЯТЬ", o_candy, 9), ("10", "ДЕСЯТЬ", o_heart, 10),
]
PAGES = ([(lambda l=l, w=w, o=o: letter_page(l, w, o)) for l, w, o in LETTERS]
         + [(lambda d=d, w=w, o=o, n=n: number_page(d, w, o, n)) for d, w, o, n in NUMBERS])

if __name__ == "__main__":
    out = []
    for i, fn in enumerate(PAGES, 1):
        path = f"/home/claude/art/out/letters/{i:03d}.png"
        render(fn(), path)
        out.append(path)
    sheet(out, "/home/claude/art/sheet_letters.png", cols=8, size=200)
    sheet(out, "/home/claude/art/sheet_letters_regions.png", cols=8, size=200, regions=True)
