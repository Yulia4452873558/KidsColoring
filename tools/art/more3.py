from lib import *
from animals import sit_body, scene_bg, mane, CX
from farm_sea import fence, waves, bubbles
from dino_trans_space import wheel, stars_bg
from more import unicorn_side, curl, horn, princess
from more2 import tree_bg
from letters import o_butterfly


def mini_bee(p, x, y, s=1.0, flip=1):
    with p.group(f"translate({x} {y}) scale({s * flip} {s})"):
        p.ellipse(-15, -35, 22, 28, rot=-20, sw=7)
        p.ellipse(15, -35, 22, 28, rot=20, sw=7)
        body = "M-45 0 C-45 -30 45 -30 45 0 C45 30 -45 30 -45 0 Z"
        p.shape(body, sw=8)
        p.line("M-12 -25 L-12 25", sw=7)
        p.line("M12 -25 L12 25", sw=7)
        p.dot(-30, -5, 5)


# ================= КОСМОС =================

def moon_rover():
    p = Pic()
    stars_bg(p, [(120, 120, 30), (420, 90, 22), (700, 200, 26), (930, 520, 24)])
    # Земля
    earth = "M850 230 m-110 0 a110 110 0 1 0 220 0 a110 110 0 1 0 -220 0 Z"
    p.shape(earth)
    with p.clipped(earth):
        p.shape("M760 180 Q800 140 840 170 Q860 220 820 250 Q780 260 760 230 Z", sw=8)
        p.shape("M870 260 Q910 230 950 270 Q940 330 890 320 Q860 300 870 260 Z", sw=8)
    p.shape(earth, fill="none")
    # поверхность
    p.shape("M-10 800 Q250 740 512 770 Q780 800 1034 750 L1034 1034 L-10 1034 Z")
    for x, y, rx in ((160, 900, 70), (760, 930, 90), (480, 970, 50)):
        p.ellipse(x, y, rx, rx * 0.35, sw=8)
    # антенна и тарелка
    p.line("M640 430 L680 300", sw=10)
    p.shape("M630 300 Q680 230 760 260 Q720 330 630 300 Z")
    p.circle(700, 270, 10, sw=6)
    # солнечная панель
    p.line("M330 430 L300 360", sw=10)
    p.rect(180, 300, 220, 80, r=8, rot=-10)
    p.line("M253 292 L262 377 M326 280 L338 368", sw=7)
    # корпус
    p.rect(250, 430, 520, 200, r=40)
    p.rect(300, 470, 180, 110, r=24)
    p.eye(355, 520, 30)
    p.eye(425, 520, 30)
    p.smile(390, 560, 22, 8, sw=7)
    for x in (560, 640, 710):
        p.circle(x, 520, 22, sw=8)
    p.rect(220, 620, 580, 40, r=18)
    for x in (300, 512, 724):
        wheel(p, x, 710, 70)
    return p


def alien():
    p = Pic()
    stars_bg(p, [(120, 150, 34), (880, 120, 30), (150, 520, 24), (900, 470, 28)])
    # планета-земля под ногами
    p.shape("M-10 820 Q512 720 1034 820 L1034 1034 L-10 1034 Z")
    for x, y, rx in ((200, 900, 60), (800, 910, 70), (500, 960, 40)):
        p.ellipse(x, y, rx, rx * 0.35, sw=8)
    # тарелка вдалеке
    p.ellipse(800, 300, 110, 34)
    p.shape("M740 290 Q745 220 800 220 Q855 220 860 290 Z")
    for x in (740, 800, 860):
        p.circle(x, 310, 9, sw=6)
    # ножки
    p.rect(430, 680, 60, 130, r=24)
    p.rect(534, 680, 60, 130, r=24)
    p.ellipse(450, 810, 55, 28)
    p.ellipse(574, 810, 55, 28)
    # ручки
    p.shape("M420 560 Q330 520 300 430 L345 415 Q370 480 440 510 Z")
    p.circle(318, 405, 34)
    p.shape("M604 560 Q680 600 700 680 L655 690 Q640 630 590 610 Z")
    p.circle(680, 705, 34)
    # тело
    p.ellipse(512, 610, 120, 120)
    p.ellipse(512, 630, 70, 70, sw=8)
    p.star(512, 630, 34)
    # антенны
    p.line("M450 230 Q420 150 380 130", sw=9)
    p.line("M574 230 Q604 150 644 130", sw=9)
    p.circle(380, 125, 24)
    p.circle(644, 125, 24)
    # голова
    p.ellipse(512, 360, 200, 160)
    p.eye(440, 360, 52)
    p.eye(584, 360, 52)
    p.cheek(400, 440, 20)
    p.cheek(624, 440, 20)
    p.open_mouth(512, 445, 30, 30)
    return p


