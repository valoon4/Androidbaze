# Kabelsalat · alle offenen Tasks atomar zerlegt

**Vollständig:** 869 von 869 offenen Parent-Tickets auf 13012 einzeln abhackbarer Mikroschritte (2–10 Minuten Zielaufwand) verteilt. **0 abgeschlossen**, nur mit Nachweis.
**Kanonischer Gesamtstand:** 888 Parent-Tickets, 19 erledigt, 869 offen. Hier sind **keine abgeschlossenen Eltern-Tickets** nochmals aufgeführt.

| Priorität | Offene Parents | Mikro-Tasks | Nachschlagen | CSV |
|---|---:|---:|---|---|
| S0 | 92 | 1351 | [S0 Mikroaufgaben](./ATOMIC/S0.md) | [S0 CSV](./ATOMIC/S0.csv) |
| S1 | 125 | 1974 | [S1 Mikroaufgaben](./ATOMIC/S1.md) | [S1 CSV](./ATOMIC/S1.csv) |
| S2 | 166 | 2583 | [S2 Mikroaufgaben](./ATOMIC/S2.md) | [S2 CSV](./ATOMIC/S2.csv) |
| S3 | 108 | 1667 | [S3 Mikroaufgaben](./ATOMIC/S3.md) | [S3 CSV](./ATOMIC/S3.csv) |
| S4 | 162 | 2504 | [S4 Mikroaufgaben](./ATOMIC/S4.md) | [S4 CSV](./ATOMIC/S4.csv) |
| S5 | 54 | 862 | [S5 Mikroaufgaben](./ATOMIC/S5.md) | [S5 CSV](./ATOMIC/S5.csv) |
| S6 | 162 | 2071 | [S6 Mikroaufgaben](./ATOMIC/S6.md) | [S6 CSV](./ATOMIC/S6.csv) |
| **Gesamt** | **869** | **13012** | | |

## Verbindliche Arbeitsregel

- **ID-Schema:** KAB-XXXX bleibt das Feature-/Prüf-Ticket. KAB-XXXX/M01 bis Mnn sind die atomaren Schritte, ohne Parent-IDs zu ersetzen oder bei der 888er-Zählung mitzurechnen.
- **Eine einzelne Handlung + überprüfbares Fertig-Kriterium**; 2–10 Minuten sind eine Planungs-Zielgröße für meine aktive Arbeit, keine Zusage. Wenn der Schritt real größer wird, noch vor Umsetzung weiter teilen.
- **Schrittstatus:** Offene/erledigte Schritte werden allein aus [ATOMIC/COMPLETED.csv](./ATOMIC/COMPLETED.csv) übernommen. Einträge nur mit Datum und Test-/Commitbeleg; direktes Ändern generierter Checkboxen wird beim Neuaufbau überschrieben.
- **Externes:** Warten auf Bro, originale PDFs/XLSX, Hersteller oder Brandschutz-Fachentscheidung ist keine 5-Minuten-Arbeit. Solche Punkte bleiben blockiert, bis Daten vorliegen.
- **Sicherheit:** Keine echten Durchmesser aus unvermessenen Bildern erfinden; DN, Innen-, Außen- und Lochdurchmesser getrennt halten. Demo verändert keine bestätigten Rohrmittelpunkte.
- **Mikro-Aufwände nicht summieren, um Liefertermine zu versprechen.** Die alte grobe Parent-Schätzung wurde nicht proportional verteilt; Integration/Unsicherheit muss nach Durchführung real neu bewertet werden.

## Aktualisieren / Prüfen

Aus PC/Kabelsalat: node TASKS/generate-atomic.js --write (regenerieren); node TASKS/generate-atomic.js --check (Vollständigkeit, Einzigartigkeit, keine erledigten Parent-IDs, Maximaldauer und Git-Abgleich).

Vorheriger 42-Schritte-Pilot unverändert archiviert: [Originalpilot](./ATOMIC/PILOT_ORIGINAL.md). Seine IDs sind in S0 enthalten; KAB-0881 und KAB-0882 wurden um die vollständigen PDF-/Foto-Teilpfade ergänzt.
