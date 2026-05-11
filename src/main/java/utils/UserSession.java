package utils;

import entities.Utilisateur;

public final class UserSession {

    private static Utilisateur currentUser;

    private UserSession() {
    }

    public static void start(Utilisateur utilisateur) {
        currentUser = utilisateur;
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
    }
}
