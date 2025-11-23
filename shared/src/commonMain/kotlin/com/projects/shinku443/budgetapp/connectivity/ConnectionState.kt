package com.projects.shinku443.budgetapp.connectivity

sealed class ConnectionState {
    object Connected : ConnectionState()
    object Disconnected : ConnectionState()
}
