from lib import *
from animals import sit_body, scene_bg, mane, CX, bone
from farm_sea import fence, waves, bubbles, seaweed, seabed, shell
from dino_trans_space import palm, volcano, wheel, road, stars_bg
from more import unicorn_side, curl, horn


# ================= ЖИВОТНЫЕ =================

def tree_bg(p, x, y, s=1.0):
    p.rect(x - 25 * s, y - 160 * s, 50 * s, 160 * s, r=8)
    p.shape(f"M{x - 110 * s} {y - 170 * s} C{x - 150 * s} {y - 250 * s} {x - 90 * s} {y - 330 * s} {x - 30 * s} {y - 310 * s} "
            f"C{x - 10 * s} {y - 380 * s} {x + 90 * s} {y - 370 * s} {x + 90 * s} {y - 300 * s} C{x + 150 * s} {y - 290 * s} {x + 160 * s} {y - 190 * s} "
            f"{x + 100 * s} {y - 160 * s} C{x + 60 * s} {y - 130 * s} {x - 60 * s} {y - 130 * s} {x - 110 * s} {y - 170 * s} Z")


def fox():
    p = Pic()
    tree_bg(p, 140, 880, 0.9)
    p.sun(870, 140, 55)
    p.ground()
    p.grass(780, 885, 3, 50)
    # хвост
    tail = "M620 860 C780 890 920 780 900 630 C890 545 815 530 795 590 C835 680 770 790 620 800 Z"
    with p.group("translate(50 -20)"):
        p.shape(tail)
        with p.clipped(tail):
            p.ellipse(860, 590, 90, 75)
        p.shape(tail, fill="none")
    sit_body(p)
    # уши
    p.shape("M330 360 Q300 210 330 140 Q420 170 460 260 Z")
    p.shape("M694 360 Q724 210 694 140 Q604 170 564 260 Z")
    p.shape("M350 310 Q335 230 350 190 Q400 210 425 260 Z", sw=7)
    p.shape("M674 310 Q689 230 674 190 Q624 210 599 260 Z", sw=7)
    # голова
    head = ("M300 380 C300 260 400 220 512 220 C624 220 724 260 724 380 C724 430 765 455 745 480 "
            "C700 525 620 565 512 605 C404 565 324 525 279 480 C259 455 300 430 300 380 Z")
    p.shape(head)
    p.shape("M300 470 Q420 425 512 500 Q604 425 724 470 Q640 545 512 595 Q384 545 300 470 Z", sw=8)
    p.eye(440, 395, 40, lash=True, side=-1)
    p.eye(584, 395, 40, lash=True, side=1)
    p.nose(512, 515, 26, 17)
    p.cat_mouth(512, 532, 22)
    return p


def owl():
    p = Pic()
    # ночь: луна и звёзды
    p.shape("M820 110 C760 130 730 200 760 260 C790 320 860 330 910 300 C860 300 815 270 805 215 C795 165 800 135 820 110 Z")
    for x, y, r in ((150, 150, 34), (300, 90, 22), (680, 120, 24), (160, 360, 22), (900, 420, 26)):
        p.star(x, y, r)
    # ветка
    p.shape("M-10 790 Q300 760 600 780 Q800 790 1034 760 L1034 820 Q800 845 600 835 Q300 820 -10 845 Z")
    p.line("M800 800 Q860 740 900 700", sw=10)
    p.leaf(900, 700, -60, 90, 30)
    p.leaf(120, 805, 120, 90, 30)
    # ушки-пёрышки
    p.poly([(340, 280), (330, 150), (440, 230)])
    p.poly([(684, 280), (694, 150), (584, 230)])
    # крылья
    p.ellipse(320, 560, 70, 170, rot=15)
    p.ellipse(704, 560, 70, 170, rot=-15)
    # тело
    body = "M512 190 C680 190 740 330 740 500 C740 680 650 790 512 790 C374 790 284 680 284 500 C284 330 344 190 512 190 Z"
    p.shape(body)
    belly = "M512 470 C620 470 660 560 650 650 C640 740 590 780 512 780 C434 780 384 740 374 650 C364 560 404 470 512 470 Z"
    p.shape(belly)
    with p.clipped(belly):
        for row, y in enumerate((540, 610, 680, 750)):
            off = 0 if row % 2 == 0 else 35
            for x in range(360 + off, 680, 70):
                p.line(f"M{x - 30} {y} Q{x} {y + 30} {x + 30} {y}", sw=7)
    # глаза
    p.circle(430, 370, 88)
    p.circle(594, 370, 88)
    p.eye(435, 375, 52)
    p.eye(589, 375, 52)
    p.poly([(485, 430), (539, 430), (512, 480)])
    # лапки
    for x in (450, 574):
        p.shape(f"M{x - 40} 790 Q{x - 30} 830 {x - 20} 800 Q{x} 840 {x + 20} 800 Q{x + 30} 830 {x + 40} 790 Z", sw=8)
    return p


