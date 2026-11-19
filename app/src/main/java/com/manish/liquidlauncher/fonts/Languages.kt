package com.manish.liquidlauncher.fonts

/**
 * Languages the Type Studio can target.
 *
 * [subset] is the Google Fonts language subset key, so the online catalogue can be
 * filtered by any language Google Fonts publishes a font for. [script] is the writing
 * system, which is what actually decides whether a downloaded file can render the text.
 */
data class LanguageOption(
    val tag: String,
    val label: String,
    val native: String,
    val script: String,
    val subset: String
)

object Languages {

    val all: List<LanguageOption> = listOf(
        LanguageOption("en", "English", "English", "Latin", "latin"),
        LanguageOption("hi", "Hindi", "हिन्दी", "Devanagari", "devanagari"),
        LanguageOption("mr", "Marathi", "मराठी", "Devanagari", "devanagari"),
        LanguageOption("ne", "Nepali", "नेपाली", "Devanagari", "devanagari"),
        LanguageOption("sa", "Sanskrit", "संस्कृतम्", "Devanagari", "devanagari"),
        LanguageOption("kok", "Konkani", "कोंकणी", "Devanagari", "devanagari"),
        LanguageOption("mai", "Maithili", "मैथिली", "Devanagari", "devanagari"),
        LanguageOption("doi", "Dogri", "डोगरी", "Devanagari", "devanagari"),
        LanguageOption("bn", "Bengali", "বাংলা", "Bengali", "bengali"),
        LanguageOption("as", "Assamese", "অসমীয়া", "Bengali", "bengali"),
        LanguageOption("pa", "Punjabi", "ਪੰਜਾਬੀ", "Gurmukhi", "gurmukhi"),
        LanguageOption("gu", "Gujarati", "ગુજરાતી", "Gujarati", "gujarati"),
        LanguageOption("or", "Odia", "ଓଡ଼ିଆ", "Oriya", "oriya"),
        LanguageOption("ta", "Tamil", "தமிழ்", "Tamil", "tamil"),
        LanguageOption("te", "Telugu", "తెలుగు", "Telugu", "telugu"),
        LanguageOption("kn", "Kannada", "ಕನ್ನಡ", "Kannada", "kannada"),
        LanguageOption("ml", "Malayalam", "മലയാളം", "Malayalam", "malayalam"),
        LanguageOption("si", "Sinhala", "සිංහල", "Sinhala", "sinhala"),
        LanguageOption("ur", "Urdu", "اردو", "Arabic", "arabic"),
        LanguageOption("ar", "Arabic", "العربية", "Arabic", "arabic"),
        LanguageOption("fa", "Persian", "فارسی", "Arabic", "arabic"),
        LanguageOption("he", "Hebrew", "עברית", "Hebrew", "hebrew"),
        LanguageOption("th", "Thai", "ไทย", "Thai", "thai"),
        LanguageOption("lo", "Lao", "ລາວ", "Lao", "lao"),
        LanguageOption("km", "Khmer", "ខ្មែរ", "Khmer", "khmer"),
        LanguageOption("my", "Burmese", "မြန်မာ", "Myanmar", "myanmar"),
        LanguageOption("bo", "Tibetan", "བོད་ཡིག", "Tibetan", "tibetan"),
        LanguageOption("dz", "Dzongkha", "རྫོང་ཁ", "Tibetan", "tibetan"),
        LanguageOption("am", "Amharic", "አማርኛ", "Ethiopic", "ethiopic"),
        LanguageOption("ti", "Tigrinya", "ትግርኛ", "Ethiopic", "ethiopic"),
        LanguageOption("hy", "Armenian", "Հայերեն", "Armenian", "armenian"),
        LanguageOption("ka", "Georgian", "ქართული", "Georgian", "georgian"),
        LanguageOption("ru", "Russian", "Русский", "Cyrillic", "cyrillic"),
        LanguageOption("uk", "Ukrainian", "Українська", "Cyrillic", "cyrillic"),
        LanguageOption("bg", "Bulgarian", "Български", "Cyrillic", "cyrillic"),
        LanguageOption("el", "Greek", "Ελληνικά", "Greek", "greek"),
        LanguageOption("vi", "Vietnamese", "Tiếng Việt", "Latin", "vietnamese"),
        LanguageOption("zh", "Chinese", "中文", "Han", "chinese-simplified"),
        LanguageOption("ja", "Japanese", "日本語", "Japanese", "japanese"),
        LanguageOption("ko", "Korean", "한국어", "Korean", "korean"),
        LanguageOption("chr", "Cherokee", "ᏣᎳᎩ", "Cherokee", "cherokee"),
        LanguageOption("syr", "Syriac", "ܣܘܪܝܝܐ", "Syriac", "syriac"),
        LanguageOption("dv", "Dhivehi", "ދިވެހި", "Thaana", "thaana"),
        LanguageOption("ber", "Tamazight", "ⵜⴰⵎⴰⵣⵉⵖⵜ", "Tifinagh", "tifinagh"),
        LanguageOption("ff", "Fulani", "𞤊𞤵𞤤𞤬𞤵𞤤𞤣𞤫", "Adlam", "adlam"),
        LanguageOption("sat", "Santali", "ᱥᱟᱱᱛᱟᱲᱤ", "Ol Chiki", "latin"),
        LanguageOption("mni", "Manipuri", "ꯃꯤꯇꯩ ꯂꯣꯟ", "Meitei Mayek", "latin"),
        LanguageOption("brx", "Bodo", "बड़ो", "Devanagari", "devanagari"),
        LanguageOption("ks", "Kashmiri", "کٲشُر", "Arabic", "arabic"),
        LanguageOption("sd", "Sindhi", "سنڌي", "Arabic", "arabic")
    )

