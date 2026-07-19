package com.example.hkradio

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches the channel list from the CDN at runtime, falling back to the bundled
 * list on any network/parse failure. Matches the app's existing "singleton object +
 * plain state" style (like [RadioPlayer]) instead of introducing a ViewModel.
 */
object ChannelRepository {
    private const val TAG = "ChannelRepository"

    // In-memory cache so rotation / recomposition never refetches within a process.
    // @Volatile: written on Dispatchers.IO, read from the main thread.
    @Volatile
    private var cache: List<ChannelData>? = null

    suspend fun fetchChannels(): List<ChannelData> {
        cache?.let { return it }

        return withContext(Dispatchers.IO) {
            val result = try {
                parseChannels(downloadText(Config.CHANNELS_URL))
            } catch (e: Exception) {
                // Explicit, logged fallback — never a silent empty screen.
                Log.w(TAG, "channels.json fetch/parse failed; using bundled fallback: ${e.message}", e)
                ChannelData.fallback
            }
            cache = result
            result
        }
    }

    private fun downloadText(urlString: String): String {
        val conn = (URL(urlString).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        try {
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP $code for $urlString")
            }
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    private fun parseChannels(jsonText: String): List<ChannelData> {
        val root = JSONObject(jsonText)
        // Read defensively; ignored for now. Gate future breaking-schema logic on this.
        @Suppress("UNUSED_VARIABLE")
        val version = root.optInt("version", 1)

        val array = root.getJSONArray("channels")
        val list = ArrayList<ChannelData>(array.length())
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            list.add(
                ChannelData(
                    id = o.getString("id"),
                    artist = o.getString("artist"),
                    name = o.getString("name"),
                    link = o.getString("link"),
                    // Relative "images/rthk1.png" -> absolute CDN URL.
                    artwork = Config.CDN_BASE + o.getString("artwork")
                )
            )
        }
        if (list.isEmpty()) throw IOException("channels array is empty") // -> triggers fallback
        return list
    }
}
