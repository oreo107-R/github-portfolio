package com.app.dinolearnerfree

import android.content.Context
import androidx.datastore.preferences.core.edit

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


// -------------------------------------------------------------------------------------
// DataStore instance bound to Context
// -------------------------------------------------------------------------------------
private val Context.profilesDataStore by preferencesDataStore("profiles_store")

// -------------------------------------------------------------------------------------
// ProfilesStore: manages player profiles + progress using Preferences DataStore + JSON
// -------------------------------------------------------------------------------------
object ProfilesStore {

    // ---- Keys ----
    private val KEY_PROFILES_JSON = stringPreferencesKey("profiles_json")
    private val KEY_SELECTED_ID   = stringPreferencesKey("selected_profile_id")

    // ---- Settings ----
    private const val MAX_LEVEL = 10

    // ---- JSON ----
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // ---- Internal helpers ----
    private fun decodeProfilesOrEmpty(s: String?): List<Profile> =
        runCatching {
            if (s == null) emptyList() else json.decodeFromString<List<Profile>>(s)
        }.getOrElse { emptyList() }

    private suspend fun saveProfiles(context: Context, list: List<Profile>) {
        context.profilesDataStore.edit { prefs ->
            prefs[KEY_PROFILES_JSON] = json.encodeToString(list)
        }
    }

    // ---- Flows ----

    /** Stream of all profiles (empty if none exist). */
    fun profilesFlow(context: Context): Flow<List<Profile>> =
        context.profilesDataStore.data.map { prefs ->
            decodeProfilesOrEmpty(prefs[KEY_PROFILES_JSON])
        }

    /** Stream of selected profile id (nullable). */
    fun selectedIdFlow(context: Context): Flow<String?> =
        context.profilesDataStore.data.map { prefs -> prefs[KEY_SELECTED_ID] }

    /** Stream of a specific profile by id (nullable). */
    fun profileFlow(context: Context, id: String?): Flow<Profile?> =
        profilesFlow(context).map { list -> list.find { it.id == id } }

    // ---- One-shot reads ----

    suspend fun getProfilesOnce(context: Context): List<Profile> =
        profilesFlow(context).first()

    suspend fun getSelectedIdOnce(context: Context): String? =
        selectedIdFlow(context).first()

    // ---- Create / Select ----

    suspend fun createProfile(context: Context, name: String): String {
        val trimmed = name.trim().ifEmpty { "Player" }
        val all = getProfilesOnce(context).toMutableList()

        // Make name unique (simple scheme)
        val base = trimmed
        val uniqueName = if (all.any { it.name.equals(base, ignoreCase = true) }) {
            "$base ${all.size + 1}"
        } else base

        val new = Profile(name = uniqueName)
        all += new
        saveProfiles(context, all)
        setSelected(context, new.id)
        return new.id
    }

    suspend fun setSelected(context: Context, id: String?) {
        context.profilesDataStore.edit { prefs ->
            if (id == null) prefs.remove(KEY_SELECTED_ID) else prefs[KEY_SELECTED_ID] = id
        }
    }

    // ---- Delete ----

    suspend fun deleteProfile(context: Context, id: String): Boolean {
        var deleted = false
        context.profilesDataStore.edit { prefs ->
            val currentList = decodeProfilesOrEmpty(prefs[KEY_PROFILES_JSON])

            if (currentList.any { it.id == id }) {
                val newList = currentList.filterNot { it.id == id }
                val oldSelected = prefs[KEY_SELECTED_ID]
                val newSelected =
                    if (oldSelected == id) newList.firstOrNull()?.id else oldSelected

                if (newList.isEmpty()) {
                    prefs.remove(KEY_PROFILES_JSON)
                    prefs.remove(KEY_SELECTED_ID)
                } else {
                    prefs[KEY_PROFILES_JSON] = json.encodeToString(newList)
                    if (newSelected == null) prefs.remove(KEY_SELECTED_ID)
                    else prefs[KEY_SELECTED_ID] = newSelected
                }
                deleted = true
            }
        }
        return deleted
    }

    suspend fun deleteCurrentProfile(context: Context): String? {
        getSelectedIdOnce(context)?.let { deleteProfile(context, it) }
        return getSelectedIdOnce(context)
    }

    // ---- Progression / Objectives ----

    /**
     * Mark an objective flag true for the selected profile, and bump unlockedLevel according
     * to any rules you want (keeps prior rules you had for quiz objectives).
     */
    suspend fun markObjective(context: Context, objectiveKey: String) {
        val selected = selectedIdFlow(context).first() ?: return
        val all = getProfilesOnce(context).toMutableList()
        val idx = all.indexOfFirst { it.id == selected }
        if (idx < 0) return

        val p = all[idx]
        val newObj = p.progress.objectives + (objectiveKey to true)

        // Example gates you already used:
        val unlockedLevel = when {
            newObj["quiz_az_20plus"] == true -> maxOf(3, p.progress.unlockedLevel)
            newObj["quiz_az_passed"] == true -> maxOf(2, p.progress.unlockedLevel)
            else -> p.progress.unlockedLevel
        }

        all[idx] = p.copy(
            progress = p.progress.copy(
                unlockedLevel = unlockedLevel,
                objectives = newObj
            )
        )
        saveProfiles(context, all)
    }

    /**
     * Set the selected profile's unlocked level.
     * - Clamped to [1, MAX_LEVEL]
     * - By default only increases (won't downgrade)
     */
    suspend fun setUnlockedLevel(
        context: Context,
        newLevel: Int,
        onlyIncrease: Boolean = true
    ) {
        val selectedId = selectedIdFlow(context).first() ?: return

        context.profilesDataStore.edit { prefs ->
            val list = decodeProfilesOrEmpty(prefs[KEY_PROFILES_JSON]).toMutableList()
            val idx = list.indexOfFirst { it.id == selectedId }
            if (idx < 0) return@edit

            val p = list[idx]
            val clamped = newLevel.coerceIn(1, MAX_LEVEL)
            val target = if (onlyIncrease) maxOf(clamped, p.progress.unlockedLevel) else clamped

            if (target != p.progress.unlockedLevel) {
                list[idx] = p.copy(
                    progress = p.progress.copy(unlockedLevel = target)
                )
                prefs[KEY_PROFILES_JSON] = json.encodeToString(list)
            }
        }
    }
}






