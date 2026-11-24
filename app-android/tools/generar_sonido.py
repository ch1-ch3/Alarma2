"""Genera el sonido de reserva de la app (tono sintetizado, sin derechos de terceros).

Uso: python generar_sonido.py <salida.wav>
Campanita de notas que suben (La2, Do#3, Mi3, La3) y repiten las tres primeras más cortas, tonos graves
y suaves con caída natural. ~2,2 s, mono, 44,1 kHz, 16 bits.
Versión 2: la primera (bips de 880-3136 Hz) era demasiado aguda, según el usuario.
"""
import math, struct, sys, wave

RATE = 44100

def nota(freq, dur, vol=0.8):
    n = int(RATE * dur)
    out = []
    for i in range(n):
        t = i / RATE
        ataque = min(1.0, i / (0.01 * RATE))
        caida = math.exp(-3.0 * t / dur)
        final = min(1.0, (n - i) / (0.03 * RATE))
        s = math.sin(2 * math.pi * freq * t) + 0.25 * math.sin(2 * math.pi * 2 * freq * t)
        out.append(vol * ataque * caida * final * s / 1.25)
    return out

muestras = []
for f in (220.0, 277.2, 329.6):
    muestras += nota(f, 0.35)
muestras += nota(440.0, 0.6)
for f in (220.0, 277.2, 329.6):
    muestras += nota(f, 0.25)

with wave.open(sys.argv[1], "wb") as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(RATE)
    w.writeframes(b"".join(struct.pack("<h", int(max(-1, min(1, m)) * 32767)) for m in muestras))
