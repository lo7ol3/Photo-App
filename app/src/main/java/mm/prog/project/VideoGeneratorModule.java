package mm.prog.project;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class VideoGeneratorModule {
    private VBox layout;
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    private Slider fpsSlider;
    private Label statusLabel;
    private TextField inputTextField; // New Field for user input

    public VideoGeneratorModule() {
        createUI();
    }

    private void createUI() {
        layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #121212;");

        Label header = new Label("Video Generator");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        // 2. Video Preview Area
        mediaView = new MediaView();
        StackPane videoContainer = new StackPane(mediaView);
        videoContainer.setPrefSize(600, 350);
        videoContainer.setStyle("-fx-background-color: black; -fx-border-color: #333;");
        mediaView.setFitWidth(580);
        mediaView.setPreserveRatio(true);

        // --- NEW: User Input for Text Overlay ---
        VBox inputContainer = new VBox(5);
        inputContainer.setAlignment(Pos.CENTER);
        Label inputLabel = new Label("Enter Text/Poem for Video Overlay:");
        inputLabel.setStyle("-fx-text-fill: #aaa;");
        inputTextField = new TextField("Type here..."); // Default text
        inputTextField.setMaxWidth(400);
        inputContainer.getChildren().addAll(inputLabel, inputTextField);

        // 3. Playback Controls
        HBox playbackBox = new HBox(15);
        playbackBox.setAlignment(Pos.CENTER);
        Button btnPlay = new Button("▶ Play");
        Button btnPause = new Button("⏸ Pause");
        btnPlay.setOnAction(e -> { if(mediaPlayer != null) mediaPlayer.play(); });
        btnPause.setOnAction(e -> { if(mediaPlayer != null) mediaPlayer.pause(); });
        playbackBox.getChildren().addAll(btnPlay, btnPause);

        // 4. FPS Slider
        VBox sliderBox = new VBox(5);
        sliderBox.setAlignment(Pos.CENTER);
        Label lblFps = new Label("Adjust Frames Per Second (FPS):");
        lblFps.setStyle("-fx-text-fill: #aaa;");
        fpsSlider = new Slider(1, 60, 24);
        fpsSlider.setMaxWidth(400);
        fpsSlider.setShowTickLabels(true);
        sliderBox.getChildren().addAll(lblFps, fpsSlider);

        Button btnExport = new Button("Export Video from Favorites");
        btnExport.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-padding: 10 20;");
        btnExport.setOnAction(e -> handleExport());

        statusLabel = new Label("Ready to synthesize");
        statusLabel.setStyle("-fx-text-fill: #666;");

        layout.getChildren().addAll(header, videoContainer, playbackBox, inputContainer, sliderBox, btnExport, statusLabel);
    }

    private void handleExport() {
        // Use your specific local paths
        List<String> images = Arrays.asList(
            "C:\\Users\\imanu\\OneDrive\\Desktop\\Y3S2\\MM PROG\\MM PROG Project\\app\\src\\main\\resources\\aimi.jpeg", 
            "C:\\Users\\imanu\\OneDrive\\Desktop\\Y3S2\\MM PROG\\MM PROG Project\\app\\src\\main\\resources\\aina.jpeg", 
            "C:\\Users\\imanu\\OneDrive\\Desktop\\Y3S2\\MM PROG\\MM PROG Project\\app\\src\\main\\resources\\hadif.jpeg"
        );
        
        String outputFileName = "C:\\Users\\imanu\\OneDrive\\Desktop\\Y3S2\\MM PROG\\MM PROG Project\\app\\src\\main\\resources\\synthesized_video.mp4";
        int fps = (int) fpsSlider.getValue();
        String userPoem = inputTextField.getText(); // Get text from UI

        Mat sampleFrame = Imgcodecs.imread(images.get(0));
        Size frameSize = new Size(sampleFrame.width(), sampleFrame.height());
        int fourcc = VideoWriter.fourcc('m', 'p', '4', 'v');
        VideoWriter writer = new VideoWriter(outputFileName, fourcc, fps, frameSize, true);

        if (!writer.isOpened()) {
            statusLabel.setText("Error: Could not open VideoWriter.");
            return;
        }

        for (String path : images) {
            Mat frame = Imgcodecs.imread(path);
            if (frame.empty()) continue;

            // --- REQUIREMENT 2.3: DYNAMIC CONTENT OVERLAYS ---
            Imgproc.putText(frame, userPoem, new Point(50, 100), 
                            Imgproc.FONT_HERSHEY_COMPLEX, 1.5, new Scalar(255, 255, 255), 3);

            writer.write(frame);
        }

        writer.release();
        statusLabel.setText("Video Exported Successfully!");
        
        playVideo(outputFileName);
    }

    private void playVideo(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) return;

            Media media = new Media(file.toURI().toString());
            
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            // Important: Handle potential codec issues
            mediaPlayer.setOnError(() -> {
                System.err.println("Media Player Error: " + mediaPlayer.getError().getMessage());
                statusLabel.setText("Playback Error: " + mediaPlayer.getError().getMessage());
            });

            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
                statusLabel.setText("Playing: " + file.getName());
            });

        } catch (Exception e) {
            statusLabel.setText("Media Error: " + e.getMessage());
        }
    }

    public VBox getLayout() { return layout; }
}