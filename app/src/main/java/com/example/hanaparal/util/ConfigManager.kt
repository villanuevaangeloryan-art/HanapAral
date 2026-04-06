package com.example.hanaparal.util

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConfigManager {
    private val remoteConfig = Firebase.remoteConfig

    // Max members
    private val _maxMembers = MutableStateFlow(10L)
    val maxMembers: StateFlow<Long> = _maxMembers

    // Toggle for Group Creation module
    private val _isCreationEnabled = MutableStateFlow(false)
    val isCreationEnabled: StateFlow<Boolean> = _isCreationEnabled

    //External UI Strings -Announcements
    private val _announcement = MutableStateFlow("Welcome to HanapAral")
    val announcement: StateFlow<String> = _announcement

    // Superuser toggle for field editability
    private val _isMaxMemberEditable = MutableStateFlow(false)
    val isMaxMemberEditable: StateFlow<Boolean> = _isMaxMemberEditable

    init {
        // Fetch immediately for testing/lab
        val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 0 }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set safety defaults
        remoteConfig.setDefaultsAsync(mapOf(
            "enable_group_creation" to false,
            "max_member_per_group" to 10L,
            "global_announcement" to "Welcome to HanapAral",
            "is_max_member_editable" to false
        ))
        fetchConfigs()
    }

    fun fetchConfigs() {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _maxMembers.value = remoteConfig.getLong("max_member_per_group")
                _announcement.value = remoteConfig.getString("global_announcement")
                _isMaxMemberEditable.value = remoteConfig.getBoolean("is_max_member_editable")
                Log.d("ConfigManager", "Cloud configs applied successfully")
            }
        }
    }

    // Called after successful Biometric Authentication
    fun unlockCreationFeature() {
        _isCreationEnabled.value = true
    }
}