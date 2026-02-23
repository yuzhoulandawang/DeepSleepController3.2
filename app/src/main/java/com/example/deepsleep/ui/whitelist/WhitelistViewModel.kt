package com.example.deepsleep.ui.whitelist

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.data.WhitelistRepository
import com.example.deepsleep.model.WhitelistItem
import com.example.deepsleep.model.WhitelistType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class WhitelistUiState(
    val currentType: WhitelistType = WhitelistType.SUPPRESS,
    val items: List<WhitelistItem> = emptyList()
)

class WhitelistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WhitelistRepository()

    private val _uiState = MutableStateFlow(WhitelistUiState())
    val uiState: StateFlow<WhitelistUiState> = _uiState.asStateFlow()

    private val _apps = MutableStateFlow<List<WhitelistItem>>(emptyList())
    val apps: StateFlow<List<WhitelistItem>> = _apps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadItems()
    }

    fun switchType(type: WhitelistType) {
        _uiState.value = _uiState.value.copy(currentType = type)
        loadItems()
    }

    private fun loadItems() {
        viewModelScope.launch {
            val items = repository.loadItems(
                getApplication(),
                _uiState.value.currentType
            )
            _uiState.value = _uiState.value.copy(items = items)
        }
    }

    suspend fun addItem(name: String, note: String, type: WhitelistType) {
        repository.addItem(getApplication(), name, note, type)
        loadItems()
    }

    suspend fun updateItem(item: WhitelistItem) {
        repository.updateItem(getApplication(), item)
        loadItems()
    }

    suspend fun deleteItem(item: WhitelistItem) {
        repository.deleteItem(getApplication(), item)
        loadItems()
    }

    fun loadInstalledApps(showSystemApps: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val packageManager = getApplication<Application>().packageManager
                val appsList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { appInfo ->
                        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0)
                        showSystemApps || !isSystem
                    }
                    .map { appInfo ->
                        WhitelistItem(
                            packageName = appInfo.packageName,
                            name = appInfo.loadLabel(packageManager).toString(),
                            icon = appInfo.loadIcon(packageManager),
                            isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0),
                            // 修复：添加 type 参数（根据上下文选择默认类型）
                            type = WhitelistType.SUPPRESS
                        )
                    }
                    .sortedBy { it.name.lowercase(Locale.getDefault()) }

                _apps.value = appsList
                LogRepository.info(tag = "WhitelistViewModel", message = "加载应用列表: ${appsList.size} 个应用")

            } catch (e: Exception) {
                LogRepository.error(tag = "WhitelistViewModel", message = "加载应用列表失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToWhitelist(items: List<WhitelistItem>) {
        viewModelScope.launch {
            try {
                val packagesToAdd = items.map { it.packageName }
                val currentItems = _uiState.value.items.toMutableList()
                val newItemNames = packagesToAdd.toSet()
                val updatedItems = currentItems.filterNot { it.name in newItemNames } + items.map { pkg ->
                    WhitelistItem(
                        id = (pkg.packageName).hashCode().toString(),
                        name = pkg.name,
                        note = pkg.note,
                        type = _uiState.value.currentType
                    )
                }

                _uiState.value = _uiState.value.copy(items = updatedItems)
                LogRepository.success("WhitelistViewModel", "添加 ${items.size} 个应用到白名单")
            } catch (e: Exception) {
                LogRepository.error("WhitelistViewModel", "添加到白名单失败: ${e.message}")
            }
        }
    }

    fun addToWhitelist(item: WhitelistItem) {
        addToWhitelist(listOf(item))
    }

    fun removeFromWhitelist(items: List<WhitelistItem>) {
        viewModelScope.launch {
            try {
                val packagesToRemove = items.map { it.name }.toSet()
                val currentItems = _uiState.value.items
                val updatedItems = currentItems.filter { it.name !in packagesToRemove }

                _uiState.value = _uiState.value.copy(items = updatedItems)
                LogRepository.info("WhitelistViewModel", "从白名单移除 ${items.size} 个应用")
            } catch (e: Exception) {
                LogRepository.error("WhitelistViewModel", "从白名单移除失败: ${e.message}")
            }
        }
    }

    fun removeFromWhitelist(item: WhitelistItem) {
        removeFromWhitelist(listOf(item))
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredApps(): List<WhitelistItem> {
        val query = _searchQuery.value.lowercase(Locale.getDefault())
        return if (query.isBlank()) {
            _apps.value
        } else {
            _apps.value.filter { it.name.lowercase(Locale.getDefault()).contains(query) }
        }
    }

    fun isAppInWhitelist(packageName: String): Boolean {
        return _uiState.value.items.any { it.packageName == packageName }
    }
}