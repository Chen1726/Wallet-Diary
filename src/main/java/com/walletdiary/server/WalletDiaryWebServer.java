package com.walletdiary.server;

import com.walletdiary.dao.ExpenseDAO;
import com.walletdiary.dao.ExpenseDeleteDAO;
import com.walletdiary.dao.ExpenseSearchDAO;
import com.walletdiary.dao.ExpenseSummaryDAO;
import com.walletdiary.dao.ExpenseUpdateDAO;
import com.walletdiary.dao.ExpenseViewDAO;
import com.walletdiary.model.Expense;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WalletDiaryWebServer {
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "web";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/dashboard", new DashboardHandler());
        server.createContext("/api/expenses", new ExpenseHandler());
        server.createContext("/api/summary", new SummaryHandler());
        server.start();

        System.out.println("WalletDiary web server is running at http://localhost:8080");
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String jsonBody) throws IOException {
        byte[] responseBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    private static void sendText(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] responseBytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(responseBytes);
        }
    }

    private static String getRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseJsonBody(String body) {
        Map<String, String> values = new HashMap<>();
        if (body == null || body.isBlank()) {
            return values;
        }

        Pattern pattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\"([^\"]*)\"|([^,}\"]+))");
        Matcher matcher = pattern.matcher(body);

        while (matcher.find()) {
            String key = matcher.group(1);
            String rawValue = matcher.group(2);
            String value = rawValue;

            if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
                value = rawValue.substring(1, rawValue.length() - 1);
            }

            values.put(key, value.trim());
        }

        return values;
    }

    private static String toJsonMoney(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private static String toJsonExpense(Expense expense) {
        return "{\"expenseId\":" + expense.getExpenseId()
                + ",\"amount\":\"" + toJsonMoney(expense.getAmount()) + "\""
                + ",\"category\":\"" + escapeJson(expense.getCategory()) + "\""
                + ",\"description\":\"" + escapeJson(expense.getDescription()) + "\""
                + ",\"expenseDate\":\"" + expense.getExpenseDate() + "\"}";
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String toJsonList(List<Expense> expenses) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < expenses.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(toJsonExpense(expenses.get(i)));
        }
        builder.append("]");
        return builder.toString();
    }

    private static String getQueryValue(String query, String key) {
        if (query == null || query.isBlank()) {
            return "";
        }
        String[] parts = query.split("&");
        for (String part : parts) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && key.equals(pair[0])) {
                try {
                    return URLDecoder.decode(pair[1], StandardCharsets.UTF_8.name());
                } catch (Exception e) {
                    return pair[1];
                }
            }
        }
        return "";
    }

    private static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                path = "/index.html";
            }

            File file = new File(WEB_ROOT + path);
            if (!file.exists() || !file.isFile()) {
                sendText(exchange, 404, "File not found");
                return;
            }

            byte[] content = Files.readAllBytes(file.toPath());
            String mimeType = "text/plain";
            if (path.endsWith(".html")) {
                mimeType = "text/html; charset=UTF-8";
            } else if (path.endsWith(".css")) {
                mimeType = "text/css; charset=UTF-8";
            } else if (path.endsWith(".js")) {
                mimeType = "application/javascript; charset=UTF-8";
            }

            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(content);
            }
        }
    }

    private static class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method not allowed");
                return;
            }

            try {
                ExpenseSummaryDAO.ExpenseSummary summary = new ExpenseSummaryDAO().getSummary();
                List<Expense> recentExpenses = new ExpenseViewDAO().getAllExpenses();

                List<Expense> limitedRecent = recentExpenses.size() > 5 ? recentExpenses.subList(0, 5) : recentExpenses;
                StringBuilder response = new StringBuilder();
                response.append("{\"totalExpenses\":")
                        .append(summary.getTotalExpenses())
                        .append(",\"totalAmount\":\"")
                        .append(toJsonMoney(summary.getTotalAmount()))
                        .append("\",\"recentExpenses\":")
                        .append(toJsonList(limitedRecent))
                        .append("}");

                sendJson(exchange, 200, response.toString());
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":\"Unable to load dashboard data.\"}");
            }
        }
    }

    private static class SummaryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendText(exchange, 405, "Method not allowed");
                return;
            }

            try {
                ExpenseSummaryDAO.ExpenseSummary summary = new ExpenseSummaryDAO().getSummary();
                String json = "{\"totalExpenses\":" + summary.getTotalExpenses()
                        + ",\"totalAmount\":\"" + toJsonMoney(summary.getTotalAmount()) + "\"}";
                sendJson(exchange, 200, json);
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":\"Unable to load summary.\"}");
            }
        }
    }

    private static class ExpenseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method) && "/api/expenses/search".equals(path)) {
                handleSearchExpenses(exchange);
                return;
            }

            if (path != null && path.startsWith("/api/expenses/")) {
                handleExpenseById(exchange, path, method);
                return;
            }

            if ("GET".equalsIgnoreCase(method) && path.equals("/api/expenses")) {
                handleListExpenses(exchange);
                return;
            }

            if ("POST".equalsIgnoreCase(method) && path.equals("/api/expenses")) {
                handleAddExpense(exchange);
                return;
            }

            sendText(exchange, 404, "Not found");
        }

        private void handleListExpenses(HttpExchange exchange) throws IOException {
            try {
                List<Expense> expenses = new ExpenseViewDAO().getAllExpenses();
                sendJson(exchange, 200, toJsonList(expenses));
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":\"Unable to load expenses.\"}");
            }
        }

        private void handleAddExpense(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> data = parseJsonBody(getRequestBody(exchange));
                String amountText = data.get("amount");
                String category = data.get("category");
                String description = data.get("description");
                String dateText = data.get("expenseDate");

                if (amountText == null || amountText.isBlank()) {
                    sendJson(exchange, 400, "{\"error\":\"Amount is required.\"}");
                    return;
                }
                if (category == null || category.isBlank()) {
                    sendJson(exchange, 400, "{\"error\":\"Category is required.\"}");
                    return;
                }
                if (dateText == null || dateText.isBlank()) {
                    sendJson(exchange, 400, "{\"error\":\"Expense date is required.\"}");
                    return;
                }

                BigDecimal amount = new BigDecimal(amountText);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    sendJson(exchange, 400, "{\"error\":\"Amount must be a positive number.\"}");
                    return;
                }

                LocalDate expenseDate = LocalDate.parse(dateText);
                Expense expense = new Expense(amount, category.trim(), description == null ? "" : description.trim(), expenseDate);
                int generatedId = new ExpenseDAO().addExpense(expense);

                String response = "{\"success\":true,\"expenseId\":" + generatedId + "}";
                sendJson(exchange, 200, response);
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":\"Unable to add expense.\"}");
            }
        }

        private void handleSearchExpenses(HttpExchange exchange) throws IOException {
            try {
                String category = getQueryValue(exchange.getRequestURI().getQuery(), "category");
                if (category == null || category.isBlank()) {
                    sendJson(exchange, 400, "{\"error\":\"Category is required.\"}");
                    return;
                }

                List<Expense> expenses = new ExpenseSearchDAO().searchByCategory(category.trim());
                sendJson(exchange, 200, toJsonList(expenses));
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":\"Unable to search expenses.\"}");
            }
        }

        private void handleExpenseById(HttpExchange exchange, String path, String method) throws IOException {
            String idText = path.substring("/api/expenses/".length());
            int expenseId;

            try {
                expenseId = Integer.parseInt(idText);
            } catch (NumberFormatException e) {
                sendJson(exchange, 400, "{\"error\":\"Invalid Expense ID.\"}");
                return;
            }

            if ("GET".equalsIgnoreCase(method)) {
                try {
                    List<Expense> expenses = new ExpenseViewDAO().getAllExpenses();
                    List<Expense> matching = new ArrayList<>();
                    for (Expense expense : expenses) {
                        if (expense.getExpenseId() == expenseId) {
                            matching.add(expense);
                        }
                    }
                    if (matching.isEmpty()) {
                        sendJson(exchange, 404, "{\"error\":\"Expense not found.\"}");
                    } else {
                        sendJson(exchange, 200, toJsonList(matching));
                    }
                } catch (Exception e) {
                    sendJson(exchange, 500, "{\"error\":\"Unable to load expense.\"}");
                }
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {
                try {
                    Map<String, String> data = parseJsonBody(getRequestBody(exchange));
                    String amountText = data.get("amount");
                    String category = data.get("category");
                    String description = data.get("description");
                    String dateText = data.get("expenseDate");

                    if (amountText == null || amountText.isBlank() || category == null || category.isBlank() || dateText == null || dateText.isBlank()) {
                        sendJson(exchange, 400, "{\"error\":\"All fields are required.\"}");
                        return;
                    }

                    ExpenseUpdateDAO updateDAO = new ExpenseUpdateDAO();
                    if (!updateDAO.exists(expenseId)) {
                        sendJson(exchange, 404, "{\"error\":\"Expense not found.\"}");
                        return;
                    }

                    BigDecimal amount = new BigDecimal(amountText);
                    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                        sendJson(exchange, 400, "{\"error\":\"Amount must be a positive number.\"}");
                        return;
                    }

                    LocalDate expenseDate = LocalDate.parse(dateText);
                    boolean updated = updateDAO.updateExpense(expenseId, amount, category.trim(), description == null ? "" : description.trim(), expenseDate);
                    if (updated) {
                        sendJson(exchange, 200, "{\"success\":true,\"message\":\"Expense updated successfully.\"}");
                    } else {
                        sendJson(exchange, 404, "{\"error\":\"Expense not found.\"}");
                    }
                } catch (Exception e) {
                    sendJson(exchange, 500, "{\"error\":\"Unable to update expense.\"}");
                }
                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {
                try {
                    ExpenseDeleteDAO deleteDAO = new ExpenseDeleteDAO();
                    if (!deleteDAO.exists(expenseId)) {
                        sendJson(exchange, 404, "{\"error\":\"Expense not found.\"}");
                        return;
                    }

                    boolean deleted = deleteDAO.deleteExpense(expenseId);
                    if (deleted) {
                        sendJson(exchange, 200, "{\"success\":true,\"message\":\"Expense deleted successfully.\"}");
                    } else {
                        sendJson(exchange, 404, "{\"error\":\"Expense not found.\"}");
                    }
                } catch (Exception e) {
                    sendJson(exchange, 500, "{\"error\":\"Unable to delete expense.\"}");
                }
                return;
            }

            sendText(exchange, 405, "Method not allowed");
        }
    }
}
