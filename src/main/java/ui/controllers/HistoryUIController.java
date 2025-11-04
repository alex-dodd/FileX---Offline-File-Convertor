package ui.controllers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import database.ConversionHistoryDAO;
import models.ConversionRecord;
import utils.SettingsManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class HistoryUIController {

    @FXML private TableView<ConversionRecord> historyTable;
    @FXML private TableColumn<ConversionRecord, Integer> idColumn;
    @FXML private TableColumn<ConversionRecord, String> sourcePathColumn;
    @FXML private TableColumn<ConversionRecord, String> targetPathColumn;
    @FXML private TableColumn<ConversionRecord, String> sourceFormatColumn;
    @FXML private TableColumn<ConversionRecord, String> targetFormatColumn;
    @FXML private TableColumn<ConversionRecord, Boolean> successColumn;
    @FXML private TableColumn<ConversionRecord, LocalDateTime> timestampColumn;
    @FXML private TextField filterField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> timeFilter;
    @FXML private ComboBox<String> formatFilter;
    
    @FXML private Button clearFilterButton;
    @FXML private Button resetFiltersButton;
    @FXML private Button clearHistoryButton;

    private final ConversionHistoryDAO historyDAO = new ConversionHistoryDAO();
    private final SettingsManager settingsManager = SettingsManager.getInstance();
    private final ObservableList<ConversionRecord> masterData = FXCollections.observableArrayList();
    private FilteredList<ConversionRecord> filteredData;

    @FXML
    private void initialize() {
        setupTableColumns();
        setupComboBoxes();
        loadData();
        setupFiltering();
        setupDoubleClickBehavior();
        setupTooltips();
        setupKeyboardShortcuts();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        sourcePathColumn.setCellValueFactory(new PropertyValueFactory<>("sourcePath"));
        targetPathColumn.setCellValueFactory(new PropertyValueFactory<>("targetPath"));
        sourceFormatColumn.setCellValueFactory(new PropertyValueFactory<>("sourceFormat"));
        targetFormatColumn.setCellValueFactory(new PropertyValueFactory<>("targetFormat"));
        successColumn.setCellValueFactory(new PropertyValueFactory<>("success"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        
        successColumn.setCellFactory(column -> new TableCell<ConversionRecord, Boolean>() {
            @Override
            protected void updateItem(Boolean success, boolean empty) {
                super.updateItem(success, empty);
                if (empty || success == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(success ? "Success" : "Failed");
                    setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
    }
    
    private void setupComboBoxes() {
        statusFilter.setItems(FXCollections.observableArrayList(
            "All", "Completed", "Failed"
        ));
        statusFilter.setValue("All");
        
        timeFilter.setItems(FXCollections.observableArrayList(
            "All Time", "Today", "This Week", "This Month", "Last 30 Days"
        ));
        timeFilter.setValue("All Time");
        
        formatFilter.setValue("All Formats");
        
        statusFilter.setOnAction(e -> applyFilters());
        timeFilter.setOnAction(e -> applyFilters());
        formatFilter.setOnAction(e -> applyFilters());
    }
    
    private void loadData() {
        masterData.clear();
        masterData.addAll(historyDAO.getAllRecords());
        updateFormatFilter();
    }
    
    private void updateFormatFilter() {
        Set<String> formats = new HashSet<>();
        for (ConversionRecord record : masterData) {
            formats.add(record.getSourceFormat());
            formats.add(record.getTargetFormat());
        }
        
        ObservableList<String> formatOptions = FXCollections.observableArrayList("All Formats");
        formatOptions.addAll(formats);
        formatFilter.setItems(formatOptions);
        formatFilter.setValue("All Formats");
    }
    
    private void setupFiltering() {
        filteredData = new FilteredList<>(masterData, p -> true);
        
        filterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        
        SortedList<ConversionRecord> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(historyTable.comparatorProperty());
        historyTable.setItems(sortedData);
    }
    
    private void setupDoubleClickBehavior() {
        historyTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                ConversionRecord selectedRecord = historyTable.getSelectionModel().getSelectedItem();
                if (selectedRecord != null) {
                    showConversionDetails(selectedRecord);
                }
            }
        });
    }
    
    private void applyFilters() {
        filteredData.setPredicate(createFilterPredicate());
    }
    
    private Predicate<ConversionRecord> createFilterPredicate() {
        return record -> {
            // Thr text search filter
            String searchText = filterField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                boolean textMatch = record.getSourcePath().toLowerCase().contains(lowerCaseFilter) || record.getTargetPath().toLowerCase().contains(lowerCaseFilter) || record.getSourceFormat().toLowerCase().contains(lowerCaseFilter) || record.getTargetFormat().toLowerCase().contains(lowerCaseFilter);
                if (!textMatch) return false;
            }
            
            // The tatus filter
            String statusValue = statusFilter.getValue();
            if (statusValue != null && !statusValue.equals("All")) {
                boolean isSuccess = record.isSuccess();
                if (statusValue.equals("Completed") && !isSuccess) return false;
                if (statusValue.equals("Failed") && isSuccess) return false;
            }
            
            // YHr time filter
            String timeValue = timeFilter.getValue();
            if (timeValue != null && !timeValue.equals("All Time")) {
                LocalDateTime recordTime = record.getTimestamp();
                LocalDateTime now = LocalDateTime.now();
                
                switch (timeValue) {
                    case "Today":
                        if (recordTime.toLocalDate().isBefore(now.toLocalDate())) return false;
                        break;
                    case "This Week":
                        LocalDateTime weekStart = now.minus(7, ChronoUnit.DAYS);
                        if (recordTime.isBefore(weekStart)) return false;
                        break;
                    case "This Month":
                        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                        if (recordTime.isBefore(monthStart)) return false;
                        break;
                    case "Last 30 Days":
                        LocalDateTime thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
                        if (recordTime.isBefore(thirtyDaysAgo)) return false;
                        break;
                }
            }
            
            //The gormat filter
            String formatValue = formatFilter.getValue();
            if (formatValue != null && !formatValue.equals("All Formats")) {
                boolean formatMatch = record.getSourceFormat().equals(formatValue) ||
                                    record.getTargetFormat().equals(formatValue);
                if (!formatMatch) return false;
            }
            
            return true;
        };
    }

    @FXML
    private void handleClearFilter() {
        filterField.setText("");
    }
    
    @FXML
    private void handleResetFilters() {
        filterField.setText("");
        statusFilter.setValue("All");
        timeFilter.setValue("All Time");
        formatFilter.setValue("All Formats");
    }

    @FXML
    private void handleClearHistory() {
        boolean proceed = true;
        if (settingsManager.getShowConfirmationDialogs()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Clear History");
            alert.setHeaderText("Clear All Conversion History");
            alert.setContentText("Are you sure you want to clear all conversion history? This action cannot be undone.");

            ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
            if (result != ButtonType.OK) {
                proceed = false;
            }
        }

        if (proceed) {
            historyDAO.clearAllRecords();
            refreshTable();
            showAlert("History Cleared", "All conversion history has been cleared.");
        }
    }

    public void refreshTable() {
        loadData();
        applyFilters();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showConversionDetails(ConversionRecord record) {
        Alert detailsAlert = new Alert(Alert.AlertType.INFORMATION);
        detailsAlert.setTitle("Conversion Details");
        detailsAlert.setHeaderText("Conversion Record Information");
        detailsAlert.setResizable(true);

        String details = String.format(
            "ID: %d\n\n" +
            "Source File: %s\n" +
            "Target File: %s\n\n" +
            "Source Format: %s\n" +
            "Target Format: %s\n\n" +
            "Status: %s\n" +
            "Date & Time: %s",
            record.getId(),
            record.getSourcePath(),
            record.getTargetPath(),
            record.getSourceFormat(),
            record.getTargetFormat(),
            record.isSuccess() ? "✅ Success" : "❌ Failed",
            record.getTimestamp().toString()
        );

        detailsAlert.setContentText(details);
        detailsAlert.showAndWait();
    }

    private void setupTooltips() {
        filterField.setTooltip(new Tooltip("Search by filename, format, or path (Ctrl+F)"));
        statusFilter.setTooltip(new Tooltip("Filter by conversion status"));
        timeFilter.setTooltip(new Tooltip("Filter by time period"));
        formatFilter.setTooltip(new Tooltip("Filter by file format"));
        
        if (clearFilterButton != null) {
            clearFilterButton.setTooltip(new Tooltip("Clear search text (Escape)"));
        }
        if (resetFiltersButton != null) {
            resetFiltersButton.setTooltip(new Tooltip("Reset all filters (Ctrl+R)"));
        }
        if (clearHistoryButton != null) {
            clearHistoryButton.setTooltip(new Tooltip("Clear all conversion history (Ctrl+Delete)"));
        }
        
        historyTable.setTooltip(new Tooltip("Double-click any row to view conversion details"));
        
        idColumn.getGraphic();
        Tooltip.install(idColumn.getGraphic(), new Tooltip("Unique conversion ID"));
        sourcePathColumn.getGraphic();
        Tooltip.install(sourcePathColumn.getGraphic(), new Tooltip("Original file location"));
        targetPathColumn.getGraphic();
        Tooltip.install(targetPathColumn.getGraphic(), new Tooltip("Converted file location"));
        sourceFormatColumn.getGraphic();
        Tooltip.install(sourceFormatColumn.getGraphic(), new Tooltip("Original file format"));
        targetFormatColumn.getGraphic();
        Tooltip.install(targetFormatColumn.getGraphic(), new Tooltip("Target file format"));
        successColumn.getGraphic();
        Tooltip.install(successColumn.getGraphic(), new Tooltip("Conversion success status"));
        timestampColumn.getGraphic();
        Tooltip.install(timestampColumn.getGraphic(), new Tooltip("When the conversion was performed"));
    }
    
    private void setupKeyboardShortcuts() {
        historyTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(this::handleKeyPress);
            }
        });
        
        filterField.setOnKeyPressed(this::handleSearchFieldKeyPress);
    }
    
    private void handleKeyPress(KeyEvent event) {
        if (event.isControlDown()) {
            switch (event.getCode()) {
                case F:
                    filterField.requestFocus();
                    event.consume();
                    break;
                case R:
                    handleResetFilters();
                    event.consume();
                    break;
                case DELETE:
                    handleClearHistory();
                    event.consume();
                    break;
                default:
                    break;
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            handleClearFilter();
            event.consume();
        } else if (event.getCode() == KeyCode.F5) {
            refreshTable();
            event.consume();
        }
    }
    
    private void handleSearchFieldKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            handleClearFilter();
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER) {
            historyTable.requestFocus();
            event.consume();
        }
    }
}
