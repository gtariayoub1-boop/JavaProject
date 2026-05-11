package gui;

import entities.Module;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleController {

    private static final List<String> ALLOWED_STATUS = Arrays.asList("brouillon", "publie", "archive");

    public void configureFormDefaults(ComboBox<String> statutCombo, DatePicker datePublicationPicker) {
        statutCombo.setItems(FXCollections.observableArrayList(ALLOWED_STATUS));
        if (statutCombo.getValue() == null) {
            statutCombo.setValue("brouillon");
        }
        if (datePublicationPicker.getValue() == null) {
            datePublicationPicker.setValue(LocalDate.now());
        }
    }

    public Map<String, String> validateModuleForm(
            TextField titreField,
            TextArea descriptionArea,
            TextField ordreField,
            TextArea objectifsArea,
            TextField dureeField,
            ComboBox<String> statutCombo
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        String titre = safeText(titreField.getText());
        String description = safeText(descriptionArea.getText());
        String ordre = safeText(ordreField.getText());
        String objectifs = safeText(objectifsArea.getText());
        String duree = safeText(dureeField.getText());
        String statut = statutCombo.getValue();

        if (titre.length() < 3) {
            errors.put("titre", "Le titre du module doit contenir au moins 3 caracteres.");
        }
        if (description.length() < 10) {
            errors.put("description", "La description du module doit contenir au moins 10 caracteres.");
        }
        if (objectifs.length() < 10) {
            errors.put("objectifs", "Les objectifs d'apprentissage doivent contenir au moins 10 caracteres.");
        }
        if (ordre.isEmpty()) {
            errors.put("ordre", "L'ordre d'affichage est obligatoire.");
        } else {
            try {
                int ordreAffichage = Integer.parseInt(ordre);
                if (ordreAffichage < 0) {
                    errors.put("ordre", "L'ordre d'affichage doit etre positif ou nul.");
                }
            } catch (NumberFormatException e) {
                errors.put("ordre", "L'ordre d'affichage doit etre un entier valide.");
            }
        }
        if (!duree.isEmpty()) {
            try {
                int dureeEstimee = Integer.parseInt(duree);
                if (dureeEstimee <= 0) {
                    errors.put("duree", "La duree estimee doit etre superieure a zero.");
                }
            } catch (NumberFormatException e) {
                errors.put("duree", "La duree estimee doit etre un entier valide.");
            }
        }
        if (statut == null || !ALLOWED_STATUS.contains(statut)) {
            errors.put("statut", "Veuillez choisir un statut valide pour le module.");
        }
        return errors;
    }

    public Module buildModule(
            Module module,
            TextField titreField,
            TextArea descriptionArea,
            TextField ordreField,
            TextArea objectifsArea,
            TextField dureeField,
            DatePicker datePublicationPicker,
            ComboBox<String> statutCombo,
            TextArea ressourcesArea
    ) {
        Module target = module != null ? module : new Module();
        target.setTitreModule(safeText(titreField.getText()));
        target.setDescription(safeText(descriptionArea.getText()));
        target.setOrdreAffichage(Integer.parseInt(safeText(ordreField.getText())));
        target.setObjectifsApprentissage(safeText(objectifsArea.getText()));
        target.setDureeEstimeeHeures(parseNullableInteger(dureeField.getText()));
        target.setDatePublication(datePublicationPicker.getValue() != null
                ? datePublicationPicker.getValue().atStartOfDay()
                : LocalDateTime.now());
        target.setStatut(statutCombo.getValue());
        target.setRessourcesComplementaires(parseResources(ressourcesArea.getText()));
        return target;
    }

    public void populateForm(
            Module module,
            TextField titreField,
            TextArea descriptionArea,
            TextField ordreField,
            TextArea objectifsArea,
            TextField dureeField,
            DatePicker datePublicationPicker,
            ComboBox<String> statutCombo,
            TextArea ressourcesArea
    ) {
        titreField.setText(module.getTitreModule());
        descriptionArea.setText(module.getDescription());
        ordreField.setText(String.valueOf(module.getOrdreAffichage()));
        objectifsArea.setText(module.getObjectifsApprentissage());
        dureeField.setText(module.getDureeEstimeeHeures() != null ? String.valueOf(module.getDureeEstimeeHeures()) : "");
        datePublicationPicker.setValue(module.getDatePublication() != null ? module.getDatePublication().toLocalDate() : LocalDate.now());
        statutCombo.setValue(module.getStatut() != null ? module.getStatut() : "brouillon");
        ressourcesArea.setText(String.join(", ", module.getRessourcesComplementaires()));
    }

    public void clearForm(
            TextField titreField,
            TextArea descriptionArea,
            TextField ordreField,
            TextArea objectifsArea,
            TextField dureeField,
            DatePicker datePublicationPicker,
            ComboBox<String> statutCombo,
            TextArea ressourcesArea
    ) {
        titreField.clear();
        descriptionArea.clear();
        ordreField.setText("0");
        objectifsArea.clear();
        dureeField.clear();
        datePublicationPicker.setValue(LocalDate.now());
        statutCombo.setValue("brouillon");
        ressourcesArea.clear();
    }

    private Integer parseNullableInteger(String value) {
        String text = safeText(value);
        return text.isEmpty() ? null : Integer.parseInt(text);
    }

    private List<String> parseResources(String rawValue) {
        String text = safeText(rawValue);
        if (text.isEmpty()) {
            return FXCollections.observableArrayList();
        }
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}
