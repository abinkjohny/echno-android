package org.echno.feature.organization.data

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
abstract class OrganizationDataModule {
    @Binds
    abstract fun bindOrganizationRepository(repository: DefaultOrganizationRepository): OrganizationRepository

    companion object {
        @Provides
        @Singleton
        fun provideOrganizationApi(retrofit: Retrofit): OrganizationApi = retrofit.create()
    }
}
