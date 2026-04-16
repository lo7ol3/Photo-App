/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hellofx;

/**
 *
 * @author Acer
 */
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import javafx.geometry.Insets;

public class Module3 extends Application{
    private ImageView imageView = new ImageView();
    private BufferedImage originalImage;
    private BufferedImage processedImage;
    
    private WritableImage overlay;
    private PixelWriter writer;
    private ImageView overlayView;
    private boolean[][]mask; //selected area
    
    //to track drawing line
    private int startX;
    private int startY;
    private int lastX ;
    private int lastY;
    
    private int maskWidth;
    private int maskHeight;
    
    private boolean eyedropperMode = false;
    private Color targetColor = Color.RED;
    
    @Override
    public void start(Stage stage){
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(500);
        
        //for image and drawing layer
        overlay = new WritableImage(500,500);
        writer = overlay.getPixelWriter();
        overlayView = new ImageView(overlay);
        StackPane stack = new StackPane(imageView, overlayView);
        
        //draw lasso  
        stack.setOnMousePressed(e -> { 
            startX = toX(e.getX());
            startY = toY( e.getY());
            lastX = startX;
            lastY = startY;
        });
        
        stack.setOnMouseDragged(e -> {
            int x = toX(e.getX());
            int y = toY( e.getY());
            
            drawLine(lastX, lastY, x, y); //smooth drawing
            lastX = x;
            lastY = y;
        });
        
        //auto close the line
        stack.setOnMouseReleased(e -> {
            // connect last point back to start point
            drawLine(lastX, lastY, startX, startY);
            previewSelection();
        });
               
        //load button 
        Button loadButton = new Button("Upload Image");
        loadButton.setOnAction(e -> loadImage(stage));
        
        //resize
        TextField widthField = new TextField();
        TextField heightField = new TextField();
        Button resizeBtn = new Button("Apply Resize");
        
        resizeBtn.setOnAction(e -> {
            int w = Integer.parseInt(widthField.getText());
            int h = Integer.parseInt(heightField.getText());
            
            BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g = resized.createGraphics();
            g.drawImage(originalImage, 0, 0, w, h, null);
            g.dispose();
            
            processedImage = resized;
            imageView.setImage(SwingFXUtils.toFXImage(resized, null));
        });
        
        VBox resizeBox = createSection("Resize", 
                new HBox(5, new Label("Width:"), widthField),
                new HBox(5, new Label("Height:"), heightField),
                resizeBtn);
        
        //rotation
        Button rotatePlus = new Button("Rotate +90°");
        Button rotateMinus = new Button("Rotate -90°");
        
        rotatePlus.setOnAction(e -> 
              imageView.setRotate(imageView.getRotate() + 90));
        
        rotateMinus.setOnAction(e -> 
              imageView.setRotate(imageView.getRotate() - 90));
        
        VBox rotateBox = createSection("Rotation", rotatePlus, rotateMinus);
        
        //translation
        TextField xField = new TextField();
        TextField yField = new TextField();
        Button moveBtn = new Button("Move");
        
        moveBtn.setOnAction(e -> {
            double x = Double.parseDouble(xField.getText());
            double y = Double.parseDouble(yField.getText());
            
            imageView.setTranslateX(x);
            imageView.setTranslateY(y);
        });
        
        VBox translateBox = createSection("Translation",
                new HBox(5, new Label("X:"), xField),
                new HBox(5, new Label("Y:"), yField),
                moveBtn);
        
        // extraction        
        Button extractBtn = new Button("Extract Object");
        Button clearBtn = new Button("Clear Selection");
        Button saveBtn = new Button("Save Image");
        
        extractBtn.setOnAction(e -> extractUsingMask());
        
        clearBtn.setOnAction(e -> clearMask());
        saveBtn.setOnAction(e -> saveImage(stage));
        
        //eyedropper
        Button eyedropBtn = new Button("Eyedropper Mode");
        eyedropBtn.setOnAction(e -> {
            eyedropperMode = true;
            System.out.println("Eyedropper on");
        });
        
        imageView.setOnMouseClicked(e -> { 
            if(!eyedropperMode || originalImage == null) return;
            
            int x = toX(e.getX());
            int y = toY(e.getY());
            
            if(x < 0 || y < 0 || x >= originalImage.getWidth() || y>=originalImage.getHeight())
                return;
            
            java.awt.Color c = new java.awt.Color(originalImage.getRGB(x, y));
            
            targetColor = new Color(
            c.getRed() / 255.0, 
            c.getGreen() / 255.0,
            c.getBlue() / 255.0,
            1.0);
            
            System.out.println("Picked colour: " + targetColor);
            eyedropperMode = false;
        });
        
        
        VBox extractBox = createSection("Object Extraction", 
                new HBox(5, new Label("Draw on image (lasso)")), 
                new HBox(5, new Label("Target Color: "), eyedropBtn), 
                extractBtn, clearBtn,
                saveBtn);
        
        //right panel - place the features
        VBox rightPanel = new VBox(15,loadButton, resizeBox, rotateBox, translateBox, extractBox);
        
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(250);
        
        BorderPane root = new BorderPane();
        root.setCenter(stack);
        root.setRight(rightPanel);
        
        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Module 3");
        stage.setScene(scene);
        stage.show();
    }
    
