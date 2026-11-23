# 🚲 JOSM D-A-CH Bicycle Plugin

Ein leistungsstarkes JOSM-Plugin zur Erstellung, Analyse und Validierung von Fahrrad-Routen im D-A-CH-Raum. Entwickelt für Verkehrsplaner, die ÖROK und die OSM-Community.

![Status](https://img.shields.io/badge/Status-Prototyp%20(MVP)-success)
![JOSM](https://img.shields.io/badge/JOSM-Plugin-blue)

Dieses Plugin vereint topologische Graphen-Berechnungen (A*-Routing) mit einem tiefgreifenden Profiler, der Fahrradinfrastruktur auf ihre echte Praxistauglichkeit für verschiedene Zielgruppen (Lastenräder, Rennräder, Familien) prüft.

---

## ✨ Features

### 1. Intelligenter Fahrrad-Profiler (Validator)
Der Profiler warnt nicht nur vor fehlenden Tags, sondern analysiert die reale Geometrie und Sicherheit der Route:

* 🚴 **Rennrad-Check:** Erkennt ungeeignete Oberflächen (`gravel`, `cobblestone`) und schlechte Ebenheit (`smoothness`).
* 🛒 **Lastenrad-Engstellen:** Findet Poller, Drängelgitter und Blockaden (`barrier=bollard/chicane`) mit einer physischen Durchfahrtsbreite von unter 1,30 m.
* 👨‍👩‍👧 **Familien- & Sicherheits-Check:** Schlägt Alarm, wenn die Route über Straßen mit Tempo 50 (oder mehr) führt, ohne dass es einen baulich getrennten Radweg gibt (Mischverkehr/Mehrzweckstreifen).
* ⛰️ **Pendler-Check:** Warnt vor anstrengenden Steigungen (`incline > 8%`).
* ⚠️ **Die "Todesweiche" (Straßenbahn-Kreuzungen):** Nutzt Vektormathematik, um den Kreuzungswinkel zwischen Radweg und Straßenbahnschienen (`railway=tram`) zu berechnen. Warnt bei extrem spitzen Winkeln (< 40°), bei denen akute Sturzgefahr durch Einfädeln besteht!

*(Füge hier deinen Screenshot vom Arne-Karlsson-Park ein!)*
> `![Todesweichen Profiler](pfad/zum/screenshot.png)`

### 2. A* Auto-Routing
Ein integrierter, lokaler Routing-Algorithmus, der den kürzesten und legalen Weg für Fahrräder über das bestehende OSM-Netzwerk findet.
* Bevorzugt Fahrradinfrastruktur (`cycleway`, `path`).
* Respektiert Einbahnstraßen, erkennt aber Ausnahmen für Radfahrer (`oneway:bicycle=no`).

---

## 🛠️ Installation (Für Entwickler & Tester)

Da sich das Plugin aktuell in der aktiven Entwicklung befindet, wird es lokal kompiliert und in JOSM eingebunden:

1. Repository klonen:
   ```bash
   git clone [https://github.com/Das-Wien-Team/josm-dach-bicycle.git](https://github.com/Das-Wien-Team/josm-dach-bicycle.git)