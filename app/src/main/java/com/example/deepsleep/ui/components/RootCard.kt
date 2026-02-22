package com.example.deepsleep.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RootCard(
    rootAvailable: Boolean,
    onRequestRoot: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (rootAvailable) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (rootAvailable) "✅ Root 权限已获取" else "❌ Root 权限未获取",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = if (rootAvailable) 
                    "所有优化功能可用" 
                else 
                    "部分优化功能受限",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (!rootAvailable) {
                Button(
                    onClick = onRequestRoot,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("请求 Root 权限")
                }
            }
        }
    }
}
