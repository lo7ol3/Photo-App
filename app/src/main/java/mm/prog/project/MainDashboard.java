package mm.prog.project;

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
    private ShareModule shareModule;
    private LoadImageTab loadImageTab;

    public MainDashboard() {
        root = new BorderPane();
        
        // Initialize central storage and operational workspaces
        repositoryModule = new RepositoryModule();
        videoModule = new VideoGeneratorModule();
        imageEditorModule = new ImageEditorModule();
        transformationModule = new TransformationModule();
        shareModule = new ShareModule();
        loadImageTab = new LoadImageTab(this);
        
        ImageView mosaicImageView = new ImageView();
        mosaicImageView.setFitWidth(700);
        mosaicImageView.setPreserveRatio(true);
        mosaicModule = new MosaicModule(mosaicImageView);
        
        // Navigation bar architecture
        HBox navBar = new HBox(15);
        navBar.setPadding(new Insets(15));
        navBar.setStyle("-fx-background-color: #222;");
        
        Button btnLoadTab = new Button("📂 Load Image Hub");
        Button btnRepository = new Button("🗄️ Repository");
        Button btnEditor = new Button("DIP Editor");
        Button btnTransformation = new Button("Transformation");
        Button btnMosaic = new Button("Image Mosaic");
        Button btnShare = new Button("🔗 Share");
        btnShare.setStyle("-fx-background-color: #25d366; -fx-text-fill: black; -fx-font-weight: bold;");
        Button btnVideo = new Button("Video Generator");
        
        // Assign visual styles to navigation buttons
        String btnStyle = "-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold;";
        btnLoadTab.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        btnEditor.setStyle(btnStyle);
        btnTransformation.setStyle(btnStyle);
        btnMosaic.setStyle(btnStyle);
        btnVideo.setStyle(btnStyle);
        
        // Dynamic Workspace Views Switching
        btnLoadTab.setOnAction(e -> root.setCenter(loadImageTab.getLayout()));
        btnEditor.setOnAction(e -> root.setCenter(imageEditorModule.getLayout()));
        btnTransformation.setOnAction(e -> root.setCenter(transformationModule.getLayout()));
        btnMosaic.setOnAction(e -> root.setCenter(createMosaicLayout(mosaicImageView)));
        btnShare.setOnAction(e -> root.setCenter(shareModule.getLayout()));
        btnVideo.setOnAction(e -> root.setCenter(videoModule.getLayout()));
        btnRepository.setOnAction(e -> root.setCenter(repositoryModule.getLayout()));
        
        navBar.getChildren().addAll(btnLoadTab, btnEditor,btnRepository, btnTransformation, btnMosaic, btnVideo, btnShare);
        root.setTop(navBar);
        
        // Default execution view
        root.setCenter(repositoryModule.getLayout());
    }

    private VBox createMosaicLayout(ImageView mosaicImageView) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #121212;");

        // Title
        Label title = new Label("🎨 Image Mosaic Generator");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // First control row - Tile Size and Shape Selection
        HBox controlRow1 = new HBox(15);
        controlRow1.setAlignment(Pos.CENTER_LEFT);

        Label sizeLabel = new Label("Tile Size:");
        sizeLabel.setStyle("-fx-text-fill: white;");

        ComboBox<Integer> sizePicker = new ComboBox<>();
        sizePicker.getItems().addAll(10, 15, 20, 25, 30, 35, 40);
        sizePicker.setValue(20);
        sizePicker.setOnAction(e -> mosaicModule.setTileSize(sizePicker.getValue()));

        Label shapeLabel = new Label("Shape:");
        shapeLabel.setStyle("-fx-text-fill: white;");

        ComboBox<MosaicModule.MosaicShape> shapePicker = new ComboBox<>();
        shapePicker.getItems().addAll(mosaicModule.getAvailableShapes());
        shapePicker.setValue(MosaicModule.MosaicShape.RECTANGLE);
        shapePicker.setOnAction(e -> mosaicModule.setMosaicShape(shapePicker.getValue()));

        controlRow1.getChildren().addAll(sizeLabel, sizePicker, shapeLabel, shapePicker);

        // Second control row - Image source selection
        HBox controlRow2 = new HBox(15);
        controlRow2.setAlignment(Pos.CENTER_LEFT);

        CheckBox useRepositoryCheck = new CheckBox("Use Repository Images");
        useRepositoryCheck.setStyle("-fx-text-fill: white;");
        useRepositoryCheck.setSelected(true);
        useRepositoryCheck.setOnAction(e -> mosaicModule.setUseRepositoryImages(useRepositoryCheck.isSelected()));

        Button btnLoadCustomTiles = new Button("📁 Load Custom Tiles");
        btnLoadCustomTiles.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;");
        btnLoadCustomTiles.setOnAction(e -> loadCustomTileImages());

        Button btnRefreshRepo = new Button("🔄 Refresh Repository");
        btnRefreshRepo.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold;");
        btnRefreshRepo.setOnAction(e -> {
            mosaicModule.setUseRepositoryImages(true);
            Alert info = new Alert(Alert.AlertType.INFORMATION, "Repository images refreshed! Available: " + mosaicModule.getTileCount() + " images");
            info.showAndWait();
        });

        Button btnClearTiles = new Button("🗑️ Clear Tiles");
        btnClearTiles.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold;");
        btnClearTiles.setOnAction(e -> {
            mosaicModule.clearTileLibrary();
            Alert info = new Alert(Alert.AlertType.INFORMATION, "All custom tiles cleared!");
            info.showAndWait();
        });

        controlRow2.getChildren().addAll(useRepositoryCheck, btnLoadCustomTiles, btnRefreshRepo, btnClearTiles);

        // Third control row - Generation button and info
        HBox controlRow3 = new HBox(15);
        controlRow3.setAlignment(Pos.CENTER);

        Button btnGenerate = new Button("⚙️ Generate Mosaic");
        btnGenerate.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; -fx-font-size: 14px;");

        btnGenerate.setOnAction(e -> {
            if (SharedData.selectedImagePath != null && !SharedData.selectedImagePath.isEmpty()) {
                mosaicModule.generateMosaic(SharedData.selectedImagePath);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please go to the 'Load Image Hub' tab and select an image workspace first!");
                alert.showAndWait();
            }
        });

        // Info label
        Label infoLabel = new Label("Tiles available: " + mosaicModule.getTileCount());
        infoLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");

        controlRow3.getChildren().addAll(btnGenerate, infoLabel);

        // Display area
        StackPane display = new StackPane(mosaicImageView);
        display.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333; -fx-border-width: 2px;");
        display.setPrefSize(750, 500);

        layout.getChildren().addAll(title, controlRow1, controlRow2, controlRow3, display);
        return layout;
    }

    private void loadCustomTileImages() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Custom Tile Images for Mosaic");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif")
        );

        java.util.List<java.io.File> files = fileChooser.showOpenMultipleDialog(root.getScene().getWindow());

        if (files != null && !files.isEmpty()) {
            mosaicModule.setUseRepositoryImages(false); // Switch to custom mode
            for (java.io.File file : files) {
                mosaicModule.addTile(file.getAbsolutePath());
            }

            Alert info = new Alert(Alert.AlertType.INFORMATION,
                    "Loaded " + files.size() + " custom tile images.\nTotal tiles available: " + mosaicModule.getTileCount() +
                            "\nNow using custom tiles instead of repository images.");
            info.showAndWait();
        }
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
