package com.example.genzeb;
public class Transaction {
    private int id;
    private String type;
    private String category;
    private double amount;
    private String date;
    private String description;
    private int userId;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}