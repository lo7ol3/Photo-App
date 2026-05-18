package mm.prog.project;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.opencv.core.Core;
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
import javafx.stage.FileChooser;
import javafx.util.Duration;

public class VideoGeneratorModule {
    private VBox layout;
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    private Slider fpsSlider;
    private Slider seekSlider;
    private Label statusLabel;
    private Label timeLabel;
    private TextField inputTextField; // New Field for user input
    private Button btnSaveVideo;
    private List<String> favoriteImages = new ArrayList<>();
    private Supplier<List<String>> favoriteImageSupplier;
    private String generatedVideoPath;
    private boolean isSeeking = false;

    public VideoGeneratorModule() {
        createUI();
    }

    private java.util.function.Consumer<String> onVideoSaved;

    public void setOnVideoSaved(java.util.function.Consumer<String> handler) { this.onVideoSaved = handler; }

    private void createUI() {
        layout = new VBox(16);
        layout.setPadding(new Insets(18));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #121212;");

        Label header = new Label("Video Generator");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;");

        Button btnGenerate = new Button("Generate Video from Favourites");
        btnGenerate.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10 18;");
        btnGenerate.setOnAction(e -> generateAndPreviewVideo());

        btnSaveVideo = new Button("Save Video");
        btnSaveVideo.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-padding: 10 18;");
        btnSaveVideo.setDisable(true);
        btnSaveVideo.setOnAction(e -> saveGeneratedVideo());

        HBox headerRow = new HBox(12, header, btnGenerate, btnSaveVideo);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // 2. Video Preview Area
        mediaView = new MediaView();
        StackPane videoContainer = new StackPane(mediaView);
        videoContainer.setPrefSize(420, 260);
        videoContainer.setMaxSize(420, 260);
        videoContainer.setStyle("-fx-background-color: black; -fx-border-color: #333;");
        mediaView.setFitWidth(420);
        mediaView.setFitHeight(260);
        mediaView.setPreserveRatio(true);

        // --- NEW: User Input for Text Overlay ---
        VBox inputContainer = new VBox(5);
        inputContainer.setAlignment(Pos.CENTER);
        Label inputLabel = new Label("Enter Text/Poem for Video Overlay:");
        inputLabel.setStyle("-fx-text-fill: #aaa;");
        inputTextField = new TextField();
        inputTextField.setPromptText("Type here...");
        inputTextField.setMaxWidth(360);
        inputContainer.getChildren().addAll(inputLabel, inputTextField);

        // 3. Playback Controls
        HBox playbackBox = new HBox(15);
        playbackBox.setAlignment(Pos.CENTER);
        Button btnPlay = new Button("▶ Play");
        Button btnPause = new Button("⏸ Pause");
        btnPlay.setOnAction(e -> { if(mediaPlayer != null) mediaPlayer.play(); });
        btnPause.setOnAction(e -> { if(mediaPlayer != null) mediaPlayer.pause(); });
        playbackBox.getChildren().addAll(btnPlay, btnPause);

        seekSlider = new Slider(0, 100, 0);
        seekSlider.setMaxWidth(420);
        seekSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null && seekSlider.isValueChanging()) {
                isSeeking = true;
                mediaPlayer.seek(Duration.seconds(newVal.doubleValue()));
            }
        });

        timeLabel = new Label("00:00 / 00:00");
        timeLabel.setStyle("-fx-text-fill: #ccc;");

        HBox seekBox = new HBox(10, seekSlider, timeLabel);
        seekBox.setAlignment(Pos.CENTER);

        // 4. FPS Slider
        VBox sliderBox = new VBox(5);
        sliderBox.setAlignment(Pos.CENTER);
        Label lblFps = new Label("Adjust Frames Per Second (FPS):");
        lblFps.setStyle("-fx-text-fill: #aaa;");
        fpsSlider = new Slider(1, 60, 24);
        fpsSlider.setMaxWidth(360);
        fpsSlider.setShowTickLabels(true);
        sliderBox.getChildren().addAll(lblFps, fpsSlider);

        statusLabel = new Label("Ready to synthesize");
        statusLabel.setStyle("-fx-text-fill: #666;");

        layout.getChildren().addAll(headerRow, videoContainer, playbackBox, seekBox, inputContainer, sliderBox, statusLabel);
    }

    private void generateAndPreviewVideo() {
        List<String> imagesToUse = getCurrentFavoriteImages();
        if (imagesToUse.isEmpty()) {
            statusLabel.setText("Error: No images have been favourited in the Repository yet!");
            return;
        }

        statusLabel.setText("Generating preview video from " + imagesToUse.size() + " favourite image(s)...");
        String tempPath = createTempVideoPath();
        if (tempPath == null) {
            statusLabel.setText("Could not create temporary video file.");
            return;
        }

        if (!generateVideoFile(imagesToUse, tempPath)) {
            statusLabel.setText("Failed to generate preview video.");
            return;
        }

        generatedVideoPath = tempPath;
        btnSaveVideo.setDisable(false);
        statusLabel.setText("Video generated. Click Save Video to save it to your repository.");
        playVideo(generatedVideoPath);
    }

    private void saveGeneratedVideo() {
        if (generatedVideoPath == null || generatedVideoPath.isBlank()) {
            statusLabel.setText("Generate a video first before saving.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Video to Repository");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MP4 Files", "*.mp4"));
        File outputFile = fileChooser.showSaveDialog(layout.getScene().getWindow());
        if (outputFile == null) {
            statusLabel.setText("Save cancelled.");
            return;
        }

        String outputFileName = outputFile.getAbsolutePath();
        if (!outputFileName.toLowerCase().endsWith(".mp4")) {
            outputFileName += ".mp4";
        }

        try {
            Files.copy(Path.of(generatedVideoPath), Path.of(outputFileName), StandardCopyOption.REPLACE_EXISTING);
            statusLabel.setText("Video saved as " + outputFile.getName());
            if (onVideoSaved != null) {
                onVideoSaved.accept(outputFileName);
            }
        } catch (Exception ex) {
            statusLabel.setText("Save failed: " + ex.getMessage());
        }
    }

    private List<String> getCurrentFavoriteImages() {
        List<String> current = favoriteImageSupplier != null ? favoriteImageSupplier.get() : favoriteImages;
        return current == null ? new ArrayList<>() : new ArrayList<>(current);
    }

    private String createTempVideoPath() {
        try {
            Path tempFile = Files.createTempFile("favourites_video_preview_", ".mp4");
            tempFile.toFile().deleteOnExit();
            return tempFile.toAbsolutePath().toString();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean generateVideoFile(List<String> imagesToUse, String outputFileName) {
        int fps = (int) fpsSlider.getValue();
        String userPoem = inputTextField.getText().trim();

        Mat sampleFrame = Imgcodecs.imread(imagesToUse.get(0));
        if (sampleFrame.empty()) {
            return false;
        }

        Size frameSize = new Size(sampleFrame.width(), sampleFrame.height());
        int fourcc = VideoWriter.fourcc('m', 'p', '4', 'v');
        VideoWriter writer = new VideoWriter(outputFileName, fourcc, fps, frameSize, true);

        if (!writer.isOpened()) {
            return false;
        }

        for (int i = 0; i < imagesToUse.size(); i++) {
            String path = imagesToUse.get(i);
            Mat rawFrame = Imgcodecs.imread(path);
            if (rawFrame.empty()) continue;

            // FIX 1: Resize frame if it doesn't match sample dimensions to prevent corruption
            Mat frame = new Mat();
            Imgproc.resize(rawFrame, frame, frameSize);
            rawFrame.release();

            // Setup a semi-transparent dark overlay container at the bottom for readability
            Mat overlay = frame.clone();
            Imgproc.rectangle(overlay, new Point(0, frame.rows() - 100), new Point(frame.cols(), frame.rows()), new Scalar(0, 0, 0), Imgproc.FILLED);
            Core.addWeighted(overlay, 0.45, frame, 0.55, 0, frame);
            overlay.release();

            String overlayText = userPoem.isBlank() ? "A photo story" : userPoem;
            Imgproc.putText(frame, overlayText, new Point(30, frame.rows() - 45),
                    Imgproc.FONT_HERSHEY_DUPLEX, 1.0, new Scalar(255, 255, 255), 2);

            
            writer.write(frame);
            frame.release();
        }

        writer.release();
        return true;
    }

    public void setFavoriteImages(List<String> favoriteImages) {
        this.favoriteImages.clear();
        if (favoriteImages != null) {
            for (String path : favoriteImages) {
                if (path != null && !path.isBlank()) {
                    this.favoriteImages.add(path);
                }
            }
        }
    }

    public void setFavoriteImageSupplier(Supplier<List<String>> supplier) {
        this.favoriteImageSupplier = supplier;
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
                Duration duration = mediaPlayer.getTotalDuration();
                seekSlider.setMax(duration.toSeconds());
                seekSlider.setValue(0);
                timeLabel.setText(formatDuration(Duration.ZERO) + " / " + formatDuration(duration));
                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!isSeeking) {
                        seekSlider.setValue(newTime.toSeconds());
                        timeLabel.setText(formatDuration(newTime) + " / " + formatDuration(duration));
                    }
                });
                mediaPlayer.play();
                statusLabel.setText("Playing: " + file.getName());
            });

            seekSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (!isChanging && mediaPlayer != null) {
                    mediaPlayer.seek(Duration.seconds(seekSlider.getValue()));
                    isSeeking = false;
                }
            });

        } catch (Exception e) {
            statusLabel.setText("Media Error: " + e.getMessage());
        }
    }

    private String formatDuration(Duration duration) {
        int totalSeconds = (int) Math.floor(duration.toSeconds());
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public VBox getLayout() { return layout; }
}
