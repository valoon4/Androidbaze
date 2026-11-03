# M7.4.3 – Freiwillige Verknüpfung von Strategieatlas, eigenen Ideen und Erkenntnissen

**Stand:** 2026-11-02. **Status:** eigener Draft-PR, nicht gemergt. Ergänzt die bereits in main integrierten Etappen M7.4.1 (EKS1/EKB3/EKR2) und M7.4.2 (eigene Ideen).

## 1. Nutzen und Bedienkonzept

**Die Person entscheidet, keine automatische Empfehlung.** Der Verlauf kann an drei Stellen beginnen:

- **Rückblick → Ausprobieren → Strategieatlas ansehen.** Der Atlas wird mit sichtbarem Rückweg geöffnet. Erst nach dem Lesen von „Wann es passen könnte“, „Wichtige Grenze“ und möglichen Nachteilen kann man auf einer ausdrücklich zugelassenen Karte „Freiwillig zum Ausprobieren vormerken“ antippen. Dies öffnet den Entwurf im Bereich Ausprobieren, **speichert aber noch nichts**. Auch Abbrechen löscht lediglich den Entwurf.
- **Rückblick → Meine Erkenntnisse → Daraus freiwillig eine Idee entwickeln.** Im geöffneten Entwurf ist die ausgewählte Erkenntnis als möglicher Kontext sichtbar. Der Bezug bleibt **bis zur separaten Auswahl** ausdrücklich **nicht verknüpft**. Ein eigener Titel ist Pflicht, der Erkenntnistext wird nicht in die neue Idee kopiert.
- **Rückblick → Ausprobieren → gespeicherte Katalog-Idee → Strategie, Grenzen und Wissen ansehen.** Die gespeicherte Strategie-ID öffnet genau die entsprechende Karte. Deren bestehende redaktionelle Links zu Wissensartikeln, Bedürfnissen, Alternativen und Fallbeispielen bleiben verwendbar. Die Rücknavigation aus der Karte führt wieder zu „Ausprobieren“; ein ungültiger Katalogbezug wird nicht erraten.

## 2. Sicherheits- und Redaktionsentscheidung

Eine **feste explizite Freigabeliste mit 28 Karten** ist der aktuelle Ausgangspunkt. Die zwei reinen Erklärungskarten mit `familyIds` `patterns` (**„Wenn Vermeidung zum Muster wird“**, **„Gefühle verbergen oder zurückhalten“**) besitzen keinen „Vormerken“-CTA. Das Prüfkriterium schließt auch unbekannte zukünftige Karten ohne vorherige Freigabe aus. Eine Begrenzung/Warnung muss vor dem CTA erscheinen. Die Freigabeliste ist eine redaktionelle Einordnung der *Kartenart*, keine individuelle Sicherheits- oder Wirksamkeitsberatung; unabhängige Fachprüfung bleibt offen.

**Wichtig:** Auch zulässige Karten können in konkreten Situationen unpassend oder riskant sein (z. B. direkte Grenzsetzung bei Gewalt, Körperübungen bei Beschwerden). Deshalb bleiben Sicherheitswarnungen sichtbar; die App verspricht keinen Effekt und zwingt weder zum Handeln noch zu einer Einschätzung.

- Der neue Atlas-Entwurf hält die kuratierte ID und den Titel-Fallback fest, ohne freien Text aus der Karte als persönliche Aussage zu speichern.
- Erkenntnisse werden nur über ihre bestehenden stabilen UUIDs verknüpft; maximal 12 ID-Bezüge gemäß vorhandenem Datenmodell. Der Benutzer wählt die Bezüge ausdrücklich an/ab.
- Bei später gelöschten Erkenntnissen werden Links wie bereits in EKR2 koordiniert entfernt, eigene Experimente aber nicht gelöscht. Frühere Strategiekarten-IDs ohne aktuellen Treffer bleiben als eigene Idee mit einem deutlichen Hinweis erhalten.
- Verknüpfungsdialoge nutzen flüchtigen Compose-Zustand (`remember`, **kein** `rememberSaveable` für private ID-Auswahlen und Textentwürfe).
- Keine Speicherung durch Öffnen, Lesen, Wechseln von Artikeln, Auswählen einer Karte oder Abbrechen. Ausschließlich „Ausdrücklich speichern“ schreibt in den bereits verschlüsselten EKS1-Store; der Datensatz wird als Bestandteil einer EKB3-Sicherung erfasst.
- Bei ungültigem/ungeladenem Erkenntnisspeicher blockiert die UI die Bearbeitung statt still verlorene Links zu riskieren.
- Keine neue Android-Berechtigung, kein Cloud-Dienst, kein Tracking, keine automatische Verhaltensanalyse.

