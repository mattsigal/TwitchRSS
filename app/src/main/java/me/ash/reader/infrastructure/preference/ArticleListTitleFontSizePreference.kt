package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalArticleListTitleFontSize =
    compositionLocalOf<ArticleListTitleFontSizePreference> { ArticleListTitleFontSizePreference.default }

sealed class ArticleListTitleFontSizePreference(val value: Int, val label: String) : Preference() {
    object Compact : ArticleListTitleFontSizePreference(14, "Compact (14sp)")
    object Default : ArticleListTitleFontSizePreference(16, "Default (16sp)")
    object Large : ArticleListTitleFontSizePreference(18, "Large (18sp)")

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.articleListTitleFontSize,
                value
            )
        }
    }

    companion object {
        val default = Default
        val values = listOf(Compact, Default, Large)

        fun fromPreferences(preferences: Preferences): ArticleListTitleFontSizePreference {
            val v = preferences[DataStoreKey.keys[DataStoreKey.articleListTitleFontSize]?.key as Preferences.Key<Int>]
            return values.find { it.value == v } ?: default
        }
    }
}
