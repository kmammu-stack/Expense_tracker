public class Statistics {
    private ArrayList<Expense> expenses;

    public Statistics(ArrayList<Expense> expenses) {
        this.expenses = expenses;
    }

    public double getTotalSpent() { }

    public double getAverageExpense() { }

    public Expense getHighestExpense() { }

    public Expense getLowestExpense() { }

    public Map<String, Double> getCategoryTotals() { }

    public int getTotalTransactions() { }
}
