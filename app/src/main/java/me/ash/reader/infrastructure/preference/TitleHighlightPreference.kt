package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.datastore.preferences.core.Preferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

data class HighlightRule(
    val pattern: String,
    val colorHex: String,
)

val LocalHighlightSubreddits =
    compositionLocalOf { HighlightSubredditsPreference.default }

val LocalHighlightSubredditColor =
    compositionLocalOf { HighlightSubredditColorPreference.default }

val LocalCustomHighlightRules =
    compositionLocalOf { CustomHighlightRulesPreference.default }

class HighlightSubredditsPreference(val value: Boolean) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.highlightSubreddits, value)
        }
    }

    companion object {
        const val default = true

        fun fromPreferences(preferences: Preferences): Boolean {
            return preferences[DataStoreKey.keys[DataStoreKey.highlightSubreddits]?.key as Preferences.Key<Boolean>]
                ?: default
        }
    }
}

class HighlightSubredditColorPreference(val value: String) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.highlightSubredditColor, value)
        }
    }

    companion object {
        const val default = "#FF4500" // Signature Reddit Orange

        fun fromPreferences(preferences: Preferences): String {
            return preferences[DataStoreKey.keys[DataStoreKey.highlightSubredditColor]?.key as Preferences.Key<String>]
                ?: default
        }
    }
}

class CustomHighlightRulesPreference(val value: List<HighlightRule>) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            val json = Gson().toJson(value)
            context.dataStore.put(DataStoreKey.customHighlightRules, json)
        }
    }

    companion object {
        val default: List<HighlightRule> = emptyList()

        fun fromPreferences(preferences: Preferences): List<HighlightRule> {
            val json = preferences[DataStoreKey.keys[DataStoreKey.customHighlightRules]?.key as Preferences.Key<String>]
            if (json.isNullOrBlank()) return default
            return try {
                val type = object : TypeToken<List<HighlightRule>>() {}.type
                Gson().fromJson(json, type) ?: default
            } catch (e: Exception) {
                default
            }
        }
    }
}

fun parseHexColor(hex: String, fallback: Color = Color(0xFFFF4500)): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        when (clean.length) {
            6 -> Color(android.graphics.Color.parseColor("#$clean"))
            8 -> Color(android.graphics.Color.parseColor("#$clean"))
            else -> fallback
        }
    } catch (e: Exception) {
        fallback
    }
}

private val subredditRegex = Regex("(?i)/r/[a-zA-Z0-9_]+/?")

fun String.highlightTitle(
    highlightSubreddits: Boolean,
    subredditColorHex: String,
    customRules: List<HighlightRule>,
): AnnotatedString {
    if (!highlightSubreddits && customRules.isEmpty()) {
        return AnnotatedString(this)
    }

    val ranges = mutableListOf<Triple<IntRange, Color, FontWeight>>()

    if (highlightSubreddits) {
        val subColor = parseHexColor(subredditColorHex, Color(0xFFFF4500))
        subredditRegex.findAll(this).forEach { matchResult ->
            ranges.add(Triple(matchResult.range, subColor, FontWeight.Bold))
        }
    }

    for (rule in customRules) {
        if (rule.pattern.isBlank()) continue
        val ruleColor = parseHexColor(rule.colorHex)
        var startIndex = 0
        while (startIndex < this.length) {
            val idx = this.indexOf(rule.pattern, startIndex, ignoreCase = true)
            if (idx == -1) break
            val range = idx until (idx + rule.pattern.length)
            ranges.add(Triple(range, ruleColor, FontWeight.Bold))
            startIndex = idx + rule.pattern.length
        }
    }

    if (ranges.isEmpty()) {
        return AnnotatedString(this)
    }

    return buildAnnotatedString {
        append(this@highlightTitle)
        for ((range, color, weight) in ranges) {
            addStyle(
                style = SpanStyle(color = color, fontWeight = weight),
                start = range.first,
                end = range.last + 1,
            )
        }
    }
}
