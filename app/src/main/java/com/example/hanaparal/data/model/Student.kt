package com.example.hanaparal.data.model

data class Student(
    val uid: String = "",
    val name: String = "",
    val course: String = "",
    val email: String = ""
)

data class StudyGroupItem(
    val id: String = "",
    val name: String = "",
    val course: String = "",
    val memberCount: Int = 0,
    val isAdmin: Boolean = false
)
