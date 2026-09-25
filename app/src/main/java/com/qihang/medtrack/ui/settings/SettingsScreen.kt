package com.qihang.medtrack.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onClinicianLogin: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        val error = state.error
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            else -> SettingsContent(
                state = state,
                onLogout = onLogout,
                onClinicianLogin = onClinicianLogin
            )
        }
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onLogout: () -> Unit,
    onClinicianLogin: () -> Unit
) {
    // ── Account info card ────────────────────────────────────────────
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Account Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(icon = Icons.Filled.Person, label = "Name", value = state.name)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(icon = Icons.Filled.Phone, label = "Phone", value = state.phoneNumber)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(icon = Icons.Filled.Badge, label = "Patient ID", value = state.patientId)
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // ── Clinician login ─────────────────────────────────────
    OutlinedButton(
        onClick = onClinicianLogin,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Shield,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text("Clinician Login")
    }

    Spacer(modifier = Modifier.height(12.dp))

    // ── Logout ───────────────────────────────────────────────────────
    Button(
        onClick = onLogout,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text("Logout")
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
