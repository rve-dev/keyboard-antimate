package dev.rve.keyboardantimate

/**
 * Keyboard layouts ported from the Argon AntiMate Ultimate specification.
 * A layout here is only a key map; it does not imply IME-level transliteration
 * or input-method support for scripts that need composition (e.g. CJK).
 */
data class AntimateLayout(
    val id: String,
    val title: String,
    val spaceLabel: String,
    val rows: List<List<String>>
)

object AntimateLayouts {
    val all: List<AntimateLayout> = listOf(
        AntimateLayout("ru_jcuken", "Русский (ЙЦУКЕН)", "РУССКИЙ", listOf(
            listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
            listOf("ф","ы","в","а","п","р","о","л","д","ж","э"),
            listOf("я","ч","с","м","и","т","ь","б","ю")
        )),
        AntimateLayout("en_qwerty", "English (QWERTY)", "ENGLISH", listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("z","x","c","v","b","n","m")
        )),
        AntimateLayout("ru_phonetic", "Русский (ЯШЕРТЫ)", "РУС. ФОНЕТ.", listOf(
            listOf("я","ш","е","р","т","ы","у","и","о","п","ю"),
            listOf("а","с","д","ф","г","ч","й","к","л","ь","ж"),
            listOf("з","х","ц","в","б","н","м","щ","э")
        )),
        AntimateLayout("uk_ua", "Українська", "УКРАЇНСЬКА", listOf(
            listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
            listOf("ф","і","в","а","п","р","о","л","д","ж","є"),
            listOf("я","ч","с","м","и","т","ь","б","ю","ї")
        )),
        AntimateLayout("kk_kz", "Қазақша", "ҚАЗАҚША", listOf(
            listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
            listOf("ф","ы","в","а","п","р","о","л","д","ж","э"),
            listOf("я","ч","с","м","и","т","ь","б","ю","ә","ғ","қ","ң","ө","ұ","і")
        )),
        AntimateLayout("be_by", "Беларуская", "БЕЛАРУСКАЯ", listOf(
            listOf("й","ц","у","к","е","н","г","ш","ў","з","х"),
            listOf("ф","ы","в","а","п","р","о","л","д","ж","э"),
            listOf("я","ч","с","м","і","т","ь","б","ю")
        )),
        AntimateLayout("de_qwertz", "Deutsch (QWERTZ)", "DEUTSCH", listOf(
            listOf("q","w","e","r","t","z","u","i","o","p","ü"),
            listOf("a","s","d","f","g","h","j","k","l","ö","ä"),
            listOf("y","x","c","v","b","n","m","ß")
        )),
        AntimateLayout("fr_azerty", "Français (AZERTY)", "FRANÇAIS", listOf(
            listOf("a","z","e","r","t","y","u","i","o","p"),
            listOf("q","s","d","f","g","h","j","k","l","m"),
            listOf("w","x","c","v","b","n","é","è","ç")
        )),
        AntimateLayout("es_qwerty", "Español", "ESPAÑOL", listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l","ñ"),
            listOf("z","x","c","v","b","n","m")
        )),
        AntimateLayout("it_qwerty", "Italiano", "ITALIANO", listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l","ò","à"),
            listOf("z","x","c","v","b","n","m","ù")
        )),
        AntimateLayout("tr_qwerty", "Türkçe", "TÜRKÇE", listOf(
            listOf("q","w","e","r","t","y","u","ı","o","p","ğ","ü"),
            listOf("a","s","d","f","g","h","j","k","l","ş","i"),
            listOf("z","x","c","v","b","n","m","ö","ç")
        )),
        AntimateLayout("ar_sa", "العربية", "العربية", listOf(
            listOf("ض","ص","ث","ق","ف","غ","ع","ه","خ","ح","ج"),
            listOf("ش","س","ي","ب","ل","ا","ت","ن","م","ك","ط"),
            listOf("ئ","ء","ؤ","ر","لا","ى","ة","و","ز","ظ")
        )),
        AntimateLayout("zh_pinyin", "中文 (Pinyin keys)", "中文", listOf(
            listOf("q","w","e","r","t","y","u","i","o","p"),
            listOf("a","s","d","f","g","h","j","k","l"),
            listOf("z","x","c","v","b","n","m")
        )),
        AntimateLayout("ja_kana", "日本語 (かな)", "日本語", listOf(
            listOf("あ","か","さ","た","な","は","ま","や","ら","わ"),
            listOf("い","き","し","ち","に","ひ","み","ゆ","り","ん"),
            listOf("う","く","す","つ","ぬ","ふ","む","よ","る","ー")
        )),
        AntimateLayout("ko_kr", "한국어 (Jamo)", "한국어", listOf(
            listOf("ㅂ","ㅈ","ㄷ","ㄱ","ㅅ","ㅛ","ㅕ","ㅑ","ㅐ","ㅔ"),
            listOf("ㅁ","ㄴ","ㅇ","ㄹ","ㅎ","ㅗ","ㅓ","ㅏ","ㅣ"),
            listOf("ㅋ","ㅌ","ㅊ","ㅍ","ㅠ","ㅜ","ㅡ")
        )),
        AntimateLayout("hi_in", "हिन्दी", "हिन्दी", listOf(
            listOf("ौ","ै","ा","ी","ू","ब","ह","ग","द","ज","ड"),
            listOf("ो","े","्","ि","ु","प","र","क","त","च","ट"),
            listOf("ं","म","न","व","ल","स","य","श")
        )),
        AntimateLayout("en_dvorak", "English (Dvorak)", "DVORAK", listOf(
            listOf("p","y","f","g","c","r","l"),
            listOf("a","o","e","u","i","d","h","t","n","s"),
            listOf("q","j","k","x","b","m","w","v","z")
        ))
    )

    fun byId(id: String): AntimateLayout = all.firstOrNull { it.id == id } ?: all.first()
}
