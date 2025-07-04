package com.example.genzeb;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {
    private RadioGroup rgTransactionType;
    private RadioButton rbIncome, rbExpense;
    private TextInputEditText etAmount, etCategory, etDescription, etDate;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private int currentUserId;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        dbHelper = new DatabaseHelper(this);
        currentUserId = getCurrentUserId();
        calendar = Calendar.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        rgTransactionType = findViewById(R.id.rgTransactionType);
        rbIncome = findViewById(R.id.rbIncome);
        rbExpense = findViewById(R.id.rbExpense);
        etAmount = findViewById(R.id.etAmount);
        etCategory = findViewById(R.id.etCategory);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        return dbHelper.getUserByEmail(email).getId();
    }

    private void setupListeners() {
        etDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateLabel();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateLabel() {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void saveTransaction() {
        String type = rbIncome.isChecked() ? "income" : "expense";
        String category = etCategory.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String date = etDate.getText().toString().trim();

        if (category.isEmpty()) {
            etCategory.setError("This field is required");
            return;
        }

        if (amountStr.isEmpty()) {
            etAmount.setError("Amount is required");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            return;
        }

        if (date.isEmpty()) {
            etDate.setError("Date is required");
            return;
        }

        Transaction transaction = new Transaction();
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setDescription(description);
        transaction.setUserId(currentUserId);

        long id = dbHelper.addTransaction(transaction);
        if (id > 0) {
            Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE);
        String lang = prefs.getString("language", "en");
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

}