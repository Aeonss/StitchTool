import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;

public class Controller implements Initializable {

    @FXML private TextField inputField;
    @FXML private Button inputFolderBTN;
    @FXML private Button inputImagesBTN;
    @FXML private TextField outputField;
    @FXML private TextField nameField;
    @FXML private RadioButton stitchVert;
    @FXML private RadioButton stitchHor;
    @FXML private RadioButton splitHor;
    @FXML private RadioButton splitVert;
    @FXML private Slider opacitySlider;
    @FXML private CheckBox greyOption;
    @FXML private Text title;

    private String inputPath = getConfig("inputPath");
    private String outputPath = getConfig("outputPath");
    private String imagePath = getConfig("imagePath");
    private String watermarkPath = getConfig("watermarkPath");

    private List<File> files = new ArrayList<>();

    private final String[] EXTENSIONS = new String[] {"png", "jpg", "jpeg"};
    private final FilenameFilter IMAGE_FILTER = (dir, name) -> {
        for (final String ext : EXTENSIONS) {
            if (name.endsWith("." + ext)) {
                return (true);
            }
        }
        return (false);
    };

    // Default Constructor
    public Controller() throws IOException {
    }

    // Initializes the Controller Class
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            File f1 = new File(getConfig("inputPath"));
            File f2 = new File(getConfig("outputPath"));
            File f3 = new File(getConfig("imagePath"));
            File f4 = new File(getConfig("watermarkPath"));

