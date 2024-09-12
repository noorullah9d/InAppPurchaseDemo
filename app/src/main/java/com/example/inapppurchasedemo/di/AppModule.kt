package com.example.inapppurchasedemo.di

import android.app.Application
import android.content.Context
import com.example.inapppurchasedemo.premium.BillingViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideBillingViewModel(
        context: Context
    ): BillingViewModel {
        return BillingViewModel(context)
    }
}