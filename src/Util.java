import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Properties;
import java.util.Random;

public class Util {

    // Gets the color of a pixel
    public static Color getColor(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new Color(red, green, blue, alpha);
    }

    // Creates a new JFrame that uses the ScrollPanel
    public static void previewImage(JFrame f, BufferedImage source, int num) {
        int height = source.getHeight();
        int width = source.getWidth();
        double ratio = (double) width / height;

        // Rescales the image to 1/3 the size
        Image tempImage = source.getScaledInstance((int) (height / 3 * ratio), height / 3, Image.SCALE_FAST);
        BufferedImage previewImage = new BufferedImage((int) (height / 3 * ratio), height / 3, BufferedImage.TYPE_INT_ARGB);
        previewImage.getGraphics().drawImage(tempImage, 0, 0, null);

        // Creates a new JFrame window
        JPanel p = new ScrollPanel(previewImage, num);
        f.setContentPane(p);
        f.setResizable(false);
        f.setSize(700, 700);
        f.setVisible(true);
    }

    // Gets the value in the config
    public static String getConfig(String key) {
        Properties p = new Properties();
        try {
            FileInputStream i = new FileInputStream(System.getProperty("user.home") + File.separator + "stitchtool-config.properties");
            p.load(i);
            i.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Error getting the config! Please create an issue on the github page!");
            a.showAndWait();
        }
        return p.getProperty(key);
    }

    // Writes a new value in the config
    public static void setConfig(String key, String value) {
        try {
            Properties p = new Properties();
            FileInputStream i = new FileInputStream(System.getProperty("user.home") + File.separator + "stitchtool-config.properties");
            p.load(i);
            i.close();

            OutputStream o = new FileOutputStream(System.getProperty("user.home") + File.separator + "stitchtool-config.properties");
            p.setProperty(key, value);
            p.store(o, null);
            o.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Error writing to the config! Please create an issue on the github page!");
            a.showAndWait();
        }
    }

    // Generates a random name for the file
    public static String generateString(int n) {
        final String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder s = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            s.append(chars.charAt(r.nextInt(chars.length())));
        }
        return s.toString();

    }

    // Checks if the String is a number
    public static boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    // Ask the user how many images he wants the image to be split into
    public static int askSplitImages() {
        int num;
        while (true) {
            // Asks the user how many images they want
            TextInputDialog input = new TextInputDialog();
            input.setHeaderText(null);
            input.setContentText("How many images would you like the image to be split into?");
            input.showAndWait();
            if (input.getResult() == null) {
                return -1;
            }

            // Checks if the input is a number
            try {
                num = Integer.parseInt(input.getResult());
            } catch (NumberFormatException nfe) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("Please input numbers only!");
                a.showAndWait();
                continue;
            }

            // Makes sure they're not inputting negative numbers or trying to split into 1 image (?)
            if (num < 2) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("Please input a number that is at least 2!");
                a.showAndWait();
            } else {
                return num;
            }
        }
    }

    // Converts Image to BufferedImage
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = image.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return image;
    }

}
