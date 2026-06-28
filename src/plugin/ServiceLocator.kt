package plugin

import plugin.models.AppConfig

object ServiceLocator {
    var appConfig: AppConfig? = null
    
    fun provideConfig(): AppConfig {
        return appConfig ?: throw IllegalStateException("AppConfig not initialized in ServiceLocator")
    }
}