    fun byTag(tag: String): LanguageOption? = all.firstOrNull { it.tag == tag }

    fun bySubset(subset: String): LanguageOption? = all.firstOrNull { it.subset == subset }

    fun scriptFor(tag: String): String = byTag(tag)?.script ?: ""

    /** Short sample string in the language, used for font previews. */
    fun sample(tag: String): String = when (tag) {
        "hi", "mr", "ne", "sa", "kok", "mai", "doi", "brx" -> "नमस्ते 12:30"
        "bn", "as" -> "নমস্কার 12:30"
        "pa" -> "ਸਤ ਸ੍ਰੀ ਅਕਾਲ 12:30"
        "gu" -> "નમસ્તે 12:30"
        "or" -> "ନମସ୍କାର 12:30"
        "ta" -> "வணக்கம் 12:30"
        "te" -> "నమస్కారం 12:30"
        "kn" -> "ನಮಸ್ಕಾರ 12:30"
        "ml" -> "നമസ്കാരം 12:30"
        "si" -> "ආයුබෝවන් 12:30"
        "ur", "ar", "fa", "ks", "sd" -> "مرحبا 12:30"
        "he" -> "שלום 12:30"
        "th" -> "สวัสดี 12:30"
        "lo" -> "ສະບາຍດີ 12:30"
        "km" -> "សួស្តី 12:30"
        "my" -> "မင်္ဂလာပါ 12:30"
        "bo", "dz" -> "བཀྲ་ཤིས་བདེ་ལེགས 12:30"
        "am", "ti" -> "ሰላም 12:30"
        "hy" -> "Բարեւ 12:30"
        "ka" -> "გამარჯობა 12:30"
        "ru", "uk", "bg" -> "Привет 12:30"
        "el" -> "Γειά 12:30"
        "vi" -> "Xin chào 12:30"
        "zh" -> "你好 12:30"
        "ja" -> "こんにちは 12:30"
        "ko" -> "안녕하세요 12:30"
        "chr" -> "ᎣᏏᏲ 12:30"
        "dv" -> "އައްސަލާމް 12:30"
        else -> "Aa 12:30"
    }
}
