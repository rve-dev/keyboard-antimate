package dev.rve.keyboardantimate

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AntimateKeyboardService : InputMethodService() {
    private lateinit var suggestions: LinearLayout
    private val dark = Color.rgb(20, 20, 20)
    private val keyColor = Color.rgb(43, 43, 43)
    private val accent = Color.rgb(220, 45, 45)

    // First MVP dictionary. Suggestions are opt-in: the original word is never
    // replaced unless the user taps a suggestion.
    private val replacements = mapOf(
        "пиздец" to "пипец",
        "блять" to "блин",
        "блядь" to "блин",
        "сука" to "блин",
        "заебало" to "надоело",
        "заебал" to "достал",
        "хуёво" to "плохо",
        "хреново" to "плохо"
    )

    override fun onCreateInputView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(5, 7, 5, 7)
            setBackgroundColor(dark)
        }

        suggestions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        root.addView(suggestions, rowParams(42))

        listOf(
            listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х"),
            listOf("ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж", "э"),
            listOf("⇧", "я", "ч", "с", "м", "и", "т", "ь", "б", "ю", "⌫"),
            listOf("?123", ",", "☻", "РУС", ".", "↵")
        ).forEachIndexed { index, row ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            row.forEach { label ->
                val weight = if (label in listOf("?123", "РУС", "↵", "⇧", "⌫")) 1.35f else 1f
                line.addView(makeKey(label), LinearLayout.LayoutParams(0, dp(48), weight).apply {
                    setMargins(2, 3, 2, 3)
                })
            }
            root.addView(line, rowParams(if (index == 3) 50 else 51))
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        bottom.addView(makeKey("🌐"), LinearLayout.LayoutParams(0, dp(46), 1f).apply { setMargins(2, 3, 2, 3) })
        bottom.addView(makeKey("English / Русский"), LinearLayout.LayoutParams(0, dp(46), 5f).apply { setMargins(2, 3, 2, 3) })
        bottom.addView(makeKey("⏎"), LinearLayout.LayoutParams(0, dp(46), 1f).apply { setMargins(2, 3, 2, 3) })
        root.addView(bottom)

        refreshSuggestions()
        return root
    }

    private fun makeKey(label: String): Button = Button(this).apply {
        text = label
        textSize = if (label.length > 2) 11f else 17f
        isAllCaps = false
        setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT
        background = rounded(if (label in listOf("⌫", "↵", "⏎")) accent else keyColor)
        minWidth = 0
        minimumWidth = 0
        minHeight = 0
        minimumHeight = 0
        setPadding(0, 0, 0, 0)
        setOnClickListener { handleKey(label) }
    }

    private fun handleKey(key: String) {
        val connection = currentInputConnection ?: return
        when (key) {
            "⌫" -> {
                val selected = connection.getSelectedText(0)
                if (!selected.isNullOrEmpty()) connection.commitText("", 1)
                else connection.deleteSurroundingText(1, 0)
            }
            "↵", "⏎" -> connection.commitText("\n", 1)
            "РУС", "English / Русский", "🌐" -> { /* Layout switch is a later MVP task. */ }
            "⇧" -> { /* Shift state will be added with the layout settings. */ }
            "?123" -> { /* Numeric layout will be added in the next iteration. */ }
            "☻" -> connection.commitText("🙂", 1)
            "," , "." -> connection.commitText(key, 1)
            else -> connection.commitText(key, 1)
        }
        refreshSuggestions()
    }

    private fun refreshSuggestions() {
        if (!::suggestions.isInitialized) return
        suggestions.removeAllViews()
        val word = currentWord() ?: return
        val replacement = replacements[word.lowercase()] ?: return

        val original = TextView(this).apply {
            text = word
            textSize = 13f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(8, 0, 8, 0)
        }
        val arrow = TextView(this).apply {
            text = "→"
            textSize = 16f
            setTextColor(accent)
            gravity = Gravity.CENTER
        }
        val suggestion = TextView(this).apply {
            text = replacement.replaceFirstChar { if (word.firstOrNull()?.isUpperCase() == true) it.uppercase() else it.toString() }
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(12, 4, 12, 4)
            background = rounded(accent)
            setOnClickListener { applyReplacement(word, replacement) }
        }
        suggestions.addView(original, LinearLayout.LayoutParams(0, -1, 1f))
        suggestions.addView(arrow)
        suggestions.addView(suggestion, LinearLayout.LayoutParams(0, -1, 1f))
    }

    private fun currentWord(): String? {
        val before = currentInputConnection?.getTextBeforeCursor(64, 0)?.toString() ?: return null
        return Regex("[А-Яа-яЁё]+$").find(before)?.value
    }

    private fun applyReplacement(original: String, replacement: String) {
        val connection: InputConnection = currentInputConnection ?: return
        connection.deleteSurroundingText(original.length, 0)
        val result = if (original.firstOrNull()?.isUpperCase() == true) {
            replacement.replaceFirstChar { it.uppercase() }
        } else replacement
        connection.commitText(result, 1)
        refreshSuggestions()
    }

    private fun rounded(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(9).toFloat()
    }

    private fun rowParams(height: Int) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, dp(height)
    )

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
