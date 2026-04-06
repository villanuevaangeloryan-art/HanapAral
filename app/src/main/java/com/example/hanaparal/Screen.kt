package com.example.hanaparal

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Profile : Screen("profile")
}