def sleepy_moon():
    p = Pic()
    # звёзды на ниточках
    for x, L, r in ((180, 300, 50), (330, 180, 38), (840, 260, 46), (700, 140, 32)):
        p.line(f"M{x} -10 L{x} {L - r}", sw=6)
        p.star(x, L, r)
    # облака
    p.cloud(250, 820, 1.1)
    p.cloud(760, 860, 1.2)
    # месяц
    moon = ("M560 260 C420 290 340 420 360 560 C380 700 510 790 650 780 C560 740 490 650 480 540 "
            "C470 420 520 320 560 260 Z")
    p.shape(moon)
    # колпачок
    p.shape("M520 300 Q600 200 700 230 Q780 260 790 340 Q720 300 640 330 Z")
    p.circle(800, 355, 30)
    p.closed_eye(440, 470, 32)
    p.smile(435, 560, 22, 8, sw=8)
    p.cheek(470, 520, 18)
    p.line("M560 420 q20 -25 40 0 M590 380 q20 -25 40 0", sw=7)
    return p


# ================= СКАЗКИ =================

def fairy():
    wings = Pic()
    for sx in (-1, 1):
        wings.ellipse(512 + sx * 230, 520, 120, 170, rot=sx * 30)
        wings.ellipse(512 + sx * 200, 720, 80, 110, rot=sx * -20)
        wings.ellipse(512 + sx * 230, 510, 50, 90, rot=sx * 30, sw=7)
    p = princess()
    p.items = p.items[:5] + wings.items + p.items[5:]
    return p


def cat_wizard():
    p = Pic()
    for x, y, r in ((140, 140, 34), (880, 160, 30), (120, 450, 22)):
        p.star(x, y, r)
    p.sparkle(860, 420, 34)
    p.ground(512, 900, 380, 45)
    # книга
    p.shape("M650 840 L640 760 Q720 730 800 760 Q880 730 960 760 L950 840 Q880 815 800 840 Q720 815 650 840 Z")
    p.line("M800 760 L800 840", sw=8)
    # хвост
    p.shape("M380 830 C220 840 160 730 205 640 C222 605 262 618 250 655 C222 725 268 790 380 780 Z")
    sit_body(p)
    # плащ-звёздочки на груди
    p.star(512, 740, 38)
    # уши
    p.shape("M340 360 Q320 240 345 195 Q400 210 455 280 Z")
    p.shape("M684 360 Q704 240 679 195 Q624 210 569 280 Z")
    p.ellipse(CX, 430, 200, 165)
    # шляпа волшебника
    p.shape("M300 330 Q512 380 724 330 Q740 300 700 290 L560 60 Q540 40 520 70 L330 290 Q290 300 300 330 Z")
    p.line("M350 300 Q512 340 680 300", sw=8)
    p.star(520, 200, 30)
    p.star(600, 255, 18)
    p.eye(440, 440, 44, lash=True, side=-1)
    p.eye(584, 440, 44, lash=True, side=1)
    p.nose(512, 500, 18, 12)
    p.cat_mouth(512, 512, 24)
    p.cheek(390, 505)
    p.cheek(634, 505)
    # палочка
    p.line("M640 700 L780 520", sw=10)
    p.star(795, 500, 40)
    return p


def treasure():
    p = Pic()
    p.sparkle(150, 160, 44)
    p.sparkle(880, 200, 38)
    p.star(860, 420, 26)
    p.star(150, 420, 24)
    p.ground(512, 880, 400, 50)
    # монетки
    for x, y in ((230, 830), (290, 860), (790, 840), (740, 870)):
        p.ellipse(x, y, 45, 25)
    # открытая крышка
    p.shape("M270 430 L310 210 Q512 160 714 210 L754 430 Z")
    p.line("M410 190 L400 430 M614 190 L624 430", sw=9)
    # сокровища
    p.shape("M260 430 Q300 350 360 380 Q400 330 450 370 Q512 330 560 370 Q620 330 660 380 Q720 350 764 430 Z")
    p.shape("M430 380 L450 300 L490 340 L512 280 L534 340 L574 300 L594 380 Z")
    p.circle(512, 330, 12, sw=6)
    p.poly([(320, 400), (345, 370), (370, 400), (345, 430)], sw=7)
    p.poly([(660, 395), (690, 365), (720, 395), (690, 425)], sw=7)
    # сундук
    p.rect(250, 430, 524, 380, r=20)
    p.line("M400 430 L400 810 M624 430 L624 810", sw=9)
    p.rect(470, 470, 84, 100, r=14)
    p.circle(512, 510, 14, sw=7)
    p.line("M512 524 L512 550", sw=7)
    p.eye(440, 680, 30)
    p.eye(584, 680, 30)
    p.smile(512, 720, 28, 10)
    return p


