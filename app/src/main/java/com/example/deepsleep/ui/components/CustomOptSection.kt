package com.example.deepsleep.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class CustomMode(
    val id: String,
    val name: String,
    val description: String,
    val icon: String
)

@Composable
fun CustomOptSection(
    currentMode: String,
    onModeSelect: (String) -> Unit
) {
    val modes = listOf(
        CustomMode("light", "轻度省电", "平衡性能", "🔋"),
        CustomMode("medium", "中度省电", "降低功耗", "📉"),
        CustomMode("heavy", "重度省电", "极致省电", "🌙"),
        CustomMode("game", "游戏模式", "性能优先", "🎮"),
        CustomMode("office", "办公模式", "稳定高效", "💼")
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "自定义优化模式",
                style = MaterialTheme.typography.titleMedium
            )
            
            Divider()
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(250.dp)
            ) {
                items(modes) { mode ->
                    FilterChip(
                        selected = currentMode == mode.id,
                        onClick = { onModeSelect(mode.id) },
                        label = {
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Text(mode.icon, style = MaterialTheme.typography.headlineSmall)
                                Text(mode.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    mode.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.height(100.dp)
                    )
                }
            }
        }
    }
}
