import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import model.SharedData;

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
    private boolean eyedropperMode = false;
    private Color targetColor = Color.RED;
    private Label statusLabel;

    public TransformationModule() {
        createUI();
        // Register instance hooks into global event lifecycle
        SharedData.registerListener(this);
    }

    private void createUI() {
        layout = new BorderPane();
        layout.setStyle("-fx-background-color: #121212;");

        // Center Display Workspace Layout
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600);
        overlayView.setPreserveRatio(true);
        overlayView.setFitWidth(600);
        
        StackPane stack = new StackPane(imageView, overlayView);
        stack.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333;");
        layout.setCenter(stack);

        // --- ATTACH LASSO MOUSE INTERFACES ---
        stack.setOnMousePressed(e -> { 
            if (imageView.getImage() == null) return;
            startX = toX(e.getX());
            startY = toY(e.getY());
            lastX = startX;
            lastY = startY;
        });
        
        stack.setOnMouseDragged(e -> {
            if (imageView.getImage() == null) return;
            int x = toX(e.getX());
            int y = toY(e.getY());
            
            drawLine(lastX, lastY, x, y); // Safe point bridging
            lastX = x;
            lastY = y;
        });
        
        stack.setOnMouseReleased(e -> {
            if (imageView.getImage() == null) return;
            // Complete bounding ring closure tracking
            drawLine(lastX, lastY, startX, startY);
            previewSelection();
        });

        // Sidebar Configuration Panel
        VBox controls = new VBox(15);
        controls.setPadding(new Insets(15));
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
        resizeBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
        
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
                statusLabel.setText("Image resized to " + w + "x" + h);
            } catch (NumberFormatException nfe) {
                statusLabel.setText("Invalid dimensions typed.");
            }
        });
        
        HBox resizeInputs = new HBox(5, new Label("W:"), widthField, new Label("H:"), heightField);
        resizeInputs.setAlignment(Pos.CENTER_LEFT);
        controls.getChildren().add(createSection("Resize Frame Matrix", new VBox(8, resizeInputs, resizeBtn)));

        // Feature Block 2: Spatial Canvas Rotations
        Button rotatePlus = new Button("Rotate +90°");
        Button rotateMinus = new Button("Rotate -90°");
        HBox rotationRow = new HBox(10, rotatePlus, rotateMinus);
        
        rotatePlus.setOnAction(e -> imageView.setRotate(imageView.getRotate() + 90));
        rotateMinus.setOnAction(e -> imageView.setRotate(imageView.getRotate() - 90));
        controls.getChildren().add(createSection("Spatial Rotation", rotationRow));

        // Feature Block 3: Spatial Transformations (Translation)
        TextField xField = new TextField("0");
        xField.setPrefWidth(60);
        TextField yField = new TextField("0");
        yField.setPrefWidth(60);
        Button moveBtn = new Button("Move Image");
        
        moveBtn.setOnAction(e -> {
            try {
                double x = Double.parseDouble(xField.getText());
                double y = Double.parseDouble(yField.getText());
                imageView.setTranslateX(x);
                imageView.setTranslateY(y);
            } catch (NumberFormatException nfe) {
                statusLabel.setText("Invalid coordinate entry.");
            }
        });
        
        HBox translationInputs = new HBox(5, new Label("X:"), xField, new Label("Y:"), yField);
        translationInputs.setAlignment(Pos.CENTER_LEFT);
        controls.getChildren().add(createSection("Translation Matrix", new VBox(8, translationInputs, moveBtn)));

        // Feature Block 4: Target Object Selection & Isolation Extraction Suite
        Button eyedropBtn = new Button("Color Picker Mode");
        Button extractBtn = new Button("Extract Object");
        Button btnClear = new Button("🧹 Clear Selection");
        Button saveBtn = new Button("💾 Save Transformed");
        
        extractBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        
        eyedropBtn.setOnAction(e -> {
            eyedropperMode = true;
            statusLabel.setText("Click on image display to pick color...");
        });

        // Handle Color selection directly over the raw content view
        imageView.setOnMouseClicked(e -> { 
            if (!eyedropperMode || originalImage == null) return;
            
            int x = toX(e.getX());
            int y = toY(e.getY());
            
            if (x < 0 || y < 0 || x >= originalImage.getWidth() || y >= originalImage.getHeight())
                return;
            
            java.awt.Color c = new java.awt.Color(originalImage.getRGB(x, y));
            targetColor = new Color(c.getRed() / 255.0, c.getGreen() / 255.0, c.getBlue() / 255.0, 1.0);
            
            statusLabel.setText("Picked Color Target successfully!");
            eyedropperMode = false;
        });

        extractBtn.setOnAction(e -> extractUsingMask());
        btnClear.setOnAction(e -> clearMask());
        saveBtn.setOnAction(e -> saveTransformedImage());
        
        VBox segmentationControls = new VBox(8, 
            new Label("Trace selection outline on layout."),
            eyedropBtn, extractBtn, btnClear, saveBtn
        );
        controls.getChildren().add(createSection("Object Extraction Bounding", segmentationControls));

        // Bottom Dashboard Log Tracker Status
        statusLabel = new Label("No active image. Load via Hub.");
        statusLabel.setStyle("-fx-text-fill: #aaa; -fx-wrap-text: true;");
        controls.getChildren().add(statusLabel);

        layout.setRight(controls);
    }

    @Override
    public void onImageChanged(String newPath) {
        if (newPath != null && !newPath.isEmpty()) {
            try {
                File file = new File(newPath);
                originalImage = ImageIO.read(file);
                
                if (originalImage != null) {
                    processedImage = originalImage;
                    imageView.setImage(new javafx.scene.image.Image(file.toURI().toString()));
                    
                    // Force the coordinate spaces to match the visual canvas constraints safely
                    maskWidth = 600; 
                    maskHeight = (int) (600.0 * originalImage.getHeight() / originalImage.getWidth());

                    clearMask();
                    statusLabel.setText("Workspace loaded: " + file.getName());
                }
            } catch (Exception ex) {
                statusLabel.setText("Failed processing selected image framework configuration.");
                System.err.println("Transformation image loading failure: " + ex.getMessage());
            }
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
        statusLabel.setText("Selection mask traces cleared cleanly.");
    }

    private void drawLine(int x1, int y1, int x2, int y2) {
        if (writer == null) return;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            // Generates thick selection profile lines
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
        Image currentViewImg = imageView.getImage();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int mx = x * maskWidth / w;
                int my = y * maskHeight / h;
                
                Color originalColor = currentViewImg.getPixelReader().getColor(x, y);

                if (region[mx][my]) {
                    // Highlights selected region with a blue overlay tint
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

        boolean[][] region = fillRegion(mask);
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        java.awt.Color target = new java.awt.Color(
            (float) targetColor.getRed(),
            (float) targetColor.getGreen(),
            (float) targetColor.getBlue()
        );

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int mx = x * maskWidth / w;
                int my = y * maskHeight / h;

                if (region[mx][my]) {
                    java.awt.Color pixel = new java.awt.Color(originalImage.getRGB(x, y));
                    int diff = Math.abs(pixel.getRed() - target.getRed())
                             + Math.abs(pixel.getGreen() - target.getGreen())
                             + Math.abs(pixel.getBlue() - target.getBlue());

                    if (diff < 180) { // Extraction similarity profile threshold limits
                        result.setRGB(x, y, originalImage.getRGB(x, y));
                    } else {
                        result.setRGB(x, y, 0x00000000); // Set transparent
                    }
                } else {
                    result.setRGB(x, y, 0x00000000); // Non-lasso zones turn alpha transparent
                }
            }
        }

        processedImage = result;
        imageView.setImage(SwingFXUtils.toFXImage(result, null));
        statusLabel.setText("Target object extracted!");
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
        chooser.setInitialFileName("transformed_output.png");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG File Image System (*.png)", "*.png"));
        
        File file = chooser.showSaveDialog(layout.getScene().getWindow());
        if (file != null) {
            try {
                ImageIO.write(processedImage, "png", file);
                statusLabel.setText("Output modifications saved to disk successfully.");
            } catch (Exception e) {
                statusLabel.setText("Failed saving operations process.");
                e.printStackTrace();
            }
        }
    }

    private int toX(double x) {
        if (imageView.getImage() == null) return 0;
        return (int) (x * imageView.getImage().getWidth() / imageView.getBoundsInLocal().getWidth());
    }

    private int toY(double y) {
        if (imageView.getImage() == null) return 0;
        return (int) (y * imageView.getImage().getHeight() / imageView.getBoundsInLocal().getHeight());
    }

    private VBox createSection(String title, javafx.scene.Node content) {
        Label l = new Label(title);
        l.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        VBox box = new VBox(10, l, content);
        box.setStyle("-fx-border-color: #444; -fx-padding: 10; -fx-border-radius: 5;");
        return box;
    }
    
    public BorderPane getLayout() {
        return this.layout;
    }
}
