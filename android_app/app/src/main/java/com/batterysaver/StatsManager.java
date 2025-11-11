package com.batterysaver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class StatsManager {

    private static final String STATS_FILE = "/data/adb/service.d/govbattery.stats";

    // Clase para representar un cambio de estado
    public static class StateChange {
        public long timestamp;      // Unix timestamp en segundos
        public String state;        // "LOCKED" o "UNLOCKED"
        public String governor;     // Nombre del governor

        public StateChange(long timestamp, String state, String governor) {
            this.timestamp = timestamp;
            this.state = state;
            this.governor = governor;
        }
    }

    // Clase para estadísticas calculadas
    public static class Stats {
        public long totalTimeLocked;        // milisegundos bloqueado
        public long totalTimeUnlocked;      // milisegundos desbloqueado
        public int lockedCount;             // número de veces bloqueado
        public int unlockedCount;           // número de veces desbloqueado
        public Map<String, Long> governorUsage;  // governor -> tiempo en ms
        public long firstEventTime;         // timestamp del primer evento
        public long lastEventTime;          // timestamp del último evento

        public Stats() {
            governorUsage = new LinkedHashMap<>();
            totalTimeLocked = 0;
            totalTimeUnlocked = 0;
            lockedCount = 0;
            unlockedCount = 0;
            firstEventTime = 0;
            lastEventTime = 0;
        }

        // Calcular porcentaje de tiempo bloqueado
        public double getLockedPercentage() {
            long total = totalTimeLocked + totalTimeUnlocked;
            return total > 0 ? (totalTimeLocked * 100.0) / total : 0;
        }

        // Calcular porcentaje de tiempo desbloqueado
        public double getUnlockedPercentage() {
            long total = totalTimeLocked + totalTimeUnlocked;
            return total > 0 ? (totalTimeUnlocked * 100.0) / total : 0;
        }

        // Obtener período total de datos
        public long getTotalPeriod() {
            return lastEventTime - firstEventTime;
        }
    }

    // Leer todos los eventos del archivo
    public static List<StateChange> loadStats() {
        List<StateChange> changes = new ArrayList<>();

        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "su", "-c", "cat " + STATS_FILE
            });
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    try {
                        long timestamp = Long.parseLong(parts[0]);
                        String state = parts[1];
                        String governor = parts[2];
                        changes.add(new StateChange(timestamp, state, governor));
                    } catch (NumberFormatException e) {
                        // Ignorar líneas con formato incorrecto
                    }
                }
            }
            reader.close();
            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return changes;
    }

    // Calcular estadísticas desde una lista de cambios
    public static Stats calculateStats(List<StateChange> changes) {
        Stats stats = new Stats();

        if (changes.isEmpty()) {
            return stats;
        }

        // Ordenar por timestamp (por si acaso no están ordenados)
        Collections.sort(changes, (a, b) -> Long.compare(a.timestamp, b.timestamp));

        stats.firstEventTime = changes.get(0).timestamp;
        stats.lastEventTime = changes.get(changes.size() - 1).timestamp;

        // Calcular duraciones entre eventos
        for (int i = 0; i < changes.size() - 1; i++) {
            StateChange current = changes.get(i);
            StateChange next = changes.get(i + 1);

            long durationSeconds = next.timestamp - current.timestamp;
            long durationMs = durationSeconds * 1000;

            // Acumular tiempo por estado
            if ("LOCKED".equals(current.state)) {
                stats.totalTimeLocked += durationMs;
                stats.lockedCount++;
            } else if ("UNLOCKED".equals(current.state)) {
                stats.totalTimeUnlocked += durationMs;
                stats.unlockedCount++;
            }

            // Acumular tiempo por governor
            String gov = current.governor;
            stats.governorUsage.put(gov,
                    stats.governorUsage.getOrDefault(gov, 0L) + durationMs);
        }

        return stats;
    }

    // Obtener estadísticas de las últimas N horas
    public static Stats getStatsLastHours(int hours) {
        List<StateChange> allChanges = loadStats();
        long cutoffTime = (System.currentTimeMillis() / 1000) - (hours * 3600);

        List<StateChange> recentChanges = new ArrayList<>();
        for (StateChange change : allChanges) {
            if (change.timestamp >= cutoffTime) {
                recentChanges.add(change);
            }
        }

        return calculateStats(recentChanges);
    }

    // Obtener estadísticas desde una fecha específica
    public static Stats getStatsSince(long timestampSeconds) {
        List<StateChange> allChanges = loadStats();

        List<StateChange> recentChanges = new ArrayList<>();
        for (StateChange change : allChanges) {
            if (change.timestamp >= timestampSeconds) {
                recentChanges.add(change);
            }
        }

        return calculateStats(recentChanges);
    }

    // Limpiar estadísticas antiguas
    public static void clearStats() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "su", "-c", "rm -f " + STATS_FILE
            });
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Verificar si hay datos disponibles
    public static boolean hasStats() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "su", "-c", "[ -f " + STATS_FILE + " ] && echo 'exists'"
            });
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            reader.close();
            process.waitFor();

            return "exists".equals(result);
        } catch (Exception e) {
            return false;
        }
    }

    // Obtener número de eventos registrados
    public static int getEventCount() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "su", "-c", "wc -l < " + STATS_FILE
            });
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            reader.close();
            process.waitFor();

            return result != null ? Integer.parseInt(result.trim()) : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
