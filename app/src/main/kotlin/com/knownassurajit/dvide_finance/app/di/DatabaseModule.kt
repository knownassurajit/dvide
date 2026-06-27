package com.knownassurajit.dvide_finance.app.di

import android.content.Context
import androidx.room.Room
import com.knownassurajit.dvide_finance.app.data.local.CycleDao
import com.knownassurajit.dvide_finance.app.data.local.DvideDatabase
import com.knownassurajit.dvide_finance.app.data.local.TransactionDao
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
    fun provideDatabase(@ApplicationContext context: Context): DvideDatabase =
        Room.databaseBuilder(
            context,
            DvideDatabase::class.java,
            DvideDatabase.DATABASE_NAME,
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideTransactionDao(db: DvideDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCycleDao(db: DvideDatabase): CycleDao = db.cycleDao()
}
