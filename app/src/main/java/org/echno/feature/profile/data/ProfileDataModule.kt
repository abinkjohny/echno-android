package org.echno.feature.profile.data

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
abstract class ProfileDataModule {
    @Binds
    abstract fun bindProfileRepository(repository: DefaultProfileRepository): ProfileRepository

    companion object {
        @Provides
        @Singleton
        fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create()
    }
}
