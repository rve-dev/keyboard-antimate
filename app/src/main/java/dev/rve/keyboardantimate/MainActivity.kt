package dev.rve.keyboardantimate

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
            setBackgroundColor(0xFF101010.toInt())
        }

        val title = TextView(this).apply {
            text = "ANTIMATE"
            textSize = 28f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "A keyboard that helps you say it differently."
            textSize = 14f
            setTextColor(0xFFBDBDBD.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 28)
        }

        val enable = Button(this).apply {
            text = "1. Enable Antimate"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }
        val choose = Button(this).apply {
            text = "2. Choose keyboard"
            setOnClickListener {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.showInputMethodPicker()
            }
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(enable, matchWidth())
        root.addView(choose, matchWidth())
        setContentView(root)
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = 10 }
}
