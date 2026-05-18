package mm.prog.project;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class MainDashboard {

    private BorderPane root;
    private RepositoryModule repositoryModule;
    private VideoGeneratorModule videoModule;
    private ImageEditorModule imageEditorModule;
    private TransformationModule transformationModule;
    private MosaicModule mosaicModule;
    private CollageModule collageModule; // NEW: Added Collage Module reference
    private ShareModule shareModule;

    public MainDashboard() {
        root = new BorderPane();

        // Initialize central storage and operational workspaces
        repositoryModule = new RepositoryModule();
        videoModule = new VideoGeneratorModule();
        imageEditorModule = new ImageEditorModule();
        transformationModule = new TransformationModule();
        shareModule = new ShareModule();

        // Connect the video generator to your repository's love-icon (annotated) images!
        videoModule.setFavoriteImageSupplier(() -> repositoryModule.getAnnotatedFavoriteImages());

        // Keep the original Mosaic setup
        ImageView mosaicImageView = new ImageView();
        mosaicImageView.setFitWidth(700);
        mosaicImageView.setPreserveRatio(true);
        mosaicModule = new MosaicModule(mosaicImageView);

        // NEW: Initialize the Collage Module and pass the repository to it
        collageModule = new CollageModule(repositoryModule);

        // Navigation bar architecture
        HBox navBar = new HBox(15);
        navBar.setPadding(new Insets(15));
        navBar.setStyle("-fx-background-color: #222;");

        Button btnRepository = new Button("🗄️ Repository Archive");
        Button btnEditor = new Button("DIP Editor");
        Button btnTransformation = new Button("Transformation");
        Button btnMosaic = new Button("Image Mosaic");
        Button btnCollage = new Button("🖼️ Photo Collage"); // NEW: Collage Button
        Button btnShare = new Button("🔗 Share");
        btnShare.setStyle("-fx-background-color: #25d366; -fx-text-fill: black; -fx-font-weight: bold;");
        Button btnVideo = new Button("Video Generator");

        // Assign visual styles to navigation buttons
        String btnStyle = "-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold;";
        btnRepository.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEditor.setStyle(btnStyle);
        btnTransformation.setStyle(btnStyle);
        btnMosaic.setStyle(btnStyle);
        btnCollage.setStyle("-fx-background-color: #e83e8c; -fx-text-fill: white; -fx-font-weight: bold;"); // NEW: Distinct style for Collage
        btnVideo.setStyle(btnStyle);

        // Dynamic Workspace Views Switching
        btnRepository.setOnAction(e -> root.setCenter(repositoryModule.getLayout()));
        btnEditor.setOnAction(e -> root.setCenter(imageEditorModule.getLayout()));
        btnTransformation.setOnAction(e -> root.setCenter(transformationModule.getLayout()));
        btnMosaic.setOnAction(e -> root.setCenter(createMosaicLayout(mosaicImageView)));
        btnCollage.setOnAction(e -> root.setCenter(collageModule.getView())); // NEW: Switch to Collage view
        btnShare.setOnAction(e -> root.setCenter(shareModule.getLayout()));
        btnVideo.setOnAction(e -> root.setCenter(videoModule.getLayout()));

        // NEW: Added btnCollage to the navigation bar
        navBar.getChildren().addAll(btnRepository, btnEditor, btnTransformation, btnMosaic, btnCollage, btnVideo, btnShare);
        root.setTop(navBar);

        // Default execution view
        root.setCenter(repositoryModule.getLayout());
    }

    // Your original createMosaicLayout method remains completely untouched!
    private VBox createMosaicLayout(ImageView mosaicImageView) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #121212;");

        HBox controlBar = new HBox(15);
        controlBar.setAlignment(Pos.CENTER_LEFT);

        ComboBox<Integer> sizePicker = new ComboBox<>();
        sizePicker.getItems().addAll(10, 20, 30);
        sizePicker.setValue(20);
        sizePicker.setOnAction(e -> mosaicModule.setTileSize(sizePicker.getValue()));

        Label sizeLabel = new Label("Tile Size:");
        sizeLabel.setStyle("-fx-text-fill: white;");

        Button btnGenerate = new Button("⚙ Generate Mosaic");
        btnGenerate.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");

        // Pulls dynamic target path directly out of global SharedData registry
        btnGenerate.setOnAction(e -> {
            if (SharedData.selectedImagePath != null && !SharedData.selectedImagePath.isEmpty()) {
                mosaicModule.generateMosaic(SharedData.selectedImagePath);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select an image profile tracking thumbnail in the Repository Archive first!");
                alert.showAndWait();
            }
        });

        controlBar.getChildren().addAll(sizeLabel, sizePicker, btnGenerate);

        StackPane display = new StackPane(mosaicImageView);
        display.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333;");
        display.setPrefSize(750, 500);

        layout.getChildren().addAll(controlBar, display);
        return layout;
    }

    public void dispose() {
        if (imageEditorModule != null) {
            imageEditorModule.dispose();
        }
    }

    public BorderPane getContainer() {
        return this.root;
    }

    public BorderPane getLayout() {
        return this.root;
    }

    public RepositoryModule getRepositoryModule() {
        return this.repositoryModule;
    }
}