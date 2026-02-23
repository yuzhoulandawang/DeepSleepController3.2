package com.example.deepsleep.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.*

enum class ToastType { SUCCESS, ERROR, WARNING, INFO }

data class ToastMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ToastType,
    val title: String,
    val message: String = "",
    val duration: Long = 3000L,
    val action: (() -> Unit)? = null,
    val actionText: String? = null
)

class ToastManager {
    private val _toasts = mutableStateListOf<ToastMessage>()
    val toasts: List<ToastMessage> = _toasts

    // 所有 show 方法统一使用命名参数调用 ToastMessage
    fun showSuccess(
        title: String,
        message: String = "",
        duration: Long = 3000L,
        action: (() -> Unit)? = null,
        actionText: String? = null
    ) {
        showToast(
            ToastMessage(
                type = ToastType.SUCCESS,
                title = title,
                message = message,
                duration = duration,
                action = action,
                actionText = actionText
            )
        )
    }

    fun showError(
        title: String,
        message: String = "",
        duration: Long = 5000L,
        action: (() -> Unit)? = null,
        actionText: String? = null
    ) {
        showToast(
            ToastMessage(
                type = ToastType.ERROR,
                title = title,
                message = message,
                duration = duration,
                action = action,
                actionText = actionText
            )
        )
    }

    fun showWarning(
        title: String,
        message: String = "",
        duration: Long = 4000L,
        action: (() -> Unit)? = null,
        actionText: String? = null
    ) {
        showToast(
            ToastMessage(
                type = ToastType.WARNING,
                title = title,
                message = message,
                duration = duration,
                action = action,
                actionText = actionText
            )
        )
    }

    fun showInfo(
        title: String,
        message: String = "",
        duration: Long = 3000L,
        action: (() -> Unit)? = null,
        actionText: String? = null
    ) {
        showToast(
            ToastMessage(
                type = ToastType.INFO,
                title = title,
                message = message,
                duration = duration,
                action = action,
                actionText = actionText
            )
        )
    }

    private fun showToast(message: ToastMessage) {
        _toasts.add(message)
        CoroutineScope(Dispatchers.Main).launch {
            delay(message.duration)
            _toasts.remove(message)
        }
    }

    fun dismiss(id: String) { _toasts.removeAll { it.id == id } }
    fun clearAll() { _toasts.clear() }
}

@Composable
fun ToastComponent(manager: ToastManager, modifier: Modifier = Modifier) {
    val toasts by rememberUpdatedState(manager.toasts)
    Box(modifier.fillMaxSize(), Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            toasts.forEach { toast ->
                AnimatedToast(toast) { manager.dismiss(toast.id) }
            }
        }
    }
}

@Composable
fun AnimatedToast(message: ToastMessage, onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    val enter = slideInVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeIn(tween(300))
    val exit = slideOutVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)) + fadeOut(tween(300))
    AnimatedVisibility(visible, enter = enter, exit = exit) {
        ToastCard(
            message = message,
            onAction = {
                message.action?.invoke()
                visible = false
                onDismiss()
            },
            onDismiss = {
                visible = false
                onDismiss()
            }
        )
    }
}

@Composable
fun ToastCard(message: ToastMessage, onAction: () -> Unit, onDismiss: () -> Unit) {
    val (containerColor, contentColor) = when (message.type) {
        ToastType.SUCCESS -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        ToastType.ERROR   -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        ToastType.WARNING -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        ToastType.INFO    -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
    }
    val icon = when (message.type) {
        ToastType.SUCCESS -> Icons.Default.CheckCircle
        ToastType.ERROR   -> Icons.Default.Error
        ToastType.WARNING -> Icons.Default.Warning
        ToastType.INFO    -> Icons.Default.Info
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
            Icon(icon, null, tint = contentColor, Modifier.size(24.dp))
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(message.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = contentColor)
                if (message.message.isNotBlank())
                    Text(message.message, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
            }
            if (message.action != null && message.actionText != null) {
                TextButton(onAction, colors = ButtonDefaults.textButtonColors(contentColor = contentColor)) {
                    Text(message.actionText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onDismiss, Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, "关闭", tint = contentColor, Modifier.size(18.dp))
            }
        }
    }
}

val GlobalToastManager = ToastManager()

fun showSuccessToast(
    title: String,
    message: String = "",
    duration: Long = 3000L,
    action: (() -> Unit)? = null,
    actionText: String? = null
) {
    GlobalToastManager.showSuccess(title, message, duration, action, actionText)
}

fun showErrorToast(
    title: String,
    message: String = "",
    duration: Long = 5000L,
    action: (() -> Unit)? = null,
    actionText: String? = null
) {
    GlobalToastManager.showError(title, message, duration, action, actionText)
}

fun showWarningToast(
    title: String,
    message: String = "",
    duration: Long = 4000L,
    action: (() -> Unit)? = null,
    actionText: String? = null
) {
    GlobalToastManager.showWarning(title, message, duration, action, actionText)
}

fun showInfoToast(
    title: String,
    message: String = "",
    duration: Long = 3000L,
    action: (() -> Unit)? = null,
    actionText: String? = null
) {
    GlobalToastManager.showInfo(title, message, duration, action, actionText)
}

@Composable
fun DeepSleepSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier.padding(16.dp)) { data ->
        Snackbar(data, shape = RoundedCornerShape(8.dp)) {
            data.visuals.actionLabel?.let { actionLabel ->
                TextButton(onClick = data::performAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Warning,
    iconColor: Color = MaterialTheme.colorScheme.tertiary
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, null, tint = iconColor, Modifier.size(32.dp)) },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = { Button(onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onDismiss) { Text(dismissText) } }
    )
}

@Composable
fun LoadingDialog(message: String = "加载中...", visible: Boolean) {
    if (visible) Dialog(onDismissRequest = {}) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.bodySmall)
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
            }
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun StatusIndicator(
    status: Boolean,
    activeText: String = "已启用",
    inactiveText: String = "已禁用",
    modifier: Modifier = Modifier
) {
    Row(modifier, Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(if (status) Color.Green else Color.Gray, androidx.compose.foundation.shape.CircleShape))
        Text(if (status) activeText else inactiveText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}