def hedgehog():
    p = Pic()
    p.cloud(180, 160, 0.6)
    p.sun(870, 150, 55)
    p.ground(512, 880, 400, 55)
    for x, y in ((150, 830), (860, 820)):
        p.leaf(x, y, -30, 90, 32)
    # иголки
    pts = []
    n = 26
    for i in range(n + 1):
        a = math.radians(180 + i * 180 / n)
        r = 1.0 if i % 2 == 0 else 1.18
        pts.append((560 + 270 * r * math.cos(a), 640 + 230 * r * math.sin(a)))
    pts.append((830, 760))
    pts.append((330, 760))
    p.poly(pts)
    # ножки
    for x in (400, 640):
        p.ellipse(x, 800, 55, 40)
    # мордочка
    p.shape("M470 540 C380 520 250 570 175 650 C150 680 175 715 225 715 C310 725 420 760 500 770 "
            "C560 700 540 600 470 540 Z")
    p.dot(178, 668, 18)
    p.eye(340, 610, 32)
    p.cheek(345, 680, 20)
    p.smile(255, 700, 24, 8, sw=8)
    # яблоко и грибок на спинке
    p.circle(600, 420, 70)
    p.line("M600 350 Q605 320 620 305", sw=8)
    p.leaf(615, 320, -20, 60, 22, sw=7)
    p.shape("M690 470 Q690 380 760 380 Q830 380 830 470 Q760 490 690 470 Z")
    p.rect(735, 470, 50, 60, r=10)
    p.circle(740, 425, 12, sw=6)
    p.circle(785, 430, 10, sw=6)
    return p


def honey_bear():
    p = Pic()
    scene_bg(p)
    # пчёлки
    for x, y in ((200, 230), (830, 300)):
        p.ellipse(x - 15, y - 35, 22, 28, rot=-20, sw=7)
        p.ellipse(x + 15, y - 35, 22, 28, rot=20, sw=7)
        body = f"M{x - 45} {y} C{x - 45} {y - 30} {x + 45} {y - 30} {x + 45} {y} C{x + 45} {y + 30} {x - 45} {y + 30} {x - 45} {y} Z"
        p.shape(body, sw=8)
        p.line(f"M{x - 12} {y - 25} L{x - 12} {y + 25}", sw=7)
        p.line(f"M{x + 12} {y - 25} L{x + 12} {y + 25}", sw=7)
        p.dot(x - 30, y - 5, 5)
    p.line("M250 240 Q320 280 300 330 Q280 360 330 360", sw=5)
    sit_body(p)
    # ушки
    p.circle(355, 270, 60)
    p.circle(669, 270, 60)
    p.circle(355, 270, 30, sw=8)
    p.circle(669, 270, 30, sw=8)
    p.ellipse(CX, 400, 205, 175)
    p.eye(440, 380, 40)
    p.eye(584, 380, 40)
    p.ellipse(CX, 480, 80, 58)
    p.nose(512, 460, 30, 19)
    p.cat_mouth(512, 478, 24)
    p.cheek(388, 465)
    p.cheek(636, 465)
    # бочонок мёда в лапках
    p.shape("M400 640 Q395 600 430 590 L594 590 Q629 600 624 640 Q650 760 600 840 L424 840 Q374 760 400 640 Z")
    p.shape("M410 640 Q450 610 470 650 Q490 690 510 640 Q530 600 560 650 Q580 690 614 640 L614 600 L410 600 Z", sw=8)
    p.rect(415, 575, 194, 30, r=12)
    p.ellipse(512, 740, 60, 40, sw=8)
    # лапки на бочонке
    p.ellipse(410, 700, 45, 55, rot=-20)
    p.ellipse(614, 700, 45, 55, rot=20)
    return p


