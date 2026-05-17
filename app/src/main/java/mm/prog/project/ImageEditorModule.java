

import model.SharedData;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class ImageEditorModule implements SharedData.ImageChangeListener {
    private BorderPane layout;
    private Image_Editing imageProcessor; 
    private ImageView imageView;
    private Label statusLabel;
    private VBox controlPanel;
    
    // UI Controls
    private Slider brightnessSlider;
    private Slider contrastSlider;
    private Slider borderSlider;
    private CheckBox grayscaleCheckbox;
    private ColorPicker borderColorPicker;
    private Slider blurSlider;

    public ImageEditorModule() {
        imageProcessor = new Image_Editing();
        createUI();
        
        // Register this workspace to listen to global image selection updates
        SharedData.registerListener(this);
        
        // Initial load check if an image is already selected in the hub
        if (SharedData.selectedImagePath != null && !SharedData.selectedImagePath.isEmpty()) {
            onImageChanged(SharedData.selectedImagePath);
        }
    }

    private void createUI() {
        layout = new BorderPane();
        layout.setStyle("-fx-background-color: #121212;");

        // Top Header: Visual Title Bar only (Load button completely removed)
        VBox header = createHeader();
        layout.setTop(header);

        // Center Area: Main Workspace Display Viewport
        VBox imageArea = createImageArea();
        layout.setCenter(imageArea);

        // Right Control Strip: Transformation & Enhancement tools
        controlPanel = createControlPanel();
        layout.setRight(controlPanel);

        // Bottom Strip: Status Feedback Messaging Unit
        HBox statusBar = new HBox();
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: #1e1e1e;");
        statusLabel = new Label("No workspace image active. Choose an image from 'Load Image Hub'.");
        statusLabel.setStyle("-fx-text-fill: #aaaaaa;");
        statusBar.getChildren().add(statusLabel);
        layout.setBottom(statusBar);

        // Disable operational controls initially until a target image is populated
        enableControls(false);
    }

    private VBox createHeader() {
        VBox headerBox = new VBox(5);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: #1c1c1c; -fx-border-color: #2c2c2c; -fx-border-width: 0 0 1 0;");
        
        Label titleLabel = new Label("🎨 Digital Image Processing (DIP) Workspace");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label infoLabel = new Label("Enhancements apply dynamically to the image loaded via the central Hub.");
        infoLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        
        headerBox.getChildren().addAll(titleLabel, infoLabel);
        return headerBox;
    }

    private VBox createImageArea() {
        VBox area = new VBox();
        area.setAlignment(Pos.CENTER);
        area.setPadding(new Insets(20));
        
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(650);
        imageView.setFitHeight(450);
        
        area.getChildren().add(imageView);
        return area;
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(260);
        panel.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #2c2c2c; -fx-border-width: 0 0 0 1;");

        Label toolsTitle = new Label("Processing Filters");
        toolsTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        panel.getChildren().add(toolsTitle);

        // Brightness Slider
        brightnessSlider = new Slider(-255, 255, 0);
        addSliderControl(panel, "Brightness", brightnessSlider);
        brightnessSlider.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Contrast Slider
        contrastSlider = new Slider(0.1, 3.0, 1.0);
        addSliderControl(panel, "Contrast", contrastSlider);
        contrastSlider.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Blur Slider
        blurSlider = new Slider(1, 25, 1);
        blurSlider.setBlockIncrement(2);
        addSliderControl(panel, "Gaussian Blur", blurSlider);
        blurSlider.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Grayscale Option
        grayscaleCheckbox = new CheckBox("Convert to Grayscale");
        grayscaleCheckbox.setStyle("-fx-text-fill: white;");
        grayscaleCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        panel.getChildren().add(grayscaleCheckbox);

        // Border Customization Layout
        VBox borderSection = new VBox(5);
        Label borderLabel = new Label("Outer Border Thickness:");
        borderLabel.setStyle("-fx-text-fill: #ccc;");
        borderSlider = new Slider(0, 50, 0);
        borderColorPicker = new ColorPicker(javafx.scene.paint.Color.BLACK);
        
        borderSlider.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        borderColorPicker.setOnAction(e -> applyFilters());
        
        borderSection.getChildren().addAll(borderLabel, borderSlider, borderColorPicker);
        panel.getChildren().add(borderSection);

        // Save Artifact Action Button
        Button btnSave = new Button("💾 Export Processed Image");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSave.setOnAction(e -> saveImage());
        panel.getChildren().add(btnSave);

        return panel;
    }

    private void addSliderControl(VBox parent, String labelText, Slider slider) {
        VBox container = new VBox(5);
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #cccccc;");
        slider.setShowTickMarks(false);
        slider.setShowTickLabels(false);
        container.getChildren().addAll(label, slider);
        parent.getChildren().add(container);
    }

    /**
     * Intercepts global context modifications emitted by the Repository Hub
     */
    @Override
    public void onImageChanged(String newPath) {
        if (newPath != null && !newPath.isEmpty()) {
            boolean loaded = imageProcessor.loadImage(newPath);
            if (loaded) {
                imageView.setImage(imageProcessor.getOriginalImage());
                statusLabel.setText("Active Core Workspace: " + new File(newPath).getName());
                resetControls();
                enableControls(true);
            } else {
                statusLabel.setText("Failed to process asset context via OpenCV matrix definitions.");
                enableControls(false);
            }
        }
    }

    private void applyFilters() {
        if (imageProcessor.isImageLoaded()) {
            // Read target parameters
            double brightness = brightnessSlider.getValue();
            double contrast = contrastSlider.getValue();
            int blurRadius = (int) blurSlider.getValue();
            if (blurRadius % 2 == 0) blurRadius++; // Must remain odd for Gaussian matrices
            
            boolean grayscale = grayscaleCheckbox.isSelected();
            int borderSize = (int) borderSlider.getValue();
            javafx.scene.paint.Color fxColor = borderColorPicker.getValue();

            // Execute matrix conversions pipeline via your Image_Editing.java class file
            imageProcessor.applySettings(brightness, contrast, grayscale, blurRadius, borderSize, fxColor);
            
            // Render back up to screen
            imageView.setImage(imageProcessor.getEditedImage());
        }
    }

    private void saveImage() {
        if (imageProcessor.isImageLoaded()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image Changes");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("PNG Image Files (*.png)", "*.png"));
            
            File file = fileChooser.showSaveDialog(layout.getScene().getWindow());
            if (file != null) {
                boolean success = imageProcessor.saveImage(file.getAbsolutePath());
                statusLabel.setText(success ? "Changes saved successfully!" : "Error encountered processing file stream save.");
            }
        }
    }

    private void enableControls(boolean enable) {
        brightnessSlider.setDisable(!enable);
        contrastSlider.setDisable(!enable);
        borderSlider.setDisable(!enable);
        grayscaleCheckbox.setDisable(!enable);
        borderColorPicker.setDisable(!enable);
        blurSlider.setDisable(!enable);
    }

    private void resetControls() {
        brightnessSlider.setValue(0);
        contrastSlider.setValue(1.0);
        borderSlider.setValue(0);
        blurSlider.setValue(1);
        grayscaleCheckbox.setSelected(false);
        borderColorPicker.setValue(javafx.scene.paint.Color.BLACK);
    }

    public BorderPane getLayout() {
        return layout;
    }

    public void dispose() {
        if (imageProcessor != null) {
            imageProcessor.dispose();
        }
    }
}
