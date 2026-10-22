package itmo.soa.grammy.service

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyStore
import java.time.Duration
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import org.eclipse.microprofile.config.ConfigProvider
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class MusicBandsClient {
    private val config = ConfigProvider.getConfig()
    private val baseUrl = value("music-bands.base-url")
    private val connectTimeoutSeconds = value("music-bands.connect-timeout-seconds").toLong()
    private val readTimeoutSeconds = value("music-bands.read-timeout-seconds").toLong()
    private val client: HttpClient = HttpClient.newBuilder()
        .sslContext(trustStoreContext())
        .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
        .build()

    /** Returns false only when Music Bands answers 404; transport failures remain exceptions. */
    fun bandExists(bandId: Long): Boolean {
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${baseUrl.trimEnd('/')}/music-bands/$bandId"))
            .timeout(Duration.ofSeconds(readTimeoutSeconds))
            .header("Accept", "application/json")
            .GET()
            .build()
        val response = try {
            client.send(request, HttpResponse.BodyHandlers.discarding())
        } catch (e: java.net.http.HttpTimeoutException) {
            throw ApiException(504, "MUSIC_BANDS_TIMEOUT", "Music Bands service did not respond in time", e)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw ApiException(502, "MUSIC_BANDS_UNAVAILABLE", "Music Bands service is unavailable", e)
        } catch (e: Exception) {
            throw ApiException(502, "MUSIC_BANDS_UNAVAILABLE", "Music Bands service is unavailable", e)
        }
        when (response.statusCode()) {
            200 -> return true
            404 -> return false
            else -> throw ApiException(502, "MUSIC_BANDS_BAD_GATEWAY", "Music Bands service returned HTTP ${response.statusCode()}")
        }
    }

    private fun trustStoreContext(): SSLContext {
        val store = KeyStore.getInstance("PKCS12")
        val trustStore = value("grammy.ssl.trust-store")
        requireNotNull(javaClass.classLoader.getResourceAsStream(trustStore)).use {
            store.load(it, value("grammy.ssl.trust-store-password").toCharArray())
        }
        val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        factory.init(store)
        return SSLContext.getInstance("TLS").also { it.init(null, factory.trustManagers, null) }
    }

    private fun value(name: String): String = config.getOptionalValue(name, String::class.java)
        .orElseThrow { IllegalStateException("Required configuration property $name is missing") }
}

class ApiException(val status: Int, val code: String, override val message: String, cause: Throwable? = null) : RuntimeException(message, cause)
