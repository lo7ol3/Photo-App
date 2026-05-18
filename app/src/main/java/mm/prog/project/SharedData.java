/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mm.prog.project;

import java.util.ArrayList;
import java.util.List;

public class SharedData {

    public static String selectedImagePath;
    public static List<ImageData> imageList = new ArrayList<>();
    public static boolean fromGallery = false;

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
}