# ================= ЕДИНОРОГИ =================

def unicorn_balloons():
    p = Pic()
    p.ground(560, 895, 400, 50)
    p.cloud(160, 170, 0.6)
    p.star(900, 600, 30)
    # шарики
    for x, y, kind in ((640, 190, "heart"), (780, 150, "star"), (900, 260, "plain")):
        p.line(f"M{x} {y + 85} Q{x - 40} {y + 220} 610 470", sw=6)
        p.ellipse(x, y, 62, 78)
        p.poly([(x, y + 75), (x - 14, y + 95), (x + 14, y + 95)], sw=7)
        if kind == "heart":
            p.heart(x, y, 26, sw=7)
        elif kind == "star":
            p.star(x, y, 30, sw=7)
    unicorn_side(p, 0, 20)
    return p


def pegasus():
    p = Pic()
    for x, y, r in ((120, 140, 34), (900, 120, 30), (680, 110, 22), (160, 520, 24)):
        p.star(x, y, r)
    p.cloud(200, 880, 1.0)
    p.cloud(800, 900, 1.1)
    p.cloud(880, 640, 0.5)
    unicorn_side(p, 30, -40, 0.95, wing=2)
    return p


# ================= ВКУСНЯШКИ =================

def pizza():
    p = Pic()
    p.sparkle(150, 160, 40)
    p.sparkle(880, 860, 34)
    p.star(860, 180, 30)
    # корочка
    p.shape("M170 250 Q512 150 854 250 L512 900 Z")
    p.shape("M210 300 Q512 210 814 300 L512 860 Z")
    p.line("M180 250 Q512 160 844 250", sw=9)
    p.shape("M210 300 Q512 210 814 300 L512 860 Z", fill="none")
    for x, y, r in ((360, 360, 45), (620, 340, 40), (500, 520, 42), (420, 640, 34), (600, 620, 30)):
        p.circle(x, y, r)
        p.dot(x - r * 0.3, y - r * 0.2, r * 0.12)
    p.eye(450, 450, 30)
    p.eye(570, 450, 30)
    p.smile(510, 500, 22, 8, sw=8)
    # тянущийся сыр
    p.shape("M480 860 Q470 930 490 960 Q510 975 520 950 Q530 920 525 860 Z", sw=8)
    return p


def burger():
    p = Pic()
    p.sparkle(150, 160, 40)
    p.sparkle(880, 200, 34)
    p.ellipse(512, 880, 360, 50)
    p.shape("M230 760 Q230 840 300 850 L724 850 Q794 840 794 760 Z")  # нижняя булка
    p.rect(220, 690, 584, 80, r=40)  # котлета
    # сыр
    p.shape("M210 680 L814 680 L790 720 L700 720 Q690 780 660 780 Q640 760 640 720 L420 720 Q410 790 380 790 Q350 770 360 720 L230 720 Z")
    # салат
    p.shape("M200 650 Q240 610 280 650 Q320 610 360 650 Q400 610 440 650 Q480 610 520 650 Q560 610 600 650 "
            "Q640 610 680 650 Q720 610 760 650 Q800 610 830 650 L830 690 L200 690 Z")
    # верхняя булка
    p.shape("M210 630 C210 400 350 330 512 330 C674 330 814 400 814 630 Z")
    for x, y, a in ((330, 450, 20), (420, 390, -10), (600, 390, 15), (700, 460, -20), (520, 360, 0)):
        p.ellipse(x, y, 9, 16, rot=a, sw=6)
    p.eye(450, 520, 34)
    p.eye(574, 520, 34)
    p.smile(512, 570, 30, 12)
    p.cheek(400, 580, 20)
    p.cheek(624, 580, 20)
    return p


