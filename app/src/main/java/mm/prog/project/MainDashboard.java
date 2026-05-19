package mm.prog.project;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MainDashboard {

    private BorderPane root;
    private RepositoryModule repositoryModule;
    private VideoGeneratorModule videoModule;
    private ImageEditorModule imageEditorModule;
    private TransformationModule transformationModule;
    private MosaicModule mosaicModule;
    private ShareModule shareModule;

    // Define our button styles so they are easy to change later
    private final String defaultBtnStyle = "-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold;";
    private final String activeBtnStyle = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold;"; // Bright Blue

    private final String defaultShareStyle = "-fx-background-color: #25d366; -fx-text-fill: black; -fx-font-weight: bold;";
    private final String activeShareStyle = "-fx-background-color: #1da849; -fx-text-fill: white; -fx-font-weight: bold;"; // Darker Green

    public MainDashboard() {
        root = new BorderPane();

        // Initialize central storage and operational workspaces
        repositoryModule = new RepositoryModule();
        videoModule = new VideoGeneratorModule();
        imageEditorModule = new ImageEditorModule();
        transformationModule = new TransformationModule();
        shareModule = new ShareModule();

        // Connect the video generator to your repository's love-icon (annotated) images
        videoModule.setFavoriteImageSupplier(() -> repositoryModule.getAnnotatedFavoriteImages());

        // Initialize the Collage (Mosaic) module
        mosaicModule = new MosaicModule(repositoryModule);

        // =========================================================
        // Anchor the Repository permanently to the LEFT Side
        // =========================================================
        BorderPane repoSidebar = repositoryModule.getLayout();
        repoSidebar.setPrefWidth(350); // Constrain width so it acts like a sidebar
        repoSidebar.setMinWidth(300);
        root.setLeft(repoSidebar);

        // Navigation bar architecture
        HBox navBar = new HBox(15);
        navBar.setPadding(new Insets(15));
        navBar.setStyle("-fx-background-color: #222;");

        // Buttons
        Button btnEditor = new Button("Image Editor");
        Button btnTransformation = new Button("Transformation");
        Button btnMosaic = new Button("Image Mosaic");
        Button btnVideo = new Button("Video Generator");
        Button btnShare = new Button("🔗 Share");

        // Dynamic Workspace Views Switching (Center Panel Only) + Active Tab Highlighting
        btnEditor.setOnAction(e -> {
            root.setCenter(imageEditorModule.getLayout());
            setActiveTab(btnEditor, btnEditor, btnTransformation, btnMosaic, btnVideo, btnShare);
        });

        btnTransformation.setOnAction(e -> {
            root.setCenter(transformationModule.getLayout());
            setActiveTab(btnTransformation, btnEditor, btnTransformation, btnMosaic, btnVideo, btnShare);
        });

        btnMosaic.setOnAction(e -> {
            root.setCenter(mosaicModule.getView());
            setActiveTab(btnMosaic, btnEditor, btnTransformation, btnMosaic, btnVideo, btnShare);
        });

        btnVideo.setOnAction(e -> {
            root.setCenter(videoModule.getLayout());
            setActiveTab(btnVideo, btnEditor, btnTransformation, btnMosaic, btnVideo, btnShare);
        });

        btnShare.setOnAction(e -> {
            root.setCenter(shareModule.getLayout());
            setActiveTab(btnShare, btnEditor, btnTransformation, btnMosaic, btnVideo, btnShare);
        });

        navBar.getChildren().addAll(btnEditor, btnTransformation, btnMosaic, btnVideo, btnShare);
        root.setTop(navBar);

        // Default execution view (Now defaults to DIP Editor in the center)
        root.setCenter(imageEditorModule.getLayout());

        // Set the initial active tab colors to match the default view
        setActiveTab(btnEditor, btnEditor, btnTransformation, btnMosaic, btnVideo, btnShare);
    }

    /**
     * Helper method to reset all button colors and highlight the active one
     */
    private void setActiveTab(Button activeBtn, Button btnEditor, Button btnTransformation, Button btnMosaic, Button btnVideo, Button btnShare) {
        // 1. Reset all to default styles
        btnEditor.setStyle(defaultBtnStyle);
        btnTransformation.setStyle(defaultBtnStyle);
        btnMosaic.setStyle(defaultBtnStyle);
        btnVideo.setStyle(defaultBtnStyle);
        btnShare.setStyle(defaultShareStyle);

        // 2. Apply the active style to whichever button was clicked
        if (activeBtn == btnShare) {
            activeBtn.setStyle(activeShareStyle);
        } else {
            activeBtn.setStyle(activeBtnStyle);
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