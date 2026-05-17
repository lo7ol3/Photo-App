package mm.prog.project;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class ImageRepositoryModule {
    private final BorderPane layout = new BorderPane();
    private final TilePane galleryPane = new TilePane();
    private final ScrollPane galleryScroll = new ScrollPane(galleryPane);
    private final ImageView previewImageView = new ImageView();
    private final TextArea annotationArea = new TextArea();
    private final CheckBox favoriteCheckbox = new CheckBox("Favourite image");
    private final Label statusLabel = new Label();
    private final Label heartOverlay = new Label("♥");
    private String selectedImagePath;

    private final Map<String, String> annotations = new HashMap<>();
    private final Set<String> favorites = new HashSet<>();
    private final List<String> imagePaths = new ArrayList<>();
    private final List<String> editedImagePaths = new ArrayList<>();
    private final List<String> mosaicPaths = new ArrayList<>();
    private final List<String> videoPaths = new ArrayList<>();

    private Consumer<String> editHandler;
    private Consumer<String> transformHandler;
    private Consumer<String> selectionHandler;
    private Consumer<List<String>> favoritesChangedHandler;

    private enum ViewMode { IMPORTED, EDITED, MOSAICS, VIDEOS }
    private ViewMode viewMode = ViewMode.IMPORTED;

    public ImageRepositoryModule() {
        createUI();
    }

    private void createUI() {
        layout.setStyle("-fx-background-color: #121212;");

        VBox topBar = new VBox(8);
        topBar.setPadding(new Insets(12));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #222;");

        Button btnLoadImages = new Button("📁 Add Images");
        btnLoadImages.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white;");
        btnLoadImages.setOnAction(e -> loadImages());

        Button btnClearList = new Button("Clear Repository");
        btnClearList.setStyle("-fx-background-color: #666; -fx-text-fill: white;");
        btnClearList.setOnAction(e -> clearRepository());

        topBar.getChildren().addAll(btnLoadImages, btnClearList);
        layout.setTop(topBar);

        galleryPane.setHgap(10);
        galleryPane.setVgap(12);
        galleryPane.setPadding(new Insets(10));
        galleryPane.setPrefColumns(0); 

        galleryScroll.setFitToWidth(true);
        galleryPane.prefWidthProperty().bind(galleryScroll.widthProperty());
        galleryScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        HBox sectionBar = new HBox(8);
        sectionBar.setPadding(new Insets(8));
        Button btnImported = new Button("Imported");
        Button btnEdited = new Button("Edited");
        Button btnMosaics = new Button("Mosaics");
        Button btnVideos = new Button("Videos");
        btnImported.setOnAction(e -> { viewMode = ViewMode.IMPORTED; refreshGallery(); });
        btnEdited.setOnAction(e -> { viewMode = ViewMode.EDITED; refreshGallery(); });
        btnMosaics.setOnAction(e -> { viewMode = ViewMode.MOSAICS; refreshGallery(); });
        btnVideos.setOnAction(e -> { viewMode = ViewMode.VIDEOS; refreshGallery(); });
        sectionBar.getChildren().addAll(btnImported, btnEdited, btnMosaics, btnVideos);

        VBox leftPanel = new VBox(10, new Label("Image Repository"), sectionBar, galleryScroll);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setStyle("-fx-background-color: #1a1a1a;");
        
        leftPanel.setPrefWidth(480); 

        // MODIFIED: Shrunk preview dimensions from 560x360 down to 420x280
        previewImageView.setFitWidth(420);
        previewImageView.setFitHeight(280);
        previewImageView.setPreserveRatio(true);
        previewImageView.setStyle("-fx-border-color: #333; -fx-border-width: 2;");

        heartOverlay.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 42px; -fx-opacity: 0.85;");
        heartOverlay.setVisible(false);

        StackPane previewPane = new StackPane(previewImageView, heartOverlay);
        StackPane.setAlignment(heartOverlay, Pos.TOP_RIGHT);
        previewPane.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 10;");

        annotationArea.setPromptText("Add metadata annotation for selected image...");
        annotationArea.setWrapText(true);
        annotationArea.setPrefRowCount(4);

        favoriteCheckbox.setStyle("-fx-text-fill: white;");
        favoriteCheckbox.setOnAction(e -> toggleFavorite());

        Button btnSaveAnnotation = new Button("Save Annotation");
        btnSaveAnnotation.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnSaveAnnotation.setOnAction(e -> saveAnnotation());

        VBox annotationBox = new VBox(10, new Label("Metadata Annotation:"), annotationArea, favoriteCheckbox, btnSaveAnnotation);
        annotationBox.setPadding(new Insets(15));
        annotationBox.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333; -fx-border-width: 1;");

        statusLabel.setText("Load images to begin browsing your repository.");
        statusLabel.setStyle("-fx-text-fill: #aaa;");

        VBox rightSection = new VBox(15, previewPane, annotationBox, statusLabel);
        rightSection.setPadding(new Insets(15));

        layout.setLeft(leftPanel);
        layout.setCenter(rightSection);
    }

    private void loadImages() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image Files");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"));
        List<File> files = chooser.showOpenMultipleDialog(layout.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            for (File file : files) {
                if (!imagePaths.contains(file.getAbsolutePath())) {
                    imagePaths.add(file.getAbsolutePath());
                }
            }
            refreshGallery();
            if (selectedImagePath == null && !imagePaths.isEmpty()) {
                selectImage(imagePaths.get(0));
            }
            statusLabel.setText(imagePaths.size() + " images loaded.");
        }
    }

    private void selectImage(String path) {
        selectedImagePath = path;
        if (path == null) {
            previewImageView.setImage(null);
            annotationArea.clear();
            favoriteCheckbox.setSelected(false);
            heartOverlay.setVisible(false);
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            statusLabel.setText("Selected file no longer exists.");
            return;
        }

        previewImageView.setImage(new Image(file.toURI().toString()));
        annotationArea.setText(annotations.getOrDefault(path, ""));
        favoriteCheckbox.setSelected(favorites.contains(path));
        updateHeartOverlay();
        statusLabel.setText("Selected: " + file.getName());
        refreshGallery();
        if (selectionHandler != null) selectionHandler.accept(path);
    }

    public void setSelectionHandler(Consumer<String> handler) { this.selectionHandler = handler; }

    private void saveAnnotation() {
        if (selectedImagePath == null) {
            statusLabel.setText("Select an image before saving annotation.");
            return;
        }
        String text = annotationArea.getText().trim();
        if (!text.isEmpty()) {
            annotations.put(selectedImagePath, text);
            statusLabel.setText("Annotation saved.");
        } else {
            annotations.remove(selectedImagePath);
            statusLabel.setText("Annotation removed.");
        }
        refreshGallery();
        updateHeartOverlay();
        notifyFavoritesChanged();
    }

    private void toggleFavorite() {
        if (selectedImagePath == null) return;
        if (favoriteCheckbox.isSelected()) {
            favorites.add(selectedImagePath);
            statusLabel.setText("Marked as favourite.");
        } else {
            favorites.remove(selectedImagePath);
            statusLabel.setText("Removed from favourites.");
        }
        refreshGallery();
        notifyFavoritesChanged();
    }

    private void clearRepository() {
        imagePaths.clear();
        annotations.clear();
        favorites.clear();
        selectedImagePath = null;
        galleryPane.getChildren().clear();
        previewImageView.setImage(null);
        annotationArea.clear();
        favoriteCheckbox.setSelected(false);
        heartOverlay.setVisible(false);
        statusLabel.setText("Repository cleared.");
        notifyFavoritesChanged();
    }

    private void updateHeartOverlay() {
        heartOverlay.setVisible(selectedImagePath != null && annotations.containsKey(selectedImagePath) && !annotations.get(selectedImagePath).isBlank());
    }

    private void refreshGallery() {
        galleryPane.getChildren().clear();
            List<String> source = switch(viewMode) {
                case EDITED -> editedImagePaths;
                case MOSAICS -> mosaicPaths;
                case VIDEOS -> videoPaths;
                default -> imagePaths;
            };
            for (String path : source) {
            File f = new File(path);
            Image thumbImg = new Image(f.toURI().toString(), 160, 120, true, true);
            ImageView thumbView = new ImageView(thumbImg);
            thumbView.setFitWidth(160);
            thumbView.setFitHeight(120);
            thumbView.setPreserveRatio(true);

            Label badge = new Label("♥");
            badge.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 20px; -fx-opacity: 0.9;");
            boolean hasAnnotation = annotations.containsKey(path) && !annotations.get(path).isBlank();
            badge.setVisible(hasAnnotation);

            StackPane stack = new StackPane(thumbView, badge);
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            stack.setCursor(Cursor.HAND);
            Label name = new Label(f.getName());
            name.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

            HBox actionBar = new HBox(6);
            actionBar.setAlignment(Pos.CENTER);
            Button btnEdit = new Button("Edit");
            btnEdit.setOnAction(e -> { if (editHandler != null) editHandler.accept(path); });
            Button btnTransform = new Button("Transform");
            btnTransform.setOnAction(e -> { if (transformHandler != null) transformHandler.accept(path); });
            Button btnSave = new Button("Save");
            btnSave.setOnAction(e -> saveItemForCurrentMode(path));
            Button btnShare = new Button("Share");
            btnShare.setOnAction(e -> ShareService.shareViaWhatsApp("Check this out: " + f.getAbsolutePath()));

            // Only show relevant actions per mode
            if (viewMode == ViewMode.IMPORTED) {
                actionBar.getChildren().addAll(btnEdit, btnTransform, btnSave);
            } else if (viewMode == ViewMode.EDITED) {
                actionBar.getChildren().addAll(btnShare);
            } else if (viewMode == ViewMode.MOSAICS) {
                actionBar.getChildren().addAll(btnShare);
            } else if (viewMode == ViewMode.VIDEOS) {
                actionBar.getChildren().addAll(btnShare);
            }

            VBox item = new VBox(6, stack, name, actionBar);
            item.setOnMouseClicked(e -> selectImage(path));
            galleryPane.getChildren().add(item);
        }
    }

    private void saveItemForCurrentMode(String path) {
        if (viewMode == ViewMode.IMPORTED) {
            // save imported into edited by default
            if (!editedImagePaths.contains(path)) editedImagePaths.add(path);
            statusLabel.setText("Saved to Edited.");
        }
        refreshGallery();
    }

    public void setEditHandler(Consumer<String> handler) { this.editHandler = handler; }
    public void setTransformHandler(Consumer<String> handler) { this.transformHandler = handler; }

    // External modules can add items when they produce edited/mosaic/video outputs
    public void addEditedImage(String path) { if (!editedImagePaths.contains(path)) editedImagePaths.add(path); }
    public void addMosaic(String path) { if (!mosaicPaths.contains(path)) mosaicPaths.add(path); }
    public void addVideo(String path) { if (!videoPaths.contains(path)) videoPaths.add(path); }

    public BorderPane getLayout() {
        return layout;
    }

    public List<String> getFavoriteImagePaths() {
        return new ArrayList<>(favorites);
    }

    public void setFavoritesChangedHandler(Consumer<List<String>> handler) {
        this.favoritesChangedHandler = handler;
    }

    private void notifyFavoritesChanged() {
        if (favoritesChangedHandler != null) {
            favoritesChangedHandler.accept(getFavoriteImagePaths());
        }
    }

    public String getSelectedImagePath() {
        return selectedImagePath;
    }
}
