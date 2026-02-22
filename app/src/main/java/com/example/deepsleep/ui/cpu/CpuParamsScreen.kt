package com.example.deepsleep.ui.cpu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.deepsleep.model.CpuParams
import com.example.deepsleep.model.CpuGovernor

/**
 * CPU 参数设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuParamsScreen(
    viewModel: CpuParamsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val currentParams by viewModel.currentParams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val availableFreqs by viewModel.availableFreqs.collectAsState()
    val availableGovernors by viewModel.availableGovernors.collectAsState()
    
    // 可见性状态
    var showWaltParams by remember { mutableStateOf(false) }
    var showFreqParams by remember { mutableStateOf(false) }
    var showGovernorParams by remember { mutableStateOf(false) }
    
    // Toast 显示
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            kotlinx.coroutines.delay(3000)
            viewModel.clearToast()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CPU 参数设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 应用按钮
                    Button(
                        onClick = { viewModel.applyCustomParams() },
                        enabled = !isLoading && currentParams.isValid(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("应用")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // 恢复默认按钮
                    IconButton(onClick = { viewModel.restoreDefault() }, enabled = !isLoading) {
                        Icon(Icons.Default.Restore, contentDescription = "恢复默认")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 预设模式
                SectionTitle("预设模式", icon = Icons.Default.Tune)
                PresetModeRow(
                    selectedPreset = currentParams,
                    onPresetSelected = { viewModel.applyPreset(it) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // WALT 参数
                ExpandableSection(
                    title = "WALT 参数",
                    subtitle = "工作负载感知任务调度参数",
                    icon = Icons.Default.Speed,
                    isExpanded = showWaltParams,
                    onToggle = { showWaltParams = !showWaltParams }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SliderWithLabel(
                            label = "利用率阈值",
                            value = currentParams.utilThreshold.toFloat(),
                            valueRange = 0f..1000f,
                            onValueChange = { 
                                viewModel.updateParams(
                                    currentParams.copy(utilThreshold = it.toInt())
                                )
                            },
                            unit = ""
                        )
                        
                        SliderWithLabel(
                            label = "估算因子",
                            value = currentParams.utilEstFactor.toFloat(),
                            valueRange = 0f..200f,
                            onValueChange = { 
                                viewModel.updateParams(
                                    currentParams.copy(utilEstFactor = it.toInt())
                                )
                            },
                            unit = "%"
                        )
                        
                        SliderWithLabel(
                            label = "过滤阈值",
                            value = currentParams.utilFilter.toFloat(),
                            valueRange = 0f..100f,
                            onValueChange = { 
                                viewModel.updateParams(
                                    currentParams.copy(utilFilter = it.toInt())
                                )
                            },
                            unit = "%"
                        )
                        
                        SliderWithLabel(
                            label = "窗口大小",
                            value = currentParams.windowSize.toFloat(),
                            valueRange = 5f..50f,
                            onValueChange = { 
                                viewModel.updateParams(
                                    currentParams.copy(windowSize = it.toInt())
                                )
                            },
                            unit = "ms"
                        )
                        
                        SliderWithLabel(
                            label = "衰减率",
                            value = currentParams.decayRate.toFloat(),
                            valueRange = 1f..20f,
                            onValueChange = { 
                                viewModel.updateParams(
                                    currentParams.copy(decayRate = it.toInt())
                                )
                            },
                            unit = ""
                        )
                        
                        SliderWithLabel(
                            label = "最小负载",
                            value = currentParams.minLoad.toFloat(),
                            valueRange = 0f..100f,
                            onValueChange = { 
                                viewModel.updateParams(
                                    currentParams.copy(minLoad = it.toInt())
                                )
                            },
                            unit = "%"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // CPU 频率参数
                ExpandableSection(
                    title = "CPU 频率",
                    subtitle = "CPU 运行频率范围设置",
                    icon = Icons.Default.Memory,
                    isExpanded = showFreqParams,
                    onToggle = { showFreqParams = !showFreqParams }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 最小频率
                        Text(
                            text = "最小频率: ${currentParams.minFreq / 1000} MHz",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (availableFreqs.isNotEmpty()) {
                            FrequencySelector(
                                selectedFreq = currentParams.minFreq,
                                availableFreqs = availableFreqs.filter { it <= currentParams.maxFreq },
                                onFreqSelected = { viewModel.updateMinFreq(it) },
                                label = "选择最小频率"
                            )
                        }
                        
                        // 最大频率
                        Text(
                            text = "最大频率: ${currentParams.maxFreq / 1000} MHz",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (availableFreqs.isNotEmpty()) {
                            FrequencySelector(
                                selectedFreq = currentParams.maxFreq,
                                availableFreqs = availableFreqs.filter { it >= currentParams.minFreq },
                                onFreqSelected = { viewModel.updateMaxFreq(it) },
                                label = "选择最大频率"
                            )
                        }
                        
                        // 频率范围说明
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "频率范围: ${currentParams.minFreq / 1000}-${currentParams.maxFreq / 1000} MHz",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 调度器参数
                ExpandableSection(
                    title = "调度器设置",
                    subtitle = "CPU 调度器和阈值配置",
                    icon = Icons.Default.Settings,
                    isExpanded = showGovernorParams,
                    onToggle = { showGovernorParams = !showGovernorParams }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 调度器选择
                        Text(
                            text = "调度器模式",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (availableGovernors.isNotEmpty()) {
                            GovernorSelector(
                                selectedGovernor = currentParams.scalingGovernor,
                                availableGovernors = availableGovernors,
                                onGovernorSelected = { viewModel.updateGovernor(it) }
                            )
                        }
                        
                        SliderWithLabel(
                            label = "上升阈值",
                            value = currentParams.upThreshold.toFloat(),
                            valueRange = 0f..100f,
                            onValueChange = { 
                                viewModel.updateParams(
                                    currentParams.copy(
                                        upThreshold = it.toInt(),
                                        downThreshold = minOf(it.toInt(), currentParams.downThreshold)
                                    )
                                )
                            },
                            unit = "%"
                        )
                        
                        SliderWithLabel(
                            label = "下降阈值",
                            value = currentParams.downThreshold.toFloat(),
                            valueRange = 0f..currentParams.upThreshold.toFloat(),
                            onValueChange = { 
                                viewModel.updateParams(
                                    currentParams.copy(downThreshold = it.toInt())
                                )
                            },
                            unit = "%"
                        )
                        
                        SliderWithLabel(
                            label = "采样率",
                            value = currentParams.samplingRate.toFloat(),
                            valueRange = 1000f..50000f,
                            onValueChange = { 
                                viewModel.updateParams(
                                    currentParams.copy(samplingRate = it.toInt())
                                )
                            },
                            unit = "ms"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 参数验证提示
                if (!currentParams.isValid()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Column {
                                Text(
                                    text = "参数配置无效",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "请检查参数范围是否合理",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                
                // 底部提示
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "配置提示",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = """
                                • 调整参数后点击"应用"按钮生效
                                • 点击"恢复默认"可重置为系统默认值
                                • 不合理的参数可能导致性能下降
                                • 建议使用预设模式进行快速配置
                            """.trimIndent(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 加载遮罩
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "正在应用参数...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // Toast 提示
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = toastMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * 章节标题
 */
