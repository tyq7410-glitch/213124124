import os
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'src', 'main', 'resources')
import os, zlib, struct, math
R = ROOT

def png(path, w, h, px):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    raw = b''.join(b'\x00' + bytes(c for p in px[y] for c in p) for y in range(h))
    def ch(t, d):
        return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    open(path, 'wb').write(b'\x89PNG\r\n\x1a\n' + ch(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)) + ch(b'IDAT', zlib.compress(raw, 9)) + ch(b'IEND', b''))

styles = {'angel': ((250, 250, 255), (185, 195, 235)), 'demon': ((70, 0, 6), (215, 25, 40)),
          'dead': ((14, 14, 18), (170, 16, 42)), 'neon': ((0, 255, 225), (175, 0, 255)),
          'ice': ((235, 250, 255), (120, 200, 255)), 'gold': ((255, 240, 170), (220, 150, 20)),
          'galaxy': ((20, 10, 60), (200, 60, 255))}
for n, (a, b) in styles.items():
    px = []
    for y in range(64):
        row = []
        for x in range(64):
            t = ((x - 1) % 16) / 15.0
            c = [a[i] + (b[i] - a[i]) * t for i in range(3)]
            k = 1 - 0.07 * ((x * 7 + y * 3) % 3)
            if y % 4 == 0:
                k *= 0.72
            if n == 'galaxy' and (x * 31 + y * 17) % 23 == 0:
                row.append((255, 255, 255, 255))
                continue
            row.append((int(c[0] * k), int(c[1] * k), int(c[2] * k), 255))
        px.append(row)
    png(f'{R}/assets/deadvisuals/textures/wings/{n}.png', 64, 64, px)

def blank():
    return [[(0, 0, 0, 0) for _ in range(16)] for _ in range(16)]

def put(g, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        g[y][x] = tuple(c) + (255,)

def outline(g):
    out = [row[:] for row in g]
    for y in range(16):
        for x in range(16):
            if g[y][x][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and g[ny][nx][3] > 0:
                        out[y][x] = (8, 6, 8, 255)
                        break
    return out

def disc(g, cx, cy, r, fn):
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            if d <= r:
                put(g, x, y, fn(d / r, x, y))

def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))

def sword(main, edge, guard, grip):
    g = blank()
    for t in range(5, 14):
        put(g, t, 15 - t, main)
        put(g, t + 1, 15 - t, edge)
    put(g, 14, 1, main)
    for t in range(0, 4):
        put(g, t, 15 - t, grip)
    for (x, y) in ((2, 9), (3, 10), (4, 11), (5, 12), (6, 13)):
        put(g, x, y, guard)
    return outline(g)

swords = {
    'wooden_sword': ((120, 70, 60), (80, 40, 40), (60, 30, 30), (40, 25, 25)),
    'stone_sword': ((110, 110, 118), (70, 70, 78), (50, 50, 60), (35, 28, 30)),
    'iron_sword': ((200, 200, 210), (130, 130, 145), (70, 60, 60), (45, 30, 30)),
    'golden_sword': ((230, 190, 60), (170, 130, 30), (100, 60, 30), (60, 35, 30)),
    'diamond_sword': ((80, 220, 230), (30, 140, 160), (40, 60, 70), (40, 25, 30)),
    'netherite_sword': ((35, 35, 42), (15, 15, 20), (170, 20, 44), (25, 18, 20)),
}
IT = f'{R}/resourcepacks/items/assets/minecraft/textures/item'
for n, cols in swords.items():
    png(f'{IT}/{n}.png', 16, 16, sword(*cols))

g = blank()
disc(g, 7.5, 7.5, 6, lambda t, x, y: lerp((35, 180, 160), (10, 50, 50), t))
for (x, y) in ((5, 5), (6, 5), (5, 6)):
    put(g, x, y, (210, 255, 240))
png(f'{IT}/ender_pearl.png', 16, 16, outline(g))

def apple():
    g = blank()
    disc(g, 7.5, 9, 5.5, lambda t, x, y: lerp((240, 200, 60), (160, 110, 20), t))
    for (x, y) in ((5, 7), (6, 7), (5, 8)):
        put(g, x, y, (255, 245, 170))
    put(g, 8, 3, (70, 40, 20)); put(g, 8, 2, (70, 40, 20))
    put(g, 9, 2, (30, 120, 40)); put(g, 10, 2, (30, 120, 40))
    return outline(g)
png(f'{IT}/golden_apple.png', 16, 16, apple())
png(f'{IT}/enchanted_golden_apple.png', 16, 16, apple())