## 3. Manuelle Abnahme (nur fiktive Daten!)

1. Unter **Rückblick → Ausprobieren → Strategieatlas ansehen** den Atlas öffnen; mit „Zurück zu Ausprobieren“ zurückkehren. Ohne Vormerken darf **keine** Idee neu erscheinen.
2. Im Atlas „Kleine Schritte planen“ öffnen, **„Wann es passen könnte“** und **„Wichtige Grenze“** lesen und erst dann „Freiwillig zum Ausprobieren vormerken“ auswählen. Der Editor öffnet einen *nicht gespeicherten* Entwurf. Abbrechen/Verwerfen erzeugt keinen neuen Datensatz.
3. Die Karte erneut wählen, diesmal erst „Ausdrücklich speichern“. Die Idee muss eine Katalogquelle haben; der aktuelle Titel erscheint, ohne dass fremde Artikeltexte kopiert werden.
4. Die gespeicherte Idee öffnen, **„Strategie, Grenzen und Wissen ansehen“** wählen und eine passende Vertiefung aus der Wissenswelt lesen. Wieder zur Strategie und dann zu „Ausprobieren“ zurückkehren; Texte und Status der eigenen Idee bleiben unverändert.
5. Im Strategieatlas **„Wenn Vermeidung zum Muster wird“** und **„Gefühle verbergen oder zurückhalten“** öffnen. Keine der beiden Erklärungskarten darf die aktive Vormerken-Aktion zeigen.
6. In **Meine Erkenntnisse** eine fiktive Erkenntnis öffnen und auf **„Daraus freiwillig eine Idee entwickeln“** tippen. Im Entwurf zunächst keine Erkenntnisverknüpfung markieren und ausdrücklich speichern: Die Idee darf keinen Erkenntnis-Link bekommen.
7. Noch einmal versuchen, diesmal die gewünschte Erkenntnis ausdrücklich markieren. Nach Speichern kontrollieren, dass nur die ID verknüpft ist und der persönliche Erkenntnistext **nicht** in Plan, Titel oder Reflexion kopiert wurde.
8. Eine eigene Idee mit freiwilligen Verknüpfungen bearbeiten und einen Bezug lösen. Nach App-Neustart muss die Auswahl unverändert sein. Ein archivierter oder später gelöschter verknüpfter Erkenntnistext darf nicht als verwaister Link bestehen bleiben.
9. An einer künstlich importierten Idee mit **früherer nicht mehr existierender Strategie-ID**: klaren Hinweis auf fehlende Katalogkarte sehen, aber eigenen Plan und Reflexion weiterhin bearbeiten können; die App darf keine ähnliche Karte automatisch zuordnen.
10. Große Schrift (200 %), schmaler Bildschirm (320 dp), Querformat, TalkBack, Dark Mode und Android-Zurück ausprobieren. Keine wesentlichen Aktionen dürfen abgeschnitten sein.
11. Optional: EKB3 mit fiktiven verknüpften Ideen exportieren und auf einem **zweiten physischen Testgerät** importieren; UUIDs, Status, Texte und Links müssen unverändert bleiben. Ein physisches Zwei-Geräte-Backup ist **nicht** bereits durch Emulatorprüfungen erbracht.

## 4. Technische CI-Gates

- **26 Compose-UI-Tests** jeweils bei normaler und 200 % Schrift (= 52 Durchläufe) inkl. sieben neuer Fälle (geeigneter CTA, Erklärungskarten ohne CTA, Katalog-Rückweg, ungespeicherter Kartenentwurf, freiwillige Erkenntnis-Verknüpfung, fehlende Katalogkarte, Einladung von eigener Erkenntnis ohne automatisches Speichern).
- Bestehende 3 EKB2- und 8 EKB3-/AndroidKeyStore-Integrationsprüfungen bleiben Pflicht.
- Kotlin-/JVM-Tests, Testkompilierung und signierte Debug-APK müssen auf dem tatsächlichen finalen PR-HEAD grün sein.
- **Offen bis M7.4.4/M7.6:** echter TalkBack-/Gerätetest, unabhängige fachliche Prüfung aller Kartentexte und Sicherheits-Hinweise, Zwei-Geräte-Backups und Security-Review.

**Status:** Entwurf für Abnahme; keine automatische Merge-Freigabe.
