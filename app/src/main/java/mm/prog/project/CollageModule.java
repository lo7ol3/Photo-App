package mm.prog.project;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import java.io.File;
import java.util.List;
import java.util.Random;

public class CollageModule {

    private BorderPane rootLayout;
    private VBox mainLayout;
    private Pane collageArea;
    private ComboBox<String> styleCombo;
    private ComboBox<String> shapeCombo;
    private Random random;
    private RepositoryModule repo;

    // Define the canvas size
    private final double CANVAS_WIDTH = 600;
    private final double CANVAS_HEIGHT = 600;

    public CollageModule(RepositoryModule repo) {
        this.repo = repo;
        this.random = new Random();

        // --- NEW: Adjusted Title Header ---
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #121212;");

        Label titleLabel = new Label("Image Mosaic");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        HBox headerBox = new HBox(titleLabel);
        // Matches the 22px top padding for perfect horizontal alignment across tabs
        headerBox.setPadding(new Insets(22, 15, 10, 20));
        rootLayout.setTop(headerBox);

        // --- Original Workspace Setup ---
        mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(10, 20, 20, 20)); // Adjusted top padding slightly since header has padding
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: #121212;"); // Match dark theme of the root

        // --- 1. UI Controls ---
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);

        styleCombo = new ComboBox<>(FXCollections.observableArrayList("Scrapbook", "Masonry Grid"));
        styleCombo.setValue("Scrapbook");

        shapeCombo = new ComboBox<>(FXCollections.observableArrayList("Rectangle", "Circle", "Star"));
        shapeCombo.setValue("Rectangle");

        Button generateBtn = new Button("Generate Mosaic");
        generateBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
        generateBtn.setOnAction(e -> generateCollage());

        Label styleLabel = new Label("Style:");
        styleLabel.setTextFill(Color.WHITE);
        Label shapeLabel = new Label("Shape:");
        shapeLabel.setTextFill(Color.WHITE);

        controls.getChildren().addAll(styleLabel, styleCombo, shapeLabel, shapeCombo, generateBtn);

        // --- 2. Collage Display Area ---
        collageArea = new Pane();
        collageArea.setMaxSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        collageArea.setMinSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        collageArea.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444; -fx-border-width: 2px;");

        mainLayout.getChildren().addAll(controls, collageArea);

        // Place the main layout in the center of the root layout
        rootLayout.setCenter(mainLayout);
    }

    public Node getView() {
        // Return the rootLayout now, which includes the title and the workspace
        return rootLayout;
    }

    private void generateCollage() {
        collageArea.getChildren().clear(); // Clear old collage
        collageArea.setClip(null); // Reset previous shapes

        // Fetch ONLY the highlighted images (yellow border) from the Repo tab
        List<String> targetImagePaths = repo.getSelectedImagePaths();

        // Fallback: If no explicit yellow border selections exist, safely process everything in the library
        if (targetImagePaths == null || targetImagePaths.isEmpty()) {
            targetImagePaths = repo.getAllImagePaths();
        }

        if (targetImagePaths == null || targetImagePaths.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No images available! Please load or select images first.");
            alert.showAndWait();
            return;
        }

        String selectedStyle = styleCombo.getValue();

        // --- Apply Logic based on Style ---
        if ("Scrapbook".equals(selectedStyle)) {
            // Simulate scattered photos for the scrapbook effect based on available subset size
            int loops = Math.min(60, targetImagePaths.size() * 3);
            for (int i = 0; i < loops; i++) {
                String path = targetImagePaths.get(random.nextInt(targetImagePaths.size()));
                try {
                    File file = new File(path);
                    Image img = new Image(file.toURI().toURL().toExternalForm(), true);

                    ImageView photo = createPolaroidView(img);

                    // Random position and rotation
                    photo.setLayoutX(random.nextDouble() * (CANVAS_WIDTH - 100));
                    photo.setLayoutY(random.nextDouble() * (CANVAS_HEIGHT - 100));
                    photo.setRotate(random.nextInt(40) - 20); // -20 to 20 degrees

                    collageArea.getChildren().add(photo);
                } catch (Exception ex) {
                    System.err.println("Rendering failure on scrapbook object: " + ex.getMessage());
                }
            }
        } else {
            // Masonry Grid layout
            int cols = 5;
            int rows = 5;
            double cellW = CANVAS_WIDTH / cols;
            double cellH = CANVAS_HEIGHT / rows;
            int imgIndex = 0;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    String path = targetImagePaths.get(imgIndex % targetImagePaths.size());
                    try {
                        File file = new File(path);
                        Image img = new Image(file.toURI().toURL().toExternalForm(), cellW, cellH, false, true);

                        ImageView photo = new ImageView(img);
                        photo.setFitWidth(cellW);
                        photo.setFitHeight(cellH);
                        photo.setPreserveRatio(false); // Force it to fill the grid square

                        photo.setLayoutX(c * cellW);
                        photo.setLayoutY(r * cellH);

                        collageArea.getChildren().add(photo);
                        imgIndex++;
                    } catch (Exception ex) {
                        System.err.println("Rendering failure on grid item: " + ex.getMessage());
                    }
                }
            }
        }

        // --- 3. Apply Shape Mask ---
        applyShapeMask();
    }

    // Helper: Creates a photo view for the Scrapbook layout
    private ImageView createPolaroidView(Image img) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(100);
        iv.setFitHeight(100);
        iv.setPreserveRatio(true);
        return iv;
    }

    // Helper: Masks the entire Collage Area to form the selected shape
    private void applyShapeMask() {
        String shape = shapeCombo.getValue();
        double centerX = CANVAS_WIDTH / 2;
        double centerY = CANVAS_HEIGHT / 2;

        if ("Circle".equals(shape)) {
            Circle circleMask = new Circle(centerX, centerY, CANVAS_WIDTH / 2.2);
            collageArea.setClip(circleMask);
        }
        else if ("Star".equals(shape)) {
            Polygon starMask = createStarPolygon(centerX, centerY, CANVAS_WIDTH / 2.2, CANVAS_WIDTH / 5.0, 5);
            collageArea.setClip(starMask);
        }
        // Rectangle requires no clip (default)
    }

    // Helper: Mathematical generation of a Star shape for the mask
    private Polygon createStarPolygon(double centerX, double centerY, double outerRadius, double innerRadius, int numPoints) {
        Polygon polygon = new Polygon();
        double angle = Math.PI / numPoints;

        for (int i = 0; i < 2 * numPoints; i++) {
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = centerX + Math.cos(i * angle - Math.PI / 2) * r;
            double y = centerY + Math.sin(i * angle - Math.PI / 2) * r;
            polygon.getPoints().addAll(x, y);
        }
        return polygon;
    }
}