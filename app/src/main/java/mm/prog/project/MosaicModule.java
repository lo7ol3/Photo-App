package mm.prog.project;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MosaicModule {
    private int tileSize = 20;
    private final List<Mat> tileLibrary = new ArrayList<>();
    private final List<String> availableImagePaths = new ArrayList<>();
    private final ImageView mosaicView;
    private MosaicShape selectedShape = MosaicShape.RECTANGLE;
    private boolean useRepositoryImages = true;

    // Static block to load OpenCV native library
    static {
        try {
            // Try different common OpenCV loading methods
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e1) {
            try {
                // Alternative loading method
                System.loadLibrary("opencv_java");
            } catch (UnsatisfiedLinkError e2) {
                try {
                    // Another alternative
                    System.loadLibrary("opencv_java470"); // or whatever version you have
                } catch (UnsatisfiedLinkError e3) {
                    System.err.println("Failed to load OpenCV native library. Please ensure OpenCV is properly installed.");
                    System.err.println("Error details: " + e3.getMessage());
                }
            }
        }
    }

    // Enum for different shapes
    public enum MosaicShape {
        RECTANGLE("Rectangle"),
        CIRCLE("Circle"),
        HEART("Heart"),
        STAR("Star"),
        DIAMOND("Diamond");

        private final String displayName;

        MosaicShape(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public MosaicModule(ImageView mosaicView) {
        this.mosaicView = mosaicView;

        // Check if OpenCV is loaded properly
        try {
            // Test OpenCV functionality
            Mat testMat = new Mat();
            System.out.println("OpenCV loaded successfully. Version: " + Core.VERSION);
            testMat.release();
        } catch (Exception e) {
            System.err.println("OpenCV initialization failed: " + e.getMessage());
        }

        // Load images from repository if available
        loadRepositoryImages();
    }

    public void setTileSize(int size) {
        this.tileSize = size;
    }

    public void setMosaicShape(MosaicShape shape) {
        this.selectedShape = shape;
    }

    public void setUseRepositoryImages(boolean useRepository) {
        this.useRepositoryImages = useRepository;
        if (useRepository) {
            loadRepositoryImages();
        }
    }

    private void loadRepositoryImages() {
        availableImagePaths.clear();

        // Get images from SharedData (repository)
        if (SharedData.imageList != null) {
            for (ImageData imageData : SharedData.imageList) {
                availableImagePaths.add(imageData.imagePath);
            }
        }

        // Load the images into OpenCV Mat objects
        refreshTileLibrary();

        System.out.println("Loaded " + availableImagePaths.size() + " images from repository for mosaic");
    }

    private void refreshTileLibrary() {
        // Clear existing tiles
        for (Mat tile : tileLibrary) {
            tile.release();
        }
        tileLibrary.clear();

        // Load new tiles from available paths
        for (String path : availableImagePaths) {
            try {
                Mat tile = Imgcodecs.imread(path);
                if (!tile.empty()) {
                    tileLibrary.add(tile);
                }
            } catch (Exception e) {
                System.err.println("Error loading tile from: " + path + " - " + e.getMessage());
            }
        }
    }

    public void generateMosaic(String imagePath) {
        try {
            Mat target = Imgcodecs.imread(imagePath);
            if (target.empty()) {
                System.err.println("Could not load image from: " + imagePath);
                return;
            }

            Mat result = new Mat(target.size(), target.type());
            Mat mask = createShapeMask(target.cols(), target.rows());

            // If no repository images available, fall back to color blocks
            if (tileLibrary.isEmpty() && useRepositoryImages) {
                System.out.println("No repository images available, using color blocks");
                generateColorMosaic(target, result, mask);
            } else {
                generateImageMosaic(target, result, mask);
            }

            // Convert and display the result
            Image mosaicImage = matToImage(result);
            if (mosaicImage != null) {
                mosaicView.setImage(mosaicImage);
            }

            result.release();
            target.release();
            mask.release();

        } catch (Exception e) {
            System.err.println("Error generating mosaic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Mat createShapeMask(int width, int height) {
        Mat mask = Mat.zeros(height, width, CvType.CV_8UC1);

        switch (selectedShape) {
            case RECTANGLE:
                mask.setTo(new Scalar(255)); // Full rectangle (entire image)
                break;

            case CIRCLE:
                Point center = new Point(width / 2.0, height / 2.0);
                int radius = Math.min(width, height) / 2 - 20;
                Imgproc.circle(mask, center, radius, new Scalar(255), -1);
                break;

            case HEART:
                createHeartMask(mask, width, height);
                break;

            case STAR:
                createStarMask(mask, width, height);
                break;

            case DIAMOND:
                createDiamondMask(mask, width, height);
                break;
        }

        return mask;
    }

    private void createHeartMask(Mat mask, int width, int height) {
        Point center = new Point(width / 2.0, height / 2.0);
        int size = Math.min(width, height) / 3;

        // Create heart shape using circles and triangle
        Point leftCircle = new Point(center.x - size/2, center.y - size/3);
        Point rightCircle = new Point(center.x + size/2, center.y - size/3);

        // Draw two circles for top of heart
        Imgproc.circle(mask, leftCircle, size/2, new Scalar(255), -1);
        Imgproc.circle(mask, rightCircle, size/2, new Scalar(255), -1);

        // Draw triangle for bottom of heart
        Point[] trianglePoints = {
                new Point(center.x - size, center.y),
                new Point(center.x + size, center.y),
                new Point(center.x, center.y + size)
        };

        MatOfPoint triangle = new MatOfPoint(trianglePoints);
        List<MatOfPoint> triangles = new ArrayList<>();
        triangles.add(triangle);
        Imgproc.fillPoly(mask, triangles, new Scalar(255));
    }

    private void createStarMask(Mat mask, int width, int height) {
        Point center = new Point(width / 2.0, height / 2.0);
        int outerRadius = Math.min(width, height) / 2 - 20;
        int innerRadius = outerRadius / 2;

        List<Point> starPoints = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            double angle = (i * Math.PI) / 5;
            int radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = center.x + radius * Math.cos(angle - Math.PI/2);
            double y = center.y + radius * Math.sin(angle - Math.PI/2);
            starPoints.add(new Point(x, y));
        }

        MatOfPoint star = new MatOfPoint(starPoints.toArray(new Point[0]));
        List<MatOfPoint> stars = new ArrayList<>();
        stars.add(star);
        Imgproc.fillPoly(mask, stars, new Scalar(255));
    }

    private void createDiamondMask(Mat mask, int width, int height) {
        Point center = new Point(width / 2.0, height / 2.0);
        int size = Math.min(width, height) / 2 - 20;

        Point[] diamondPoints = {
                new Point(center.x, center.y - size),      // Top
                new Point(center.x + size, center.y),     // Right
                new Point(center.x, center.y + size),     // Bottom
                new Point(center.x - size, center.y)      // Left
        };

        MatOfPoint diamond = new MatOfPoint(diamondPoints);
        List<MatOfPoint> diamonds = new ArrayList<>();
        diamonds.add(diamond);
        Imgproc.fillPoly(mask, diamonds, new Scalar(255));
    }

    private void generateImageMosaic(Mat target, Mat result, Mat mask) {
        Random random = new Random();

        for (int y = 0; y < target.rows(); y += tileSize) {
            for (int x = 0; x < target.cols(); x += tileSize) {
                int width = Math.min(tileSize, target.cols() - x);
                int height = Math.min(tileSize, target.rows() - y);
                Rect roi = new Rect(x, y, width, height);

                // Check if this tile is within our shape mask
                Mat maskRegion = mask.submat(roi);
                Scalar maskMean = Core.mean(maskRegion);

                if (maskMean.val[0] > 128) { // Inside shape
                    Mat sourceRegion = target.submat(roi);

                    if (!tileLibrary.isEmpty()) {
                        // Use a random image from repository as tile
                        Mat selectedTile = tileLibrary.get(random.nextInt(tileLibrary.size()));
                        Mat resizedTile = new Mat();
                        Imgproc.resize(selectedTile, resizedTile, new Size(width, height));

                        // Blend the tile with original image for better effect
                        Mat blendedTile = new Mat();
                        Core.addWeighted(resizedTile, 0.7, sourceRegion, 0.3, 0, blendedTile);
                        blendedTile.copyTo(result.submat(roi));

                        resizedTile.release();
                        blendedTile.release();
                    } else {
                        // Fallback to average color
                        Scalar avgColor = Core.mean(sourceRegion);
                        result.submat(roi).setTo(avgColor);
                    }
                    sourceRegion.release();
                } else {
                    // Outside shape - use original image or make it darker
                    Mat sourceRegion = target.submat(roi);
                    Mat darkenedRegion = new Mat();
                    sourceRegion.convertTo(darkenedRegion, -1, 0.3, 0); // Darken by 70%
                    darkenedRegion.copyTo(result.submat(roi));
                    darkenedRegion.release();
                    sourceRegion.release();
                }
                maskRegion.release();
            }
        }
    }

    private void generateColorMosaic(Mat target, Mat result, Mat mask) {
        for (int y = 0; y < target.rows(); y += tileSize) {
            for (int x = 0; x < target.cols(); x += tileSize) {
                int width = Math.min(tileSize, target.cols() - x);
                int height = Math.min(tileSize, target.rows() - y);
                Rect roi = new Rect(x, y, width, height);

                // Check if this tile is within our shape mask
                Mat maskRegion = mask.submat(roi);
                Scalar maskMean = Core.mean(maskRegion);

                if (maskMean.val[0] > 128) { // Inside shape
                    Mat sourceRegion = target.submat(roi);
                    Scalar avgColor = Core.mean(sourceRegion);

                    // Create enhanced color blocks
                    Scalar enhancedColor = new Scalar(
                            Math.min(255, avgColor.val[0] * 1.2),
                            Math.min(255, avgColor.val[1] * 1.2),
                            Math.min(255, avgColor.val[2] * 1.2)
                    );

                    result.submat(roi).setTo(enhancedColor);
                    sourceRegion.release();
                } else {
                    // Outside shape - darken original
                    Mat sourceRegion = target.submat(roi);
                    Mat darkenedRegion = new Mat();
                    sourceRegion.convertTo(darkenedRegion, -1, 0.3, 0);
                    darkenedRegion.copyTo(result.submat(roi));
                    darkenedRegion.release();
                    sourceRegion.release();
                }
                maskRegion.release();
            }
        }
    }

    private Image matToImage(Mat matrix) {
        try {
            MatOfByte byteMat = new MatOfByte();
            boolean success = Imgcodecs.imencode(".png", matrix, byteMat);

            if (success && byteMat.total() > 0) {
                return new Image(new ByteArrayInputStream(byteMat.toArray()));
            } else {
                System.err.println("Failed to encode image to bytes");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error converting Mat to Image: " + e.getMessage());
            return null;
        }
    }

    public void addTile(String path) {
        try {
            Mat tile = Imgcodecs.imread(path);
            if (!tile.empty()) {
                tileLibrary.add(tile);
                availableImagePaths.add(path);
                System.out.println("Added tile: " + path);
            } else {
                System.err.println("Could not load tile image: " + path);
            }
        } catch (Exception e) {
            System.err.println("Error adding tile: " + e.getMessage());
        }
    }

    public void clearTileLibrary() {
        for (Mat tile : tileLibrary) {
            tile.release();
        }
        tileLibrary.clear();
        availableImagePaths.clear();
    }

    public int getTileCount() {
        return tileLibrary.size();
    }

    public MosaicShape[] getAvailableShapes() {
        return MosaicShape.values();
    }

    public List<String> getAvailableImagePaths() {
        return new ArrayList<>(availableImagePaths);
    }

    public boolean isUsingRepositoryImages() {
        return useRepositoryImages;
    }

    public int getTileSize() {
        return tileSize;
    }

    public MosaicShape getSelectedShape() {
        return selectedShape;
    }

    public void dispose() {
        for (Mat m : tileLibrary) {
            if (m != null) {
                m.release();
            }
        }
        tileLibrary.clear();
        availableImagePaths.clear();
    }
}