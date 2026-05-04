package controller;

import model.ImageData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GalleryController implements Initializable {

    @FXML
    private Button btnStartEditing;

    @FXML
    private Button btnUpload;

    @FXML
    private TilePane tilePane;

    // STORAGE
    private List<ImageData> imageList = new ArrayList<>();
    private final String FILE_PATH = "data.json";
    private final Gson gson = new Gson();

    private StackPane selectedContainer = null;

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tilePane.setHgap(20);
        tilePane.setVgap(20);

        loadData();
    }

    @FXML
    private void btnStartEditingClicked(javafx.event.ActionEvent event) {

        if (selectedContainer == null) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Image Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an image before editing.");
            alert.showAndWait();

            return;
        }

        model.SharedData.imageList = imageList;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/edit.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // UPLOAD IMAGES
    @FXML
    private void btnUploadClicked(javafx.event.ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(null);
        if (files == null) {
            return;
        }

        for (File file : files) {

            ImageData data = new ImageData(file.toURI().toString(), "");
            imageList.add(data);

            addImageToUI(data);
        }

        saveData();
    }

    // BUILD UI TILE
    private void addImageToUI(ImageData data) {

        Image image = new Image(data.imagePath);
        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        // HEART ICON
        Label heart = new Label("♥");
        heart.setStyle("-fx-text-fill: red; -fx-font-size: 25;");
        StackPane.setAlignment(heart, Pos.TOP_RIGHT);
        StackPane.setMargin(heart, new javafx.geometry.Insets(0, 8, 0, 0));
        heart.setVisible(data.hasAnnotation);

        // CONTAINER
        StackPane container = new StackPane();
        container.setStyle("-fx-border-color: #444; -fx-border-width: 2;");

        Button editBtn = new Button();

        // load edit icon
        Image editIcon = new Image(getClass().getResourceAsStream("/icons/edit.png"));
        ImageView iconView = new ImageView(editIcon);

        iconView.setFitWidth(30);
        iconView.setFitHeight(30);

        editBtn.setGraphic(iconView);
        editBtn.setStyle(
                "-fx-background-color: transparent;"
                + "-fx-cursor: hand;"
        );

        editBtn.setOnAction(e -> {
            openAnnotationDialog(imageView, heart, data);
        });


        StackPane.setAlignment(editBtn, Pos.BOTTOM_RIGHT);

        container.getChildren().addAll(imageView, heart, editBtn);

        // TOOLTIP
        Tooltip tooltip = new Tooltip(
                data.annotation == null || data.annotation.isEmpty()
                ? "No annotation"
                : data.annotation
        );
        tooltip.setStyle("-fx-font-size: 15px;");
        Tooltip.install(container, tooltip);

        container.setOnMouseEntered(e -> {
            tooltip.setText(
                    data.annotation == null || data.annotation.isEmpty()
                    ? "No annotation"
                    : data.annotation
            );
        });

        container.setOnMouseClicked(e -> {

            if (selectedContainer != null) {
                selectedContainer.setStyle("-fx-border-color: #444; -fx-border-width: 2;");
            }

            container.setStyle("-fx-border-color: yellow; -fx-border-width: 3;");
            selectedContainer = container;

            model.SharedData.selectedImagePath = data.imagePath;
        });

        tilePane.getChildren().add(container);
    }

    // ANNOTATION
    private void openAnnotationDialog(ImageView imageView, Label heart, ImageData data) {

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add/Edit Annotation");
        dialog.setHeaderText(null);

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        TextArea textArea = new TextArea();
        textArea.setPrefRowCount(6);
        textArea.setPrefColumnCount(25);
        textArea.setText(data.annotation == null ? "" : data.annotation);

        dialog.getDialogPane().setContent(textArea);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                return textArea.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(text -> {

            data.annotation = text;
            data.hasAnnotation = text != null && !text.trim().isEmpty();

            heart.setVisible(data.hasAnnotation);

            saveData();
        });
    }

    // SAVE TO JSON
    private void saveData() {
        try ( FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(imageList, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // LOAD FROM JSON
    private void loadData() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));

            Type listType = new TypeToken<List<ImageData>>() {
            }.getType();
            imageList = gson.fromJson(reader, listType);

            reader.close();

            if (imageList == null) {
                imageList = new ArrayList<>();
            }

            for (ImageData data : imageList) {
                addImageToUI(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