    private void loadImage(Stage stage){
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(stage);
        
        if(file != null){
            try{
                originalImage = ImageIO.read(file);
                processedImage = originalImage;
                
                imageView.setImage(SwingFXUtils.toFXImage(originalImage, null));
                
                maskWidth = (int) imageView.getFitWidth();
                maskHeight = (int) (maskWidth * originalImage.getHeight() / originalImage.getWidth());

                overlay = new WritableImage(maskWidth, maskHeight);
                writer = overlay.getPixelWriter();
                overlayView.setImage(overlay);

                mask = new boolean[maskWidth][maskHeight];
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    //lasso extraction
    private void extractUsingMask() {

        boolean [][]region = fillRegion(mask);
        if (originalImage == null ) return;

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

                   if (diff < 180) { //  slightly higher threshold
                      result.setRGB(x, y, originalImage.getRGB(x, y));
                   } else {
                      result.setRGB(x, y, 0x00000000);
                   }
                } 
            }
        }

    processedImage = result;
    imageView.setImage(SwingFXUtils.toFXImage(result, null));
    }
    
    //use flood fill
    private boolean[][] fillRegion(boolean[][] mask) {
       int w = maskWidth;
       int h = maskHeight;

       boolean[][] filled = new boolean[w][h];
       boolean[][] visited = new boolean[w][h];

       java.util.Queue<int[]> queue = new java.util.LinkedList<>();

       // Start from outside (0,0)
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
                //flood fill outside area
                if (!visited[nx][ny] && !mask[nx][ny]) {
                    visited[nx][ny] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
        }
    }

    // invert inside region (inside lasso = true)
    boolean[][] region = new boolean[w][h];

    for (int x = 0; x < w; x++) {
        for (int y = 0; y < h; y++) {
            region[x][y] = !filled[x][y];
        }
    }

    return region;
}
    
    //clear lasso
    private void clearMask(){
        mask = new boolean[maskWidth][maskHeight];
        
        //clear drawing overlay
        overlay = new WritableImage(maskWidth, maskHeight);
        writer = overlay.getPixelWriter();
        overlayView.setImage(overlay);
        
        //restore original image
        imageView.setImage(SwingFXUtils.toFXImage(originalImage, null));
        processedImage = originalImage;
               
    }
    
    //use bresenham line
    private void drawLine(int x1, int y1, int x2, int y2) {

    int dx = Math.abs(x2 - x1);
    int dy = Math.abs(y2 - y1);

    int sx = x1 < x2 ? 1 : -1;
    int sy = y1 < y2 ? 1 : -1;

    int err = dx - dy;

    while (true) {

        // draw thicker line
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                int nx = x1 + i;
                int ny = y1 + j;

                if (nx >= 0 && ny >= 0 && nx < maskWidth && ny < maskHeight) {
                    writer.setColor(nx, ny, Color.RED); //draw red line
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

    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {

            int mx = x * maskWidth / w;
            int my = y * maskHeight / h;
            
            javafx.scene.paint.Color originalColor =
                        imageView.getImage().getPixelReader().getColor(x, y);


            if (region[mx][my]) {

                // highlight (blue overlay)
                pw.setColor(x, y,
                        originalColor.interpolate(Color.BLUE, 0.4));

            } else {
                pw.setColor(x, y,
                        imageView.getImage().getPixelReader().getColor(x, y));
            }
        }
    }

    imageView.setImage(preview);
}
    
    private void enableEyedropper(){
        imageView.setOnMouseClicked(e -> { 
            if(originalImage == null) return;
            
            double viewX = imageView.getBoundsInLocal().getWidth();
            double viewY = imageView.getBoundsInLocal().getHeight();

            int imgX = (int) (e.getX() * originalImage.getWidth() / viewX);
            int imgY = (int) (e.getY() * originalImage.getHeight() / viewY);
            
            if(imgX >= 0 && imgY >= 0 &&
                imgX < originalImage.getWidth() &&
                imgY < originalImage.getHeight()) {
                
                java.awt.Color awt = new java.awt.Color(originalImage.getRGB(imgX, imgY));

            targetColor = new Color(
                    awt.getRed() / 255.0,
                    awt.getGreen() / 255.0,
                    awt.getBlue() / 255.0,
                    1.0
            );

            System.out.println("Picked color: " + targetColor);
            
            imageView.setOnMouseClicked(null);
            System.out.println("Eyedropper disabled, back to lasso mode");
        }
    });
        System.out.println("Eyedropper mode ON");
    }
    
    private void saveImage(Stage stage){
        if(processedImage == null) return;
        
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName("output.png");
        
        File file = chooser.showSaveDialog(stage);
        
        if(file != null){
            try{
                ImageIO.write(processedImage, "png", file);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private VBox createSection(String title, javafx.scene.Node... nodes){
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        VBox box = new VBox(5);
        box.getChildren().add(label);
        box.getChildren().addAll(nodes);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5;");
        return box;
    }
    
    private int toX(double x){
        return (int) (x * imageView.getImage().getWidth() / imageView.getBoundsInLocal().getWidth());
        
    }
    private int toY(double y){
        return (int) (y * imageView.getImage().getHeight() / imageView.getBoundsInLocal().getHeight());
        
    }
    
    public static void main(String[] args) {
        launch();
    }
}