# ================= ФЕРМА =================

def pony():
    p = Pic()
    fence(p, 600)
    p.sun(150, 140, 55)
    p.cloud(840, 160, 0.7)
    p.ground(560, 900, 420, 45)
    unicorn_side(p, 0, 20, with_horn=False, wing=0)
    return p


def rooster():
    p = Pic()
    fence(p, 640)
    p.sun(860, 150, 60)
    p.cloud(180, 170, 0.6)
    # хвост
    for i, (a, L) in enumerate(((-60, 230), (-30, 250), (0, 230), (30, 200))):
        r = math.radians(a)
        x0, y0 = 620, 520
        x1, y1 = x0 + L * math.cos(r), y0 + L * math.sin(r) - 60
        p.shape(f"M{x0} {y0} Q{f((x0 + x1) / 2 + 40)} {f((y0 + y1) / 2 - 60)} {f(x1)} {f(y1)} "
                f"Q{f((x0 + x1) / 2 - 10)} {f((y0 + y1) / 2 + 10)} {x0} {y0 + 40} Z")
    # ножки
    for x in (450, 540):
        p.line(f"M{x} 700 L{x} 790", sw=12)
        p.line(f"M{x} 790 L{x - 40} 815 M{x} 790 L{x} 822 M{x} 790 L{x + 35} 812", sw=10)
    # тело
    p.shape("M380 400 C300 470 320 650 460 710 C560 750 680 690 690 580 C700 500 640 460 560 470 C520 470 470 420 470 360 Z")
    p.shape("M480 560 C540 520 630 540 650 600 C600 650 520 650 480 560 Z")
    p.line("M520 590 Q570 600 610 590", sw=7)
    # голова
    p.shape("M340 230 Q330 160 380 170 Q390 120 430 140 Q460 110 480 160 Q520 170 490 230 Z")  # гребешок
    p.circle(420, 320, 100)
    p.poly([(325, 310), (255, 335), (325, 355)])
    p.shape("M335 370 Q320 430 350 440 Q380 430 365 380 Z", sw=8)
    p.eye(420, 305, 32)
    p.cheek(460, 360, 18)
    return p


def tractor():
    p = Pic()
    p.sun(150, 140, 55)
    p.cloud(820, 150, 0.7)
    p.hill(870, 30)
    # дымок
    p.circle(700, 230, 28)
    p.circle(760, 170, 38)
    p.circle(850, 120, 46)
    p.rect(660, 260, 40, 140, r=8)
    # кабина
    p.shape("M180 600 L180 260 Q180 230 210 230 L460 230 Q490 230 490 260 L490 600 Z")
    p.rect(220, 270, 230, 160, r=20)
    # капот
    p.rect(470, 400, 380, 220, r=30)
    for y in (460, 510, 560):
        p.line(f"M780 {y} L840 {y}", sw=8)
    p.rect(130, 580, 760, 60, r=20)
    # колёса
    p.circle(300, 700, 160)
    p.circle(300, 700, 90)
    p.circle(300, 700, 30)
    for a in range(0, 360, 30):
        r = math.radians(a)
        p.line(f"M{f(300 + 160 * math.cos(r))} {f(700 + 160 * math.sin(r))} L{f(300 + 180 * math.cos(r))} {f(700 + 180 * math.sin(r))}", sw=12)
    p.circle(720, 750, 100)
    p.circle(720, 750, 50)
    p.circle(720, 750, 16)
    # мордочка на капоте
    p.eye(560, 470, 30)
    p.eye(660, 470, 30)
    p.smile(610, 530, 34, 12)
    return p


