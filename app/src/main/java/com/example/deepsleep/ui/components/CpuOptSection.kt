package com.example.deepsleep.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CpuOptSection(
    enabled: Boolean,
    currentGovernor: String,
    minFrequency: Int,
    maxFrequency: Int,
    onToggle: (Boolean) -> Unit,
    onGovernorChange: (String) -> Unit,
    onFrequencyChange: (Int, Int) -> Unit
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
                    text = "CPU 调度器",
                    style = MaterialTheme.typography.titleMedium
                )
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle
                )
            }
            
            if (enabled) {
                Divider()
                
                // 调节器选择
                var selectedGovernor by remember { mutableStateOf(currentGovernor) }
                val governors = listOf("performance", "ondemand", "conservative", "powersave", "interactive")
                
                Text(
                    text = "当前调节器: $selectedGovernor",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    governors.forEach { governor ->
                        FilterChip(
                            selected = selectedGovernor == governor,
                            onClick = {
                                selectedGovernor = governor
                                onGovernorChange(governor)
                            },
                            label = { Text(governor) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 频率设置
                Text(
                    text = "频率范围: ${minFrequency}MHz - ${maxFrequency}MHz",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var minFreqText by remember { mutableStateOf(minFrequency.toString()) }
                    var maxFreqText by remember { mutableStateOf(maxFrequency.toString()) }
                    
                    OutlinedTextField(
                        value = minFreqText,
                        onValueChange = { value ->
                            minFreqText = value
                            value.toIntOrNull()?.let { min ->
                                maxFreqText.toIntOrNull()?.let { max ->
                                    onFrequencyChange(min, max)
                                }
                            }
                        },
                        label = { Text("最小频率") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = maxFreqText,
                        onValueChange = { value ->
                            maxFreqText = value
                            minFreqText.toIntOrNull()?.let { min ->
                                value.toIntOrNull()?.let { max ->
                                    onFrequencyChange(min, max)
                                }
                            }
                        },
                        label = { Text("最大频率") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
