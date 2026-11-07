# Datenabgleich Kabelsalat 1.1 – HTML-Referenzen

**Eingangsdaten (vom Benutzer am 07.11.2026 bereitgestellt)**

1. `13_T_021_Kabeleditor_2026-11-05_09-05.html` – eingebettete `initial-state`- und `model-data`-JSON.
2. `13_U_028_Kabeleditor_2026-11-05_08-19.html` – dieselben eingebetteten Datentypen, zusätzliche Planmarker und grafische Kurven.

## Übernahme

| Quellenmerkmal | 13_T_021 | 13_U_028 |
|---|---:|---:|
| Kabel im gesicherten Zustand | 16 | 18 |
| Physisch als Öffnung gezeichnete Elemente | 48 | 78 |
| Ausschließlich schematische Planmarker | 0 | 15 |
| Kabelenden ohne bestätigte sichtbare Öffnung | 0 | 7 |
| Modellabmessungen in beiden HTML-Vorlagen | 2750 × 2350 × 1850 mm | 2750 × 2350 × 1850 mm |
| Quelle im HTML | Schachtbelegung, S. 1420–1421 | KD-001 j, Stand 03.07.2026, S. 1504–1507 |

Beide Referenzen werden **getrennt** und zusätzlich als unverknüpftes Vergleichsnetz angeboten. Die Koordinaten der Netzkarte im Vergleich sind frei gewählte Bildschirmabstände, **keine Vermessungspositionen**. Es wurden keinerlei Verbindungen erfunden.

### Bewusst nicht als fachlich bestätigt übernommen

- Die beiden Quellmodelle sind **schematische 3D-Visualisierungen** und keine bestätigten Bestandspläne.
- DN-Angaben sind nicht verbindlich bekannt. Der Editor verwendet den grafischen Lochradius (× 2) als **Darstellungsdurchmesser**, ausdrücklich nicht als geprüfte nominale DN. Auch die Kabeldurchmesser sind lediglich Visualisierungswerte.
- Im U-Schacht liegen 15 Planmarker ohne bestätigte Zuordnung zu den sichtbaren Öffnungen. Sie werden **nicht** als physische Öffnungen erstellt. Sieben Kabelanschlüsse bleiben daher im geometrischen Modell bewusst offen. Ihre originalen Planadressen bleiben in den Kabel-Quelldaten erhalten.
- Die vereinfacht intern unterstützten Gruppen OLA/MS/Weitere ersetzen nicht die Systembegriffe der Quellen. Ursprüngliche Gruppen und Kennungen bleiben an jedem Kabel erhalten.
- Originale Darstellungsbögen werden, wo vorhanden, anhand ausgewählter Stützpunkte **näherungsweise** übernommen. Das beweist weder reale Leitungswege noch Mindestbiegeradien.
- Das Material/der Aufbau von Einhausungen kann nicht allein aus den zwei HTML-Vorlagen fachlich freigegeben werden.
- Bei U_028 ist in den verarbeiteten Quelldaten keine Wandstärke angegeben. Die Software nutzt 120 mm nur als Darstellungs-Standardwert.

### Datenprovenienz und Tests

Die Namen, Original-Kabel-IDs, Quellanschluss-IDs, Planadressen, Seitenzahlen und Statusbemerkungen überleben JSON-Speichern/Laden und Schachtwechsel. Tests in `tests/references.test.js` prüfen zusätzlich Anzahl, den plan-only-Status, fehlende Kabelenden, keine erfundene Netz-Verbindung und datentreue JSON-Fixtures.

**Status:** Interaktive technische Visualisierung / Datenvorbereitung; keine vermessungs-, bau-, elektro-, brandschutz- oder eisenbahntechnische Ausführungsfreigabe.
