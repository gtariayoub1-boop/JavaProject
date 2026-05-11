package utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class PasswordResetLinkServer {

    private static final int PORT = 8765;
    private static HttpServer server;

    private PasswordResetLinkServer() {
    }

    public static synchronized void start() {
        if (server != null) {
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
            server.createContext("/reset-password", PasswordResetLinkServer::handleResetLink);
            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            System.err.println("Unable to start password reset link server: " + e.getMessage());
            server = null;
        }
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private static void handleResetLink(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI());
        String token = params.get("token");

        String response;
        int statusCode;
        if (token == null || token.isBlank()) {
            statusCode = 400;
            response = buildHtml("Missing token", "The reset link is invalid. Return to the desktop app and request another email.");
        } else {
            PasswordResetContext.setPendingToken(token.trim());
            Platform.runLater(() -> {
                try {
                    SceneManager.switchScene("/forgot-password.fxml", "Reset Password");
                    SceneManager.bringToFront();
                } catch (IOException e) {
                    System.err.println("Unable to open reset password page: " + e.getMessage());
                }
            });
            statusCode = 200;
            response = buildHtml("Link received", "The desktop app received your reset token. Go back to the app to choose a new password.");
        }

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                params.put(keyValue[0], java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
            }
        }
        return params;
    }

    private static String buildHtml(String title, String body) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>" + title + "</title>"
                + "<style>body{font-family:Arial,sans-serif;background:#f4f6fb;color:#172033;padding:40px;}"
                + ".card{max-width:620px;margin:0 auto;background:#fff;padding:28px;border-radius:16px;"
                + "box-shadow:0 18px 40px rgba(20,34,66,.12);}h1{margin-top:0;}p{line-height:1.6;}</style>"
                + "</head><body><div class=\"card\"><h1>" + title + "</h1><p>" + body + "</p></div></body></html>";
    }
}
