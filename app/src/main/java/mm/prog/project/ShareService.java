package mm.prog.project;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class ShareService {

    public static void shareViaEmail(String subject, String body) {
        try {
            String mailto = "mailto:?subject=" + URLEncoder.encode(subject, StandardCharsets.UTF_8.name()) +
                    "&body=" + URLEncoder.encode(body, StandardCharsets.UTF_8.name());
            Desktop.getDesktop().browse(new URI(mailto));
        } catch (Exception e) {
            System.err.println("Error opening Email client: " + e.getMessage());
        }
    }

    public static void shareViaWhatsApp(String message, String imagePath) {
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                File file = new File(imagePath);
                if (file.exists()) {
                    // Grab system clipboard and inject the physical file payload
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putFiles(Collections.singletonList(file));
                    clipboard.setContent(content);
                }
            }

            // FIX: Encode the message into the URL so the API accepts it as a valid link.
            // This safely triggers the browser prompt to open the Desktop App!
            String url = "https://api.whatsapp.com/send?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8.name());
            Desktop.getDesktop().browse(new URI(url));

        } catch (Exception e) {
            System.err.println("Error opening WhatsApp: " + e.getMessage());
        }
    }
}