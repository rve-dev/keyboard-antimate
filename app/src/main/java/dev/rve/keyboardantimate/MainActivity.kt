package dev.rve.keyboardantimate

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences(AntimatePrefs.FILE, Context.MODE_PRIVATE) }
    private lateinit var sourceSpinner: Spinner
    private lateinit var targetSpinner: Spinner
    private lateinit var languages: List<LanguageOption>

    private data class LanguageOption(val code: String, val title: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSettings()
    }

    private fun showSettings() {
        languages = TranslateLanguage.getAllLanguages().map { code ->
            val locale = Locale.forLanguageTag(code)
            LanguageOption(code, "${locale.getDisplayName(Locale.getDefault())} · $code")
        }.sortedBy { it.title.lowercase(Locale.getDefault()) }

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
        root.addView(info("Uses the Android system sans-serif font automatically."))

        section(root, "Typing")
        addSwitch(root, "Automatic soft-word replacement", AntimatePrefs.AUTO_REPLACE, false,
            "Replace matching words from the built-in dictionary when you type a space or punctuation.")
        addSwitch(root, "Show replacement suggestions", AntimatePrefs.SHOW_SUGGESTIONS, true,
            "Show a softer alternative without changing your text.")
        addSwitch(root, "Key haptic feedback", AntimatePrefs.HAPTIC, true, "Use the device's keyboard vibration setting.")
        addSwitch(root, "Key sound", AntimatePrefs.SOUND, false, "Play a short sound when a key is pressed.")

        section(root, "Languages and translation")
        root.addView(info("Choose the language you want to translate from and to. The translator supports ${languages.size} languages; language models are downloaded when first needed."))
        root.addView(label("Translate from"))
        sourceSpinner = languageSpinner(AntimatePrefs.TRANSLATE_SOURCE, "en")
        root.addView(sourceSpinner, fullWidth())
        root.addView(label("Translate to"))
        targetSpinner = languageSpinner(AntimatePrefs.TRANSLATE_TARGET, "ru")
        root.addView(targetSpinner, fullWidth())
        root.addView(actionButton("Open translator") { showTranslatorDialog() })

        section(root, "AI replacement")
        root.addView(info("AI fallback is prepared as an extension point, but no AI API is connected yet. Typed text will not be sent to an AI service."))

        section(root, "Keyboard setup")
        root.addView(actionButton("Enable Antimate") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })
        root.addView(actionButton("Choose Antimate") {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.showInputMethodPicker()
        })

        root.addView(TextView(this).apply {
            text = "Clipboard history, T9, glide typing and voice input are not implemented in this build yet."
            textSize = 12f
            setTextColor(0xFF858891.toInt())
            setPadding(0, dp(22), 0, 0)
        })
        setContentView(scroll)
    }

    private fun languageSpinner(key: String, defaultCode: String): Spinner {
        return Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                languages.map { it.title }
            )
            val saved = prefs.getString(key, defaultCode) ?: defaultCode
            setSelection(languages.indexOfFirst { it.code == saved }.coerceAtLeast(0))
            onItemSelectedListener = SimpleItemSelectedListener { position ->
                prefs.edit().putString(key, languages[position].code).apply()
            }
        }
    }

    private fun showTranslatorDialog() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(4))
        }
        val input = EditText(this).apply {
            hint = "Enter text to translate"
            minLines = 3
            gravity = android.view.Gravity.TOP
        }
        val output = TextView(this).apply {
            text = "Translation will appear here."
            textSize = 15f
            setTextColor(0xFF202127.toInt())
            setPadding(0, dp(16), 0, dp(8))
        }
        val status = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFF666666.toInt())
        }
        content.addView(input, fullWidth())
        content.addView(status, fullWidth())
        content.addView(output, fullWidth())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Antimate Translator")
            .setView(content)
            .setNegativeButton("Close", null)
            .setPositiveButton("Translate", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val text = input.text.toString().trim()
                if (text.isEmpty()) {
                    input.error = "Enter text first"
                    return@setOnClickListener
                }
                val source = languages[sourceSpinner.selectedItemPosition].code
                val target = languages[targetSpinner.selectedItemPosition].code
                if (source == target) {
                    output.text = text
                    status.text = "Source and target languages are the same."
                    return@setOnClickListener
                }
                status.text = "Preparing language model…"
                output.text = ""
                translate(text, source, target, status, output)
            }
        }
        dialog.show()
    }

    private fun translate(text: String, source: String, target: String, status: TextView, output: TextView) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(source)
            .setTargetLanguage(target)
            .build()
        val translator: Translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder().build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                status.text = "Translating…"
                translator.translate(text)
                    .addOnSuccessListener { translated ->
                        output.text = translated
                        status.text = "Translation complete"
                        translator.close()
                    }
                    .addOnFailureListener { error ->
                        status.text = "Translation failed: ${error.localizedMessage ?: "unknown error"}"
                        translator.close()
                    }
            }
            .addOnFailureListener { error ->
                status.text = "Could not download language model: ${error.localizedMessage ?: "check your connection"}"
                translator.close()
            }
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

    private fun info(value: String) = TextView(this).apply {
        text = value
        textSize = 13f
        setTextColor(0xFFB7B9C0.toInt())
        setPadding(0, 0, 0, dp(12))
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
