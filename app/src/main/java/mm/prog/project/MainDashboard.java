package mm.prog.project;

import java.io.File;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class MainDashboard {
    private BorderPane root;
    private VideoGeneratorModule videoModule;
    private ImageRepositoryModule repositoryModule;
    private ImageEditorModule imageEditorModule;
    private TransformationModule transformationModule;
    private MosaicModule mosaicModule;
    private String selectedMosaicPath = ""; 
    private String activeView = "Repository";

    public MainDashboard() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #101820;");
        
        repositoryModule = new ImageRepositoryModule();
        videoModule = new VideoGeneratorModule();
        videoModule.setFavoriteImageSupplier(() -> repositoryModule.getFavoriteImagePaths());
        imageEditorModule = new ImageEditorModule();
        transformationModule = new TransformationModule();
        
        ImageView mosaicImageView = new ImageView();
        mosaicImageView.setFitWidth(700);
        mosaicImageView.setPreserveRatio(true);
        mosaicModule = new MosaicModule(mosaicImageView);
        
        // Setup Navigation
        HBox navBar = new HBox(16);
        navBar.setPadding(new Insets(16, 24, 16, 24));
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setStyle("-fx-background-color: linear-gradient(to right, #16222a, #3a6073); -fx-border-color: #1f2a35; -fx-border-width: 0 0 2 0;");
        
        Button btnRepository = new Button("Image Repository");
        Button btnMosaic = new Button("Create Mosaic");
        Button btnVideo = new Button("Create Video");
        
        // REQUIREMENT 2.5: Global Share Button
        Button btnShare = new Button("📤 Share");
        btnShare.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-padding: 10 18; -fx-background-radius: 6;");
        btnShare.setOnAction(e -> showShareDialog("General App Share", "Check out my Multimedia Project creations!"));

        String defaultStyle = "-fx-background-color: #23313a; -fx-text-fill: #f7f7f7; -fx-padding: 10 18; -fx-background-radius: 6; -fx-border-radius: 6;";
        btnRepository.setStyle(defaultStyle);
        btnMosaic.setStyle(defaultStyle);
        btnVideo.setStyle(defaultStyle);

        navBar.getChildren().addAll(btnRepository, btnMosaic, btnVideo, btnShare);
        root.setTop(navBar);

        btnRepository.setOnAction(e -> {
            root.setCenter(repositoryModule.getLayout());
            setActiveNavButton(btnRepository, btnMosaic, btnVideo);
            activeView = "Repository";
        });
        btnVideo.setOnAction(e -> {
            videoModule.setFavoriteImages(repositoryModule.getFavoriteImagePaths());
            root.setCenter(videoModule.getLayout());
            setActiveNavButton(btnVideo, btnRepository, btnMosaic);
            activeView = "Video";
        });
        btnMosaic.setOnAction(e -> {
            root.setCenter(createMosaicTabLayout(mosaicImageView));
            setActiveNavButton(btnMosaic, btnRepository, btnVideo);
            activeView = "Mosaic";
        });

        // Wire repository edit/transform handlers so clicking 'Edit' opens editor
        repositoryModule.setEditHandler(path -> {
            if (path != null) imageEditorModule.loadImage(path);
            root.setCenter(imageEditorModule.getLayout());
            activeView = "Editor";
        });
        imageEditorModule.setOnBack(() -> {
            root.setCenter(repositoryModule.getLayout());
            setActiveNavButton(btnRepository, btnMosaic, btnVideo);
            activeView = "Repository";
        });
        repositoryModule.setTransformHandler(path -> {
            if (path != null) transformationModule.loadImage(path);
            root.setCenter(transformationModule.getLayout());
            activeView = "Transformation";
        });

        repositoryModule.setSelectionHandler(path -> {
            if (path == null) return;
            if ("Repository".equals(activeView)) return; // no-op when viewing repository
            if ("Editor".equals(activeView)) imageEditorModule.loadImage(path);
            if ("Transformation".equals(activeView)) transformationModule.loadImage(path);
        });

        repositoryModule.setFavoritesChangedHandler(list -> videoModule.setFavoriteImages(list));

        // wire save callbacks from modules to repository
        imageEditorModule.setOnSave(path -> { if (path != null) repositoryModule.addEditedImage(path); });
        mosaicModule.setOnMosaicSaved(path -> { if (path != null) repositoryModule.addMosaic(path); });
        videoModule.setOnVideoSaved(path -> { if (path != null) repositoryModule.addVideo(path); });

        setActiveNavButton(btnRepository, btnMosaic, btnVideo);
        root.setCenter(repositoryModule.getLayout());
    }

    // Social Integration Dialog
    private void showShareDialog(String subject, String defaultMsg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Social Integration");
        alert.setHeaderText("External Distribution");
        alert.setContentText("Choose a platform to share your content:");

        ButtonType emailBtn = new ButtonType("Email");
        ButtonType whatsappBtn = new ButtonType("WhatsApp");
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(emailBtn, whatsappBtn, closeBtn);

        alert.showAndWait().ifPresent(type -> {
            if (type == emailBtn) ShareService.shareViaEmail(subject, defaultMsg);
            else if (type == whatsappBtn) ShareService.shareViaWhatsApp(defaultMsg);
        });
    }

    private VBox createMosaicTabLayout(ImageView mosaicImageView) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #121212;");

        HBox controlBar = new HBox(15);
        controlBar.setAlignment(Pos.CENTER);

        Button btnLoad = new Button("📁 Load Local Image");
        btnLoad.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 10 16;");
        btnLoad.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            File file = chooser.showOpenDialog(root.getScene().getWindow());
            if (file != null) selectedMosaicPath = file.getAbsolutePath();
        });

        String repoImage = repositoryModule.getSelectedImagePath();
        if (repoImage != null && !repoImage.isEmpty()) {
            selectedMosaicPath = repoImage;
        }

        Label mosaicStatus = new Label(selectedMosaicPath.isEmpty()
            ? "Select an image from the repository or load one locally."
            : "Repository image ready for mosaic.");
        mosaicStatus.setStyle("-fx-text-fill: #ccc; -fx-font-size: 13px;");

        Button btnUseRepoImage = new Button("Use Repo Selection");
        btnUseRepoImage.setOnAction(e -> {
            String selectedRepoPath = repositoryModule.getSelectedImagePath();
            if (selectedRepoPath != null) {
                selectedMosaicPath = selectedRepoPath;
                mosaicStatus.setText("Selected repository image for mosaic.");
            } else {
                mosaicStatus.setText("Select an image in the repository first.");
            }
        });

        ComboBox<Integer> sizePicker = new ComboBox<>();
        sizePicker.setStyle("-fx-background-color: #1f2a38; -fx-text-fill: white; -fx-pref-width: 100;");
        sizePicker.getItems().addAll(10, 20, 30);
        sizePicker.setValue(20); 
        sizePicker.setOnAction(e -> mosaicModule.setTileSize(sizePicker.getValue()));

        Button btnGenerate = new Button("⚙ Generate Mosaic");
        btnGenerate.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 18; -fx-background-radius: 6;");
        btnGenerate.setOnAction(e -> {
            if (!selectedMosaicPath.isEmpty()) mosaicModule.generateMosaic(selectedMosaicPath);
        });

        Button btnSaveMosaic = new Button("💾 Save Mosaic");
        btnSaveMosaic.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 18; -fx-background-radius: 6;");
        btnSaveMosaic.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Mosaic As");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
            File out = chooser.showSaveDialog(root.getScene().getWindow());
            if (out != null) {
                boolean ok = mosaicModule.saveDisplayedMosaic(out);
                mosaicStatus.setText(ok ? "Mosaic saved: " + out.getName() : "Error saving mosaic.");
            }
        });

        // Tab-Specific Share
        Button btnTabShare = new Button("📤 Share Mosaic");
        btnTabShare.setStyle("-fx-background-color: #ff8c42; -fx-text-fill: white; -fx-padding: 10 18; -fx-background-radius: 6;");
        btnTabShare.setOnAction(e -> showShareDialog("My Image Mosaic", "Look at this mosaic I generated with a tile size of " + sizePicker.getValue()));

        controlBar.getChildren().addAll(btnLoad, btnUseRepoImage, sizePicker, btnGenerate, btnSaveMosaic, btnTabShare);
        controlBar.setStyle("-fx-background-color: #1f2a38; -fx-padding: 16; -fx-background-radius: 10; -fx-border-radius: 10;");

        StackPane display = new StackPane(mosaicImageView);
        display.setStyle("-fx-background-color: #121b25; -fx-border-color: #2b3a4d; -fx-border-width: 2; -fx-background-radius: 10; -fx-border-radius: 10;");
        display.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(display, Priority.ALWAYS);
        mosaicImageView.setPreserveRatio(true);
        mosaicImageView.setFitWidth(760);
        mosaicImageView.setFitHeight(440);

        layout.getChildren().addAll(controlBar, mosaicStatus, display);
        return layout;
    }

    private void setActiveNavButton(Button active, Button... others) {
        String activeStyle = "-fx-background-color: #0f9dff; -fx-text-fill: white; -fx-padding: 10 16; -fx-background-radius: 6; -fx-border-color: #74b9ff; -fx-border-width: 2;";
        String inactiveStyle = "-fx-background-color: #23313a; -fx-text-fill: #dfe6e9; -fx-padding: 10 16; -fx-background-radius: 6; -fx-border-color: transparent;";
        active.setStyle(activeStyle);
        for (Button button : others) {
            button.setStyle(inactiveStyle);
        }
    }

    public BorderPane getContainer() { return root; }
    
    public void dispose() {
        if (imageEditorModule != null) imageEditorModule.dispose();
        if (mosaicModule != null) mosaicModule.dispose();
    }
}
