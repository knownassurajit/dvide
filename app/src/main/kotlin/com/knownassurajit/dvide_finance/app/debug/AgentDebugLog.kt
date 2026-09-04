package com.knownassurajit.dvide_finance.app.debug

import android.util.Log
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

internal object AgentDebugLog {
    fun log(
        location: String,
        message: String,
        hypothesisId: String,
        data: Map<String, Any?> = emptyMap(),
        runId: String = "post-fix",
    ) {
        // #region agent log
        val payload = JSONObject()
            .put("sessionId", "10dec4")
            .put("runId", runId)
            .put("timestamp", System.currentTimeMillis())
            .put("location", location)
            .put("message", message)
            .put("hypothesisId", hypothesisId)
            .put("data", JSONObject(data.mapValues { it.value ?: JSONObject.NULL }))
            .toString()
        thread(name = "dvide-debug-log", isDaemon = true) {
            try {
                File("/Users/knownassurajit/Documents/Codes/GitHub/dvide/.cursor/debug-10dec4.log")
                    .appendText(payload + "\n")
            } catch (_: Exception) {
            }
            try {
                val conn = URL("http://127.0.0.1:7855/ingest/b1ca1511-966d-41db-a1c4-7276496767fe")
                    .openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("X-Debug-Session-Id", "10dec4")
                conn.doOutput = true
                conn.connectTimeout = 400
                conn.readTimeout = 400
                conn.outputStream.use { it.write(payload.toByteArray()) }
                conn.inputStream.close()
                conn.disconnect()
            } catch (_: Exception) {
            }
            Log.d("DvideDebug", payload)
        }
        // #endregion
    }
}
