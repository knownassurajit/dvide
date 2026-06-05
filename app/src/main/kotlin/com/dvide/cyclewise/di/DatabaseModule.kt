package com.dvide.app.di

import android.content.Context
import androidx.room.Room
import com.dvide.app.data.local.CyclewiseDatabase
import com.dvide.app.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CyclewiseDatabase =
        Room.databaseBuilder(
            context,
            CyclewiseDatabase::class.java,
            CyclewiseDatabase.DATABASE_NAME,
        ).build()

    @Provides
    fun provideTransactionDao(db: CyclewiseDatabase): TransactionDao = db.transactionDao()
}
