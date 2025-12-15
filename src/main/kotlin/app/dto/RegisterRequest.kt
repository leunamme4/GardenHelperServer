package app.dto

data class RegisterRequest(
    val email: String,
    val password: String,
    val confirmPassword: String
)
