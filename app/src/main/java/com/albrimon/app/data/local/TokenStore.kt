package com.albrimon.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.albrimon.app.domain.model.Session
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "albrimon_session")

/** Sesión persistente con DataStore: sobrevive al cierre de la app. */
@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = context.applicationContext.sessionDataStore

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USER_ID = stringPreferencesKey("user_id")
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val ROLE = stringPreferencesKey("role")
    }

    /** null cuando no hay sesión iniciada. */
    val session: Flow<Session?> = store.data.map { prefs ->
        val token = prefs[Keys.TOKEN]
        if (token.isNullOrBlank()) {
            null
        } else {
            Session(
                token = token,
                userId = prefs[Keys.USER_ID].orEmpty(),
                name = prefs[Keys.NAME].orEmpty(),
                email = prefs[Keys.EMAIL].orEmpty(),
                role = prefs[Keys.ROLE].orEmpty()
            )
        }
    }

    suspend fun hasSession(): Boolean = session.first() != null

    suspend fun save(session: Session) {
        store.edit {
            it[Keys.TOKEN] = session.token
            it[Keys.USER_ID] = session.userId
            it[Keys.NAME] = session.name
            it[Keys.EMAIL] = session.email
            it[Keys.ROLE] = session.role
        }
    }

    suspend fun clear() {
        store.edit { it.clear() }
    }
}
