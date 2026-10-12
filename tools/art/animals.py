from lib import *

CX = 512


def scene_bg(p, left=True, right=True):
    """Земля, травка и цветочки по краям, как в референсах."""
    p.ground()
    if left:
        p.line("M150 800 Q145 850 155 880", sw=9)
        p.leaf(152, 850, 200, 70, 24, sw=8)
        p.flower(150, 745, 58)
        p.grass(205, 885, 2, 55)
    if right:
        p.line("M875 790 Q880 840 870 880", sw=9)
        p.leaf(874, 845, -25, 70, 24, sw=8)
        p.flower(875, 735, 54, petals=6)
        p.grass(770, 888, 2, 50)


def sit_body(p, cx=CX, top=560, belly=False, toes=True):
    # задние лапки
    p.ellipse(cx - 150, 810, 92, 66)
    p.ellipse(cx + 150, 810, 92, 66)
    # туловище
    p.shape(f"M{cx - 135} {top} C{cx - 205} {top + 100} {cx - 190} 830 {cx - 112} 868 "
            f"L{cx + 112} 868 C{cx + 190} 830 {cx + 205} {top + 100} {cx + 135} {top} Z")
    if belly:
        p.ellipse(cx, 735, 78, 95)
    # передние лапки
    for sx in (-1, 1):
        a, b = cx + sx * 28, cx + sx * 102
        l, r = min(a, b), max(a, b)
        m = (l + r) / 2
        p.part(f"M{l} 680 L{l} 842 Q{l} 882 {m} 882 Q{r} 882 {r} 842 L{r} 680 Z",
               f"M{l} 700 L{l} 842 Q{l} 882 {m} 882 Q{r} 882 {r} 842 L{r} 700")
        if toes:
            w = r - l
            p.line(f"M{l + w / 3} 882 L{l + w / 3} 860", sw=7)
            p.line(f"M{l + 2 * w / 3} 882 L{l + 2 * w / 3} 860", sw=7)


def bunny():
    p = Pic()
    scene_bg(p)
    p.circle(700, 830, 48)  # хвостик
    sit_body(p)
    # уши
    for sx, rot in ((-1, -12), (1, 12)):
        p.ellipse(CX + sx * 85, 175, 58, 155, rot=rot)
        p.ellipse(CX + sx * 85, 190, 30, 115, rot=rot)
    p.ellipse(CX, 405, 205, 178)
    p.line("M490 238 Q505 205 525 232", sw=8)  # чубчик
    p.eye(435, 400, 50, lash=True, side=-1)
    p.eye(589, 400, 50, lash=True, side=1)
    p.cheek(388, 478)
    p.cheek(636, 478)
    p.rect(490, 492, 22, 30, r=6, sw=7)
    p.rect(512, 492, 22, 30, r=6, sw=7)
    p.cat_mouth(512, 478, 30)
    p.nose(512, 462, 22, 14)
    return p


def bone(p, cx, cy, s=1.0, rot=0):
    with p.group(f"rotate({rot} {cx} {cy})"):
        d = (f"M{cx - 38 * s} {cy - 9 * s} L{cx + 38 * s} {cy - 9 * s} "
             f"A{15 * s} {15 * s} 0 1 1 {cx + 50 * s} {cy} A{15 * s} {15 * s} 0 1 1 {cx + 38 * s} {cy + 9 * s} "
             f"L{cx - 38 * s} {cy + 9 * s} A{15 * s} {15 * s} 0 1 1 {cx - 50 * s} {cy} A{15 * s} {15 * s} 0 1 1 {cx - 38 * s} {cy - 9 * s} Z")
        p.shape(d, sw=8)


