package com.kidsplay

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(col("#FFF8E1"))
        root.setPadding(dp(12), dp(12), dp(12), dp(12))

        fun addButton(text: String, color: String, target: Class<*>) {
            val b = bigButton(text, col(color))
            b.setOnClickListener { startActivity(Intent(this@MainActivity, target)) }
            val lp = LinearLayout.LayoutParams(-1, 0, 1f)
            lp.setMargins(dp(8), dp(8), dp(8), dp(8))
            root.addView(b, lp)
        }

        addButton("🎨\nРисовалка", "#EF5350", DrawActivity::class.java)
        addButton("🎈\nЛопни шарики", "#42A5F5", BalloonsActivity::class.java)
        addButton("❌⭕\nКрестики-нолики", "#66BB6A", TicTacToeActivity::class.java)

        setContentView(root)
    }
}
