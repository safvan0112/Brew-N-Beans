package com.example.coffeeshop.di

import com.example.coffeeshop.data.repository.AuthRepository
import com.example.coffeeshop.data.repository.CartRepository
import com.example.coffeeshop.data.repository.MenuRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAuthRepo(auth: FirebaseAuth): AuthRepository =
        AuthRepository(auth)

    @Provides
    @Singleton
    fun provideMenuRepo(db: FirebaseFirestore): MenuRepository =
        MenuRepository(db)

    @Provides
    @Singleton // Must be Singleton so Menu and Cart screens share the exact same instance
    fun provideCartRepo(): CartRepository =
        CartRepository()
}