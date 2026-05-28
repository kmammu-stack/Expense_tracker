import java.util.*;

public class SortFilter {

    // ─────────────────────────────────────────────
    // SORT BY AMOUNT
    // ─────────────────────────────────────────────
    public static ArrayList<Expense> sortByAmount(ArrayList<Expense> expenses) {

        ArrayList<Expense> sorted = new ArrayList<>(expenses);

        sorted.sort(Comparator.comparingDouble(e -> e.amount));

        return sorted;
    }

    // ─────────────────────────────────────────────
    // SORT BY DATE
    // ─────────────────────────────────────────────
    public static ArrayList<Expense> sortByDate(ArrayList<Expense> expenses) {

        ArrayList<Expense> sorted = new ArrayList<>(expenses);

        sorted.sort(Comparator.comparing(e -> e.date));

        return sorted;
    }

    // ─────────────────────────────────────────────
    // FILTER BY CATEGORY
    // ─────────────────────────────────────────────
    public static ArrayList<Expense> filterByCategory(
            ArrayList<Expense> expenses,
            String category) {

        ArrayList<Expense> filtered = new ArrayList<>();

        for (Expense e : expenses) {

            if (e.cat.equalsIgnoreCase(category)) {
                filtered.add(e);
            }
        }

        return filtered;
    }

    // ─────────────────────────────────────────────
    // FILTER BY DATE RANGE
    // ─────────────────────────────────────────────
    public static ArrayList<Expense> filterByDateRange(
            ArrayList<Expense> expenses,
            String start,
            String end) {

        ArrayList<Expense> filtered = new ArrayList<>();

        for (Expense e : expenses) {

            if (e.date.compareTo(start) >= 0 &&
                e.date.compareTo(end) <= 0) {

                filtered.add(e);
            }
        }

        return filtered;
    }
}