g = blank()
disc(g, 7.5, 4.5, 3.4, lambda t, x, y: lerp((240, 200, 70), (170, 120, 30), t))
put(g, 6, 4, (20, 10, 12)); put(g, 9, 4, (20, 10, 12))
for y in range(8, 15):
    hw = 2 + (y - 8) * 0.55
    for x in range(16):
        d = abs(x - 7.5)
        if d <= hw:
            put(g, x, y, (240, 200, 70) if d > hw - 1 else (170, 20, 44))
png(f'{IT}/totem_of_undying.png', 16, 16, outline(g))

g = blank()
for y in range(1, 15):
    hw = int(round(6 - abs(y - 7.5) * 0.8))
    for x in range(16):
        if abs(x - 7.5) <= hw:
            put(g, x, y, lerp((190, 90, 240), (80, 20, 130), abs(x - 7.5) / 7.0))
disc(g, 7.5, 7.5, 2.2, lambda t, x, y: (210, 30, 70))
png(f'{IT}/end_crystal.png', 16, 16, outline(g))

pets = {'ghost': ((235, 240, 255), (20, 20, 30), (235, 240, 255)),
        'imp': ((190, 25, 40), (255, 220, 40), (110, 10, 20)),
        'cube': ((28, 28, 36), (0, 255, 230), (28, 28, 36)),
        'cat': ((18, 18, 22), (255, 40, 70), (18, 18, 22))}
for n, (base, eye, ear) in pets.items():
    px = []
    for y in range(32):
        row = []
        for x in range(32):
            k = 1 - 0.06 * ((x * 5 + y * 3) % 3)
            c = base
            if 12 <= y < 16:
                c = eye
            elif 16 <= y < 22 and x < 16:
                c = ear
            row.append((int(c[0] * k), int(c[1] * k), int(c[2] * k), 255))
        px.append(row)
    png(f'{R}/assets/deadvisuals/textures/pets/{n}.png', 32, 32, px)
print('assets ok')

import os, zlib, struct, math, json
R = ROOT

def png(path, w, h, px):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    raw = b''.join(b'\x00' + bytes(c for p in px[y] for c in p) for y in range(h))
    def ch(t, d):
        return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    open(path, 'wb').write(b'\x89PNG\r\n\x1a\n' + ch(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)) + ch(b'IDAT', zlib.compress(raw, 9)) + ch(b'IEND', b''))

def blank():
    return [[(0, 0, 0, 0) for _ in range(16)] for _ in range(16)]

def put(g, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        g[y][x] = tuple(c) + (255,)

def outline(g):
    out = [row[:] for row in g]
    for y in range(16):
        for x in range(16):
            if g[y][x][3] == 0:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    if 0 <= nx < 16 and 0 <= ny < 16 and g[ny][nx][3] > 0:
                        out[y][x] = (8, 6, 8, 255)
                        break
    return out

def disc(g, cx, cy, r, fn):
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - cx, y - cy)
            if d <= r:
                put(g, x, y, fn(d / r, x, y))

# ---- extra wing / cape textures ----
wing_styles = {'blood': ((60, 0, 10), (210, 20, 40)), 'sakura': ((255, 220, 235), (255, 140, 190)),
               'void': ((10, 5, 25), (150, 0, 255)), 'toxic': ((20, 60, 10), (130, 255, 60)),
               'fire': ((120, 20, 0), (255, 200, 40)), 'mono': ((230, 230, 235), (90, 90, 100))}
for n, (a, b) in wing_styles.items():
    px = []
    for y in range(64):
        row = []
        for x in range(64):
            t = ((x - 1) % 16) / 15.0
            c = [a[i] + (b[i] - a[i]) * t for i in range(3)]
            k = 1 - 0.07 * ((x * 7 + y * 3) % 3)
            if y % 4 == 0:
                k *= 0.72
            row.append((int(c[0] * k), int(c[1] * k), int(c[2] * k), 255))
        px.append(row)
    png(f'{R}/assets/deadvisuals/textures/wings/{n}.png', 64, 64, px)

# ---- extra pets ----
pets = {'fox': ((235, 120, 40), (30, 20, 20), (235, 120, 40)),
        'slime': ((80, 200, 80), (20, 60, 20), (80, 200, 80)),
        'pumpkin': ((230, 120, 20), (40, 20, 10), (60, 100, 30)),
        'frost': ((190, 235, 255), (30, 60, 120), (230, 250, 255)),
        'gold': ((255, 205, 70), (90, 50, 10), (255, 205, 70))}
