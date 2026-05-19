package mm.prog.project;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ShareModule implements SharedData.ImageChangeListener {
    private BorderPane layout;
    private Label statusLabel;
    private TextArea messageArea;
    private TextField subjectField;

    public ShareModule() {
        createUI();
        SharedData.addImageChangeListener(this);
    }

    private void createUI() {
        layout = new BorderPane();
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #121212;");

        VBox contentCard = new VBox(20);
        contentCard.setAlignment(Pos.TOP_LEFT);
        contentCard.setPadding(new Insets(25));

        contentCard.setStyle("-fx-background-color: #1e1e1e; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #25d366; " +
                "-fx-border-width: 2px; " +
                "-fx-border-radius: 8;");
        contentCard.setMaxWidth(650);
        contentCard.setMaxHeight(550);

        Label headerTitle = new Label("📤 Share Studio Suite");
        headerTitle.setStyle("-fx-text-fill: #25d366; -fx-font-size: 20px; -fx-font-weight: bold;");

        statusLabel = new Label("Active Tracked Workspace: No image selected.");
        statusLabel.setStyle("-fx-text-fill: #ffa000; -fx-font-style: italic; -fx-wrap-text: true;");

        if (SharedData.selectedImagePath != null && !SharedData.selectedImagePath.isEmpty()) {
            onImageChanged(SharedData.selectedImagePath);
        }

        Label lblSubject = new Label("Email Subject Header Line:");
        lblSubject.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold;");
        subjectField = new TextField("Check out my edited image!");
        subjectField.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white; -fx-border-color: #444; -fx-border-radius: 4;");

        Label lblMessage = new Label("Custom Share Note Contents:");
        lblMessage.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold;");

        messageArea = new TextArea("Hey! I just used our Digital Image Processing Studio to render this file. Check it out!");
        messageArea.setStyle("-fx-control-inner-background: #2b2b2b; -fx-text-fill: white;");
        messageArea.setPrefRowCount(5);
        messageArea.setWrapText(true);

        HBox actionRow = new HBox(15);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Button btnEmail = new Button("📧 Share via Email Client");
        btnEmail.setStyle("-fx-background-color: #128C7E; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 18; -fx-cursor: hand;");
        btnEmail.setOnAction(e -> executeEmailShare());

        Button btnWhatsApp = new Button("💬 Send to WhatsApp Mobile/Web");
        btnWhatsApp.setStyle("-fx-background-color: #25d366; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 10 18; -fx-cursor: hand;");
        btnWhatsApp.setOnAction(e -> executeWhatsAppShare());

        actionRow.getChildren().addAll(btnEmail, btnWhatsApp);

        contentCard.getChildren().addAll(
                headerTitle,
                statusLabel,
                new Separator(javafx.geometry.Orientation.HORIZONTAL),
                lblSubject, subjectField,
                lblMessage, messageArea,
                actionRow
        );

        layout.setCenter(contentCard);
    }

    private void executeEmailShare() {
        String subject = subjectField.getText().trim();
        StringBuilder body = new StringBuilder(messageArea.getText().trim());

        if (SharedData.selectedImagePath != null && !SharedData.selectedImagePath.isEmpty()) {
            body.append("\n\n[Active Local Project Asset File Path Location]:\n").append(SharedData.selectedImagePath);
        }

        ShareService.shareViaEmail(subject, body.toString());
    }

    private void executeWhatsAppShare() {
        // FIX: Grab the actual text from the text area so the API doesn't crash!
        String message = messageArea.getText().trim();
        String targetedPath = SharedData.selectedImagePath;

        ShareService.shareViaWhatsApp(message, targetedPath);
    }

    @Override
    public void onImageChanged(String newPath) {
        if (newPath != null && !newPath.isEmpty()) {
            java.io.File file = new java.io.File(newPath);
            if (statusLabel != null) {
                statusLabel.setText("Active Workspace Attached: " + file.getName());
                statusLabel.setStyle("-fx-text-fill: #25d366; -fx-font-style: normal; -fx-font-weight: bold;");
            }
        }
    }

    public BorderPane getLayout() {
        return this.layout;
    }
}