# ================= МОРЕ =================

def seahorse():
    p = Pic()
    seabed(p)
    seaweed(p, 140, 880, 330)
    seaweed(p, 860, 880, 300, -1)
    bubbles(p, [(250, 230, 30), (300, 150, 20), (800, 200, 26)])
    # гребень
    for i, y in enumerate(range(300, 620, 55)):
        x = 600 + (y - 300) * 0.35 - (0 if y < 450 else (y - 450) * 0.5)
        p.poly([(x - 20, y - 20), (x + 45, y + 5), (x - 10, y + 35)], sw=8)
    # тело с хвостиком-завитком
    body = ("M560 300 C680 380 700 520 620 620 C570 690 600 760 640 790 C700 830 690 910 620 920 "
            "C540 930 480 870 500 800 C515 750 560 760 550 800 C545 830 580 845 600 830 "
            "C620 805 560 760 520 720 C440 640 420 520 450 420 Z")
    p.shape(body)
    with p.clipped(body):
        for y in (470, 520, 570, 620):
            p.line(f"M420 {y} Q480 {y + 15} 560 {y - 10}", sw=7)
    # голова и мордочка
    p.shape("M420 270 L280 255 Q255 280 280 305 L420 320 Z")
    p.circle(500, 270, 105)
    p.shape("M520 165 Q500 110 540 100 Q560 140 560 175 Z")
    p.eye(500, 255, 40)
    p.cheek(470, 320, 20)
    p.smile(285, 280, 10, 6, sw=7)
    return p


def submarine():
    p = Pic()
    waves(p, 150, 25, 5)
    bubbles(p, [(860, 380, 30), (900, 300, 20), (840, 240, 14)])
    seabed(p)
    seaweed(p, 130, 880, 260)
    shell(p, 820, 900, 0.8)
    # рыбки
    for x, y, s in ((200, 330, 1), (780, 760, -1)):
        p.shape(f"M{x - 60 * s} {y} Q{x} {y - 50} {x + 50 * s} {y} Q{x} {y + 50} {x - 60 * s} {y} Z", sw=8)
        p.poly([(x + 45 * s, y), (x + 90 * s, y - 30), (x + 90 * s, y + 30)], sw=8)
        p.dot(x - 30 * s, y - 8, 7)
    # перископ и рубка
    p.shape("M560 360 L560 210 L640 210 L640 250 L600 250 L600 360 Z")
    p.rect(450, 330, 220, 120, r=30)
    # корпус
    p.shape("M200 560 C200 450 320 420 520 420 C720 420 840 460 840 560 C840 660 720 700 520 700 C320 700 200 670 200 560 Z")
    for x in (360, 500, 640):
        p.circle(x, 550, 50)
        p.circle(x, 550, 32, sw=7)
    # винт
    p.rect(840, 535, 50, 50, r=10)
    p.ellipse(910, 500, 22, 50, rot=20)
    p.ellipse(910, 620, 22, 50, rot=-20)
    p.circle(900, 560, 18, sw=7)
    p.eye(260, 560, 28)
    p.smile(260, 610, 22, 8, sw=7)
    return p


