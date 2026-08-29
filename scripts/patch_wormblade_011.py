from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCES = [
    ROOT / "app/src/main/java/com/valoon4/wormblade/MainActivity.kt",
    ROOT / "games/Wormblade/src/MainActivity.kt",
]

old_hide = '''    private fun hideSystemUi() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            run {
                window.decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }
    }
'''
new_hide = '''    @Suppress("DEPRECATION")
    private fun hideSystemUi() {
        // Keep startup compatible across old vendor Android builds as well as new ones.
        // The legacy flags are harmless on modern Android and avoid loading newer
        // WindowInsets controller APIs during Activity startup.
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
'''

old_field = '''    private var invulnUntil = 0f
    private var dragging = false
'''
new_field = '''    private var invulnUntil = 0f
    private var dragging = false
    private var fatalMessage: String? = null
'''

old_draw = '''    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        val now = System.nanoTime()
        if (lastFrameNs == 0L) lastFrameNs = now
        val dt = min(0.034f, (now - lastFrameNs) / 1_000_000_000f)
        lastFrameNs = now
        if (!paused && !gameOver && !victory) updateGame(dt)
        drawBackground(canvas)
        drawWorld(canvas)
        drawHud(canvas)
        if (paused) drawOverlay(canvas, "PAUSED", "Tap pause to continue")
        if (gameOver) drawOverlay(canvas, "GAME OVER", "Tap to restart")
        if (victory) drawOverlay(canvas, "LEVEL CLEAR!", "Tap to play again")
        postInvalidateOnAnimation()
    }
'''
new_draw = '''    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)

        // Some Android/vendor combinations can deliver an initial draw while the
        // surface is still effectively 0x0. Gradients require distinct endpoints,
        // so never enter the game renderer until we have a real surface size.
        if (width <= 1 || height <= 1) {
            canvas.drawColor(Color.rgb(32, 39, 48))
            postInvalidateOnAnimation()
            return
        }

        val previousFailure = fatalMessage
        if (previousFailure != null) {
            drawEmergencyScreen(canvas, previousFailure)
            return
        }

        try {
            val now = System.nanoTime()
            if (lastFrameNs == 0L) lastFrameNs = now
            val dt = min(0.034f, (now - lastFrameNs) / 1_000_000_000f)
            lastFrameNs = now
            if (!paused && !gameOver && !victory) updateGame(dt)
            drawBackground(canvas)
            drawWorld(canvas)
            drawHud(canvas)
            if (paused) drawOverlay(canvas, "PAUSED", "Tap pause to continue")
            if (gameOver) drawOverlay(canvas, "GAME OVER", "Tap to restart")
            if (victory) drawOverlay(canvas, "LEVEL CLEAR!", "Tap to play again")
            postInvalidateOnAnimation()
        } catch (t: Throwable) {
            fatalMessage = "${t.javaClass.simpleName}: ${t.message ?: "unknown renderer error"}"
            drawEmergencyScreen(canvas, fatalMessage!!)
        }
    }

    private fun drawEmergencyScreen(canvas: android.graphics.Canvas, message: String) {
        canvas.drawColor(Color.rgb(31, 39, 49))
        textPaint.style = Paint.Style.FILL
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.WHITE
        textPaint.textSize = max(28f, width * 0.055f)
        canvas.drawText("WORMBLADE", width * 0.5f, height * 0.40f, textPaint)
        textPaint.color = Color.rgb(255, 116, 116)
        textPaint.textSize = max(18f, width * 0.032f)
        canvas.drawText("Renderer abgefangen", width * 0.5f, height * 0.47f, textPaint)
        textPaint.color = Color.rgb(220, 230, 235)
        textPaint.textSize = max(14f, width * 0.024f)
        val safe = message.take(80)
        canvas.drawText(safe, width * 0.5f, height * 0.53f, textPaint)
    }
'''

for path in SOURCES:
    text = path.read_text(encoding="utf-8")
    for old, new, label in [
        (old_hide, new_hide, "startup UI compatibility"),
        (old_field, new_field, "fatal renderer state"),
        (old_draw, new_draw, "safe onDraw"),
    ]:
        if old not in text:
            raise SystemExit(f"{path}: expected block missing: {label}")
        text = text.replace(old, new, 1)
    path.write_text(text, encoding="utf-8")

build = ROOT / "app/build.gradle.kts"
text = build.read_text(encoding="utf-8")
if 'versionCode = 1' not in text or 'versionName = "0.1.0-debug"' not in text:
    raise SystemExit("Unexpected Wormblade version block")
text = text.replace('versionCode = 1', 'versionCode = 2', 1)
text = text.replace('versionName = "0.1.0-debug"', 'versionName = "0.1.1-debug"', 1)
build.write_text(text, encoding="utf-8")

readme = ROOT / "games/Wormblade/README.md"
text = readme.read_text(encoding="utf-8")
text = text.replace('Version: `0.1.0-debug`', 'Version: `0.1.1-debug`')
text += '\n## 0.1.1 startup hotfix\n\n- guards zero-sized first frames before creating gradients;\n- uses conservative immersive flags for wider Android/vendor compatibility;\n- catches renderer exceptions and shows a diagnostic screen instead of terminating the process.\n'
readme.write_text(text, encoding="utf-8")

print("Wormblade 0.1.1 startup hotfix applied")