def puppy():
    p = Pic()
    scene_bg(p)
    p.cloud(170, 170, 0.8)
    p.cloud(860, 220, 0.65)
    # хвост
    p.shape("M640 820 Q770 790 780 690 Q790 650 820 665 Q840 700 815 760 Q780 845 650 860 Z")
    sit_body(p)
    p.ellipse(640, 720, 45, 60, rot=20)  # пятнышко
    # ошейник с косточкой
    p.shape("M392 575 Q512 640 632 575 L640 610 Q512 680 384 610 Z")
    bone(p, 512, 690, 0.9)
    # голова
    p.ellipse(CX, 400, 195, 172)
    p.ellipse(605, 330, 55, 45, rot=-20)  # пятно у глаза
    # висячие уши
    p.shape("M352 285 C262 280 218 410 252 520 C268 568 330 560 345 505 C362 440 372 370 392 315 Z")
    p.shape("M672 285 C762 280 806 410 772 520 C756 568 694 560 679 505 C662 440 652 370 632 315 Z")
    p.eye(435, 395, 44)
    p.eye(589, 395, 44)
    p.ellipse(CX, 490, 92, 64)  # мордочка
    p.nose(512, 468, 34, 22)
    p.line("M512 490 L512 505", sw=8)
    p.open_mouth(512, 505, 40, 40)
    p.cheek(386, 480)
    p.cheek(638, 480)
    return p


def kitten():
    p = Pic()
    scene_bg(p, right=False)
    p.star(830, 180, 45, rot=10)
    p.star(900, 330, 28)
    p.heart(150, 200, 40)
    # клубок
    p.circle(820, 800, 85)
    p.line("M745 770 Q820 740 900 790", sw=7)
    p.line("M750 830 Q830 790 895 835", sw=7)
    p.line("M790 720 Q760 800 800 880", sw=7)
    p.line("M736 830 Q700 860 690 880", sw=7)
    # хвост
    p.shape("M380 830 C220 840 160 730 205 640 C222 605 262 618 250 655 C222 725 268 790 380 780 Z")
    sit_body(p)
    # уши
    p.shape("M340 330 Q320 210 345 165 Q400 180 455 250 Z")
    p.shape("M684 330 Q704 210 679 165 Q624 180 569 250 Z")
    p.shape("M362 290 Q352 225 365 200 Q395 215 420 255 Z", sw=7)
    p.shape("M662 290 Q672 225 659 200 Q629 215 604 255 Z", sw=7)
    # голова с щёчками-пушками
    p.shape("M320 380 C320 260 410 225 512 225 C614 225 704 260 704 380 C704 420 730 440 715 470 "
            "C735 490 700 520 690 530 C650 590 580 600 512 600 C444 600 374 590 334 530 "
            "C324 520 289 490 309 470 C294 440 320 420 320 380 Z")
    p.line("M490 250 L493 290", sw=8)
    p.line("M512 245 L512 295", sw=8)
    p.line("M534 250 L531 290", sw=8)
    p.eye(435, 405, 48, lash=True, side=-1)
    p.eye(589, 405, 48, lash=True, side=1)
    p.cheek(380, 480)
    p.cheek(644, 480)
    p.nose(512, 470, 20, 13)
    p.cat_mouth(512, 484, 28)
    for dy, dx in ((-10, -5), (8, 0), (26, 8)):
        p.line(f"M455 {482 + dy} L{345 + dx} {470 + dy * 2}", sw=6)
        p.line(f"M569 {482 + dy} L{679 - dx} {470 + dy * 2}", sw=6)
    return p


def mane(p, cx, cy, r, n=14, bump=0.16, ry=None):
    ry = ry or r
    d = ""
    for i in range(n):
        a0 = math.radians(i * 360 / n)
        a1 = math.radians((i + 1) * 360 / n)
        am = (a0 + a1) / 2
        x0, y0 = cx + r * math.cos(a0), cy + ry * math.sin(a0)
        x1, y1 = cx + r * math.cos(a1), cy + ry * math.sin(a1)
        xm, ym = cx + r * (1 + bump * 2) * math.cos(am), cy + ry * (1 + bump * 2) * math.sin(am)
        d += (f"M{f(x0)} {f(y0)} " if i == 0 else "") + f"Q{f(xm)} {f(ym)} {f(x1)} {f(y1)} "
    p.shape(d + "Z")