def fish_family():
    p = Pic()
    seabed(p)
    seaweed(p, 120, 880, 300)
    seaweed(p, 900, 880, 260, -1)
    # морская звезда и коралл
    p.star(300, 900, 55, inner=0.55)
    p.shape("M700 880 L700 760 Q680 700 650 690 Q690 680 715 740 L720 640 Q740 680 735 740 Q770 690 800 700 Q760 720 745 790 L745 880 Z")
    bubbles(p, [(520, 160, 26), (580, 100, 16), (460, 110, 12)])

    def fishy(cx, cy, s, flip=1):
        with p.group(f"translate({cx} {cy}) scale({s * flip} {s})"):
            p.poly([(150, 0), (260, -90), (240, 0), (260, 90)])
            p.shape("M-50 -110 Q30 -200 110 -100 Z")
            body = "M-190 0 C-190 -110 -50 -150 60 -130 C150 -110 200 -50 200 0 C200 50 150 110 60 130 C-50 150 -190 110 -190 0 Z"
            p.shape(body)
            with p.clipped(body):
                for x in (0, 90):
                    p.shape(f"M{x} -200 Q{x + 40} 0 {x} 200 L{x + 40} 200 Q{x + 80} 0 {x + 40} -200 Z", sw=8)
            p.shape(body, fill="none")
            p.eye(-110, -20, 34)
            p.smile(-130, 50, 25, 8, sw=8)
            p.shape("M20 40 Q80 60 70 100 Q30 90 20 40 Z", sw=8)

    fishy(420, 470, 1.2, -1)
    fishy(700, 320, 0.7, -1)
    fishy(720, 610, 0.6, 1)
    return p


# ================= ДИНОЗАВРЫ =================

def stegosaurus():
    p = Pic()
    p.hill(880, 25)
    p.sun(140, 140, 55)
    p.cloud(840, 160, 0.7)
    volcano(p, 870, 600)
    # пластины
    for x, y, h in ((380, 470, 100), (470, 410, 130), (570, 400, 140), (670, 430, 120), (760, 490, 90)):
        p.poly([(x - 45, y + 60), (x - 40, y - h * 0.4), (x, y - h), (x + 40, y - h * 0.4), (x + 45, y + 60)])
    # хвост с шипами
    p.shape("M780 620 C860 620 920 660 960 720 C880 720 830 700 780 690 Z")
    p.poly([(900, 680), (930, 610), (945, 690)], sw=8)
    p.poly([(850, 660), (870, 590), (895, 670)], sw=8)
    # ноги
    for x in (380, 470, 650, 740):
        p.rect(x - 42, 650, 84, 210, r=30)
    # тело
    p.shape("M260 650 C260 500 420 440 570 450 C720 460 820 540 820 650 C820 720 720 730 560 730 C400 730 260 720 260 650 Z")
    # голова
    p.shape("M300 620 C250 600 170 590 130 620 C100 650 120 700 170 700 C230 705 290 690 320 680 Z")
    p.eye(185, 640, 26)
    p.smile(150, 680, 22, 6, sw=7)
    p.cheek(220, 680, 16)
    for x, y in ((480, 560), (600, 600), (700, 560)):
        p.circle(x, y, 22, sw=8)
    return p


def pterodactyl():
    p = Pic()
    # горы
    p.shape("M-10 880 L200 600 L330 760 L480 560 L700 880 Z")
    p.shape("M520 880 L760 620 L1034 880 Z")
    p.line("M150 670 L200 600 L250 670", sw=8)
    p.line("M430 630 L480 560 L530 630", sw=8)
    p.cloud(180, 180, 0.6)
    p.cloud(860, 420, 0.55)
    # крылья
    p.shape("M480 350 C380 260 220 200 80 220 C150 260 170 300 160 340 C220 330 260 360 280 400 C340 380 420 390 470 420 Z")
    p.shape("M560 350 C660 260 820 200 960 220 C890 260 870 300 880 340 C820 330 780 360 760 400 C700 380 620 390 570 420 Z")
    # тело
    p.ellipse(520, 400, 70, 110)
    p.ellipse(520, 420, 40, 70, sw=8)
    p.shape("M490 500 L470 560 L500 540 Z", sw=8)
    p.shape("M550 500 L570 560 L540 540 Z", sw=8)
    # голова с хохолком и клювом
    p.shape("M560 230 C620 180 700 170 760 150 C720 200 660 230 600 250 Z")
    p.shape("M480 260 C420 260 360 280 300 300 C360 310 430 310 490 300 Z")
    p.circle(520, 260, 70)
    p.eye(530, 250, 26)
    p.cheek(500, 295, 14)
    return p


