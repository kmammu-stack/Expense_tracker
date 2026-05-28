import java.util.*;

public class Statistics {

    private ArrayList<Expense> expenses;

    public Statistics(ArrayList<Expense> expenses) {
        this.expenses = expenses;
    }

    // TOTAL SPENT
    public double getTotalSpent() {

        double total = 0;

        for (Expense e : expenses) {
            total += e.amount;
        }

        return total;
    }

    // AVERAGE EXPENSE
    public double getAverageExpense() {

        if (expenses.isEmpty()) return 0;

        return getTotalSpent() / expenses.size();
    }

    // HIGHEST EXPENSE
    public Expense getHighestExpense() {

        if (expenses.isEmpty()) return null;

        Expense highest = expenses.get(0);

        for (Expense e : expenses) {
            if (e.amount > highest.amount) {
                highest = e;
            }
        }

        return highest;
    }

    // LOWEST EXPENSE
    public Expense getLowestExpense() {

        if (expenses.isEmpty()) return null;

        Expense lowest = expenses.get(0);

        for (Expense e : expenses) {
            if (e.amount < lowest.amount) {
                lowest = e;
            }
        }

        return lowest;
    }

    // CATEGORY TOTALS
    public Map<String, Double> getCategoryTotals() {

        Map<String, Double> totals = new HashMap<>();

        for (Expense e : expenses) {

            double current = totals.getOrDefault(e.cat, 0.0);

            totals.put(e.cat, current + e.amount);
        }

        return totals;
    }

    // TOP CATEGORY
    public String getTopCategory() {

        Map<String, Double> totals = getCategoryTotals();

        String topCategory = "";
        double max = 0;

        for (String cat : totals.keySet()) {

            double value = totals.get(cat);

            if (value > max) {
                max = value;
                topCategory = cat;
            }
        }

        return topCategory;
    }
}
