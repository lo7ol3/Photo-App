package mm.prog.project;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainDashboard {
    private BorderPane root;
    private VideoGeneratorModule videoModule;
    private ImageEditorModule imageEditorModule;

    public MainDashboard() {
        root = new BorderPane();
        
        // Initialize modules
        videoModule = new VideoGeneratorModule();
        imageEditorModule = new ImageEditorModule();
        
        // 1. Navigation Bar (Requirement 2.1)
        HBox navBar = new HBox(20);
        navBar.setPadding(new Insets(15));
        navBar.setStyle("-fx-background-color: #333;");
        
        Button btnRepository = new Button("Image Repository");
        Button btnEditor = new Button("DIP Editor");
        Button btnVideo = new Button("Video Generator");
        
        // Style buttons
        String buttonStyle = "-fx-background-color: #555; -fx-text-fill: white; -fx-padding: 10 20; -fx-border-radius: 5; -fx-background-radius: 5;";
        String activeButtonStyle = "-fx-background-color: #0078D7; -fx-text-fill: white; -fx-padding: 10 20; -fx-border-radius: 5; -fx-background-radius: 5;";
        
        btnRepository.setStyle(buttonStyle);
        btnEditor.setStyle(buttonStyle);
        btnVideo.setStyle(activeButtonStyle); // Default active
        
        // Button hover effects
        btnRepository.setOnMouseEntered(e -> btnRepository.setStyle(activeButtonStyle));
        btnRepository.setOnMouseExited(e -> btnRepository.setStyle(buttonStyle));
        
        btnEditor.setOnMouseEntered(e -> btnEditor.setStyle(activeButtonStyle));
        btnEditor.setOnMouseExited(e -> btnEditor.setStyle(buttonStyle));
        
        btnVideo.setOnMouseEntered(e -> btnVideo.setStyle(activeButtonStyle));
        btnVideo.setOnMouseExited(e -> btnVideo.setStyle(buttonStyle));
        
        navBar.getChildren().addAll(btnRepository, btnEditor, btnVideo);
        root.setTop(navBar);

        // 2. Navigation actions
        btnVideo.setOnAction(e -> {
            root.setCenter(videoModule.getLayout());
            updateButtonStyles(btnVideo, btnRepository, btnEditor);
        });
        
        btnEditor.setOnAction(e -> {
            root.setCenter(imageEditorModule.getLayout());
            updateButtonStyles(btnEditor, btnRepository, btnVideo);
        });
        
        btnRepository.setOnAction(e -> {
            // Placeholder for Image Repository module
            root.setCenter(createPlaceholderView("Image Repository"));
            updateButtonStyles(btnRepository, btnEditor, btnVideo);
        });
        
        // 3. Initialize with Video module visible
        root.setCenter(videoModule.getLayout());
    }
    
    private void updateButtonStyles(Button activeButton, Button... otherButtons) {
        String buttonStyle = "-fx-background-color: #555; -fx-text-fill: white; -fx-padding: 10 20; -fx-border-radius: 5; -fx-background-radius: 5;";
        String activeButtonStyle = "-fx-background-color: #0078D7; -fx-text-fill: white; -fx-padding: 10 20; -fx-border-radius: 5; -fx-background-radius: 5;";
        
        activeButton.setStyle(activeButtonStyle);
        for (Button button : otherButtons) {
            button.setStyle(buttonStyle);
        }
    }
    
    private javafx.scene.layout.StackPane createPlaceholderView(String moduleName) {
        javafx.scene.layout.StackPane placeholder = new javafx.scene.layout.StackPane();
        javafx.scene.control.Label label = new javafx.scene.control.Label(moduleName + " - Coming Soon");
        label.setStyle("-fx-text-fill: white; -fx-font-size: 24px;");
        placeholder.getChildren().add(label);
        placeholder.setStyle("-fx-background-color: #121212;");
        return placeholder;
    }

    public BorderPane getContainer() {
        return root;
    }
    
    public void dispose() {
        if (imageEditorModule != null) {
            imageEditorModule.dispose();
        }
    }
}