package dev.rve.keyboardantimate

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
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
    private lateinit var sourceLabel: LinearLayout
    private lateinit var targetLabel: LinearLayout
    private lateinit var languages: List<LanguageOption>
    private val page = Color.rgb(32, 33, 36)
    private val card = Color.rgb(42, 43, 47)
    private val key = Color.rgb(55, 56, 60)
    private val white = Color.rgb(241, 243, 244)
    private val muted = Color.rgb(174, 177, 183)
    private val accent = Color.rgb(168, 199, 250)
    private val themes = listOf("System", "Light", "Dark", "AMOLED", "Ocean", "Crimson", "Forest")

    private data class LanguageOption(val code: String, val title: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languages = TranslateLanguage.getAllLanguages().map { code ->
            val locale = Locale.forLanguageTag(code)
            LanguageOption(code, "${locale.getDisplayName(Locale.getDefault())} · $code")
        }.sortedBy { it.title.lowercase(Locale.getDefault()) }
        showSettings()
    }

    private fun showSettings() {
        val scroll = ScrollView(this).apply { setBackgroundColor(page) }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(32))
        }
        scroll.addView(root)

        root.addView(TextView(this).apply {
            text = "ANTIMATE"
            textSize = 29f
            letterSpacing = 0.08f
            setTextColor(white)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
        root.addView(TextView(this).apply {
            text = "Keyboard"
            textSize = 15f
            setTextColor(muted)
            setPadding(0, dp(4), 0, dp(22))
        })

        section(root, "Appearance")
        val themeRow = settingRow("Keyboard theme", prefs.getString(AntimatePrefs.THEME, "Dark") ?: "Dark") {
            AlertDialog.Builder(this)
                .setTitle("Keyboard theme")
                .setSingleChoiceItems(themes.toTypedArray(), themes.indexOf(prefs.getString(AntimatePrefs.THEME, "Dark")).coerceAtLeast(0)) { dialog, which ->
                    prefs.edit().putString(AntimatePrefs.THEME, themes[which]).apply()
                    dialog.dismiss()
                    showSettings()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        root.addView(themeRow)

        section(root, "Typing")
        addSwitch(root, "Automatic soft-word replacement", AntimatePrefs.AUTO_REPLACE, false,
            "Replace selected strong words with softer alternatives as you type.")
        addSwitch(root, "Replacement suggestions", AntimatePrefs.SHOW_SUGGESTIONS, true,
            "Show optional alternatives without changing your text automatically.")
        addSwitch(root, "Key haptic feedback", AntimatePrefs.HAPTIC, true,
            "Use the system keyboard vibration response.")
        addSwitch(root, "Key sound", AntimatePrefs.SOUND, false,
            "Play a short system click when a key is pressed.")

        section(root, "Languages and translation")
        root.addView(TextView(this).apply {
            text = "Offline translation powered by Google ML Kit. Models download when first needed."
            textSize = 13f
            setTextColor(muted)
            setPadding(dp(2), 0, dp(2), dp(12))
        })
        val sourceCode = prefs.getString(AntimatePrefs.TRANSLATE_SOURCE, "en") ?: "en"
        val targetCode = prefs.getString(AntimatePrefs.TRANSLATE_TARGET, "ru") ?: "ru"
        sourceLabel = settingRow("Translate from", languageTitle(sourceCode)) {
            chooseLanguage(AntimatePrefs.TRANSLATE_SOURCE, "Translate from")
        }
        targetLabel = settingRow("Translate to", languageTitle(targetCode)) {
            chooseLanguage(AntimatePrefs.TRANSLATE_TARGET, "Translate to")
        }
        root.addView(sourceLabel)
        root.addView(targetLabel)
        root.addView(actionRow("Open translator", "Translate text offline") { showTranslatorDialog() })

        section(root, "Keyboard setup")
        root.addView(actionRow("Enable Antimate", "Open Android keyboard settings") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        })
        root.addView(actionRow("Choose Antimate", "Select it as your current keyboard") {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.showInputMethodPicker()
        })

        section(root, "About this build")
        root.addView(TextView(this).apply {
            text = "Clipboard history, T9, glide typing and voice input are not connected yet. Translation works after downloading the required language models."
            textSize = 12f
            setTextColor(muted)
            setPadding(dp(3), dp(3), dp(3), dp(10))
        })
        setContentView(scroll)
    }

    private fun chooseLanguage(prefKey: String, title: String) {
        val current = prefs.getString(prefKey, if (prefKey == AntimatePrefs.TRANSLATE_SOURCE) "en" else "ru")
        val selected = languages.indexOfFirst { it.code == current }.coerceAtLeast(0)
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(languages.map { it.title }.toTypedArray()) { _, which ->
                prefs.edit().putString(prefKey, languages[which].code).apply()
                showSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun languageTitle(code: String): String =
        languages.firstOrNull { it.code == code }?.title ?: code

    private fun showTranslatorDialog() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), dp(4))
        }
        val input = EditText(this).apply {
            hint = "Enter text to translate"
            minLines = 3
            gravity = Gravity.TOP
        }
        val output = TextView(this).apply {
            text = "Translation will appear here."
            textSize = 15f
            setTextColor(Color.WHITE)
            setPadding(0, dp(16), 0, dp(8))
        }
        val status = TextView(this).apply {
            textSize = 12f
            setTextColor(muted)
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
                val source = prefs.getString(AntimatePrefs.TRANSLATE_SOURCE, "en") ?: "en"
                val target = prefs.getString(AntimatePrefs.TRANSLATE_TARGET, "ru") ?: "ru"
                if (source == target) {
                    output.text = text
                    status.text = "Source and target languages are the same."
                } else {
                    status.text = "Preparing language model…"
                    output.text = ""
                    translate(text, source, target, status, output)
                }
            }
        }
        dialog.show()
    }

    private fun translate(text: String, source: String, target: String, status: TextView, output: TextView) {
        val options = TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(target).build()
        val translator: Translator = Translation.getClient(options)
        translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
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
                status.text = "Could not download model: ${error.localizedMessage ?: "check your connection"}"
                translator.close()
            }
    }

    private fun section(root: LinearLayout, title: String) {
        root.addView(TextView(this).apply {
            text = title.uppercase(Locale.getDefault())
            textSize = 11f
            letterSpacing = 0.12f
            setTextColor(accent)
            setPadding(dp(2), dp(20), 0, dp(9))
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
    }

    private fun settingRow(title: String, value: String, action: () -> Unit): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = rounded(card, 15)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        row.addView(TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(white)
        })
        row.addView(TextView(this).apply {
            text = value
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(4), 0, 0)
        })
        return row
    }

    private fun actionRow(title: String, subtitle: String, action: () -> Unit): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(14), dp(15), dp(14))
            background = rounded(key, 14)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        row.addView(TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(white)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        })
        row.addView(TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(3), 0, 0)
        })
        return row
    }

    private fun addSwitch(root: LinearLayout, title: String, prefKey: String, default: Boolean, summary: String) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(12), dp(15), dp(12))
            background = rounded(card, 14)
        }
        val toggle = Switch(this).apply {
            text = title
            textSize = 14f
            setTextColor(white)
            isChecked = prefs.getBoolean(prefKey, default)
            thumbTintList = ColorStateList.valueOf(if (isChecked) accent else muted)
            trackTintList = ColorStateList.valueOf(if (isChecked) 0xFF526783.toInt() else 0xFF55565A.toInt())
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(prefKey, checked).apply()
                thumbTintList = ColorStateList.valueOf(if (checked) accent else muted)
                trackTintList = ColorStateList.valueOf(if (checked) 0xFF526783.toInt() else 0xFF55565A.toInt())
            }
        }
        container.addView(toggle)
        container.addView(TextView(this).apply {
            text = summary
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(5), 0, 0)
        })
        root.addView(container, fullWidth())
    }

    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun fullWidth() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { bottomMargin = dp(7) }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
