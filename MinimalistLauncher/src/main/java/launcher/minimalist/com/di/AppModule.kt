package launcher.minimalist.com.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import launcher.minimalist.com.HomeRepository
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun provideHomeRepository(
            @ApplicationContext context: Context,
    ): HomeRepository =
            HomeRepository(
                    context,
            )
}