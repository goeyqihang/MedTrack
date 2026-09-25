package com.qihang.medtrack.ui.welcome

data class WelcomeUiState(
    val appTitle: String = "MedTrack",
    val disclaimer: String = "This app is for tracking purposes only and does not replace professional medical advice.",
    val clinicLinkLabel: String = "Visit Monash Health Clinic",
    val clinicLinkUrl: String = "https://www.monash.edu/health",
    val authorCredit: String = "Goey Qi Hang"
)
