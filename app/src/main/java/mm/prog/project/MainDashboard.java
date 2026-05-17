import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.SharedData;

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
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please go to the 'Load Image Hub' tab and select an image workspace first!");
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
