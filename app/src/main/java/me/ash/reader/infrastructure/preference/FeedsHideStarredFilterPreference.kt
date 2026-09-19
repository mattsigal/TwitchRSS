package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFeedsHideStarredFilter =
    compositionLocalOf<FeedsHideStarredFilterPreference> { FeedsHideStarredFilterPreference.default }

sealed class FeedsHideStarredFilterPreference(val value: Boolean) : Preference() {
    object ON : FeedsHideStarredFilterPreference(true)
    object OFF : FeedsHideStarredFilterPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.feedsHideStarredFilter,
                value
            )
        }
    }

    companion object {
        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[DataStoreKey.feedsHideStarredFilter]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FeedsHideStarredFilterPreference.not(): FeedsHideStarredFilterPreference =
    when (value) {
        true -> FeedsHideStarredFilterPreference.OFF
        false -> FeedsHideStarredFilterPreference.ON
    }
