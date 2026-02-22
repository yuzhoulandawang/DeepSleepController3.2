package com.example.deepsleep.ui.components
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.*

/** 
 * Toast 消息类型
 */
enum class ToastType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

/**
 * Toast 消息数据类
 */
data class ToastMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ToastType,
    val title: String,
    val message: String = "",
    val duration: Long = 3000L,
    val action: (() -> Unit)? = null,
    val actionText: String? = null
)

/** 
 * 全局 Toast 管理器
 */
class ToastManager {
    private val _toasts = mutableStateListOf<ToastMessage>()
    val toasts: List<ToastMessage> = _toasts
    
    /**
     * 显示成功提示
     */
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
    
    /** 
     * 显示错误提示
     */
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
    
    /**
     * 显示警告提示
     */
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
    
    /** 
     * 显示信息提示
     */
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
    
    /**
     * 显示自定义 Toast
     */
    private fun showToast(message: ToastMessage) {
        _toasts.add(message)
        
        // 自动移除
        CoroutineScope(Dispatchers.Main).launch {
            delay(message.duration)
            _toasts.remove(message)
        }
    }
    
    /** 
     * 移除指定 Toast
     */
    fun dismiss(id: String) {
        _toasts.removeAll { it.id == id }
    }
    
    /**
     * 清除所有 Toast
     */
    fun clearAll() {
        _toasts.clear()
    }
}

/** 
 * Toast 组件
 */
@Composable
fun ToastComponent(
    manager: ToastManager,
    modifier: Modifier = Modifier
) {
    val toasts by rememberUpdatedState(manager.toasts)
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            toasts.forEach { toast ->
                AnimatedToast(
                    message = toast,
                    onDismiss = { manager.dismiss(toast.id) }
                )
            }
        }
    }
}

/**
 * 动画 Toast
 */
@Composable
fun AnimatedToast(
    message: ToastMessage,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    
    val enterTransition = remember {
        slideInVertically(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { it }
        ) + fadeIn(
            animationSpec = tween(durationMillis = 300)
        )
    }
    
    val exitTransition = remember {
        slideOutVertically(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            ),
            targetOffsetY = { it }
        ) + fadeOut(
            animationSpec = tween(durationMillis = 300)
        )
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = enterTransition,
        exit = exitTransition
    ) {
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

/** 
 * Toast 卡片
 */
@Composable
fun ToastCard(
    message: ToastMessage,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = when (message.type) {
        ToastType.SUCCESS -> Pair(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.onPrimary
        )
        ToastType.ERROR -> Pair(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError
        )
        ToastType.WARNING -> Pair(
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary
        )
        ToastType.INFO -> Pair(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.onSecondary
        )
    }
    
    val icon = when (message.type) {
        ToastType.SUCCESS -> Icons.Default.CheckCircle
        ToastType.ERROR -> Icons.Default.Error
        ToastType.WARNING -> Icons.Default.Warning
        ToastType.INFO -> Icons.Default.Info
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.first.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Icon(
                icon,
                contentDescription = null,
                tint = colors.second,
                modifier = Modifier.size(24.dp)
            )
            
            // 内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = message.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.second
                )
                
                if (message.message.isNotBlank()) {
                    Text(
                        text = message.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.second.copy(alpha = 0.8f)
                    )
                }
            }
            
            // 操作按钮
            if (message.action != null && message.actionText != null) {
                TextButton(
                    onClick = onAction,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colors.second
                    )
                ) {
                    Text(
                        text = message.actionText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // 关闭按钮
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = colors.second,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 全局 Toast 管理器实例
 */
val GlobalToastManager = ToastManager()

/** 
 * 快捷显示成功提示
 */
fun showSuccessToast(
    title: String,
    message: String = "",
    duration: Long = 3000L,
    action: (() -> Unit)? = null,
    actionText: String? = null
) {
    GlobalToastManager.showSuccess(title, message, duration, action, actionText)
}

/**
 * 快捷显示错误提示
 */
fun showErrorToast(
    title: String,
    message: String = "",
    duration: Long = 5000L,
    action: (() -> Unit)? = null,
    actionText: String? = null
) {
    GlobalToastManager.showError(title, message, duration, action, actionText)
}

/** 
 * 快捷显示警告提示
 */
fun showWarningToast(
    title: String,
    message: String = "",
    duration: Long = 4000L,
    action: (() -> Unit)? = null,
    actionText: String? = null
) {
    GlobalToastManager.showWarning(title, message, duration, action, actionText)
}

/**
 * 快捷显示信息提示
 */
fun showInfoToast(
    title: String,
    message: String = "",
    duration: Long = 3000L,
    action: (() -> Unit)? = null,
    actionText: String? = null
) {
    GlobalToastManager.showInfo(title, message, duration, action, actionText)
}

/** 
 * Snackbar 提示组件（用于更持久的操作反馈）
 * 注意：此组件使用了 androidx.compose.material3.SnackbarHost 的名称
 * 如果需要使用，请在调用时使用完全限定名
 */
@Composable
fun DeepSleepSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.padding(16.dp),
        snackbar = { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                shape = RoundedCornerShape(8.dp)
            ) {
                snackbarData.visuals.actionLabel?.let { actionLabel ->
                    TextButton(onClick = snackbarData::performAction) {
                        Text(actionLabel)
                    }
                }
            }
        }
    )
}

/**
 * 操作确认对话框
 */
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
        icon = {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}

/** 
 * 加载进度对话框
 */
@Composable
fun LoadingDialog(
    message: String = "加载中...",
    visible: Boolean
) {
    if (visible) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * 进度指示器组件
 */
@Composable
fun ProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (label != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
    }
}

/** 
 * 状态指示器组件
 */
@Composable
fun StatusIndicator(
    status: Boolean,
    activeText: String = "已启用",
    inactiveText: String = "已禁用",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (status) Color.Green else Color.Gray,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
        Text(
            text = if (status) activeText else inactiveText,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}