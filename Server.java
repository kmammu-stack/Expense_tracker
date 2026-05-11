// FILE 3 — Server.java
// Plain Java HTTP server — no frameworks needed!
// This listens for requests from the React frontend

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    static ExpenseManager manager = new ExpenseManager();

    public static void main(String[] args) throws Exception {

        // Start server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Register all routes
        server.createContext("/api/expenses",          new ExpenseHandler());
        server.createContext("/api/summary",           new SummaryHandler());
        server.createContext("/api/budget",            new BudgetHandler());
        server.createContext("/api/health",            new HealthHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("✅ Java backend running at http://localhost:8080");
        System.out.println("📋 API ready — waiting for frontend requests...");
    }

    // ── Helper: add CORS headers so React can connect ─────
    static void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        ex.getResponseHeaders().add("Content-Type",                 "application/json");
    }

    // ── Helper: send JSON response ────────────────────────
    static void sendJson(HttpExchange ex, int code, String json) throws IOException {
        addCorsHeaders(ex);
        byte[] bytes = json.getBytes("UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    // ── Helper: read request body ─────────────────────────
    static String readBody(HttpExchange ex) throws IOException {
        InputStream is  = ex.getRequestBody();
        Scanner sc      = new Scanner(is, "UTF-8");
        String body     = sc.hasNext() ? sc.useDelimiter("\\A").next() : "";
        sc.close();
        return body;
    }

    // ── Helper: parse one value from JSON string ──────────
    // e.g. parseJson("{\"desc\":\"Pizza\"}", "desc") → "Pizza"
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

    // ════════════════════════════════════════════════════
    // ROUTE 1: /api/expenses
    // GET  → returns all expenses as JSON
    // POST → adds a new expense
    // ════════════════════════════════════════════════════
    static class ExpenseHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            String method = ex.getRequestMethod();

            // Handle browser preflight
            if (method.equals("OPTIONS")) {
                sendJson(ex, 200, "{}");
                return;
            }

            // GET /api/expenses — return all
            if (method.equals("GET")) {
                String path = ex.getRequestURI().getPath();

                // GET /api/expenses/category/Food — filter by category
                if (path.contains("/category/")) {
                    String cat = path.substring(path.lastIndexOf("/") + 1);
                    sendJson(ex, 200, manager.listToJson(manager.getByCategory(cat)));
                    return;
                }

                sendJson(ex, 200, manager.listToJson(manager.getAll()));
                return;
            }

            // POST /api/expenses — add new expense
            if (method.equals("POST")) {
                String body   = readBody(ex);
                String desc   = parseJson(body, "desc");
                String amtStr = parseJson(body, "amount");
                String cat    = parseJson(body, "cat");
                String date   = parseJson(body, "date");

                if (desc.isEmpty() || amtStr.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"desc and amount are required\"}");
                    return;
                }

                double amount = Double.parseDouble(amtStr);
                var newExp    = manager.add(desc, amount, cat, date);
                sendJson(ex, 200, newExp.toJson());
                return;
            }

            // DELETE /api/expenses/1 — delete by ID
            if (method.equals("DELETE")) {
                String path = ex.getRequestURI().getPath();
                String idStr = path.substring(path.lastIndexOf("/") + 1);
                int id       = Integer.parseInt(idStr);
                boolean done = manager.delete(id);

                if (done) {
                    sendJson(ex, 200, "{\"message\":\"Deleted successfully\"}");
                } else {
                    sendJson(ex, 404, "{\"message\":\"Expense not found\"}");
                }
            }
        }
    }

    // ════════════════════════════════════════════════════
    // ROUTE 2: /api/summary
    // GET → returns total, avg, max, by category
    // ════════════════════════════════════════════════════
    static class SummaryHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            addCorsHeaders(ex);
            if (ex.getRequestMethod().equals("OPTIONS")) { sendJson(ex, 200, "{}"); return; }
            sendJson(ex, 200, manager.getSummary());
        }
    }

    // ════════════════════════════════════════════════════
    // ROUTE 3: /api/budget?amount=5000
    // GET → checks if total spending is within budget
    // ════════════════════════════════════════════════════
    static class BudgetHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (ex.getRequestMethod().equals("OPTIONS")) { sendJson(ex, 200, "{}"); return; }
            String query  = ex.getRequestURI().getQuery(); // "amount=5000"
            double budget = 0;
            if (query != null && query.startsWith("amount=")) {
                budget = Double.parseDouble(query.split("=")[1]);
            }
            sendJson(ex, 200, manager.checkBudget(budget));
        }
    }

    // ════════════════════════════════════════════════════
    // ROUTE 4: /api/health
    // GET → just checks if backend is running
    // ════════════════════════════════════════════════════
    static class HealthHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            if (ex.getRequestMethod().equals("OPTIONS")) { sendJson(ex, 200, "{}"); return; }
            sendJson(ex, 200, "{\"status\":\"running\",\"message\":\"Java backend is up!\"}");
        }
    }
}
