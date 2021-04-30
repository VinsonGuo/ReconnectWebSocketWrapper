package com.vinsonguo.reconnectwebsocketwrapper.lib

import android.util.Log
import okhttp3.*
import okio.ByteString
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * ReconnectWebSocketWrapper is a simple wrapper class of okhttp's websocket.
 * This class extends WebSocket and can reconnect when websocket was close or failure.
 *
 * this is its work mechanism: it will start a timer to call okHttpClient.newWebSocket method
 * when received "onClosed" or "onFailure" events, and the timer will be canceled when received
 * "onOpen" event.
 */

class ReconnectWebSocketWrapper (
    private val okHttpClient: OkHttpClient,
    private val request: Request,
    listener: WebSocketListener
) : WebSocket {

    companion object {
        const val TAG = "WebSocketWrapper"
    }


    /**
     * reconnect configuration
     */
    var config: Config = Config()

    private val isConnected = AtomicBoolean(false)

    private val isConnecting = AtomicBoolean(false)

    /**
     * get the status of reconnection
     */
    val status: Status
        get() = if (isConnected.get()) Status.CONNECTED else if (isConnecting.get()) Status.CONNECTING else Status.DISCONNECT

    /**
     * if you want to listen the reconnection status change, you can set this listener
     */
    var onConnectStatusChangeListener: ((status: Status) -> Unit)? = null

    /**
     * this listener will be invoked before on reconnection
     *
     * if you want to modify request when reconnection, you can set this listener
     */
    var onPreReconnectListener: ((request: Request) -> Request) = { request -> request }

    /**
     * the count of attempt to reconnect
     */
    val reconnectAttemptCount = AtomicInteger(0)

    private var timer: Timer? = null

    private val webSocketListener = object : WebSocketListener() {
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "onClosed $code $reason")
            isConnected.compareAndSet(true, false)
            onConnectStatusChangeListener?.invoke(status)
            doReconnect()
            listener.onClosed(webSocket, code, reason)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            listener.onClosing(webSocket, code, reason)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "onFailure", t)
            isConnected.compareAndSet(true, false)
            onConnectStatusChangeListener?.invoke(status)
            doReconnect()
            listener.onFailure(webSocket, t, response)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            listener.onMessage(webSocket, text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            listener.onMessage(webSocket, bytes)
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "reconnect success")
            isConnected.compareAndSet(false, true)
            isConnecting.compareAndSet(true, false)

            onConnectStatusChangeListener?.invoke(status)

            synchronized(this) {
                timer?.cancel()
                timer = null
            }
            reconnectAttemptCount.set(0)
            listener.onOpen(webSocket, response)
        }
    }

    private var webSocket: WebSocket

    init {
        onConnectStatusChangeListener?.invoke(status)
        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
    }

    private fun doReconnect() {
        if (!config.isAllowReconnect) {
            return
        }
        if (isConnected.get() || isConnecting.get()) {
            return
        }
        isConnecting.compareAndSet(false, true)

        onConnectStatusChangeListener?.invoke(status)

        synchronized(this) {
            if (timer == null) {
                timer = Timer()
            }
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    Log.d(TAG, "reconnect attempt ${reconnectAttemptCount.getAndIncrement()}")
                    if (reconnectAttemptCount.get() > config.reconnectCount) {
                        Log.d(TAG, "reconnect attempt > reconnectCount, it won't be to reconnect")
                    }
                    webSocket.cancel()
                    val reconnectRequest = onPreReconnectListener.invoke(request)
                    webSocket = okHttpClient.newWebSocket(reconnectRequest, webSocketListener)
                }
            }, 0, config.reconnectInterval)
        }
    }

    override fun cancel() {
        isConnected.compareAndSet(true, false)
        onConnectStatusChangeListener?.invoke(status)
        timer?.cancel()
        timer = null
        webSocket.cancel()
    }

    override fun close(code: Int, reason: String?): Boolean {
        return webSocket.close(code, reason)
    }

    override fun queueSize(): Long {
        return webSocket.queueSize()
    }

    override fun request(): Request {
        return webSocket.request()
    }

    override fun send(text: String): Boolean {
        return webSocket.send(text)
    }

    override fun send(bytes: ByteString): Boolean {
        return webSocket.send(bytes)
    }

}