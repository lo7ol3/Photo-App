package mm.prog.project;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TransformationModule implements SharedData.ImageChangeListener {
    private BorderPane layout;
    private ImageView imageView = new ImageView();
    private BufferedImage originalImage;
    private BufferedImage processedImage;

    private WritableImage overlay;
    private PixelWriter writer;
    private ImageView overlayView = new ImageView();
    private boolean[][] mask;

    // Core drawing coordinates
    private int startX, startY, lastX, lastY;
    private int maskWidth, maskHeight;

    // State management for Lasso Tool
    private boolean lassoMode = false;
    private Button lassoBtn;
    private Label statusLabel;
    private StackPane displayStack;

    public TransformationModule() {
        createUI();
        // Register instance hooks into global event lifecycle
        SharedData.addImageChangeListener(this);
    }

    private void createUI() {
        layout = new BorderPane();
        layout.setStyle("-fx-background-color: #121212;");

        // --- Adjusted Title Header ---
        Label titleLabel = new Label("Image Transformation");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox headerBox = new HBox(titleLabel);
        // Adjusted top padding to horizontally align with the Image Repository title
        headerBox.setPadding(new Insets(22, 15, 10, 20));
        layout.setTop(headerBox);

        // Center Display Workspace Layout
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600);

        overlayView.setPreserveRatio(true);
        overlayView.setFitWidth(600);
        overlayView.setPickOnBounds(true);

        displayStack = new StackPane(imageView, overlayView);
        displayStack.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333;");

        // Wrap display stack to keep it top-center aligned below the header
        VBox centerContainer = new VBox(displayStack);
        centerContainer.setAlignment(Pos.TOP_CENTER);
        centerContainer.setPadding(new Insets(10, 20, 20, 20));
        layout.setCenter(centerContainer);

        // --- ATTACH LASSO MOUSE INTERFACES ---
        overlayView.setOnMousePressed(e -> {
            if (imageView.getImage() == null || !lassoMode) return;
            startX = (int) e.getX();
            startY = (int) e.getY();
            lastX = startX;
            lastY = startY;
        });

        overlayView.setOnMouseDragged(e -> {
            if (imageView.getImage() == null || !lassoMode) return;
            int x = (int) e.getX();
            int y = (int) e.getY();
            drawLine(lastX, lastY, x, y);
            lastX = x;
            lastY = y;
        });

        overlayView.setOnMouseReleased(e -> {
            if (imageView.getImage() == null || !lassoMode) return;
            drawLine(lastX, lastY, startX, startY);
            previewSelection();
        });

        // Sidebar Configuration Panel
        VBox controls = new VBox(15);
        controls.setPadding(new Insets(20, 15, 15, 15));
        controls.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-width: 0 0 0 1;");
        controls.setPrefWidth(260);

        Label lblTitle = new Label("Transformation Controls");
        lblTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        controls.getChildren().add(lblTitle);

        // Feature Block 1: Scaling/Resize Control Matrix
        TextField widthField = new TextField();
        widthField.setPromptText("Width");
        widthField.setPrefWidth(70);
        TextField heightField = new TextField();
        heightField.setPromptText("Height");
        heightField.setPrefWidth(70);
        Button resizeBtn = new Button("Apply Resize");
        resizeBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white;");

        resizeBtn.setOnAction(e -> {
            if (originalImage == null) return;
            try {
                int w = Integer.parseInt(widthField.getText());
                int h = Integer.parseInt(heightField.getText());

                BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g = resized.createGraphics();
                g.drawImage(originalImage, 0, 0, w, h, null);
                g.dispose();

                processedImage = resized;
                imageView.setImage(SwingFXUtils.toFXImage(resized, null));
                deactivateLassoTool();
                statusLabel.setText("Image resized to " + w + "x" + h);
            } catch (NumberFormatException nfe) {
                statusLabel.setText("Invalid dimensions typed.");
            }
        });

        // --- NEW: White Text Colors for Resize Labels ---
        Label wLbl = new Label("W:");
        wLbl.setStyle("-fx-text-fill: white;");
        Label hLbl = new Label("H:");
        hLbl.setStyle("-fx-text-fill: white;");

        HBox resizeInputs = new HBox(5, wLbl, widthField, hLbl, heightField);
        resizeInputs.setAlignment(Pos.CENTER_LEFT);
        controls.getChildren().add(createSection("Resize Frame Matrix", new VBox(8, resizeInputs, resizeBtn)));

        // Feature Block 2: Spatial Canvas Rotations
        Button rotatePlus = new Button("Rotate +90°");
        Button rotateMinus = new Button("Rotate -90°");
        HBox rotationRow = new HBox(10, rotatePlus, rotateMinus);
        String subBtnStyle = "-fx-background-color: #444; -fx-text-fill: white;";
        rotatePlus.setStyle(subBtnStyle);
        rotateMinus.setStyle(subBtnStyle);

        rotatePlus.setOnAction(e -> {
            imageView.setRotate(imageView.getRotate() + 90);
            deactivateLassoTool();
        });
        rotateMinus.setOnAction(e -> {
            imageView.setRotate(imageView.getRotate() - 90);
            deactivateLassoTool();
        });
        controls.getChildren().add(createSection("Spatial Rotation", rotationRow));

        // Feature Block 3: Spatial Transformations (Translation)
        TextField xField = new TextField("0");
        xField.setPrefWidth(60);
        TextField yField = new TextField("0");
        yField.setPrefWidth(60);
        Button moveBtn = new Button("Move Image");
        moveBtn.setStyle(subBtnStyle);

        moveBtn.setOnAction(e -> {
            try {
                double x = Double.parseDouble(xField.getText());
                double y = Double.parseDouble(yField.getText());
                imageView.setTranslateX(x);
                imageView.setTranslateY(y);
                deactivateLassoTool();
            } catch (NumberFormatException nfe) {
                statusLabel.setText("Invalid coordinate entry.");
            }
        });

        // --- NEW: White Text Colors for Translation Labels ---
        Label xLbl = new Label("X:");
        xLbl.setStyle("-fx-text-fill: white;");
        Label yLbl = new Label("Y:");
        yLbl.setStyle("-fx-text-fill: white;");

        HBox translationInputs = new HBox(5, xLbl, xField, yLbl, yField);
        translationInputs.setAlignment(Pos.CENTER_LEFT);
        controls.getChildren().add(createSection("Translation Matrix", new VBox(8, translationInputs, moveBtn)));

        // --- Feature Block 4: Target Object Selection & Isolation Extraction Suite ---
        lassoBtn = new Button("🔄 Lasso Tool");
        lassoBtn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #555;");
        lassoBtn.setPrefWidth(230);

        lassoBtn.setOnAction(e -> toggleLassoTool());

        Button extractBtn = new Button("✂ Extract Object");
        Button btnClear = new Button("🧹 Clear Selection");
        Button saveBtn = new Button("💾 Save Transformed");

        String greenBtnStyle = "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";
        extractBtn.setStyle(greenBtnStyle);
        String grayBtnStyle = "-fx-background-color: #444; -fx-text-fill: white; -fx-cursor: hand;";
        btnClear.setStyle(grayBtnStyle);
        String saveBtnStyle = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-cursor: hand;";
        saveBtn.setStyle(saveBtnStyle);

        extractBtn.setOnAction(e -> extractUsingMask());
        btnClear.setOnAction(e -> clearMask());
        saveBtn.setOnAction(e -> saveTransformedImage());

        // --- NEW: White Text Colors for Extraction Descriptions ---
        Label step1Lbl = new Label("1. Activate tool and trace shape.");
        step1Lbl.setStyle("-fx-text-fill: white;");
        Label toolsLbl = new Label("Tools:");
        toolsLbl.setStyle("-fx-text-fill: white;");

        VBox segmentationControls = new VBox(8,
                step1Lbl,
                lassoBtn,
                extractBtn,
                toolsLbl,
                btnClear, saveBtn
        );
        segmentationControls.setAlignment(Pos.CENTER_LEFT);

        controls.getChildren().add(createSection("Object Extraction Bounding", segmentationControls));

        // Bottom Dashboard Log Tracker Status
        statusLabel = new Label("Ready. Select an image from the repo.");
        statusLabel.setStyle("-fx-text-fill: #aaa; -fx-wrap-text: true;");
        controls.getChildren().add(statusLabel);

        layout.setRight(controls);
    }

    private void toggleLassoTool() {
        if (imageView.getImage() == null) {
            statusLabel.setText("Cannot activate Lasso: No image loaded.");
            return;
        }

        lassoMode = !lassoMode;

        if (lassoMode) {
            lassoBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: #555;");
            statusLabel.setText("Lasso Tool ACTIVE. Trace outline on image.");
            displayStack.setCursor(Cursor.CROSSHAIR);
        } else {
            deactivateLassoTool();
        }
    }

    private void deactivateLassoTool() {
        lassoMode = false;
        if (lassoBtn != null) {
            lassoBtn.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #555;");
        }
        if (statusLabel != null) {
            statusLabel.setText("Lasso Tool INACTIVE.");
        }
        if (displayStack != null) {
            displayStack.setCursor(Cursor.DEFAULT);
        }
    }

    @Override
    public void onImageChanged(String newPath) {
        deactivateLassoTool();

        if (newPath != null && !newPath.isEmpty()) {
            try {
                File file = new File(newPath);
                originalImage = ImageIO.read(file);

                if (originalImage != null) {
                    processedImage = originalImage;
                    imageView.setImage(new javafx.scene.image.Image(file.toURI().toString()));

                    maskWidth = 600;
                    maskHeight = (int) (600.0 * originalImage.getHeight() / originalImage.getWidth());

                    clearMask();
                    statusLabel.setText("Workspace loaded: " + file.getName());
                }
            } catch (Exception ex) {
                statusLabel.setText("Failed processing selected image.");
                System.err.println("Transformation image loading failure: " + ex.getMessage());
            }
        } else {
            imageView.setImage(null);
            processedImage = null;
            originalImage = null;
            clearMask();
        }
    }

    private void clearMask() {
        if (maskWidth <= 0 || maskHeight <= 0) return;
        mask = new boolean[maskWidth][maskHeight];
        overlay = new WritableImage(maskWidth, maskHeight);
        writer = overlay.getPixelWriter();
        overlayView.setImage(overlay);

        if (originalImage != null) {
            imageView.setImage(SwingFXUtils.toFXImage(originalImage, null));
            processedImage = originalImage;
        }
        statusLabel.setText("Selection cleared.");
    }

    private void drawLine(int x1, int y1, int x2, int y2) {
        if (writer == null) return;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    int nx = x1 + i;
                    int ny = y1 + j;

                    if (nx >= 0 && ny >= 0 && nx < maskWidth && ny < maskHeight) {
                        writer.setColor(nx, ny, Color.RED);
                        mask[nx][ny] = true;
                    }
                }
            }

            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void previewSelection() {
        if (mask == null || originalImage == null) return;

        boolean[][] region = fillRegion(mask);
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();

        WritableImage preview = new WritableImage(w, h);
        PixelWriter pw = preview.getPixelWriter();
        Image currentViewImg = SwingFXUtils.toFXImage(originalImage, null);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int mx = x * maskWidth / w;
                int my = y * maskHeight / h;

                Color originalColor = currentViewImg.getPixelReader().getColor(x, y);

                if (region[mx][my]) {
                    pw.setColor(x, y, originalColor.interpolate(Color.BLUE, 0.4));
                } else {
                    pw.setColor(x, y, originalColor);
                }
            }
        }
        imageView.setImage(preview);
    }

    private void extractUsingMask() {
        if (originalImage == null || mask == null) return;

        statusLabel.setText("Processing extraction...");

        boolean[][] region = fillRegion(mask);
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int mx = x * maskWidth / w;
                int my = y * maskHeight / h;

                if (region[mx][my]) {
                    result.setRGB(x, y, originalImage.getRGB(x, y));
                } else {
                    result.setRGB(x, y, 0x00000000);
                }
            }
        }

        deactivateLassoTool();

        processedImage = result;
        imageView.setImage(SwingFXUtils.toFXImage(result, null));
        statusLabel.setText("Object extracted! Save to disk to finalize.");
    }

    private boolean[][] fillRegion(boolean[][] mask) {
        int w = maskWidth;
        int h = maskHeight;
        boolean[][] filled = new boolean[w][h];
        boolean[][] visited = new boolean[w][h];
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();

        queue.add(new int[]{0, 0});
        visited[0][0] = true;

        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int x = p[0];
            int y = p[1];
            filled[x][y] = true;

            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nx = x + d[0];
                int ny = y + d[1];

                if (nx >= 0 && ny >= 0 && nx < w && ny < h) {
                    if (!visited[nx][ny] && !mask[nx][ny]) {
                        visited[nx][ny] = true;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }

        boolean[][] region = new boolean[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                region[x][y] = !filled[x][y];
            }
        }
        return region;
    }

    private void saveTransformedImage() {
        if (processedImage == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Transformed Output File Image");
        chooser.setInitialFileName("extracted_object.png");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG File Image System (*.png)", "*.png"));

        File file = chooser.showSaveDialog(layout.getScene().getWindow());
        if (file != null) {
            try {
                ImageIO.write(processedImage, "png", file);
                statusLabel.setText("Modifications saved successfully.");
            } catch (Exception e) {
                statusLabel.setText("Failed saving output.");
                e.printStackTrace();
            }
        }
    }

    private VBox createSection(String title, javafx.scene.Node content) {
        Label l = new Label(title);
        l.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 0 0 5 0;");
        VBox box = new VBox(0, l, content);
        box.setStyle("-fx-border-color: #444; -fx-padding: 10; -fx-border-radius: 5;");
        return box;
    }

    public BorderPane getLayout() {
        return this.layout;
    }
}