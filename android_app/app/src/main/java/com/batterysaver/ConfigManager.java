package com.batterysaver;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConfigManager {

    private static final String PREFS_NAME = "BatterySaverConfig";
    private static final String CONFIG_FILE = "/data/adb/service.d/govbattery.conf";
    private static final String RELOAD_SIGNAL = "/data/adb/service.d/govbattery.reload";

    private SharedPreferences prefs;
    private Context context;

    // Claves de configuración
    public static final String KEY_GOV_LOCKED = "gov_locked";
    public static final String KEY_GOV_UNLOCKED = "gov_unlocked";
    public static final String KEY_CHECK_INTERVAL = "check_interval";
    public static final String KEY_BATTERY_SAVER_ENABLED = "battery_saver_enabled";
    public static final String KEY_CPU_LIST = "cpu_list";

    // Valores por defecto
    public static final String DEFAULT_GOV_LOCKED = "powersave";
    public static final String DEFAULT_GOV_UNLOCKED = "sched_pixel";
    public static final int DEFAULT_CHECK_INTERVAL = 1;
    public static final boolean DEFAULT_BATTERY_SAVER = true;
    public static final String DEFAULT_CPU_LIST = "0 1 2 3 4 5 6 7 8";

    public ConfigManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Getters
    public String getGovLocked() {
        return prefs.getString(KEY_GOV_LOCKED, DEFAULT_GOV_LOCKED);
    }

    public String getGovUnlocked() {
        return prefs.getString(KEY_GOV_UNLOCKED, DEFAULT_GOV_UNLOCKED);
    }

    public int getCheckInterval() {
        return prefs.getInt(KEY_CHECK_INTERVAL, DEFAULT_CHECK_INTERVAL);
    }

    public boolean isBatterySaverEnabled() {
        return prefs.getBoolean(KEY_BATTERY_SAVER_ENABLED, DEFAULT_BATTERY_SAVER);
    }

    public String getCpuList() {
        return prefs.getString(KEY_CPU_LIST, DEFAULT_CPU_LIST);
    }

    // Setters
    public void setGovLocked(String governor) {
        prefs.edit().putString(KEY_GOV_LOCKED, governor).apply();
    }

    public void setGovUnlocked(String governor) {
        prefs.edit().putString(KEY_GOV_UNLOCKED, governor).apply();
    }

    public void setCheckInterval(int seconds) {
        prefs.edit().putInt(KEY_CHECK_INTERVAL, seconds).apply();
    }

    public void setBatterySaverEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BATTERY_SAVER_ENABLED, enabled).apply();
    }

    public void setCpuList(String cpuList) {
        prefs.edit().putString(KEY_CPU_LIST, cpuList).apply();
    }

    // Escribir configuración al archivo y enviar señal de recarga
    public void saveToFile(Runnable onSuccess, Runnable onError) {
        new Thread(() -> {
            try {
                StringBuilder config = new StringBuilder();
                config.append("# Lock Screen Battery Saver - Configuración\n");
                config.append("# Generado por la app\n\n");
                config.append("GOV_LOCKED=\"").append(getGovLocked()).append("\"\n");
                config.append("GOV_UNLOCKED=\"").append(getGovUnlocked()).append("\"\n");
                config.append("CHECK_INTERVAL=").append(getCheckInterval()).append("\n");
                config.append("BATTERY_SAVER_ENABLED=").append(isBatterySaverEnabled() ? "1" : "0").append("\n");
                config.append("CPU_LIST=\"").append(getCpuList()).append("\"\n");

                // Escribir archivo con root
                String escapedConfig = config.toString().replace("'", "'\\''");
                String cmd = "echo '" + escapedConfig + "' > " + CONFIG_FILE;
                Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                process.waitFor();

                // Dar permisos de lectura
                Runtime.getRuntime().exec(new String[]{"su", "-c", "chmod 644 " + CONFIG_FILE}).waitFor();

                // NUEVO: Crear archivo de señal para recarga en tiempo real
                Runtime.getRuntime().exec(new String[]{"su", "-c", "touch " + RELOAD_SIGNAL}).waitFor();
                Runtime.getRuntime().exec(new String[]{"su", "-c", "chmod 644 " + RELOAD_SIGNAL}).waitFor();

                if (onSuccess != null) {
                    onSuccess.run();
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (onError != null) {
                    onError.run();
                }
            }
        }).start();
    }

    // Obtener governors disponibles en el sistema
    public String[] getAvailableGovernors() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{
                    "su", "-c", "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors"
            });
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            reader.close();

            if (line != null) {
                return line.trim().split("\\s+");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Valores por defecto si falla
        return new String[]{"powersave", "schedutil", "sched_pixel", "performance", "conservative", "ondemand"};
    }
}
