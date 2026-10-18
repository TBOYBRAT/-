package com.kidsplay

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class DrawView(c: Context) : View(c) {
    private class Stroke(val path: Path, val paint: Paint)

    private val strokes = ArrayList<Stroke>()
    private var current: Stroke? = null
    var color: Int = Color.BLACK
    var strokeW: Float = 16f

    fun clear() {
        strokes.clear()
        invalidate()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val p = Path()
                p.moveTo(e.x, e.y)
                p.lineTo(e.x + 0.1f, e.y)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.style = Paint.Style.STROKE
                paint.strokeCap = Paint.Cap.ROUND
                paint.strokeJoin = Paint.Join.ROUND
                paint.color = color
                paint.strokeWidth = strokeW
                val s = Stroke(p, paint)
                strokes.add(s)
                current = s
            }
            MotionEvent.ACTION_MOVE -> {
                val s = current
                if (s != null) {
                    for (i in 0 until e.historySize) {
                        s.path.lineTo(e.getHistoricalX(i), e.getHistoricalY(i))
                    }
                    s.path.lineTo(e.x, e.y)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> current = null
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.WHITE)
        for (s in strokes) canvas.drawPath(s.path, s.paint)
    }
}

class DrawActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goFullscreen()

        val draw = DrawView(this)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(col("#ECEFF1"))
        root.addView(withHome(draw), LinearLayout.LayoutParams(-1, 0, 1f))

        fun rowParams(): LinearLayout.LayoutParams {
            val lp = LinearLayout.LayoutParams(0, dp(56), 1f)
            lp.setMargins(dp(4), dp(4), dp(4), dp(4))
            return lp
        }

        // Ряд цветов
        val colors = listOf(
            "#212121", "#E53935", "#FB8C00", "#FDD835",
            "#43A047", "#1E88E5", "#8E24AA", "#6D4C41"
        )
        val row1 = LinearLayout(this)
        row1.orientation = LinearLayout.HORIZONTAL
        for (c in colors) {
            val v = View(this)
            val g = GradientDrawable()
            g.shape = GradientDrawable.OVAL
            g.setColor(col(c))
            g.setStroke(dp(2), col("#55000000"))
            v.background = g
            v.setOnClickListener { draw.color = col(c) }
            row1.addView(v, rowParams())
        }
        root.addView(row1, LinearLayout.LayoutParams(-1, -2))

        // Ряд инструментов
        val row2 = LinearLayout(this)
        row2.orientation = LinearLayout.HORIZONTAL

        fun tool(text: String, size: Float, action: () -> Unit) {
            val b = Button(this)
            b.text = text
            b.textSize = size
            b.isAllCaps = false
            b.setTextColor(Color.BLACK)
            b.background = roundBg(col("#CFD8DC"), 14)
            b.setOnClickListener { action() }
            row2.addView(b, rowParams())
        }

        tool("•", 12f) { draw.strokeW = dp(6).toFloat() }
        tool("●", 22f) { draw.strokeW = dp(14).toFloat() }
        tool("⬤", 34f) { draw.strokeW = dp(30).toFloat() }
        tool("🧽", 24f) { draw.color = Color.WHITE }
        tool("🗑️", 24f) { draw.clear() }
        root.addView(row2, LinearLayout.LayoutParams(-1, -2))

        draw.strokeW = dp(14).toFloat()
        setContentView(root)
    }
}
