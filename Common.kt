package com.kidsplay

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast

fun col(s: String): Int = Color.parseColor(s)

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

fun Context.roundBg(color: Int, radiusDp: Int = 24): GradientDrawable {
    val g = GradientDrawable()
    g.setColor(color)
    g.cornerRadius = dp(radiusDp).toFloat()
    return g
}

@Suppress("DEPRECATION")
fun Activity.goFullscreen() {
    window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
}

fun Activity.bigButton(text: String, color: Int): Button {
    val b = Button(this)
    b.text = text
    b.textSize = 30f
    b.setTextColor(Color.WHITE)
    b.isAllCaps = false
    b.background = roundBg(color)
    return b
}

/** Кнопка "домой": выход только долгим нажатием, чтобы малыши не выходили случайно. */
fun Activity.homeButton(): Button {
    val act = this
    val b = Button(this)
    b.text = "🏠"
    b.textSize = 22f
    b.background = roundBg(col("#55000000"), 20)
    b.setOnClickListener {
        Toast.makeText(act, "Нажми и держи 🏠", Toast.LENGTH_SHORT).show()
    }
    b.setOnLongClickListener {
        act.finish()
        true
    }
    return b
}

fun Activity.withHome(content: View): FrameLayout {
    val root = FrameLayout(this)
    root.addView(content, FrameLayout.LayoutParams(-1, -1))
    val lp = FrameLayout.LayoutParams(dp(60), dp(60), Gravity.TOP or Gravity.END)
    lp.topMargin = dp(8)
    lp.rightMargin = dp(8)
    root.addView(homeButton(), lp)
    return root
}
