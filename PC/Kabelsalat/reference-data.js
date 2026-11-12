/* Kabelsalat 1.1 – selected source data extracted from two user-supplied standalone HTML editors.
   This is NOT a confirmed survey, nominal-DN schedule, physical cable-pairing certification, or build plan.
   Source files: 13_T_021_Kabeleditor_2026-11-05_09-05.html,
                 13_U_028_Kabeleditor_2026-11-05_08-19.html.
   The original 3D mesh/base64 screenshot content is intentionally not republished.
*/
(function(root,factory){
 const api=factory(root?.KabelsalatComponents||null,root?.KabelsalatNetwork||null);
 if(typeof module==='object'&&module.exports)module.exports={factory};
 if(root)root.KabelsalatReferences=api;
})(typeof window!=='undefined'?window:null,function(components,network){
'use strict';
const RECORDS={"13_T_021":{"name":"13_T_021","refFile":"13_T_021_Kabeleditor_2026-11-05_09-05.html","savedAt":"2026-11-05T09:05:27.170Z","dims":[2.75,2.35,1.85,0.13],"ports":[{"id":"SW1_T1_LR1","wall":1,"pos":[-1.375,0.05,0.75],"radius":0.05,"row":1,"col":null,"dn":null,"sourceAddress":"SW1/T1/LR1","sourcePage":1420,"sourceCount":1,"category":"LST","kind":""},{"id":"SW1_T1_LR2","wall":1,"pos":[-1.375,0.05,0.25],"radius":0.05,"row":1,"col":null,"dn":null,"sourceAddress":"SW1/T1/LR2","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW1_T1_LR3","wall":1,"pos":[-1.375,0.05,-0.25],"radius":0.05,"row":1,"col":null,"dn":null,"sourceAddress":"SW1/T1/LR3","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW1_T1_LR4","wall":1,"pos":[-1.375,0.05,-0.75],"radius":0.05,"row":1,"col":null,"dn":null,"sourceAddress":"SW1/T1/LR4","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR1","wall":2,"pos":[-1.03,0.6,-1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR1","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR2","wall":2,"pos":[-0.62,0.6,-1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR2","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR3","wall":2,"pos":[-0.21,0.6,-1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR3","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR4","wall":2,"pos":[0.2,0.6,-1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR4","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR5","wall":2,"pos":[0.61,0.6,-1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR5","sourcePage":1420,"sourceCount":5,"category":"LST/LWL","kind":""},{"id":"SW2_T1_LR6","wall":2,"pos":[1.02,0.6,-1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR6","sourcePage":1420,"sourceCount":1,"category":"Weitere","kind":""},{"id":"SW2_T1_LR7","wall":2,"pos":[-1.03,0.25,-1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR7","sourcePage":1420,"sourceCount":3,"category":"LWL","kind":""},{"id":"SW2_T1_LR8","wall":2,"pos":[-0.62,0.25,-1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR8","sourcePage":1420,"sourceCount":1,"category":"TK","kind":""},{"id":"SW2_T1_LR9","wall":2,"pos":[-0.21,0.25,-1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR9","sourcePage":1420,"sourceCount":1,"category":"Weitere","kind":""},{"id":"SW2_T1_LR10","wall":2,"pos":[0.2,0.25,-1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR10","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR11","wall":2,"pos":[0.61,0.25,-1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR11","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR12","wall":2,"pos":[1.02,0.25,-1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR12","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR13","wall":2,"pos":[-1.03,-0.26,-1.175],"radius":0.061,"row":3,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR13","sourcePage":1420,"sourceCount":1,"category":"MS","kind":""},{"id":"SW2_T1_LR14","wall":2,"pos":[-0.62,-0.26,-1.175],"radius":0.061,"row":3,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR14","sourcePage":1420,"sourceCount":1,"category":"MS","kind":""},{"id":"SW2_T1_LR15","wall":2,"pos":[0.2,-0.26,-1.175],"radius":0.061,"row":3,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR15","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR16","wall":2,"pos":[0.61,-0.26,-1.175],"radius":0.061,"row":3,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR16","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR17","wall":2,"pos":[1.02,-0.26,-1.175],"radius":0.061,"row":3,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR17","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW2_T1_LR18","wall":2,"pos":[0.2,-0.65,-1.175],"radius":0.061,"row":4,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR18","sourcePage":1420,"sourceCount":1,"category":"OLA","kind":""},{"id":"SW2_T1_LR19","wall":2,"pos":[0.61,-0.65,-1.175],"radius":0.061,"row":4,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR19","sourcePage":1420,"sourceCount":1,"category":"OLA","kind":""},{"id":"SW2_T1_LR20","wall":2,"pos":[1.02,-0.65,-1.175],"radius":0.061,"row":4,"col":null,"dn":null,"sourceAddress":"SW2/T1/LR20","sourcePage":1420,"sourceCount":1,"category":"OLA","kind":""},{"id":"SW3_T1_LR1","wall":3,"pos":[1.375,0.05,0.75],"radius":0.05,"row":1,"col":null,"dn":null,"sourceAddress":"SW3/T1/LR1","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW3_T1_LR2","wall":3,"pos":[1.375,0.05,0.25],"radius":0.05,"row":1,"col":null,"dn":null,"sourceAddress":"SW3/T1/LR2","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW3_T1_LR3","wall":3,"pos":[1.375,0.05,-0.25],"radius":0.05,"row":1,"col":null,"dn":null,"sourceAddress":"SW3/T1/LR3","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW3_T1_LR4","wall":3,"pos":[1.375,0.05,-0.75],"radius":0.05,"row":1,"col":null,"dn":null,"sourceAddress":"SW3/T1/LR4","sourcePage":1420,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR1","wall":4,"pos":[-1.03,0.6,1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR1","sourcePage":1421,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR2","wall":4,"pos":[-0.62,0.6,1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR2","sourcePage":1421,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR3","wall":4,"pos":[-0.21,0.6,1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR3","sourcePage":1421,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR4","wall":4,"pos":[0.2,0.6,1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR4","sourcePage":1421,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR5","wall":4,"pos":[0.61,0.6,1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR5","sourcePage":1421,"sourceCount":4,"category":"LWL","kind":""},{"id":"SW4_T1_LR6","wall":4,"pos":[1.02,0.6,1.175],"radius":0.061,"row":1,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR6","sourcePage":1421,"sourceCount":1,"category":"Weitere","kind":""},{"id":"SW4_T1_LR7","wall":4,"pos":[-1.03,0.25,1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR7","sourcePage":1421,"sourceCount":3,"category":"LWL","kind":""},{"id":"SW4_T1_LR8","wall":4,"pos":[-0.62,0.25,1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR8","sourcePage":1421,"sourceCount":1,"category":"TK","kind":""},{"id":"SW4_T1_LR9","wall":4,"pos":[-0.21,0.25,1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR9","sourcePage":1421,"sourceCount":1,"category":"Weitere","kind":""},{"id":"SW4_T1_LR10","wall":4,"pos":[0.2,0.25,1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR10","sourcePage":1421,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR11","wall":4,"pos":[0.61,0.25,1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR11","sourcePage":1421,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR12","wall":4,"pos":[1.02,0.25,1.175],"radius":0.061,"row":2,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR12","sourcePage":1421,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR13","wall":4,"pos":[-1.03,-0.26,1.175],"radius":0.061,"row":3,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR13","sourcePage":1421,"sourceCount":1,"category":"MS","kind":""},{"id":"SW4_T1_LR14","wall":4,"pos":[-0.62,-0.26,1.175],"radius":0.061,"row":3,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR14","sourcePage":1421,"sourceCount":1,"category":"MS","kind":""},{"id":"SW4_T1_LR15","wall":4,"pos":[0.2,-0.26,1.175],"radius":0.061,"row":3,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR15","sourcePage":1421,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR16","wall":4,"pos":[0.61,-0.26,1.175],"radius":0.061,"row":3,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR16","sourcePage":1421,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR17","wall":4,"pos":[1.02,-0.26,1.175],"radius":0.061,"row":3,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR17","sourcePage":1421,"sourceCount":0,"category":"leer","kind":""},{"id":"SW4_T1_LR18","wall":4,"pos":[0.2,-0.65,1.175],"radius":0.061,"row":4,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR18","sourcePage":1421,"sourceCount":1,"category":"OLA","kind":""},{"id":"SW4_T1_LR19","wall":4,"pos":[0.61,-0.65,1.175],"radius":0.061,"row":4,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR19","sourcePage":1421,"sourceCount":1,"category":"OLA","kind":""},{"id":"SW4_T1_LR20","wall":4,"pos":[1.02,-0.65,1.175],"radius":0.061,"row":4,"col":null,"dn":null,"sourceAddress":"SW4/T1/LR20","sourcePage":1421,"sourceCount":1,"category":"OLA","kind":""}],"planPorts":[],"cables":[{"id":"ola-18","name":"OLA · LR18","group":"OLA","color":"#df323c","a":"SW2_T1_LR18","b":"SW4_T1_LR18","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR18","sourceB":"SW4/T1/LR18","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"ola-19","name":"OLA · LR19","group":"OLA","color":"#df323c","a":"SW2_T1_LR19","b":"SW4_T1_LR19","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR19","sourceB":"SW4/T1/LR19","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"ola-20","name":"OLA · LR20","group":"OLA","color":"#df323c","a":"SW2_T1_LR20","b":"SW4_T1_LR20","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR20","sourceB":"SW4/T1/LR20","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"ms-13","name":"MS · LR13","group":"MS","color":"#147ec1","a":"SW2_T1_LR13","b":"SW4_T1_LR13","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR13","sourceB":"SW4/T1/LR13","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"ms-14","name":"MS · LR14","group":"MS","color":"#147ec1","a":"SW2_T1_LR14","b":"SW4_T1_LR14","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR14","sourceB":"SW4/T1/LR14","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"lst-h10b","name":"S01AZ-H10.b","group":"LST","color":"#e49319","a":"SW2_T1_LR5","b":"SW1_T1_LR1","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR5","sourceB":"SW1/T1/LR1","sourcePages":[1420],"mappingStatus":"shown","geometryStatus":"","kind":""},{"id":"lwl-5-1","name":"LWL · LR5 · 1","group":"LWL","color":"#8a60b4","a":"SW2_T1_LR5","b":"SW4_T1_LR5","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR5","sourceB":"SW4/T1/LR5","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"lwl-5-2","name":"LWL · LR5 · 2","group":"LWL","color":"#8a60b4","a":"SW2_T1_LR5","b":"SW4_T1_LR5","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR5","sourceB":"SW4/T1/LR5","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"lwl-5-3","name":"LWL · LR5 · 3","group":"LWL","color":"#8a60b4","a":"SW2_T1_LR5","b":"SW4_T1_LR5","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR5","sourceB":"SW4/T1/LR5","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"lwl-5-4","name":"LWL · LR5 · 4","group":"LWL","color":"#8a60b4","a":"SW2_T1_LR5","b":"SW4_T1_LR5","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR5","sourceB":"SW4/T1/LR5","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"lwl-7-1","name":"LWL · LR7 · 1","group":"LWL","color":"#8a60b4","a":"SW2_T1_LR7","b":"SW4_T1_LR7","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR7","sourceB":"SW4/T1/LR7","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"lwl-7-2","name":"LWL · LR7 · 2","group":"LWL","color":"#8a60b4","a":"SW2_T1_LR7","b":"SW4_T1_LR7","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR7","sourceB":"SW4/T1/LR7","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"lwl-7-3","name":"LWL · LR7 · 3","group":"LWL","color":"#8a60b4","a":"SW2_T1_LR7","b":"SW4_T1_LR7","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR7","sourceB":"SW4/T1/LR7","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"tk-cu-8","name":"TK Cu · LR8","group":"TK","color":"#188f7d","a":"SW2_T1_LR8","b":"SW4_T1_LR8","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR8","sourceB":"SW4/T1/LR8","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"other-6","name":"Weitere · LR6","group":"Weitere","color":"#96a3ae","a":"SW2_T1_LR6","b":"SW4_T1_LR6","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR6","sourceB":"SW4/T1/LR6","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""},{"id":"other-9","name":"Weitere · LR9","group":"Weitere","color":"#96a3ae","a":"SW2_T1_LR9","b":"SW4_T1_LR9","curve":"","source":"Angehängte Schachtbelegung 13_T_021","sourceA":"SW2/T1/LR9","sourceB":"SW4/T1/LR9","sourcePages":[1420,1421],"mappingStatus":"schematic","geometryStatus":"","kind":""}],"originalDetails":{"title":"13_T_021 Schachtbelegung und Trennung","pages":[1420,1421],"image":"image(8).png","dimensions":"Schematische Abmessungen; Rohbauquerschnitt fehlt","diameters":"DN-Zuordnung in der Vorlage offen","routing":"Kabelanzahl und belegte Rohre gemäß Bild. Durchgehende Einzelkabelpaare und Bögen schematisch. LST-Abgang S01AZ-H10.b ausdrücklich gezeigt."},"concept":{"materialRequirement":"Aestuver bzw. fermacell Powerpanel; Systemauswahl offen","panelsPerEnclosure":6,"ownBottomAndEndPanels":true,"technicalReview":"Plattendicken, Fugen, Befestigungen und Durchführungen offen; keine Anrechnung vorhandener Wände oder Boden","dimensions":"schematisch","referenceFor":["13_T_017","13_T_018","13_T_021"],"singleCablePairing":"schematisch, außer LST-Abgang S01AZ-H10.b gemäß Bildhinweis"},"curveSamples":{}},"13_U_028":{"name":"13_U_028","refFile":"13_U_028_Kabeleditor_2026-11-05_08-19.html","savedAt":"2026-11-05T08:19:36.165Z","dims":[2.75,2.35,1.85,null],"ports":[{"id":"SW1_R1_C1","wall":1,"pos":[-1.375,0.67122,0.86661],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R1_C2","wall":1,"pos":[-1.375,0.66349,0.48979],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R1_C3","wall":1,"pos":[-1.375,0.62921,0.13643],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R1_C4","wall":1,"pos":[-1.375,0.59394,-0.20447],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R1_C5","wall":1,"pos":[-1.375,0.55106,-0.52778],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R2_C1","wall":1,"pos":[-1.375,0.35147,0.87201],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R2_C2","wall":1,"pos":[-1.375,0.34726,0.48979],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R2_C3","wall":1,"pos":[-1.375,0.3314,0.14177],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R2_C4","wall":1,"pos":[-1.375,0.30899,-0.19915],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R2_C5","wall":1,"pos":[-1.375,0.27745,-0.52778],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R3_C1","wall":1,"pos":[-1.375,0.04511,0.86661],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R3_C2","wall":1,"pos":[-1.375,0.02696,0.48979],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R3_C3","wall":1,"pos":[-1.375,0.01598,0.14177],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R3_C4","wall":1,"pos":[-1.375,-0.01229,-0.19384],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R3_C5","wall":1,"pos":[-1.375,-0.02695,-0.52249],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R4_C1","wall":1,"pos":[-1.375,-0.26388,0.86661],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R4_C2","wall":1,"pos":[-1.375,-0.28116,0.48979],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R4_C3","wall":1,"pos":[-1.375,-0.29944,0.14177],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R4_C4","wall":1,"pos":[-1.375,-0.3229,-0.19384],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R4_C5","wall":1,"pos":[-1.375,-0.41396,-0.60177],"radius":0.179,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"gross"},{"id":"SW1_R5_C1","wall":1,"pos":[-1.375,-0.581,0.86661],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R5_C2","wall":1,"pos":[-1.375,-0.60551,0.48979],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R5_C3","wall":1,"pos":[-1.375,-0.62838,0.13643],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R5_C4","wall":1,"pos":[-1.375,-0.64963,-0.19384],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW1_R5_C5","wall":1,"pos":[-1.375,-0.80003,-0.60177],"radius":0.179,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"gross"},{"id":"SW3_R1_C1","wall":3,"pos":[1.375,0.55573,-0.46173],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R1_C2","wall":3,"pos":[1.375,0.554,-0.17755],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R1_C3","wall":3,"pos":[1.375,0.56434,0.10723],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R1_C4","wall":3,"pos":[1.375,0.5816,0.4036],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R1_C5","wall":3,"pos":[1.375,0.58422,0.70613],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R1_C6","wall":3,"pos":[1.375,0.60298,1.00933],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R2_C1","wall":3,"pos":[1.375,0.25059,-0.46173],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R2_C2","wall":3,"pos":[1.375,0.25256,-0.17755],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R2_C3","wall":3,"pos":[1.375,0.24649,0.10723],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R2_C4","wall":3,"pos":[1.375,0.25535,0.4036],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R2_C5","wall":3,"pos":[1.375,0.26566,0.70613],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R2_C6","wall":3,"pos":[1.375,0.276,1.00933],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R3_C1","wall":3,"pos":[1.375,-0.04651,-0.46173],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R3_C2","wall":3,"pos":[1.375,-0.04888,-0.17755],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R3_C3","wall":3,"pos":[1.375,-0.03917,0.10723],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R3_C4","wall":3,"pos":[1.375,-0.04271,0.4036],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R3_C5","wall":3,"pos":[1.375,-0.0287,0.70613],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R3_C6","wall":3,"pos":[1.375,-0.01061,1.00933],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R4_C1","wall":3,"pos":[1.375,-0.52672,-0.64176],"radius":0.179,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"gross"},{"id":"SW3_R4_C2","wall":3,"pos":[1.375,-0.3463,-0.17755],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R4_C3","wall":3,"pos":[1.375,-0.34093,0.10723],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R4_C4","wall":3,"pos":[1.375,-0.34219,0.39811],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R4_C5","wall":3,"pos":[1.375,-0.34606,0.69511],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R4_C6","wall":3,"pos":[1.375,-0.3243,0.99829],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R5_C1","wall":3,"pos":[1.375,-0.65034,-0.17208],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R5_C2","wall":3,"pos":[1.375,-0.64791,0.1182],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R5_C3","wall":3,"pos":[1.375,-0.64688,0.4036],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R5_C4","wall":3,"pos":[1.375,-0.64444,0.69511],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW3_R5_C5","wall":3,"pos":[1.375,-0.6432,0.99829],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW2_Oeffnung_01","wall":2,"pos":[-0.74953,0.24426,-1.175],"radius":0.218,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"gross"},{"id":"SW2_Oeffnung_02","wall":2,"pos":[-0.2016,0.29632,-1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW2_Oeffnung_03","wall":2,"pos":[0.05169,0.28831,-1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW2_Oeffnung_04","wall":2,"pos":[0.29981,0.28431,-1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW2_Oeffnung_05","wall":2,"pos":[-0.2016,0.10411,-1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW2_Oeffnung_06","wall":2,"pos":[0.04652,0.0961,-1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW2_Oeffnung_07","wall":2,"pos":[0.30498,0.0921,-1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW2_Oeffnung_08","wall":2,"pos":[-0.2016,-0.08009,-1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW2_Oeffnung_09","wall":2,"pos":[0.04652,-0.0881,-1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW2_Oeffnung_10","wall":2,"pos":[0.31015,-0.0921,-1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW2_Oeffnung_11","wall":2,"pos":[0.65648,-0.11212,-1.175],"radius":0.051,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"mini"},{"id":"SW2_Oeffnung_12","wall":2,"pos":[0.87876,-0.11212,-1.175],"radius":0.051,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"mini"},{"id":"SW4_Oeffnung_01","wall":4,"pos":[-0.74953,0.24426,1.175],"radius":0.218,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"gross"},{"id":"SW4_Oeffnung_02","wall":4,"pos":[-0.2016,0.29632,1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW4_Oeffnung_03","wall":4,"pos":[0.05169,0.28831,1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW4_Oeffnung_04","wall":4,"pos":[0.29981,0.28431,1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW4_Oeffnung_05","wall":4,"pos":[-0.2016,0.10411,1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW4_Oeffnung_06","wall":4,"pos":[0.04652,0.0961,1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW4_Oeffnung_07","wall":4,"pos":[0.30498,0.0921,1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW4_Oeffnung_08","wall":4,"pos":[-0.2016,-0.08009,1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW4_Oeffnung_09","wall":4,"pos":[0.04652,-0.0881,1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW4_Oeffnung_10","wall":4,"pos":[0.31015,-0.0921,1.175],"radius":0.071,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"klein"},{"id":"SW4_Oeffnung_11","wall":4,"pos":[0.65648,-0.11212,1.175],"radius":0.051,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"mini"},{"id":"SW4_Oeffnung_12","wall":4,"pos":[0.87876,-0.11212,1.175],"radius":0.051,"row":null,"col":null,"dn":null,"sourceAddress":"","sourcePage":null,"sourceCount":null,"category":"","kind":"mini"}],"planPorts":[{"id":"PLAN_SW1_T1_LR12","wall":1,"sourceAddress":"SW1/T1/LR12","sourcePage":1502,"destination":"13_U_042"},{"id":"PLAN_SW2_T1_LR2","wall":2,"sourceAddress":"SW2/T1/LR2","sourcePage":1502,"destination":"21_B_010"},{"id":"PLAN_SW1_T1_LR25","wall":1,"sourceAddress":"SW1/T1/LR25","sourcePage":1502,"destination":"13_U_042"},{"id":"PLAN_SW3_T1_LR3","wall":3,"sourceAddress":"SW3/T1/LR3","sourcePage":1503,"destination":"13_U_032"},{"id":"PLAN_SW1_T1_LR14","wall":1,"sourceAddress":"SW1/T1/LR14","sourcePage":1502,"destination":"13_U_042"},{"id":"PLAN_SW2_T1_LR1","wall":2,"sourceAddress":"SW2/T1/LR1","sourcePage":1502,"destination":"21_B_010"},{"id":"PLAN_SW4_T1_LR9","wall":4,"sourceAddress":"SW4/T1/LR9","sourcePage":1503,"destination":"13_U_021"},{"id":"PLAN_SW1_T1_LR3","wall":1,"sourceAddress":"SW1/T1/LR3","sourcePage":1502,"destination":"13_U_042"},{"id":"PLAN_SW3_T1_LR5","wall":3,"sourceAddress":"SW3/T1/LR5","sourcePage":1503,"destination":"13_U_032"},{"id":"PLAN_SW3_T1_LR7","wall":3,"sourceAddress":"SW3/T1/LR7","sourcePage":1503,"destination":"13_U_032"},{"id":"PLAN_SW2_T1_LR3","wall":2,"sourceAddress":"SW2/T1/LR3","sourcePage":1502,"destination":"21_B_010"},{"id":"PLAN_SW4_T1_LR11","wall":4,"sourceAddress":"SW4/T1/LR11","sourcePage":1503,"destination":"13_U_021"},{"id":"PLAN_SW1_T1_LR16","wall":1,"sourceAddress":"SW1/T1/LR16","sourcePage":1502,"destination":"13_U_042"},{"id":"PLAN_SW1_T1_LR20","wall":1,"sourceAddress":"SW1/T1/LR20","sourcePage":1502,"destination":"13_U_042"},{"id":"PLAN_SW4_T1_LR5","wall":4,"sourceAddress":"SW4/T1/LR5","sourcePage":1503,"destination":"13_U_021"}],"cables":[{"id":"blue-1","name":"Blau 1","group":"MS","color":"#087be8","a":"SW1_R5_C5","b":"SW2_Oeffnung_01","curve":"Blau_SW1_oben_bis_SW2","source":"","sourceA":"","sourceB":"","sourcePages":[],"mappingStatus":"","geometryStatus":"","kind":""},{"id":"blue-2","name":"Blau 2","group":"MS","color":"#087be8","a":"SW1_R4_C5","b":"SW3_R4_C1","curve":"Blau_SW1_unten_bis_SW3","source":"","sourceA":"","sourceB":"","sourcePages":[],"mappingStatus":"","geometryStatus":"","kind":""},{"id":"blue-3","name":"Blau 3","group":"MS","color":"#087be8","a":"SW1_R4_C5","b":"SW3_R4_C1","curve":"Blau_SW2_bis_SW3","source":"","sourceA":"","sourceB":"","sourcePages":[],"mappingStatus":"","geometryStatus":"","kind":""},{"id":"red-1","name":"Rot 1","group":"OLA","color":"#e32525","a":"SW1_R5_C2","b":"SW3_R5_C4","curve":"Rot_1_SW1_bis_SW3","source":"","sourceA":"","sourceB":"","sourcePages":[],"mappingStatus":"","geometryStatus":"","kind":""},{"id":"red-2","name":"Rot 2","group":"OLA","color":"#e32525","a":"SW1_R5_C3","b":"SW3_R5_C3","curve":"Rot_2_SW1_bis_SW3","source":"","sourceA":"","sourceB":"","sourcePages":[],"mappingStatus":"","geometryStatus":"","kind":""},{"id":"tk-0","name":"Cu_F4734_20\"_03","group":"TK","color":"#13936b","a":"SW1_R3_C2","b":"SW2_Oeffnung_09","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW1/T1/LR12","sourceB":"SW2/T1/LR2","sourcePages":[1504,1505],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"TK_CU"},{"id":"tk-3","name":"FB 420107 TS","group":"TK","color":"#13936b","a":"SW1_R4_C3","b":"PLAN_SW3_T1_LR3","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW1/T1/LR25","sourceB":"SW3/T1/LR3","sourcePages":[1505,1506],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"TK_CU"},{"id":"tk-4","name":"LWL_F7279_96'_02","group":"TK","color":"#13936b","a":"PLAN_SW1_T1_LR14","b":"SW2_Oeffnung_10","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW1/T1/LR14","sourceB":"SW2/T1/LR1","sourcePages":[1504,1505],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"TK_LWL"},{"id":"tk-5","name":"LWL_F772840_24 Hin","group":"TK","color":"#13936b","a":"SW2_Oeffnung_10","b":"SW4_Oeffnung_05","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW2/T1/LR1","sourceB":"SW4/T1/LR9","sourcePages":[1505,1507],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"TK_LWL"},{"id":"tk-6","name":"LWL_F772863_60'_02","group":"TK","color":"#13936b","a":"PLAN_SW1_T1_LR14","b":"SW2_Oeffnung_10","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW1/T1/LR14","sourceB":"SW2/T1/LR1","sourcePages":[1504,1505],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"TK_LWL"},{"id":"tk-7","name":"LWL_St1_F7279_96'","group":"TK","color":"#13936b","a":"SW1_R1_C2","b":"PLAN_SW3_T1_LR5","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW1/T1/LR3","sourceB":"SW3/T1/LR5","sourcePages":[1504,1506],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"TK_LWL"},{"id":"tk-8","name":"LWL_St1_F772835_96_02","group":"TK","color":"#13936b","a":"SW1_R1_C2","b":"PLAN_SW3_T1_LR5","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW1/T1/LR3","sourceB":"SW3/T1/LR5","sourcePages":[1504,1506],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"TK_LWL"},{"id":"tk-9","name":"LWL_St1_F772863_60'","group":"TK","color":"#13936b","a":"SW1_R1_C2","b":"PLAN_SW3_T1_LR7","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW1/T1/LR3","sourceB":"SW3/T1/LR7","sourcePages":[1504,1506],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"TK_LWL"},{"id":"tk-10","name":"LWL_St1_F772874_24'","group":"TK","color":"#13936b","a":"SW1_R3_C3","b":"PLAN_SW3_T1_LR5","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW1/T1/LR14","sourceB":"SW3/T1/LR5","sourcePages":[1504,1506],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"TK_LWL"},{"id":"lst-11","name":"S01AZ-F15.a","group":"LST","color":"#e09922","a":"SW2_Oeffnung_08","b":"SW4_Oeffnung_11","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW2/T1/LR3","sourceB":"SW4/T1/LR11","sourcePages":[1505,1507],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"LST_EL"},{"id":"lst-12","name":"S01AZ-F15.f","group":"LST","color":"#e09922","a":"SW1_R4_C1","b":"SW4_Oeffnung_11","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW1/T1/LR16","sourceB":"SW4/T1/LR11","sourcePages":[1504,1507],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"LST_EL"},{"id":"lst-13","name":"S01AZ-F20.1","group":"LST","color":"#e09922","a":"SW2_Oeffnung_08","b":"SW4_Oeffnung_11","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW2/T1/LR3","sourceB":"SW4/T1/LR11","sourcePages":[1505,1507],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"LST_EL"},{"id":"tk-14","name":"Sek 6_SK_b","group":"TK","color":"#13936b","a":"SW1_R4_C4","b":"SW4_Oeffnung_12","curve":"","source":"KD-001 j, 03.07.2026","sourceA":"SW1/T1/LR20","sourceB":"SW4/T1/LR5","sourcePages":[1504,1507],"mappingStatus":"","geometryStatus":"Plananschlüsse; Zuordnung zum geänderten Lochbild offen","kind":"TK_COAX"}],"originalDetails":{"sourceDocument":"A-01-17-B1N24-99-KD-001-j-26.pdf","sourceVersion":"Index j vom 03.07.2026","sourcePages":[1504,1505,1506,1507],"crossCheck":"AVL0116_Schachtbewertung_Trennkonzept_2026-10-23.pdf, S. 19","anchoring":"Plan-Marker auf der jeweiligen Schachtwand; keine bestätigte Zuordnung zu sichtbaren Bildöffnungen. Keine zusätzlichen physischen Leerrohre."},"concept":{"enabled":true,"opacity":0.08,"materialRequirement":"Aestuver / fermacell Powerpanel","construction":"Bodenplatte, Deckel, zwei Seitenplatten, zwei Stirnplatten mit Kabeleinführungen","dimensions":"schematisch","exactProductAndThickness":"offen"},"curveSamples":{"Blau_SW1_oben_bis_SW2":[[-1.2625,-0.51287,-0.62099],[-1.11267,-0.68918,-0.65526],[-0.95466,-0.76533,-0.67254],[-0.80932,-0.76468,-0.7932],[-0.78063,-0.68906,-0.96531],[-0.78353,-0.43542,-1.01949],[-0.78353,-0.07294,-1.03731],[-0.78353,0.19394,-1.16208]],"Blau_SW1_unten_bis_SW3":[[-1.24601,-0.8127,-0.57591],[-1.04589,-0.83529,-0.52982],[-0.80906,-0.845,-0.51],[-0.13424,-0.845,-0.51],[0.55969,-0.845,-0.51],[1.07882,-0.83609,-0.51274],[1.20496,-0.70965,-0.55157],[1.36344,-0.55982,-0.5976]],"Blau_SW2_bis_SW3":[[-0.71553,0.03013,-1.066],[-0.71553,-0.4014,-1.02948],[-0.6805,-0.66867,-0.93139],[-0.54401,-0.75804,-0.7164],[-0.06873,-0.765,-0.68],[0.57053,-0.765,-0.68],[1.1005,-0.75052,-0.67974],[1.26544,-0.61059,-0.67725]],"Rot_1_SW1_bis_SW3":[[-1.26002,-0.68285,0.53775],[-1.11176,-0.82071,0.62324],[-0.88884,-0.88,0.66],[-0.20368,-0.88,0.66],[0.505,-0.88,0.66],[1.02968,-0.8734,0.66098],[1.16954,-0.77983,0.67493],[1.35056,-0.66894,0.69146]],"Rot_2_SW1_bis_SW3":[[-1.26002,-0.69927,0.19942],[-1.11176,-0.82565,0.31171],[-0.88884,-0.88,0.36],[-0.20368,-0.88,0.36],[0.505,-0.88,0.36],[1.02968,-0.87347,0.36122],[1.16954,-0.78087,0.37854],[1.35056,-0.67113,0.39907]]}}};
const manifest=[
 {id:'source-t021',name:'4 · 13_T_021 · Referenzdaten',description:'Originales Schacht-HTML: 48 sichtbare Rohre, 16 Kabel, 2 getrennte Systeme, schematische Maße'},
 {id:'source-u028',name:'5 · 13_U_028 · Referenzdaten',description:'Originales Schacht-HTML: 78 sichtbare Rohre, 18 Kabel, 15 Planmarker (nicht physisch bestätigt)'},
 {id:'source-both',name:'6 · Beide Referenzschächte (Vergleich)',description:'Beide Datenquellen in einem Projekt; Kartenpositionen nur zur Ansicht erfunden, ohne bestätigte Verbindung'},
 {id:'pdf-u028',name:'★ 13_U_028 · PDF V2 Sollbild (91 LR)',description:'PDF S.21–24: vier Wandbilder, nominale DN und dokumentierte Belegungen, XYZ offen'}
];
const round=v=>Math.round(v*100000)/100000;
const clamp=(v,min,max)=>Math.min(max,Math.max(min,v));
const pick=p=>p?.length?p[0]:null;
// Compact demo arrangement, preserve original PDF-circle centers separately.
const compactPdf=v=>round(.5+(v-.5)*.62);

/* PDF V2 pp.21-24: transcribed DN and cable allocations.
   Relative hole positions are normalized from the original PDF vector circle
   centers. These are diagram positions, NOT surveyed 3D coordinates.
   Shaft/wall dimensions remain visibly flagged as old-HTML placeholders. */
function createPdfU028(){
 const shaftId='13_U_028',dims={w:2750,d:2350,h:1850};
 const rows={
 SW1:[
 [1,[1,2,3,4,5],[.0513,.1734,.2956,.4178,.5400],.8373],
 [1,[6,7,8,9,10],[.0513,.1734,.2956,.4178,.5400],.6727],
 [1,[11,12,13,14,15],[.0513,.1734,.2956,.4178,.5400],.5081],
 [1,[16,17,18,19,20,21],[.0513,.1734,.2956,.4178,.5400,.6621],.3436],
 [1,[22,23,24,25,26,27],[.0513,.1734,.2956,.4178,.5400,.6621],.1790],
 [2,[1,2],[.8597,.9653],.8373]],
 SW2:[
 [1,[11,10,9],[.2489,.3906,.5324],.8174],
 [1,[5,8,7,6],[.1071,.2489,.3906,.5324],.6528],
 [1,[1,4,3,2],[.1071,.2489,.3906,.5324],.4882],
 [3,[1],[.8597],.8174],[3,[2],[.8597],.6528]],
 SW3:[
 [1,[5,4,3,2,1],[.1976,.3198,.4419,.5641,.6863],.8373],
 [1,[10,9,8,7,6],[.1976,.3198,.4419,.5641,.6863],.6727],
 [1,[15,14,13,12,11],[.1976,.3198,.4419,.5641,.6863],.5081],
 [1,[20,19,18,17,16],[.1976,.3198,.4419,.5641,.6863],.3436],
 [1,[26,25,24,23,22,21],[.0754,.1976,.3198,.4419,.5641,.6863],.1790]],
 SW4:[
 [1,[6,5,4,3,2,1],[.0754,.2127,.3499,.4872,.6244,.7617],.8282],
 [1,[12,11,10,9,8,7],[.0754,.2127,.3499,.4872,.6244,.7617],.6637],
 [1,[18,17,16,15,14,13],[.0754,.2127,.3499,.4872,.6244,.7617],.4991],
 [1,[23,22,21,20,19],[.2127,.3499,.4872,.6244,.7617],.3345]]};
 const key=(w,t,r)=>w+'_T'+t+'_LR'+String(r).padStart(2,'0'),byId=new Map(),walls=[];
 for(const [w,grid] of Object.entries(rows)){
  const holes=[],page={SW1:21,SW2:22,SW3:23,SW4:24}[w];
  for(const [trasse,numbers,x,v] of grid)for(let col=0;col<numbers.length;col++){
   const lr=numbers[col],dn=w==='SW1'&&trasse===2?63:
    w==='SW2'&&trasse===3&&lr===1?75:
    w==='SW2'&&trasse===1&&lr===5?160:
    w==='SW1'&&trasse===1&&[21,27].includes(lr)?160:
    w==='SW3'&&lr===26?160:110;
   const hole={id:key(w,trasse,lr),row:Math.round((1-v)*10),
    col:col+1,u:x[col],v:v,
    sourceU:x[col],sourceV:v,dn,
    sourceAddress:'Trasse '+trasse+' / LR '+String(lr).padStart(2,'0'),
    sourcePage:page,sourceCount:0,sourceCategory:'Trasse '+trasse,
    nominalDnConfirmed:false,origin:'pdf-v2-nominal-dn-unverified'};
   holes.push(hole);byId.set(hole.id,hole);
  }
  walls.push({id:w,name:w,visible:true,rows:5,cols:6,thickness:120,dn:110,holes});
 }
 const named={
  'SW1/1/1':'SK6.0',
  'SW1/1/3':"LWL-St1-F7279_96'|LWL-St1-F772835_96_02|LWL-St1-F772863_60'",
  'SW1/1/5':'OLA TST 25.1- KEV25_01',
  'SW1/1/12':'Cu F4734/20',
  'SW1/1/14':"LWL_F7279_96'_02|LWL_F772863_60'_02|LWL-St1-F772874_24'",
  'SW1/1/15':'NS-HUP-0201',
  'SW1/1/16':'Begleiterde S05+S06|S01AZ-F15.f',
  'SW1/1/18':'Feederkabel S05|Feederkabel S06',
  'SW1/1/20':'Sek 6_SK_b',
  'SW1/1/21':'MS-SNG-0038|NS-HUP-0203',
  'SW1/1/27':'MS-SNG-0056',
  'SW2/1/10':'OLSP 2960|OLSP 2961|OLSP 2962|OLSP 2963',
  'SW2/1/9':'OLSP 2060|OLSP 2061|OLSP 2062|OLSP 2063',
  'SW2/1/5':'MS-SNG-0056',
  'SW2/1/1':"LWL_F7279_96'_02|LWL_F772863_60'_02|LWL_F772840_24 Hin",
  'SW2/1/4':'Cu F4734/20',
  'SW2/1/3':'S01AZ-F15.a|S01AZ-F20.1',
  'SW3/1/5':"LWL-St1-F7279_96'|LWL-St1-F772835_96_02|LWL-St1-F772874_24'",
  'SW3/1/4':'F772623',
  'SW3/1/3':'FB 420107 TS',
  'SW3/1/2':'OLSP 100 SK|OLSP 200 SK',
  'SW3/1/10':'TSB-HUP-2203_01|TÜZ-HUP-2406_04',
  'SW3/1/8':'St1AA8763173M1/2_Strom|St2AA8763176M1/2_Strom',
  'SW3/1/7':"LWL-St1-F772863_60'",
  'SW3/1/17':'SK6.0',
  'SW3/1/26':'MS-SNG-0038',
  'SW3/1/25':'NS-HUP-0201',
  'SW4/1/6':'Begleiterde S05+S06',
  'SW4/1/5':'Sek 6_SK_b',
  'SW4/1/3':'F772623',
  'SW4/1/1':'TSB-HUP-2104|TSB-HUP-2104|TÜZ-HUP-2406_03',
  'SW4/1/11':'S01AZ-F15.f|S01AZ-F15.a|S01AZ-F20.1',
  'SW4/1/10':'Feederkabel S05|Feederkabel S06',
  'SW4/1/9':'LWL_F772840_24 Hin'
 };
 // Names absent from the supplied document are kept as explicitly unnamed entries.
 const missing={
  'SW1/1/22':['OLSP',8],'SW1/1/24':['Weitere',2],
  'SW1/1/25':['TK/LWL',1],'SW1/1/26':['OLA',1],
  'SW1/1/27':['Weitere',4],'SW1/2/1':['OLSP',2],
  'SW2/3/1':['EEA',5],'SW3/1/1':['GSM-P',4],
  'SW3/1/18':['OLSP',16],'SW3/1/24':['EEA',1],
  'SW3/1/23':['OLA',1],'SW3/1/22':['OLA',1]
 };
 const category=name=>name.startsWith('MS-')?'MS':
  name.startsWith('OLA ')?'OLA':name.startsWith('OLSP')?'OLSP':
  /^(LWL|Cu |F772623)/.test(name)?'TK/LWL':
  /^(S01AZ|FB )/.test(name)?'LST':
  /^(TSB|TÜZ|NS-)/.test(name)?'EEA':'Weitere';
 const colors={MS:'#247dc5',OLA:'#d62f48',OLSP:'#9351b5',
  'TK/LWL':'#119e91',LST:'#a06a46',EEA:'#c58c16',
  'GSM-P':'#79899c',Weitere:'#82929f'};
 const entries=[];
 function add(address,name,kind,unknown){
  const [w,t,lr]=address.split('/'),hole=byId.get(key(w,+t,+lr));
  if(!hole)throw Error('PDF V2: Missing documented pipe '+address);
  entries.push({w,name,kind,unknown,hole,page:hole.sourcePage});
  hole.sourceCount++;
 }
 for(const [address,text] of Object.entries(named))
  for(const name of text.split('|'))add(address,name,category(name),false);
 for(const [address,[kind,count]] of Object.entries(missing))
  for(let i=1;i<=count;i++)add(address,kind+' · Bezeichnung offen '+i+'/'+count,kind,true);
 const byName=new Map();
 for(const e of entries)if(!e.unknown)byName.set(e.name,[...(byName.get(e.name)||[]),e]);
 const used=new Set(),endpoint=e=>({wall:e.w,hole:e.hole.id}),cables=[];
 for(let i=0;i<entries.length;i++){
  const e=entries[i];if(used.has(e))continue;
  const matches=byName.get(e.name)||[];
  const peer=!e.unknown&&matches.length===2&&matches[0].w!==matches[1].w?
   matches.find(x=>x!==e):null;
  used.add(e);if(peer)used.add(peer);
  cables.push({id:'PDF28-'+String(i+1).padStart(3,'0'),name:e.name,
   group:['MS','OLA'].includes(e.kind)?e.kind:'Weitere',
   color:colors[e.kind]||colors.Weitere,diameter:12,route:[],
   a:endpoint(e),b:peer?endpoint(peer):null,
   referenceGroup:e.kind,sourceOriginalA:e.hole.id,
   sourceOriginalB:peer?.hole.id||'',sourceA:e.w+'/'+e.hole.sourceAddress,
   sourceB:peer?peer.w+'/'+peer.hole.sourceAddress:'',
   source:'AvL0116 V2 PDF S. '+e.page+(peer?' / '+peer.page:''),
   sourcePages:peer?[e.page,peer.page]:[e.page],
   mappingStatus:e.unknown?'Einzelidentität offen (nur Anzahl im PDF)':
    peer?'Gleichnamige Wandangaben, 3D-Verbindung schematisch / Rmin ungeprüft':
    'Nur eine Wandbelegung; anderer Kabelanschluss offen',
   sourceKind:'pdf-v2-wall',drawingDiameterOnly:true
  });
 }
 const enclosures=components.defaults(dims);
 for(const g of ['OLA','MS']){
  enclosures[g].visible=false; // No invented route or verified mounting.
  for(const plate of enclosures[g].panels){
   plate.material='fermacell Powerpanel H2O (nur Konzept/ungeprüft)';
   plate.thickness=24; // generic editor placeholder, not a certified dimension
  }
 }
 const sourceInfo={shaft:shaftId,sourceFile:'AVL0116_Fachkonzept_Eugen_NEU.pdf',
  savedAt:'2026-11-08',document:'AvL0116 Fachkonzept V2, 08.11.2026',
  sourceVersion:'PDF V2 (Konzept, kein Ausführungsplan)',
  sourcePages:[21,22,23,24],geometryStatus:'schematic',
  warning:'91 nominale DN und Rohrbelegungen aus PDF S.21–24. Original-Rohrmittelpunkte aus PDF-Vektorgrafik erhalten; Nur die optionale Basic-3D-Vorschau zeigt auf 62 % verdichtete Abstände. Datenmodell und Editor halten die originalen PDF-Kreismittelpunkte unverändert; diese sind nicht vermessen. Reale XYZ-Abstände und Schachtmaße fehlen. 2750/2350/1850/120 mm aus HTML nur Platzhalter. Kabellängen, Rmin, Powerpanel-Kanäle und Auflager ungeprüft.',
  originalNote:'PDF S.13 nennt SW2/Trasse 3 TSB/TÜZ, Detailseite S.22 dagegen 5 EEA. Hier Detailseite S.22 abgebildet; Klärung offen. Vollständiges angekündigtes Kabelverzeichnis nicht enthalten; keine realen Kanalpositionen.',
  planAnchors:[],unmatchedEndpointCount:cables.filter(c=>!c.b).length,
  originalCableCount:cables.length,originalPhysicalPortCount:91
 };
 const project={version:5,name:'13_U_028 · PDF-V2-Sollbild (schematisch)',
  dims,walls,cables,opacity:42,showBox:false,showLabels:true,splitEnabled:true,
  enclosures,supports:[],variants:[],sourceInfo};
 const shaft={id:shaftId,name:project.name,x:0,z:0,snapshot:network.snapshot(project)};
 return {...project,activeShaftId:shaftId,network:{
  version:1,shafts:[shaft],links:[],routes:[],audit:[],
  quality:{minPipeGapMm:null,minEdgeMm:null,minBendRadiusMm:null},
  reviews:[{shaftId,entityType:'shaft',entityId:shaftId,
   status:'Zur Prüfung',source:'PDF AvL0116 V2, Seiten 21–24',
   checkedBy:'Konzeptübernahme',checkedAt:'2026-11-08T00:00:00Z',
   note:'Nominale Rohrgrößen und Belegungen aus Wandbildern; reale Geometrie und Powerpanel-Konstruktion offen.'}]
 }};
}

function create(id){
 if(!components||!network)throw Error('Kabelsalat-Referenzdaten benötigen Komponenten und Netzmodell');
 if(id==='pdf-u028')return createPdfU028();
 if(id==='source-both'){
  const first=create('source-t021'),second=create('source-u028');
  const other={...second.network.shafts[0],x:15000,z:0};
  const net={...first.network,shafts:[first.network.shafts[0],other],
   links:[],routes:[],reviews:[...first.network.reviews,...second.network.reviews],audit:[]};
  return {...first,name:'Referenzvergleich: 13_T_021 + 13_U_028 (schematisch)',
   network:net,activeShaftId:'13_T_021'};
 }

 const key=id==='source-t021'?'13_T_021':id==='source-u028'?'13_U_028':null;
 if(!key)throw Error('Unbekannter Referenzdatensatz: '+id);
 const original=RECORDS[key];
 const [width,depth,height,wallThickness]=original.dims;
 const dims={w:Math.round(width*1000),d:Math.round(depth*1000),h:Math.round(height*1000)};
 const walls=[1,2,3,4].map(num=>{
  const wallId='SW'+num,ports=original.ports.filter(x=>x.wall===num);
  const holes=ports.map((p,i)=>{
   const [x,y,z]=p.pos;
   // Original physical port coordinates are centred around the middle of the shaft.
   const u=(num===1||num===3)?(z+depth/2)/depth:(x+width/2)/width;
   const v=(y+height/2)/height;
   // radius = graphical aperture radius, NOT confirmed pipe nominal diameter.
   return {id:p.id,row:Number(p.row)||0,col:Number(p.col)||0,u:round(clamp(u,0,1)),
    v:round(clamp(v,0,1)),dn:Math.round(p.radius*2000),
    sourceAddress:p.sourceAddress||p.id,sourcePage:p.sourcePage,
    sourceCount:p.sourceCount,sourceCategory:p.category||p.kind||'',
    nominalDnConfirmed:false,origin:'uploaded-html-graphical-opening'};
  });
  return {id:wallId,name:wallId,visible:true,rows:5,
   cols:Math.min(20,Math.max(5,Math.ceil(ports.length/5))),
   thickness:Number.isFinite(wallThickness)?Math.round(wallThickness*1000):120,
   dn:Math.round((pick(ports)?.radius||.06)*2000),holes};
 });
 const present=new Map(original.ports.map(p=>[p.id,p]));
 const resolve=portId=>{
  const port=present.get(portId);
  return port?{wall:'SW'+port.wall,hole:port.id}:null;
 };
 const cables=original.cables.map(c=>{
  const route=(original.curveSamples[c.curve]||[]).slice(0,10).map(p=>
   [Math.round(p[0]*1000),Math.round((p[1]+height/2)*1000),Math.round(p[2]*1000)]);
  const suffix=c.group==='OLA'?'OLA':c.group==='MS'?'MS':'Weitere';
  return {id:key+'-'+c.id,name:c.name,group:suffix,color:c.color,
   diameter:12,route,a:resolve(c.a),b:resolve(c.b),
   referenceGroup:c.group,sourceOriginalA:c.a,sourceOriginalB:c.b,
   sourceA:c.sourceA,sourceB:c.sourceB,source:c.source,
   sourcePages:c.sourcePages,
   mappingStatus:c.mappingStatus||c.geometryStatus||'Schematische Zuordnung',
   sourceKind:c.kind,sourceCableId:c.id,
   drawingDiameterOnly:true};
 });
 const enclosures=components.defaults(dims);
 enclosures.MS.visible=key==='13_T_021';
 enclosures.OLA.opacity=key==='13_T_021'?38:25;
 enclosures.MS.opacity=38;
 const numberOfMissingEndpoints=cables.reduce((n,c)=>n+(!c.a?1:0)+(!c.b?1:0),0);
 const sourceInfo={
  shaft:key,sourceFile:original.refFile,savedAt:original.savedAt,
  document:original.originalDetails.sourceDocument||original.originalDetails.title||original.refFile,
  sourceVersion:original.originalDetails.sourceVersion||'Vorlage vom 05.11.2026',
  sourcePages:original.originalDetails.sourcePages||original.originalDetails.pages||[],
  geometryStatus:'schematic',
  warning:'Rohrdurchmesser aus grafischen Radien abgeleitet; echte DN, Baugeometrie und physische Kabelwege nicht bestätigt.'+(Number.isFinite(wallThickness)?'':' Wandstärke (120 mm) nur Software-Standardwert, kein Quellenmaß.'),
  originalNote:JSON.stringify(original.originalDetails).slice(0,1800),
  planAnchors:original.planPorts.map(x=>({
   id:x.id,wall:'SW'+x.wall,sourceAddress:x.sourceAddress,sourcePage:x.sourcePage,
   destination:x.destination||'',physicalHole:false
  })),
  unmatchedEndpointCount:numberOfMissingEndpoints,
  originalCableCount:original.cables.length,originalPhysicalPortCount:original.ports.length
 };
 const project={version:5,name:key+' · aus HTML-Referenz (schematisch)',
  dims,walls,cables,opacity:52,showBox:true,showLabels:true,splitEnabled:true,
  enclosures,supports:[],variants:[],sourceInfo};
 const shaftId=key;
 const shaft={id:shaftId,name:project.name,x:0,z:0,snapshot:network.snapshot(project)};
 return {...project,activeShaftId:shaftId,
  network:{version:1,shafts:[shaft],links:[],routes:[],
   quality:{minPipeGapMm:null,minEdgeMm:null,minBendRadiusMm:null},
   reviews:[{shaftId,entityType:'shaft',entityId:shaftId,
     status:'Zur Prüfung',source:original.refFile,
     checkedBy:'Import (nicht geprüft)',checkedAt:original.savedAt,
     note:'Übernommen aus schematischem HTML-Editor; Anschlüsse/Abmessungen vor Ort prüfen.'}],
   audit:[]}};
}
function describe(id){
 if(id==='pdf-u028'){const p=createPdfU028();return {
 shaft:p.name,cables:p.cables.length,physicalPorts:91,planAnchors:0,
 pendingEndpoints:p.cables.filter(c=>!c.b).length};}
 const d=RECORDS[id==='source-t021'?'13_T_021':'13_U_028'];
 return {shaft:d.name,cables:d.cables.length,physicalPorts:d.ports.length,
 planAnchors:d.planPorts.length,pendingEndpoints:d.cables.reduce((n,c)=>n+
 (!d.ports.some(p=>p.id===c.a)?1:0)+(!d.ports.some(p=>p.id===c.b)?1:0),0)};
}
return {manifest,create,describe};
});
