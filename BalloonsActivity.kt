package com.kidsplay

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import java.util.Random

class BalloonsView(c: Context) : View(c) {
    private class Balloon(var x: Float, var y: Float, val r: Float, val color: Int, val speed: Float, val side: Int)
    private class Pop(val x: Float, val y: Float, val color: Int, val r: Float, var age: Float = 0f)

    private val balloons = ArrayList<Balloon>()
    private val pops = ArrayList<Pop>()
    private val scores = IntArray(2)
    private val spawn = FloatArray(2)
    private val palette = listOf(
        "#EF5350", "#FFA726", "#FFEE58", "#66BB6A", "#42A5F5", "#AB47BC", "#EC407A"
    ).map { col(it) }
    private val rnd = Random()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val oval = RectF()
    private val total = 45f
    private var timeLeft = total
    private var last = 0L
    private var over = false
    private var overAt = 0L

    init {
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
    }

    private fun restart() {
        balloons.clear()
        pops.clear()
        scores[0] = 0
        scores[1] = 0
        spawn[0] = 0f
        spawn[1] = 0f
        timeLeft = total
        over = false
    }

    private fun addBalloon(side: Int, w: Float, h: Float) {
        val r = (w / 2f * 0.17f).coerceIn(context.dp(34).toFloat(), context.dp(72).toFloat())
        val minX = side * w / 2f + r
        val maxX = (side + 1) * w / 2f - r
        val x = if (maxX > minX) minX + rnd.nextFloat() * (maxX - minX) else (minX + maxX) / 2f
        val speed = h * (0.2f + rnd.nextFloat() * 0.15f)
        balloons.add(Balloon(x, h + r * 1.3f, r, palette[rnd.nextInt(palette.size)], speed, side))
    }

    private fun update(dt: Float, w: Float, h: Float) {
        timeLeft -= dt
        if (timeLeft <= 0f) {
            timeLeft = 0f
            over = true
            overAt = System.currentTimeMillis()
            balloons.clear()
            return
        }
        for (side in 0..1) {
            spawn[side] -= dt
            if (spawn[side] <= 0f) {
                spawn[side] = 0.5f + rnd.nextFloat() * 0.5f
                addBalloon(side, w, h)
            }
        }
        val it = balloons.iterator()
        while (it.hasNext()) {
            val b = it.next()
            b.y -= b.speed * dt
            if (b.y + b.r * 2.2f < 0f) it.remove()
        }
        val pi = pops.iterator()
        while (pi.hasNext()) {
            val p = pi.next()
            p.age += dt
            if (p.age > 0.35f) pi.remove()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val now = System.nanoTime()
        val dt = if (last == 0L) 0f else ((now - last) / 1e9f).coerceAtMost(0.05f)
        last = now
        if (!over) update(dt, w, h)

        // Фон
        paint.style = Paint.Style.FILL
        paint.color = col("#E3F2FD")
        canvas.drawRect(0f, 0f, w / 2f, h, paint)
        paint.color = col("#FFF3E0")
        canvas.drawRect(w / 2f, 0f, w, h, paint)
        paint.color = Color.WHITE
        canvas.drawRect(w / 2f - 4f, 0f, w / 2f + 4f, h, paint)

        // Шарики
        for (b in balloons) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f * resources.displayMetrics.density
            paint.color = col("#757575")
            canvas.drawLine(b.x, b.y + b.r * 1.05f, b.x, b.y + b.r * 2.1f, paint)
            paint.style = Paint.Style.FILL
            paint.color = b.color
            oval.set(b.x - b.r * 0.85f, b.y - b.r * 1.05f, b.x + b.r * 0.85f, b.y + b.r * 1.05f)
            canvas.drawOval(oval, paint)
            paint.color = Color.argb(110, 255, 255, 255)
            oval.set(b.x - b.r * 0.5f, b.y - b.r * 0.8f, b.x - b.r * 0.15f, b.y - b.r * 0.35f)
            canvas.drawOval(oval, paint)
        }

        // Хлопки
        for (p in pops) {
            val f = p.age / 0.35f
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 6f * resources.displayMetrics.density
            paint.color = p.color
            paint.alpha = (255 * (1f - f)).toInt().coerceIn(0, 255)
            canvas.drawCircle(p.x, p.y, p.r * (0.8f + f * 0.9f), paint)
            paint.alpha = 255
        }

        // Счёт и таймер
        val ts = minOf(h * 0.12f, w * 0.1f)
        paint.style = Paint.Style.FILL
        paint.textSize = ts
        paint.color = col("#1565C0")
        canvas.drawText(scores[0].toString(), w * 0.25f, ts * 1.4f, paint)
        paint.color = col("#E65100")
        canvas.drawText(scores[1].toString(), w * 0.75f, ts * 1.4f, paint)
        paint.color = col("#66BB6A")
        canvas.drawRect(0f, 0f, w * (timeLeft / total), 8f * resources.displayMetrics.density, paint)

        // Конец игры
        if (over) {
            paint.color = Color.argb(215, 255, 255, 255)
            canvas.drawRect(0f, 0f, w, h, paint)
            val winSide = if (scores[0] > scores[1]) 0 else if (scores[1] > scores[0]) 1 else -1
            for (side in 0..1) {
                val emoji = if (winSide == -1) "🤝" else if (winSide == side) "🏆" else "👏"
                val cx = w * (0.25f + 0.5f * side)
                paint.textSize = minOf(w / 2f * 0.4f, h * 0.3f)
                paint.color = Color.BLACK
                canvas.drawText(emoji, cx, h * 0.45f, paint)
                paint.textSize = ts
                paint.color = if (side == 0) col("#1565C0") else col("#E65100")
                canvas.drawText(scores[side].toString(), cx, h * 0.45f + ts * 1.5f, paint)
            }
            if (System.currentTimeMillis() - overAt > 1000) {
                paint.textSize = ts
                paint.color = Color.BLACK
                canvas.drawText("🔄", w / 2f, h * 0.88f, paint)
            }
        }

        postInvalidateOnAnimation()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val a = e.actionMasked
        if (a == MotionEvent.ACTION_DOWN || a == MotionEvent.ACTION_POINTER_DOWN) {
            val i = e.actionIndex
            val x = e.getX(i)
            val y = e.getY(i)
            if (over) {
                if (System.currentTimeMillis() - overAt > 1000) restart()
                return true
            }
            val pad = context.dp(10).toFloat()
            for (k in balloons.indices.reversed()) {
                val b = balloons[k]
                val rx = b.r * 0.85f + pad
                val ry = b.r * 1.05f + pad
                val nx = (x - b.x) / rx
                val ny = (y - b.y) / ry
                if (nx * nx + ny * ny <= 1f) {
                    balloons.removeAt(k)
                    scores[b.side]++
                    pops.add(Pop(b.x, b.y, b.color, b.r))
                    break
                }
            }
        }
        return true
    }
}

class BalloonsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goFullscreen()
        setContentView(withHome(BalloonsView(this)))
    }
}
