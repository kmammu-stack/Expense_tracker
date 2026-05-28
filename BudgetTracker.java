import java.util.*;

public class BudgetTracker {

    // Stores budgets by category
    private Map<String, Double> budgets = new HashMap<>();

    // ─────────────────────────────────────────────
    // SET BUDGET
    // ─────────────────────────────────────────────
    public void setBudget(String category, double amount) {

        budgets.put(category, amount);
    }

    // ─────────────────────────────────────────────
    // GET BUDGET
    // ─────────────────────────────────────────────
    public double getBudget(String category) {

        return budgets.getOrDefault(category, 0.0);
    }

    // ─────────────────────────────────────────────
    // TOTAL SPENT FOR CATEGORY
    // ─────────────────────────────────────────────
    public double getSpentAmount(
            String category,
            ArrayList<Expense> expenses) {

        double total = 0;

        for (Expense e : expenses) {

            if (e.cat.equalsIgnoreCase(category)) {
                total += e.amount;
            }
        }

        return total;
    }

    // ─────────────────────────────────────────────
    // REMAINING BUDGET
    // ─────────────────────────────────────────────
    public double getRemainingBudget(
            String category,
            ArrayList<Expense> expenses) {

        double limit = getBudget(category);

        double spent = getSpentAmount(category, expenses);

        return limit - spent;
    }

    // ─────────────────────────────────────────────
    // CHECK IF BUDGET EXCEEDED
    // ─────────────────────────────────────────────
    public boolean isBudgetExceeded(
            String category,
            ArrayList<Expense> expenses) {

        return getRemainingBudget(category, expenses) < 0;
    }
}
