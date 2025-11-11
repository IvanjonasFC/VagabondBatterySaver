package com.batterysaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class StatsActivity extends Activity {

    private TextView textStats;
    private ProgressBar progressBar;
    private Button btnRefresh;
    private Button btnClear;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        textStats = findViewById(R.id.textStats);
        progressBar = findViewById(R.id.progressBar);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnClear = findViewById(R.id.btnClear);
        btnBack = findViewById(R.id.btnBack);

        btnRefresh.setOnClickListener(v -> loadStats());
        btnClear.setOnClickListener(v -> confirmClearStats());
        btnBack.setOnClickListener(v -> finish());

        loadStats();
    }

    private void loadStats() {
        // Mostrar ProgressBar, ocultar contenido
        progressBar.setVisibility(View.VISIBLE);
        textStats.setVisibility(View.GONE);
        btnRefresh.setEnabled(false);
        btnClear.setEnabled(false);

        new Thread(() -> {
            // Verificar si hay datos
            if (!StatsManager.hasStats()) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    textStats.setVisibility(View.VISIBLE);
                    btnRefresh.setEnabled(true);
                    btnClear.setEnabled(true);

                    textStats.setText("📊 SIN DATOS DISPONIBLES\n\n" +
                            "El sistema de estadísticas aún no ha registrado eventos.\n\n" +
                            "Las estadísticas aparecerán después de que el dispositivo " +
                            "cambie entre estados bloqueado/desbloqueado.\n\n" +
                            "Intenta:\n" +
                            "1. Bloquea el dispositivo\n" +
                            "2. Espera 5 segundos\n" +
                            "3. Desbloquea el dispositivo\n" +
                            "4. Vuelve a esta pantalla");
                });
                return;
            }

            // Cargar estadísticas de diferentes períodos
            StatsManager.Stats stats24h = StatsManager.getStatsLastHours(24);
            StatsManager.Stats stats7d = StatsManager.getStatsLastHours(24 * 7);
            int eventCount = StatsManager.getEventCount();

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                textStats.setVisibility(View.VISIBLE);
                btnRefresh.setEnabled(true);
                btnClear.setEnabled(true);
                displayStats(stats24h, stats7d, eventCount);
            });
        }).start();
    }

    private void displayStats(StatsManager.Stats stats24h, StatsManager.Stats stats7d, int totalEvents) {
        StringBuilder sb = new StringBuilder();

        sb.append("📊 ESTADÍSTICAS DE USO\n");
        sb.append("═══════════════════════════════════════\n\n");
        sb.append("📈 Total de eventos registrados: ").append(totalEvents).append("\n\n");

        // Últimas 24 horas
        sb.append("⏰ ÚLTIMAS 24 HORAS\n");
        sb.append("───────────────────────────────────────\n\n");

        if (stats24h.lockedCount == 0 && stats24h.unlockedCount == 0) {
            sb.append("Sin datos en este período.\n\n");
        } else {
            long totalTime24h = stats24h.totalTimeLocked + stats24h.totalTimeUnlocked;

            if (totalTime24h > 0) {
                sb.append("🔒 Tiempo bloqueado:\n");
                sb.append("   ").append(formatDuration(stats24h.totalTimeLocked));
                sb.append(String.format(Locale.US, " (%.1f%%)\n\n", stats24h.getLockedPercentage()));

                sb.append("🔓 Tiempo desbloqueado:\n");
                sb.append("   ").append(formatDuration(stats24h.totalTimeUnlocked));
                sb.append(String.format(Locale.US, " (%.1f%%)\n\n", stats24h.getUnlockedPercentage()));
            }

            int totalChanges24h = stats24h.lockedCount + stats24h.unlockedCount;
            sb.append("🔄 Cambios de estado: ").append(totalChanges24h).append("\n");
            sb.append("   • Bloqueos: ").append(stats24h.lockedCount).append("\n");
            sb.append("   • Desbloqueos: ").append(stats24h.unlockedCount).append("\n\n");

            if (stats24h.unlockedCount > 0) {
                long avgUnlocked = stats24h.totalTimeUnlocked / stats24h.unlockedCount;
                sb.append("📱 Promedio sesión activa: ");
                sb.append(formatDuration(avgUnlocked)).append("\n\n");
            }

            if (!stats24h.governorUsage.isEmpty()) {
                sb.append("⚙️  USO DE GOVERNORS:\n");
                for (Map.Entry<String, Long> entry : stats24h.governorUsage.entrySet()) {
                    double percent = (entry.getValue() * 100.0) / totalTime24h;
                    sb.append("   • ").append(entry.getKey()).append(":\n");
                    sb.append("     ").append(formatDuration(entry.getValue()));
                    sb.append(String.format(Locale.US, " (%.1f%%)\n", percent));
                }
                sb.append("\n");
            }
        }

        // Últimos 7 días
        sb.append("📅 ÚLTIMOS 7 DÍAS\n");
        sb.append("───────────────────────────────────────\n\n");

        if (stats7d.lockedCount == 0 && stats7d.unlockedCount == 0) {
            sb.append("Sin datos en este período.\n\n");
        } else {
            long totalTime7d = stats7d.totalTimeLocked + stats7d.totalTimeUnlocked;

            if (totalTime7d > 0) {
                sb.append("🔒 Tiempo bloqueado:\n");
                sb.append("   ").append(formatDuration(stats7d.totalTimeLocked));
                sb.append(String.format(Locale.US, " (%.1f%%)\n\n", stats7d.getLockedPercentage()));

                sb.append("🔓 Tiempo desbloqueado:\n");
                sb.append("   ").append(formatDuration(stats7d.totalTimeUnlocked));
                sb.append(String.format(Locale.US, " (%.1f%%)\n\n", stats7d.getUnlockedPercentage()));
            }

            int totalChanges7d = stats7d.lockedCount + stats7d.unlockedCount;
            sb.append("🔄 Total cambios: ").append(totalChanges7d).append("\n\n");

            long daysWithData = stats7d.getTotalPeriod() / (24 * 3600);
            if (daysWithData > 0) {
                double avgChangesPerDay = totalChanges7d / (double) daysWithData;
                sb.append(String.format(Locale.US, "📊 Promedio: %.1f cambios/día\n\n", avgChangesPerDay));
            }
        }

        // Período de datos
        if (stats7d.firstEventTime > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            sb.append("📅 PERÍODO DE DATOS\n");
            sb.append("───────────────────────────────────────\n");
            sb.append("Desde: ").append(sdf.format(new Date(stats7d.firstEventTime * 1000))).append("\n");
            sb.append("Hasta: ").append(sdf.format(new Date(stats7d.lastEventTime * 1000))).append("\n");
        }

        textStats.setText(sb.toString());
    }

    private String formatDuration(long millis) {
        if (millis < 0) millis = 0;

        long days = millis / (1000 * 60 * 60 * 24);
        long hours = (millis / (1000 * 60 * 60)) % 24;
        long minutes = (millis / (1000 * 60)) % 60;

        if (days > 0) {
            return String.format(Locale.US, "%dd %02dh %02dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format(Locale.US, "%dh %02dm", hours, minutes);
        } else {
            return String.format(Locale.US, "%dm", minutes);
        }
    }

    private void confirmClearStats() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Borrar Estadísticas")
                .setMessage("¿Estás seguro de que quieres eliminar todas las estadísticas?\n\n" +
                        "Esta acción no se puede deshacer.")
                .setPositiveButton("Borrar", (dialog, which) -> clearStats())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void clearStats() {
        progressBar.setVisibility(View.VISIBLE);
        textStats.setVisibility(View.GONE);
        btnRefresh.setEnabled(false);
        btnClear.setEnabled(false);

        new Thread(() -> {
            StatsManager.clearStats();

            runOnUiThread(() -> {
                Toast.makeText(this, "✅ Estadísticas eliminadas", Toast.LENGTH_SHORT).show();
                loadStats();
            });
        }).start();
    }
}
