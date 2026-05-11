// FILE 2 — ExpenseManager.java
// Same as your lab ExpenseManager — add, delete, get, report
// But now also saves to a file so data doesn't disappear!

import java.util.*;
import java.io.*;

public class ExpenseManager {

    // Same ArrayList from your lab!
    private ArrayList<Expense> expenses = new ArrayList<>();
    private int nextId = 1;
    private static final String FILE = "expenses.txt";

    // Load saved expenses when server starts
    public ExpenseManager() {
        loadFromFile();
    }

    // ── ADD expense ───────────────────────────────────────
    public Expense add(String desc, double amount, String cat, String date) {
        Expense e = new Expense(nextId++, desc, amount, cat, date);
        expenses.add(e);
        saveToFile();
        return e;
    }

    // ── GET ALL expenses ──────────────────────────────────
    public ArrayList<Expense> getAll() {
        return expenses;
    }

    // ── DELETE expense by ID ──────────────────────────────
    public boolean delete(int id) {
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).id == id) {
                expenses.remove(i);
                saveToFile();
                return true;
            }
        }
        return false; // not found
    }

    // ── FILTER by category ────────────────────────────────
    public ArrayList<Expense> getByCategory(String cat) {
        ArrayList<Expense> result = new ArrayList<>();
        for (Expense e : expenses) {
            if (e.cat.equalsIgnoreCase(cat)) {
                result.add(e);
            }
        }
        return result;
    }

    // ── SUMMARY report ────────────────────────────────────
    // Same as your ReportGenerator from the lab!
    public String getSummary() {
        double total = 0, max = 0;
        for (Expense e : expenses) {
            total += e.amount;
            if (e.amount > max) max = e.amount;
        }
        double avg = expenses.isEmpty() ? 0 : total / expenses.size();

        // Category totals
        String[] cats = {"Food","Travel","Academic","Personal","Bills","Other"};
        StringBuilder catJson = new StringBuilder();
        for (int i = 0; i < cats.length; i++) {
            double catTotal = 0;
            for (Expense e : expenses) {
                if (e.cat.equalsIgnoreCase(cats[i])) catTotal += e.amount;
            }
            catJson.append("\"").append(cats[i]).append("\":").append(catTotal);
            if (i < cats.length - 1) catJson.append(",");
        }

        return "{" +
            "\"count\":"  + expenses.size() + "," +
            "\"total\":"  + total + "," +
            "\"avg\":"    + (int) avg + "," +
            "\"max\":"    + max + "," +
            "\"byCategory\":{" + catJson + "}" +
        "}";
    }

    // ── BUDGET check ──────────────────────────────────────
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

    // ── Convert list to JSON ──────────────────────────────
    public String listToJson(ArrayList<Expense> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toJson());
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // ── Save to file (so data survives restart) ───────────
    private void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println(nextId); // save next ID too
            for (Expense e : expenses) {
                pw.println(e.toText());
            }
        } catch (IOException ex) {
            System.out.println("Could not save: " + ex.getMessage());
        }
    }

    // ── Load from file on startup ─────────────────────────
    private void loadFromFile() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            nextId = Integer.parseInt(br.readLine().trim());
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", 5);
                if (p.length == 5) {
                    expenses.add(new Expense(
                        Integer.parseInt(p[0]),
                        p[1],
                        Double.parseDouble(p[2]),
                        p[3],
                        p[4]
                    ));
                }
            }
        } catch (Exception ex) {
            System.out.println("Could not load: " + ex.getMessage());
        }
    }
}