for n, (base, eye, ear) in pets.items():
    px = []
    for y in range(32):
        row = []
        for x in range(32):
            k = 1 - 0.06 * ((x * 5 + y * 3) % 3)
            c = base
            if 12 <= y < 16:
                c = eye
            elif 16 <= y < 22 and x < 16:
                c = ear
            row.append((int(c[0] * k), int(c[1] * k), int(c[2] * k), 255))
        px.append(row)
    png(f'{R}/assets/deadvisuals/textures/pets/{n}.png', 32, 32, px)

# ---- weapons / tools ----
def scale(c, f):
    return tuple(max(0, min(255, int(v * f))) for v in c)

def handle(g, grip, t0, t1):
    for t in range(t0, t1):
        put(g, t, 15 - t, grip)

def sword(main, edge, guard, grip):
    g = blank()
    for t in range(5, 14):
        put(g, t, 15 - t, main)
        put(g, t + 1, 15 - t, edge)
    put(g, 14, 1, main)
    for t in range(0, 4):
        put(g, t, 15 - t, grip)
    for (x, y) in ((2, 9), (3, 10), (4, 11), (5, 12), (6, 13)):
        put(g, x, y, guard)
    return outline(g)

def pickaxe(main, edge, guard, grip):
    g = blank()
    handle(g, grip, 2, 11)
    for x, y in [(5, 5), (6, 4), (7, 3), (8, 2), (9, 2), (10, 2), (11, 2), (12, 3), (13, 4), (14, 5), (14, 6)]:
        put(g, x, y, main)
    for x, y in [(6, 5), (7, 4), (8, 3), (9, 3), (10, 3), (11, 3), (12, 4), (13, 5)]:
        put(g, x, y, edge)
    return outline(g)

def axe(main, edge, guard, grip):
    g = blank()
    handle(g, grip, 2, 11)
    rows = {2: (9, 12), 3: (8, 13), 4: (8, 13), 5: (9, 13), 6: (10, 12)}
    for y, (a, b) in rows.items():
        for x in range(a, b + 1):
            put(g, x, y, edge if x == a else main)
    return outline(g)

def shovel(main, edge, guard, grip):
    g = blank()
    handle(g, grip, 2, 11)
    disc(g, 11.5, 3.5, 2.8, lambda t, x, y: edge if t > 0.7 else main)
    return outline(g)

def hoe(main, edge, guard, grip):
    g = blank()
    handle(g, grip, 2, 11)
    for x in range(9, 14):
        put(g, x, 2, main)
    for x in (9, 10, 13):
        put(g, x, 3, edge)
    return outline(g)

TOOLS = {'sword': sword, 'pickaxe': pickaxe, 'axe': axe, 'shovel': shovel, 'hoe': hoe}
MATS = ['wooden', 'stone', 'iron', 'golden', 'diamond', 'netherite']
FACS = [0.5, 0.65, 0.8, 0.95, 1.1, 1.25]

def write_theme(dirpath, main, guard, only_tools=False):
    for mi, mat in enumerate(MATS):
        f = FACS[mi]
        m = scale(main, f)
        e = scale(main, f * 0.55)
        for name, fn in TOOLS.items():
            if only_tools and name == 'sword':
                continue
            png(f'{dirpath}/{mat}_{name}.png', 16, 16, fn(m, e, guard, (40, 25, 30)))

themes = {'neon': ((0, 255, 200), (255, 0, 180)), 'ice': ((140, 220, 255), (255, 255, 255)),
          'gold': ((255, 200, 60), (120, 70, 20)), 'galaxy': ((140, 80, 255), (255, 100, 220)),
          'blood': ((200, 20, 40), (60, 0, 10)), 'sakura': ((255, 160, 200), (255, 255, 255)),
          'void': ((90, 60, 140), (190, 0, 255)), 'toxic': ((120, 255, 60), (30, 80, 20)),
          'fire': ((255, 120, 20), (255, 230, 80)), 'mono': ((200, 200, 205), (60, 60, 70))}
for name, (main, guard) in themes.items():
    base = f'{R}/resourcepacks/weapons_{name}'
    os.makedirs(base, exist_ok=True)
    json.dump({'pack': {'pack_format': 46, 'description': f'Dead Visuals weapons: {name}'}},
              open(f'{base}/pack.mcmeta', 'w'))
    write_theme(f'{base}/assets/minecraft/textures/item', main, guard)

# default (Dead) pack also gets matching tools (swords already there)
write_theme(f'{R}/resourcepacks/items/assets/minecraft/textures/item', (150, 20, 45), (30, 30, 38), only_tools=True)
print('gen2 ok')

png(f'{R}/resourcepacks/crosshair/assets/minecraft/textures/gui/sprites/hud/crosshair.png', 15, 15,
    [[(0, 0, 0, 0) for _ in range(15)] for _ in range(15)])
print('all assets generated')
