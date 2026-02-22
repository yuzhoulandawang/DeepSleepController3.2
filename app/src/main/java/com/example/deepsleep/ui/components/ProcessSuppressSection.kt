package com.example.deepsleep.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProcessSuppressSection(
    enabled: Boolean,
    suppressLevel: Int,
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
                    text = "进程压制",
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
                    text = "压制等级: $suppressLevel",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = suppressLevel.toFloat(),
                    onValueChange = { onLevelChange(it.toInt()) },
                    valueRange = 1f..3f,
                    steps = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = when (suppressLevel) {
                        1 -> "轻度压制 - 仅限制高耗能进程"
                        2 -> "中度压制 - 限制后台进程数量"
                        3 -> "重度压制 - 最严格进程限制"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