            if (getConfig("inputPath").equals("") || !f1.isDirectory()) {
                inputPath = System.getProperty("user.home") + "\\";
                setConfig("inputPath", System.getProperty("user.home") + "\\");
            } else if (getConfig("outputPath").equals("") || !f2.isDirectory()) {
                outputPath = System.getProperty("user.home") + "\\";
                setConfig("outputPath", System.getProperty("user.home") + "\\");
            } else if (getConfig("imagePath").equals("") || !f3.isDirectory()) {
                imagePath = System.getProperty("user.home") + "\\";
                setConfig("outputPath", System.getProperty("user.home") + "\\");
            } else if (getConfig("watermarkPath").equals("") || !f4.isDirectory()) {
                watermarkPath = System.getProperty("user.home") + "\\";
                setConfig("outputPath", System.getProperty("user.home") + "\\");
            }
        } catch (IOException io) { io.printStackTrace(); }

        files = new ArrayList<>();
        outputField.setText(outputPath);
    }

    public void onInputFolderBTN(MouseEvent e) throws IOException {
        files = new ArrayList<>();

        DirectoryChooser input = new DirectoryChooser();
        input.setTitle("Select Folder of Images");
        input.setInitialDirectory(new File(inputPath));
        File folder = input.showDialog(title.getScene().getWindow());

        if (folder == null) {
            reset();
            return;
        }

        inputPath = folder.getParent();
        setConfig("inputPath", folder.getParentFile().getAbsolutePath() + "\\");

        for (File image : folder.listFiles(IMAGE_FILTER)) {
            files.add(image);
        }

        if ((files.size() <= 1 && (stitchVert.isSelected() || stitchHor.isSelected()))) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setHeaderText(null);
            error.setContentText("Not enough images selected!");
            error.showAndWait();
            reset();
        } else {
            inputField.setText(folder.getAbsolutePath() + "\\");
            if ((stitchVert.isSelected() || stitchHor.isSelected()) && nameField.getText().isEmpty()) {
                nameField.setText(folder.getName());
            }
        }
    }

    public void onInputImagesBTN(MouseEvent e) throws IOException {
        files = new ArrayList<>();

        FileChooser input = new FileChooser();
        input.setTitle("Select Folder of Images");
        input.setInitialDirectory(new File(inputPath));
        input.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        if (stitchVert.isSelected() || stitchHor.isSelected()) {
            files = input.showOpenMultipleDialog(title.getScene().getWindow());
            if (files == null) {
                files = new ArrayList<>();
                reset();
                return;
            }
        } else if (splitHor.isSelected() || splitVert.isSelected()) {
            File f = input.showOpenDialog(title.getScene().getWindow());
            if (f == null) {
                reset();
                return;
            } else {
                files.add(f);
            }
        }

        inputPath = files.get(0).getParentFile().getAbsolutePath() + "\\";
        setConfig("inputPath", files.get(0).getParentFile().getAbsolutePath() + "\\");

        if (files.size() <= 1 && (stitchVert.isSelected() || stitchHor.isSelected())) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("At least 2 images is needed to stitch!");
            a.showAndWait();
            reset();
            return;
        }

        inputField.setText(files.get(0).getParentFile().getAbsolutePath() + "\\");

        if ((stitchVert.isSelected() || stitchHor.isSelected()) && nameField.getText().isEmpty()) {
            nameField.setText(files.get(0).getParentFile().getName());
        } else if ((splitVert.isSelected() || splitHor.isSelected()) & nameField.getText().isEmpty()) {
            nameField.setText("StitchTool-");
        }
    }

    public void onOutputBTN(MouseEvent e) throws IOException {
        DirectoryChooser output = new DirectoryChooser();
        if (getConfig("inputPath").isEmpty()) {
            output.setInitialDirectory(new File(System.getProperty("user.home")));
        } else {
            output.setInitialDirectory(new File(outputPath));
        }
        File folder = output.showDialog(title.getScene().getWindow());

        if (folder == null) {
            reset();
            return;
        }

        outputField.setText(folder.getAbsolutePath() + "\\");
        outputPath = folder.getAbsolutePath() + "\\";
        setConfig("outputPath", folder.getAbsolutePath() + "\\");
    }

    public void onStitchVert(MouseEvent e) {
        inputField.setText("");
        files = new ArrayList<>();
        nameField.setText("");
        inputImagesBTN.setText("Import Images");
        inputFolderBTN.setVisible(true);

        inputImagesBTN.setTranslateX(420);
        inputImagesBTN.setTranslateY(-30);
        inputImagesBTN.setPrefWidth(120.0);
    }

    public void onStitchHor(MouseEvent e) {
        inputField.setText("");
        files = new ArrayList<>();
        nameField.setText("");
        inputImagesBTN.setText("Import Images");
        inputFolderBTN.setVisible(true);

        inputImagesBTN.setTranslateX(420);
        inputImagesBTN.setTranslateY(-30);
        inputImagesBTN.setPrefWidth(120.0);
    }

    public void onSplitHor(MouseEvent e) {
        inputField.setText("");
        files = new ArrayList<>();
        nameField.setText("StitchTool-");
        inputImagesBTN.setText("Import Image");
        inputFolderBTN.setVisible(false);

        inputImagesBTN.setTranslateX(310);
        inputImagesBTN.setTranslateY(-30);
        inputImagesBTN.setPrefWidth(220.0);
    }

    public void onSplitVert(MouseEvent e) {
        inputField.setText("");
        files = new ArrayList<>();
        nameField.setText("StitchTool-");
        inputImagesBTN.setText("Import Image");
        inputFolderBTN.setVisible(false);

        inputImagesBTN.setTranslateX(310);
        inputImagesBTN.setTranslateY(-30);
        inputImagesBTN.setPrefWidth(220.0);
    }

    public void onRunBTN(MouseEvent e) throws IOException, InterruptedException {
        if (files.size() == 0) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Please select an input!");
            a.showAndWait();
        } else if (nameField.getText().equals("")) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Please enter an image name!");
            a.showAndWait();
        } else {
            if (files.size() > 1) {
                List<File> temp = new ArrayList<>(files);
                Collections.sort(temp, Comparator.comparing(File::getName, new FileNameComparator()));
                files = new ArrayList<>(temp);
            }
            loadImages(files);
        }
    }

    public void onWatermarkBTN(MouseEvent e) throws IOException {
        // Requires name to start
        if (nameField.getText().isEmpty()) {
            nameField.setText("StitchTool");
        }
        BufferedImage image;
        BufferedImage watermark;
        List<File> watermarkArray = new ArrayList<>();

        // Asks for the image to be watermarked
        FileChooser imageInput = new FileChooser();
        imageInput.setTitle("Select Image to Watermark!");
        imageInput.setInitialDirectory(new File(imagePath));
        File imageFile = imageInput.showOpenDialog(title.getScene().getWindow());

        if (imageFile == null) {
            reset();
            return;
        }

        watermarkArray.add(imageFile);

        image = ImageIO.read(watermarkArray.get(0));
        setConfig("imagePath", imageFile.getParentFile().getAbsolutePath());

        // Asks for the watermark image
        FileChooser watermarkInput = new FileChooser();
        watermarkInput.setTitle("Select the Watermark Image");
        watermarkInput.setInitialDirectory(new File(watermarkPath));
        File watermarkFile = watermarkInput.showOpenDialog(title.getScene().getWindow());

        if (watermarkFile == null) {
            reset();
            return;
        }

        watermarkArray.add(watermarkFile);

        watermark = ImageIO.read(watermarkArray.get(1));
        setConfig("watermarkPath", watermarkFile.getParentFile().getAbsolutePath());

        // Makes the watermark black and white
        if (greyOption.isSelected()) {
            ImageFilter imageFilter = new GrayFilter(true, 5);
            ImageProducer producer = new FilteredImageSource(watermark.getSource(), imageFilter);
            watermark = toBufferedImage(Toolkit.getDefaultToolkit().createImage(producer));
        }

        // Rescales the image
        double ratio = (double) watermark.getWidth() / watermark.getHeight();
        Image tempImage = watermark.getScaledInstance(image.getWidth() / 5,
                (int)(image.getWidth() / 5 * ratio), Image.SCALE_DEFAULT);

        watermark = toBufferedImage(tempImage);

        // Asks how many times the watermark will show up
        TextInputDialog input = new TextInputDialog();
        input.setHeaderText(null);
        input.setContentText("How many watermarks would you like the image to have?");
        input.showAndWait();
        if (input.getResult() == null) {
            return;
        }

        // Makes sure the input is a number
        int num = 0;
        while (num <= 0) {
            try {
                num = Integer.parseInt(input.getResult());
            } catch (NumberFormatException nfe) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("Please input a number only!");
                a.showAndWait();
                input.showAndWait();
                if (input.getResult() == null) {
                    return;
                }
            }
        }

        // Loop to draw the watermark
        Graphics2D g = image.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)opacitySlider.getValue() / 100));
        for (int i = 1; i < num + 1; i++) {
            int size = image.getHeight() / num;
            g.drawImage(watermark, (image.getWidth() - ((int)(watermark.getWidth()*1.1))),
                    (image.getHeight() - (size * i)), null);
        }
        g.dispose();

        // Exports the image
        try {
            ImageIO.write(image, "PNG", new File(outputPath + nameField.getText() + ".png"));
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setContentText("The image has been watermarked " + num + " times!");
            a.showAndWait();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        watermarkArray.clear();
    }

    // Loads the selected images for stitching/splitting
    public void loadImages(List<File> files) throws IOException, InterruptedException {
        ArrayList<BufferedImage> images = new ArrayList<>();
        for (File image : files) {
            BufferedImage b = ImageIO.read(image);
            images.add(b);
        }

        if (stitchVert.isSelected()) {
            verticalStitch(images);
        } else if (stitchHor.isSelected()){
            horizontalStitch(images);
        } else if (splitVert.isSelected()) {
            imageSplit(images.get(0));
        } else if (splitHor.isSelected()) {
            imageSplit(images.get(0));
        }
    }

    // Stitches an ArrayList of images vertically
    public void verticalStitch(ArrayList<BufferedImage> images) throws IOException {
        int concatHeight = 0;

        // Gets the total height of the stitches image and prepares a Graphics2D for it
        for (BufferedImage b : images) {
            concatHeight += b.getHeight();
            Graphics2D g2d = b.createGraphics();
            g2d.dispose();
        }

        int heightCurr = 0;

        BufferedImage concatImage = new BufferedImage(images.get(0).getWidth(), concatHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = concatImage.createGraphics();

        // Draws the images on the Graphics2D
        for (BufferedImage b : images) {
            g2d.drawImage(b, 0, heightCurr, null);
            heightCurr += b.getHeight();
        }
        g2d.dispose();

        // Exports the image
        ImageIO.write(concatImage, "PNG", new File(outputPath + nameField.getText() + ".png"));
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(null);
        a.setContentText(files.size() + " images were successfully stitched!");
        a.showAndWait();
    }

    // Stitches an ArrayList of images horizontally
    public void horizontalStitch(ArrayList<BufferedImage> images) throws IOException {
        int concatWidth = 0;

        // Gets the total width of the stitches image and prepares a Graphics2D for it
        for (BufferedImage b : images) {
            concatWidth += b.getWidth();
            Graphics2D g2d = b.createGraphics();
            g2d.dispose();
        }

        int widthCurr = 0;
        BufferedImage concatImage = new BufferedImage(concatWidth, images.get(0).getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = concatImage.createGraphics();

        // Draws the images on the Graphics2D
        for (BufferedImage b : images) {
            g2d.drawImage(b, widthCurr, 0, null);
            widthCurr += b.getWidth();
        }
        g2d.dispose();

        // Exports the image
        ImageIO.write(concatImage, "PNG", new File(outputPath + nameField.getText() + ".png"));
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(null);
        a.setContentText(files.size() + " images were successfully stitched!");
        a.showAndWait();
    }

    // Splits a BufferedImage vertically or horizontally
    public void imageSplit(BufferedImage source) throws IOException {

        JFrame frame = new JFrame();
        boolean split = true;
        int num = 0;

        while (split) {
            // Asks the user how many images they want
            TextInputDialog input = new TextInputDialog();
            input.setHeaderText(null);
            input.setContentText("How many images would you like it to be split into?");
            input.showAndWait();
            if (input.getResult() == null) {
                return;
            }

            // Checks if the input is a number
            try {
                num = Integer.parseInt(input.getResult());
            } catch (NumberFormatException nfe) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("Please input a number only!");
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
                // Creates a preview of the window
                previewImage(frame, source, num);

                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setHeaderText(null);
                a.setContentText("Continue with these splits?");
                a.showAndWait();
                if (a.getResult().getText().equalsIgnoreCase("OK")) {
                    split = false;
                }
                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            }
        }

        double height = (double) source.getHeight() / num;
        double width = (double) source.getWidth() / num;

        // Checks if splitting vertically/horizontally that it can be split evenly
        if ((splitHor.isSelected() && height % 1 != 0) || (splitVert.isSelected() && width % 1 != 0)) {
            ArrayList<Integer> divisible = new ArrayList<>();

            // Adds divisible numbers to an array to be shown
            if (splitHor.isSelected()) {
                for (int i = 2; i < source.getHeight(); i ++) {
                    if (source.getHeight() % i == 0 && i <= 100) {
                        divisible.add(i);
                    }
                }
            } else if (splitVert.isSelected()) {
                for (int k = 2; k < source.getWidth(); k++) {
                    if (source.getWidth() % k == 0 && k <= 100) {
                        divisible.add(k);
                    }
                }
            }

            // Lets the user know the image cannot be divided evenly. Allows them to continue or not.
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setHeaderText("Do you wish to continue?");
            a.setContentText("The image cannot be divided into " + num +
                    " images evenly. \n You can divide the image equally into: " +
                    divisible.toString() + " images! \n");
            a.showAndWait();

            if (a.getResult().getText().equalsIgnoreCase("Cancel")) {
                return;
            }
        }

        int i = 1;
        new File(outputPath + "\\StitchTool\\").mkdirs();

        if (splitHor.isSelected()) {
            int r = source.getHeight() % num;
            int parts = (source.getHeight() - r) / num;

            // Exports the images into a folder
            for (int y = 0; y < source.getHeight() - r; y += parts) {
                ImageIO.write(source.getSubimage(0, y, source.getWidth(), parts), "PNG",
                        new File(outputPath + "\\StitchTool\\" + nameField.getText() + i++ + ".png"));
            }

            // Exports the remainder if split unevenly
            if (r != 0) {
                ImageIO.write(source.getSubimage(0, source.getHeight() - (parts * num), source.getWidth(), r), "PNG",
                        new File(outputPath + "\\StitchTool\\" + nameField.getText() + i + ".png"));
            }
        } else {
            int r = source.getWidth() % num;
            int parts = source.getWidth() / num;

            // Exports the images into a folder
            for (int x = 0; x < (source.getWidth() - r) / num; x += parts) {
                ImageIO.write(source.getSubimage(x, 0, parts, source.getHeight()), "PNG",
                        new File(outputPath + "\\StitchTool\\" + nameField.getText() + i++ + ".png"));
            }

            // Exports the remainder if split unevenly
            if (r != 0) {
            ImageIO.write(source.getSubimage(source.getWidth() - (parts * num), 0, source.getHeight(), r), "PNG",
                    new File(outputPath + "\\StitchTool\\" + nameField.getText() + i + ".png"));
            }
        }

        // Finished message
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(null);
        a.setContentText(num + " images were successfully created!");
        a.showAndWait();
    }

    // Custom comparator class that makes sure files are in alpha-numerical order
    public static final class FileNameComparator implements Comparator<String> {
        public final Pattern NUMBERS =
                Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        @Override public final int compare(String o1, String o2) {
            // Optional "NULLS LAST" semantics:
            if (o1 == null || o2 == null)
                return o1 == null ? o2 == null ? 0 : -1 : 1;

            // Splitting both input strings by the above patterns
            String[] split1 = NUMBERS.split(o1);
            String[] split2 = NUMBERS.split(o2);
            for (int i = 0; i < Math.min(split1.length, split2.length); i++) {
                char c1 = split1[i].charAt(0);
                char c2 = split2[i].charAt(0);
                int cmp = 0;

                // If both segments start with a digit, sort them numerically using
                // BigInteger to stay safe
                if (c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9')
                    cmp = new BigInteger(split1[i]).compareTo(new BigInteger(split2[i]));

                // If we haven't sorted numerically before, or if numeric sorting yielded
                // equality (e.g 007 and 7) then sort lexicographically
                if (cmp == 0)
                    cmp = split1[i].compareTo(split2[i]);

                // Abort once some prefix has unequal ordering
                if (cmp != 0)
                    return cmp;
            }

            // If we reach this, then both strings have equally ordered prefixes, but
            // maybe one string is longer than the other (i.e. has more segments)
            return split1.length - split2.length;
        }
    }

    // Converts Image to BufferedImage
    public BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = image.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return image;
    }

    // Custom JPanel that creates a scrollable image
    public class ScrollPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private BufferedImage image;
        private JPanel canvas;

        public ScrollPanel(BufferedImage image, int num) {
            this.image = image;
            this.canvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(image, 0, 0, null);

                    int parts = image.getHeight() / num;

                    for (int i = parts; i < image.getHeight() - (parts/2); i += parts) {
                        g.setColor(Color.RED);
                        g.drawLine(0, i, image.getWidth(), i);
                    }
                }
            };
            canvas.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            JScrollPane sp = new JScrollPane(canvas);
            setLayout(new BorderLayout());
            add(sp, BorderLayout.CENTER);
        }
    }

    // Creates a new JFrame that uses the ScrollPanel
    public void previewImage(JFrame f, BufferedImage source, int num) {
        int height = source.getHeight();
        int width = source.getWidth();
        double ratio = (double) width / height;

        // Rescales the image to 1/3 the size
        Image tempImage = source.getScaledInstance((int) (height / 3 * ratio), height / 3, Image.SCALE_FAST);
        BufferedImage previewImage = new BufferedImage((int) (height / 3 * ratio), height / 3, BufferedImage.TYPE_INT_ARGB);
        previewImage.getGraphics().drawImage(tempImage, 0, 0, null);

        // Creates a new JFrame window
        JPanel p = new ScrollPanel(toBufferedImage(previewImage), num);
        f.setContentPane(p);
        f.setResizable(false);
        f.setSize(700, 700);
        f.setVisible(true);
    }

    // Clears the files selected and various components
    public void reset() {
        inputField.setText("");
        nameField.setText("");
        files = new ArrayList<>();
    }

    // Gets the value in the config
    public String getConfig(String key) throws IOException {
        Properties p = new Properties();
        FileInputStream i = new FileInputStream(System.getProperty("user.home") + File.separator + "stitchtool-config.properties");
        p.load(i);
        i.close();
        return p.getProperty(key);
    }

    // Writes a new value in the config
    public void setConfig(String key, String value) throws IOException{
        Properties p = new Properties();
        FileInputStream i = new FileInputStream(System.getProperty("user.home") + File.separator + "stitchtool-config.properties");
        p.load(i);
        i.close();

        OutputStream o = new FileOutputStream(System.getProperty("user.home") + File.separator + "stitchtool-config.properties");
        p.setProperty(key, value);
        p.store(o, null);
        o.close();
    }
}
