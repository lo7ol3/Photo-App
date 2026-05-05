package mm.prog.project;



import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    static {
        try {
            // 1. First, try the standard way
            System.loadLibrary("opencv_java4120");
            System.out.println("Success! OpenCV Loaded normally.");
        } catch (UnsatisfiedLinkError e) {
            // 2. Fallback: Manually point to the file in your project folder
            String libPath = System.getProperty("user.dir") + File.separator + "opencv_native" + File.separator + "opencv_java4120.dll";
            try {
                System.load(libPath);
                System.out.println("Success! OpenCV Loaded via manual path: " + libPath);
            } catch (UnsatisfiedLinkError e2) {
                System.err.println("CRITICAL ERROR: Could not find DLL at " + libPath);
                System.err.println("Please ensure the file 'opencv_java4120.dll' is inside: " + System.getProperty("user.dir") + "\\opencv_native\\");
                System.exit(1);
            }
        }
    }

    @Override
    public void start(Stage stage) {
       // VideoGeneratorModule module = new VideoGeneratorModule();
        MainDashboard dashboard = new MainDashboard();

        stage.setScene(new Scene(dashboard.getContainer(), 1024, 768));
        stage.setTitle("MM PROG Project - Video Synthesis");
        stage.show();

        stage.setOnCloseRequest(e -> dashboard.dispose());
    }

    public static void main(String[] args) { launch(args); }
}
