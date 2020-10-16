package launcher.minimalist.com.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import launcher.minimalist.com.HomeRepository
import launcher.minimalist.com.network.WeatherApi
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun provideHomeRepository(
            @ApplicationContext context: Context,
            weatherApi: WeatherApi,
    ): HomeRepository =
            HomeRepository(
                    context,
                    weatherApi
            )
}