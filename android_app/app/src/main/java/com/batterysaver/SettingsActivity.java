package com.batterysaver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private Spinner spinnerGovLocked;
    private Spinner spinnerGovUnlocked;
    private SeekBar seekBarInterval;
    private TextView textIntervalValue;
    private Switch switchBatterySaver;
    private EditText editTextCpuList;
    private Button btnSave;
    private Button btnCancel;

    private ConfigManager configManager;
    private String[] availableGovernors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        configManager = new ConfigManager(this);

        spinnerGovLocked = findViewById(R.id.spinnerGovLocked);
        spinnerGovUnlocked = findViewById(R.id.spinnerGovUnlocked);
        seekBarInterval = findViewById(R.id.seekBarInterval);
        textIntervalValue = findViewById(R.id.textIntervalValue);
        switchBatterySaver = findViewById(R.id.switchBatterySaver);
        editTextCpuList = findViewById(R.id.editTextCpuList);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        loadGovernors();
        loadCurrentConfig();
        setupSeekBar();

        btnSave.setOnClickListener(v -> saveConfiguration());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadGovernors() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando governors disponibles...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            availableGovernors = configManager.getAvailableGovernors();

            runOnUiThread(() -> {
                progressDialog.dismiss();

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        SettingsActivity.this,
                        android.R.layout.simple_spinner_item,
                        availableGovernors
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerGovLocked.setAdapter(adapter);
                spinnerGovUnlocked.setAdapter(adapter);

                setSpinnerSelection(spinnerGovLocked, configManager.getGovLocked());
                setSpinnerSelection(spinnerGovUnlocked, configManager.getGovUnlocked());
            });
        }).start();
    }

    private void loadCurrentConfig() {
        int interval = configManager.getCheckInterval();
        seekBarInterval.setProgress(interval - 1);
        updateIntervalText(interval);

        switchBatterySaver.setChecked(configManager.isBatterySaverEnabled());
        editTextCpuList.setText(configManager.getCpuList());
    }

    private void setupSeekBar() {
        seekBarInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int seconds = progress + 1;
                updateIntervalText(seconds);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateIntervalText(int seconds) {
        String text;
        if (seconds == 1) {
            text = "1 segundo (~2s de respuesta real)\n⚡ Ultra-responsivo | Consumo: ~0.05%/hora";
        } else if (seconds <= 3) {
            text = seconds + " segundos (~" + (seconds + 1) + "s de respuesta real)\n⚡ Rápido | Consumo: ~0.03%/hora";
        } else if (seconds <= 5) {
            text = seconds + " segundos (~" + (seconds + 1) + "s de respuesta real)\n⚖️ Equilibrado | Consumo: ~0.02%/hora";
        } else {
            text = seconds + " segundos (~" + (seconds + 1) + "s de respuesta real)\n🔋 Ahorro | Consumo: ~0.01%/hora";
        }
        textIntervalValue.setText(text);
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        if (adapter != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private void saveConfiguration() {
        String cpuList = editTextCpuList.getText().toString().trim();
        if (cpuList.isEmpty()) {
            Toast.makeText(this, "⚠️ La lista de CPUs no puede estar vacía", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cpuList.matches("^[0-9\\s]+$")) {
            Toast.makeText(this, "⚠️ Lista de CPUs inválida. Usa números separados por espacios", Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Aplicando configuración en tiempo real...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        configManager.setGovLocked(spinnerGovLocked.getSelectedItem().toString());
        configManager.setGovUnlocked(spinnerGovUnlocked.getSelectedItem().toString());
        configManager.setCheckInterval(seekBarInterval.getProgress() + 1);
        configManager.setBatterySaverEnabled(switchBatterySaver.isChecked());
        configManager.setCpuList(cpuList);

        configManager.saveToFile(
                () -> {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this,
                                "✅ Configuración aplicada instantáneamente",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
                },
                () -> {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this,
                                "❌ Error al guardar configuración",
                                Toast.LENGTH_SHORT).show();
                    });
                }
        );
    }
}
