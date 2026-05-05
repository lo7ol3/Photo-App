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

public class TransformationModule {
    private BorderPane layout;
    private ImageView imageView = new ImageView();
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    
    private WritableImage overlay;
    private PixelWriter writer;
    private ImageView overlayView;
    private boolean[][] mask;
    
    private int startX, startY, lastX, lastY;
    private int maskWidth, maskHeight;
    private boolean eyedropperMode = false;
    private Color targetColor = Color.RED;

    public TransformationModule() {
        createUI();
    }

    private void createUI() {
        layout = new BorderPane();
        layout.setStyle("-fx-background-color: #121212;");

        // Center Area: StackPane for Image + Lasso Drawing
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(600);
        overlayView = new ImageView();
        StackPane stack = new StackPane(imageView, overlayView);
        stack.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #333;");
        
        setupLassoEvents(stack);

        // Right Panel: Controls
        VBox rightPanel = new VBox(15);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setPrefWidth(280);
        rightPanel.setStyle("-fx-background-color: #1a1a1a;");

        Button loadBtn = new Button("Upload Image");
        loadBtn.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-pref-width: 240;");
        loadBtn.setOnAction(e -> loadImage());

        // Sections
        VBox resizeBox = createSection("Resize", createResizeControls());
        VBox rotateBox = createSection("Rotation", createRotationControls());
        VBox extractBox = createSection("Object Extraction", createExtractionControls());

        rightPanel.getChildren().addAll(loadBtn, resizeBox, rotateBox, extractBox);
        
        layout.setCenter(stack);
        layout.setRight(rightPanel);
    }