@Composable
fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 可展开章节
 */
@Composable
fun ExpandableSection(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Column {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    modifier = Modifier.rotate(rotation)
                )
            }
            
            // 内容
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Divider()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * 预设模式行
 */
@Composable
fun PresetModeRow(
    selectedPreset: CpuParams,
    onPresetSelected: (CpuParams) -> Unit
) {
    val presets = listOf(
        "日常" to CpuParams.DAILY_MODE,
        "待机" to CpuParams.STANDBY_MODE,
        "性能" to CpuParams.PERFORMANCE_MODE,
        "默认" to CpuParams.DEFAULT_MODE
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { (name, preset) ->
            PresetButton(
                name = name,
                isSelected = selectedPreset == preset,
                onClick = { onPresetSelected(preset) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 预设按钮
 */
@Composable
fun PresetButton(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) 
                MaterialTheme.colorScheme.onPrimary 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = if (!isSelected) 
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline) 
        else 
            null
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * 带标签的滑块
 */
@Composable
fun SliderWithLabel(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    unit: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${value.toInt()}$unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = ((valueRange.endInclusive - valueRange.start) / 10).toInt()
        )
    }
}

/**
 * 频率选择器
 */
@Composable
fun FrequencySelector(
    selectedFreq: Int,
    availableFreqs: List<Int>,
    onFreqSelected: (Int) -> Unit,
    label: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableFreqs) { freq ->
                FrequencyChip(
                    freq = freq,
                    isSelected = freq == selectedFreq,
                    onClick = { onFreqSelected(freq) }
                )
            }
        }
    }
}

/**
 * 频率芯片
 */
@Composable
fun FrequencyChip(
    freq: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = "${freq / 1000}MHz",
                fontSize = 12.sp
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/**
 * 调度器选择器
 */
@Composable
fun GovernorSelector(
    selectedGovernor: String,
    availableGovernors: List<String>,
    onGovernorSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        availableGovernors.forEach { governor ->
            val governorInfo = CpuGovernor.fromString(governor)
            
            GovernorOption(
                name = governor,
                displayName = governorInfo.displayName,
                description = governorInfo.description,
                isSelected = governor == selectedGovernor,
                onClick = { onGovernorSelected(governor) }
            )
        }
    }
}

/**
 * 调度器选项
 */
@Composable
fun GovernorOption(
    name: String,
    displayName: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else 
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
