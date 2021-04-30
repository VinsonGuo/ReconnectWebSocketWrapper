# ReconnectWebSocketWrapper

## Introduction

This is a wrapper class based on OkHttp's WebSocket implementation with automatic reconnection. 
It retains the WebSocket interface, while adding features such as getting connection status and 
setting connection status listener. It is easy to use and the following is how it is used.

If you don't need to be concerned about reconnection status, you can use it like this
```java
    WebSocket ws = ReconnectWebSocketWrapper(okHttpClient, request, webSocketListener);
```

If you need the status, you can use it like this
```java
    ReconnectWebSocketWrapper ws = new ReconnectWebSocketWrapper(okHttpClient, request, webSocketListener);
    
    ws.setOnConnectStatusChangeListener(status -> {
        // your code
        return null;
    });
```

```kotlin
    val ws = ReconnectWebSocketWrapper(okHttpClient, request, webSocketListener)
    webSocketWrapper.onConnectStatusChangeListener = {
        // your code
    }
```

other api:

```kotlin
    // set reconnection configuration
    webSocketWrapper.config = Config()

    // if reconnection url differ with initial url, you can rebuild a request from this listener
    webSocketWrapper.onPreReconnectListener = { request -> 
        // to build your request
        request
    }
```
