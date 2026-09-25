package com.qihang.medtrack.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qihang.medtrack.R

@Composable
fun WelcomeScreen(
    modifier: Modifier,
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit,
    onClaimClick: () -> Unit,
    viewModel: WelcomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Text(
                text = state.appTitle,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Image(
                painter = painterResource(R.drawable.medtrack_logo),
                contentDescription = "MedTrack Logo",
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = state.disclaimer,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextButton(
                onClick = { uriHandler.openUri(state.clinicLinkUrl) }
            ) {
                Text(state.clinicLinkLabel)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text("Login", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSignupClick,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(50.dp)
            ) {
                Text("Sign Up", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onClaimClick) {
                Text("First-time login? Claim your account")
            }
        }

        Text(
            text = state.authorCredit,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
