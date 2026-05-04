/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

import model.SharedData;
import model.ImageData;

/**
 * FXML Controller class
 *
 * @author user
 */
public class EditController implements Initializable {

    @FXML
    private Button btnEdit;
    @FXML
    private Button btnMosaic;
    @FXML
    private Button btnVideo;

    @FXML
    private Label lblNoOfImages;
    @FXML
    private ImageView imageView;
    @FXML
    private TilePane editTilePane;
    @FXML
    private Button btnBack;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        editTilePane.setHgap(10);
        editTilePane.setVgap(10);

        editTilePane.setPrefWidth(280);
        editTilePane.setMaxWidth(280);

        editTilePane.setPrefColumns(1);

        editTilePane.setPrefTileWidth(250);
        editTilePane.setPrefTileHeight(140);

        if (SharedData.selectedImagePath != null) {
            imageView.setImage(new Image(SharedData.selectedImagePath));
        }

        if (SharedData.imageList != null) {
            for (ImageData data : SharedData.imageList) {
                addImageTile(data);
            }
             lblNoOfImages.setText(SharedData.imageList.size() + " image(s)");
        }
    }

    @FXML
    private void EditPage(ActionEvent event) {
    }

    @FXML
    private void MosaicPage(ActionEvent event) {
    }

    @FXML
    private void VideoPage(ActionEvent event) {
    }

    @FXML
    private void btnBackClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/gallery.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addImageTile(ImageData data) {

        ImageView imageViewer = new ImageView(new Image(data.imagePath));
        imageViewer.setFitWidth(200);
        imageViewer.setFitHeight(120);
        imageViewer.setPreserveRatio(true);

        Label heart = new Label("♥");
        heart.setStyle("-fx-text-fill: red; -fx-font-size: 20;");
        StackPane.setAlignment(heart, Pos.TOP_RIGHT);
        StackPane.setMargin(heart, new Insets(3));
        heart.setVisible(data.hasAnnotation);

        StackPane container = new StackPane();
        container.setStyle("-fx-border-color: #444; -fx-border-width: 2;");
        container.getChildren().addAll(imageViewer, heart);

        Tooltip tooltip = new Tooltip(
                data.annotation == null || data.annotation.isEmpty()
                ? "No annotation"
                : data.annotation
        );

        tooltip.setStyle("-fx-font-size: 14px;");

        Tooltip.install(container, tooltip);

        container.setOnMouseClicked(e -> {
            imageView.setImage(new Image(data.imagePath));
            SharedData.selectedImagePath = data.imagePath;
        });

        editTilePane.getChildren().add(container);
    }

}
