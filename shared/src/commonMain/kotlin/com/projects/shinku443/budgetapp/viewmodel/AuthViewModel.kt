//package com.projects.shinku443.budgetapp.viewmodel
//
//import com.projects.shinku443.budgetapp.data.auth.AuthRepository
//import io.github.jan.supabase.auth.providers.OAuthProvider
//import kotlinx.coroutines.flow.MutableStateFlow
//
//class AuthViewModel(private val repo: AuthRepository) {
//    val authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
//
//    suspend fun signUp(email: String, password: String) {
//        runCatching { repo.signUpWithEmail(email, password) }
//            .onSuccess { authState.value = AuthState.LoggedIn(it) }
//            .onFailure { authState.value = AuthState.Error(it.message ?: "Unknown error") }
//    }
//
//    suspend fun login(email: String, password: String) {
//        runCatching { repo.loginWithEmail(email, password) }
//            .onSuccess { authState.value = AuthState.LoggedIn(it) }
//            .onFailure { authState.value = AuthState.Error(it.message ?: "Unknown error") }
//    }
//
//    suspend fun loginWithGoogle() {
//        runCatching { repo.loginWithProvider(OAuthProvider.Google) }
//            .onSuccess { authState.value = AuthState.LoggedIn(it) }
//            .onFailure { authState.value = AuthState.Error(it.message ?: "Unknown error") }
//    }
//
//    suspend fun logout() {
//        repo.logout()
//        authState.value = AuthState.LoggedOut
//    }
//}
//
//sealed class AuthState {
//    object LoggedOut : AuthState()
//    data class LoggedIn(val session: Any) : AuthState()
//    data class Error(val message: String) : AuthState()
//}
