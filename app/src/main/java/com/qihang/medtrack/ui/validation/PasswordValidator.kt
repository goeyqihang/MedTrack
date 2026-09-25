package com.qihang.medtrack.ui.validation

/** Password rules shared by the Sign Up and Claim Account forms. */
object PasswordValidator {

    private const val MIN_LENGTH = 8
    private val LETTER_AND_DIGIT = Regex("^(?=.*[a-zA-Z])(?=.*\\d).*$")

    /** Returns an error message for [password], or null if it is valid. */
    fun validate(password: String): String? = when {
        password.isBlank() -> "Password is required"
        password.length < MIN_LENGTH -> "At least $MIN_LENGTH characters required"
        !password.matches(LETTER_AND_DIGIT) -> "Must contain at least 1 letter and 1 number"
        else -> null
    }

    /** Returns an error message if [confirmation] does not match [password], or null. */
    fun validateConfirmation(password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> "Please confirm your password"
        confirmation != password -> "Passwords do not match"
        else -> null
    }
}
