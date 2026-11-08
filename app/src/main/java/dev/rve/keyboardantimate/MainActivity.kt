package dev.rve.keyboardantimate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences(AntimatePrefs.FILE, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSettings()
    }

    private fun showSettings() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(28))
            setBackgroundColor(0xFF101114.toInt())
        }
        scroll.addView(root)

        root.addView(TextView(this).apply {
            text = "ANTIMATE"
            textSize = 28f
            setTextColor(0xFFFFFFFF.toInt())
            typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "Keyboard settings"
            textSize = 14f
            setTextColor(0xFFB7B9C0.toInt())
            setPadding(0, dp(5), 0, dp(22))
        })

        section(root, "Appearance")
        val themes = listOf("System", "Light", "Dark", "AMOLED", "Ocean", "Crimson", "Forest")
        val themeSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, themes)
            setSelection(themes.indexOf(prefs.getString(AntimatePrefs.THEME, "Dark")).coerceAtLeast(0))
            onItemSelectedListener = SimpleItemSelectedListener { position ->
                prefs.edit().putString(AntimatePrefs.THEME, themes[position]).apply()
            }
        }
        root.addView(label("Keyboard theme"))
        root.addView(themeSpinner, fullWidth())

        root.addView(label("Font"))
        root.addView(TextView(this).apply {
            text = "Uses the Android system sans-serif font automatically."
            textSize = 13f
            setTextColor(0xFFB7B9C0.toInt())
            setPadding(0, 0, 0, dp(12))
        })

        section(root, "Typing")
        addSwitch(root, "Automatic soft-word replacement", AntimatePrefs.AUTO_REPLACE, false,
            "Replace matching words from the built-in dictionary when you type a space or punctuation.")
        addSwitch(root, "Show replacement suggestions", AntimatePrefs.SHOW_SUGGESTIONS, true,
            "Show a softer alternative without changing your text.")
        addSwitch(root, "Key haptic feedback", AntimatePrefs.HAPTIC, true, "Use the device's keyboard vibration setting.")
        addSwitch(root, "Key sound", AntimatePrefs.SOUND, false, "Play a short sound when a key is pressed.")

        section(root, "AI replacement")
        root.addView(TextView(this).apply {
            text = "AI fallback is prepared as an extension point, but no API is connected yet. Typed text will not be sent anywhere."
            textSize = 13f
            setTextColor(0xFFB7B9C0.toInt())
            setPadding(0, 0, 0, dp(14))
        })

        section(root, "Keyboard setup")
        root.addView(actionButton("Enable Antimate") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })
        root.addView(actionButton("Choose Antimate") {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.showInputMethodPicker()
        })

        root.addView(TextView(this).apply {
            text = "Antimate is an experimental keyboard. Clipboard history, T9, glide typing, voice input and cloud AI are not implemented in this build yet."
            textSize = 12f
            setTextColor(0xFF858891.toInt())
            setPadding(0, dp(22), 0, 0)
        })
        setContentView(scroll)
    }

    private fun section(root: LinearLayout, title: String) {
        root.addView(TextView(this).apply {
            text = title.uppercase()
            textSize = 12f
            letterSpacing = 0.08f
            setTextColor(0xFFE34B55.toInt())
            setPadding(0, dp(18), 0, dp(8))
        })
    }

    private fun label(value: String) = TextView(this).apply {
        text = value
        textSize = 14f
        setTextColor(0xFFFFFFFF.toInt())
        setPadding(0, dp(8), 0, dp(4))
    }

    private fun addSwitch(root: LinearLayout, title: String, key: String, default: Boolean, summary: String) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        val toggle = Switch(this).apply {
            text = title
            textSize = 15f
            setTextColor(0xFFFFFFFF.toInt())
            isChecked = prefs.getBoolean(key, default)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(key, checked).apply()
            }
        }
        container.addView(toggle)
        container.addView(TextView(this).apply {
            text = summary
            textSize = 12f
            setTextColor(0xFF9A9DA6.toInt())
            setPadding(0, dp(3), 0, 0)
        })
        root.addView(container, fullWidth())
    }

    private fun actionButton(title: String, action: () -> Unit) = Button(this).apply {
        text = title
        isAllCaps = false
        setOnClickListener { action() }
    }

    private fun fullWidth() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
