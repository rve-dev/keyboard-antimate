package dev.rve.keyboardantimate

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AntimateKeyboardService : InputMethodService() {
    private lateinit var suggestions: LinearLayout
    private var themeName: String = "Dark"
    private var autoReplace = false
    private var showSuggestions = true
    private var haptic = true
    private var sound = false
    private var shift = false
    private var symbols = false

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

    private val colors: Triple<Int, Int, Int>
        get() = when (themeName) {
            "Light" -> Triple(Color.rgb(242, 243, 246), Color.WHITE, Color.rgb(210, 45, 58))
            "AMOLED" -> Triple(Color.BLACK, Color.rgb(25, 25, 25), Color.rgb(230, 50, 65))
            "Ocean" -> Triple(Color.rgb(10, 24, 38), Color.rgb(24, 48, 67), Color.rgb(48, 155, 210))
            "Crimson" -> Triple(Color.rgb(28, 12, 16), Color.rgb(55, 24, 31), Color.rgb(225, 55, 75))
            "Forest" -> Triple(Color.rgb(14, 28, 22), Color.rgb(30, 52, 41), Color.rgb(80, 165, 112))
            "System" -> if ((resources.configuration.uiMode and 0x30) == 0x20)
                Triple(Color.rgb(18, 18, 20), Color.rgb(42, 42, 46), Color.rgb(190, 190, 195))
            else Triple(Color.rgb(238, 239, 242), Color.WHITE, Color.rgb(65, 105, 180))
            else -> Triple(Color.rgb(20, 20, 23), Color.rgb(45, 45, 49), Color.rgb(220, 55, 68))
        }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        loadPreferences()
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences(AntimatePrefs.FILE, Context.MODE_PRIVATE)
        themeName = prefs.getString(AntimatePrefs.THEME, "Dark") ?: "Dark"
        autoReplace = prefs.getBoolean(AntimatePrefs.AUTO_REPLACE, false)
        showSuggestions = prefs.getBoolean(AntimatePrefs.SHOW_SUGGESTIONS, true)
        haptic = prefs.getBoolean(AntimatePrefs.HAPTIC, true)
        sound = prefs.getBoolean(AntimatePrefs.SOUND, false)
    }

    override fun onCreateInputView(): View {
        loadPreferences()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(5), dp(6), dp(5), dp(5))
            setBackgroundColor(colors.first)
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        suggestions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        toolbar.addView(suggestions, LinearLayout.LayoutParams(0, dp(42), 1f))
        toolbar.addView(makeKey("⋮", isAction = true), LinearLayout.LayoutParams(dp(44), dp(40)))
        root.addView(toolbar, rowParams(44))

        val letterRows = listOf(
            listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х"),
            listOf("ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж", "э"),
            listOf("⇧", "я", "ч", "с", "м", "и", "т", "ь", "б", "ю", "⌫")
        )
        val symbolRows = listOf(
            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
            listOf("@", "#", "₽", "%", "&", "-", "+", "(", ")", "/"),
            listOf("=\\<", "*", "\" ", "'", ":", ";", "!", "?", "⌫")
        )
        (if (symbols) symbolRows else letterRows).forEach { row ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            row.forEach { label ->
                val weight = if (label in listOf("⇧", "⌫", "=\\<")) 1.35f else 1f
                line.addView(makeKey(label, isAction = label in listOf("⇧", "⌫", "=\\<")),
                    LinearLayout.LayoutParams(0, dp(49), weight).apply {
                        setMargins(dp(2), dp(3), dp(2), dp(3))
                    })
            }
            root.addView(line, rowParams(52))
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        bottom.addView(makeKey(if (symbols) "ABC" else "?123", isAction = true),
            LinearLayout.LayoutParams(0, dp(46), 1.35f).apply { setMargins(dp(2), dp(3), dp(2), dp(3)) })
        bottom.addView(makeKey(",", isAction = true),
            LinearLayout.LayoutParams(0, dp(46), 0.8f).apply { setMargins(dp(2), dp(3), dp(2), dp(3)) })
        bottom.addView(makeKey("Русский · English", isAction = true),
            LinearLayout.LayoutParams(0, dp(46), 4.2f).apply { setMargins(dp(2), dp(3), dp(2), dp(3)) })
        bottom.addView(makeKey(".", isAction = true),
            LinearLayout.LayoutParams(0, dp(46), 0.8f).apply { setMargins(dp(2), dp(3), dp(2), dp(3)) })
        bottom.addView(makeKey("↵", isAction = true),
            LinearLayout.LayoutParams(0, dp(46), 1.25f).apply { setMargins(dp(2), dp(3), dp(2), dp(3)) })
        root.addView(bottom)

        refreshSuggestions()
        return root
    }

    private fun makeKey(label: String, isAction: Boolean = false): Button = Button(this).apply {
        text = if (shift && label.length == 1 && label[0].isLetter()) label.uppercase() else label
        textSize = if (label.length > 3) 11f else 17f
        isAllCaps = false
        setTextColor(if (themeName == "Light") Color.rgb(25, 25, 28) else Color.WHITE)
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        background = rounded(if (isAction) colors.third else colors.second)
        minWidth = 0
        minimumWidth = 0
        minHeight = 0
        minimumHeight = 0
        stateListAnimator = null
        setPadding(0, 0, 0, 0)
        setOnClickListener {
            if (haptic) performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            if (sound) {
                val audio = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
                audio.playSoundEffect(android.view.SoundEffectConstants.CLICK)
            }
            handleKey(label)
        }
    }

    private fun handleKey(key: String) {
        val connection = currentInputConnection ?: return
        when (key) {
            "⌫" -> {
                val selected = connection.getSelectedText(0)
                if (!selected.isNullOrEmpty()) connection.commitText("", 1)
                else connection.deleteSurroundingText(1, 0)
            }
            "↵" -> connection.commitText("\n", 1)
            "⋮" -> {
                val intent = android.content.Intent(this, MainActivity::class.java)
                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            "?123" -> { symbols = true; recreateKeyboard() }
            "ABC" -> { symbols = false; recreateKeyboard() }
            "⇧" -> { shift = !shift; recreateKeyboard() }
            "Русский · English" -> { /* Language switching is planned for the next iteration. */ }
            else -> {
                if (key == " " || key in listOf(",", ".", "!", "?", ":", ";")) {
                    if (autoReplace) replaceCurrentWordIfKnown(connection)
                    connection.commitText(key, 1)
                } else {
                    val value = if (shift && key.length == 1) key.uppercase() else key
                    connection.commitText(value, 1)
                    if (shift) shift = false
                }
            }
        }
        refreshSuggestions()
    }

    private fun replaceCurrentWordIfKnown(connection: InputConnection) {
        val word = currentWord() ?: return
        val replacement = replacements[word.lowercase()] ?: return
        connection.deleteSurroundingText(word.length, 0)
        connection.commitText(matchCase(word, replacement), 1)
    }

    private fun matchCase(original: String, replacement: String): String =
        if (original.firstOrNull()?.isUpperCase() == true) replacement.replaceFirstChar { it.uppercase() } else replacement

    private fun refreshSuggestions() {
        if (!::suggestions.isInitialized) return
        suggestions.removeAllViews()
        if (!showSuggestions) return
        val word = currentWord() ?: return
        val replacement = replacements[word.lowercase()] ?: return

        val original = TextView(this).apply {
            text = word
            textSize = 13f
            setTextColor(if (themeName == "Light") Color.DKGRAY else Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(dp(8), 0, dp(8), 0)
        }
        val arrow = TextView(this).apply {
            text = "→"
            textSize = 16f
            setTextColor(colors.third)
            gravity = Gravity.CENTER
        }
        val suggestion = TextView(this).apply {
            text = matchCase(word, replacement)
            textSize = 14f
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(4), dp(12), dp(4))
            background = rounded(colors.third)
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
        connection.commitText(matchCase(original, replacement), 1)
        refreshSuggestions()
    }

    private fun recreateKeyboard() {
        setInputView(onCreateInputView())
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
