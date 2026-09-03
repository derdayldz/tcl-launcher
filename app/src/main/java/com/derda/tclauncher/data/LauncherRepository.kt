package com.derda.tclauncher.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "tcl_launcher_prefs")

/**
 * Uygulamanın tüm kalıcı verisini (satırlar + ayarlar) yönetir.
 * Sıfırdan kurulumda varsayılan satır seti otomatik oluşturulur.
 */
class LauncherRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val ROWS_KEY = stringPreferencesKey("rows_json")
    private val SETTINGS_KEY = stringPreferencesKey("settings_json")
    private val RECENT_KEY = stringPreferencesKey("recent_packages_json")

    // ---------- Satırlar ----------

    val rowsFlow: Flow<List<LauncherRow>> = context.dataStore.data.map { prefs ->
        val raw = prefs[ROWS_KEY]
        if (raw.isNullOrBlank()) {
            defaultRows()
        } else {
            runCatching { json.decodeFromString<List<LauncherRow>>(raw) }.getOrElse { defaultRows() }
        }.sortedBy { it.order }
    }

    suspend fun saveRows(rows: List<LauncherRow>) {
        context.dataStore.edit { prefs ->
            prefs[ROWS_KEY] = json.encodeToString(rows)
        }
    }

    private fun defaultRows(): List<LauncherRow> = listOf(
        LauncherRow(id = UUID.randomUUID().toString(), title = "Uygulamalarım", type = RowType.ALL_APPS, order = 0),
        LauncherRow(id = UUID.randomUUID().toString(), title = "Son Kullanılanlar", type = RowType.RECENTLY_USED, order = 1),
        LauncherRow(id = UUID.randomUUID().toString(), title = "İzlenenler", type = RowType.WATCH_HISTORY, order = 2)
    )

    // ---------- Genel ayarlar ----------

    val settingsFlow: Flow<LauncherSettings> = context.dataStore.data.map { prefs ->
        val raw = prefs[SETTINGS_KEY]
        if (raw.isNullOrBlank()) LauncherSettings()
        else runCatching { json.decodeFromString<LauncherSettings>(raw) }.getOrElse { LauncherSettings() }
    }

    suspend fun saveSettings(settings: LauncherSettings) {
        context.dataStore.edit { prefs ->
            prefs[SETTINGS_KEY] = json.encodeToString(settings)
        }
    }

    // ---------- "Son kullanılanlar" geçmişi ----------

    val recentPackagesFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[RECENT_KEY]
        if (raw.isNullOrBlank()) emptyList()
        else runCatching { json.decodeFromString<List<String>>(raw) }.getOrElse { emptyList() }
    }

    suspend fun recordAppOpened(packageName: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[RECENT_KEY]
            val current = if (raw.isNullOrBlank()) emptyList()
            else runCatching { json.decodeFromString<List<String>>(raw) }.getOrElse { emptyList() }
            val updated = (listOf(packageName) + current.filter { it != packageName }).take(20)
            prefs[RECENT_KEY] = json.encodeToString(updated)
        }
    }
}
