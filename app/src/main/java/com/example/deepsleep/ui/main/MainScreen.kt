package com.example.deepsleep.ui.main

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.deepsleep.ui.components.CpuOptSection
import com.example.deepsleep.ui.components.DeepSleepSection
import com.example.deepsleep.ui.components.DozeSection
import com.example.deepsleep.ui.components.ProcessSuppressSection
import com.example.deepsleep.ui.components.BackgroundOptSection
import com.example.deepsleep.ui.components.PowerSaverSection
import com.example.deepsleep.ui.components.CustomOptSection
import com.example.deepsleep.ui.components.RootCard

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // 观察所有状态
    val uiState by viewModel.uiState.collectAsState()
    val rootAvailable by viewModel.rootAvailable.collectAsState()
    val cpuParams by viewModel.cpuParams.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 标题
        Text(
            text = "优化控制台",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Divider()
        
        // Root 权限状态卡片
        RootCard(
            rootAvailable = rootAvailable,
            onRequestRoot = { viewModel.requestRoot() }
        )
        
        // CPU 调度器优化
        CpuOptSection(
            enabled = uiState.cpuOptEnabled,
            currentGovernor = cpuParams.governor,
            minFrequency = cpuParams.minFreq,
            maxFrequency = cpuParams.maxFreq,
            onToggle = { viewModel.toggleCpuOptimization() },
            onGovernorChange = { viewModel.setCpuGovernor(it) },
            onFrequencyChange = { min, max -> viewModel.setCpuFrequency(min, max) }
        )
        
        // 深度睡眠优化
        DeepSleepSection(
            enabled = uiState.deepSleepHookEnabled,
            batterySaving = uiState.powerSaverEnabled,
            onToggle = { viewModel.toggleDeepSleep() },
            onBatterySavingToggle = { viewModel.togglePowerSaver() }
        )
        
        // Doze 模式优化
        DozeSection(
            enabled = uiState.dozeEnabled,
            onToggle = { viewModel.toggleDoze() },
            onForceIdle = { viewModel.forceDoze() }
        )
        
        // 进程压制优化
        ProcessSuppressSection(
            enabled = uiState.processSuppressEnabled,
            suppressLevel = uiState.suppressLevel,
            onToggle = { viewModel.toggleProcessSuppress() },
            onLevelChange = { viewModel.setSuppressLevel(it) }
        )
        
        // 后台优化
        BackgroundOptSection(
            enabled = uiState.backgroundOptEnabled,
            optimizationLevel = uiState.backgroundOptLevel,
            onToggle = { viewModel.toggleBackgroundOpt() },
            onLevelChange = { viewModel.setBackgroundOptLevel(it) }
        )
        
        // 省电模式
        PowerSaverSection(
            enabled = uiState.powerSaverEnabled,
            powerSaverLevel = uiState.powerSaverLevel,
            onToggle = { viewModel.togglePowerSaver() },
            onLevelChange = { viewModel.setPowerSaverLevel(it) }
        )
        
        // 自定义优化模式
        CustomOptSection(
            currentMode = uiState.customMode,
            onModeSelect = { viewModel.applyCustomMode(it) }
        )
    }
}
