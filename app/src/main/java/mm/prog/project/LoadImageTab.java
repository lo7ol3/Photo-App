package mm.prog.project;

import java.io.File;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class LoadImageTab {
    private VBox layout;
    private Label statusLabel;
    private MainDashboard dashboard;

    // Receives main dashboard reference to find the repository backend safely
    public LoadImageTab(MainDashboard dashboard) {
        this.dashboard = dashboard;
        createUI();
    }

    private void createUI() {
        layout = new VBox(25);
        layout.setPadding(new Insets(40));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #121212;"); // Sleek dark mode

        // Central interaction window widget box
        VBox loaderWidgetBox = new VBox(15);
        loaderWidgetBox.setAlignment(Pos.CENTER);
        loaderWidgetBox.setPadding(new Insets(30));
        loaderWidgetBox.setStyle("-fx-border-color: #444; -fx-border-radius: 8; -fx-background-color: #1e1e1e;");
        loaderWidgetBox.setMaxWidth(450);

        Label lblSection = new Label("Image Ingestion Hub");
        lblSection.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label lblSubText = new Label("Select local image assets to save into your persistent Repository.");
        lblSubText.setStyle("-fx-text-fill: #888; -fx-font-size: 12px; -fx-text-alignment: center;");
        lblSubText.setWrapText(true);

        // Your requested explicitly named button
        Button btnLoadImage = new Button("Load Image");
        btnLoadImage.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 25; -fx-font-size: 14px; -fx-cursor: hand;");

        // Multi-file selection logic mapped directly to your Repository module
        btnLoadImage.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Local Images to Register into Workspace");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Supported Image Formats", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
            );

            // Open native file explorer prompting multiple item selections simultaneously
            List<File> chosenLocalFiles = fileChooser.showOpenMultipleDialog(layout.getScene().getWindow());
            
            if (chosenLocalFiles != null && !chosenLocalFiles.isEmpty()) {
                int importedCounter = 0;
                for (File rawFile : chosenLocalFiles) {
                    String standardPathString = rawFile.getAbsolutePath();
                    
                    // Direct pipeline update inside the Repository module instance layer
                    if (dashboard != null && dashboard.getRepositoryModule() != null) {
                        dashboard.getRepositoryModule().registerNewImage(standardPathString);
                        importedCounter++;
                    }
                }
                statusLabel.setText("Success! " + importedCounter + " image(s) processed and stored inside Repository.");
                statusLabel.setStyle("-fx-text-fill: #28a745;"); // Green for success
            } else {
                statusLabel.setText("Ingestion canceled. No files were loaded.");
                statusLabel.setStyle("-fx-text-fill: #aaa;");
            }
        });

        statusLabel = new Label("Ready to import image assets.");
        statusLabel.setStyle("-fx-text-fill: #888;");
        statusLabel.setWrapText(true);

        loaderWidgetBox.getChildren().addAll(lblSection, lblSubText, btnLoadImage, statusLabel);
        layout.getChildren().add(loaderWidgetBox);
    }

    public VBox getLayout() {
        return this.layout;
    }
}
