package ui.controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import utils.SettingsManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;


public class SettingsUIController implements Initializable {

    @FXML private TextField defaultOutputLocationField;
    @FXML private ComboBox<String> fileNamingConventionBox;
    @FXML private CheckBox overwriteExistingFilesCheckbox;
    @FXML private Button browseButton;
    @FXML private Button resetButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private SettingsManager settingsManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        settingsManager = SettingsManager.getInstance();
        
        fileNamingConventionBox.setItems(FXCollections.observableArrayList(
            "Keep original name",
            "Add timestamp",
            "Add _converted suffix"
        ));
        fileNamingConventionBox.setValue("Keep original name");
        
        loadSettings();
        
        setupTooltips();
    }

    private void setupTooltips() {
        defaultOutputLocationField.setTooltip(new Tooltip("Default folder where converted files will be saved"));
        browseButton.setTooltip(new Tooltip("Browse for output folder"));
        fileNamingConventionBox.setTooltip(new Tooltip("Choose how converted files should be named"));
        overwriteExistingFilesCheckbox.setTooltip(new Tooltip("Automatically overwrite files with the same name"));
        resetButton.setTooltip(new Tooltip("Reset all settings to default values"));
        saveButton.setTooltip(new Tooltip("Save current settings"));
        cancelButton.setTooltip(new Tooltip("Close without saving changes"));
    }

    private void loadSettings() {
        String outputLocation = settingsManager.getDefaultOutputLocation();
        if (outputLocation != null && !outputLocation.isEmpty()) {
            defaultOutputLocationField.setText(outputLocation);
        }
        
        String namingConvention = settingsManager.getFileNamingConvention();
        fileNamingConventionBox.setValue(namingConvention);
        
        overwriteExistingFilesCheckbox.setSelected(settingsManager.getOverwriteExistingFiles());
    }

    @FXML
    private void handleBrowseOutputLocation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Default Output Location");
        
        String currentPath = defaultOutputLocationField.getText();
        if (!currentPath.isEmpty()) {
            File currentDir = new File(currentPath);
            if (currentDir.exists() && currentDir.isDirectory()) {
                directoryChooser.setInitialDirectory(currentDir);
            }
        }
        
        Stage stage = (Stage) browseButton.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            defaultOutputLocationField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleResetDefaults() {
        defaultOutputLocationField.setText("");
        fileNamingConventionBox.setValue("Keep original name");
        overwriteExistingFilesCheckbox.setSelected(false);
        
        showInformation("Settings Reset", "All settings have been reset to default values.");
    }

    @FXML
    private void handleSaveSettings() {
        try {
            String outputLocation = defaultOutputLocationField.getText();
            if (!outputLocation.isEmpty()) {
                settingsManager.setDefaultOutputLocation(outputLocation);
            }
            
            String namingConvention = fileNamingConventionBox.getValue();
            if (namingConvention != null) {
                settingsManager.setFileNamingConvention(namingConvention);
            }
            
            settingsManager.setOverwriteExistingFiles(overwriteExistingFilesCheckbox.isSelected());
        
            settingsManager.saveSettings();
            
            settingsManager.reloadSettings();
            loadSettings();
            
            showInformation("Settings Saved", "Your settings have been saved successfully.");
            closeWindow();
            
        } catch (Exception e) {
            showError("Save Error", "Could not save settings: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelSettings() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
