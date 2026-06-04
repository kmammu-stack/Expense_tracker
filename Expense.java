// Expense.java — Updated to match your teammates' code
// They use: getId(), getAmount(), getCategory(), getDate(), getDescription()
// So we add those getters to match!

public class Expense {

    public int    id;
    public double amount;
    public String cat;         // used by features team (e.cat)
    public String date;
    public String desc;        // used by features team (e.desc)

    public Expense(int id, double amount, String cat, String date, String desc) {
        this.id     = id;
        this.amount = amount;
        this.cat    = cat;
        this.date   = date;
        this.desc   = desc;
    }

    // ── Getters (used by FileHandler and DisplayHelper from frontend team) ───
    public int    getId()          { return id; }
    public double getAmount()      { return amount; }
    public String getCategory()    { return cat; }   // frontend uses getCategory()
    public String getDate()        { return date; }
    public String getDescription() { return desc; }  // frontend uses getDescription()

    // ── Used by Server.java to save to file ──────────────────────────────────
    public String toText() {
        return id + "," + amount + "," + cat + "," + date + "," + desc;
    }

    // ── Used by Server.java to send data to React frontend ───────────────────
    public String toJson() {
        return "{\"id\":"      + id +
               ",\"amount\":" + amount +
               ",\"cat\":\""   + cat  + "\"" +
               ",\"date\":\""  + date + "\"" +
               ",\"desc\":\""  + desc + "\"}";
    }
}
