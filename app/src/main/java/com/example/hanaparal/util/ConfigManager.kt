package com.example.hanaparal.util

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConfigManager {
    private val remoteConfig = Firebase.remoteConfig

    private val _maxMembers = MutableStateFlow(10L)
    val maxMembers: StateFlow<Long> = _maxMembers

    private val _isCreationEnabled = MutableStateFlow(false)
    val isCreationEnabled: StateFlow<Boolean> = _isCreationEnabled

    private val _announcement = MutableStateFlow("Welcome to HanapAral")
    val announcement: StateFlow<String> = _announcement

    // ADD THIS BACK IN!
    private val _isMaxMemberEditable = MutableStateFlow(false)
    val isMaxMemberEditable: StateFlow<Boolean> = _isMaxMemberEditable

    init {
        val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 0 }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(mapOf(
            "enable_group_creation" to false,
            "max_member_per_group" to 10L,
            "global_announcement" to "Welcome to HanapAral",
            "is_max_member_editable" to false // AND THIS!
        ))
        fetchConfigs()
    }

    fun fetchConfigs(onComplete: (Boolean) -> Unit = {}) {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _maxMembers.value = remoteConfig.getLong("max_member_per_group")
                _announcement.value = remoteConfig.getString("global_announcement")
                _isCreationEnabled.value = remoteConfig.getBoolean("enable_group_creation")
                _isMaxMemberEditable.value = remoteConfig.getBoolean("is_max_member_editable") // AND THIS!
                Log.d("ConfigManager", "Remote Config Synced Successfully")
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }

    fun unlockCreationFeature() {
        _isCreationEnabled.value = true
    }
}