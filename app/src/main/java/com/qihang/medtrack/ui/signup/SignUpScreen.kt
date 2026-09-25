package com.qihang.medtrack.ui.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    onNavigateToLogin: () -> Unit,
    viewModel: SignUpViewModel = viewModel(
        factory = SignUpViewModel.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    state.generatedPatientId?.let { patientId ->
        AlertDialog(
            onDismissRequest = { /* force user to acknowledge */ },
            title = { Text("Account Created!") },
            text = {
                Column {
                    Text("Please remember your Patient ID — you'll need it to log in:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = patientId,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.consumeSuccess()
                    onNavigateToLogin()
                }) {
                    Text("Got it, go to Login")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create Account", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.fullName,
            onValueChange = { value -> viewModel.onNameChange(value) },
            label = { Text("Full Name *") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
            ),
            isError = state.nameError != null,
            supportingText = { state.nameError?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.phone,
            onValueChange = { value -> viewModel.onPhoneChange(value) },
            label = { Text("Phone Number *") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next
            ),
            isError = state.phoneError != null,
            supportingText = { state.phoneError?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { value -> viewModel.onPasswordChange(value) },
            label = { Text("Password *") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
            ),
            isError = state.passwordError != null,
            supportingText = { state.passwordError?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = { value -> viewModel.onConfirmPasswordChange(value) },
            label = { Text("Confirm Password *") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
            ),
            isError = state.confirmError != null,
            supportingText = { state.confirmError?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        state.generalError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.signUp() },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Back to Login")
        }
    }
}
