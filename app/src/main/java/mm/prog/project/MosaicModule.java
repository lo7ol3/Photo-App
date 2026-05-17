import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc; // Added for color calculation
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class MosaicModule {
    private int tileSize = 20; 
    private final List<Mat> tileLibrary = new ArrayList<>();
    private final ImageView mosaicView;

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

    public void dispose() {
        for (Mat m : tileLibrary) m.release();
        tileLibrary.clear();
    }
}
