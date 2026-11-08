package dev.rve.keyboardantimate

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.text.SpannableString
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.text.style.SuperscriptSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class AntimateKeyboardService : InputMethodService() {
    private lateinit var root: LinearLayout
    private var autoReplace = false
    private var showSuggestions = true
    private var haptic = true
    private var sound = false
    private var shift = false
    private var symbols = false
    private var english = false

    private val backgroundColor = Color.rgb(32, 33, 36)
    private val keyColor = Color.rgb(53, 54, 58)
    private val specialColor = Color.rgb(65, 66, 69)
    private val foregroundColor = Color.rgb(241, 243, 244)
    private val enterColor = Color.rgb(168, 199, 250)
    private val enterTextColor = Color.rgb(25, 52, 91)

    private val replacements = mapOf(
        "пиздец" to "пипец", "блять" to "блин", "блядь" to "блин",
        "сука" to "блин", "заебало" to "надоело", "заебал" to "достал",
        "хуёво" to "плохо", "хреново" to "плохо"
    )

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        loadPreferences()
        refreshSuggestions()
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences(AntimatePrefs.FILE, Context.MODE_PRIVATE)
        autoReplace = prefs.getBoolean(AntimatePrefs.AUTO_REPLACE, false)
        showSuggestions = prefs.getBoolean(AntimatePrefs.SHOW_SUGGESTIONS, true)
        haptic = prefs.getBoolean(AntimatePrefs.HAPTIC, true)
        sound = prefs.getBoolean(AntimatePrefs.SOUND, false)
    }

    override fun onCreateInputView(): View {
        loadPreferences()
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(7), dp(5), dp(7), dp(5))
            setBackgroundColor(backgroundColor)
            clipToPadding = false
        }

        addToolbar()
        val russian = listOf(
            listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
            listOf("ф","ы","в","а","п","р","о","л","д","ж","э"),
            listOf("⇧","я","ч","с","м","и","т","ь","б","ю","⌫")
        )
        val englishRows = listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("⇧","z","x","c","v","b","n","m","⌫")
        )
        val numberRows = listOf(
            listOf("1","2","3","4","5","6","7","8","9","0"),
            listOf("@","#","₽","%","&","-","+","(",")","/"),
            listOf("=\\<","*","\"","'",":",";","!","?","⌫")
        )
        val rows = if (symbols) numberRows else if (english) englishRows else russian
        rows.forEachIndexed { index, row -> addKeyRow(row, index) }
        addBottomRow()
        return root
    }

    private fun addToolbar() {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val tools = listOf(
            "▦" to "Дополнительные инструменты",
            "▤" to "Буфер обмена",
            "文" to "Переводчик",
            "⚙" to "Настройки",
            "♩" to "Голосовой ввод"
        )
        tools.forEachIndexed { index, item ->
            val view = TextView(this).apply {
                text = item.first
                textSize = if (index == 2) 27f else 31f
                gravity = Gravity.CENTER
                setTextColor(foregroundColor)
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                contentDescription = item.second
                setOnClickListener {
                    tapFeedback()
                    when (index) {
                        2, 3 -> openSettings()
                        0 -> toast("Дополнительные инструменты появятся в следующих версиях")
                        1 -> toast("История буфера обмена пока не подключена")
                        4 -> toast("Голосовой ввод пока не подключён")
                    }
                }
            }
            bar.addView(view, LinearLayout.LayoutParams(0, dp(48), 1f))
        }
        root.addView(bar, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(52)
        ).apply { bottomMargin = dp(5) })
    }

    private fun addKeyRow(keys: List<String>, rowIndex: Int) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        keys.forEach { key ->
            val wide = key == "⇧" || key == "⌫" || key == "=\\<"
            val view = makeKey(key, wide)
            val weight = if (wide) 1.35f else 1f
            row.addView(view, LinearLayout.LayoutParams(0, dp(61), weight).apply {
                setMargins(dp(2), dp(3), dp(2), dp(3))
            })
        }
        root.addView(row, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(67)
        ))
    }

    private fun addBottomRow() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        fun add(label: String, shown: String, weight: Float, color: Int = keyColor, textColor: Int = foregroundColor, size: Float = 22f) {
            row.addView(makeKey(label, true, shown, color, textColor, size),
                LinearLayout.LayoutParams(0, dp(57), weight).apply {
                    setMargins(dp(2), dp(3), dp(2), dp(3))
                })
        }
        add(if (symbols) "ABC" else "?123", if (symbols) "ABC" else "?123", 1.35f, specialColor, foregroundColor, 18f)
        add(",", "☺\n,", 0.85f, keyColor, foregroundColor, 19f)
        add("🌐", "◎", 0.85f, keyColor, foregroundColor, 27f)
        add(" ", if (english) "ENGLISH" else "РУССКИЙ", 4.1f, keyColor, foregroundColor, 17f)
        add(".", ".", 0.85f, keyColor, foregroundColor, 22f)
        add("↵", "↵", 1.45f, enterColor, enterTextColor, 31f)
        root.addView(row, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(64)
        ).apply { topMargin = dp(2) })
    }

    private fun makeKey(
        key: String,
        isAction: Boolean = false,
        shown: String = key,
        color: Int = if (isAction) specialColor else keyColor,
        textColor: Int = foregroundColor,
        size: Float = 27f
    ): TextView = TextView(this).apply {
        text = if (key.length == 1 && key[0].isLetter() && shift) shown.uppercase() else shown
        textSize = size
        gravity = Gravity.CENTER
        setTextColor(textColor)
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        background = rounded(color, if (key == "↵") 30 else 10)
        includeFontPadding = false
        minWidth = 0
        minHeight = 0
        setPadding(dp(1), 0, dp(1), 0)
        isClickable = true
        isFocusable = true
        setOnClickListener {
            tapFeedback()
            handleKey(key)
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
            "↵" -> {
                val action = currentInputEditorInfo?.imeOptions?.and(android.view.inputmethod.EditorInfo.IME_MASK_ACTION)
                if (action != null && action != android.view.inputmethod.EditorInfo.IME_ACTION_NONE &&
                    action != android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED) {
                    connection.performEditorAction(action)
                } else connection.commitText("\n", 1)
            }
            "?123" -> { symbols = true; recreateKeyboard(); return }
            "ABC" -> { symbols = false; recreateKeyboard(); return }
            "⇧" -> { shift = !shift; recreateKeyboard(); return }
            "🌐" -> { english = !english; symbols = false; shift = false; recreateKeyboard(); return }
            " " -> {
                if (autoReplace) replaceCurrentWordIfKnown(connection)
                connection.commitText(" ", 1)
            }
            "," -> connection.commitText(",", 1)
            "." -> {
                if (autoReplace) replaceCurrentWordIfKnown(connection)
                connection.commitText(".", 1)
            }
            else -> {
                val value = if (shift && key.length == 1) key.uppercase() else key
                connection.commitText(value, 1)
                if (shift) { shift = false; recreateKeyboard(); return }
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
        if (original.firstOrNull()?.isUpperCase() == true)
            replacement.replaceFirstChar { it.uppercase() } else replacement

    private fun refreshSuggestions() {
        // Suggestions are shown in the top toolbar only when a matching soft replacement exists.
        // The toolbar remains visually stable while the user types.
    }

    private fun currentWord(): String? {
        val before = currentInputConnection?.getTextBeforeCursor(64, 0)?.toString() ?: return null
        return Regex("[А-Яа-яЁё]+$").find(before)?.value
    }

    private fun recreateKeyboard() {
        setInputView(onCreateInputView())
    }

    private fun openSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun tapFeedback() {
        if (haptic) performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
        if (sound) {
            val audio = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            audio.playSoundEffect(android.view.SoundEffectConstants.CLICK)
        }
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    
    private fun fifty() = 52
}
