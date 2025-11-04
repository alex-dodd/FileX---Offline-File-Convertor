package ui.controllers;

import models.ConversionRecord;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DetailsUIController {

    @FXML
    private VBox detailsBox;

    public void setRecord(ConversionRecord record) {
        detailsBox.getChildren().clear();
        detailsBox.getChildren().add(new Label("Source Path: " + record.getSourcePath()));
        detailsBox.getChildren().add(new Label("Target Path: " + record.getTargetPath()));
        detailsBox.getChildren().add(new Label("Source Format: " + record.getSourceFormat()));
        detailsBox.getChildren().add(new Label("Target Format: " + record.getTargetFormat()));
        detailsBox.getChildren().add(new Label("Success: " + record.isSuccess()));
        detailsBox.getChildren().add(new Label("Timestamp: " + record.getTimestamp().toString()));
    }
}
