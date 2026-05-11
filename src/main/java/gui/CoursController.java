package gui;

import entities.Cours;
import entities.Module;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoursController {

    private static final List<String> ALLOWED_STATUS = Arrays.asList("brouillon", "publie", "archive");

    public void configureFormDefaults(
            ComboBox<String> statutCombo,
            DatePicker dateDebutPicker,
            DatePicker dateFinPicker
    ) {
        statutCombo.setItems(FXCollections.observableArrayList(ALLOWED_STATUS));
        if (statutCombo.getValue() == null) {
            statutCombo.setValue("brouillon");
        }
        if (dateDebutPicker.getValue() == null) {
            dateDebutPicker.setValue(LocalDate.now());
        }
        if (dateFinPicker.getValue() == null) {
            dateFinPicker.setValue(LocalDate.now().plusDays(30));
        }
    }

    public Map<String, String> validateCoursForm(
            TextField codeField,
            TextField titreField,
            TextArea descriptionArea,
            TextField niveauField,
            TextField creditsField,
            TextField langueField,
            DatePicker dateDebutPicker,
            DatePicker dateFinPicker,
            ComboBox<String> statutCombo,
            TextField imageUrlField,
            TextArea prerequisArea
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        String code = safeText(codeField.getText());
        String titre = safeText(titreField.getText());
        String description = safeText(descriptionArea.getText());
        String niveau = safeText(niveauField.getText());
        String credits = safeText(creditsField.getText());
        String langue = safeText(langueField.getText());
        String imageUrl = safeText(imageUrlField.getText());
        String prerequis = safeText(prerequisArea.getText());

        if (code.length() < 3) {
            errors.put("code", "Le code du cours doit contenir au moins 3 caracteres.");
        }
        if (!code.isEmpty() && !code.matches("^[A-Za-z0-9_-]+$")) {
            errors.put("code", "Le code du cours ne doit contenir que lettres, chiffres, tirets ou underscores.");
        }
        if (titre.length() < 3) {
            errors.put("titre", "Le titre du cours doit contenir au moins 3 caracteres.");
        }
        if (!description.isEmpty() && description.length() < 10) {
            errors.put("description", "La description doit contenir au moins 10 caracteres lorsqu'elle est remplie.");
        }
        if (niveau.length() < 2) {
            errors.put("niveau", "Le niveau du cours est obligatoire.");
        }
        if (!credits.isEmpty()) {
            try {
                int parsedCredits = Integer.parseInt(credits);
                if (parsedCredits <= 0) {
                    errors.put("credits", "Les credits doivent etre superieurs a zero.");
                }
            } catch (NumberFormatException e) {
                errors.put("credits", "Les credits doivent etre un entier valide.");
            }
        }
        if (langue.length() < 2) {
            errors.put("langue", "La langue du cours est obligatoire.");
        }
        if (statutCombo.getValue() == null || !ALLOWED_STATUS.contains(statutCombo.getValue())) {
            errors.put("statut", "Veuillez choisir un statut valide pour le cours.");
        }
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null
                && !dateFinPicker.getValue().isAfter(dateDebutPicker.getValue())) {
            errors.put("date_fin", "La date de fin doit etre strictement superieure a la date de debut.");
        }
        if (!imageUrl.isEmpty() && !imageUrl.matches("^(https?://.+|[\\w\\-./]+)$")) {
            errors.put("image", "L'URL image doit etre un lien http(s) ou un chemin simple valide.");
        }
        if (!prerequis.isEmpty() && prerequis.length() < 3) {
            errors.put("prerequis", "Les prerequis saisis sont trop courts.");
        }
        return errors;
    }

    public Cours buildCours(
            Cours cours,
            Module module,
            TextField codeField,
            TextField titreField,
            TextArea descriptionArea,
            TextField niveauField,
            TextField creditsField,
            TextField langueField,
            DatePicker dateDebutPicker,
            DatePicker dateFinPicker,
            ComboBox<String> statutCombo,
            TextField imageUrlField,
            TextArea prerequisArea
    ) {
        Cours target = cours != null ? cours : new Cours();
        target.setModule(module);
        target.setCodeCours(safeText(codeField.getText()));
        target.setTitre(safeText(titreField.getText()));
        target.setDescription(nullableText(descriptionArea.getText()));
        target.setNiveau(safeText(niveauField.getText()));
        target.setCredits(parseNullableInteger(creditsField.getText()));
        target.setLangue(safeText(langueField.getText()));
        target.setDateDebut(dateDebutPicker.getValue());
        target.setDateFin(dateFinPicker.getValue());
        target.setStatut(statutCombo.getValue());
        target.setImageCoursUrl(nullableText(imageUrlField.getText()));
        target.setPrerequis(parseCommaSeparated(prerequisArea.getText()));
        return target;
    }

    public void populateForm(
            Cours cours,
            TextField codeField,
            TextField titreField,
            TextArea descriptionArea,
            TextField niveauField,
            TextField creditsField,
            TextField langueField,
            DatePicker dateDebutPicker,
            DatePicker dateFinPicker,
            ComboBox<String> statutCombo,
            TextField imageUrlField,
            TextArea prerequisArea
    ) {
        codeField.setText(cours.getCodeCours());
        titreField.setText(cours.getTitre());
        descriptionArea.setText(cours.getDescription());
        niveauField.setText(cours.getNiveau());
        creditsField.setText(cours.getCredits() != null ? String.valueOf(cours.getCredits()) : "");
        langueField.setText(cours.getLangue());
        dateDebutPicker.setValue(cours.getDateDebut() != null ? cours.getDateDebut() : LocalDate.now());
        dateFinPicker.setValue(cours.getDateFin() != null ? cours.getDateFin() : LocalDate.now().plusDays(30));
        statutCombo.setValue(cours.getStatut() != null ? cours.getStatut() : "brouillon");
        imageUrlField.setText(cours.getImageCoursUrl());
        prerequisArea.setText(cours.getPrerequis() != null ? String.join(", ", cours.getPrerequis()) : "");
    }

    public void clearForm(
            TextField codeField,
            TextField titreField,
            TextArea descriptionArea,
            TextField niveauField,
            TextField creditsField,
            TextField langueField,
            DatePicker dateDebutPicker,
            DatePicker dateFinPicker,
            ComboBox<String> statutCombo,
            TextField imageUrlField,
            TextArea prerequisArea
    ) {
        codeField.clear();
        titreField.clear();
        descriptionArea.clear();
        niveauField.clear();
        creditsField.clear();
        langueField.clear();
        dateDebutPicker.setValue(LocalDate.now());
        dateFinPicker.setValue(LocalDate.now().plusDays(30));
        statutCombo.setValue("brouillon");
        imageUrlField.clear();
        prerequisArea.clear();
    }

    private Integer parseNullableInteger(String value) {
        String text = safeText(value);
        return text.isEmpty() ? null : Integer.parseInt(text);
    }

    private List<String> parseCommaSeparated(String raw) {
        String value = safeText(raw);
        if (value.isEmpty()) {
            return FXCollections.observableArrayList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private String nullableText(String value) {
        String text = safeText(value);
        return text.isEmpty() ? null : text;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
