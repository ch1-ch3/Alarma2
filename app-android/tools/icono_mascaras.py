"""Dibuja el ícono de Complots: dos máscaras de teatro (alegre adelante, triste atrás), sin cinta.
Genera app/src/main/res/drawable/ic_complot.xml y una vista previa PNG (argumento opcional).
Uso: python tools/icono_mascaras.py [vista_previa.png]
"""
import math, os, sys
from shapely.geometry import Polygon
from shapely import affinity
from shapely.ops import unary_union

def bez(p0, p1, p2, p3, n=24):
    out = []
    for i in range(n + 1):
        t = i / n; u = 1 - t
        out.append((u**3*p0[0] + 3*u*u*t*p1[0] + 3*u*t*t*p2[0] + t**3*p3[0],
                    u**3*p0[1] + 3*u*u*t*p1[1] + 3*u*t*t*p2[1] + t**3*p3[1]))
    return out

def quad(p0, c, p1, n=16):
    return bez(p0, (p0[0] + 2/3*(c[0]-p0[0]), p0[1] + 2/3*(c[1]-p0[1])),
               (p1[0] + 2/3*(c[0]-p1[0]), p1[1] + 2/3*(c[1]-p1[1])), p1, n)

def cara():
    # Contorno: frente ancha con cejas marcadas, mejillas redondas, mentón suave.
    pts = []
    pts += quad((-5.6, -5.2), (-2.8, -7.4), (0, -5.9))        # frente izq
    pts += quad((0, -5.9), (2.8, -7.4), (5.6, -5.2))[1:]      # frente der
    pts += bez((5.6, -5.2), (6.2, -0.5), (4.2, 6.2), (0, 6.6))[1:]
    pts += bez((0, 6.6), (-4.2, 6.2), (-6.2, -0.5), (-5.6, -5.2))[1:]
    return Polygon(pts)

def agujero(a, b):
    return Polygon(a + b[::-1][1:-1])

def mascara(alegre):
    m = cara()
    if alegre:
        ojos = [agujero(quad((-4.1, -1.6), (-2.6, -4.6), (-1.1, -1.6)), quad((-4.1, -1.6), (-2.6, -2.9), (-1.1, -1.6))),
                agujero(quad((1.1, -1.6), (2.6, -4.6), (4.1, -1.6)), quad((1.1, -1.6), (2.6, -2.9), (4.1, -1.6)))]
        boca = agujero(quad((-3.4, 1.0), (0, 2.4), (3.4, 1.0)), quad((-3.4, 1.0), (0, 7.2), (3.4, 1.0)))
    else:
        ojos = [agujero(quad((-4.1, -3.2), (-2.6, -0.6), (-1.1, -2.2)), quad((-4.1, -3.2), (-2.6, -2.0), (-1.1, -2.2))),
                agujero(quad((1.1, -2.2), (2.6, -0.6), (4.1, -3.2)), quad((1.1, -2.2), (2.6, -2.0), (4.1, -3.2)))]
        boca = agujero(quad((-2.6, 3.9), (0, -0.7), (2.6, 3.9)), quad((-2.6, 3.9), (0, 1.8), (2.6, 3.9)))
    for h in ojos + [boca]:
        m = m.difference(h)
    return m

def poner(m, giro, x, y):
    return affinity.translate(affinity.rotate(affinity.scale(m, 0.88, 0.88, origin=(0, 0)), giro, origin=(0, 0)), x, y)

adelante = poner(mascara(True), -14, 7.5, 13.4)
atras = poner(mascara(False), 18, 16.7, 10.0)
# La de atrás se recorta con un margen alrededor de la de adelante, para que se distingan.
atras = atras.difference(Polygon(adelante.exterior).buffer(0.85, join_style=1))
todo = unary_union([adelante, atras])

def anillo(c):
    pts = list(c.coords)
    return 'M' + ' L'.join(f'{x:.2f},{y:.2f}' for x, y in pts[:-1]) + 'Z'

geoms = getattr(todo, 'geoms', [todo])
d = ''
for g in geoms:
    d += anillo(g.exterior) + ''.join(anillo(i) for i in g.interiors)

raiz = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
xml = f'''<?xml version="1.0" encoding="utf-8"?>
<!-- Ícono propio de Complots: máscara alegre y triste. Generado por tools/icono_mascaras.py -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF" android:fillType="evenOdd"
        android:pathData="{d}"/>
</vector>
'''
open(os.path.join(raiz, 'app/src/main/res/drawable/ic_complot.xml'), 'w', encoding='utf-8').write(xml)
print('minx,miny,maxx,maxy =', [round(v, 2) for v in todo.bounds])

if len(sys.argv) > 1:
    from PIL import Image, ImageDraw
    k = 20
    img = Image.new('RGB', (24 * k, 24 * k), (244, 183, 49))
    dr = ImageDraw.Draw(img)
    for g in geoms:
        dr.polygon([(x * k, y * k) for x, y in g.exterior.coords], fill=(35, 22, 14))
        for i in g.interiors:
            dr.polygon([(x * k, y * k) for x, y in i.coords], fill=(244, 183, 49))
    img.save(sys.argv[1])