    private void setupLassoEvents(StackPane stack) {
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
            drawLine(lastX, lastY, x, y);
            lastX = x;
            lastY = y;
        });

        stack.setOnMouseReleased(e -> {
            if (imageView.getImage() == null) return;
            drawLine(lastX, lastY, startX, startY);
            previewSelection();
        });
    }

    private VBox createResizeControls() {
        TextField wField = new TextField(); wField.setPromptText("Width");
        TextField hField = new TextField(); hField.setPromptText("Height");
        Button apply = new Button("Apply Resize");
        apply.setOnAction(e -> {
            int w = Integer.parseInt(wField.getText());
            int h = Integer.parseInt(hField.getText());
            BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = resized.createGraphics();
            g.drawImage(originalImage, 0, 0, w, h, null);
            g.dispose();
            processedImage = resized;
            imageView.setImage(SwingFXUtils.toFXImage(resized, null));
        });
        return new VBox(5, new Label("Width:"), wField, new Label("Height:"), hField, apply);
    }

    private VBox createRotationControls() {
        Button rotPlus = new Button("Rotate +90°");
        Button rotMinus = new Button("Rotate -90°");
        rotPlus.setOnAction(e -> imageView.setRotate(imageView.getRotate() + 90));
        rotMinus.setOnAction(e -> imageView.setRotate(imageView.getRotate() - 90));
        return new VBox(5, rotPlus, rotMinus);
    }

    private VBox createExtractionControls() {
        Button eyedropBtn = new Button("Eyedropper Mode");
        Button extractBtn = new Button("Extract Object");
        Button clearBtn = new Button("Clear Selection");
        
        eyedropBtn.setOnAction(e -> eyedropperMode = true);
        extractBtn.setOnAction(e -> extractUsingMask());
        clearBtn.setOnAction(e -> clearMask());

        imageView.setOnMouseClicked(e -> {
            if (!eyedropperMode || originalImage == null) return;
            int x = toX(e.getX());
            int y = toY(e.getY());
            if (x < 0 || y < 0 || x >= originalImage.getWidth() || y >= originalImage.getHeight()) return;
            java.awt.Color c = new java.awt.Color(originalImage.getRGB(x, y));
            targetColor = Color.rgb(c.getRed(), c.getGreen(), c.getBlue());
            eyedropperMode = false;
        });

        return new VBox(5, eyedropBtn, extractBtn, clearBtn);
    }

    // Logic Methods (Adapted from your Module 3)
    private void loadImage() {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(layout.getScene().getWindow());
        if (file != null) {
            try {
                originalImage = ImageIO.read(file);
                processedImage = originalImage;
                imageView.setImage(SwingFXUtils.toFXImage(originalImage, null));
                maskWidth = (int) imageView.getBoundsInLocal().getWidth();
                maskHeight = (int) (maskWidth * originalImage.getHeight() / originalImage.getWidth());
                overlay = new WritableImage(maskWidth, maskHeight);
                writer = overlay.getPixelWriter();
                overlayView.setImage(overlay);
                mask = new boolean[maskWidth][maskHeight];
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void drawLine(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            for (int i = -2; i <= 2; i++) {
                for (int j = -2; j <= 2; j++) {
                    int nx = x1 + i; int ny = y1 + j;
                    if (nx >= 0 && ny >= 0 && nx < maskWidth && ny < maskHeight) {
                        writer.setColor(nx, ny, Color.RED);
                        mask[nx][ny] = true;
                    }
                }
            }
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }

    private void extractUsingMask() {
        if (originalImage == null) return;
        boolean[][] region = fillRegion(mask);
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        
        java.awt.Color target = new java.awt.Color((float)targetColor.getRed(), (float)targetColor.getGreen(), (float)targetColor.getBlue());

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int mx = x * maskWidth / w;
                int my = y * maskHeight / h;
                if (region[mx][my]) {
                    java.awt.Color pixel = new java.awt.Color(originalImage.getRGB(x, y));
                    int diff = Math.abs(pixel.getRed() - target.getRed()) + Math.abs(pixel.getGreen() - target.getGreen()) + Math.abs(pixel.getBlue() - target.getBlue());
                    if (diff < 180) result.setRGB(x, y, originalImage.getRGB(x, y));
                    else result.setRGB(x, y, 0x00000000);
                }
            }
        }
        processedImage = result;
        imageView.setImage(SwingFXUtils.toFXImage(result, null));
    }

    private boolean[][] fillRegion(boolean[][] mask) {
        int w = maskWidth; int h = maskHeight;
        boolean[][] filled = new boolean[w][h];
        boolean[][] visited = new boolean[w][h];
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.add(new int[]{0, 0});
        visited[0][0] = true;

        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int x = p[0], y = p[1];
            filled[x][y] = true;
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) {
                int nx = x + d[0], ny = y + d[1];
                if (nx >= 0 && ny >= 0 && nx < w && ny < h && !visited[nx][ny] && !mask[nx][ny]) {
                    visited[nx][ny] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
        }
        boolean[][] region = new boolean[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) { region[x][y] = !filled[x][y]; }
        }
        return region;
    }

    private void previewSelection() {
        if (mask == null || originalImage == null) return;
        boolean[][] region = fillRegion(mask);
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        WritableImage preview = new WritableImage(w, h);
        PixelReader reader = imageView.getImage().getPixelReader();
        PixelWriter pw = preview.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int mx = x * maskWidth / w;
                int my = y * maskHeight / h;
                Color c = reader.getColor(x, y);
                if (region[mx][my]) pw.setColor(x, y, c.interpolate(Color.BLUE, 0.4));
                else pw.setColor(x, y, c);
            }
        }
        imageView.setImage(preview);
    }

    private void clearMask() {
        mask = new boolean[maskWidth][maskHeight];
        overlay = new WritableImage(maskWidth, maskHeight);
        writer = overlay.getPixelWriter();
        overlayView.setImage(overlay);
        imageView.setImage(SwingFXUtils.toFXImage(originalImage, null));
    }

    private VBox createSection(String title, javafx.scene.Node content) {
        Label l = new Label(title);
        l.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        VBox box = new VBox(10, l, content);
        box.setStyle("-fx-border-color: #444; -fx-padding: 10; -fx-border-radius: 5;");
        return box;
    }

    private int toX(double x) { return (int) (x * imageView.getImage().getWidth() / imageView.getBoundsInLocal().getWidth()); }
    private int toY(double y) { return (int) (y * imageView.getImage().getHeight() / imageView.getBoundsInLocal().getHeight()); }

    public BorderPane getLayout() { return layout; }
}