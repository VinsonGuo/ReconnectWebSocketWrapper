package com.vinsonguo.reconnectwebsocketwrapper.lib

data class Config(
    val isAllowReconnect: Boolean = true,
    val reconnectCount: Int = Int.MAX_VALUE,
    val reconnectInterval: Long = 5000
)
