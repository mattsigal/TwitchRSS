package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalReadingTitleFontSize =
    compositionLocalOf<ReadingTitleFontSizePreference> { ReadingTitleFontSizePreference.default }

sealed class ReadingTitleFontSizePreference(val value: Int, val label: String) : Preference() {
    object Compact : ReadingTitleFontSizePreference(22, "Compact (22sp)")
    object Medium : ReadingTitleFontSizePreference(26, "Medium (26sp)")
    object Default : ReadingTitleFontSizePreference(30, "Default (30sp)")
    object Large : ReadingTitleFontSizePreference(34, "Large (34sp)")

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.readingTitleFontSize,
                value
            )
        }
    }

    companion object {
        val default = Default
        val values = listOf(Compact, Medium, Default, Large)

        fun fromPreferences(preferences: Preferences): ReadingTitleFontSizePreference {
            val v = preferences[DataStoreKey.keys[DataStoreKey.readingTitleFontSize]?.key as Preferences.Key<Int>]
            return values.find { it.value == v } ?: default
        }
    }
}
