package com.projects.shinku443.budgetapp.ui.utils

import com.projects.shinku443.budgetapp.R

fun iconNameToRes(name: String): Int? {
    return when (name) {
        "ic_food" -> R.drawable.ic_category_food
        "ic_home" -> R.drawable.ic_category_home
//        "ic_transport" -> R.drawable.ic_transport
//        "ic_shopping" -> R.drawable.ic_category_shopping
        "ic_savings" -> R.drawable.ic_category_savings
        else -> R.drawable.ic_notification
    }
}