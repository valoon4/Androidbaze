# M3.1 – Geführte Suche und flexible Check-in-Tiefe

Version: **0.3.1-m3.1** · Android `versionCode=6` · Stand: 29.10.2026.

## Ziel

Der bisherige M3-Navigator verlangte direkt die Auseinandersetzung mit 289 Atlasbegriffen. M3.1 macht diese Auswahl leichter und lässt Nutzer entscheiden, ob sie kurz innehalten oder ausführlicher erkunden möchten. Das System vermeidet automatische psychologische Deutungen.

## Verbesserungen

### 1. Zwei Check-in-Tiefen

- **Kurz**: Start unmittelbar bei der Gefühlsbenennung; danach freiwillige Intensität je gewähltem Begriff und neutrale Zusammenfassung. Keine Körper- oder Kontextfragen.
- **Vertieft**: Körperwahrnehmung → Gefühlsbenennung → Intensität und optionaler Lebensbereich/Freitext → Zusammenfassung.
- Umschalten ist während des Check-ins möglich; gewählte Begriffe und Intensitäten bleiben erhalten, wenn man zwischen Modi wechselt.
- Beide Modi erlauben auch **null ausgewählte Gefühle**; ein Eintrag mit Intensität `0` bleibt von *nicht angegeben* (`null`) unterscheidbar.
- Schrittzähler berücksichtigt den gewählten Modus, Zurück/Beenden funktioniert auch direkt vom ersten Kurzmodus-Schritt.

### 2. Niedrigschwellige Orientierung ohne Diagnose

- Die App zeigt **nicht mehr ungefragt 289 Begriffe**.
- Sie bietet sieben bewusst grobe, verständliche Richtungen: *Angespannt, Traurig, Ärgerlich, Unsicher, Angenehm, Verbunden* und *Schwer zu sagen*.
- Jede Richtung bietet eine kleine **offline kuratierte** Wortliste (maximal acht Vorschläge).
- Alle 289 Begriffe bleiben über die bestehende Suche und die ausdrücklich wählbare Gesamtansicht verfügbar. Familienfilter greifen weiterhin.
- Es gibt keine automatischen Diagnosen, kein Auslesen von Freitext, keine Prognosen und keine versteckte Emotionserkennung.

### 3. Ausgewählte Gefühle bleiben sichtbar

- Während der Wortauswahl ist die aktuelle Auswahl **oberhalb der scrollbaren Ergebnisliste** zu sehen.
- Jeder Begriff kann dort direkt durch Antippen entfernt werden.
- Bei der Auswahl bleiben mehrere Emotionen gleichzeitig erlaubt und die App verlangt keine einzelne „Hauptemotion“.

### 4. Redaktionell geprüfte *Vergleichstexte* nutzen (ohne externe Fachvalidierung)

- Der bestehende Atlas enthält 41 redaktionell verfasste Vergleiche.
- Ist eine kuratierte Verbindung zur aktuellen Auswahl vorhanden, werden höchstens zwei Paare im Check-in mit gemeinsamem Merkmal und Unterschieden angezeigt.
- Wenn kein gepflegter Vergleich existiert, **erfindet** die App keine Abgrenzung.
- Die Texte sind redaktionelle Arbeitsfassung und ausdrücklich **nicht durch externe psychologische Fachpersonen klinisch validiert**.

## Architektur

`core/checkin/FeelingDiscovery.kt` hält die rein lokale, deterministische Vorschlagslogik. `CheckInDraft` speichert zusätzlich den Modus im flüchtigen Arbeitsspeicher. Suchfilter und Richtung sind absichtlich `remember`-Zustände **ohne dauerhafte Android-Speicherung**.

Es gibt weiterhin keine Journal-Datenbank, Cloud, Diagnosefunktion oder Analytics. Check-in-Eingaben werden beim Beenden und bei Navigation außerhalb des Check-ins durch das ViewModel verworfen. Nur die Onboarding-Präferenz ist persistent.

## Test- und Geräteabnahme

Automatisiert:
1. Ohne ausdrückliche Richtung oder Suchwort kommen keine scheinbar „erkannten“ Emotionen.
2. Alle Richtungen zeigen vorhandene ID-Referenzen, niemals mehr als acht Wörter.
3. Eine Suche überstimmt die Vorschlagsrichtung; Familienfilter bleiben aktiv.
4. Direkte Vergleichspaare kommen nur aus kuratierten Inhalten, und ohne Auswahl gibt es keine Paare.
5. Kurzmodus zeigt drei statt vier Schritte und überspringt den Körpereinstieg.
6. Wechsel zwischen Modi behält Gefühle und `0`-Intensitäten.
7. Android-Zurück schließt den Kurzmodus vom ersten Schritt und verwirft persönliche Werte.
8. Alte M3-Datenschutz- und 289-Atlas-Tests bleiben grün.

Manuell auf Android:
- Start → Kurz → Richtung „Angespannt“ → „Nervosität“ und „Besorgnis“ wählen → Intensitäten → Abschluss.
- Erneut starten → Vertieft → körperliche Eindrücke → „Schwer zu sagen“ wählen.
- Lange Liste explizit öffnen, suchen, Auswahl oben sichtbar halten und entfernen.
- „Neid“ + „Eifersucht“ über Suche wählen und kuratierten Vergleich nachvollziehen.
- Im aktiven Check-in von Kurz nach Vertieft wechseln: beide Begriffe bleiben.
- Vor Abschluss verlassen: neue Sitzung hat keine alten Check-in-Angaben.
- Große Android-Schrift, TalkBack, Dark Mode und Flugmodus prüfen.

## Was bewusst in M3.2 bleibt

Auslöser systematisch unterscheiden, Bedürfnisse und Grenzen behutsam erkunden, freiwillige Erkenntnis/Handlungsideen am Ende, empathische Fragenführung. Kein Speicherung persönlicher Eingaben vor M4 und keine erzwungene emotionale Interpretation.
