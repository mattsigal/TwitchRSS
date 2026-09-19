package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalFlowShowMarkAllAsReadButton =
    compositionLocalOf<FlowShowMarkAllAsReadButtonPreference> { FlowShowMarkAllAsReadButtonPreference.default }

sealed class FlowShowMarkAllAsReadButtonPreference(val value: Boolean) : Preference() {
    object ON : FlowShowMarkAllAsReadButtonPreference(true)
    object OFF : FlowShowMarkAllAsReadButtonPreference(false)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.flowShowMarkAllAsReadButton,
                value
            )
        }
    }

    companion object {
        val default = ON
        val values = listOf(ON, OFF)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[DataStoreKey.flowShowMarkAllAsReadButton]?.key as Preferences.Key<Boolean>]) {
                true -> ON
                false -> OFF
                else -> default
            }
    }
}

operator fun FlowShowMarkAllAsReadButtonPreference.not(): FlowShowMarkAllAsReadButtonPreference =
    when (value) {
        true -> FlowShowMarkAllAsReadButtonPreference.OFF
        false -> FlowShowMarkAllAsReadButtonPreference.ON
    }