def diplodocus():
    p = Pic()
    p.hill(880, 25)
    p.sun(860, 140, 55)
    palm(p, 140, 880, 330)
    p.grass(860, 890, 3, 50)
    # хвост
    p.shape("M760 640 C860 640 950 700 1010 800 C900 760 820 740 760 720 Z")
    # ноги
    for x in (480, 560, 700, 780):
        p.rect(x - 40, 660, 80, 210, r=30)
    # тело
    p.ellipse(630, 640, 210, 140)
    # шея и голова
    p.part("M450 600 C400 500 360 360 380 250 L470 250 C460 360 500 480 560 540 Z",
           "M440 585 C400 500 360 360 380 250 M470 250 C460 360 500 480 545 528")
    p.ellipse(400, 220, 105, 70, rot=-10)
    p.eye(380, 205, 26)
    p.smile(330, 245, 26, 8, sw=8)
    p.cheek(420, 250, 16)
    for x, y, r in ((620, 580, 30), (700, 640, 24), (560, 660, 20)):
        p.circle(x, y, r, sw=8)
    return p


# ================= ТРАНСПОРТ =================

def helicopter():
    p = Pic()
    p.cloud(180, 220, 0.7)
    p.cloud(840, 760, 0.8)
    p.sun(860, 160, 55)
    # винт
    p.rect(470, 210, 40, 70, r=8)
    p.rect(180, 180, 620, 40, r=20)
    # хвост
    p.shape("M640 470 L900 420 L900 470 L660 560 Z")
    p.ellipse(910, 410, 22, 70, sw=8)
    p.circle(910, 410, 14, sw=7)
    # полозья
    p.line("M330 650 L330 720 M600 650 L600 720", sw=10)
    p.rect(250, 710, 450, 30, r=15)
    # кабина
    p.shape("M240 480 C240 340 360 270 490 270 C620 270 700 360 700 480 C700 600 620 660 470 660 C330 660 240 600 240 480 Z")
    p.shape("M270 470 C270 370 350 320 440 320 L470 320 L470 480 Z")
    p.rect(510, 380, 130, 90, r=20)
    p.eye(380, 420, 30)
    p.smile(390, 470, 22, 8, sw=8)
    return p


def police_car():
    p = Pic()
    p.cloud(180, 160, 0.6)
    p.sun(860, 140, 55)
    road(p)
    # мигалка
    p.rect(450, 280, 120, 50, r=18)
    p.line("M510 280 L510 330", sw=8)
    p.line("M470 260 L455 225 M510 255 L510 215 M550 260 L565 225", sw=8)
    # кабина
    p.shape("M290 520 L370 340 Q385 320 410 320 L620 320 Q645 320 660 340 L750 520 Z")
    p.shape("M395 350 L500 350 L500 500 L330 500 Z")
    p.shape("M530 350 L630 350 L705 500 L530 500 Z")
    # кузов
    p.rect(110, 500, 800, 220, r=60)
    p.rect(110, 590, 800, 50, sw=8)
    p.star(512, 615, 40)
    p.ellipse(880, 550, 22, 32)
    p.rect(90, 560, 40, 40, r=8, sw=8)
    p.line("M560 540 L600 540", sw=8)
    wheel(p, 280, 730, 80)
    wheel(p, 740, 730, 80)
    return p


