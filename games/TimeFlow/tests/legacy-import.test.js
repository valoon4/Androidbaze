'use strict';
const assert=require('node:assert/strict');
const I=require('../app/src/main/assets/legacy-import.js');
const csv='\ufeff"Projekt";"Aufgabe";"Session-Notiz";"Start";"Ende";"Arbeitszeit (Sek.)";"Arbeitszeit (HH:MM:SS)";"Status"\r\n'+
  '"Client; A";"Implementierung";"Notiz mit ""Anführungszeichen""";"12.10.2026, 09:00";"12.10.2026, 12:00";"9000";"02:30:00";"Abgeschlossen"\r\n'+
  '"Client; A";"Implementierung";"Nachtschicht";"12.10.2026, 23:00";"13.10.2026, 01:30";"9000";"02:30:00";"Abgeschlossen"\r\n';
let parsed=I.parse(csv);
assert.equal(parsed.records.length,2);
assert.equal(parsed.records[0].project,'Client; A');
assert.equal(parsed.records[0].note,'Notiz mit "Anführungszeichen"');
assert.equal(parsed.records[0].pauseMs,1800000);
assert.equal(parsed.records[1].workMs,9000000);
assert.equal(parsed.skipped,0);
assert.throws(()=>I.parse('wrong;format\nx;y'));
assert.equal(I.parseDate('12.13.2026, 09:00'),null);
console.log('PASS: CSV migration parser and old TimeFlow formats');
