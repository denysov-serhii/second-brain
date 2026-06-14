package com.secondbrain.android.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.secondbrain.android.data.local.AppDatabase
import com.secondbrain.android.data.local.LogDao
import com.secondbrain.android.data.remote.LogApiService
import com.secondbrain.android.data.repository.LogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "second_brain.db"
    ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideLogDao(database: AppDatabase): LogDao = database.logDao()

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.secondbrain.example/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideLogApiService(retrofit: Retrofit): LogApiService =
        retrofit.create(LogApiService::class.java)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideLogRepository(
        logDao: LogDao,
        workManager: WorkManager,
        logApiService: LogApiService
    ): LogRepository = LogRepository(
        logDao = logDao,
        workManager = workManager,
        logApiService = logApiService
    )
}

