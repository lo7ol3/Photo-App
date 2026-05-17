import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import model.ImageData;
import model.SharedData;

public class RepositoryModule {

    private BorderPane layout;
    private TilePane tilePane;
    private List<ImageData> imageList = new ArrayList<>();
    private final String FILE_PATH = "data.json";
    private final Gson gson = new Gson();
    private StackPane selectedContainer = null;

    public RepositoryModule() {
        createUI();
        loadData(); // Automatically recover any previously loaded images and annotations on launch
    }

    private void createUI() {
        layout = new BorderPane();
        layout.setPrefSize(1500, 800);
        layout.setStyle("-fx-background-color: black;");

        // --- TOP HEADER ARCHITECTURE CONTROL BAR ---
        AnchorPane topPane = new AnchorPane();
        topPane.setPrefHeight(75);
        topPane.setStyle("-fx-background-color: #18181b; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Image Repository Archive");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System Bold", 20));
        title.setLayoutX(24);
        title.setLayoutY(22);

        // Explicitly named button that browses local file system storage files
        Button btnLoadImage = new Button("Load Image");
        btnLoadImage.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        btnLoadImage.setLayoutX(1320); // Positions it perfectly on the right-hand layout header section
        btnLoadImage.setLayoutY(18);
        btnLoadImage.setCursor(Cursor.HAND);

        // Ingestion trigger loop supporting multi-file selection via Shift/Ctrl key selections
        btnLoadImage.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Local Images to Add into Repository");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files System Matrix", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
            );

            // Prompts explorer window layer for multi selection inputs
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(layout.getScene().getWindow());
            
            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                for (File file : selectedFiles) {
                    // Normalize native path structure cleanly
                    String pathString = file.getAbsolutePath();
                    registerNewImage(pathString);
                }
            }
        });

        topPane.getChildren().addAll(title, btnLoadImage);
        layout.setTop(topPane);

        // --- CENTER DECK GRID PRESENTATION MATRIX PANEL ---
        tilePane = new TilePane();
        tilePane.setHgap(40); 
        tilePane.setVgap(40);
        tilePane.setPrefTileWidth(210);
        tilePane.setPrefTileHeight(210);
        tilePane.setPadding(new Insets(40));
        tilePane.setStyle("-fx-background-color: black;");

        ScrollPane scrollPane = new ScrollPane(tilePane);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: black; -fx-background-color: black; -fx-border-color: transparent;");

        layout.setCenter(scrollPane);
    }

    public void addImageToUI(ImageData data) {
        try {
            // Read target resource cleanly via local File conversion utilities
            File imgFile = new File(data.imagePath);
            Image image = new Image(imgFile.toURI().toString());
            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(200);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);

            // Annotation presence visual heart tracker component
            Label heart = new Label("♥");
            heart.setStyle("-fx-text-fill: red; -fx-font-size: 25;");
            StackPane.setAlignment(heart, Pos.TOP_RIGHT);
            StackPane.setMargin(heart, new Insets(0, 8, 0, 0));
            heart.setVisible(data.hasAnnotation);

            // Annotations modification utility dialogue pop-up anchor button
            Button editBtn = new Button("✍");
            editBtn.setTextFill(Color.WHITE);
            editBtn.setFont(Font.font(16));
            editBtn.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 15; -fx-cursor: hand;");
            StackPane.setAlignment(editBtn, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(editBtn, new Insets(0, 5, 5, 0));

            editBtn.setOnAction(e -> openAnnotationDialog(heart, data));

            StackPane container = new StackPane();
            container.setStyle("-fx-border-color: #444; -fx-border-width: 2; -fx-background-color: #111;");
            container.getChildren().addAll(imageView, heart, editBtn);
            container.setCursor(Cursor.HAND);

            // Dynamic Hover Tooltip update infrastructure mapping logic chains
            Tooltip tooltip = new Tooltip(data.annotation == null || data.annotation.isEmpty() ? "No annotation" : data.annotation);
            tooltip.setStyle("-fx-font-size: 14px;");
            Tooltip.install(container, tooltip);

            container.setOnMouseEntered(e -> {
                tooltip.setText(data.annotation == null || data.annotation.isEmpty() ? "No annotation" : data.annotation);
            });

            // Card highlight tracking focus block triggers
            container.setOnMouseClicked(e -> {
                if (selectedContainer != null) {
                    selectedContainer.setStyle("-fx-border-color: #444; -fx-border-width: 2; -fx-background-color: #111;");
                }

                container.setStyle("-fx-border-color: yellow; -fx-border-width: 3; -fx-background-color: #111;");
                selectedContainer = container;

                // 1. Update the shared global state path value
                SharedData.selectedImagePath = data.imagePath;

                // 2. Alert all active listening tabs to refresh their workspace views immediately!
                SharedData.setSelectedImagePath(data.imagePath);
            });

            tilePane.getChildren().add(container);
        } catch (Exception ex) {
            System.err.println("Could not parse file index layout thumbnail card: " + ex.getMessage());
        }
    }

    private void openAnnotationDialog(Label heart, ImageData data) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add/Edit Annotation Meta Notes");

        ButtonType saveBtn = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(6);
        textArea.setPrefColumnCount(25);
        textArea.setText(data.annotation == null ? "" : data.annotation);
        dialog.getDialogPane().setContent(textArea);

        dialog.setResultConverter(btn -> btn == saveBtn ? textArea.getText() : null);

        dialog.showAndWait().ifPresent(text -> {
            data.annotation = text;
            data.hasAnnotation = text != null && !text.trim().isEmpty();
            heart.setVisible(data.hasAnnotation);
            saveData(); // Commit modifications instantly back over JSON disk storage assets 
        });
    }

    private void saveData() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(imageList, writer);
        } catch (Exception e) {
            System.err.println("Persistent save failure mapping state parameters: " + e.getMessage());
        }
    }

    private void loadData() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return;

            BufferedReader reader = new BufferedReader(new FileReader(file));
            Type listType = new TypeToken<List<ImageData>>(){}.getType();
            imageList = gson.fromJson(reader, listType);
            reader.close();

            if (imageList == null) {
                imageList = new ArrayList<>();
            }

            tilePane.getChildren().clear();
            for (ImageData data : imageList) {
                addImageToUI(data);
            }
        } catch (Exception e) {
            System.err.println("Persistent tracking data initialization failed: " + e.getMessage());
        }
    }
    
    public void registerNewImage(String path) {
        if (path == null || path.trim().isEmpty()) return;
        
        // Block exact path duplicates within active file indices
        for (ImageData existing : imageList) {
            if (existing.imagePath.equalsIgnoreCase(path)) return;
        }
        
        ImageData data = new ImageData(path, "");
        imageList.add(data);
        addImageToUI(data);
        saveData();
    }

    public BorderPane getLayout() {
        return this.layout;
    }
}