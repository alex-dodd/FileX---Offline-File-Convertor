package ui.controllers;

import database.ConversionHistoryDAO;
import handlers.FileConversionHandler;
import handlers.ZipHandler;
import models.ConversionRecord;
import utils.SettingsManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.fxml.Initializable;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainUIController implements Initializable {

    @FXML private TabPane mainTabPane;
    
    @FXML private TextField sourceFileField;
    @FXML private ComboBox<String> targetFormatBox;
    @FXML private TextField outputLocationField;
    @FXML private ProgressBar conversionProgressBar;
    @FXML private Label statusLabel;
    
    @FXML private TextField sourceFolderField;
    @FXML private TextField zipOutputLocationField;
    @FXML private CheckBox encryptCheckbox;
    @FXML private VBox passwordBox;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordCheckbox;
    @FXML private ProgressBar zipProgressBar;
    @FXML private Label zipStatusLabel;

    @FXML private Button browseFileButton;
    @FXML private Button browseOutputButton;
    @FXML private Button convertFileButton;
    @FXML private Button browseFolderButton;
    @FXML private Button browseZipOutputButton;
    @FXML private Button convertToZipButton;

    private final FileConversionHandler conversionHandler = new FileConversionHandler();
    private final ZipHandler zipHandler = new ZipHandler();
    private final ConversionHistoryDAO historyDAO = new ConversionHistoryDAO();
    private final SettingsManager settingsManager = SettingsManager.getInstance();
    
    private final ObservableList<String> allFormats = FXCollections.observableArrayList(
        "PDF", "DOCX", "XLSX", "CSV", "JPG", "PNG", "WEBP"
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTabPane();
        setupFileConverter();
        setupZipConverter();
        setupPasswordToggle();
        setupTooltips();
    }

    private void setupTabPane() {
        if (mainTabPane != null) {
            mainTabPane.setVisible(true);
            mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            
            mainTabPane.getSelectionModel().select(0);
            
            mainTabPane.getSelectionModel().selectedIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            });
        }
    }

    private void setupFileConverter() {
        targetFormatBox.setItems(allFormats);
        statusLabel.setVisible(false);
        conversionProgressBar.setVisible(false);

        sourceFileField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.isEmpty()) {
                updateTargetFormatOptions(new File(newText));
                statusLabel.setVisible(false);
            } else {
                targetFormatBox.setItems(allFormats);
                targetFormatBox.getSelectionModel().clearSelection();
            }
        });
    }

    private void setupZipConverter() {
        zipStatusLabel.setVisible(false);
        zipProgressBar.setVisible(false);
        passwordBox.setVisible(false);
        passwordBox.setManaged(false);

        encryptCheckbox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            passwordBox.setVisible(isNowSelected);
            passwordBox.setManaged(isNowSelected);
            if (!isNowSelected) {
                passwordField.clear();
                passwordTextField.clear();
            }
        });
    }

    private void setupPasswordToggle() {
        passwordTextField.managedProperty().bind(showPasswordCheckbox.selectedProperty());
        passwordTextField.visibleProperty().bind(showPasswordCheckbox.selectedProperty());
        passwordField.managedProperty().bind(showPasswordCheckbox.selectedProperty().not());
        passwordField.visibleProperty().bind(showPasswordCheckbox.selectedProperty().not());
        
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    private void updateTargetFormatOptions(File sourceFile) {
        if (!sourceFile.exists()) {
            targetFormatBox.setItems(allFormats);
            return;
        }

        String fileName = sourceFile.getName().toLowerCase();
        ObservableList<String> supportedFormats = FXCollections.observableArrayList();

        if (fileName.endsWith(".pdf")) {
            supportedFormats.addAll(Arrays.asList("DOCX"));
        } else if (fileName.endsWith(".docx")) {
            supportedFormats.addAll(Arrays.asList("PDF"));
        } else if (fileName.endsWith(".csv")) {
            supportedFormats.addAll(Arrays.asList("XLSX"));
        } else if (fileName.endsWith(".xlsx")) {
            supportedFormats.addAll(Arrays.asList("CSV"));
        } else if (fileName.endsWith(".webp")) {
            supportedFormats.addAll(Arrays.asList("JPG", "PNG"));
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            supportedFormats.addAll(Arrays.asList("PNG", "WEBP"));
        } else if (fileName.endsWith(".png")) {
            supportedFormats.addAll(Arrays.asList("JPG", "WEBP"));
        } else {
            supportedFormats.addAll(allFormats);
        }

        targetFormatBox.setItems(supportedFormats);
        targetFormatBox.getSelectionModel().clearSelection();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateTargetFormats(File sourceFile) {
        String fileName = sourceFile.getName().toLowerCase();
        ObservableList<String> formats = FXCollections.observableArrayList();
        
        if (fileName.endsWith(".docx")) {
            formats.add("PDF");
        } else if (fileName.endsWith(".pdf")) {
            formats.add("DOCX");
        } else if (fileName.endsWith(".csv")) {
            formats.add("XLSX");
        } else if (fileName.endsWith(".xlsx")) {
            formats.add("CSV");
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            formats.addAll(Arrays.asList("PNG", "WEBP"));
        } else if (fileName.endsWith(".png")) {
            formats.addAll(Arrays.asList("JPG", "WEBP"));
        } else if (fileName.endsWith(".webp")) {
            formats.addAll(Arrays.asList("JPG", "PNG"));
        }
        
        targetFormatBox.setItems(formats);
        if (!formats.isEmpty()) {
            targetFormatBox.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleBrowseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Convert");
        
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Supported", "*.docx", "*.pdf", "*.csv", "*.xlsx", "*.jpg", "*.jpeg", "*.png", "*.webp"),
            new FileChooser.ExtensionFilter("Document Files", "*.docx", "*.pdf"),
            new FileChooser.ExtensionFilter("Spreadsheet Files", "*.csv", "*.xlsx"),
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.webp"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        String lastLocation = settingsManager.getLastFileLocation();
        if (lastLocation != null && !lastLocation.isEmpty()) {
            File lastDir = new File(lastLocation);
            if (lastDir.exists() && lastDir.isDirectory()) {
                fileChooser.setInitialDirectory(lastDir);
            }
        }
        
        File selectedFile = fileChooser.showOpenDialog(sourceFileField.getScene().getWindow());
        if (selectedFile != null) {
            sourceFileField.setText(selectedFile.getAbsolutePath());
            updateTargetFormats(selectedFile);
            settingsManager.setLastFileLocation(selectedFile.getParent());
        }
    }

    @FXML
    private void handleBrowseOutputLocation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Location");
        
        String lastLocation = settingsManager.getLastOutputLocation();
        if (lastLocation != null && !lastLocation.isEmpty()) {
            File lastDir = new File(lastLocation);
            if (lastDir.exists() && lastDir.isDirectory()) {
                directoryChooser.setInitialDirectory(lastDir);
            }
        }
        
        File selectedDirectory = directoryChooser.showDialog(outputLocationField.getScene().getWindow());
        if (selectedDirectory != null) {
            outputLocationField.setText(selectedDirectory.getAbsolutePath());
            settingsManager.setLastOutputLocation(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleConvertFile() {
        String sourceFile = sourceFileField.getText().trim();
        String targetFormat = targetFormatBox.getSelectionModel().getSelectedItem();
        String outputLocation = outputLocationField.getText().trim();
        
        if (sourceFile.isEmpty()) {
            showError("Input Required", "Please select a source file.");
            return;
        }
        
        if (targetFormat == null || targetFormat.isEmpty()) {
            showError("Input Required", "Please select a target format.");
            return;
        }
        
        conversionProgressBar.setVisible(true);
        statusLabel.setText("Converting...");
        
        Task<Void> conversionTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                File sourceFileObj = new File(sourceFile);
                
                String outputPath;
                if (!outputLocation.isEmpty()) {
                    outputPath = outputLocation;
                } else {
                    String defaultOutput = settingsManager.getDefaultOutputLocation();
                    outputPath = (defaultOutput != null && !defaultOutput.isEmpty()) 
                        ? defaultOutput 
                        : sourceFileObj.getParent();
                }
                
                String fileName = sourceFileObj.getName();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                String targetExtension = "." + targetFormat.toLowerCase();
                String finalFileName = settingsManager.applyFileNamingConvention(baseName, targetExtension);
                File targetFileObj = new File(outputPath, finalFileName);
                
                if (targetFileObj.exists() && !confirmOverwrite(targetFileObj)) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Conversion cancelled by user.");
                        conversionProgressBar.setVisible(false);
                    });
                    return null;
                }
                
                conversionHandler.convertFile(sourceFileObj, targetFileObj, targetFormat);
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    conversionProgressBar.setVisible(false);
                    statusLabel.setText("Conversion completed successfully!");
                    
                    if (settingsManager.getLogSuccessfulConversions()) {
                        String outputPath = outputLocation.isEmpty() ? new File(sourceFile).getParent() : outputLocation;
                        String fileName = new File(sourceFile).getName();
                        String sourceFormat = fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();
                        historyDAO.addConversionRecord(new ConversionRecord(
                            sourceFile, outputPath, sourceFormat, targetFormat, true
                        ));
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    conversionProgressBar.setVisible(false);
                    statusLabel.setText("Conversion failed");
                    showError("Conversion Failed", getException().getMessage());
                });
            }
        };
        
        new Thread(conversionTask).start();
    }

    @FXML
    private void handleBrowseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Convert to ZIP");
        
        String lastLocation = settingsManager.getLastFileLocation();
        if (lastLocation != null && !lastLocation.isEmpty()) {
            File lastDir = new File(lastLocation);
            if (lastDir.exists() && lastDir.isDirectory()) {
                directoryChooser.setInitialDirectory(lastDir);
            }
        }
        
        File selectedDirectory = directoryChooser.showDialog(sourceFolderField.getScene().getWindow());
        if (selectedDirectory != null) {
            sourceFolderField.setText(selectedDirectory.getAbsolutePath());
            settingsManager.setLastFileLocation(selectedDirectory.getParent());
        }
    }

    @FXML
    private void handleEncryptionToggle() {
        boolean isSelected = encryptCheckbox.isSelected();
        passwordBox.setVisible(isSelected);
        passwordBox.setManaged(isSelected);
    }

    @FXML
    private void handleConvertToZip() {
        String sourceFolder = sourceFolderField.getText().trim();
        String outputLocation = zipOutputLocationField.getText().trim();
        boolean encrypt = encryptCheckbox.isSelected();
        String password = encrypt ? passwordField.getText() : null;
        
        if (sourceFolder.isEmpty()) {
            showError("Input Required", "Please select a source folder.");
            return;
        }
        
        if (encrypt && (password == null || password.trim().isEmpty())) {
            showError("Input Required", "Please enter a password for encryption.");
            return;
        }
        
        zipProgressBar.setVisible(true);
        zipStatusLabel.setText("Creating ZIP archive...");
        
        Task<Void> zipTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                File sourceFile = new File(sourceFolder);
                
                String outputDir;
                if (!outputLocation.isEmpty()) {
                    outputDir = outputLocation;
                } else {
                    String defaultOutput = settingsManager.getDefaultOutputLocation();
                    outputDir = (defaultOutput != null && !defaultOutput.isEmpty()) 
                        ? defaultOutput 
                        : sourceFile.getParent();
                }
                
                File zipFile = new File(outputDir, sourceFile.getName() + ".zip");
                
                if (zipFile.exists() && !confirmOverwrite(zipFile)) {
                    Platform.runLater(() -> {
                        zipStatusLabel.setText("ZIP creation cancelled by user.");
                        zipProgressBar.setVisible(false);
                    });
                    return null;
                }
                
                zipHandler.createZip(sourceFile, zipFile, password);
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    zipProgressBar.setVisible(false);
                    zipStatusLabel.setText("ZIP archive created successfully!");
                    
                    if (settingsManager.getLogSuccessfulConversions()) {
                        String outputPath = outputLocation.isEmpty() ? new File(sourceFolder).getParent() : outputLocation;
                        historyDAO.addConversionRecord(new ConversionRecord(
                            sourceFolder, outputPath, "FOLDER", "ZIP", true
                        ));
                    }
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    zipProgressBar.setVisible(false);
                    zipStatusLabel.setText("ZIP creation failed");
                    showError("ZIP Creation Failed", getException().getMessage());
                });
            }
        };
        
        new Thread(zipTask).start();
    }

    @FXML
    private void handleOpenFile() {
        mainTabPane.getSelectionModel().select(0);
        handleBrowseFile();
    }

    @FXML
    private void handleOpenFolder() {
        mainTabPane.getSelectionModel().select(1);
        handleBrowseFolder();
    }

    @FXML
    private void handleNewConversion() {
        sourceFileField.clear();
        targetFormatBox.setValue(null);
        outputLocationField.clear();
        conversionProgressBar.setVisible(false);
        statusLabel.setText("Ready for conversion");
        
        sourceFolderField.clear();
        zipOutputLocationField.clear();
        encryptCheckbox.setSelected(false);
        passwordField.clear();
        showPasswordCheckbox.setSelected(false);
        zipProgressBar.setVisible(false);
        zipStatusLabel.setText("Ready to create ZIP archive");
        
        mainTabPane.getSelectionModel().select(0);
    }

    @FXML
    private void handleShowHistory() {
        openWindow("/ui/HistoryUI.fxml", "Conversion History", 1200, 750);
    }

    @FXML
    private void handleShowSettings() {
        openWindow("/ui/SettingsUI.fxml", "Settings", 700, 600);
    }

    @FXML
    private void handleShowDocumentation() {
        openWindow("/ui/DocumentationUI.fxml", "User Documentation", 900, 700);
    }

    @FXML
    private void handleShowAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About FileX");
        alert.setHeaderText("FileX School PAT v0.1.4");
        alert.setContentText("Created by Alex Dodd");
        alert.showAndWait();
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleShowPasswordToggle() {
        if (showPasswordCheckbox.isSelected()) {
            passwordField.setPromptText(passwordField.getText());
        } else {
            passwordField.setPromptText("Enter password");
        }
    }

    @FXML
    private void handleBrowseZipOutputLocation() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select ZIP Output Location");
        
        if (!zipOutputLocationField.getText().isEmpty()) {
            File initialDir = new File(zipOutputLocationField.getText());
            if (initialDir.exists() && initialDir.isDirectory()) {
                directoryChooser.setInitialDirectory(initialDir);
            }
        }
        
        Stage stage = (Stage) zipOutputLocationField.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            zipOutputLocationField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleClearFileConverter() {
        sourceFileField.clear();
        targetFormatBox.getSelectionModel().clearSelection();
        outputLocationField.clear();
        statusLabel.setText("");
        statusLabel.setVisible(false);
        conversionProgressBar.setVisible(false);
    }

    @FXML
    private void handleClearZipConverter() {
        sourceFolderField.clear();
        zipOutputLocationField.clear();
        encryptCheckbox.setSelected(false);
        passwordField.clear();
        passwordTextField.clear();
        showPasswordCheckbox.setSelected(false);
        zipStatusLabel.setText("");
        zipStatusLabel.setVisible(false);
        zipProgressBar.setVisible(false);
    }

    @FXML
    private void handleViewHistory() {
        openWindow("/ui/HistoryUI.fxml", "Conversion History", 800, 600);
    }

    @FXML
    private String determineOutputPath(File sourceFile, String targetFormat, String outputLocation) {
        String baseFileName = sourceFile.getName().replaceFirst("[.][^.]+$", "");
        String targetExtension = "." + targetFormat.toLowerCase();
        String fileName = settingsManager.applyFileNamingConvention(baseFileName, targetExtension);
        
        if (outputLocation.isEmpty()) {
            return sourceFile.getParent() + File.separator + fileName;
        } else {
            return outputLocation + File.separator + fileName;
        }
    }

    private boolean confirmOverwrite(File file) {
        if (settingsManager.getOverwriteExistingFiles()) {
            return true;
        }
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean userResponse = new AtomicBoolean(false);
        
        Platform.runLater(() -> {
            try {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("File Exists");
                confirmAlert.setHeaderText("Overwrite Existing File?");
                confirmAlert.setContentText("The file '" + file.getName() + "' already exists. Do you want to overwrite it?");
                
                Optional<ButtonType> result = confirmAlert.showAndWait();
                userResponse.set(result.isPresent() && result.get() == ButtonType.OK);
            } finally {
                latch.countDown();
            }
        });
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        
        return userResponse.get();
    }

    private void performFileConversion(File sourceFile, File targetFile, String targetFormat) {
        Task<Boolean> conversionTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateProgress(0, 1);
                return conversionHandler.convertFile(sourceFile, targetFile, targetFormat);
            }

            @Override
            protected void succeeded() {
                conversionProgressBar.setVisible(false);
                if (getValue()) {
                    statusLabel.setText("✓ Conversion completed successfully!");
                    statusLabel.getStyleClass().clear();
                    statusLabel.getStyleClass().add("success-text");
                    
                    historyDAO.addConversionRecord(new ConversionRecord(
                        sourceFile.getAbsolutePath(),
                        targetFile.getAbsolutePath(),
                        getSourceFormat(sourceFile),
                        targetFormat,
                        true,
                        System.currentTimeMillis()
                    ));
                } else {
                    statusLabel.setText("✗ Conversion failed. Please check the file format.");
                    statusLabel.getStyleClass().clear();
                    statusLabel.getStyleClass().add("error-text");
                }
                statusLabel.setVisible(true);
            }

            @Override
            protected void failed() {
                conversionProgressBar.setVisible(false);
                statusLabel.setText("✗ Conversion failed: " + getException().getMessage());
                statusLabel.getStyleClass().clear();
                statusLabel.getStyleClass().add("error-text");
                statusLabel.setVisible(true);
            }
        };

        conversionProgressBar.progressProperty().bind(conversionTask.progressProperty());
        conversionProgressBar.setVisible(true);
        statusLabel.setText("Converting file...");
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add("field-label");
        statusLabel.setVisible(true);

        new Thread(conversionTask).start();
    }

    private void performZipCreation(File sourceFolder, File zipFile, String password) {
        Task<Boolean> zipTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateProgress(0, 1);
                return zipHandler.createZip(sourceFolder, zipFile, password);
            }

            @Override
            protected void succeeded() {
                zipProgressBar.setVisible(false);
                if (getValue()) {
                    zipStatusLabel.setText("✓ ZIP archive created successfully!");
                    zipStatusLabel.getStyleClass().clear();
                    zipStatusLabel.getStyleClass().add("success-text");
                    
                    historyDAO.addConversionRecord(new ConversionRecord(
                        sourceFolder.getAbsolutePath(),
                        zipFile.getAbsolutePath(),
                        "FOLDER",
                        "ZIP",
                        true,
                        System.currentTimeMillis()
                    ));
                } else {
                    zipStatusLabel.setText("✗ ZIP creation failed. Please check folder permissions.");
                    zipStatusLabel.getStyleClass().clear();
                    zipStatusLabel.getStyleClass().add("error-text");
                }
                zipStatusLabel.setVisible(true);
            }

            @Override
            protected void failed() {
                zipProgressBar.setVisible(false);
                zipStatusLabel.setText("✗ ZIP creation failed: " + getException().getMessage());
                zipStatusLabel.getStyleClass().clear();
                zipStatusLabel.getStyleClass().add("error-text");
                zipStatusLabel.setVisible(true);
            }
        };

        zipProgressBar.progressProperty().bind(zipTask.progressProperty());
        zipProgressBar.setVisible(true);
        zipStatusLabel.setText("Creating ZIP archive...");
        zipStatusLabel.getStyleClass().clear();
        zipStatusLabel.getStyleClass().add("field-label");
        zipStatusLabel.setVisible(true);

        new Thread(zipTask).start();
    }

    private String getSourceFormat(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf")) return "PDF";
        if (name.endsWith(".docx")) return "DOCX";
        if (name.endsWith(".xlsx")) return "XLSX";
        if (name.endsWith(".csv")) return "CSV";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "JPG";
        if (name.endsWith(".png")) return "PNG";
        if (name.endsWith(".webp")) return "WEBP";
        return "UNKNOWN";
    }

    private void openWindow(String fxmlPath, String title, int width, int height) {
        try {
            java.net.URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                showErrorAlert("Error", "FXML file not found: " + fxmlPath);
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, width, height));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("IO Error", "Could not load " + title + " window: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Runtime Error", "Could not open " + title + " window: " + e.getMessage());
        }
    }

    private Stage getStage() {
        return (Stage) mainTabPane.getScene().getWindow();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInformationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupTooltips() {
        sourceFileField.setTooltip(new Tooltip("Select the file you want to convert"));
        targetFormatBox.setTooltip(new Tooltip("Choose the format to convert your file to"));
        outputLocationField.setTooltip(new Tooltip("Choose where to save the converted file\n(leave empty to save in same folder as source)"));
        conversionProgressBar.setTooltip(new Tooltip("Conversion progress indicator"));
        statusLabel.setTooltip(new Tooltip("Current conversion status"));
        
        if (browseFileButton != null) {
            browseFileButton.setTooltip(new Tooltip("Click to select a file for conversion"));
        }
        if (browseOutputButton != null) {
            browseOutputButton.setTooltip(new Tooltip("Click to choose where to save the converted file"));
        }
        if (convertFileButton != null) {
            convertFileButton.setTooltip(new Tooltip("Start the file conversion process"));
        }
        
        sourceFolderField.setTooltip(new Tooltip("Select the folder you want to compress into a ZIP file"));
        zipOutputLocationField.setTooltip(new Tooltip("Choose where to save the ZIP file"));
        encryptCheckbox.setTooltip(new Tooltip("Enable password protection for your ZIP file"));
        passwordField.setTooltip(new Tooltip("Enter a password to protect your ZIP file"));
        passwordTextField.setTooltip(new Tooltip("Enter a password to protect your ZIP file"));
        showPasswordCheckbox.setTooltip(new Tooltip("Toggle password visibility"));
        zipProgressBar.setTooltip(new Tooltip("ZIP creation progress indicator"));
        zipStatusLabel.setTooltip(new Tooltip("Current ZIP creation status"));
        
        if (browseFolderButton != null) {
            browseFolderButton.setTooltip(new Tooltip("Click to select a folder to compress"));
        }
        if (browseZipOutputButton != null) {
            browseZipOutputButton.setTooltip(new Tooltip("Click to choose where to save the ZIP file"));
        }
        if (convertToZipButton != null) {
            convertToZipButton.setTooltip(new Tooltip("Create a ZIP archive from the selected folder"));
        }
        
        mainTabPane.setTooltip(new Tooltip("Switch between File Converter and ZIP Creator"));
    }
}