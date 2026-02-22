package com.example.deepsleep.model

import java.util.UUID
import android.graphics.drawable.Drawable

enum class WhitelistType {
    SUPPRESS,
    BACKGROUND
}

data class WhitelistItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val note: String = "",
    val type: WhitelistType,
    val packageName: String = "",
    val icon: Drawable? = null,
    val isSystem: Boolean = false
)
