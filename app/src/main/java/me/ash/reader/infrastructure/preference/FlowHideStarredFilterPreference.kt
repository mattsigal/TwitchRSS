package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFlowHideStarredFilter =
    compositionLocalOf<FlowHideStarredFilterPreference> { FlowHideStarredFilterPreference.default }

sealed class FlowHideStarredFilterPreference(val value: Boolean) : Preference() {
    object ON : FlowHideStarredFilterPreference(true)
    object OFF : FlowHideStarredFilterPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.flowHideStarredFilter,
                value
            )
        }
    }

    companion object {
        val default = OFF
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[DataStoreKey.flowHideStarredFilter]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowHideStarredFilterPreference.not(): FlowHideStarredFilterPreference =
    when (value) {
        true -> FlowHideStarredFilterPreference.OFF
        false -> FlowHideStarredFilterPreference.ON
    }
