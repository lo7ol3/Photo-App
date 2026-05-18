package mm.prog.project;

import java.util.ArrayList;
import java.util.List;

public class SharedData {

    public static String selectedImagePath;
    public static List<ImageData> imageList = new ArrayList<>();
    public static boolean fromGallery = false;

    // Track multiple selections for features like Collage
    private static List<String> selectedImagePaths = new ArrayList<>();

    // List to hold all registered listeners
    private static List<ImageChangeListener> listeners = new ArrayList<>();

    // Interface for components that want to be notified when image selection changes
    public interface ImageChangeListener {
        void onImageChanged(String imagePath);
    }

    // Method to unregister a listener
    public static void removeImageChangeListener(ImageChangeListener listener) {
        listeners.remove(listener);
    }

    // Method to register a listener
    public static void addImageChangeListener(ImageChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void setSelectedImagePath(String path) {
        selectedImagePath = path;

        // Notify all registered listeners about the image change
        for (ImageChangeListener listener : listeners) {
            try {
                listener.onImageChanged(path);
            } catch (Exception e) {
                System.err.println("Error notifying image change listener: " + e.getMessage());
            }
        }
    }

    // --- Added for Multi-Selection Management ---
    public static void setSelectedImagePaths(List<String> paths) {
        selectedImagePaths = paths;
        // Fallback backward-compatibility: point the old single variable to the latest selection
        if (!paths.isEmpty()) {
            setSelectedImagePath(paths.get(paths.size() - 1));
        } else {
            setSelectedImagePath(null);
        }
    }

    public static List<String> getSelectedImagePaths() {
        return new ArrayList<>(selectedImagePaths);
    }
}