def candies():
    p = Pic()
    p.sparkle(880, 160, 36)
    p.star(140, 860, 30)
    # леденец
    p.rect(300, 520, 34, 380, r=12)
    lol = "M317 360 m-190 0 a190 190 0 1 0 380 0 a190 190 0 1 0 -380 0 Z"
    p.shape(lol)
    p.line("M317 360 C317 330 357 330 357 360 C357 410 277 410 277 360 C277 290 407 280 417 360 "
           "C427 460 227 470 217 360 C207 230 467 210 487 360", sw=9)
    p.shape("M300 560 Q260 600 270 640 Q300 610 317 580 Q334 610 364 640 Q374 600 334 560 Z", sw=8)
    # конфеты в фантиках
    for cx, cy, rot in ((720, 380, -20), (700, 720, 15)):
        with p.group(f"rotate({rot} {cx} {cy})"):
            p.poly([(cx - 90, cy), (cx - 160, cy - 60), (cx - 150, cy), (cx - 160, cy + 60)])
            p.poly([(cx + 90, cy), (cx + 160, cy - 60), (cx + 150, cy), (cx + 160, cy + 60)])
            p.ellipse(cx, cy, 100, 70)
            p.line(f"M{cx - 40} {cy - 62} Q{cx - 10} {cy} {cx - 40} {cy + 62}", sw=7)
            p.line(f"M{cx + 40} {cy - 62} Q{cx + 70} {cy} {cx + 40} {cy + 62}", sw=7)
    p.eye(690, 380, 22)
    p.eye(750, 370, 22)
    return p


def pancakes():
    p = Pic()
    p.sparkle(150, 160, 40)
    p.star(870, 180, 30)
    p.ellipse(512, 860, 380, 60)
    p.ellipse(512, 860, 300, 40, sw=7)
    for y in (790, 700, 610):
        p.shape(f"M200 {y} Q200 {y - 50} 512 {y - 55} Q824 {y - 50} 824 {y} Q824 {y + 55} 512 {y + 60} Q200 {y + 55} 200 {y} Z")
    # сироп
    p.shape("M230 560 Q240 500 512 495 Q784 500 794 560 Q790 600 760 590 Q740 650 710 640 Q700 600 650 610 "
            "Q600 620 560 600 Q540 680 500 670 Q490 610 430 605 Q380 640 340 610 Q300 600 280 650 Q250 650 250 600 Q230 590 230 560 Z")
    p.rect(450, 470, 120, 60, r=14)
    for x, y in ((360, 470), (650, 470), (720, 500)):
        p.circle(x, y, 32)
    p.eye(440, 720, 30)
    p.eye(584, 720, 30)
    p.smile(512, 755, 26, 10, sw=8)
    return p


# ================= ПРИРОДА =================

