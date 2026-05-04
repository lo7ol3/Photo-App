/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author user
 */
public class ImageData {

    public String imagePath;
    public String annotation;
    public boolean hasAnnotation;

    public ImageData() {
    }

    public ImageData(String imagePath, String annotation) {
        this.imagePath = imagePath;
        this.annotation = annotation;
        this.hasAnnotation = annotation != null && !annotation.trim().isEmpty();
    }

    @Override
    public String toString() {
        return (hasAnnotation ? "❤️ " : "")
                + annotation + " - " + imagePath;
    }
}
