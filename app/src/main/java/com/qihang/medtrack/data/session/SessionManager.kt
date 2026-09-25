package com.qihang.medtrack.data.session

import android.content.Context
import androidx.core.content.edit

/** Remembers which patient is logged in. */
interface SessionManager {

    /** ID of the logged-in patient, or null when nobody is logged in. */
    val loggedInPatientId: String?

    fun login(patientId: String)

    fun logout()
}

/** [SessionManager] backed by SharedPreferences, so the session survives app restarts. */
class SharedPrefsSessionManager(context: Context) : SessionManager {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override val loggedInPatientId: String?
        get() = prefs.getString(KEY_PATIENT_ID, null)

    override fun login(patientId: String) {
        prefs.edit { putString(KEY_PATIENT_ID, patientId) }
    }

    override fun logout() {
        prefs.edit { remove(KEY_PATIENT_ID) }
    }

    companion object {
        /** Preferences file, shared with [com.qihang.medtrack.data.DatabaseSeeder]. */
        const val PREFS_NAME = "MedTrackPrefs"
        private const val KEY_PATIENT_ID = "logged_in_patient_id"
    }
}
