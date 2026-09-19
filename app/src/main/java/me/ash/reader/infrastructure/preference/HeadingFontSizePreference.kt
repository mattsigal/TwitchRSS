package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalHeadingFontSize =
    compositionLocalOf<HeadingFontSizePreference> { HeadingFontSizePreference.default }

sealed class HeadingFontSizePreference(val value: Int, val label: String) : Preference() {
    object Compact : HeadingFontSizePreference(24, "Compact (24sp)")
    object Medium : HeadingFontSizePreference(28, "Medium (28sp)")
    object Default : HeadingFontSizePreference(32, "Default (32sp)")
    object Large : HeadingFontSizePreference(36, "Large (36sp)")

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.headingFontSize,
                value
            )
        }
    }

    companion object {
        val default = Default
        val values = listOf(Compact, Medium, Default, Large)

        fun fromPreferences(preferences: Preferences): HeadingFontSizePreference {
            val v = preferences[DataStoreKey.keys[DataStoreKey.headingFontSize]?.key as Preferences.Key<Int>]
            return values.find { it.value == v } ?: default
        }
    }
}
