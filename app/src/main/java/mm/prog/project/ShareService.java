package mm.prog.project;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ShareService {
    public static void shareViaEmail(String subject, String body) {
        try {
            String mailto = "mailto:?subject=" + URLEncoder.encode(subject, "UTF-8") +
                            "&body=" + URLEncoder.encode(body, "UTF-8");
            Desktop.getDesktop().browse(new URI(mailto));
        } catch (Exception e) {
            System.err.println("Error opening Email client: " + e.getMessage());
        }
    }

    public static void shareViaWhatsApp(String message) {
        try {
            String url = "https://api.whatsapp.com/send?text=" + 
                         URLEncoder.encode(message, StandardCharsets.UTF_8.name());
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.err.println("Error opening WhatsApp: " + e.getMessage());
        }
    }
}
