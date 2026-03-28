package com.example.hanaparal.data.model

data class Group(
    val groupId: String = "",
    val title: String = "",
    val subject: String = "",
    val description: String = "",
    val adminId: String = "",
    val adminName: String = "",
    val members: List<String> = emptyList(),
    val maxMembers: Int = 20,
    val isOpen: Boolean = true
)
