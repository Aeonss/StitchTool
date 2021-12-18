import com.luciad.imageio.webp.WebPReadParam;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.*;
import java.awt.*;
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

    // FXML Variables
    @FXML private TextField inputField;
    @FXML private Button inputFolderBTN;
    @FXML private Button inputImagesBTN;
    @FXML private TextField outputField;
    @FXML private TextField nameField;
    @FXML private ChoiceBox<String> fileOptions;

    @FXML private GridPane mainPane;
    @FXML private FlowPane waifuPane;
    @FXML private FlowPane actionPane;
    @FXML private FlowPane watermarkPane;

    @FXML private TextField waifuField;
    @FXML private CheckBox denoiseBox;
    @FXML private Slider denoiseSlider;
    @FXML private ChoiceBox<String> modelOptions;
    @FXML private CheckBox scaleBox;
    @FXML private TextField scaleField;
    @FXML private TextField scaleHeightField;
    @FXML private TextField scaleWidthField;

    @FXML private RadioButton stitchSplit;
    @FXML private RadioButton stitch;
    @FXML private RadioButton split;
    @FXML private RadioButton smartSplit;
    @FXML private ChoiceBox<String> stitchSplitOptions;
    @FXML private RadioButton vertical;
    @FXML private RadioButton horizontal;

    @FXML private Slider opacitySlider;
    @FXML private CheckBox greyOption;
    @FXML private Text title;

    // Read from config
    private String inputPath;
    private String outputPath;
    private String waifuPath;
    private String imagePath;
    private String watermarkPath;
    private String lastAction;
    private String actionOption;
    private String ssOption;
    private String fileOption;
    private String modelOption;

    private String[] options;

    // List of files
    private List<File> files;

    // Makes sure input files are .png, .jpg, or .jpeg
    private final String[] EXTENSIONS = new String[] {"png", "jpg", "jpeg", "webp"};
    private final FilenameFilter IMAGE_FILTER = (dir, name) -> {
        for (final String ext : EXTENSIONS) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    };

    // Default Constructor
    public Controller() {
        inputPath = getConfig("inputPath");
        outputPath = getConfig("outputPath");
        waifuPath = getConfig("waifuPath");
        imagePath = getConfig("imagePath");
        watermarkPath = getConfig("watermarkPath");
        lastAction = getConfig("lastAction");
        actionOption = getConfig("actionOption");
        ssOption = getConfig("ssOption");
        fileOption = getConfig("fileOption");
        modelOption = getConfig("modelOption");
        files = new ArrayList<>();
    }

    // Called after constructor
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        final String[] files = new String[]{"PNG", "JPG"};
        final String[] models = new String[]{"cunet", "upresnet10", "upconv_7_anime_style_art_rgb", "upconv_7_photo", "anime_style_art_rgb", "photo", "anime_style_art_y"};
        final String[] actions = new String[]{"STITCHSPLIT", "STITCH", "SPLIT", "SMARTSPLIT"};
        options = new String[]{"Stitch Vertically, Smart Split", "Stitch Vertically, Split Horizontally",
                "Stitch Vertically, Split Vertically", "Stitch Horizontally, Smart Split",
                "Stitch Horizontally, Split Horizontally", "Stitch Horizontally, Split Vertically"};


        // Checks to make sure the config is correct
        File f1 = new File(inputPath);
        File f2 = new File(outputPath);
        File f3 = new File(imagePath);
        File f4 = new File(watermarkPath);

        if (inputPath == null || !f1.isDirectory()) {
            inputPath = System.getProperty("user.home") + File.separator;
            setConfig("inputPath", inputPath);
        }
        if (outputPath == null || !f2.isDirectory()) {
            outputPath = System.getProperty("user.home") + File.separator;
            setConfig("outputPath", outputPath);
        }
        if (getConfig("waifuPath") != null) {
            waifuField.setText(waifuPath);
        }
        if (imagePath == null || !f3.isDirectory()) {
            imagePath = System.getProperty("user.home") + File.separator;
            setConfig("imagePath", imagePath);
        }
        if (watermarkPath == null || !f4.isDirectory()) {
            watermarkPath = System.getProperty("user.home") + File.separator;
            setConfig("watermarkPath", watermarkPath);
        }
        if (lastAction == null || !Arrays.asList(actions).contains(lastAction)) {
            lastAction = "STITCHSPLIT";
            setConfig("lastAction", lastAction);
        }
        if (actionOption == null) {
            actionOption = "VERTICAL";
            setConfig("actionOption", actionOption);
        }
        if (ssOption == null || !Arrays.asList(options).contains(ssOption)) {
            ssOption = "Stitch Vertically, Smart Split";
            setConfig("ssOption", ssOption);
        }
        if (fileOption == null || !Arrays.asList(files).contains(fileOption)) {
            fileOption = "PNG";
            setConfig("fileOption", "PNG");
        }
        if (modelOption == null || !Arrays.asList(models).contains(modelOption)) {
            modelOption = "cunet";
            setConfig("modelOption", "cunet");
        }

        outputField.setText(outputPath);

        final ObservableList<String> fileList = FXCollections.observableArrayList(files);
        fileOptions.setItems(fileList);
        fileOptions.setValue(fileOption);

        final ObservableList<String> optionsList = FXCollections.observableArrayList(options);
        stitchSplitOptions.setItems(optionsList);
        stitchSplitOptions.setValue(ssOption);

        final ObservableList<String> modelList = FXCollections.observableArrayList(models);
        modelOptions.setItems(modelList);
        modelOptions.setValue(modelOption);

        switch (lastAction) {
            case "STITCHSPLIT":
                stitchSplit.setSelected(true);
                break;
            case "STITCH":
                stitch.setSelected(true);
                break;
            case "SPLIT":
                split.setSelected(true);
                break;
            case "SMARTSPLIT":
                smartSplit.setSelected(true);
                break;
        }

        if (actionOption.equalsIgnoreCase("VERTICAL")) {
            vertical.isSelected();
        }
        else if (actionOption.equalsIgnoreCase("HORIZONTAL")) {
            horizontal.isSelected();
        }

        if (fileOption.equalsIgnoreCase("PNG")) {
            fileOptions.setValue("PNG");
        } else {
            fileOptions.setValue("JPG");
        }

        updateGUI();
        onDenoise();
        onScale();
    }

    // Updates the GUI when action is switched
    public void updateGUI() {
        if (files == null) {
            files = new ArrayList<>();
        }

        // Changing from stitching to splitting
        if (files.size() >= 1 && (smartSplit.isSelected() || split.isSelected())) {
            files = new ArrayList<>();
            inputField.setText("");
            nameField.setText("");

            inputImagesBTN.setText("Import Image");
            inputFolderBTN.setVisible(false);
            inputImagesBTN.setPrefWidth(220);
            inputImagesBTN.setTranslateX(310);
            inputImagesBTN.setTranslateY(-50);
        }

        // Changing from splitting to stitching
        else if (files.size() == 1 && (stitchSplit.isSelected() || stitch.isSelected())) {
            files = new ArrayList<>();
            inputField.setText("");
            nameField.setText("");

            inputImagesBTN.setText("Import Images");
            inputFolderBTN.setVisible(true);
            inputImagesBTN.setPrefWidth(100.0);
            inputImagesBTN.setTranslateX(0);
            inputImagesBTN.setTranslateY(0);
        }

        // No image has been selected yet
        else {
            if (stitchSplit.isSelected()) {
                inputImagesBTN.setText("Import Images");
                inputFolderBTN.setVisible(true);
                inputImagesBTN.setPrefWidth(100.0);
                inputImagesBTN.setTranslateX(0);
                inputImagesBTN.setTranslateY(0);
                vertical.setDisable(true);
                horizontal.setDisable(true);
                stitchSplitOptions.setDisable(false);
            }
            else if (stitch.isSelected()) {
                inputImagesBTN.setText("Import Images");
                inputFolderBTN.setVisible(true);
                inputImagesBTN.setPrefWidth(100.0);
                inputImagesBTN.setTranslateX(0);
                inputImagesBTN.setTranslateY(0);
                vertical.setDisable(false);
                horizontal.setDisable(false);
                stitchSplitOptions.setDisable(true);

            }
            else if (split.isSelected()) {
                inputImagesBTN.setText("Import Image");
                inputFolderBTN.setVisible(false);
                inputImagesBTN.setPrefWidth(220);
                inputImagesBTN.setTranslateX(330);
                inputImagesBTN.setTranslateY(-50);
                vertical.setDisable(false);
                horizontal.setDisable(false);
                stitchSplitOptions.setDisable(true);
            }
            else if (smartSplit.isSelected()) {
                inputImagesBTN.setText("Import Image");
                inputFolderBTN.setVisible(false);
                inputImagesBTN.setPrefWidth(220);
                inputImagesBTN.setTranslateX(330);
                inputImagesBTN.setTranslateY(-50);
                vertical.setDisable(true);
                horizontal.setDisable(true);
                stitchSplitOptions.setDisable(true);
            }
        }

    }

    // ImportFolder button pressed
    public void onImportFolder() {
        // Empty list of existing images
        files = new ArrayList<>();

        // Ask user to select a folder
        DirectoryChooser input = new DirectoryChooser();
        input.setTitle("Select Folder of Images");
        input.setInitialDirectory(new File(inputPath));
        File folder = input.showDialog(title.getScene().getWindow());

        // If no folder is selected
        if (folder == null) {
            return;
        }

        // Updates config
        inputPath = folder.getParentFile().getAbsolutePath() + File.separator;
        setConfig("inputPath", inputPath);

        // Adds all images in the folder to list
        File[] images = folder.listFiles(IMAGE_FILTER);
        if (images == null) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("No images in folder!");
            a.showAndWait();
            return;
        }

        files.addAll(Arrays.asList(images));

        // Updates GUI
        inputField.setText(folder.getAbsolutePath() + File.separator);
        if (nameField.getText().isEmpty()) {
            nameField.setText(folder.getName());
        }
    }

    // Import images button pressed
    public void onImportImages() {
        // Empty list of existing images
        files = new ArrayList<>();

        // Ask user to select image(s)
        FileChooser input = new FileChooser();
        input.setTitle("Select Folder of Images");
        input.setInitialDirectory(new File(inputPath));
        input.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));

        // Add all images to list if user is stitching
        if (stitchSplit.isSelected() || stitch.isSelected()) {
            files = input.showOpenMultipleDialog(title.getScene().getWindow());
            if (files == null) {
                return;
            }

            // If less than 2 images are chosen, alert user
            if (files.size() <= 1) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("At least 2 images is needed to stitch!");
                a.showAndWait();
                return;
            }
        }

        // Add image to list if user is splitting
        else if (split.isSelected() || smartSplit.isSelected()) {
            File f = input.showOpenDialog(title.getScene().getWindow());
            if (f == null) {
                return;
            }
            files.add(f);
        }

        // Update config
        inputPath = files.get(0).getParentFile().getAbsolutePath() + File.separator;
        setConfig("inputPath", inputPath);


        // Update GUI
        inputField.setText(files.get(0).getParentFile().getAbsolutePath() + File.separator);

        if ((stitchSplit.isSelected() || stitch.isSelected()) && nameField.getText().isEmpty()) {
            nameField.setText(files.get(0).getParentFile().getName());
        }
        else if ((split.isSelected() || smartSplit.isSelected()) && nameField.getText().isEmpty()) {
            nameField.setText("StitchTool-");
        }
    }

    // Output button pressed
    public void onOutput() {
        // Ask user to choose output directory
        DirectoryChooser output = new DirectoryChooser();
        output.setInitialDirectory(new File(outputPath));
        File folder = output.showDialog(title.getScene().getWindow());

        if (folder == null) {
            return;
        }

        outputField.setText(folder.getAbsolutePath() + File.separator);
        outputPath = folder.getAbsolutePath() + File.separator;
        setConfig("outputPath", outputPath);
    }

    // Waifu button pressed
    public void onWaifu() {
        // Ask user to select
        FileChooser input = new FileChooser();
        input.setTitle("Select Folder of Images");
        input.setInitialDirectory(new File(inputPath));
        input.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Waifu2X-Caffe CUI", "*.exe"));

        File f = input.showOpenDialog(title.getScene().getWindow());
        if (f == null) {
            return;
        }
        waifuPath = f.getAbsolutePath() + File.separator;
        setConfig("waifuPath", waifuPath);
        waifuField.setText(waifuPath);
    }

    public void onRunWaifu() {
        if (waifuPath == null) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Waifu2X_caffe_cui.exe was not found!");
            a.showAndWait();
            return;
        }

        // No options chosen
        String sf = scaleField.getText();
        String shf = scaleHeightField.getText();
        String swf = scaleWidthField.getText();

        if ((!denoiseBox.isSelected() || sf.equalsIgnoreCase("1")) && (sf.isEmpty() && shf.isEmpty() && swf.isEmpty())) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("No Waifu2X options were chosen!");
            a.showAndWait();
            return;
        }

        if (shf.isEmpty() && swf.isEmpty() && !sf.isEmpty()) {
            if (!isNumber(sf)) {
                scaleField.setText("1");
                scaleWidthField.clear();
                scaleHeightField.clear();
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("Please enter only numbers for the scale height and width!");
                a.showAndWait();
                return;
            }
        } else if (shf.isEmpty() && !swf.isEmpty() && !sf.isEmpty()) {
            if (!isNumber(shf) || !isNumber(swf)) {
                scaleField.setText("1");
                scaleWidthField.clear();
                scaleHeightField.clear();
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("Please enter only numbers for the scale ratio!");
                a.showAndWait();
                return;
            }
        }

        // Ask user to select image
        FileChooser input = new FileChooser();
        input.setTitle("Select image to Waifu2X");
        input.setInitialDirectory(new File(inputPath));
        input.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));

        // Add all images to list if user is stitching
        File f = input.showOpenDialog(title.getScene().getWindow());
        if (f == null) {
            return;
        }

        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(waifuField.getText().substring(0, waifuField.getLength()-1));
        cmd.add("-i");
        cmd.add("\"" + f.getAbsolutePath() + "\"");
        cmd.add("-m");

        // NOISE AND SCALE
        if (denoiseBox.isSelected() && scaleBox.isSelected()) {
            cmd.add("noise_scale");
            if (!shf.isEmpty() && !swf.isEmpty()) {
                cmd.add("-h");
                cmd.add(shf);
                cmd.add("-w");
                cmd.add(swf);
            } else if (!sf.isEmpty()) {
                cmd.add("-s");
                cmd.add(sf);
            }
            cmd.add("noise");
            cmd.add("-n");
            cmd.add(((int) denoiseSlider.getValue()) + "");
        }
        // ONLY NOISE
        else if (denoiseBox.isSelected() && !scaleBox.isSelected()) {
            cmd.add("noise");
            cmd.add("-n");
            cmd.add(((int) denoiseSlider.getValue()) + "");
        }
        else if (scaleBox.isSelected() && denoiseBox.isSelected()) {
            cmd.add("scale");
            if (!shf.isEmpty() && !swf.isEmpty()) {
                cmd.add("-h");
                cmd.add(shf);
                cmd.add("-w");
                cmd.add(swf);
            } else if (!sf.isEmpty()) {
                cmd.add("-s");
                cmd.add(sf);
            }
        }

        if (nameField.getText().isEmpty()) {
            nameField.setText(generateString(10));
        }

        cmd.add("-o");
        cmd.add("\"" + outputField.getText() + nameField.getText() + "." + fileOption + "\"");

        cmd.add("--model_dir");
        cmd.add(new File(waifuField.getText()).getParent() + File.separator + "models" + File.separator + modelOptions.getValue());

        try {
            Process p = new ProcessBuilder(cmd).start();
            p.waitFor();

        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Error using Waifu2X! Please create an issue on the github page!");
            a.showAndWait();
        }

        Alert a2 = new Alert(Alert.AlertType.INFORMATION);
        a2.setHeaderText(null);
        if (denoiseBox.isSelected() && scaleBox.isSelected()) {
            a2.setContentText("Image has been successfully denoised with level " + (int)denoiseSlider.getValue() + "and scaled by " + scaleField.getText() + "!");
        } else if (denoiseBox.isSelected() && !scaleBox.isSelected()) {
            a2.setContentText("Image has been successfully denoised with level " + (int)denoiseSlider.getValue());
        } else if (!denoiseBox.isSelected() && scaleBox.isSelected()) {
            a2.setContentText("Image has been successfully scaled by " + scaleField.getText() + "!");
        }
        a2.showAndWait();
    }

    // Denoise box toggled
    public void onDenoise() {
        if (denoiseBox.isSelected()) {
            denoiseSlider.setDisable(false);
            modelOptions.setDisable(false);
        } else {
            denoiseSlider.setDisable(true);
        }
        if (!denoiseBox.isSelected() && !scaleBox.isSelected()) {
            modelOptions.setDisable(true);
        }
    }

    // Scale box toggled
    public void onScale() {
        if (scaleBox.isSelected()) {
            scaleField.setDisable(false);
            scaleHeightField.setDisable(false);
            scaleWidthField.setDisable(false);
            modelOptions.setDisable(false);
        } else {
            scaleField.setDisable(true);
            scaleHeightField.setDisable(true);
            scaleWidthField.setDisable(true);
        }
        if (!denoiseBox.isSelected() && !scaleBox.isSelected()) {
            modelOptions.setDisable(true);
        }
    }

    // Dropdown button pressed
    public void onDrop(MouseEvent e) {
        switch (((Button)e.getSource()).getId()) {
            case "dropBTN1":
                if (waifuPane.isVisible()) {
                    waifuPane.setVisible(false);
                    ((Button) e.getSource()).setText("\uD83E\uDC1A");
                    mainPane.getRowConstraints().get(8).setPrefHeight(0);
                }
                else {
                    waifuPane.setVisible(true);
                    ((Button) e.getSource()).setText("\uD83E\uDC0B");
                    mainPane.getRowConstraints().get(8).setPrefHeight(200);
                }
                break;
            case "dropBTN2":
                if (actionPane.isVisible()) {
                    actionPane.setVisible(false);
                    ((Button) e.getSource()).setText("\uD83E\uDC1A");
                    mainPane.getRowConstraints().get(10).setPrefHeight(0);
                }
                else {
                    actionPane.setVisible(true);
                    ((Button) e.getSource()).setText("\uD83E\uDC0B");
                    mainPane.getRowConstraints().get(10).setPrefHeight(120);
                }
                break;
            case "dropBTN3":
                if (watermarkPane.isVisible()) {
                    watermarkPane.setVisible(false);
                    ((Button) e.getSource()).setText("\uD83E\uDC1A");
                    mainPane.getRowConstraints().get(12).setPrefHeight(0);
                }
                else {
                    watermarkPane.setVisible(true);
                    ((Button) e.getSource()).setText("\uD83E\uDC0B");
                    mainPane.getRowConstraints().get(12).setPrefHeight(70);
                }
                break;
            default:
                break;
        }
    }

    // Run button pressed
    public void onRun() {
        // No images in list
        if (files.size() == 0) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Please select image(s) to stitch or split!");
            a.showAndWait();
            return;
        }

        // Checks if the image has a name
        if (nameField.getText().isEmpty()) {
            nameField.setText(generateString(10));
        }

        // Stitch / Split
        try {
            // Sort the files in alphanumerical order and load them
            BufferedImage b;
            ArrayList<File> newFiles = new ArrayList<>(files);
            Collections.sort(newFiles, Comparator.comparing(File::getName, new FileNameComparator()));
            files = new ArrayList<>(newFiles);

            ArrayList<BufferedImage> images = new ArrayList<>();
            for (File image : files) {
                // Gets the extension of the file
                String fileName = image.toString();
                String extension = "";
                int index = fileName.lastIndexOf('.');
                if(index > 0) {
                    extension = fileName.substring(index + 1);
                }

                // If the image is a webp
                if (extension.equals("webp")) {
                    ImageReader reader = ImageIO.getImageReadersByMIMEType("image/webp").next();
                    WebPReadParam readParam = new WebPReadParam();
                    readParam.setBypassFiltering(true);
                    reader.setInput(new FileImageInputStream(image));
                    b = reader.read(0, readParam);
                }

                // PNG/JPG/JPEG
                else {
                    b = ImageIO.read(image);
                }
                images.add(b);
            }

            if (stitchSplit.isSelected()) {
                doStitchSplit(images);
                lastAction = "STITCHSPLIT";
                ssOption = stitchSplitOptions.getValue();
                setConfig("ssOption", ssOption);
            }

            else if (stitch.isSelected()) {
                doStitch(images);
                lastAction = "STITCH";
            }
            else if (split.isSelected()) {
                doImageSplit(images.get(0));
                lastAction = "SPLIT";
            }
            else if (smartSplit.isSelected()) {
                doSmartSplit(images.get(0));
                lastAction = "SMARTSPLIT";
            }
            setConfig("lastAction", lastAction);

            if (vertical.isSelected()) {
                actionOption = "VERTICAL";
            } else {
                actionOption = "HORIZONTAL";
            }
            setConfig("actionOption", actionOption);
            setConfig("fileOption", fileOption);
            setConfig("modelOption", modelOption);

        } catch (IOException ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Error Running! Please create an issue on the github page!");
            a.showAndWait();
        }
    }

    // Watermark button pressed
    public void onWatermark() throws IOException {
        // Asks for the image to be watermarked
        FileChooser imageInput = new FileChooser();
        imageInput.setTitle("Select Image to Watermark!");
        imageInput.setInitialDirectory(new File(imagePath));
        File imageFile = imageInput.showOpenDialog(title.getScene().getWindow());

        if (imageFile == null) {
            return;
        }

        // Updates config
        BufferedImage image = ImageIO.read(imageFile);
        imagePath = imageFile.getParentFile().getAbsolutePath() + File.separator;
        setConfig("imagePath", imagePath);

        // Asks for the watermark image
        FileChooser watermarkInput = new FileChooser();
        watermarkInput.setTitle("Select the Watermark Image");
        watermarkInput.setInitialDirectory(new File(watermarkPath));
        File watermarkFile = watermarkInput.showOpenDialog(title.getScene().getWindow());

        if (watermarkFile == null) {
            return;
        }

        // Updates config
        BufferedImage watermark = ImageIO.read(watermarkFile);
        watermarkPath = watermarkFile.getParentFile().getAbsolutePath() + File.separator;
        setConfig("watermarkPath", watermarkPath);

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

        // Checks if the image has a name
        if (nameField.getText().isEmpty()) {
            nameField.setText(imageFile.getName() + "_watermarked");
        }

        // Exports the image
        try {
            ImageIO.write(image, "PNG", new File(outputPath + nameField.getText() + ".png"));
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            a.setContentText("The image has been watermarked " + num + " times!");
            a.showAndWait();
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Error watermarking! Please create an issue on the github page!");
            a.showAndWait();
        }
    }

    // Stitch vertically/horizontally and Split horizontally/vertically/smart
    public void doStitchSplit(ArrayList<BufferedImage> images) throws IOException {
        String option = stitchSplitOptions.getValue();

        // Stitch vertically first
        int concat = 0;
        int curr = 0;
        boolean stitchVert = option.equalsIgnoreCase(options[0]) || option.equalsIgnoreCase(options[1]) || option.equalsIgnoreCase(options[2]);

        // STITCH
        for (BufferedImage b : images) {
            if (stitchVert) {
                concat += b.getHeight();
            } else {
                concat += b.getWidth();
            }
            Graphics2D g2d = b.createGraphics();
            g2d.dispose();
        }
        BufferedImage concatImage;
        if (stitchVert) {
            concatImage = new BufferedImage(images.get(0).getWidth(), concat, BufferedImage.TYPE_INT_RGB);
        } else {
            concatImage = new BufferedImage(concat, images.get(0).getHeight(), BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D g2d = concatImage.createGraphics();

        // Draws the images on the Graphics2D
        for (BufferedImage b : images) {
            if (stitchVert) {
                g2d.drawImage(b, 0, curr, null);
                curr += b.getHeight();
            } else {
                g2d.drawImage(b, curr, 0, null);
                curr += b.getWidth();
            }
        }
        g2d.dispose();

        int splitAction = -1;
        // Smart Split = 0
        if (option.equalsIgnoreCase(options[0]) || option.equalsIgnoreCase(options[3])) {
            splitAction = 0;
        }
        // Split horizontally = 1
        if (option.equalsIgnoreCase(options[1]) || option.equalsIgnoreCase(options[4])) {
            splitAction = 1;
        }
        // Split vertically = 2
        if (option.equalsIgnoreCase(options[2]) || option.equalsIgnoreCase(options[5])) {
            splitAction = 2;
        }


        // Split vertically or split horizontally
        if (splitAction == 1 || splitAction == 2) {
            // Ask user how many images the image is to be split into
            TextInputDialog input = new TextInputDialog();
            input.setHeaderText(null);
            input.setContentText("How many images would you like the image to be split into?");
            input.showAndWait();
            if (input.getResult() == null) {
                return;
            }
            int num = 0;
            boolean cont = true;
            while (cont) {
                try {
                    num = Integer.parseInt(input.getResult());
                } catch (NumberFormatException nfe) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setHeaderText(null);
                    a.setContentText("Please input numbers only!");
                    a.showAndWait();
                    continue;
                }

                if (num < 2) {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setHeaderText(null);
                    a.setContentText("Please input a number that is at least 2!");
                    a.showAndWait();
                } else {
                    cont = false;
                }
            }

            JFrame frame = new JFrame();
            frame.setTitle("Split Preview");
            frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));

            int i = 1;
            new File(outputPath + File.separator + "StitchTool" + File.separator).mkdirs();

            // Split horizontally
            if (splitAction == 1) {
                int r = concatImage.getHeight() % num;
                int parts = (concatImage.getHeight() - r) / num;

                // Exports the images into a folder
                for (int y = 0; y < concatImage.getHeight() - r; y += parts) {
                    File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i++ + "." + outputPNG());
                    ImageIO.write(concatImage.getSubimage(0, y, concatImage.getWidth(), parts), outputPNG(), f);
                    if (doWaifu(f)) {
                        f.delete();
                    }
                }

                // Exports the remainder if split unevenly
                if (r != 0) {
                    File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i + "." + outputPNG());
                    ImageIO.write(concatImage.getSubimage(0, concatImage.getHeight() - (parts * num), concatImage.getWidth(), r), outputPNG(), f);
                    if (doWaifu(f)) {
                        f.delete();
                    }
                }
            }

            // Split vertically
            else {
                int r = concatImage.getWidth() % num;
                int parts = concatImage.getWidth() / num;

                // Exports the images into a folder
                for (int x = 0; x < (concatImage.getWidth() - r); x += parts) {
                    File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i++ + "." + outputPNG());
                    ImageIO.write(concatImage.getSubimage(x, 0, parts, concatImage.getHeight()), outputPNG(), f);
                    if (doWaifu(f)) {
                        f.delete();
                    }
                }

                // Exports the remainder if split unevenly
                if (r != 0) {
                    File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i + "." + outputPNG());
                    ImageIO.write(concatImage.getSubimage(concatImage.getWidth() - (parts * num), 0, concatImage.getHeight(), r), outputPNG(), f);
                    if (doWaifu(f)) {
                        f.delete();
                    }
                }
            }

            // Finished message
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setHeaderText(null);
            a.setContentText("Successfully split into " + num + " images!");
            a.showAndWait();
            return;

        }

        // Smart split
        if (splitAction == 0) {
            doSmartSplit(concatImage);
        }

    }

    // Stitches an ArrayList of images vertically/horizontally
    public void doStitch(ArrayList<BufferedImage> images) throws IOException {
        int concat = 0;
        int curr = 0;
        for (BufferedImage b : images) {
            if (vertical.isSelected()) {
                concat += b.getHeight();
            }
            else if (horizontal.isSelected()) {
                concat += b.getWidth();
            }
            Graphics2D g2d = b.createGraphics();
            g2d.dispose();
        }

        BufferedImage concatImage;

        if (vertical.isSelected()) {
            concatImage = new BufferedImage(images.get(0).getWidth(), concat, BufferedImage.TYPE_INT_RGB);
        } else {
            concatImage = new BufferedImage(concat, images.get(0).getHeight(), BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g2d = concatImage.createGraphics();

        // Draws the images on the Graphics2D
        for (BufferedImage b : images) {
            if (vertical.isSelected()) {
                g2d.drawImage(b, 0, curr, null);
                curr += b.getHeight();
            }
            else if (horizontal.isSelected()) {
                g2d.drawImage(b, curr, 0, null);
                curr += b.getWidth();
            }
        }
        g2d.dispose();

        // Exports the image
        File f = new File(outputPath + nameField.getText() + "." + outputPNG());
        ImageIO.write(concatImage, outputPNG(), f);

        if (doWaifu(f)) {
            f.delete();
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(null);
        a.setContentText(files.size() + " images were successfully stitched!");
        a.showAndWait();
    }

    // Splits a BufferedImage vertically/horizontally
    public void doImageSplit(BufferedImage source) throws IOException {
        JFrame frame = new JFrame();
        frame.setTitle("Split Preview");
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));
        boolean split = true;
        int num = 0;

        while (split) {
            // Asks the user how many images they want
            TextInputDialog input = new TextInputDialog();
            input.setHeaderText(null);
            input.setContentText("How many images would you like the image to be split into?");
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
                // Creates a preview of the window
                boolean showedPreview = true;
                try {
                    previewImage(frame, source, num);
                } catch (Exception e) {
                    showedPreview = false;
                    e.printStackTrace();
                }

                Alert a = new Alert(Alert.AlertType.CONFIRMATION);
                a.setHeaderText(null);
                if (showedPreview) {
                    a.setContentText("Continue with these splits?");
                } else {
                    a.setContentText("Image too large to preview");
                }
                a.showAndWait();
                if (a.getResult().getText().equalsIgnoreCase("OK")) {
                    split = false;
                }
                frame.dispose();
            }
        }

        int i = 1;
        new File(outputPath + File.separator + "StitchTool" + File.separator).mkdirs();

        // Horizontal
        if (horizontal.isSelected()) {
            int r = source.getHeight() % num;
            int parts = (source.getHeight() - r) / num;

            // Exports the images into a folder
            for (int y = 0; y < source.getHeight() - r; y += parts) {
                File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i++ + "." + outputPNG());
                ImageIO.write(source.getSubimage(0, y, source.getWidth(), parts), outputPNG(), f);
                if (doWaifu(f)) {
                    f.delete();
                }
            }

            // Exports the remainder if split unevenly
            if (r != 0) {
                File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i + "." + outputPNG());
                ImageIO.write(source.getSubimage(0, source.getHeight() - (parts * num), source.getWidth(), r), outputPNG(), f);
                if (doWaifu(f)) {
                    f.delete();
                }
            }
        }

        // Vertical
        else {
            int r = source.getWidth() % num;
            int parts = source.getWidth() / num;

            // Exports the images into a folder
            for (int x = 0; x < (source.getWidth() - r); x += parts) {
                File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i++ + "." + outputPNG());
                ImageIO.write(source.getSubimage(x, 0, parts, source.getHeight()), outputPNG(), f);
                if (doWaifu(f)) {
                    f.delete();
                }
            }

            // Exports the remainder if split unevenly
            if (r != 0) {
                File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i + "." + outputPNG());
                ImageIO.write(source.getSubimage(source.getWidth() - (parts * num), 0, source.getHeight(), r), outputPNG(), f);
                if (doWaifu(f)) {
                    f.delete();
                }
            }
        }

        // Finished message
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(null);
        a.setContentText("Successfully split into " + num + " images!");
        a.showAndWait();
    }

    // Splits a BufferedImage by space (row of same colored pixels)
    public void doSmartSplit(BufferedImage source) throws IOException {
        boolean split = true;
        int num = 0;
        while (split) {
            // Asks the user how many images they want
            TextInputDialog input = new TextInputDialog();
            input.setHeaderText(null);
            input.setContentText("How many images would you like the image to be split into? (±1)");
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
                split = false;
            }
        }

        // Approximate size of each image
        int startHeight = source.getHeight() / num;
        ArrayList<Integer> splitHeights = new ArrayList<>();
        // Starts from 0
        splitHeights.add(0);

        for (int i = startHeight; i < source.getHeight(); i++ ) {
            for (int j = 0; j < source.getWidth() - 1; j++) {
                // Checks if the row of pixels is the same color, if not, moves 1 pixel down
                if (!ColorCompare(getColor(source.getRGB(j, i)),getColor(source.getRGB(j+1, i)))) {
                    break;
                }
                // Otherwise, it saves the height to split, and moves to the next part
                if (j+1 == source.getWidth() - 1) {
                    splitHeights.add(i);
                    i += startHeight;
                    break;
                }
            }
        }

        // Adds the remainder of the image to the array
        if (splitHeights.get(splitHeights.size() - 1) != source.getHeight()) {
            splitHeights.add(source.getHeight());
        }

        new File(outputPath + File.separator + "StitchTool" + File.separator).mkdirs();

        // Exports the images into a folder
        for (int i = 0; i < splitHeights.size() - 1; i++) {
            File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + (i+1) + "." + outputPNG());
            ImageIO.write(source.getSubimage(0, splitHeights.get(i), source.getWidth(), splitHeights.get(i+1) - splitHeights.get(i)), outputPNG(), f);
            if (doWaifu(f)) {
                f.delete();
            }
        }

        // Finished message
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText(null);
        int numImages = splitHeights.size() - 1;
        if (numImages == 1) {
            a.setContentText("Unable to smart split!");
        } else {
            a.setContentText(splitHeights.size() - 1 + " images were successfully created!");
        }
        a.showAndWait();
    }

    // Run waifu given a file
    public boolean doWaifu(File f) {
        // No options chosen
        String sf = scaleField.getText();
        String shf = scaleHeightField.getText();
        String swf = scaleWidthField.getText();

        if ((!denoiseBox.isSelected() || sf.equalsIgnoreCase("1")) && (sf.isEmpty() && shf.isEmpty() && swf.isEmpty())) {
            return false;
        }

        if (shf.isEmpty() && swf.isEmpty() && !sf.isEmpty()) {
            if (!isNumber(sf)) {
                scaleField.setText("1");
                scaleWidthField.clear();
                scaleHeightField.clear();
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("Please enter only numbers for the scale height and width!");
                a.showAndWait();
                return false;
            }
        } else if (shf.isEmpty() && !swf.isEmpty() && !sf.isEmpty()) {
            if (!isNumber(shf) || !isNumber(swf)) {
                scaleField.setText("1");
                scaleWidthField.clear();
                scaleHeightField.clear();
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("Please enter only numbers for the scale ratio!");
                a.showAndWait();
                return false;
            }
        }

        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(waifuField.getText());
        cmd.add("-i");
        cmd.add("\"" + f.getAbsolutePath() + "\"");
        cmd.add("-m");

        // NOISE AND SCALE
        if (denoiseBox.isSelected() && scaleBox.isSelected()) {
            cmd.add("noise_scale");
            if (!shf.isEmpty() && !swf.isEmpty()) {
                cmd.add("-h");
                cmd.add(shf);
                cmd.add("-w");
                cmd.add(swf);
            } else if (!sf.isEmpty()) {
                cmd.add("-s");
                cmd.add(sf);
            }
            cmd.add("noise");
            cmd.add("-n");
            cmd.add(((int) denoiseSlider.getValue()) + "");
        }
        // ONLY NOISE
        else if (denoiseBox.isSelected() && !scaleBox.isSelected()) {
            cmd.add("noise");
            cmd.add("-n");
            cmd.add(((int) denoiseSlider.getValue()) + "");
        }
        else if (scaleBox.isSelected() && !denoiseBox.isSelected()) {
            cmd.add("scale");
            if (!shf.isEmpty() && !swf.isEmpty()) {
                cmd.add("-h");
                cmd.add(shf);
                cmd.add("-w");
                cmd.add(swf);
            } else {
                cmd.add("-s");
                cmd.add(sf);
            }
        }

        if (nameField.getText().isEmpty()) {
            nameField.setText(generateString(10));
        }

        cmd.add("-o");
        cmd.add("\"" + outputField.getText() + f.getName().replaceFirst("[.][^.]+$", "") + "_waifu" + "." + fileOption + "\"");

        cmd.add("--model_dir");
        cmd.add(new File(waifuField.getText()).getParent() + File.separator + "models" + File.separator + modelOptions.getValue());

        try {
            Process p = new ProcessBuilder(cmd).start();
            p.waitFor();

        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Error using Waifu2X! Please create an issue on the github page!");
            a.showAndWait();
            return false;
        }
        return true;
    }

    // Gets the color of a pixel
    public Color getColor(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        return new Color(red, green, blue, alpha);
    }

    // Compares 2 color objects. True if same, false if not.
    public boolean ColorCompare(Color o1, Color o2) {
        return o1.getAlpha() == o2.getAlpha() && o1.getRed() == o2.getRed() && o1.getGreen() == o2.getGreen() && o1.getBlue() == o2.getBlue();
    }

    // Custom comparator class that makes sure files are in alpha-numerical order
    public static final class FileNameComparator implements Comparator<String> {
        public final Pattern NUMBERS =
                Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        @Override public int compare(String o1, String o2) {
            if (o1 == null || o2 == null)
                return o1 == null ? o2 == null ? 0 : -1 : 1;

            String[] split1 = NUMBERS.split(o1);
            String[] split2 = NUMBERS.split(o2);
            for (int i = 0; i < Math.min(split1.length, split2.length); i++) {
                char c1 = split1[i].charAt(0);
                char c2 = split2[i].charAt(0);
                int cmp = 0;

                if (c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
                    cmp = new BigInteger(split1[i]).compareTo(new BigInteger(split2[i]));
                }
                if (cmp == 0) {
                    cmp = split1[i].compareTo(split2[i]);
                }

                if (cmp != 0) {
                    return cmp;
                }
            }
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

        return image;
    }

    // Custom JPanel that creates a scrollable image
    public static class ScrollPanel extends JPanel {
        public ScrollPanel(BufferedImage image, int num) {
            JPanel canvas = new JPanel() {
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

    // Gets the value in the config
    public String getConfig(String key) {
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
    public void setConfig(String key, String value) {
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
    public String generateString(int n) {
        final String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder s = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < n; i++) {
            s.append(chars.charAt(r.nextInt(chars.length())));
        }
        return s.toString();

    }

    // Returns file type
    public String outputPNG() {
        if (fileOption.equalsIgnoreCase("PNG")) {
            return "PNG";
        }
        return "JPG";
    }

    // Checks if the String is a number
    public boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
