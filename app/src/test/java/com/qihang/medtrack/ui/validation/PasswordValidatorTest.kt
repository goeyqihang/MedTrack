package com.qihang.medtrack.ui.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun `accepts a password with letters and digits`() {
        assertNull(PasswordValidator.validate("pass1234"))
    }

    @Test
    fun `rejects a blank password`() {
        assertEquals("Password is required", PasswordValidator.validate("   "))
    }

    @Test
    fun `rejects a password shorter than 8 characters`() {
        assertEquals("At least 8 characters required", PasswordValidator.validate("abc123"))
    }

    @Test
    fun `rejects a password without both a letter and a digit`() {
        val expected = "Must contain at least 1 letter and 1 number"
        assertEquals(expected, PasswordValidator.validate("password"))
        assertEquals(expected, PasswordValidator.validate("12345678"))
    }

    @Test
    fun `confirmation must be present and match`() {
        assertEquals(
            "Please confirm your password",
            PasswordValidator.validateConfirmation("pass1234", "")
        )
        assertEquals(
            "Passwords do not match",
            PasswordValidator.validateConfirmation("pass1234", "pass12345")
        )
        assertNull(PasswordValidator.validateConfirmation("pass1234", "pass1234"))
    }
}
