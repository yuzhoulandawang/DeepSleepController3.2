package com.example.deepsleep.di

import android.content.Context
import com.example.deepsleep.data.LogRepository
import com.example.deepsleep.data.SettingsRepository
import com.example.deepsleep.data.StatsRepository
import com.example.deepsleep.data.WhitelistRepository
import com.example.deepsleep.root.BackgroundOptimizer
import com.example.deepsleep.root.DeepSleepController
import com.example.deepsleep.root.DozeController
import com.example.deepsleep.root.PowerSaverController
import com.example.deepsleep.root.ProcessSuppressor
import com.example.deepsleep.root.RootCommander
import com.example.deepsleep.root.WaltOptimizer
import com.example.deepsleep.utils.CustomOptimizer
import com.example.deepsleep.utils.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    // ========== Root 相关 ==========

    @Provides
    @Singleton
    fun provideRootCommander(): RootCommander {
        return RootCommander
    }

    @Provides
    @Singleton
    fun provideDozeController(): DozeController {
        return DozeController
    }

    @Provides
    @Singleton
    fun provideDeepSleepController(): DeepSleepController {
        return DeepSleepController
    }

    @Provides
    @Singleton
    fun providePowerSaverController(): PowerSaverController {
        return PowerSaverController
    }

    @Provides
    @Singleton
    fun provideBackgroundOptimizer(): BackgroundOptimizer {
        return BackgroundOptimizer
    }

    @Provides
    @Singleton
    fun provideProcessSuppressor(): ProcessSuppressor {
        return ProcessSuppressor
    }

    @Provides
    @Singleton
    fun provideWaltOptimizer(): WaltOptimizer {
        return WaltOptimizer
    }

    // ========== 数据层 ==========

    @Provides
    @Singleton
    fun provideLogRepository(): LogRepository {
        return LogRepository
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideStatsRepository(): StatsRepository {
        return StatsRepository
    }

    @Provides
    @Singleton
    fun provideWhitelistRepository(): WhitelistRepository {
        return WhitelistRepository()
    }

    // ========== 工具类 ==========

    @Provides
    @Singleton
    fun provideLogger(): Logger {
        return Logger
    }

    @Provides
    @Singleton
    fun provideCustomOptimizer(): CustomOptimizer {
        return CustomOptimizer()
    }

    // ========== 协程调度器 ==========

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
