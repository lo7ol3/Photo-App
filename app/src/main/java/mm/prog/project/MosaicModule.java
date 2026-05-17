package mm.prog.project;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File; // Added for color calculation
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MosaicModule {
    private int tileSize = 20; 
    private final List<Mat> tileLibrary = new ArrayList<>();
    private final ImageView mosaicView;
    private Image lastMosaicImage;
    private Consumer<String> onMosaicSaved;

    public MosaicModule(ImageView mosaicView) {
        this.mosaicView = mosaicView;
    }

    public void setTileSize(int size) {
        this.tileSize = size;
    }

    public void generateMosaic(String imagePath) {
        Mat target = Imgcodecs.imread(imagePath);
        if (target.empty()) return;

        Mat result = new Mat(target.size(), target.type());

        for (int y = 0; y < target.rows(); y += tileSize) {
            for (int x = 0; x < target.cols(); x += tileSize) {
                int width = Math.min(tileSize, target.cols() - x);
                int height = Math.min(tileSize, target.rows() - y);
                Rect roi = new Rect(x, y, width, height);
                Mat sourceRegion = target.submat(roi);
                
                if (!tileLibrary.isEmpty()) {
                    // Option A: Use image tiles if library is populated[cite: 6]
                    Mat tile = tileLibrary.get((x + y) % tileLibrary.size());
                    Mat resizedTile = new Mat();
                    Imgproc.resize(tile, resizedTile, new Size(width, height));
                    resizedTile.copyTo(result.submat(roi));
                    resizedTile.release();
                } else {
                    // Option B: Average Color Fallback (Visual Mosaic)[cite: 6]
                    // This makes the tile size changes visible even without tile images
                    Scalar avgColor = Core.mean(sourceRegion);
                    result.submat(roi).setTo(avgColor);
                }
            }
        }

        mosaicView.setImage(matToImage(result));
        lastMosaicImage = matToImage(result);
        result.release();
        target.release();
    }

    private Image matToImage(Mat matrix) {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", matrix, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }

    public void addTile(String path) {
        Mat tile = Imgcodecs.imread(path);
        if (!tile.empty()) tileLibrary.add(tile);
    }

    public boolean saveDisplayedMosaic(File file) {
        if (lastMosaicImage == null || file == null) return false;
        try {
            BufferedImage bImage = SwingFXUtils.fromFXImage(lastMosaicImage, null);
            ImageIO.write(bImage, "png", file);
            if (onMosaicSaved != null) onMosaicSaved.accept(file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setOnMosaicSaved(Consumer<String> handler) { this.onMosaicSaved = handler; }

    public void dispose() {
        for (Mat m : tileLibrary) m.release();
        tileLibrary.clear();
    }
}