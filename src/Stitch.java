import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

public class Stitch extends Application {
    private final String version = "3.0";

    @Override
    public void start(Stage window) throws Exception{
        initialization();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("stitch.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 700, 900);

        window.setTitle("Stitch Tool v" + version + " by Aeon");
        window.setResizable(false);
        window.setScene(scene);
        window.getIcons().add(new Image("icon.png"));
        window.show();

        // Checks if there's a newer version of StitchTool
        try {
            checkVersion();
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
    }

    // Main Method
    public static void main(String[] args) {
        launch(args);
    }

    // Checks the latest GitHub releases to see if there's an updated version of StitchTool
    public void checkVersion() throws IOException {
        // Connects to the latest repository API
        URL url = new URL("https://api.github.com/repos/Aeonss/StitchTool/releases/latest");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.addRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

        // Reads the API
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
            response.append("\n");
        }
        in.close();

        // Parses the API to find the latest tag version
        double latestVersion = Double.parseDouble(response.substring(response.toString().indexOf("tag_name") + 11,
                response.toString().indexOf(",", response.toString().indexOf("tag_name")) - 1));

        // If an updated version is found, notifies the user
        if (Double.parseDouble(version) < latestVersion) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setHeaderText(null);
            a.setTitle("Update Available!");
            a.setContentText("A new version of StitchTool v" + latestVersion + " is available for download. \nWould you like to visit the website to download it?");
            a.showAndWait();
            if (a.getResult().getText().equals("OK")) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://github.com/Aeonss/StitchTool/releases/latest"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    // Creates a properties file if it doesn't exist
    public void initialization() throws IOException {
        // Creates a properties file if it does not exist
        String name = System.getProperty("user.home") + File.separator + "stitchtool-config.properties";
        File f = new File(name);
        if (f.exists()) {
            return;
        }

        OutputStream file = new FileOutputStream(name);
        file.close();

        Properties p = new Properties();
        FileInputStream i = new FileInputStream(name);
        p.load(i);
        i.close();

        OutputStream o = new FileOutputStream(name);
        p.setProperty("inputPath", System.getProperty("user.home") + File.separator);
        p.setProperty("outputPath", System.getProperty("user.home") + File.separator);
        p.setProperty("waifuPath", null);
        p.setProperty("imagePath", System.getProperty("user.home") + File.separator);
        p.setProperty("watermarkPath", System.getProperty("user.home") + File.separator);
        p.setProperty("lastAction", "STITCHSPLIT");
        p.setProperty("actionOption", "VERTICAL");
        p.setProperty("ssOption", "Stitch Vertically, Smart Split");
        p.setProperty("fileOption", "PNG");
        p.setProperty("modelOption", "cunet");

        p.store(o, null);
        o.close();
    }
}
