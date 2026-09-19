package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

val LocalSyncAtMinute =
    compositionLocalOf<Int> { 0 }

class SyncAtMinutePreference(val value: Int) : Preference() {
    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKey.syncAtMinute,
                value
            )
        }
    }

    companion object {
        const val default = 0

        fun fromPreferences(preferences: Preferences): Int {
            return preferences[DataStoreKey.keys[DataStoreKey.syncAtMinute]?.key as Preferences.Key<Int>] ?: default
        }
    }
}
