package mm.prog.project;

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

public class RepositoryModule {

    private BorderPane layout;
    private TilePane tilePane;
    private List<ImageData> imageList = new ArrayList<>();
    private final String FILE_PATH = "data.json";
    private final Gson gson = new Gson();

    // A list to keep track of ALL currently selected images
    private List<String> selectedImagePaths = new ArrayList<>();

    public RepositoryModule() {
        createUI();
        loadData();
    }

    private void createUI() {
        layout = new BorderPane();
        layout.setPrefSize(350, 800);
        layout.setStyle("-fx-background-color: black;");

        AnchorPane topPane = new AnchorPane();
        topPane.setPrefHeight(75);
        topPane.setStyle("-fx-background-color: #18181b; -fx-border-color: #333333; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Image Repository");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System Bold", 16));

        AnchorPane.setLeftAnchor(title, 15.0);
        AnchorPane.setTopAnchor(title, 26.0);

        Button btnLoadImage = new Button("📂 Load");
        btnLoadImage.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 15; -fx-background-radius: 4;");
        btnLoadImage.setCursor(Cursor.HAND);

        AnchorPane.setRightAnchor(btnLoadImage, 15.0);
        AnchorPane.setTopAnchor(btnLoadImage, 22.0);

        btnLoadImage.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Local Images to Add into Repository");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files System Matrix", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
            );

            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(layout.getScene().getWindow());

            if (selectedFiles != null && !selectedFiles.isEmpty()) {
                for (File file : selectedFiles) {
                    String pathString = file.getAbsolutePath();
                    registerNewImage(pathString);
                }
            }
        });

        topPane.getChildren().addAll(title, btnLoadImage);
        layout.setTop(topPane);

        tilePane = new TilePane();
        tilePane.setHgap(40);
        tilePane.setVgap(40);
        tilePane.setPrefTileWidth(210);
        tilePane.setPrefTileHeight(210);
        tilePane.setPadding(new Insets(40));
        tilePane.setStyle("-fx-background-color: black;");
        tilePane.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(tilePane);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: black; -fx-background-color: black; -fx-border-color: transparent;");

        layout.setCenter(scrollPane);
    }

    public List<String> getAnnotatedFavoriteImages() {
        List<String> favorites = new ArrayList<>();
        for (ImageData data : imageList) {
            if (data.hasAnnotation) {
                favorites.add(data.imagePath);
            }
        }
        return favorites;
    }

    public List<String> getAllImagePaths() {
        List<String> paths = new ArrayList<>();
        for (ImageData data : imageList) {
            paths.add(data.imagePath);
        }
        return paths;
    }

    // Method to return all currently highlighted images
    public List<String> getSelectedImagePaths() {
        return new ArrayList<>(selectedImagePaths);
    }

    public void addImageToUI(ImageData data) {
        try {
            File imgFile = new File(data.imagePath);
            Image image = new Image(imgFile.toURI().toString());
            ImageView imageView = new ImageView(image);

            imageView.setFitWidth(200);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);

            Label heart = new Label("♥");
            heart.setStyle("-fx-text-fill: red; -fx-font-size: 25;");
            StackPane.setAlignment(heart, Pos.TOP_RIGHT);
            StackPane.setMargin(heart, new Insets(0, 8, 0, 0));
            heart.setVisible(data.hasAnnotation);

            Button editBtn = new Button("✍");
            editBtn.setTextFill(Color.WHITE);
            editBtn.setFont(Font.font(16));
            editBtn.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 15; -fx-cursor: hand;");
            StackPane.setAlignment(editBtn, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(editBtn, new Insets(0, 5, 5, 0));

            // --- NEW: DELETE BUTTON ---
            Button deleteBtn = new Button("🗑");
            deleteBtn.setTextFill(Color.WHITE);
            deleteBtn.setFont(Font.font(14));
            deleteBtn.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 15; -fx-cursor: hand;");
            StackPane.setAlignment(deleteBtn, Pos.BOTTOM_LEFT);
            StackPane.setMargin(deleteBtn, new Insets(0, 0, 5, 5));

            StackPane container = new StackPane();
            container.setStyle("-fx-border-color: #444; -fx-border-width: 2; -fx-background-color: #111;");
            container.getChildren().addAll(imageView, heart, editBtn, deleteBtn);
            container.setCursor(Cursor.HAND);

            // Prevent button clicks from bubbling up and triggering the selection logic on the container
            editBtn.setOnMouseClicked(e -> e.consume());
            deleteBtn.setOnMouseClicked(e -> e.consume());

            editBtn.setOnAction(e -> openAnnotationDialog(heart, data));

            // --- NEW: DELETE BUTTON ACTION CONFIGURATION ---
            deleteBtn.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove this image from the repository?", ButtonType.YES, ButtonType.NO);
                alert.setHeaderText(null);
                alert.setTitle("Delete Image");
                alert.initOwner(layout.getScene().getWindow());
                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        // 1. Remove from inner data tracking
                        imageList.remove(data);
                        selectedImagePaths.remove(data.imagePath);

                        // 2. Remove visually from layout grid
                        tilePane.getChildren().remove(container);

                        // 3. Clear global single-image tool selection if this was active
                        if (SharedData.selectedImagePath != null && SharedData.selectedImagePath.equals(data.imagePath)) {
                            if (!selectedImagePaths.isEmpty()) {
                                String fallback = selectedImagePaths.get(selectedImagePaths.size() - 1);
                                SharedData.selectedImagePath = fallback;
                                SharedData.setSelectedImagePath(fallback);
                            } else {
                                SharedData.selectedImagePath = null;
                            }
                        }

                        // 4. Commit changes to JSON file
                        saveData();
                    }
                });
            });

            Tooltip tooltip = new Tooltip(data.annotation == null || data.annotation.isEmpty() ? "No annotation" : data.annotation);
            tooltip.setStyle("-fx-font-size: 14px;");
            Tooltip.install(container, tooltip);

            // Multi-select toggle logic
            container.setOnMouseClicked(e -> {
                if (selectedImagePaths.contains(data.imagePath)) {
                    // If already selected, UN-SELECT it
                    selectedImagePaths.remove(data.imagePath);
                    container.setStyle("-fx-border-color: #444; -fx-border-width: 2; -fx-background-color: #111;");
                } else {
                    // If not selected, SELECT it
                    selectedImagePaths.add(data.imagePath);
                    container.setStyle("-fx-border-color: yellow; -fx-border-width: 3; -fx-background-color: #111;");
                }

                // Keep SharedData updated for your other tabs (DIP Editor, etc.)
                if (!selectedImagePaths.isEmpty()) {
                    // Sets the most recently clicked image for single-image tools
                    String lastSelected = selectedImagePaths.get(selectedImagePaths.size() - 1);
                    SharedData.selectedImagePath = lastSelected;
                    SharedData.setSelectedImagePath(lastSelected);
                } else {
                    SharedData.selectedImagePath = null;
                }
            });

            tilePane.getChildren().add(container);
        } catch (Exception ex) {
            System.err.println("Could not parse file thumbnail card: " + ex.getMessage());
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
            saveData();
        });
    }

    private void saveData() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(imageList, writer);
        } catch (Exception e) {
            System.err.println("Persistent save failure: " + e.getMessage());
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
            System.err.println("Persistent data initialization failed: " + e.getMessage());
        }
    }

    public void registerNewImage(String path) {
        if (path == null || path.trim().isEmpty()) return;

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