def lion():
    p = Pic()
    scene_bg(p)
    p.sun(860, 160, 62)
    # хвост с кисточкой
    p.line("M650 830 Q790 840 800 730 Q805 690 780 670", sw=12)
    p.shape("M780 670 Q740 640 760 600 Q790 620 800 600 Q830 630 812 660 Q805 690 780 670 Z")
    sit_body(p)
    mane(p, CX, 395, 215, 16, 0.12)
    p.circle(395, 270, 42)
    p.circle(629, 270, 42)
    p.circle(395, 270, 20, sw=7)
    p.circle(629, 270, 20, sw=7)
    p.ellipse(CX, 410, 168, 150)
    p.eye(445, 390, 40)
    p.eye(579, 390, 40)
    p.line("M410 335 Q440 320 470 335", sw=7)
    p.line("M554 335 Q584 320 614 335", sw=7)
    p.ellipse(478, 488, 46, 34)
    p.ellipse(546, 488, 46, 34)
    for x in (460, 480, 544, 564):
        p.dot(x, 485, 5)
    p.nose(512, 458, 26, 17)
    p.smile(512, 520, 22, 8, sw=8)
    p.cheek(390, 470)
    p.cheek(634, 470)
    return p


def elephant():
    p = Pic()
    scene_bg(p)
    p.cloud(180, 180, 0.75)
    p.sparkle(860, 170, 40)
    p.sparkle(800, 260, 22)
    p.line("M650 830 Q740 840 760 780", sw=10)
    p.shape("M760 780 Q750 750 775 745 Q795 760 780 790 Z", sw=8)
    sit_body(p, belly=False)
    # уши
    p.ellipse(318, 395, 135, 155, rot=-18)
    p.ellipse(706, 395, 135, 155, rot=18)
    p.ellipse(330, 400, 85, 105, rot=-18, sw=8)
    p.ellipse(694, 400, 85, 105, rot=18, sw=8)
    p.ellipse(CX, 385, 172, 158)
    # хобот
    p.shape("M462 450 C455 560 470 650 535 662 C600 672 628 610 598 595 C575 585 560 615 540 610 "
            "C515 600 548 520 562 450 Z")
    p.line("M478 520 Q505 528 532 520", sw=7)
    p.line("M482 565 Q508 572 532 563", sw=7)
    p.eye(445, 375, 40)
    p.eye(579, 375, 40)
    p.cheek(408, 455)
    p.cheek(616, 455)
    p.line("M495 232 Q480 200 500 185", sw=8)
    p.line("M515 230 Q520 195 545 192", sw=8)
    return p


def panda():
    p = Pic()
    scene_bg(p, right=False)
    # бамбук справа
    p.rect(800, 380, 52, 500, r=10)
    for y in (480, 600, 720):
        p.line(f"M800 {y} L852 {y}")
    p.leaf(852, 470, -30, 110, 34)
    p.leaf(800, 600, 200, 100, 30)
    p.leaf(852, 720, -10, 90, 28)
    sit_body(p)
    # ушки
    p.circle(360, 265, 62)
    p.circle(664, 265, 62)
    p.ellipse(CX, 405, 210, 180)
    # пятна вокруг глаз
    p.ellipse(430, 405, 70, 88, rot=30)
    p.ellipse(594, 405, 70, 88, rot=-30)
    p.eye(438, 400, 38)
    p.eye(586, 400, 38)
    p.nose(512, 480, 30, 19)
    p.cat_mouth(512, 498, 26)
    p.cheek(380, 500)
    p.cheek(644, 500)
    # веточка бамбука в лапке
    p.rect(470, 610, 30, 170, r=8, rot=-25, cx=485, cy=695)
    p.leaf(535, 625, -50, 70, 22, sw=8)
    return p


ALL = [bunny, puppy, kitten, lion, elephant, panda]

if __name__ == "__main__":
    out = []
    for i, fn in enumerate(ALL, 1):
        path = f"/home/claude/art/out/animals/{i:03d}.png"
        render(fn(), path)
        out.append(path)
    sheet(out, "/home/claude/art/sheet_animals.png", cols=3)
    sheet(out, "/home/claude/art/sheet_animals_regions.png", cols=3, regions=True)