def tulips():
    p = Pic()
    p.sun(150, 140, 55)
    p.cloud(840, 150, 0.6)
    o_butterfly(p, 840, 400, 0.45)
    for x, top, a in ((330, 360, -12), (512, 280, 0), (694, 360, 12)):
        p.line(f"M{x} {top + 80} Q{x + a * 2} 600 {512 + (x - 512) // 4} 720", sw=12)
        p.leaf(512 + (x - 512) // 4, 660, -90 + a * 4 + (40 if x > 512 else -40 if x < 512 else 20), 130, 40)
        with p.group(f"rotate({a} {x} {top})"):
            p.shape(f"M{x - 70} {top - 60} L{x - 35} {top - 20} L{x} {top - 75} L{x + 35} {top - 20} L{x + 70} {top - 60} "
                    f"Q{x + 85} {top + 50} {x} {top + 90} Q{x - 85} {top + 50} {x - 70} {top - 60} Z")
            p.line(f"M{x} {top - 40} Q{x - 15} {top + 20} {x} {top + 85}", sw=7)
    # горшок
    p.shape("M330 740 L694 740 L650 950 L374 950 Z")
    p.rect(300, 700, 424, 60, r=14)
    p.eye(460, 830, 26)
    p.eye(564, 830, 26)
    p.smile(512, 870, 24, 8, sw=8)
    return p


def cactus():
    p = Pic()
    p.sun(860, 140, 60)
    p.cloud(180, 160, 0.6)
    # кактус
    p.shape("M300 520 Q260 520 260 480 L260 380 Q260 340 300 340 Q340 340 340 380 L340 460 L400 460 L400 560 L300 560 Z")
    p.shape("M724 460 Q764 460 764 420 L764 340 Q764 300 724 300 Q684 300 684 340 L684 400 L624 400 L624 500 L724 500 Z")
    p.shape("M380 750 L380 330 C380 230 440 200 512 200 C584 200 644 230 644 330 L644 750 Z")
    for x, y in ((420, 300), (600, 330), (440, 640), (590, 660), (300, 420), (725, 360)):
        p.line(f"M{x - 10} {y} L{x + 10} {y} M{x} {y - 10} L{x} {y + 10}", sw=6)
    p.flower(512, 190, 50)
    p.eye(465, 430, 34)
    p.eye(559, 430, 34)
    p.cheek(430, 500, 20)
    p.cheek(594, 500, 20)
    p.smile(512, 495, 28, 12)
    # горшок
    p.shape("M330 760 L694 760 L650 950 L374 950 Z")
    p.rect(300, 720, 424, 60, r=14)
    p.heart(512, 860, 34)
    return p


def fir(p, x, y, s=1.0):
    p.rect(x - 22 * s, y - 50 * s, 44 * s, 60 * s)
    p.shape(f"M{x} {y - 420 * s} L{x + 90 * s} {y - 290 * s} L{x + 50 * s} {y - 290 * s} L{x + 140 * s} {y - 160 * s} "
            f"L{x + 90 * s} {y - 160 * s} L{x + 180 * s} {y - 40 * s} L{x - 180 * s} {y - 40 * s} L{x - 90 * s} {y - 160 * s} "
            f"L{x - 140 * s} {y - 160 * s} L{x - 50 * s} {y - 290 * s} L{x - 90 * s} {y - 290 * s} Z")


def forest():
    p = Pic()
    p.sun(860, 130, 55)
    p.cloud(420, 140, 0.6)
    p.hill(880, 25)
    fir(p, 180, 880, 1.1)
    tree_bg(p, 820, 880, 1.0)
    fir(p, 640, 870, 0.75)
    # грибочки
    for x, s in ((380, 1.0), (470, 0.7)):
        p.shape(f"M{x - 30 * s} 880 Q{x - 35 * s} {880 - 70 * s} {x - 25 * s} {880 - 100 * s} L{x + 25 * s} {880 - 100 * s} Q{x + 35 * s} {880 - 70 * s} {x + 30 * s} 880 Z")
        p.shape(f"M{x - 90 * s} {880 - 90 * s} C{x - 90 * s} {880 - 210 * s} {x + 90 * s} {880 - 210 * s} {x + 90 * s} {880 - 90 * s} Q{x} {880 - 70 * s} {x - 90 * s} {880 - 90 * s} Z")
        p.circle(x - 35 * s, 880 - 150 * s, 15 * s, sw=7)
        p.circle(x + 30 * s, 880 - 135 * s, 12 * s, sw=7)
    o_butterfly(p, 450, 450, 0.55)
    p.flower(560, 830, 36)
    p.line("M560 866 L560 900", sw=8)
    return p


# ================= ПРАЗДНИКИ =================

def easter_basket():
    p = Pic()
    p.sparkle(150, 150, 40)
    p.sparkle(880, 170, 34)
    p.flower(150, 820, 45)
    p.flower(880, 830, 40, petals=6)
    # ручка
    p.line("M230 520 C230 140 794 140 794 520", sw=36)
    p.raw('<path d="M230 520 C230 140 794 140 794 520" fill="none" stroke="#fff" stroke-width="16"/>')
    # яйца
    eggs = ((330, 470, -15), (460, 420, -5), (590, 430, 8), (710, 480, 18))
    for i, (x, y, a) in enumerate(eggs):
        egg = f"M{x} {y - 110} C{x + 80} {y - 110} {x + 85} {y + 60} {x} {y + 70} C{x - 85} {y + 60} {x - 80} {y - 110} {x} {y - 110} Z"
        with p.group(f"rotate({a} {x} {y})"):
            p.shape(egg)
            with p.clipped(egg):
                if i % 2 == 0:
                    p.line(f"M{x - 90} {y - 20} L{x - 60} {y - 50} L{x - 30} {y - 20} L{x} {y - 50} L{x + 30} {y - 20} L{x + 60} {y - 50} L{x + 90} {y - 20}", sw=7)
                    p.line(f"M{x - 90} {y + 20} L{x + 90} {y + 20}", sw=7)
                else:
                    for dx, dy in ((-30, -50), (25, -30), (-20, 10), (30, 30)):
                        p.circle(x + dx, y + dy, 13, sw=6)
            p.shape(egg, fill="none")
    # корзинка
    basket = "M200 520 L824 520 L760 860 Q512 900 264 860 Z"
    p.shape(basket)
    with p.clipped(basket):
        for y in (600, 680, 760, 840):
            p.line(f"M180 {y} Q512 {y + 30} 844 {y}", sw=7)
        for x in range(250, 800, 70):
            p.line(f"M{x} 520 L{x + (x - 512) * -0.08} 900", sw=6)
    p.shape(basket, fill="none")
    p.rect(185, 500, 654, 50, r=20)
    return p


def pumpkin():
    p = Pic()
    p.cloud(170, 150, 0.6)
    p.star(870, 150, 34)
    p.ground(512, 880, 380, 50)
    # листья и усики
    p.leaf(560, 300, -40, 140, 50)
    p.line("M470 300 C420 250 440 200 480 210 C510 220 500 250 480 245", sw=8)
    p.rect(490, 230, 50, 110, r=12, rot=10)
    # тыква
    for x, rx in ((340, 150), (684, 150), (430, 140), (594, 140), (512, 140)):
        p.ellipse(x, 600, rx, 240)
    p.eye(450, 560, 40)
    p.eye(574, 560, 40)
    p.cheek(410, 640)
    p.cheek(614, 640)
    p.smile(512, 650, 40, 16)
    return p


def santa_bear():
    p = Pic()
    for x, y, r in ((130, 170, 26), (890, 160, 30), (120, 500, 20), (900, 470, 24)):
        for a in (0, 60, 120):
            ra = math.radians(a)
            p.line(f"M{f(x - r * math.cos(ra))} {f(y - r * math.sin(ra))} L{f(x + r * math.cos(ra))} {f(y + r * math.sin(ra))}", sw=7)
    p.shape("M-10 850 Q300 800 512 840 Q760 880 1034 820 L1034 1034 L-10 1034 Z")
    sit_body(p)
    p.circle(355, 280, 58)
    p.circle(669, 280, 58)
    p.ellipse(CX, 410, 205, 175)
    # колпак
    p.shape("M320 330 Q512 260 704 330 Q690 200 600 170 Q560 140 520 110 Q450 80 430 170 Q350 220 320 330 Z")
    p.circle(430, 120, 36)
    p.rect(300, 300, 424, 60, r=30, rot=-2)
    p.eye(440, 410, 40)
    p.eye(584, 410, 40)
    p.ellipse(CX, 490, 78, 55)
    p.nose(512, 470, 28, 18)
    p.cat_mouth(512, 487, 22)
    p.cheek(390, 480)
    p.cheek(634, 480)
    # подарок
    p.rect(400, 620, 224, 180, r=10)
    p.rect(490, 620, 44, 180, sw=8)
    p.shape("M512 620 C470 560 420 580 445 610 Z", sw=8)
    p.shape("M512 620 C554 560 604 580 579 610 Z", sw=8)
    p.ellipse(400, 700, 45, 55, rot=-20)
    p.ellipse(624, 700, 45, 55, rot=20)
    return p


# ================= РОБОТЫ =================

def robot_flower():
    p = Pic()
    p.hill(890, 20)
    p.sun(860, 140, 55)
    p.flower(150, 760, 50)
    p.line("M150 810 L150 900", sw=8)
    # ноги
    p.rect(420, 700, 70, 160, r=16)
    p.rect(534, 700, 70, 160, r=16)
    p.rect(400, 840, 110, 50, r=20)
    p.rect(514, 840, 110, 50, r=20)
    # руки
    p.rect(250, 470, 90, 50, r=20)
    p.circle(240, 495, 38)
    p.rect(684, 400, 50, 150, r=20, rot=-20)
    p.circle(740, 380, 38)
    # цветок в руке
    p.line("M745 345 L760 220", sw=9)
    p.flower(765, 185, 48, petals=6)
    p.leaf(752, 290, -30, 60, 20, sw=7)
    # корпус
    p.rect(330, 430, 364, 290, r=40)
    p.heart(512, 560, 60)
    for x in (430, 512, 594):
        p.circle(x, 670, 18, sw=7)
    # голова
    p.rect(470, 390, 84, 50, r=10)
    p.rect(350, 170, 324, 230, r=50)
    p.circle(440, 270, 46)
    p.dot(440, 270, 22)
    p.circle(584, 270, 46)
    p.dot(584, 270, 22)
    p.rect(450, 330, 124, 34, r=14)
    for x in (480, 512, 544):
        p.line(f"M{x} 330 L{x} 364", sw=6)
    p.line("M512 170 L512 110", sw=9)
    p.heart(512, 95, 22, sw=8)
    return p


def robot_friends():
    p = Pic()
    p.line("M-10 880 L1034 880")
    p.star(140, 140, 34)
    p.sparkle(880, 150, 40)
    p.heart(512, 220, 46)
    # круглый робот
    p.rect(200, 700, 60, 150, r=14)
    p.rect(320, 700, 60, 150, r=14)
    p.rect(180, 840, 100, 40, r=16)
    p.rect(300, 840, 100, 40, r=16)
    p.circle(290, 590, 170)
    p.circle(290, 590, 80, sw=8)
    p.line("M290 420 L290 360", sw=9)
    p.circle(290, 345, 20)
    p.eye(250, 520, 32)
    p.eye(330, 520, 32)
    p.smile(290, 600, 30, 12)
    p.rect(440, 560, 70, 40, r=18)
    # квадратный робот
    p.rect(620, 760, 220, 80, r=30)
    for x in (660, 730, 800):
        p.circle(x, 850, 34)
    p.rect(600, 440, 260, 330, r=30)
    p.rect(640, 480, 180, 120, r=20)
    p.eye(690, 535, 30)
    p.eye(770, 535, 30)
    p.smile(730, 580, 20, 6, sw=7)
    for x, y in ((650, 670), (730, 670), (810, 670)):
        p.rect(x - 25, y - 25, 50, 50, r=10, sw=8)
    p.rect(520, 560, 90, 40, r=18)
    p.line("M690 440 L670 380 M770 440 L790 380", sw=9)
    p.circle(668, 370, 18)
    p.circle(792, 370, 18)
    return p


def robot_cat():
    p = Pic()
    p.ground(512, 890, 380, 50)
    p.star(140, 160, 34)
    p.sparkle(880, 170, 40)
    # хвост-пружинка
    p.line("M680 760 Q760 760 760 700 Q760 650 800 650 Q840 650 840 600 Q840 550 880 550", sw=12)
    p.circle(890, 540, 26)
    # тело
    p.rect(330, 600, 364, 260, r=40)
    p.rect(420, 650, 184, 100, r=18)
    p.line("M440 700 L480 680 L520 720 L560 680 L590 700", sw=7)
    for x in (380, 470, 554, 644):
        p.rect(x - 32, 820, 64, 60, r=14)
    # голова
    p.poly([(330, 320), (300, 150), (440, 250)])
    p.poly([(694, 320), (724, 150), (584, 250)])
    p.rect(300, 230, 424, 330, r=60)
    p.rect(350, 290, 324, 200, r=40)
    p.eye(445, 380, 42)
    p.eye(579, 380, 42)
    p.poly([(495, 440), (529, 440), (512, 460)], sw=7)
    p.smile(512, 465, 30, 10, sw=7)
    for sx in (-1, 1):
        for dy in (-12, 12):
            p.line(f"M{512 + sx * 120} {450 + dy} L{512 + sx * 250} {440 + dy * 2}", sw=6)
    for x, y in ((360, 520), (664, 520)):
        p.circle(x, y, 14, sw=6)
    return p


# ================= БУКАШКИ =================

def dragonfly():
    p = Pic()
    p.sun(860, 130, 55)
    # пруд и камыши
    p.ellipse(512, 880, 420, 70)
    p.ellipse(380, 890, 60, 16, sw=7)
    p.ellipse(650, 870, 50, 14, sw=7)
    for x in (120, 170, 860, 910):
        p.line(f"M{x} 880 Q{x - 10} 700 {x + 10} 560", sw=9)
        p.rect(x - 2, 540, 30, 110, r=15, rot=4)
    # крылья
    for sx in (-1, 1):
        p.ellipse(512 + sx * 200, 330, 200, 70, rot=sx * -12)
        p.ellipse(512 + sx * 190, 470, 180, 60, rot=sx * 15)
        p.line(f"M{512 + sx * 60} 340 L{512 + sx * 350} 300", sw=6)
        p.line(f"M{512 + sx * 60} 460 L{512 + sx * 330} 500", sw=6)
    # хвостик-сегменты
    for i in range(6):
        p.ellipse(512, 520 + i * 55, 32 - i * 2, 34)
    # грудка и голова
    p.ellipse(512, 410, 55, 90)
    p.circle(512, 270, 85)
    p.eye(475, 255, 34)
    p.eye(549, 255, 34)
    p.smile(512, 310, 22, 8, sw=8)
    return p


def ant():
    p = Pic()
    p.sun(150, 140, 55)
    p.cloud(840, 160, 0.6)
    p.hill(870, 20)
    p.grass(830, 880, 3, 55)
    # листик над головой
    p.line("M560 420 L620 300", sw=10)
    p.shape("M620 300 C560 180 640 90 800 110 C820 260 740 330 620 300 Z")
    p.line("M640 290 Q700 220 790 125", sw=7)
    # ножки
    for x in (430, 530, 640):
        p.line(f"M{x} 640 L{x - 40} 760 L{x - 70} 770", sw=11)
        p.line(f"M{x + 10} 640 L{x + 50} 770 L{x + 80} 780", sw=11)
    # тело из трёх частей
    p.ellipse(700, 600, 140, 110)
    p.ellipse(500, 600, 90, 70)
    # голова
    p.line("M310 420 Q290 330 250 310", sw=9)
    p.line("M360 410 Q380 320 420 300", sw=9)
    p.circle(250, 305, 20)
    p.circle(420, 295, 20)
    p.circle(340, 520, 120)
    p.eye(300, 510, 34)
    p.eye(380, 510, 34)
    p.cheek(280, 575, 18)
    p.cheek(400, 575, 18)
    p.smile(340, 575, 24, 10, sw=8)
    # ручка держит листик
    p.line("M520 560 Q560 480 570 420", sw=11)
    return p


def beehive():
    p = Pic()
    p.sun(870, 130, 55)
    # ветка
    p.shape("M-10 140 Q300 120 600 150 L600 190 Q300 170 -10 190 Z")
    p.leaf(200, 160, -110, 90, 30)
    p.line("M430 175 L430 230", sw=10)
    # улей
    hive = "M430 230 C560 230 600 300 600 360 C640 390 660 450 640 500 C680 540 690 620 650 670 C680 720 670 800 600 830 L260 830 C190 800 180 720 210 670 C170 620 180 540 220 500 C200 450 220 390 260 360 C260 300 300 230 430 230 Z"
    p.shape(hive)
    with p.clipped(hive):
        for y in (360, 500, 670):
            p.line(f"M150 {y} Q430 {y + 40} 720 {y}", sw=9)
    p.shape(hive, fill="none")
    p.ellipse(430, 600, 60, 50, fill="#000")
    p.flower(800, 820, 50)
    p.line("M800 870 L800 950", sw=8)
    p.hill(950, 15)
    mini_bee(p, 760, 420, 1.4, -1)
    mini_bee(p, 860, 600, 1.1, -1)
    mini_bee(p, 150, 560, 1.2)
    p.line("M700 470 Q650 520 690 560", sw=5)
    return p


def spider():
    p = Pic()
    # паутина
    cx, cy = 512, 380
    spokes = 10
    for i in range(spokes):
        a = math.radians(i * 360 / spokes)
        p.line(f"M{cx} {cy} L{f(cx + 500 * math.cos(a))} {f(cy + 500 * math.sin(a))}", sw=6)
    for R in (90, 180, 270, 360):
        pts = []
        for i in range(spokes + 1):
            a = math.radians(i * 360 / spokes)
            pts.append(f"{f(cx + R * math.cos(a))} {f(cy + R * math.sin(a))}")
        p.line(f"M{pts[0]} Q" + " Q".join(
            f"{f(cx + R * 0.9 * math.cos(math.radians((i + 0.5) * 360 / spokes)))} {f(cy + R * 0.9 * math.sin(math.radians((i + 0.5) * 360 / spokes)))} {pts[i + 1]}"
            for i in range(spokes)), sw=6)
    # паучок на ниточке
    p.line("M512 380 L512 600", sw=6)
    for sx in (-1, 1):
        for i, y in enumerate((660, 700, 740, 780)):
            p.line(f"M{512 + sx * 80} {y} Q{512 + sx * 170} {y - 60 + i * 20} {512 + sx * 200} {y + 30}", sw=10)
    p.circle(512, 720, 120)
    p.eye(470, 700, 36)
    p.eye(554, 700, 36)
    p.cheek(445, 770, 18)
    p.cheek(579, 770, 18)
    p.smile(512, 770, 26, 10, sw=8)
    p.ground(512, 930, 320, 40)
    return p


GROUPS = {
    "space": ([moon_rover, alien, sleepy_moon], 4),
    "fairy": ([fairy, cat_wizard, treasure], 5),
    "unicorns": ([unicorn_balloons, pegasus], 4),
    "food": ([pizza, burger, candies, pancakes], 5),
    "nature": ([tulips, cactus, forest], 5),
    "holidays": ([easter_basket, pumpkin, santa_bear], 4),
    "robots": ([robot_flower, robot_friends, robot_cat], 3),
    "insects": ([dragonfly, ant, beehive, spider], 5),
}

if __name__ == "__main__":
    import sys
    names = sys.argv[1:] or list(GROUPS)
    allp = []
    for name in names:
        fns, start = GROUPS[name]
        for i, fn in enumerate(fns):
            path = f"/home/claude/art/out/{name}/{start + i:03d}.png"
            render(fn(), path)
            allp.append(path)
    sheet(allp, "/home/claude/art/sheet_new2.png", cols=5, size=260)
    sheet(allp, "/home/claude/art/sheet_new2_regions.png", cols=5, size=260, regions=True)
