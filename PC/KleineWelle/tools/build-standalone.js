/* Rebuild the no-dependencies one-file PC edition with: node tools/build-standalone.js */
"use strict";
const fs = require("fs");
const path = require("path");
const root = path.resolve(__dirname, "..");
const get = name => fs.readFileSync(path.join(root, name), "utf8");
const html = get("index.html");
const game = get("game.js");
const three = get("vendor/three.min.js");
const license = get("vendor/LICENSE.threejs");
const inline = text => text.replace(/<\/script/gi, "<\\/script");
const banner = "<!doctype html>\n<!--\nIncludes Three.js r150 (https://threejs.org/), licensed under MIT.\n" + license + "\n-->";
const output = html
  .replace("<!doctype html>", () => banner)
  .replace('<script src="./vendor/three.min.js"></script>', () => "<script>\n" + inline(three) + "\n</script>")
  .replace('<script src="./game.js"></script>', () => "<script>\n" + inline(game) + "\n</script>");
if (output.includes('src="./game.js"') || output.includes('src="./vendor/three.min.js"')) {
  throw new Error("A local script reference was not replaced");
}
fs.writeFileSync(path.join(root, "KleineWelle-offline.html"), output, "utf8");
console.log("Updated KleineWelle-offline.html (" + Buffer.byteLength(output) + " bytes)");
