package mm.prog.project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;

public class MainDashboard {
    private BorderPane root;
    private VideoGeneratorModule videoModule;
    private ImageEditorModule imageEditorModule;
    private TransformationModule transformationModule;
    private MosaicModule mosaicModule;
    private String selectedMosaicPath = ""; 

    public MainDashboard() {
        root = new BorderPane();
        
        // Initialize Modules[cite: 5, 6, 7]
        videoModule = new VideoGeneratorModule();
        imageEditorModule = new ImageEditorModule();
        transformationModule = new TransformationModule();
        
        ImageView mosaicImageView = new ImageView();
        mosaicImageView.setFitWidth(700);
        mosaicImageView.setPreserveRatio(true);
        mosaicModule = new MosaicModule(mosaicImageView);
        
        // Setup Navigation[cite: 5]
        HBox navBar = new HBox(15);
        navBar.setPadding(new Insets(15));
        navBar.setStyle("-fx-background-color: #333;");
        
        Button btnEditor = new Button("DIP Editor");
        Button btnTransformation = new Button("Transformation");
        Button btnMosaic = new Button("Image Mosaic");
        Button btnVideo = new Button("Video Generator");
        
        String style = "-fx-background-color: #555; -fx-text-fill: white; -fx-padding: 8 15;";
        btnEditor.setStyle(style);
        btnTransformation.setStyle(style);
        btnMosaic.setStyle(style);
        btnVideo.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-padding: 8 15;");

        navBar.getChildren().addAll(btnEditor, btnTransformation, btnMosaic, btnVideo);
        root.setTop(navBar);

        // Actions[cite: 5]
        btnEditor.setOnAction(e -> root.setCenter(imageEditorModule.getLayout()));
        btnTransformation.setOnAction(e -> root.setCenter(transformationModule.getLayout()));
        btnVideo.setOnAction(e -> root.setCenter(videoModule.getLayout()));
        btnMosaic.setOnAction(e -> root.setCenter(createMosaicTabLayout(mosaicImageView)));

        root.setCenter(videoModule.getLayout());
    }

    private VBox createMosaicTabLayout(ImageView mosaicImageView) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #121212;");

        HBox controlBar = new HBox(20);
        controlBar.setAlignment(Pos.CENTER);

        // Control 1: Load[cite: 6]
        Button btnLoad = new Button("📁 Load Image");
        btnLoad.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(root.getScene().getWindow());
            if (file != null) selectedMosaicPath = file.getAbsolutePath();
        });

        // Control 2: Size Selector[cite: 6]
        Label lblSize = new Label("Tile Size:");
        lblSize.setStyle("-fx-text-fill: white;");
        ComboBox<Integer> sizePicker = new ComboBox<>();
        sizePicker.getItems().addAll(10, 20, 30);
        sizePicker.setValue(20); 
        sizePicker.setOnAction(e -> mosaicModule.setTileSize(sizePicker.getValue()));

        // Control 3: Generate[cite: 6]
        Button btnGenerate = new Button("⚙ Generate Mosaic");
        btnGenerate.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        btnGenerate.setOnAction(e -> {
            if (!selectedMosaicPath.isEmpty()) mosaicModule.generateMosaic(selectedMosaicPath);
        });

        controlBar.getChildren().addAll(btnLoad, lblSize, sizePicker, btnGenerate);

        StackPane display = new StackPane(mosaicImageView);
        display.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333;");
        display.setPrefSize(750, 500);

        layout.getChildren().addAll(controlBar, display);
        return layout;
    }

    public BorderPane getContainer() { return root; }
    
    public void dispose() {
        if (imageEditorModule != null) imageEditorModule.dispose();
        if (mosaicModule != null) mosaicModule.dispose();
    }
}
