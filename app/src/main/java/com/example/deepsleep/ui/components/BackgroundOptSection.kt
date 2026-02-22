package com.example.deepsleep.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BackgroundOptSection(
    enabled: Boolean,
    optimizationLevel: Int,
    onToggle: (Boolean) -> Unit,
    onLevelChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "后台优化",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle
                )
            }
            
            if (enabled) {
                Divider()
                
                Text(
                    text = "优化等级: $optimizationLevel",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = optimizationLevel.toFloat(),
                    onValueChange = { onLevelChange(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = when (optimizationLevel) {
                        1 -> "轻度优化 - 仅限制高耗能应用"
                        2 -> "中度优化 - 限制后台同步"
                        3 -> "标准优化 - 平衡性能与功耗"
                        4 -> "强力优化 - 限制后台进程"
                        5 -> "极致优化 - 最严格后台限制"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
