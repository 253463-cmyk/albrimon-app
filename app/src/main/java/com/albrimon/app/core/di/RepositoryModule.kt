package com.albrimon.app.core.di

import com.albrimon.app.data.repository.AuthRepositoryImpl
import com.albrimon.app.data.repository.ClientRepositoryImpl
import com.albrimon.app.data.repository.ProjectRepositoryImpl
import com.albrimon.app.data.repository.ScrapRepositoryImpl
import com.albrimon.app.domain.repository.AuthRepository
import com.albrimon.app.domain.repository.ClientRepository
import com.albrimon.app.domain.repository.ProjectRepository
import com.albrimon.app.domain.repository.ScrapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt: enlaza cada contrato de domain/repository con su implementación en data/repository. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindClientRepository(impl: ClientRepositoryImpl): ClientRepository

    @Binds
    @Singleton
    abstract fun bindProjectRepository(impl: ProjectRepositoryImpl): ProjectRepository

    @Binds
    @Singleton
    abstract fun bindScrapRepository(impl: ScrapRepositoryImpl): ScrapRepository
}
