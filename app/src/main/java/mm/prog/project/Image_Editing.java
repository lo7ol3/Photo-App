package mm.prog.project;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class Image_Editing {
    
    // OpenCV Mat objects for image processing
    private Mat originalMat;
    private Mat currentMat;
    private Mat tempMat;
    
    // JavaFX Image objects for UI display
    private Image originalImage;
    private WritableImage editedImage;
    
    // Image dimensions
    private int imageWidth;
    private int imageHeight;
    
    // File handling
    private File imageFile;
    private String imagePath;
    
    // Current adjustment values for sliders
    private double currentBrightness = 0.0;
    private double currentContrast = 1.0;
    private boolean isGrayscale = false;
    
    // Border properties
    private Scalar borderColor = new Scalar(0, 0, 0); // Black in BGR
    private int borderWidth = 0;
    
    /**
     * Default constructor
     */
    public Image_Editing() {
        this.originalMat = new Mat();
        this.currentMat = new Mat();
        this.tempMat = new Mat();
        this.imageFile = null;
        this.imagePath = "";
    }
    
    /**
     * Constructor with image path
     * @param imagePath Path to the image file
     */
    public Image_Editing(String imagePath) {
        this();
        this.imagePath = imagePath;
        loadImage(imagePath);
    }
    
    /**
     * Load image from file path using OpenCV
     * @param imagePath Path to the image file
     * @return true if successful, false otherwise
     */
    public boolean loadImage(String imagePath) {
        try {
            this.imagePath = imagePath;
            this.imageFile = new File(imagePath);
            
            if (!imageFile.exists()) {
                System.err.println("Image file not found: " + imagePath);
                return false;
            }
            
            // Load image using OpenCV
            this.originalMat = Imgcodecs.imread(imagePath, Imgcodecs.IMREAD_COLOR);
            
            if (originalMat.empty()) {
                System.err.println("Error loading image with OpenCV: " + imagePath);
                return false;
            }
            
            initializeImageProperties();
            resetAdjustments();
            return true;
            
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initialize image properties and create working copy
     */
    private void initializeImageProperties() {
        if (!originalMat.empty()) {
            this.imageWidth = originalMat.cols();
            this.imageHeight = originalMat.rows();
            
            // Create working copy
            this.currentMat = new Mat();
            originalMat.copyTo(currentMat);
            
            // Convert to JavaFX Image for display
            updateJavaFXImage();
        }
    }
    
    /**
     * Reset all adjustment values to default
     */
    private void resetAdjustments() {
        this.currentBrightness = 0.0;
        this.currentContrast = 1.0;
        this.isGrayscale = false;
        this.borderWidth = 0;
        this.borderColor = new Scalar(0, 0, 0);
    }
    
    /**
     * Apply all current adjustments to the image using OpenCV
     */
    private void applyAllAdjustments() {
        if (originalMat.empty()) return;
        
        // Start with original image
        originalMat.copyTo(currentMat);
        
        // Apply brightness and contrast using OpenCV
        if (currentBrightness != 0.0 || currentContrast != 1.0) {
            currentMat.convertTo(currentMat, -1, currentContrast, currentBrightness * 255);
        }
        
        // Apply grayscale conversion
        if (isGrayscale) {
            Mat grayMat = new Mat();
            Imgproc.cvtColor(currentMat, grayMat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.cvtColor(grayMat, currentMat, Imgproc.COLOR_GRAY2BGR);
            grayMat.release();
        }
        
        // Apply border
        if (borderWidth > 0) {
            applyImageBorder();
        }
        
        // Update JavaFX display image
        updateJavaFXImage();
    }
    
    /**
     * Apply border using OpenCV
     */
    private void applyImageBorder() {
        if (borderWidth <= 0 || currentMat.empty()) return;
        
        Mat borderedMat = new Mat();
        Core.copyMakeBorder(currentMat, borderedMat, 
                           borderWidth, borderWidth, borderWidth, borderWidth, 
                           Core.BORDER_CONSTANT, borderColor);
        
        borderedMat.copyTo(currentMat);
        borderedMat.release();
    }
    
    /**
     * Convert OpenCV Mat to JavaFX Image
     */
    private void updateJavaFXImage() {
        if (currentMat.empty()) return;
        
        try {
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".png", currentMat, matOfByte);
            byte[] byteArray = matOfByte.toArray();
            
            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
            this.editedImage = new WritableImage((int)currentMat.cols(), (int)currentMat.rows());
            this.originalImage = new Image(inputStream);
            
            // Create writable image from the original for pixel manipulation if needed
            this.editedImage = new WritableImage(originalImage.getPixelReader(), 
                                               (int)originalImage.getWidth(), 
                                               (int)originalImage.getHeight());
            
            matOfByte.release();
            
        } catch (Exception e) {
            System.err.println("Error converting Mat to JavaFX Image: " + e.getMessage());
        }
    }
    
    /**
     * Toggle grayscale function using OpenCV
     */
    public void toggleGrayscale() {
        this.isGrayscale = !this.isGrayscale;
        applyAllAdjustments();
    }
    
    /**
     * Set grayscale mode using OpenCV
     * @param grayscaleEnabled true to enable grayscale, false to disable
     */
    public void setGrayscaleEnabled(boolean grayscaleEnabled) {
        this.isGrayscale = grayscaleEnabled;
        applyAllAdjustments();
    }
    
    /**
     * Slider function for brightness adjustment using OpenCV
     * @param brightnessLevel Brightness level (-100.0 to 100.0, 0.0 = no change)
     */
    public void setBrightnessSlider(double brightnessLevel) {
        this.currentBrightness = Math.max(-100.0, Math.min(100.0, brightnessLevel)) / 100.0;
        applyAllAdjustments();
    }
    
    /**
     * Slider function for contrast adjustment using OpenCV
     * @param contrastLevel Contrast level (0.0 to 3.0, 1.0 = no change)
     */
    public void setContrastSlider(double contrastLevel) {
        this.currentContrast = Math.max(0.0, Math.min(3.0, contrastLevel));
        applyAllAdjustments();
    }
    
    /**
     * Set image border width using OpenCV
     * @param borderWidth Border width in pixels
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = Math.max(0, borderWidth);
        applyAllAdjustments();
    }
    
    /**
     * Set image border color using OpenCV Scalar (BGR format)
     * @param red Red component (0-255)
     * @param green Green component (0-255)
     * @param blue Blue component (0-255)
     */
    public void setBorderColorRGB(int red, int green, int blue) {
        // OpenCV uses BGR format
        this.borderColor = new Scalar(blue, green, red);
        if (borderWidth > 0) {
            applyAllAdjustments();
        }
    }
    
    /**
     * Set image border color using JavaFX Color
     * @param color JavaFX Color object
     */
    public void setBorderColor(Color color) {
        int red = (int)(color.getRed() * 255);
        int green = (int)(color.getGreen() * 255);
        int blue = (int)(color.getBlue() * 255);
        setBorderColorRGB(red, green, blue);
    }
    
    /**
     * Set image border with both width and color
     * @param borderWidth Border width in pixels
     * @param color Border color
     */
    public void setImageBorder(int borderWidth, Color color) {
        this.borderWidth = Math.max(0, borderWidth);
        setBorderColor(color);
        applyAllAdjustments();
    }
    
    /**
     * Apply advanced OpenCV blur filter
     * @param kernelSize Size of the blur kernel (must be odd)
     */
    public void applyBlurFilter(int kernelSize) {
        if (originalMat.empty() || kernelSize < 3) return;
        
        // Ensure kernel size is odd
        if (kernelSize % 2 == 0) kernelSize++;
        
        Mat blurredMat = new Mat();
        Imgproc.GaussianBlur(currentMat, blurredMat, new Size(kernelSize, kernelSize), 0);
        blurredMat.copyTo(currentMat);
        blurredMat.release();
        
        updateJavaFXImage();
    }
    
    /**
     * Apply edge detection using Canny algorithm
     * @param threshold1 First threshold for edge detection
     * @param threshold2 Second threshold for edge detection
     */
    public void applyEdgeDetection(double threshold1, double threshold2) {
        if (originalMat.empty()) return;
        
        Mat grayMat = new Mat();
        Mat edgeMat = new Mat();
        
        // Convert to grayscale first
        Imgproc.cvtColor(currentMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        
        // Apply Canny edge detection
        Imgproc.Canny(grayMat, edgeMat, threshold1, threshold2);
        
        // Convert back to BGR for display
        Imgproc.cvtColor(edgeMat, currentMat, Imgproc.COLOR_GRAY2BGR);
        
        grayMat.release();
        edgeMat.release();
        
        updateJavaFXImage();
    }
    
    /**
     * Apply histogram equalization for better contrast
     */
    public void applyHistogramEqualization() {
        if (originalMat.empty()) return;
        
        Mat yuvMat = new Mat();
        Mat equalizedMat = new Mat();
        
        // Convert to YUV color space
        Imgproc.cvtColor(currentMat, yuvMat, Imgproc.COLOR_BGR2YUV);
        
        // Split channels
        java.util.List<Mat> channels = new java.util.ArrayList<>();
        Core.split(yuvMat, channels);
        
        // Apply histogram equalization to Y channel
        Imgproc.equalizeHist(channels.get(0), channels.get(0));
        
        // Merge channels back
        Core.merge(channels, equalizedMat);
        
        // Convert back to BGR
        Imgproc.cvtColor(equalizedMat, currentMat, Imgproc.COLOR_YUV2BGR);
        
        // Release resources
        yuvMat.release();
        equalizedMat.release();
        for (Mat channel : channels) {
            channel.release();
        }
        
        updateJavaFXImage();
    }
    
    /**
     * Save the current edited image to file
     * @param outputPath Path to save the image
     * @return true if successful, false otherwise
     */
    public boolean saveImage(String outputPath) {
        if (currentMat.empty()) {
            System.err.println("No image to save");
            return false;
        }
        
        try {
            return Imgcodecs.imwrite(outputPath, currentMat);
        } catch (Exception e) {
            System.err.println("Error saving image: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Remove image border
     */
    public void removeBorder() {
        setBorderWidth(0);
    }
    
    /**
     * Reset image to original state (removes all adjustments)
     */
    public void resetToOriginal() {
        if (!originalMat.empty()) {
            resetAdjustments();
            originalMat.copyTo(currentMat);
            updateJavaFXImage();
        }
    }
    
    /**
     * Get current OpenCV Mat object
     * @return Current Mat object
     */
    public Mat getCurrentMat() {
        return currentMat;
    }
    
    /**
     * Get original OpenCV Mat object
     * @return Original Mat object
     */
    public Mat getOriginalMat() {
        return originalMat;
    }
    
    /**
     * Get current brightness value
     * @return Current brightness level
     */
    public double getCurrentBrightness() {
        return currentBrightness * 100.0; // Return as percentage
    }
    
    /**
     * Get current contrast value
     * @return Current contrast level
     */
    public double getCurrentContrast() {
        return currentContrast;
    }
    
    /**
     * Check if grayscale is enabled
     * @return true if grayscale is enabled
     */
    public boolean isGrayscaleEnabled() {
        return isGrayscale;
    }
    
    /**
     * Get current border width
     * @return Border width in pixels
     */
    public int getBorderWidth() {
        return borderWidth;
    }
    
    /**
     * Get current border color as JavaFX Color
     * @return Border color
     */
    public Color getBorderColor() {
        // Convert BGR Scalar to JavaFX Color
        return Color.rgb((int)borderColor.val[2], (int)borderColor.val[1], (int)borderColor.val[0]);
    }
    
    /**
     * Get the current edited JavaFX image
     * @return WritableImage object
     */
    public WritableImage getEditedImage() {
        return editedImage;
    }
    
    /**
     * Get the original JavaFX image
     * @return Image object
     */
    public Image getOriginalImage() {
        return originalImage;
    }
    
    /**
     * Get image width
     * @return Image width in pixels
     */
    public int getImageWidth() {
        return imageWidth;
    }
    
    /**
     * Get image height
     * @return Image height in pixels
     */
    public int getImageHeight() {
        return imageHeight;
    }
    
    /**
     * Get total width including border
     * @return Total width including border
     */
    public int getTotalWidth() {
        return imageWidth + (borderWidth * 2);
    }
    
    /**
     * Get total height including border
     * @return Total height including border
     */
    public int getTotalHeight() {
        return imageHeight + (borderWidth * 2);
    }
    
    /**
     * Get image file path
     * @return String path to image file
     */
    public String getImagePath() {
        return imagePath;
    }
    
    /**
     * Check if image is loaded
     * @return true if image is loaded, false otherwise
     */
    public boolean isImageLoaded() {
        return !originalMat.empty() && !currentMat.empty();
    }
    
    /**
     * Clean up OpenCV Mat objects to free memory
     */
    public void dispose() {
        if (originalMat != null) originalMat.release();
        if (currentMat != null) currentMat.release();
        if (tempMat != null) tempMat.release();
    }
}