package com.dvide.app.di

import android.content.Context
import androidx.room.Room
import com.dvide.app.data.local.DvideDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): DvideDatabase =
        Room.databaseBuilder(
            context,
            DvideDatabase::class.java,
            DvideDatabase.DATABASE_NAME,
        ).build()

    @Provides
    fun provideTransactionDao(db: DvideDatabase): TransactionDao = db.transactionDao()
}
