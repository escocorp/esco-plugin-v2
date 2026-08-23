package plugin.ai

import plugin.model.ProviderVersionResponse

interface Provider {
    suspend fun getVersion(): ProviderVersionResponse
    // suspend fun chat()
}
