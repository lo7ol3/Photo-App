package mm.prog.project;

import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class ImageEditorModule {
    private BorderPane layout;
    private Image_Editing imageProcessor; // Your OpenCV class
    private ImageView imageView;
    private Label statusLabel;
    private VBox controlPanel;
    
    // Control elements
    private Slider brightnessSlider;
    private Slider contrastSlider;
    private Slider borderSlider;
    private CheckBox grayscaleCheckbox;
    private ColorPicker borderColorPicker;
    private Slider blurSlider;

    public ImageEditorModule() {
        imageProcessor = new Image_Editing();
        createUI();
    }

    private void createUI() {
        layout = new BorderPane();
        layout.setStyle("-fx-background-color: #121212;");

        // Top: Header and Load Button
        VBox header = createHeader();
        layout.setTop(header);

        // Center: Image Display Area
        VBox imageArea = createImageArea();
        layout.setCenter(imageArea);

        // Right: Control Panel
        controlPanel = createControlPanel();
        layout.setRight(controlPanel);

        // Bottom: Status Bar
        statusLabel = new Label("Ready - Load an image to start editing");
        statusLabel.setStyle("-fx-text-fill: #aaa; -fx-padding: 10;");
        layout.setBottom(statusLabel);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);

        Label title = new Label("DIP Editor Module");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        Button loadButton = new Button("📁 Load Image");
        loadButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-size: 14px;");
        loadButton.setOnAction(e -> loadImage());

        header.getChildren().addAll(title, loadButton);
        return header;
    }

    private VBox createImageArea() {
        VBox imageArea = new VBox(10);
        imageArea.setAlignment(Pos.CENTER);
        imageArea.setPadding(new Insets(20));

        imageView = new ImageView();
        imageView.setFitWidth(600);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-color: #333; -fx-border-width: 2;");

        // Placeholder
        Label placeholder = new Label("No image loaded\nClick 'Load Image' to start");
        placeholder.setStyle("-fx-text-fill: #666; -fx-font-size: 16px; -fx-text-alignment: center;");
        
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(620, 420);
        imageContainer.setMaxSize(620.,420);
        imageContainer.setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #444; -fx-border-width: 1; ");


        imageContainer.getChildren().addAll(placeholder, imageView);
        
        imageArea.getChildren().add(imageContainer);
        return imageArea;
    }

    private VBox createControlPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(250);
        panel.setStyle("-fx-background-color: #1a1a1a;");

        Label controlTitle = new Label("Image Controls");
        controlTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Basic Adjustments
        VBox basicControls = createBasicControls();
        
        // Advanced Filters
        VBox advancedControls = createAdvancedControls();
        
        // Action Buttons
        VBox actionButtons = createActionButtons();

        panel.getChildren().addAll(controlTitle, 
            new Separator(), basicControls, 
            new Separator(), advancedControls,
            new Separator(), actionButtons);

        return panel;
    }

    private VBox createBasicControls() {
        VBox basic = new VBox(10);
        
        Label basicLabel = new Label("Basic Adjustments");
        basicLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Brightness Slider
        Label brightnessLabel = new Label("Brightness");
        brightnessLabel.setStyle("-fx-text-fill: white;");
        brightnessSlider = new Slider(-100, 100, 0);
        brightnessSlider.setShowTickLabels(true);
        brightnessSlider.setShowTickMarks(true);
        brightnessSlider.setOnMouseReleased(e -> applyBrightness());

        // Contrast Slider
        Label contrastLabel = new Label("Contrast");
        contrastLabel.setStyle("-fx-text-fill: white;");
        contrastSlider = new Slider(0.1, 3.0, 1.0);
        contrastSlider.setShowTickLabels(true);
        contrastSlider.setShowTickMarks(true);
        contrastSlider.setOnMouseReleased(e -> applyContrast());

        // Grayscale Checkbox
        grayscaleCheckbox = new CheckBox("Grayscale");
        grayscaleCheckbox.setStyle("-fx-text-fill: white;");
        grayscaleCheckbox.setOnAction(e -> toggleGrayscale());

        basic.getChildren().addAll(basicLabel, brightnessLabel, brightnessSlider, 
                                   contrastLabel, contrastSlider, grayscaleCheckbox);
        return basic;
    }

    private VBox createAdvancedControls() {
        VBox advanced = new VBox(10);
        
        Label advancedLabel = new Label("Advanced Effects");
        advancedLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Border Controls
        Label borderLabel = new Label("Border Width");
        borderLabel.setStyle("-fx-text-fill: white;");
        borderSlider = new Slider(0, 50, 0);
        borderSlider.setShowTickLabels(true);
        borderSlider.setOnMouseReleased(e -> applyBorder());

        Label colorLabel = new Label("Border Color");
        colorLabel.setStyle("-fx-text-fill: white;");
        borderColorPicker = new ColorPicker(javafx.scene.paint.Color.BLACK);
        borderColorPicker.setOnAction(e -> applyBorderColor());

        // Blur Slider
        Label blurLabel = new Label("Blur Effect");
        blurLabel.setStyle("-fx-text-fill: white;");
        blurSlider = new Slider(1, 21, 1);
        blurSlider.setShowTickLabels(true);
        blurSlider.setOnMouseReleased(e -> applyBlur());

        advanced.getChildren().addAll(advancedLabel, borderLabel, borderSlider,
                                      colorLabel, borderColorPicker, blurLabel, blurSlider);
        return advanced;
    }

    private VBox createActionButtons() {
        VBox actions = new VBox(10);
        
        Label actionLabel = new Label("Actions");
        actionLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button edgeDetectionBtn = new Button("🔍 Edge Detection");
        edgeDetectionBtn.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-pref-width: 200;");
        edgeDetectionBtn.setOnAction(e -> applyEdgeDetection());

        Button histogramBtn = new Button("📊 Histogram Eq.");
        histogramBtn.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-pref-width: 200;");
        histogramBtn.setOnAction(e -> applyHistogramEqualization());

        Button resetBtn = new Button("🔄 Reset to Original");
        resetBtn.setStyle("-fx-background-color: #666; -fx-text-fill: white; -fx-pref-width: 200;");
        resetBtn.setOnAction(e -> resetImage());

        Button saveBtn = new Button("💾 Save Image");
        saveBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-pref-width: 200;");
        saveBtn.setOnAction(e -> saveImage());

        actions.getChildren().addAll(actionLabel, edgeDetectionBtn, histogramBtn, resetBtn, saveBtn);
        return actions;
    }

    // Event Handlers - Connect to your Image_Editing class
    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(layout.getScene().getWindow());
        if (selectedFile != null) {
            boolean success = imageProcessor.loadImage(selectedFile.getAbsolutePath());
            if (success) {
                updateImageDisplay();
                statusLabel.setText("Loaded: " + selectedFile.getName());
                enableControls(true);
            } else {
                statusLabel.setText("Error loading image: " + selectedFile.getName());
            }
        }
    }

    private void updateImageDisplay() {
        if (imageProcessor.isImageLoaded()) {
            javafx.scene.image.Image processedImage = imageProcessor.getOriginalImage();
            if(processedImage != null){
                imageView.setImage(processedImage);
                imageView.setVisible(true);

                // Calculate proper fit dimensions while maintaining aspect ratio
                double imageWidth = processedImage.getWidth();
                double imageHeight = processedImage.getHeight();
                double containerWidth = 600;
                double containerHeight = 400;

                double scaleX = containerWidth / imageWidth;
                double scaleY = containerHeight / imageHeight;
                double scale = Math.min(scaleX, scaleY);

                imageView.setFitWidth(imageWidth * scale);
                imageView.setFitHeight(imageHeight * scale);

            }

        }
    }

    private void applyBrightness() {
        if (imageProcessor.isImageLoaded()) {
            imageProcessor.setBrightnessSlider(brightnessSlider.getValue());
            javafx.scene.image.Image editedImage = imageProcessor.getEditedImage();
            if (editedImage != null) {
                imageView.setImage(editedImage);
            }
            statusLabel.setText("Brightness adjusted");
        }
    }

    private void applyContrast() {
        if (imageProcessor.isImageLoaded()) {
            imageProcessor.setContrastSlider(contrastSlider.getValue());
            javafx.scene.image.Image editedImage = imageProcessor.getEditedImage();
            if (editedImage != null) {
                imageView.setImage(editedImage);
            }

        }
    }

    private void toggleGrayscale() {
        if (imageProcessor.isImageLoaded()) {
            imageProcessor.setGrayscaleEnabled(grayscaleCheckbox.isSelected());
            javafx.scene.image.Image editedImage = imageProcessor.getEditedImage();
            if (editedImage != null) {
                imageView.setImage(editedImage);
            }
            statusLabel.setText("Grayscale " + (grayscaleCheckbox.isSelected() ? "enabled" : "disabled"));

        }
    }

    private void applyBorder() {
        if (imageProcessor.isImageLoaded()) {
            imageProcessor.setBorderWidth((int) borderSlider.getValue());
            javafx.scene.image.Image editedImage = imageProcessor.getEditedImage();
            if (editedImage != null) {
                imageView.setImage(editedImage);
            }
            statusLabel.setText("Border applied");

        }
    }
    private void applyBorderColor() {
        if (imageProcessor.isImageLoaded()) {
            imageProcessor.setBorderColor(borderColorPicker.getValue());
            javafx.scene.image.Image editedImage = imageProcessor.getEditedImage();
            if (editedImage != null) {
                imageView.setImage(editedImage);
            }
            statusLabel.setText("Border color changed");

        }
    }

    private void applyBlur() {
        if (imageProcessor.isImageLoaded()) {
            imageProcessor.applyBlurFilter((int)blurSlider.getValue());
            javafx.scene.image.Image editedImage = imageProcessor.getEditedImage();
            if (editedImage != null) {
                imageView.setImage(editedImage);
            }
            statusLabel.setText("Blur effect applied");

        }
    }

    private void applyEdgeDetection() {
        if (imageProcessor.isImageLoaded()) {
            imageProcessor.applyEdgeDetection(50, 150);
            javafx.scene.image.Image editedImage = imageProcessor.getEditedImage();
            if (editedImage != null) {
                imageView.setImage(editedImage);
            }
            statusLabel.setText("Edge detection applied");

        }
    }

    private void applyHistogramEqualization() {
        if (imageProcessor.isImageLoaded()) {
            imageProcessor.applyHistogramEqualization();
            javafx.scene.image.Image editedImage = imageProcessor.getEditedImage();
            if (editedImage != null) {
                imageView.setImage(editedImage);
            }
            statusLabel.setText("Histogram equalization applied");
        }

    }


    private void resetImage() {
        if (imageProcessor.isImageLoaded()) {
            imageProcessor.resetToOriginal();
            javafx.scene.image.Image originalImage = imageProcessor.getOriginalImage();
            if (originalImage != null) {
                imageView.setImage(originalImage);
            }
            resetControls();
            statusLabel.setText("Image reset to original");

        }
    }

    private void saveImage() {
        if (imageProcessor.isImageLoaded()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            fileChooser.getExtensionFilters().add(
                new ExtensionFilter("PNG Files", "*.png")
            );
            
            File file = fileChooser.showSaveDialog(layout.getScene().getWindow());
            if (file != null) {
                boolean success = imageProcessor.saveImage(file.getAbsolutePath());
                statusLabel.setText(success ? "Image saved successfully" : "Error saving image");
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