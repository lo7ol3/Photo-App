package mm.prog.project;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainDashboard {
    private BorderPane root;

    public MainDashboard() {
        root = new BorderPane();
        
        // 1. Navigation Bar (Requirement 2.1)
        HBox navBar = new HBox(20);
        navBar.setPadding(new Insets(15));
        navBar.setStyle("-fx-background-color: #333;");
        
        Button btnRepository = new Button("Image Repository");
        Button btnEditor = new Button("DIP Editor");
        Button btnVideo = new Button("Video Generator"); // Your Part
        
        navBar.getChildren().addAll(btnRepository, btnEditor, btnVideo);
        root.setTop(navBar);

        // 2. Default View (Your Video Module)
        VideoGeneratorModule myVideoModule = new VideoGeneratorModule();
        
        // When you click "Video Generator", it shows your part in the center
        btnVideo.setOnAction(e -> root.setCenter(myVideoModule.getLayout()));
        
        // Initialize with your module visible
        root.setCenter(myVideoModule.getLayout());
    }

    public BorderPane getContainer() {
        return root;
    }
}