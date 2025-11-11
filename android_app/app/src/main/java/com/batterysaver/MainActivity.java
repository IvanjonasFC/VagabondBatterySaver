package com.batterysaver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private TextView statusText;
    private TextView statusTextView;
    private TextView descriptionText;
    private Handler handler = new Handler();
    private boolean isMonitoring = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        statusTextView = findViewById(R.id.statusTextView);
        descriptionText = findViewById(R.id.descriptionText);
        Button viewLogsButton = findViewById(R.id.viewLogsButton);
        Button settingsButton = findViewById(R.id.settingsButton);
        Button statsButton = findViewById(R.id.statsButton);
        viewLogsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LogViewerActivity.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });


        statsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatsActivity.class);
            startActivity(intent);
        });
        startMonitoring();
    }

    private void startMonitoring() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isMonitoring) {
                    updateStatus();
                    handler.postDelayed(this, 3000);
                }
            }
        }, 0);
    }

    private void updateStatus() {
        new Thread(() -> {
            try {
                String pid = null;
                boolean scriptRunning = false;
                String governor = null;
                String batterySaver = null;
                boolean isLocked = false;
                String savedState = "UNKNOWN";
                String uptime = "N/A";

                try {
                    Process pidProcess = Runtime.getRuntime().exec(new String[]{
                            "su", "-c", "cat /data/adb/service.d/govbattery.pid"
                    });
                    BufferedReader pidReader = new BufferedReader(
                            new InputStreamReader(pidProcess.getInputStream()));
                    pid = pidReader.readLine();
                    pidReader.close();

                    if (pid != null && !pid.isEmpty()) {
                        Process checkProcess = Runtime.getRuntime().exec(new String[]{
                                "su", "-c", "kill -0 " + pid + " 2>/dev/null && echo 'running' || echo 'stopped'"
                        });
                        BufferedReader checkReader = new BufferedReader(
                                new InputStreamReader(checkProcess.getInputStream()));
                        String status = checkReader.readLine();
                        checkReader.close();
                        scriptRunning = "running".equals(status);
                    }
                } catch (Exception e) {
                    try {
                        Process psProcess = Runtime.getRuntime().exec(new String[]{
                                "su", "-c", "ps -A | grep govbattery"
                        });
                        BufferedReader psReader = new BufferedReader(
                                new InputStreamReader(psProcess.getInputStream()));
                        scriptRunning = psReader.readLine() != null;
                        psReader.close();
                    } catch (Exception ex) {
                        // Mantener scriptRunning = false
                    }
                }

                try {
                    Process govProcess = Runtime.getRuntime().exec(new String[]{
                            "su", "-c", "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor"
                    });
                    BufferedReader govReader = new BufferedReader(
                            new InputStreamReader(govProcess.getInputStream()));
                    governor = govReader.readLine();
                    govReader.close();
                } catch (Exception e) {
                    // Mantener governor = null
                }

                try {
                    Process bsProcess = Runtime.getRuntime().exec(new String[]{
                            "su", "-c", "settings get global low_power"
                    });
                    BufferedReader bsReader = new BufferedReader(
                            new InputStreamReader(bsProcess.getInputStream()));
                    batterySaver = bsReader.readLine();
                    bsReader.close();
                } catch (Exception e) {
                    // Mantener batterySaver = null
                }

                try {
                    Process lockProcess = Runtime.getRuntime().exec(new String[]{
                            "su", "-c", "dumpsys window | grep mDreamingLockscreen"
                    });
                    BufferedReader lockReader = new BufferedReader(
                            new InputStreamReader(lockProcess.getInputStream()));
                    String lockStatus = lockReader.readLine();
                    lockReader.close();
                    isLocked = lockStatus != null && lockStatus.contains("true");
                } catch (Exception e) {
                    // Mantener isLocked = false
                }

                try {
                    Process stateProcess = Runtime.getRuntime().exec(new String[]{
                            "su", "-c", "cat /data/adb/service.d/govbattery.state"
                    });
                    BufferedReader stateReader = new BufferedReader(
                            new InputStreamReader(stateProcess.getInputStream()));
                    String tempState = stateReader.readLine();
                    if (tempState != null && !tempState.isEmpty()) {
                        savedState = tempState;
                    }
                    stateReader.close();
                } catch (Exception e) {
                    // Mantener savedState = "UNKNOWN"
                }

                try {
                    Process uptimeProcess = Runtime.getRuntime().exec(new String[]{
                            "su", "-c", "head -n 1 /data/adb/service.d/govbattery.log | cut -d']' -f1 | cut -d'[' -f2"
                    });
                    BufferedReader uptimeReader = new BufferedReader(
                            new InputStreamReader(uptimeProcess.getInputStream()));
                    String tempUptime = uptimeReader.readLine();
                    if (tempUptime != null && !tempUptime.isEmpty()) {
                        uptime = tempUptime;
                    }
                    uptimeReader.close();
                } catch (Exception e) {
                    // Mantener uptime = "N/A"
                }

                final String finalPid = pid;
                final boolean finalScriptRunning = scriptRunning;
                final String finalGovernor = governor;
                final String finalBatterySaver = batterySaver;
                final boolean finalIsLocked = isLocked;
                final String finalSavedState = savedState;
                final String finalUptime = uptime;

                runOnUiThread(() -> updateUI(finalScriptRunning, finalGovernor, finalBatterySaver,
                        finalIsLocked, finalSavedState, finalPid, finalUptime));

            } catch (Exception e) {
                runOnUiThread(() -> showError(e != null ? e.getMessage() : "Error desconocido"));
            }
        }).start();
    }

    private void updateUI(boolean scriptRunning, String governor, String batterySaver,
                          boolean isLocked, String savedState, String pid, String uptime) {
        if (scriptRunning) {
            statusText.setText("🟢 ACTIVO");
            statusText.setTextColor(0xFF4CAF50);
            descriptionText.setText("✅ El sistema está monitoreando el bloqueo de pantalla");
            descriptionText.setBackgroundColor(0xFFE8F5E9);
        } else {
            statusText.setText("🔴 INACTIVO");
            statusText.setTextColor(0xFFF44336);
            descriptionText.setText("⚠️ El script no está corriendo. Reinicia el dispositivo.");
            descriptionText.setBackgroundColor(0xFFFFEBEE);
        }

        StringBuilder info = new StringBuilder();

        if (scriptRunning && pid != null && !pid.isEmpty()) {
            info.append("🆔 PID: ").append(pid).append("\n\n");
        }

        if (savedState != null && !"UNKNOWN".equals(savedState)) {
            info.append("📌 Estado del Script: ");
            info.append("LOCKED".equals(savedState) ? "🔒 Bloqueado" : "🔓 Desbloqueado");
            info.append("\n\n");
        }

        info.append("📱 Dispositivo: ");
        info.append(isLocked ? "🔒 Bloqueado" : "🔓 Desbloqueado");
        info.append("\n\n");

        info.append("⚙️  Governor CPU: ");
        info.append(governor != null ? governor.toUpperCase() : "Desconocido");
        info.append("\n\n");

        info.append("🔋 Battery Saver: ");
        if ("1".equals(batterySaver)) {
            info.append("✅ ACTIVADO");
        } else {
            info.append("❌ DESACTIVADO");
        }
        info.append("\n\n");

        info.append("🔄 Script: ");
        info.append(scriptRunning ? "✅ En ejecución" : "❌ Detenido");

        if (scriptRunning && uptime != null && !"N/A".equals(uptime)) {
            info.append("\n\n⏱️  Iniciado: ").append(uptime);
        }

        statusTextView.setText(info.toString());
    }

    private void showError(String errorMessage) {
        statusText.setText("🔴 ERROR");
        statusText.setTextColor(0xFFF44336);
        statusTextView.setText("Error al obtener estado del sistema:\n" + errorMessage);
        descriptionText.setText("⚠️ No se puede conectar con el script");
        descriptionText.setBackgroundColor(0xFFFFEBEE);
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
        updateStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isMonitoring = false;
    }
}
