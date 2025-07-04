package com.example.genzeb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchDarkMode;
    private Spinner spinnerLanguage;
    private EditText etBudgetLimit;
    private Button btnSaveSettings;
    private Button btnLogout;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedLanguage(); // Apply saved language before loading UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);

        initializeViews();
        loadCurrentSettings();
        setupListeners();
    }

    private void initializeViews() {
        switchDarkMode = findViewById(R.id.switchDarkMode);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        etBudgetLimit = findViewById(R.id.etBudgetLimit);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadCurrentSettings() {
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);

        String language = sharedPreferences.getString("language", "en");
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);
        spinnerLanguage.setSelection(language.equals("am") ? 1 : 0);

        float budgetLimit = sharedPreferences.getFloat("monthly_budget", 0);
        if (budgetLimit > 0) {
            etBudgetLimit.setText(String.valueOf(budgetLimit));
        }
    }

    private void setupListeners() {
        btnSaveSettings.setOnClickListener(v -> saveSettings());

        btnLogout.setOnClickListener(v -> {
            // Clear login session (but NOT email/password if remembered)
            SharedPreferences.Editor editor = getSharedPreferences("login_prefs", MODE_PRIVATE).edit();
            editor.remove("is_logged_in");
            editor.remove("user_email");
            editor.apply();

            // Redirect to login activity and clear back stack
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        boolean darkMode = switchDarkMode.isChecked();
        editor.putBoolean("dark_mode", darkMode);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        String language = spinnerLanguage.getSelectedItemPosition() == 0 ? "en" : "am";
        editor.putString("language", language);

        try {
            float budgetLimit = Float.parseFloat(etBudgetLimit.getText().toString());
            editor.putFloat("monthly_budget", budgetLimit);
        } catch (NumberFormatException e) {
            editor.putFloat("monthly_budget", 0);
        }

        editor.apply();

        Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void applySavedLanguage() {
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        String lang = prefs.getString("language", "en");

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE);
        String lang = prefs.getString("language", "en");
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }
}
