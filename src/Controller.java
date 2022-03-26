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
import javafx.util.StringConverter;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.*;

public class Controller implements Initializable {

    // FXML Variables
    @FXML private TextField inputField;
    @FXML private Button inputFolderBTN;
    @FXML private Button inputImagesBTN;
    @FXML private TextField outputField;
    @FXML private TextField nameField;
    @FXML private CheckBox batchBox;
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
    @FXML private Slider scaleSlider;
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

    private ObservableList<String> modelList;
    private String[] caffeModels;
    private String[] vulkanModels;
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
        inputPath = Util.getConfig("inputPath");
        outputPath = Util.getConfig("outputPath");
        waifuPath = Util.getConfig("waifuPath");
        imagePath = Util.getConfig("imagePath");
        watermarkPath = Util.getConfig("watermarkPath");
        lastAction = Util.getConfig("lastAction");
        actionOption = Util.getConfig("actionOption");
        ssOption = Util.getConfig("ssOption");
        fileOption = Util.getConfig("fileOption");
        files = new ArrayList<>();
    }

    // Called after constructor
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Temp
        batchBox.setDisable(true);

        // Final values
        final String[] files = new String[]{"PNG", "JPG"};
        caffeModels = new String[]{"cunet", "upresnet10", "upconv_7_anime_style_art_rgb", "upconv_7_photo", "anime_style_art_rgb", "photo", "anime_style_art_y"};
        vulkanModels = new String[]{"models-cunet", "models-upconv_7_anime_style_art_rgb", "models-upconv_7_photo"};
        final String[] actions = new String[]{"STITCHSPLIT", "STITCH", "SPLIT", "SMARTSPLIT"};
        options = new String[]{"Stitch Vertically, Smart Split", "Stitch Vertically, Split Horizontally",
                "Stitch Vertically, Split Vertically", "Stitch Horizontally, Smart Split",
                "Stitch Horizontally, Split Horizontally", "Stitch Horizontally, Split Vertically"};

        File f1 = new File(inputPath);
        File f2 = new File(outputPath);
        File f3 = new File(imagePath);
        File f4 = new File(watermarkPath);

        // Checks to make sure the config is not null or has any invalid values
        if (inputPath == null || !f1.isDirectory()) {
            inputPath = System.getProperty("user.home") + File.separator;
            Util.setConfig("inputPath", inputPath);
        }
        if (outputPath == null || !f2.isDirectory()) {
            outputPath = System.getProperty("user.home") + File.separator;
            Util.setConfig("outputPath", outputPath);
        }
        if (Util.getConfig("waifuPath") != null) {
            waifuField.setText(waifuPath);
        }
        if (imagePath == null || !f3.isDirectory()) {
            imagePath = System.getProperty("user.home") + File.separator;
            Util.setConfig("imagePath", imagePath);
        }
        if (watermarkPath == null || !f4.isDirectory()) {
            watermarkPath = System.getProperty("user.home") + File.separator;
            Util.setConfig("watermarkPath", watermarkPath);
        }
        if (lastAction == null || !Arrays.asList(actions).contains(lastAction)) {
            lastAction = "STITCHSPLIT";
            Util.setConfig("lastAction", lastAction);
        }
        if (actionOption == null) {
            actionOption = "VERTICAL";
            Util.setConfig("actionOption", actionOption);
        }
        if (ssOption == null || !Arrays.asList(options).contains(ssOption)) {
            ssOption = "Stitch Vertically, Smart Split";
            Util.setConfig("ssOption", ssOption);
        }
        if (fileOption == null || !Arrays.asList(files).contains(fileOption)) {
            fileOption = "PNG";
            Util.setConfig("fileOption", "PNG");
        }
        if (waifuPath == null) {
            waifuPath = "NOT FOUND";
            Util.setConfig("waifuPath", "NOT FOUND");
            waifuField.setText("");
        }

        // Default setups
        outputField.setText(outputPath);

        // Modify vanilla JavaFX slider to be in power of 2
        scaleSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double t) {
                if (t == 0) return "1";
                if (t == 1) return "2";
                if (t == 2) return "4";
                if (t == 3) return "8";
                if (t == 4) return "16";
                return "32";
            }

            @Override
            public Double fromString(String s) {
                switch (s) {
                    case "1":
                        return 0d;
                    case "2":
                        return 1d;
                    case "4":
                        return 2d;
                    case "8":
                        return 3d;
                    case "16":
                        return 4d;
                    default:
                        return 5d;
                }
            }
        });
        scaleSlider.setMin(0);
        scaleSlider.setMax(5);
        scaleSlider.setMajorTickUnit(1);
        scaleSlider.setSnapToTicks(true);
        scaleSlider.setShowTickLabels(true);
        scaleSlider.setShowTickMarks(true);

        // Adds the file types to the dropdown menu
        final ObservableList<String> fileList = FXCollections.observableArrayList(files);
        fileOptions.setItems(fileList);
        fileOptions.setValue(fileOption);

        // Adds all permutations of stitch and split to the dropdown menu
        final ObservableList<String> optionsList = FXCollections.observableArrayList(options);
        stitchSplitOptions.setItems(optionsList);
        stitchSplitOptions.setValue(ssOption);

        // Adds the waifu2x models to the dropdown menu if there is a waifuPath
        // If the waifu is CAFFE
        if (waifuPath.contains("caffe")) {
            modelList = FXCollections.observableArrayList(caffeModels);
            modelOptions.setItems(modelList);
            modelOptions.setValue("cunet");
        }
        // If the waifu is VULKAN
        else if (waifuPath.contains("vulkan")) {
            modelList = FXCollections.observableArrayList(vulkanModels);
            modelOptions.setItems(modelList);
            modelOptions.setValue("models-cunet");
        }
        // NO WAIFU
        else {
            waifuPath = "NOT FOUND";
        }

        // Sets the last action
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

        // Sets the last action option
        if (actionOption.equalsIgnoreCase("VERTICAL")) {
            vertical.isSelected();
        }
        else if (actionOption.equalsIgnoreCase("HORIZONTAL")) {
            horizontal.isSelected();
        }

        // Sets the last file option
        if (fileOption.equalsIgnoreCase("PNG")) {
            fileOptions.setValue("PNG");
        } else {
            fileOptions.setValue("JPG");
        }

        // Updates GUI
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

            if (stitchSplit.isSelected()) {
                stitchSplitOptions.setDisable(false);
            }
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

        onScale();
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
        Util.setConfig("inputPath", inputPath);

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
        Util.setConfig("inputPath", inputPath);


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
        Util.setConfig("outputPath", outputPath);
    }

    // Waifu button pressed
    public void onWaifu() {
        // Ask user to select
        FileChooser input = new FileChooser();
        input.setTitle("Select Folder of Images");
        input.setInitialDirectory(new File(inputPath));
        input.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Waifu2X EXE", "*.exe"));

        File f = input.showOpenDialog(title.getScene().getWindow());
        if (f == null) {
            return;
        }

        if (f.getName().contains("caffe")) {
            modelOptions.getItems().clear();
            modelList = FXCollections.observableArrayList(caffeModels);
            modelOptions.setItems(modelList);
            modelOptions.setValue("cunet");
        } else if (f.getName().contains("vulkan")) {
            modelOptions.getItems().clear();
            modelList = FXCollections.observableArrayList(vulkanModels);
            modelOptions.setItems(modelList);
            modelOptions.setValue("models-cunet");
        }

        waifuPath = f.getAbsolutePath() + File.separator;
        Util.setConfig("waifuPath", waifuPath);
        waifuField.setText(waifuPath);

        onScale();
    }

    // Running waifu2x standalone
    public void onRunWaifu() {
        // If the waifu2x.exe is not found
        if (waifuPath.equalsIgnoreCase("NOT FOUND")) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("Waifu2x was not found!");
            a.showAndWait();
            return;
        }

        int sf = (int) scaleSlider.getValue();
        String shf = scaleHeightField.getText();
        String swf = scaleWidthField.getText();

        // If no waifu2x options were chosen
        if ((!denoiseBox.isSelected() || sf == 1) && (sf == 1 && shf.isEmpty() && swf.isEmpty())) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText(null);
            a.setContentText("No Waifu2X options were chosen!");
            a.showAndWait();
        }
        waifuHelper(null);
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
            scaleSlider.setDisable(false);
            scaleHeightField.setDisable(false);
            scaleWidthField.setDisable(false);
            modelOptions.setDisable(false);

            // Width and height are only enabled for caffe
            if (waifuPath.contains("vulkan")) {
                scaleHeightField.setDisable(true);
                scaleWidthField.setDisable(true);
            } else if (waifuPath.contains("caffe")) {
                scaleHeightField.setDisable(false);
                scaleWidthField.setDisable(false);
            }
        } else {
            scaleSlider.setDisable(true);
            scaleHeightField.setDisable(true);
            scaleWidthField.setDisable(true);
        }
        if (!denoiseBox.isSelected() && !scaleBox.isSelected()) {
            modelOptions.setDisable(true);
        }
    }

    // Dropdown button pressed
    public void onDrop(MouseEvent e) {
        double height = mainPane.getScene().getWindow().getHeight();
        switch (((Button)e.getSource()).getId()) {
            case "dropBTN1":
                // Closing waifuPane
                if (waifuPane.isVisible()) {
                    waifuPane.setVisible(false);
                    ((Button) e.getSource()).setText("\uD83E\uDC1A");
                    mainPane.getRowConstraints().get(8).setPrefHeight(0);
                    mainPane.getScene().getWindow().setHeight(height - 200);
                }
                // Opening waifuPane
                else {
                    waifuPane.setVisible(true);
                    ((Button) e.getSource()).setText("\uD83E\uDC0B");
                    mainPane.getRowConstraints().get(8).setPrefHeight(200);
                    mainPane.getScene().getWindow().setHeight(height + 200);
                }
                break;
            case "dropBTN2":
                // Closing actionPane
                if (actionPane.isVisible()) {
                    actionPane.setVisible(false);
                    ((Button) e.getSource()).setText("\uD83E\uDC1A");
                    mainPane.getRowConstraints().get(10).setPrefHeight(0);
                    mainPane.getScene().getWindow().setHeight(height - 120);
                }
                // Opening actionPane
                else {
                    actionPane.setVisible(true);
                    ((Button) e.getSource()).setText("\uD83E\uDC0B");
                    mainPane.getRowConstraints().get(10).setPrefHeight(120);
                    mainPane.getScene().getWindow().setHeight(height + 120);
                }
                break;
            case "dropBTN3":
                // Closing watermarkPane
                if (watermarkPane.isVisible()) {
                    watermarkPane.setVisible(false);
                    ((Button) e.getSource()).setText("\uD83E\uDC1A");
                    mainPane.getRowConstraints().get(12).setPrefHeight(0);
                    mainPane.getScene().getWindow().setHeight(height - 70);
                }
                // Opening watermark pane
                else {
                    watermarkPane.setVisible(true);
                    ((Button) e.getSource()).setText("\uD83E\uDC0B");
                    mainPane.getRowConstraints().get(12).setPrefHeight(70);
                    mainPane.getScene().getWindow().setHeight(height + 70);
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
            nameField.setText(Util.generateString(10));
        }

        // Stitch / Split
        try {
            // Sort the files in alphanumerical order and load them
            BufferedImage b;
            ArrayList<File> newFiles = new ArrayList<>(files);
            newFiles.sort(Comparator.comparing(File::getName, new FileNameComparator()));
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
                Util.setConfig("ssOption", ssOption);
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
            Util.setConfig("lastAction", lastAction);

            if (vertical.isSelected()) {
                actionOption = "VERTICAL";
            } else {
                actionOption = "HORIZONTAL";
            }
            Util.setConfig("actionOption", actionOption);
            Util.setConfig("fileOption", fileOption);

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
        Util.setConfig("imagePath", imagePath);

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
        Util.setConfig("watermarkPath", watermarkPath);

        // Makes the watermark black and white
        if (greyOption.isSelected()) {
            ImageFilter imageFilter = new GrayFilter(true, 5);
            ImageProducer producer = new FilteredImageSource(watermark.getSource(), imageFilter);
            watermark = (BufferedImage) Toolkit.getDefaultToolkit().createImage(producer);
        }

        // Rescales the image
        double ratio = (double) watermark.getWidth() / watermark.getHeight();
        Image tempImage = watermark.getScaledInstance(image.getWidth() / 5,
                (int)(image.getWidth() / 5 * ratio), Image.SCALE_DEFAULT);

        watermark = (BufferedImage) tempImage;

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


    /////////////
    // Actions //
    /////////////

    // Stitch vertically/horizontally and Split horizontally/vertically/smart
    public void doStitchSplit(ArrayList<BufferedImage> images) throws IOException {
        String option = stitchSplitOptions.getValue();

        boolean vert = option.equalsIgnoreCase(options[0]) || option.equalsIgnoreCase(options[1]) || option.equalsIgnoreCase(options[2]);

        BufferedImage image = stitchHelper(images, vert);

        // Smart Split = 0
        // Split horizontally = 1
        // Split vertically = 2
        int splitType = -1;
        if (option.equalsIgnoreCase(options[0]) || option.equalsIgnoreCase(options[3])) {
            splitType = 0;
        }
        else if (option.equalsIgnoreCase(options[1]) || option.equalsIgnoreCase(options[4])) {
            splitType = 1;
        }
        else if (option.equalsIgnoreCase(options[2]) || option.equalsIgnoreCase(options[5])){
            splitType = 2;
        }

        // Split horizontally or vertically
        if (splitType == 1 || splitType == 2) {
            int num = Util.askSplitImages();
            splitHelper(image, num, splitType == 1);

            // Finished message
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setHeaderText(null);
            a.setContentText("Successfully split into " + num + " images!");
            a.showAndWait();
            return;

        }

        // Smart split
        if (splitType == 0) {
            smartSplitHelper(image);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText("Stitching and Splitting Complete!");
            if (denoiseBox.isSelected() && scaleBox.isSelected()) {
                a.setContentText("Images have been stitched and split and been denoised with level " + (int) denoiseSlider.getValue() + " and scaled by " + (int) Math.pow(2, scaleSlider.getValue()) + "!");
            } else if (denoiseBox.isSelected() && !scaleBox.isSelected()) {
                a.setContentText("Images have been stitched and split and denoised with level " + (int) denoiseSlider.getValue());
            } else if (!denoiseBox.isSelected() && scaleBox.isSelected()) {
                a.setContentText("Images have been stitched and split and been scaled by " + scaleSlider.getValue() + "!");
            }
            a.showAndWait();
        }
    }

    // Stitches an ArrayList of images vertically/horizontally
    public void doStitch(ArrayList<BufferedImage> images) throws IOException {
        boolean vert = vertical.isSelected();
        BufferedImage image = stitchHelper(images, vert);

        // Exports the image
        File f = new File(outputPath + nameField.getText() + "." + fileOption);
        ImageIO.write(image, fileOption, f);

        // Waifus the image if necessary
        if (waifuHelper(f)) {
            f.delete();
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText("Stitching Complete!");
        a.setContentText(files.size() + " images were successfully stitched!");
        a.showAndWait();
    }

    // Splits a BufferedImage vertically/horizontally
    public void doImageSplit(BufferedImage image) throws IOException {
        JFrame frame = new JFrame();
        frame.setTitle("Split Preview");
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icon.png")));

        boolean vert = vertical.isSelected();
        int num = Util.askSplitImages();

        // Preview image if it is small enough
        while (true) {
            try {
                Util.previewImage(frame, image, num);
            } catch (Exception ex) {
                break;
            }

            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setHeaderText(null);
            a.setContentText("Continue with these splits?");
            a.showAndWait();
            if (a.getResult().getText().equalsIgnoreCase("OK")) {
                break;
            }
        }
        frame.dispose();

        splitHelper(image, num, vert);

        // Finished message
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText("Splitting Complete!");
        a.setContentText("Successfully split into " + num + " images!");
        a.showAndWait();
    }

    // Splits a BufferedImage by space (row of same colored pixels)
    public void doSmartSplit(BufferedImage image) throws IOException {
        int numImages = smartSplitHelper(image);

        // Finished message
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setHeaderText("Smart Split");
        if (numImages == 1) {
            a.setContentText("Unable to smart split!");
        } else {
            a.setContentText(numImages + " images were successfully created!");
        }
        a.showAndWait();
    }



    /////////////
    // Helpers //
    /////////////

    // Given an arraylist of buffered images, returns the concatenated buffered image
    public BufferedImage stitchHelper(ArrayList<BufferedImage> images, boolean vert) {
        int concat = 0;
        int curr = 0;

        // Calculates the total size of the stitched image
        for (BufferedImage b : images) {
            // Vertical
            if (vert) {
                concat += b.getHeight();
            }
            // Horizontal
            else {
                concat += b.getWidth();
            }
            Graphics2D g2d = b.createGraphics();
            g2d.dispose();
        }

        BufferedImage concatImage;

        // Vertical
        if (vert) {
            concatImage = new BufferedImage(images.get(0).getWidth(), concat, BufferedImage.TYPE_INT_RGB);
        }
        // Horizontal
        else {
            concatImage = new BufferedImage(concat, images.get(0).getHeight(), BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g2d = concatImage.createGraphics();

        // Draws the images on the Graphics2D
        for (BufferedImage b : images) {
            // Vertical
            if (vertical.isSelected()) {
                g2d.drawImage(b, 0, curr, null);
                curr += b.getHeight();
            }
            // Horizontal
            else {
                g2d.drawImage(b, curr, 0, null);
                curr += b.getWidth();
            }
        }
        g2d.dispose();
        return concatImage;
    }

    // Splits the image and waifus as necessary
    public void splitHelper(BufferedImage image, int num, boolean hor) {
        int i = 1;
        new File(outputPath + File.separator + "StitchTool" + File.separator).mkdirs();

        try {
            // Horizontal
            if (hor) {
                int r = image.getHeight() % num;
                int parts = (image.getHeight() - r) / num;

                // Exports the images into a folder
                for (int y = 0; y < image.getHeight() - r; y += parts) {
                    File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i++ + "." + fileOption);
                    ImageIO.write(image.getSubimage(0, y, image.getWidth(), parts), fileOption, f);
                    if (waifuHelper(f)) {
                        f.delete();
                    }
                }

                // Exports the remainder if split unevenly
                if (r != 0) {
                    File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i + "." + fileOption);
                    ImageIO.write(image.getSubimage(0, image.getHeight() - (parts * num), image.getWidth(), r), fileOption, f);
                    if (waifuHelper(f)) {
                        f.delete();
                    }
                }
            }

            // Vertical
            else {
                int r = image.getWidth() % num;
                int parts = image.getWidth() / num;

                // Exports the images into a folder
                for (int x = 0; x < (image.getWidth() - r); x += parts) {
                    File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i++ + "." + fileOption);
                    ImageIO.write(image.getSubimage(x, 0, parts, image.getHeight()), fileOption, f);
                    if (waifuHelper(f)) {
                        f.delete();
                    }
                }

                // Exports the remainder if split unevenly
                if (r != 0) {
                    File f = new File(outputPath + File.separator + "StitchTool" + File.separator + nameField.getText() + i + "." + fileOption);
                    ImageIO.write(image.getSubimage(image.getWidth() - (parts * num), 0, image.getHeight(), r), fileOption, f);
                    if (waifuHelper(f)) {
                        f.delete();
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Smart split and waifus as necessary
    public int smartSplitHelper(BufferedImage image) {
        int c = 0;
        int num = Util.askSplitImages();

        // Approximate size of each image
        int startHeight = image.getHeight() / num;
        ArrayList<Integer> splitHeights = new ArrayList<>();
        splitHeights.add(0);

        for (int i = startHeight; i < image.getHeight(); i++ ) {
            for (int j = 0; j < image.getWidth() - 1; j++) {
                Color c1 = Util.getColor(image.getRGB(j, i));
                Color c2 = Util.getColor(image.getRGB(j + 1, i));

                // Checks if the row of pixels is the same color, if not, moves 1 pixel down
                if (!(c2.getAlpha()-10 <= c1.getAlpha() && c1.getAlpha() <= c2.getAlpha()+10 &&
                        c2.getRed()-10 <= c1.getRed() && c1.getRed() <= c2.getRed()+10 &&
                        c2.getGreen()-10 <= c1.getGreen() && c1.getGreen() <= c2.getGreen()+10 &&
                        c2.getBlue()-10 <= c1.getBlue() && c1.getBlue() <= c2.getBlue()+10)) {
                    break;
                }
                // Otherwise, it saves the height to split, and moves to the next part
                if (j+1 == image.getWidth() - 1) {
                    splitHeights.add(i);
                    i += startHeight;
                    c++;
                    break;
                }
            }
        }

        // Adds the remainder of the image to the array
        if (splitHeights.get(splitHeights.size() - 1) != image.getHeight()) {
            splitHeights.add(image.getHeight());
        }

        outputPath = outputPath + File.separator + "StitchTool" + File.separator;
        new File(outputPath).mkdirs();
        outputField.setText(outputPath);

        // Exports the images into a folder
        try {
            for (int i = 0; i < splitHeights.size() - 1; i++) {
                nameField.setText(nameField.getText() + "_" + (i + 1));
                File f = new File(outputPath + nameField.getText() + "." + fileOption);

                ImageIO.write(image.getSubimage(0, splitHeights.get(i), image.getWidth(), splitHeights.get(i + 1) - splitHeights.get(i)), fileOption, f);
                if (waifuHelper(f)) {
                    f.delete();
                }
                nameField.setText(nameField.getText().substring(0, nameField.getLength() - 2));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return c;
    }

    // Waifu2X the image
    public boolean waifuHelper(File f) {

        // If the waifu2x.exe is not found
        if (waifuPath.equalsIgnoreCase("NOT FOUND") || (!denoiseBox.isSelected() && !scaleBox.isSelected())) {
            return false;
        }

        int sf = (int) Math.pow(2, scaleSlider.getValue());
        String shf = scaleHeightField.getText();
        String swf = scaleWidthField.getText();

        // If shf and swf are not numbers and scale is 1
        if (!shf.isEmpty() && !swf.isEmpty() && sf == 1) {
            if (Util.isNumber(shf) || Util.isNumber(swf)) {
                scaleSlider.setValue(1);
                scaleWidthField.clear();
                scaleHeightField.clear();
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setHeaderText(null);
                a.setContentText("Please enter only numbers for the scale dimensions!");
                a.showAndWait();
                return false;
            }
        }

        // If f is null, ask user to select image
        if (f == null) {
            // Ask user to select image
            FileChooser input = new FileChooser();
            input.setTitle("Select image to Waifu2X");
            input.setInitialDirectory(new File(inputPath));
            input.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));

            // Add all images to list if user is stitching
            f = input.showOpenDialog(title.getScene().getWindow());
            if (f == null) {
                return false;
            }
        }

        // Generate name if empty
        if (nameField.getText().isEmpty()) {
            nameField.setText(Util.generateString(10));
        }

        // Create command argument
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(waifuField.getText().substring(0, waifuField.getLength()-1));
        cmd.add("-i");
        cmd.add("\"" + f.getAbsolutePath() + "\"");
        cmd.add("-o");
        cmd.add("\"" + outputField.getText() + nameField.getText() + "_waifu." + fileOption + "\"");

        if (waifuPath.contains("caffe")) {
            // NOISE AND SCALE
            cmd.add("-m");
            if (denoiseBox.isSelected() && scaleBox.isSelected()) {
                cmd.add("noise_scale");
                if (!shf.isEmpty() && !swf.isEmpty()) {
                    cmd.add("-h");
                    cmd.add(shf);
                    cmd.add("-w");
                    cmd.add(swf);
                } else if (sf != 1) {
                    cmd.add("-s");
                    cmd.add(sf + "");
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
            } else if (scaleBox.isSelected() && denoiseBox.isSelected()) {
                cmd.add("scale");
                if (!shf.isEmpty() && !swf.isEmpty()) {
                    cmd.add("-h");
                    cmd.add(shf);
                    cmd.add("-w");
                    cmd.add(swf);
                } else if (sf != 1) {
                    cmd.add("-s");
                    cmd.add(sf + "");
                }
            }
            cmd.add("--model_dir");
            cmd.add(new File(waifuField.getText()).getParent() + File.separator + "models" + File.separator + modelOptions.getValue());
        }
        else if (waifuPath.contains("vulkan")) {
            if (denoiseBox.isSelected()) {
                cmd.add("-n");
                cmd.add(((int) denoiseSlider.getValue()) + "");
                cmd.add("-s");
                cmd.add("1");
            }
            if (scaleBox.isSelected()) {
                cmd.add("-s");
                cmd.add(Math.pow(2, sf) + "");
            }
            cmd.add("-m");
            cmd.add(new File(waifuField.getText()).getParent() + File.separator + modelOptions.getValue());
        }

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

        if (!stitchSplit.isSelected() && !smartSplit.isSelected()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText(null);
            if (denoiseBox.isSelected() && scaleBox.isSelected()) {
                a.setContentText("Image has been denoised with level " + (int) denoiseSlider.getValue() + " and scaled by " + sf + "!");
            } else if (denoiseBox.isSelected() && !scaleBox.isSelected()) {
                a.setContentText("Image has been denoised with level " + (int) denoiseSlider.getValue());
            } else if (!denoiseBox.isSelected() && scaleBox.isSelected()) {
                a.setContentText("Image has been scaled by " + sf + "!");
            }
            a.showAndWait();
        }

        for (int i = 1; i < cmd.size(); i++) {
            System.out.print(cmd + " ");
        }
        System.out.println("END");


        return true;
    }
}
