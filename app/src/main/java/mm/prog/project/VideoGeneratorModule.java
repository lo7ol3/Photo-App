package mm.prog.project;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

// --- JAVA AWT GRAPHICS & FONTS FOR HIGH ACCURACY RENDERING ---
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

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
    private Slider durationSlider;
    private Slider seekSlider;
    private Label statusLabel;
    private Label timeLabel;
    private TextField inputTextField;
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
        layout = new VBox(0); // Set spacing to 0 to control header position precisely
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #121212;");

        // --- NEW ALIGNED HEADER ---
        HBox header = createHeader("Video Generator");
        layout.getChildren().add(header);

        // Functional buttons row
        Button btnGenerate = new Button("Generate Video from Favourites");
        btnGenerate.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 18;");
        btnGenerate.setOnAction(e -> generateAndPreviewVideo());

        btnSaveVideo = new Button("Save Video");
        btnSaveVideo.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 18;");
        btnSaveVideo.setDisable(true);
        btnSaveVideo.setOnAction(e -> saveGeneratedVideo());

        HBox headerRow = new HBox(12, btnGenerate, btnSaveVideo);
        headerRow.setAlignment(Pos.CENTER);
        headerRow.setPadding(new Insets(10, 0, 10, 0));

        // Video preview
        mediaView = new MediaView();
        StackPane videoContainer = new StackPane(mediaView);
        videoContainer.setPrefSize(420, 260);
        videoContainer.setMaxSize(420, 260);
        videoContainer.setStyle("-fx-background-color: black; -fx-border-color: #333;");
        mediaView.setFitWidth(420);
        mediaView.setFitHeight(260);
        mediaView.setPreserveRatio(true);

        // Subtitle input
        VBox inputContainer = new VBox(5);
        inputContainer.setAlignment(Pos.CENTER);
        Label inputLabel = new Label("Enter Text for Video Overlay:");
        inputLabel.setStyle("-fx-text-fill: #aaa;");
        inputTextField = new TextField();
        inputTextField.setPromptText("Type here...");
        inputTextField.setMaxWidth(360);
        inputContainer.getChildren().addAll(inputLabel, inputTextField);

        // Playback controls
        HBox playbackBox = new HBox(15);
        playbackBox.setAlignment(Pos.CENTER);
        Button btnPlay = new Button("▶ Play");
        Button btnPause = new Button("⏸ Pause");
        btnPlay.setOnAction(e -> { if(mediaPlayer != null) mediaPlayer.play(); });
        btnPause.setOnAction(e -> { if(mediaPlayer != null) mediaPlayer.pause(); });
        playbackBox.getChildren().addAll(btnPlay, btnPause);

        // Seek bar
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

        // Duration control
        VBox sliderBox = new VBox(5);
        sliderBox.setAlignment(Pos.CENTER);
        Label lblDuration = new Label("Seconds per image:");
        lblDuration.setStyle("-fx-text-fill: #aaa;");
        durationSlider = new Slider(1, 10, 3);
        durationSlider.setMaxWidth(360);
        durationSlider.setShowTickLabels(true);
        durationSlider.setShowTickMarks(true);
        durationSlider.setMajorTickUnit(1);
        durationSlider.setMinorTickCount(0);
        durationSlider.setSnapToTicks(true);
        sliderBox.getChildren().addAll(lblDuration, durationSlider);

        statusLabel = new Label("Ready to synthesize");
        statusLabel.setStyle("-fx-text-fill: #666;");

        // Main content container (no card styling)
        VBox contentContainer = new VBox(18, headerRow, videoContainer, playbackBox, seekBox, inputContainer, sliderBox, statusLabel);
        contentContainer.setPadding(new Insets(0, 18, 18, 18));
        contentContainer.setAlignment(Pos.CENTER);
        contentContainer.setMaxWidth(760);
        contentContainer.setFillWidth(false);

        layout.getChildren().add(contentContainer);
    }

    private HBox createHeader(String titleText) {
        Label title = new Label(titleText);
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox headerBox = new HBox(title);
        // Matches your Repository and DIP Editor headers perfectly
        headerBox.setPadding(new Insets(22, 15, 10, 20));
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setMaxWidth(Double.MAX_VALUE);
        return headerBox;
    }

    private void generateAndPreviewVideo() {
        List<String> imagesToUse = getCurrentFavoriteImages();
        if (imagesToUse.isEmpty()) {
            statusLabel.setText("Error: No images have been favourited in the Repository yet!");
            return;
        }

        statusLabel.setText("Generating preview video...");
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
        statusLabel.setText("Video generated. Previewing now.");
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
        int fps = 24;
        int secondsPerImage = (int) durationSlider.getValue();
        int framesPerImage = Math.max(1, fps * secondsPerImage);
        String userPoem = inputTextField.getText().trim();

        Mat sampleFrame = Imgcodecs.imread(imagesToUse.get(0));
        if (sampleFrame.empty()) return false;

        Size frameSize = new Size(sampleFrame.width(), sampleFrame.height());
        int fourcc = VideoWriter.fourcc('m', 'p', '4', 'v');
        VideoWriter writer = new VideoWriter(outputFileName, fourcc, fps, frameSize, true);

        if (!writer.isOpened()) return false;

        for (int i = 0; i < imagesToUse.size(); i++) {
            String path = imagesToUse.get(i);
            Mat rawFrame = Imgcodecs.imread(path);
            if (rawFrame.empty()) continue;

            Mat frame = new Mat();
            Imgproc.resize(rawFrame, frame, frameSize);
            rawFrame.release();

            Mat overlay = frame.clone();
            int boxHeight = (int) (frame.rows() * 0.14);
            if (boxHeight < 35) boxHeight = 35;

            Imgproc.rectangle(overlay,
                    new Point(0, frame.rows() - boxHeight),
                    new Point(frame.cols(), frame.rows()),
                    new Scalar(0, 0, 0), Imgproc.FILLED);

            Core.addWeighted(overlay, 0.50, frame, 0.50, 0, frame);
            overlay.release();

            if (!userPoem.isBlank()) {
                BufferedImage bimg = matToBufferedImage(frame);
                Graphics2D g2d = bimg.createGraphics();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int fontSize = (int) (frame.rows() * 0.055);
                if (fontSize < 16) fontSize = 16;

                Font font = new Font("SansSerif", Font.BOLD, fontSize);
                g2d.setFont(font);
                g2d.setColor(Color.WHITE);

                FontMetrics fm = g2d.getFontMetrics();
                int textX = (int) (frame.cols() * 0.03);
                int textY = frame.rows() - (boxHeight / 2) + (fm.getAscent() / 3);

                g2d.drawString(userPoem, textX, textY);
                g2d.dispose();

                Mat newFrame = bufferedImageToMat(bimg);
                frame.release();
                frame = newFrame;
            }

            for (int f = 0; f < framesPerImage; f++) {
                writer.write(frame);
            }
            frame.release();
        }

        writer.release();
        return true;
    }

    private BufferedImage matToBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        if (matrix.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer);

        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }

    private Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), org.opencv.core.CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
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

            mediaPlayer.setOnError(() -> {
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