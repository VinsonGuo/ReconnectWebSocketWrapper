package com.vinsonguo.reconnectwebsocketwrapper;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vinsonguo.reconnectwebsocketwrapper.lib.ReconnectWebSocketWrapper;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivityJava extends AppCompatActivity {

    public static final String TAG = "MainActivityJava";

    private final ReconnectWebSocketWrapper ws = new ReconnectWebSocketWrapper(
            new OkHttpClient.Builder().pingInterval(5000, TimeUnit.MILLISECONDS).build(),
            new Request.Builder().url("wss://echo.websocket.org").build(),
            new WebSocketListener() {
                @Override
                public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    super.onClosed(webSocket, code, reason);
                    Log.d(TAG, "onClosed ");
                }

                @Override
                public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    super.onClosing(webSocket, code, reason);
                    Log.d(TAG, "onClosing ");
                }

                @Override
                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @org.jetbrains.annotations.Nullable Response response) {
                    super.onFailure(webSocket, t, response);
                    Log.d(TAG, "onFailure ");
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    super.onMessage(webSocket, text);
                    Log.d(TAG, "onMessage ");
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                    super.onMessage(webSocket, bytes);
                    Log.d(TAG, "onMessage ");
                }

                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    super.onOpen(webSocket, response);
                    Log.d(TAG, "onOpen ");
                }
            });
    private TextView tvLog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ws.setOnConnectStatusChangeListener(status -> {
            runOnUiThread(() -> tvLog.append("\n status: " + status));
            return null;
        });
        tvLog = findViewById(R.id.tvLog);
        EditText etSend = findViewById(R.id.etSend);
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            ws.send(etSend.getText().toString());
        });
        findViewById(R.id.btnDisconnect).setOnClickListener(v -> {
            ws.close(1000, "test close");
        });

    }
}
