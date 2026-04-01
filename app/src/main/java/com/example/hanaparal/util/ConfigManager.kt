package com.example.hanaparal.util

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConfigManager {
    private val remoteConfig = Firebase.remoteConfig

    private val _maxMembers = MutableStateFlow(10L) // Default
    val maxMembers: StateFlow<Long> = _maxMembers

    private val _isCreationEnabled = MutableStateFlow(true)
    val isCreationEnabled: StateFlow<Boolean> = _isCreationEnabled

    // ADDED: Announcement state for the UI banner
    private val _announcement = MutableStateFlow("Welcome to HanapAral")
    val announcement: StateFlow<String> = _announcement

    init {
        val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 0 } // 0 for testing
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Safety Defaults
        remoteConfig.setDefaultsAsync(mapOf(
            "enable_group_creation" to true,
            "max_member_per_group" to 10L,
            "global_announcement" to "Welcome to HanapAral"
        ))

        fetchConfigs()
    }

    fun fetchConfigs() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _maxMembers.value = remoteConfig.getLong("max_member_per_group")
                _isCreationEnabled.value = remoteConfig.getBoolean("enable_group_creation")
                _announcement.value = remoteConfig.getString("global_announcement")
            }
        }
    }
}