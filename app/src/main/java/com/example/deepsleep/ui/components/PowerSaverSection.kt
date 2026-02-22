package com.example.deepsleep.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PowerSaverSection(
    enabled: Boolean,
    powerSaverLevel: Int,
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
                    text = "省电模式",
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
                    text = "省电等级: $powerSaverLevel",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = powerSaverLevel.toFloat(),
                    onValueChange = { onLevelChange(it.toInt()) },
                    valueRange = 1f..3f,
                    steps = 1,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text(
                    text = when (powerSaverLevel) {
                        1 -> "轻度省电 - 降低屏幕亮度"
                        2 -> "中度省电 - 降低CPU频率"
                        3 -> "重度省电 - 关闭非必要功能"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
