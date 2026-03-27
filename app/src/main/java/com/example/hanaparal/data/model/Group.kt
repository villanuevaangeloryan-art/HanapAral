package com.example.hanaparal.data.model

data class Group(
    val groupId: String = "",
    val title: String = "",
    val adminId: String = "",
    val members: List<String> = emptyList()
)