// ExpenseManager.java — Updated to match ALL teammates' method calls
//
// Frontend team uses:
//   manager.addExpense(amount, category, date, description)
//   manager.getAllExpenses()
//   manager.deleteExpense(id)
//   manager.updateExpense(id, amount, category, date, desc)
//   manager.findById(id)
//   manager.search(keyword)
//
// Features team uses:
//   e.cat, e.amount, e.date, e.desc (direct field access)

import java.util.*;
import java.io.*;

public class ExpenseManager {

    private ArrayList<Expense> expenses = new ArrayList<>();
    private int nextId = 1;
    private static final String FILE = "expenses.csv";

    public ExpenseManager() {
        loadFromFile();
    }

    // ── ADD — matches frontend: manager.addExpense(amount, category, date, desc)
    public Expense addExpense(double amount, String category, String date, String desc) {
        Expense e = new Expense(nextId++, amount, category, date, desc);
        expenses.add(e);
        saveToFile();
        return e;
    }

    // ── GET ALL — matches frontend: manager.getAllExpenses()
    public ArrayList<Expense> getAllExpenses() {
        return expenses;
    }

    // ── DELETE — matches frontend: manager.deleteExpense(id)
    public boolean deleteExpense(int id) {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).id == id) {
                expenses.remove(i);
                saveToFile();
                return true;
            }
        }
        return false;
    }

    // ── FIND BY ID — matches frontend: manager.findById(id)
    public Expense findById(int id) {
        for (Expense e : expenses) {
            if (e.id == id) return e;
        }
        return null;
    }

    // ── UPDATE — matches frontend: manager.updateExpense(id, amount, cat, date, desc)
    // Pass -1 for amount or null for others to keep existing value
    public boolean updateExpense(int id, double amount, String category, String date, String desc) {
        Expense e = findById(id);
        if (e == null) return false;
        if (amount > 0)       e.amount = amount;
        if (category != null) e.cat    = category;
        if (date != null)     e.date   = date;
        if (desc != null)     e.desc   = desc;
        saveToFile();
        return true;
    }

    // ── SEARCH by keyword in desc or category — matches frontend: manager.search(keyword)
    public ArrayList<Expense> search(String keyword) {
        ArrayList<Expense> result = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.desc.toLowerCase().contains(keyword.toLowerCase()) ||
                e.cat.toLowerCase().contains(keyword.toLowerCase())) {
                result.add(e);
            }
        }
        return result;
    }

    // ── FILTER BY CATEGORY — used by Server.java
    public ArrayList<Expense> getByCategory(String cat) {
        ArrayList<Expense> result = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.cat.equalsIgnoreCase(cat)) result.add(e);
        }
        return result;
    }

    // ── SUMMARY — used by Server.java to send to React frontend
    public String getSummary() {
        double total = 0, max = 0;
        for (Expense e : expenses) {
            total += e.amount;
            if (e.amount > max) max = e.amount;
        }
        double avg = expenses.isEmpty() ? 0 : total / expenses.size();

        // Use Statistics class from features team
        Statistics stats = new Statistics(expenses);
        Map<String, Double> catTotals = stats.getCategoryTotals();

        String[] cats = {"Food","Travel","Academic","Personal","Bills","Other"};
        StringBuilder catJson = new StringBuilder();
        for (int i = 0; i < cats.length; i++) {
            double catTotal = catTotals.getOrDefault(cats[i], 0.0);
            catJson.append("\"").append(cats[i]).append("\":").append(catTotal);
            if (i < cats.length - 1) catJson.append(",");
        }

        return "{" +
            "\"count\":"  + expenses.size() + "," +
            "\"total\":"  + total + "," +
            "\"avg\":"    + (int) avg + "," +
            "\"max\":"    + max + "," +
            "\"topCategory\":\"" + stats.getTopCategory() + "\"," +
            "\"byCategory\":{" + catJson + "}" +
        "}";
    }

    // ── BUDGET CHECK — used by Server.java
    public String checkBudget(double budget) {
        double total = 0;
        for (Expense e : expenses) total += e.amount;
        double remaining = budget - total;
        boolean isOver   = total > budget;
        double  pct      = budget > 0 ? (total / budget) * 100 : 0;

        return "{" +
            "\"budget\":"     + budget + "," +
            "\"totalSpent\":" + total + "," +
            "\"remaining\":"  + remaining + "," +
            "\"isOver\":"     + isOver + "," +
            "\"percentage\":" + (int) pct +
        "}";
    }

    // ── LIST TO JSON — used by Server.java
    public String listToJson(ArrayList<Expense> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toJson());
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // ── SAVE TO FILE — same format as FileHandler from frontend team
    // Format: id,amount,category,date,description
    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Expense e : expenses) {
                pw.println(e.toText());
            }
        } catch (IOException ex) {
            System.out.println("Could not save: " + ex.getMessage());
        }
    }

    // ── LOAD FROM FILE — same format as FileHandler from frontend team
    private void loadFromFile() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", 5);
                if (p.length < 5) continue;
                int    id     = Integer.parseInt(p[0].trim());
                double amount = Double.parseDouble(p[1].trim());
                String cat    = p[2].trim();
                String date   = p[3].trim();
                String desc   = p[4].trim();
                expenses.add(new Expense(id, amount, cat, date, desc));
                if (id >= nextId) nextId = id + 1;
            }
        } catch (Exception ex) {
            System.out.println("Could not load: " + ex.getMessage());
        }
    }
}