def air_balloon():
    p = Pic()
    p.cloud(170, 200, 0.7)
    p.cloud(850, 300, 0.6)
    p.cloud(260, 760, 0.6)
    p.sun(860, 120, 50)
    p.hill(900, 25)
    # корзина и стропы
    p.line("M410 640 L440 760 M614 640 L584 760", sw=8)
    p.rect(420, 750, 184, 120, r=16)
    p.line("M420 790 L604 790", sw=7)
    for x in (470, 512, 554):
        p.line(f"M{x} 790 L{x} 870", sw=6)
    # шар
    ball = "M512 120 C700 120 790 260 760 420 C740 520 640 600 600 650 L424 650 C384 600 284 520 264 420 C234 260 324 120 512 120 Z"
    p.shape(ball)
    with p.clipped(ball):
        p.line("M512 110 L512 660", sw=9)
        p.line("M512 110 C400 250 400 500 450 660", sw=9)
        p.line("M512 110 C624 250 624 500 574 660", sw=9)
        p.line("M512 110 C300 250 280 500 400 660", sw=9)
        p.line("M512 110 C724 250 744 500 624 660", sw=9)
        p.line("M200 380 Q512 440 824 380", sw=9)
    p.shape(ball, fill="none")
    p.rect(420, 640, 184, 30, r=10)
    return p


def excavator():
    p = Pic()
    p.sun(150, 140, 55)
    p.cloud(840, 150, 0.6)
    p.line("M-10 860 L1034 860")
    # кучка песка
    p.shape("M720 860 Q800 700 900 720 Q980 740 1000 860 Z")
    for x, y in ((820, 800), (900, 780), (950, 830)):
        p.dot(x, y, 6)
    # гусеница
    p.rect(150, 740, 480, 110, r=55)
    for x in (210, 330, 450, 570):
        p.circle(x, 795, 38)
        p.circle(x, 795, 12, sw=6)
    # кабина
    p.rect(200, 560, 380, 180, r=20)
    p.shape("M240 560 L240 380 Q240 360 260 360 L440 360 Q470 360 480 390 L520 560 Z")
    p.shape("M275 395 L430 395 L465 530 L275 530 Z")
    p.eye(330, 450, 26)
    p.eye(400, 450, 26)
    p.smile(365, 495, 26, 8, sw=8)
    p.rect(140, 600, 70, 100, r=14)
    # стрела и ковш
    p.shape("M560 600 L700 330 L760 350 L620 640 Z")
    p.shape("M700 330 L860 450 L830 490 L690 390 Z")
    p.shape("M820 460 L920 520 L900 640 Q840 640 800 600 Z")
    for x in (840, 870, 900):
        p.poly([(x - 12, 630), (x, 665), (x + 12, 630)], sw=7)
    return p


ANIMALS2 = [fox, owl, hedgehog, honey_bear]
FARM2 = [pony, rooster, tractor]
SEA2 = [seahorse, submarine, fish_family]
DINO2 = [stegosaurus, pterodactyl, diplodocus]
TRANSPORT2 = [helicopter, police_car, air_balloon, excavator]

GROUPS = {"animals": ANIMALS2, "farm": FARM2, "sea": SEA2, "dinosaurs": DINO2, "transport": TRANSPORT2}

if __name__ == "__main__":
    import sys, glob
    names = sys.argv[1:] or list(GROUPS)
    allp = []
    for name in names:
        start = {"animals": 7, "farm": 5, "sea": 6, "dinosaurs": 4, "transport": 6}[name]
        for i, fn in enumerate(GROUPS[name]):
            path = f"/home/claude/art/out/{name}/{start + i:03d}.png"
            render(fn(), path)
            allp.append(path)
    sheet(allp, "/home/claude/art/sheet_new1.png", cols=4, size=300)
    sheet(allp, "/home/claude/art/sheet_new1_regions.png", cols=4, size=300, regions=True)
