//package com.projects.shinku443.budgetapp.data.auth
//
//import io.github.jan.supabase.auth.providers.OAuthProvider
//
//
//class AuthRepository {
//    private val supabaseAuth = SupabaseAuth(
//        supabaseUrl = SupabaseConfig.SUPABASE_URL,
//        supabaseKey = SupabaseConfig.SUPABASE_KEY
//    )
//
//    suspend fun signUpWithEmail(email: String, password: String) =
//        supabaseAuth.signUpWithEmail(email, password)
//
//    suspend fun loginWithEmail(email: String, password: String) =
//        supabaseAuth.loginWithEmail(email, password)
//
//    suspend fun loginWithProvider(provider: OAuthProvider) =
//        supabaseAuth.loginWithProvider(provider)
//
//    suspend fun logout() = supabaseAuth.logout()
//}
