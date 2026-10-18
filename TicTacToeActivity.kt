package com.kidsplay

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View

class TicTacToeView(c: Context) : View(c) {
    private val cells = IntArray(9) // 0 пусто, 1 крестик, 2 нолик
    private var turn = 1
    private var winner = 0 // 0 игра идёт, 1/2 победил, 3 ничья
    private var winLine: IntArray? = null
    private var endAt = 0L

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val xColor = col("#E53935")
    private val oColor = col("#1E88E5")
    private val lines = arrayOf(
        intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
        intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
        intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
    )

    private var size = 0f
    private var cell = 0f
    private var left = 0f
    private var top = 0f

    init {
        paint.strokeCap = Paint.Cap.ROUND
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
    }

    private fun layoutBoard() {
        val w = width.toFloat()
        val h = height.toFloat()
        size = minOf(w * 0.94f, h * 0.78f)
        cell = size / 3f
        left = (w - size) / 2f
        top = (h - size) * 0.7f
    }

    private fun drawMark(canvas: Canvas, mark: Int, cx: Float, cy: Float, s: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = s * 0.45f
        if (mark == 1) {
            paint.color = xColor
            canvas.drawLine(cx - s, cy - s, cx + s, cy + s, paint)
            canvas.drawLine(cx - s, cy + s, cx + s, cy - s, paint)
        } else {
            paint.color = oColor
            canvas.drawCircle(cx, cy, s, paint)
        }
    }

    private fun cx(i: Int) = left + (i % 3 + 0.5f) * cell
    private fun cy(i: Int) = top + (i / 3 + 0.5f) * cell

    override fun onDraw(canvas: Canvas) {
        layoutBoard()
        canvas.drawColor(col("#FFFDE7"))

        // Сетка
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10f * resources.displayMetrics.density / 2f
        paint.color = col("#455A64")
        for (k in 1..2) {
            canvas.drawLine(left + k * cell, top + 12f, left + k * cell, top + size - 12f, paint)
            canvas.drawLine(left + 12f, top + k * cell, left + size - 12f, top + k * cell, paint)
        }

        // Фигуры
        for (i in 0 until 9) {
            if (cells[i] != 0) drawMark(canvas, cells[i], cx(i), cy(i), cell * 0.28f)
        }

        // Линия победы
        val wl = winLine
        if (wl != null) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = cell * 0.12f
            paint.color = col("#43A047")
            canvas.drawLine(cx(wl[0]), cy(wl[0]), cx(wl[2]), cy(wl[2]), paint)
        }

        // Статус сверху: чей ход / кто победил
        val sy = top / 2f
        val ss = minOf(top * 0.28f, width * 0.07f)
        val mx = width / 2f
        when (winner) {
            0 -> drawMark(canvas, turn, mx, sy, ss)
            1, 2 -> {
                drawMark(canvas, winner, mx - ss * 1.6f, sy, ss)
                paint.style = Paint.Style.FILL
                paint.color = col("#000000")
                paint.textSize = ss * 2.2f
                canvas.drawText("🏆", mx + ss * 1.6f, sy + ss * 0.8f, paint)
            }
            else -> {
                drawMark(canvas, 1, mx - ss * 1.8f, sy, ss)
                drawMark(canvas, 2, mx + ss * 1.8f, sy, ss)
            }
        }
    }

    private fun checkWin() {
        for (l in lines) {
            val a = cells[l[0]]
            if (a != 0 && a == cells[l[1]] && a == cells[l[2]]) {
                winner = a
                winLine = l
                endAt = SystemClock.uptimeMillis()
                return
            }
        }
        if (cells.all { it != 0 }) {
            winner = 3
            endAt = SystemClock.uptimeMillis()
        }
    }

    private fun reset() {
        cells.fill(0)
        turn = 1
        winner = 0
        winLine = null
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.actionMasked == MotionEvent.ACTION_DOWN) {
            if (winner != 0) {
                if (SystemClock.uptimeMillis() - endAt > 700) {
                    reset()
                    invalidate()
                }
                return true
            }
            val x = e.x - left
            val y = e.y - top
            if (x >= 0f && y >= 0f && x < size && y < size) {
                val idx = (y / cell).toInt() * 3 + (x / cell).toInt()
                if (idx in 0..8 && cells[idx] == 0) {
                    cells[idx] = turn
                    checkWin()
                    if (winner == 0) turn = if (turn == 1) 2 else 1
                    invalidate()
                }
            }
        }
        return true
    }
}

class TicTacToeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goFullscreen()
        setContentView(withHome(TicTacToeView(this)))
    }
}
