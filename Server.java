// Server.java — Plain Java HTTP Server
// Connects your backend to the React frontend
// Also wires in features team's SortFilter, Statistics, BudgetTracker

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    static ExpenseManager manager     = new ExpenseManager();
    static BudgetTracker  budgetTracker = new BudgetTracker();

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ── Routes ────────────────────────────────────────────────
        server.createContext("/api/expenses",  new ExpenseHandler());
        server.createContext("/api/summary",   new SummaryHandler());
        server.createContext("/api/budget",    new BudgetHandler());
        server.createContext("/api/sort",      new SortHandler());
        server.createContext("/api/stats",     new StatsHandler());
        server.createContext("/api/health",    new HealthHandler());
        server.createContext("/",              new FrontendHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("✅ Java backend running at http://localhost:8080");
        System.out.println("📋 API ready!");
    }

    // ── CORS headers so React frontend can connect ────────────────
    static void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        ex.getResponseHeaders().add("Content-Type",                 "application/json");
    }

    // ── Send JSON response ────────────────────────────────────────
    static void sendJson(HttpExchange ex, int code, String json) throws IOException {
        addCorsHeaders(ex);
        byte[] bytes = json.getBytes("UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    // ── Read request body ─────────────────────────────────────────
    static String readBody(HttpExchange ex) throws IOException {
        Scanner sc = new Scanner(ex.getRequestBody(), "UTF-8");
        String body = sc.hasNext() ? sc.useDelimiter("\\A").next() : "";
        sc.close();
        return body;
    }

    // ── Parse one value from JSON string ──────────────────────────
    static String parseJson(String json, String key) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int start = idx + search.length();
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            return json.substring(start + 1, end);
        } else {
            int end = json.indexOf(',', start);
            if (end == -1) end = json.indexOf('}', start);
            return json.substring(start, end).trim();
        }
    }

    // ════════════════════════════════════════════════════════
    // ROUTE 1: /api/expenses
    // GET    → all expenses or filter by category
    // POST   → add new expense
    // DELETE → delete by id  (/api/expenses/1)
    // PUT    → update by id  (/api/expenses/1)
    // ════════════════════════════════════════════════════════
    static class ExpenseHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String method = ex.getRequestMethod();
            String path   = ex.getRequestURI().getPath();
            String query  = ex.getRequestURI().getQuery();

            if (method.equals("OPTIONS")) { sendJson(ex, 200, "{}"); return; }

            // GET /api/expenses?cat=Food  → filter by category
            if (method.equals("GET") && query != null && query.startsWith("cat=")) {
                String cat = query.split("=")[1];
                sendJson(ex, 200, manager.listToJson(manager.getByCategory(cat)));
                return;
            }

            // GET /api/expenses?search=pizza  → search by keyword
            if (method.equals("GET") && query != null && query.startsWith("search=")) {
                String keyword = query.split("=")[1];
                sendJson(ex, 200, manager.listToJson(manager.search(keyword)));
                return;
            }

            // GET /api/expenses  → return all
            if (method.equals("GET")) {
                sendJson(ex, 200, manager.listToJson(manager.getAllExpenses()));
                return;
            }

            // POST /api/expenses  → add expense
            // Body: {"amount":150,"cat":"Food","date":"2024-01-15","desc":"Pizza"}
            if (method.equals("POST")) {
                String body   = readBody(ex);
                String amtStr = parseJson(body, "amount");
                String cat    = parseJson(body, "cat");
                String date   = parseJson(body, "date");
                String desc   = parseJson(body, "desc");

                if (amtStr.isEmpty() || desc.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"amount and desc are required\"}");
                    return;
                }

                double amount = Double.parseDouble(amtStr);
                Expense newExp = manager.addExpense(amount, cat, date, desc);
                sendJson(ex, 200, newExp.toJson());
                return;
            }

            // DELETE /api/expenses/1  → delete by id
            if (method.equals("DELETE")) {
                String idStr = path.substring(path.lastIndexOf("/") + 1);
                int id = Integer.parseInt(idStr);
                boolean done = manager.deleteExpense(id);
                if (done) {
                    sendJson(ex, 200, "{\"message\":\"Deleted successfully\"}");
                } else {
                    sendJson(ex, 404, "{\"message\":\"Expense not found\"}");
                }
                return;
            }

            // PUT /api/expenses/1  → update by id
            if (method.equals("PUT")) {
                String idStr  = path.substring(path.lastIndexOf("/") + 1);
                int    id     = Integer.parseInt(idStr);
                String body   = readBody(ex);
                String amtStr = parseJson(body, "amount");
                String cat    = parseJson(body, "cat");
                String date   = parseJson(body, "date");
                String desc   = parseJson(body, "desc");

                double amount = amtStr.isEmpty() ? -1 : Double.parseDouble(amtStr);
                boolean updated = manager.updateExpense(
                    id,
                    amount,
                    cat.isEmpty()  ? null : cat,
                    date.isEmpty() ? null : date,
                    desc.isEmpty() ? null : desc
                );
                if (updated) {
                    sendJson(ex, 200, "{\"message\":\"Updated successfully\"}");
                } else {
                    sendJson(ex, 404, "{\"message\":\"Expense not found\"}");
                }
            }
        }
    }

    // ════════════════════════════════════════════════════════
    // ROUTE 2: /api/summary
    // Uses Statistics class from features team!
    // ════════════════════════════════════════════════════════
    static class SummaryHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (ex.getRequestMethod().equals("OPTIONS")) { sendJson(ex, 200, "{}"); return; }
            sendJson(ex, 200, manager.getSummary());
        }
    }

    // ════════════════════════════════════════════════════════
    // ROUTE 3: /api/budget?amount=5000
    //          /api/budget/set  POST  body: {"cat":"Food","limit":2000}
    // Uses BudgetTracker class from features team!
    // ════════════════════════════════════════════════════════
    static class BudgetHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String method = ex.getRequestMethod();
            String path   = ex.getRequestURI().getPath();

            if (method.equals("OPTIONS")) { sendJson(ex, 200, "{}"); return; }

            // POST /api/budget/set  → set budget for a category
            if (method.equals("POST") && path.endsWith("/set")) {
                String body  = readBody(ex);
                String cat   = parseJson(body, "cat");
                String limit = parseJson(body, "limit");
                if (!cat.isEmpty() && !limit.isEmpty()) {
                    budgetTracker.setBudget(cat, Double.parseDouble(limit));
                    sendJson(ex, 200, "{\"message\":\"Budget set for " + cat + "\"}");
                } else {
                    sendJson(ex, 400, "{\"error\":\"cat and limit required\"}");
                }
                return;
            }

            // GET /api/budget?amount=5000  → overall budget check
            String query  = ex.getRequestURI().getQuery();
            double budget = 0;
            if (query != null && query.startsWith("amount=")) {
                budget = Double.parseDouble(query.split("=")[1]);
            }
            sendJson(ex, 200, manager.checkBudget(budget));
        }
    }

    // ════════════════════════════════════════════════════════
    // ROUTE 4: /api/sort?by=amount  or  ?by=date
    // Uses SortFilter class from features team!
    // ════════════════════════════════════════════════════════
    static class SortHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (ex.getRequestMethod().equals("OPTIONS")) { sendJson(ex, 200, "{}"); return; }

            String query = ex.getRequestURI().getQuery(); // "by=amount" or "by=date"
            ArrayList<Expense> sorted;

            if (query != null && query.equals("by=date")) {
                sorted = SortFilter.sortByDate(manager.getAllExpenses());
            } else {
                sorted = SortFilter.sortByAmount(manager.getAllExpenses());
            }

            sendJson(ex, 200, manager.listToJson(sorted));
        }
    }

    // ════════════════════════════════════════════════════════
    // ROUTE 5: /api/stats
    // Uses Statistics class from features team!
    // ════════════════════════════════════════════════════════
    static class StatsHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (ex.getRequestMethod().equals("OPTIONS")) { sendJson(ex, 200, "{}"); return; }

            Statistics stats    = new Statistics(manager.getAllExpenses());
            Expense    highest  = stats.getHighestExpense();
            Expense    lowest   = stats.getLowestExpense();

            String highJson = highest != null ? highest.toJson() : "null";
            String lowJson  = lowest  != null ? lowest.toJson()  : "null";

            String json = "{" +
                "\"totalSpent\":"    + stats.getTotalSpent()      + "," +
                "\"avgExpense\":"    + stats.getAverageExpense()   + "," +
                "\"topCategory\":\"" + stats.getTopCategory()     + "\"," +
                "\"highestExpense\":" + highJson                  + "," +
                "\"lowestExpense\":"  + lowJson                   +
            "}";

            sendJson(ex, 200, json);
        }
    }

    // ════════════════════════════════════════════════════════
    // ROUTE 6: /api/health  → quick check backend is running
    // ════════════════════════════════════════════════════════
    static class HealthHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (ex.getRequestMethod().equals("OPTIONS")) { sendJson(ex, 200, "{}"); return; }
            sendJson(ex, 200, "{\"status\":\"running\",\"message\":\"Java backend is up!\"}");
        }
    }

    // ════════════════════════════════════════════════════════
    // ROUTE 7: /  → Frontend static pages and assets
    // ════════════════════════════════════════════════════════
    static class FrontendHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();
            
            if (path.equals("/") || path.equals("/dashboard")) {
                serveFile(ex, "src/main/resources/templates/dashboard.html", "text/html");
            } else if (path.equals("/reports")) {
                serveFile(ex, "src/main/resources/templates/reports.html", "text/html");
            } else if (path.equals("/budget")) {
                serveFile(ex, "src/main/resources/templates/budget.html", "text/html");
            } else if (path.equals("/add-expense")) {
                serveFile(ex, "src/main/resources/templates/add-expense.html", "text/html");
            } else if (path.equals("/css/style.css")) {
                serveFile(ex, "src/main/resources/static/css/style.css", "text/css");
            } else if (path.equals("/js/script.js")) {
                serveFile(ex, "src/main/resources/static/js/script.js", "application/javascript");
            } else {
                String response = "404 Not Found";
                ex.sendResponseHeaders(404, response.length());
                OutputStream os = ex.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private void serveFile(HttpExchange ex, String filePath, String contentType) throws IOException {
            File file = new File(filePath);
            if (!file.exists()) {
                String response = "404 File Not Found";
                ex.sendResponseHeaders(404, response.length());
                OutputStream os = ex.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            ex.getResponseHeaders().set("Content-Type", contentType);
            ex.sendResponseHeaders(200, file.length());
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = ex.getResponseBody()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
