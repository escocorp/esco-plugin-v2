package plugin.ai.ollama

import kotlinx.coroutines.future.await
import plugin.PVars
import plugin.ai.Provider
import plugin.model.ProviderVersionResponse
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class OllamaProvider(private val apiUrl: String) : Provider {
    private val client = HttpClient.newHttpClient()

    override suspend fun getVersion(): ProviderVersionResponse =
        get("/api/version")

    private suspend inline fun <reified T> get(path: String): T =
        send(
            HttpRequest.newBuilder()
                .uri(URI.create("$apiUrl$path"))
                .GET()
                .build()
        )

    private suspend inline fun <reified T> post(path: String, body: Any): T =
        send(
            HttpRequest.newBuilder()
                .uri(URI.create("$apiUrl$path"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(PVars.objectMapper.writeValueAsString(body)))
                .build()
        )

    private suspend inline fun <reified T> send(request: HttpRequest): T {
        val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).await()
        check(response.statusCode() in 200..299) {
            "Ollama request ${request.uri()} failed: ${response.statusCode()}"
        }
        return PVars.objectMapper.readValue(response.body(), T::class.java)
    }
}