// FILE 1 — Expense.java
// Stores one expense — same as your lab!

public class Expense {
    public int id;
    public String desc;
    public double amount;
    public String cat;   // Food, Travel, Academic, Personal, Bills, Other
    public String date;

    public Expense(int id, String desc, double amount, String cat, String date) {
        this.id     = id;
        this.desc   = desc;
        this.amount = amount;
        this.cat    = cat;
        this.date   = date;
    }

    // Show expense as text
    public String toText() {
        return id + "," + desc + "," + amount + "," + cat + "," + date;
    }

    // Convert to JSON so frontend can read it
    public String toJson() {
        return "{\"id\":" + id +
               ",\"desc\":\"" + desc + "\"" +
               ",\"amount\":" + amount +
               ",\"cat\":\"" + cat + "\"" +
               ",\"date\":\"" + date + "\"}";
    }
}
