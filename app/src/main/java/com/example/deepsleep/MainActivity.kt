package com.example.deepsleep

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.deepsleep.ui.components.GlobalToastManager
import com.example.deepsleep.ui.components.ToastComponent
import com.example.deepsleep.ui.cpu.CpuParamsScreen
import com.example.deepsleep.ui.main.MainScreen
import com.example.deepsleep.ui.main.MainViewModel
import com.example.deepsleep.ui.settings.SettingsScreen
import com.example.deepsleep.ui.logs.LogsScreen
import com.example.deepsleep.ui.whitelist.WhitelistScreen
import com.example.deepsleep.ui.theme.DeepSleepTheme
import com.example.deepsleep.root.RootCommander
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        lifecycleScope.launch {
            val rootAccessGranted = RootCommander.requestRootAccess()
            
            if (rootAccessGranted) {
                val hasRoot = RootCommander.checkRoot()
                if (hasRoot) {
                    GlobalToastManager.showSuccess("Root 权限已获取")
                } else {
                    GlobalToastManager.showError("Root 授权失败，请手动授予权限")
                }
            } else {
                GlobalToastManager.showError("Root 权限请求失败，设备可能未 root")
            }
        }

        setContent {
            ToastComponent()
            
            DeepSleepTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "main"
                    ) {
                        composable("main") {
                            MainScreen(
                                viewModel = viewModel
                            )
                        }
                        composable("logs") {
                            LogsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("whitelist") {
                            WhitelistScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("cpuParams") {
                            CpuParamsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            viewModel.refreshRootStatus()
        }
    }
}