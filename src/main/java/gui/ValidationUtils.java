package gui;

import javafx.scene.control.Alert;

import java.time.LocalDate;

public class ValidationUtils {

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            showError(fieldName + " est obligatoire.");
            return false;
        }
        return true;
    }

    public static boolean validateMinLength(String value, int minLength, String fieldName) {
        if (value == null || value.trim().length() < minLength) {
            showError(fieldName + " doit contenir au moins " + minLength + " caractères.");
            return false;
        }
        return true;
    }

    public static boolean validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            showError(fieldName + " ne doit pas dépasser " + maxLength + " caractères.");
            return false;
        }
        return true;
    }

    public static boolean validateDateNotPast(LocalDate date, String fieldName) {
        if (date == null) {
            showError(fieldName + " est obligatoire.");
            return false;
        }
        if (date.isBefore(LocalDate.now())) {
            showError(fieldName + " doit être aujourd'hui ou dans le futur.");
            return false;
        }
        return true;
    }

    public static boolean validatePositiveDouble(String value, String fieldName) {
        if (!validateRequired(value, fieldName)) {
            return false;
        }
        try {
            double num = Double.parseDouble(value.trim());
            if (num <= 0) {
                showError(fieldName + " doit être un nombre positif.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showError(fieldName + " doit être un nombre valide.");
            return false;
        }
    }

    public static boolean validateDoubleRange(String value, double min, double max, String fieldName) {
        if (!validateRequired(value, fieldName)) {
            return false;
        }
        try {
            double num = Double.parseDouble(value.trim());
            if (num < min || num > max) {
                showError(fieldName + " doit être entre " + min + " et " + max + ".");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showError(fieldName + " doit être un nombre valide.");
            return false;
        }
    }

    public static boolean validateNotNull(Object value, String fieldName) {
        if (value == null) {
            showError(fieldName + " est obligatoire.");
            return false;
        }
        return true;
    }

    public static boolean validateFileSelected(String filePath, String fieldName) {
        if (filePath == null || filePath.trim().isEmpty() || filePath.equals("Aucun fichier sélectionné")) {
            showError(fieldName + " est obligatoire.");
            return false;
        }
        return true;
    }
}

