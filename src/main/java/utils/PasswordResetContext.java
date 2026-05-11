package utils;

public final class PasswordResetContext {

    private static String pendingToken;

    private PasswordResetContext() {
    }

    public static synchronized void setPendingToken(String token) {
        pendingToken = token;
    }

    public static synchronized String consumePendingToken() {
        String token = pendingToken;
        pendingToken = null;
        return token;
    }
}
