package com.batterysaver;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Button;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LogViewerActivity extends Activity {

    private TextView logTextView;
    private ScrollView scrollView;
    private Handler handler = new Handler();
    private boolean isMonitoring = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);

        logTextView = findViewById(R.id.logTextView);
        scrollView = findViewById(R.id.scrollView);
        Button backButton = findViewById(R.id.backButton);
        Button refreshButton = findViewById(R.id.refreshButton);
        Button clearButton = findViewById(R.id.clearButton);

        backButton.setOnClickListener(v -> finish());
        refreshButton.setOnClickListener(v -> updateLogs());
        clearButton.setOnClickListener(v -> logTextView.setText("Logs limpiados\n\n"));

        // Actualizar logs cada 2 segundos
        startMonitoring();
    }

    private void startMonitoring() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isMonitoring) {
                    updateLogs();
                    handler.postDelayed(this, 2000);
                }
            }
        }, 0);
    }

    private void updateLogs() {
        new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec(new String[]{
                        "su", "-c", "tail -n 50 /data/adb/service.d/govbattery.log"
                });

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));

                StringBuilder logs = new StringBuilder();
                logs.append("📋 Logs del Script (últimas 50 líneas)\n");
                logs.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

                String line;
                while ((line = reader.readLine()) != null) {
                    logs.append(line).append("\n");
                }

                if (logs.length() == 0) {
                    logs.append("⚠️ No hay logs disponibles\n\n");
                    logs.append("El script puede no estar corriendo o\n");
                    logs.append("no tiene permisos de lectura.");
                }

                runOnUiThread(() -> {
                    logTextView.setText(logs.toString());
                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    String error = "❌ Error al leer logs\n\n" + e.getMessage() +
                            "\n\nAsegúrate de tener permisos root.";
                    logTextView.setText(error);
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        isMonitoring = false;
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isMonitoring = true;
        updateLogs();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isMonitoring = false;
    }
}
