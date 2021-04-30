package com.vinsonguo.reconnectwebsocketwrapper

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vinsonguo.reconnectwebsocketwrapper.lib.Config
import com.vinsonguo.reconnectwebsocketwrapper.lib.ReconnectWebSocketWrapper
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private val ws by lazy {
        val webSocketWrapper = ReconnectWebSocketWrapper(
            OkHttpClient.Builder().pingInterval(5000, TimeUnit.MILLISECONDS).build(),
            Request.Builder().url("wss://echo.websocket.org").build(),
            object : WebSocketListener() {
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    Log.d(TAG, "onClosed $reason")
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosing(webSocket, code, reason)
                    Log.d(TAG, "onClosing")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    Log.d(TAG, "onFailure")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    Log.d(TAG, "onMessage $text")
                    runOnUiThread {
                        tvLog.append("\n onMessage: $text")
                    }
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    super.onMessage(webSocket, bytes)
                    Log.d(TAG, "onMessage $bytes")
                }

                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    Log.d(TAG, "onOpen")
                }
            })
        webSocketWrapper.onConnectStatusChangeListener = {
            runOnUiThread {
                tvLog.append("\n connect status: $it")
            }
        }
        webSocketWrapper.config = Config()
        webSocketWrapper.onPreReconnectListener = {request ->
            // to build your request
            request
        }
        webSocketWrapper
    }

    private lateinit var tvLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLog = findViewById(R.id.tvLog)
        val etSend = findViewById<TextView>(R.id.etSend)
        findViewById<View>(R.id.btnDisconnect).setOnClickListener {
            ws.close(
                1000,
                "test to disconnect"
            )
        }
        findViewById<View>(R.id.btnSend).setOnClickListener {
            val text = etSend.text.toString()
            if(ws.send(text)) {
                tvLog.append("\n sendMessage: $text")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ws.cancel()
    }
}