package com.example.genzeb;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView tvTotalIncome, tvTotalExpense,tvBalance, tvBudgetLimit;
    private ProgressBar pbBudget;
    private RecyclerView rvRecentTransactions;
    private DatabaseHelper dbHelper;
    private int currentUserId;
    private double monthlyBudgetLimit = 0;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        currentUserId = getCurrentUserId();

        initializeViews();
        setupNavigation();
        loadBudgetData();
        loadRecentTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudgetData();
        loadRecentTransactions();
        checkBudgetNotifications();
    }

    private void initializeViews() {
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tvBudgetLimit = findViewById(R.id.tvBudgetLimit);
        pbBudget = findViewById(R.id.pbBudget);
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
    }

    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        return dbHelper.getUserByEmail(email).getId();
    }

    private void loadBudgetData() {
        double totalIncome = dbHelper.getTotalIncome(currentUserId);
        double totalExpense = dbHelper.getTotalExpense(currentUserId);
        double balance = totalIncome - totalExpense;

        if (balance < 0) {
            balance = 0.0;
        }

        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        monthlyBudgetLimit = prefs.getFloat("monthly_budget", 0);

        tvTotalIncome.setText(String.format(Locale.getDefault(), "%.2f", totalIncome));
        tvTotalExpense.setText(String.format(Locale.getDefault(), "%.2f", totalExpense));
        tvBalance.setText(String.format(Locale.getDefault(), "%.2f", balance));

        if (monthlyBudgetLimit > 0) {
            tvBudgetLimit.setText(String.format(Locale.getDefault(),
                    "Budget: %.2f / %.2f", totalExpense, monthlyBudgetLimit));

            int progress = (int) ((totalExpense / monthlyBudgetLimit) * 100);
            pbBudget.setProgress(progress);
        } else {
            tvBudgetLimit.setText("No budget set");
            pbBudget.setProgress(0);
        }
    }

    private void loadRecentTransactions() {
        List<Transaction> transactions = dbHelper.getRecentTransactions(currentUserId, 5);
        TransactionAdapter adapter = new TransactionAdapter(transactions);
        rvRecentTransactions.setAdapter(adapter);
    }

    private void checkBudgetNotifications() {
        if (monthlyBudgetLimit > 0) {
            double totalExpense = dbHelper.getTotalExpense(currentUserId);
            double percentage = (totalExpense / monthlyBudgetLimit) * 100;

            if (percentage >= 90 && percentage < 100) {
                showBudgetNotification("Budget Warning",
                        "You've used " + (int)percentage + "% of your monthly budget!");
            } else if (percentage >= 100) {
                showBudgetNotification("Budget Exceeded",
                        "You've exceeded your monthly budget by " +
                                String.format(Locale.getDefault(), "%.2f", (totalExpense - monthlyBudgetLimit)));
            }
        }
    }

    private void showBudgetNotification(String title, String message) {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications", true);
        if (!notificationsEnabled) {
            Log.d("MainActivity", "Notifications disabled, skipping notification");
            return;
        }

        String channelId = "budget_alerts";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Budget Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for budget-related alerts");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_POST_NOTIFICATIONS);
                return;
            }
        }

        NotificationManagerCompat.from(this).notify(generateUniqueNotificationId(), builder.build());
    }

    private int generateUniqueNotificationId() {
        return (int) System.currentTimeMillis();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showBudgetNotification("Budget Alert", "Notification permission granted!");
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_transactions) {
                startActivity(new Intent(this, TransactionActivity.class));
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(this, AddTransactionActivity.class));
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else {
                return false;
            }
            return true;
        });
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("app_settings", MODE_PRIVATE);
        String lang = prefs.getString("language", "en");
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang));
    }

}