package AutoPathGenerator;

import Resources.Vector2D;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class WindowController {

    /*---------- Path Specification Variables ----------*/

    //display variables
    private WindowDrawer display; // TODO: make the inner classes cleaner
    public Canvas fieldDisplay;
    public Canvas robotDisplay; // this does a fun and actually has a transparent canvas on top of another so I won't have to spend time redrawing the screen for the robot
    private GraphicsContext fieldGraphics;
    private GraphicsContext robotGraphics;
    private Image field;
    private Image robot;

    //Editing and definition variables
    public ChoiceBox<String> pathModeDropdown;
    private String pathMode;

    public Label pathText;
    public Label pointText;

    // design
    public CheckBox snapToGridCheckBox;
    private boolean snapToGrid;

    public Label pathColorSetText;
    public ColorPicker currentPathColorPicker;
    private Color currentPathColor;

    public Label compSnapText;
    public ChoiceBox<String> pathComponentSnapDropdown;
    private String pathComponentSnap;

    public CheckBox drawPointCheckBox;
    private boolean drawPoint;

    public Label pointColorSetText;
    public ColorPicker pointColorPicker;
    private Color currentPointColor;

    public CheckBox drawRobotCheckBox;
    public boolean drawRobot;

    // editing
    public Label editSelectCompLabel;
    public Label editSelectPointLabel;

    public enum pathTypes {

        goToPosition("Go To Position"),
        purePursuit("Pure Pursuit");

        private String displayName;

        pathTypes(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }

        public String toString() {
            return displayName;
        }
    }

    public Label pathTypeSetText;
    public ChoiceBox<pathTypes> pathTypeDropdown;
    private int currentlySelectedPath;

//    public enum pointTypes {
//
//        Regular("Regular"),
//        Fork_Start("Fork - Start"),
//        Fork_End("Fork - End");
//
//        private String displayName;
//
//        pointTypes(String displayName) {
//            this.displayName = displayName;
//        }
//
//        public String displayName() {
//            return displayName;
//        }
//
//        public String toString() {
//            return displayName;
//        }
//    }

    public Label forkedPathOperationsText;
    public Button modifyForkBtn;
    public Button deleteForkBtn;
    private int currentlySelectedPoint;

    private ForkDialog createForkDialog;

    public Label forkViewText;
    public TreeView<String> forkTreeView;
    private int selectedItemRowID;

    // transformations
    public Label pathTransformSetText;
    public ChoiceBox<String> pathTransformDropdown;
    private String currentTransform;

    public Label pivotTransXPosText;
    public Label pivotTransYPosText;
    public Label rotRefAngleText;
    public TextField pivotTranslateXField;
    public TextField pivotTranslateYField;
    public TextField rotateReflectAngleField;
    public Button transformPathBtn;
    private Vector2D transformVector;
    private double transformAngle;

    //general
    public Label generalTextLabel;
    public Button undoButton; //TODO: code these bad boys in as soon as highlighting path pieces and editing them is done
    public Button redoButton;
    public Button clearButton;

    /*---------- Auto Initialization Variables ----------*/
    //Checkboxes
    public CheckBox visionInitCheckBox;
    public CheckBox scnBeforeStartCheckBox;
    public CheckBox intakeDeployCheckBox;
    public CheckBox IMUInitCheckBox;
    public CheckBox skystonePosAbsGenCheckBox;
    public CheckBox skystonePosRelGenCheckBox;

    // drodowns
    public ChoiceBox<String> defStoneDropdown;
    public ChoiceBox<String> allianceDropdown;
    public ChoiceBox<String> initGrabDropdown;
    public ChoiceBox<String> initArmDropdown;

    // text/numerical fields
    public TextField autoNameField;
    public TextField specArmPosField;
    public TextField startDragPosField;
    public TextField initYField;
    public TextField initXField;

    /*---------- Internal Variables ----------*/
    private autoSpecListener specListener;
    private autoInitListener initListener;
    private static final double fieldLengthInch = 24.0 * 6.0;
    private static final double pixelsPerInch = (670.0 / fieldLengthInch); // 700x700 pixel image of a 12ft x 12ft field, 670 of the pixels actually represent the length of the field
    private static final Polygon fieldBounds = new Polygon(fieldLengthInch / -2.0, fieldLengthInch / -2.0, fieldLengthInch / -2.0, fieldLengthInch / 2.0, fieldLengthInch / 2.0, fieldLengthInch / 2.0, fieldLengthInch / 2.0, fieldLengthInch / -2.0);

    //Design
    private ArrayList<Vector2D> orderedPathVectors;
    private ArrayList<Polygon> pathBounds;
    private ArrayList<Color> pathColors;
    private ArrayList<Color> pointColors;

    private Vector2D initialPos;
    private Vector2D prevMouseClick;

    //Editing
    private HashMap<Integer, pathTypes> pathTypeAssociations;
    //private HashMap<Integer, pointTypes> pointTypeAssociations;
    private ArrayList<ArrayList<ArrayList<Vector2D>>> forkedPaths; // 1st arraylist is a list of forked path groups, 2nd arraylist is a set of possible paths in between a fork start and fork end, the third describes an actual path, one of the possible within the forked path group

    public void initialize() {

        //init internal variables
        display = new WindowDrawer();
        specListener = new autoSpecListener();
        initListener = new autoInitListener();

        // init graphics
        initTabs();
    }

    private void initTabs() {

        initPathSpec();
        initAutoInit();
        initAutoMisc();
        initAutoGen();
    }

    private void initPathSpec() {

        // Graphics init
        try {
            FileInputStream f = new FileInputStream("../AutoGenerator/src/Resources/Images/topviewfield.png");
            field = new Image(f);
            f = new FileInputStream("../AutoGenerator/src/Resources/Images/RobotAssemblyNoBkg.png");
            robot = new Image(f, 17.65748 * pixelsPerInch, 22.537433 * pixelsPerInch, true, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        fieldGraphics = fieldDisplay.getGraphicsContext2D();
        resetField();
        robotGraphics = robotDisplay.getGraphicsContext2D(); //TODO: get one image from solidworks that is also top down but with the intake and draggers retracted and switc between then as deployIntake() is called
        resetRobotDisplay();

        // Data init
        orderedPathVectors = new ArrayList<Vector2D>();
        pathBounds = new ArrayList<Polygon>();
        pathColors = new ArrayList<Color>();
        pointColors = new ArrayList<Color>();

        pathTypeAssociations = new HashMap<Integer, pathTypes>();
        //pointTypeAssociations = new HashMap<Integer, pointTypes>();
        forkedPaths = new ArrayList<ArrayList<ArrayList<Vector2D>>>();

        initialPos = new Vector2D(63, -36);
        //pointTypeAssociations.put(0, pointTypes.Regular); // this is for the initial point

        // Graphics init pt.2, need to draw the initial point
        pointColors.add(currentPointColor);
        redrawPoints();

        //Path editing and definition initialization
        initPathDef();

        //Point editing and definition initialization
        initPointDef();

        //testers
        //vectorDrawTester();

        specListener.pathModeChanged();
    }

    private void initPathDef() {

        pathModeDropdown.getItems().clear();
        pathModeDropdown.getItems().add("Design");
        pathModeDropdown.getItems().add("Edit Components");
        pathModeDropdown.getItems().add("Rigid Transform");
        pathModeDropdown.getSelectionModel().select(0);
        pathModeDropdown.setOnAction(event -> specListener.pathModeChanged());
        pathMode = pathModeDropdown.getItems().get(0);

        //path design
        snapToGridCheckBox.setSelected(false);
        snapToGridCheckBox.setOnAction(event -> specListener.snapToGridChanged());
        snapToGrid = snapToGridCheckBox.isSelected();

        currentPathColorPicker.setValue(Color.LAWNGREEN);
        currentPathColorPicker.setOnAction(event -> specListener.pathColorChanged());
        currentPathColor = currentPathColorPicker.getValue();

        pathComponentSnapDropdown.getItems().clear();
        pathComponentSnapDropdown.getItems().add("None");
        pathComponentSnapDropdown.getItems().add("Vertical");
        pathComponentSnapDropdown.getItems().add("Horizontal");
        pathComponentSnapDropdown.getSelectionModel().select(0);
        pathComponentSnapDropdown.setOnAction(event -> specListener.pathComponentSnapChanged());

        //path editing
        pathTypeDropdown.getItems().clear();
        pathTypeDropdown.getItems().addAll(pathTypes.values());
        pathTypeDropdown.getSelectionModel().select(0);
        pathTypeDropdown.setOnAction(event -> specListener.pathTypeChanged());
        pathTypeDropdown.setDisable(true);
        
        currentlySelectedPath = -1;

        //path transforms
        pathTransformDropdown.getItems().clear();
        pathTransformDropdown.getItems().add("Translate");
        pathTransformDropdown.getItems().add("Rotate");
        pathTransformDropdown.getItems().add("Reflect");
        pathTransformDropdown.getSelectionModel().select(0);
        pathTransformDropdown.setOnAction(event -> specListener.transformChanged());
        currentTransform = pathTransformDropdown.getItems().get(0);

        pivotTranslateXField.textProperty().addListener((o, ov, nv) -> specListener.pivotPosChanged());
        pivotTranslateYField.textProperty().addListener((o, ov, nv) -> specListener.pivotPosChanged());
        rotateReflectAngleField.textProperty().addListener((o, ov, nv) -> specListener.rotationAngleChanged());
        transformVector = new Vector2D(0, 0);
        transformAngle = 0;

        transformPathBtn.setOnAction(event -> specListener.transform());

        // general
        clearButton.setOnAction(event -> specListener.clearPath());
        undoButton.setOnAction(event -> specListener.undo());
        redoButton.setOnAction(event -> specListener.redo());
    }

    private void initPointDef() {

        //point design
        drawPointCheckBox.setSelected(false);
        drawPointCheckBox.setOnAction(event -> specListener.drawPointChanged());
        drawPoint = drawPointCheckBox.isSelected();

        pointColorPicker.setValue(Color.RED);
        pointColorPicker.setOnAction(event -> specListener.pointColorChanged());
        currentPointColor = pointColorPicker.getValue();

        drawRobotCheckBox.setSelected(false);
        drawRobotCheckBox.setOnAction(event -> specListener.drawRobotChanged());
        drawRobot = drawPointCheckBox.isSelected();

        //point editing
//        pointTypeDropdown.getItems().clear();
//        pointTypeDropdown.getItems().addAll(pointTypes.values()); // TODO: add another textfield that either appears or becomes enabled when this is selected to specify what variable it should depend on? Work it out dude
//        pointTypeDropdown.getSelectionModel().select(0);
//        pointTypeDropdown.setOnAction(event -> specListener.pointTypeChanged());
//        pointTypeDropdown.setDisable(true);
        modifyForkBtn.setOnAction(event -> specListener.modifyForks());
        deleteForkBtn.setOnAction(event -> specListener.deleteFork());
        
        currentlySelectedPoint = -1;

        forkTreeView.setEditable(false);
        forkTreeView.setShowRoot(false);

        TreeItem<String> root = new TreeItem<String>(); //WindowController.treeViewTester(1);
        forkTreeView.setRoot(root);
        forkTreeView.getSelectionModel().selectedItemProperty().addListener(selection -> specListener.branchSelected());
        selectedItemRowID = -1;
    }

    private void initAutoInit() {

        // dropdown initialization
        defStoneDropdown.getItems().clear();
        defStoneDropdown.getItems().add("Left");
        defStoneDropdown.getItems().add("Middle");
        defStoneDropdown.getItems().add("Right");
        defStoneDropdown.getSelectionModel().select(1);
        defStoneDropdown.setOnAction(event -> specListener.defStoneDropdownChanged());

        allianceDropdown.getItems().clear();
        allianceDropdown.getItems().add("Red");
        allianceDropdown.getItems().add("Blue");
        allianceDropdown.getSelectionModel().select(0);
        allianceDropdown.setOnAction(event -> initListener.allianceDropdownChanged());

        initGrabDropdown.getItems().clear();
        initGrabDropdown.getItems().add("Grab");
        initGrabDropdown.getItems().add("Release");
        initGrabDropdown.getSelectionModel().select(1);
        initGrabDropdown.setOnAction(event -> initListener.grabDropdownChanged());

        initArmDropdown.getItems().clear();
        initArmDropdown.getItems().add("In");
        initArmDropdown.getItems().add("Standby");
        initArmDropdown.getItems().add("Intermediate 2");
        initArmDropdown.getItems().add("Drop");
        initArmDropdown.getItems().add("Specific");
        initArmDropdown.getSelectionModel().select(1);
        initArmDropdown.setOnAction(event -> initListener.armDropdownChanged());

        // field initialization
        autoNameField.textProperty().addListener((o, ov, nv) -> initListener.autoNameChanged());
        specArmPosField.textProperty().addListener((o, ov, nv) -> initListener.specArmPosChanged());
        startDragPosField.textProperty().addListener((o, ov, nv) -> initListener.startDragPosChanged());
        initXField.textProperty().addListener((o, ov, nv) -> {if (initXField.focusedProperty().getValue()) {
            initListener.initPosChanged();
        }});
        initYField.textProperty().addListener((o, ov, nv) -> {if (initYField.focusedProperty().getValue()) {
            initListener.initPosChanged();
        }});
//        autoNameField.setOnAction(event -> autoNameChanged());
//        specArmPosField.setOnAction(event -> specArmPosChanged());
//        startDragPosField.setOnAction(event -> startDragPosChanged());
//        initXField.setOnAction(event -> initPosChanged());
//        initYField.setOnAction(event -> initPosChanged());

        specArmPosField.setText("0.22"); // this is the left servos standby position
        specArmPosField.setDisable(true);

        initXField.setText("63");
        initYField.setText("-36");
    }

    private void initAutoMisc() {


    }

    private void initAutoGen() {


    }

    /*---------- Info Listeners ----------*/

    class autoSpecListener {

        private void pathModeChanged() {

            int selectIndex = pathModeDropdown.getSelectionModel().getSelectedIndex();

            if (selectIndex != -1) {

                pathMode = pathModeDropdown.getItems().get(selectIndex);
                //System.out.println("new PathMode: " + pathMode);

                display.setPathDesignVisible(false);
                display.setPathEditVisible(false);
                display.setPathRTVisible(false, currentTransform);

                switch (pathMode) {
                    case "Design":

                        display.setPathDesignVisible(true);
                        break;
                    case "Edit Components":

                        editSelectCompLabel.setText("Selected Component: NONE");
                        display.setPathEditVisible(true);
                        break;
                    case "Rigid Transform":

                        prevMouseClick = null;
                        editSelectCompLabel.setText("Path Selected: FALSE");
                        display.setPathRTVisible(true, currentTransform);
                        break;
                }
            }
        }

        private void snapToGridChanged() {

            snapToGrid = snapToGridCheckBox.isSelected();

            resetPathSpec();
        }

        private void pathColorChanged() {

            currentPathColor = currentPathColorPicker.getValue();
        }

        private void drawPointChanged() {

            drawPoint = drawPointCheckBox.isSelected();

            resetPathSpec();
        }

        private void pointColorChanged() {

            currentPointColor = pointColorPicker.getValue();
        }

        private void drawRobotChanged() {

            drawRobot = drawRobotCheckBox.isSelected();

            resetRobotDisplay();

            if (drawRobot) {

                display.drawRobotInch(getCurrentPathPos());
            }
        }

        private void pathComponentSnapChanged() {

            int selectIndex = pathComponentSnapDropdown.getSelectionModel().getSelectedIndex();

            if (selectIndex != -1) {

                pathComponentSnap = pathComponentSnapDropdown.getItems().get(selectIndex);
            }
        }

        private void pathTypeChanged() {

            int newlySelected = pathTypeDropdown.getSelectionModel().getSelectedIndex();

            if (newlySelected != -1 && currentlySelectedPath != -1) {

                pathTypes pathType = pathTypeDropdown.getItems().get(pathTypeDropdown.getSelectionModel().getSelectedIndex());
                pathTypeAssociations.put(currentlySelectedPath, pathType);
            }
        }

//        private void pointTypeChanged() {
//
//            int newlySelected = pointTypeDropdown.getSelectionModel().getSelectedIndex();
//
//            if (newlySelected != -1 && currentlySelectedPoint != -1) {
//
//                pointTypes pointType = pointTypeDropdown.getItems().get(pointTypeDropdown.getSelectionModel().getSelectedIndex());
//                pointTypeAssociations.put(currentlySelectedPoint, pointType);
//
//                display.setPathEditVisible(true);
//            }
//        }

        private void modifyForks() {

            if (selectedItemRowID != -1) { // modify if a fork is selected

                try {

                    openForkCreateDialog(ForkDialog.dialogType.modifyFork);
                } catch (IOException e) {

                    e.printStackTrace();
                }
            } else { // create if no fork is selected

                try {

                    openForkCreateDialog(ForkDialog.dialogType.createFork);
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }

            display.setPathEditVisible(true);
        }

        private void deleteFork() {

            if (selectedItemRowID != -1) {

                try {

                    openForkCreateDialog(ForkDialog.dialogType.deleteFork);
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        }

        private void branchSelected() {

            int newlySelected = forkTreeView.getSelectionModel().selectedIndexProperty().get();

            if (newlySelected != -1) {

                int level = forkTreeView.getTreeItemLevel(forkTreeView.getTreeItem(newlySelected)) - 1;
                TreeItem<String> selectedItem = (TreeItem<String>) forkTreeView.getSelectionModel().getSelectedItem();

                int rowID = getRowID(selectedItem, level);
                selectedItemRowID = rowID;
                modifyForkBtn.setText("Modify Fork");

                System.out.println("Selected Row: " + rowID);
            } else {

                selectedItemRowID = -1;
                modifyForkBtn.setText("Create Fork");
            }
        }

        private void transformChanged() {

            int newlySelected = pathTransformDropdown.getSelectionModel().getSelectedIndex();

            if (newlySelected != -1) {

                currentTransform = pathTransformDropdown.getItems().get(newlySelected);
                display.setPathRTVisible(pathMode.equals("Rigid Transform"), currentTransform);
            }
        }

        private void transform() {

            switch (currentTransform) {
                case "Translate":

                    prevMouseClick = new Vector2D(initialPos);
                    translate(Vector2D.add(transformVector, initialPos));
                    prevMouseClick = null;
                    break;
                case "Rotate":

                    rotatePath();
                    break;
                case "Reflect":

                    reflectPath();
                    break;
            }
        }

        private void defStoneDropdownChanged() {

            int selectIndex = defStoneDropdown.getSelectionModel().getSelectedIndex();

            if (selectIndex != -1) {

                String newlySelected = defStoneDropdown.getItems().get(selectIndex);
                //System.out.println("new Default Stone Dropdown value: " + newlySelected);
            }
        }

        private void pivotPosChanged() {

            double x = transformVector.getComponent(0);
            double y = transformVector.getComponent(1);

            try {

                x = Double.parseDouble(pivotTranslateXField.getText());
                y = Double.parseDouble(pivotTranslateYField.getText());
            } catch (NumberFormatException e) {

                x = transformVector.getComponent(0);
                y = transformVector.getComponent(1);
            }

            transformVector = new Vector2D(x, y);
        }

        private void rotationAngleChanged() {

            double angle = transformAngle;

            try {

                angle = Double.parseDouble(rotateReflectAngleField.getText());
            } catch (NumberFormatException e) {

                angle = transformAngle;
            }

            transformAngle = angle;

            if (currentTransform.equals("Reflect")) {

                resetField();
                display.drawReflectionLine();
                redrawPath(pathColors.toArray(new Color[0]));

                if (drawPoint) {

                    redrawPoints(pointColors.toArray(new Color[0]));
                }

                if (snapToGrid) {

                    display.drawGrid(fieldDisplay, 12, 12, Color.BLACK, new double[] {15, 15, 15, 15});
                }
            }
        }

        private void clearPath() {

            orderedPathVectors.clear();
            pathBounds.clear();
            pathColors.clear();
            pointColors.clear();
            pointColors.add(currentPointColor);

            resetField();
            display.drawPointField(fieldDisplay, initialPos, pointColors.get(0), 1.5);

            if (pathMode.equals("Rigid Transform") && currentTransform.equals("Reflect")) {

                display.drawReflectionLine();
            }

            if (snapToGrid) {

                display.drawGrid(fieldDisplay, 12, 12, Color.BLACK, new double[] {15, 15, 15, 15});
            }

            resetRobotDisplay();
        }

        private void undo() { // how should I make the undo stack generalized? make them strings for specific actions encoded in a method?

        }

        private void redo() {

        }
    }

    class autoInitListener {

        private void allianceDropdownChanged() {

            int selectIndex = allianceDropdown.getSelectionModel().getSelectedIndex();

            if (selectIndex != -1) {

                String newlySelected = allianceDropdown.getItems().get(selectIndex);
                //System.out.println("new Alliance Dropdown value: " + newlySelected);

                if (newlySelected.equals("Red")) {

                    if (initialPos.equals(new Vector2D(-63, -36))) {

                        initialPos = new Vector2D(63, -36);
                        updateInitPosFields();
                    }
                } else if (newlySelected.equals("Blue")) {

                    if (initialPos.equals(new Vector2D(63, -36))) {

                        initialPos = new Vector2D(-63, -36);
                        updateInitPosFields();
                    }
                }
            }
        }

        private void grabDropdownChanged() {

            int selectIndex = initGrabDropdown.getSelectionModel().getSelectedIndex();

            if (selectIndex != -1) {

                String newlySelected = initGrabDropdown.getItems().get(selectIndex);
                //System.out.println("new Grab Dropdown value: " + newlySelected);
            }
        }

        private void armDropdownChanged() {

            int selectIndex = initArmDropdown.getSelectionModel().getSelectedIndex();

            if (selectIndex != -1) {

                String newlySelected = initArmDropdown.getItems().get(selectIndex);
                //System.out.println("new Arm Dropdown value: " + newlySelected);

                if (newlySelected.equals("Specific")) {

                    specArmPosField.setDisable(false);
                } else {

                    specArmPosField.setDisable(true);
                }
            }
        }

        private void autoNameChanged() {

        }

        private void specArmPosChanged() {

        }

        private void startDragPosChanged() {

        }

        private void initPosChanged() {

            boolean valid = false;
            double x;
            double y;

            try {

                x = Double.parseDouble(initXField.getText());
                y = Double.parseDouble(initYField.getText());
                valid = true;
            } catch (NumberFormatException e) {

                x = initialPos.getComponent(0);
                y = initialPos.getComponent(1);
            }

            if (valid && (orderedPathVectors.size() > 0) && (x != initialPos.getComponent(0) && y !=initialPos.getComponent(1))) {

                Vector2D newFirst = Vector2D.add(initialPos, orderedPathVectors.get(0));
                newFirst = Vector2D.sub(newFirst, new Vector2D(x, y));
                orderedPathVectors.set(0, newFirst);
            }

            initialPos = new Vector2D(x, y);

            if (valid) {

                resetPathSpec();
            }
        }

        private void updateInitPosFields() {

            initXField.setText(initialPos.getComponent(0).toString());
            initYField.setText(initialPos.getComponent(1).toString());
        }
    }

    /*---------- JavaFX Convenience Methods ----------*/

    /**
     * The basic .setVisible(boolean) method implemented in all JavaFX Nodes doesn't seem to work properly, this middleman method encapsulates a command to bind the property and carry out the setVisible() command
     * @param show      Whether to show the specified Node
     * @param component The Node to be hidden or shown
     */
    public void setVisible(boolean show, Node component) {

        component.setVisible(show);
        component.managedProperty().bind(component.visibleProperty());
    }

    public ArrayList<TreeItem> obsToArrayList(ObservableList<TreeItem> obsList) {

        return new ArrayList<TreeItem>(obsList);
    }

    /**
     * Returns the row space an item takes up in it's current state
     * @param branch    The item to be analyzed
     * @return          The number of rows the item currently takes up
     */
    public int getRowSpaceExpanded(TreeItem branch) {

        int rowSpace = 1;

        if (branch.isExpanded()) {

            for (int t = 0; t < branch.getChildren().size(); t++) {

                TreeItem item = (TreeItem) branch.getChildren().get(t);
                rowSpace += getRowSpaceExpanded(item);
            }
        }

        return rowSpace;
    }

    /**
     * Returns the absolute row space an item would take up in it's completely expanded state
     * @param branch    The item to be analyzed
     * @return          The number of rows it would take up being completely expanded
     */
    public int getRowSpace(TreeItem branch) {

        int rowSpace = 1;

        for (int t = 0; t < branch.getChildren().size(); t++) {

            TreeItem item = (TreeItem) branch.getChildren().get(t);
            rowSpace += getRowSpace(item);
        }

        return rowSpace;
    }

    /**
     * Returns the nth parent of some item
     * @param branch    The item the parent is related to
     * @param level     The negative level of the parent relative to the reference branch ex.(1) is 1 level closer to the root
     * @return          The parent item
     */
    public TreeItem getParent(TreeItem branch, int level) {

        TreeItem parent = null;

        if (branch != null) {
            if (level > 1) {

                parent = getParent(branch.getParent(), level - 1);
            } else if (level == 1) {

                parent = branch.getParent();
            } else if (level == 0) {

                parent = branch;
            }
        }

        return parent;
    }

    /**
     * Returns all the children of a given level relative to the root
     * @param root  The tree structure to be analyzed
     * @param level The level of the items returned
     * @return      An arraylist of items all of a given level relative to the root
     */
    public ArrayList<TreeItem> getChildren(TreeItem root, int level) {

        ArrayList<TreeItem> children = new ArrayList<TreeItem>();

        if (level > 0) {

            for (int c = 0; c < root.getChildren().size(); c++) {

                children.addAll(getChildren((TreeItem) root.getChildren().get(c), level - 1));
            }
        } else if (level == 0) {

            children.addAll(obsToArrayList(root.getChildren()));
        }

        return children;
    }

    /**
     * Given a tree structure and a position given as the index of the nodes at each tree level, this method returns the item at that location
     * @param branch    The tree structure to navigate
     * @param position  The position of the item as the index in the children lists of each previous node: Ex. <0, 1, 1> gives the 2nd child of the 2nd child of the 1st item, <1, 0, 2> gives the 3rd child of the 1st child of the 2nd item
     * @return          The Tree Item at the specified position, null if the position does not exist inside the structure or if the position does not contain actual data
     */
    public TreeItem getChild(TreeItem branch, ArrayList<Integer> position) {

        position = new ArrayList<Integer>(position);

        if (position.size() > 0 && branch.getChildren().size() > position.get(0)) {

            TreeItem newBranch = (TreeItem) branch.getChildren().get(position.get(0));

            if (position.size() > 1) {

                position.remove(0);
                return getChild(newBranch, position);
            }

            return newBranch;
        }

        return null;
    }

    /**
     * Regardless of the full structure, given the level of the element in the appropriate sub-structure reference frame, as well as the item and it's relations, this method returns the absolute row ID of the element, or rather, the row number in the fully expanded state of the relative structure
     * @param element   The Tree Item for which the row ID is needed
     * @param level     The relative tree depth of the item
     * @return          The absolute row number, or row ID of the item
     */
    public int getRowID(TreeItem element, int level) {

        int rowSpaceBefore = 0;

        for (int l = 0; l <= level; l++) {

            TreeItem parent = getParent(element, l);
            TreeItem sibling = parent.previousSibling();

            while (sibling != null) {

                rowSpaceBefore += getRowSpace(sibling);
                sibling = sibling.previousSibling();
            }
        }

        return (rowSpaceBefore + level);
    }

    /**
     * Automatically analyzes the tree structure and determines the highest level
     * @param root  The tree structure to analyze
     * @return      The maximum depth of the tree
     */
    public int getTreeHeight(TreeItem root) {

        int lastLevel = 0;
        ArrayList<TreeItem> items = getChildren(root, lastLevel);

        while (items.size() != 0) {

            lastLevel++;
            items = getChildren(root, lastLevel);
        }

        return lastLevel;
    }

    /**
     * Given a tree structure and a rowID, the method will return the item that corresponds to the row number passed in when the structure is in a completely expanded state
     * @param root              The tree structure to navigate
     * @param absoluteRowIndex  The expanded row number
     * @return                  Returns a TreeItem, null if root is null, absoluteRowIndex is less than 0, or if the number of items in the structure is less than the row number
     */
    public TreeItem getItem(TreeItem root, int absoluteRowIndex) {

        TreeItem item = null;

        if (root != null && absoluteRowIndex >= 0) {

            // Algorithm #1: Ridiculously hard to manage data
//            final int baseLength = getChildren(root, 0).size();
//
//            ArrayList<Integer> treePosition = new ArrayList<Integer>();
//            treePosition.add(0);
//
//            int rowSpaceBefore = 0;
//            int currentLevel = 0;
//
//            while (absoluteRowIndex != rowSpaceBefore && treePosition.get(0) < baseLength) {
//
//                TreeItem currentItem = getChild(root, treePosition);
//                int addedRowSpace = getRowSpace(currentItem);
//
//                if (addedRowSpace > absoluteRowIndex) {
//
//                    int childrenCount = currentItem.getChildren().size();
//
//                    if (childrenCount > 0) {
//
//                        currentLevel++;
//                        treePosition.add(currentLevel, 0);
//                    } else {
//
//                        treePosition.remove(currentLevel);
//                        currentLevel--;
//                        treePosition.set(currentLevel, treePosition.get(currentLevel) + 1);
//                    }
//                } else if (addedRowSpace < absoluteRowIndex) {
//
//                    treePosition.set(currentLevel, treePosition.get(currentLevel) + 1);
//                }
//
//                while (treePosition.get(currentLevel) >= currentItem.getParent().getChildren().size()) {
//
//
//                    treePosition.remove(currentLevel);
//                    currentLevel--;
//                    treePosition.set(currentLevel, treePosition.get(currentLevel) + 1);
//                }
//            }

            // Algorithm #2, is this inefficient? My original algorithm is probably faster but far more complex
            int height = getTreeHeight(root);

            for (int currentLevel = 0; currentLevel <= height && item == null; currentLevel++) {

                ArrayList<TreeItem> items = getChildren(root, currentLevel);

                for (int t = 0; t < items.size() && item == null; t++) {

                    TreeItem currentItem = items.get(t);

                    if (getRowID(currentItem, currentLevel) == absoluteRowIndex) {

                        item = currentItem;
                    }
                }
            }
        }

        return item;
    }

    void openForkCreateDialog(ForkDialog.dialogType type) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ForkDialog.fxml"));
        Parent parent = fxmlLoader.load();
        ForkDialog dialogController = fxmlLoader.<ForkDialog>getController();
        dialogController.initialize(type);

        Scene scene = new Scene(parent, 300, 200);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        Main.fixBounds(stage, parent);
    }

    /*---------- Data Manipulation/Internal Processing ----------*/

    /**
     * Given a vector in the field's coordinate system, this method computes what the point would be in the canvas' coordinate system
     * @param fieldPos Some point in the field's coordinate system
     * @return  The same point in the canvas' coordinate system
     */
    public Vector2D convertFieldToCanvas(Vector2D fieldPos) {

        fieldPos = new Vector2D(fieldPos);
        fieldPos.scale(pixelsPerInch);
        fieldPos.flipDimension(1);

        double xOffset = fieldDisplay.getWidth() / 2.0;
        double yOffset = fieldDisplay.getHeight() / 2.0;
        Vector2D centerOffset = new Vector2D(xOffset, yOffset);
        fieldPos.add(centerOffset);

        return fieldPos;
    }

    /**
     * Given a vector in the canvas' coordinate system, this method computes what the point would be in the field's coordinate system
     * @param canvasPos Some point in the canvas' coordinate system
     * @return  The same point in the field's coordinate system
     */
    public Vector2D convertCanvasToField(Vector2D canvasPos) {

        canvasPos = new Vector2D(canvasPos);

        double xOffset = fieldDisplay.getWidth() / 2.0;
        double yOffset = fieldDisplay.getHeight() / 2.0;
        Vector2D centerOffset = new Vector2D(xOffset, yOffset);
        canvasPos.sub(centerOffset);
        canvasPos.flipDimension(1);
        canvasPos.scale(1 / pixelsPerInch);

        return canvasPos;
    }

    /**
     * Takes a hypothetical path vector and uses the field display's dimensions to compute the path component that would move the path to the nearest grid intersection relative to where the uncorrected path component would land
     * @param termPos   The movement vector before correction
     * @return  The corrected path vector
     */
    public Vector2D snapToGridField(Vector2D termPos) {

        double pixelsPerCol = (670.0 / 12.0);
        double pixelsPerRow = (670.0 / 12.0);

        termPos = convertFieldToCanvas(termPos);
        termPos.sub(new Vector2D(15, 15));

        termPos.setComponent(0, Math.round(termPos.getComponent(0) / pixelsPerCol) * pixelsPerCol);
        termPos.setComponent(1, Math.round(termPos.getComponent(1) / pixelsPerRow) * pixelsPerRow);

        termPos.add(new Vector2D(15, 15));
        termPos = convertCanvasToField(termPos);

        return termPos;
    }

    /**
     * takes an angle and normalizes it to be between 0 and 360 degrees
     * @param degrees   The angle to normalize in degrees
     * @return  The normalized angle
     */
    public static double normalizeAngle(double degrees) {

        while (degrees < 0) {

            degrees += 360;
        }

        while (degrees > 360) {

            degrees -= 360;
        }

        return degrees;
    }

    /**
     * Calculates the current field position given the predefined path
     * @return  The last point in the currently defined path in the field's coordinate system
     */
    public Vector2D getCurrentPathPos() {

        Vector2D currentPos = new Vector2D(initialPos);

        for (int v = 0; v < orderedPathVectors.size(); v++) {

            currentPos.add(orderedPathVectors.get(v));
        }

        return currentPos;
    }

    /**
     *  Returns a list of indexes of path components found in some circular area of the canvas
     * @param fieldPos  A position in the field's coordinate system where the method checks for path components, in Inches
     * @param radius    The radius of the selection circle around the fieldPos in Inches
     * @return          An Arraylist of integers representing the indexes of the path components found
     */
    public int[] getPathCompInRadiusInch(Vector2D fieldPos, double radius) {

        ArrayList<Integer> interceptComponentIndexes = new ArrayList<Integer>();

        // convert parameters to canvas coordinates b/c the pathbounds are already polygons in the canvas coordinate system and they're harder to convert
        fieldPos = convertFieldToCanvas(fieldPos);
        radius /= pixelsPerInch;
        Circle selectionBounds = new Circle(fieldPos.getComponent(0) - radius, fieldPos.getComponent(1) - radius, radius);

        for (int v = 0; v < pathBounds.size(); v++) {
            Polygon vectorBox = pathBounds.get(v);

            if (vectorBox.intersects(selectionBounds.getBoundsInLocal())) {

                interceptComponentIndexes.add(v);
            }
        }

        return (interceptComponentIndexes.stream().mapToInt(a -> a).toArray());
    }

    /**
     * Returns a list of indexes of path points found within a circular area of the canvas
     * @param fieldPos  A position in the field's coordinate system where the method checks for path points, in Inches
     * @param radius    The radius of the selection circle around the fieldPos in Inches
     * @return          An Arraylist of integers representing the indexes of the path points found
     */
    public int[] getPointInRadiusInch(Vector2D fieldPos, double radius) {

        ArrayList<Integer> interceptPointIndexes = new ArrayList<Integer>();

        Circle selectionBounds = new Circle(fieldPos.getComponent(0) - radius, fieldPos.getComponent(1) + radius, radius * 2);
        ArrayList<Vector2D> pathPoints = generatePathPoints();

//        Vector2D fieldPosCanvas = convertFieldToCanvas(new Vector2D(fieldPos.getComponent(0) - radius, fieldPos.getComponent(1) + radius));
//        fieldGraphics.strokeOval(fieldPosCanvas.getComponent(0), fieldPosCanvas.getComponent(1), (radius * 2 * pixelsPerInch), (radius * 2 * pixelsPerInch));

        for (int p = 0; p < pathPoints.size(); p++) {

            if (selectionBounds.contains(pathPoints.get(p).toPoint())) {

                interceptPointIndexes.add(p);
            }
        }

        return (interceptPointIndexes.stream().mapToInt(a -> a).toArray());
    }

    /**
     * Computes the bounding boxes for the path components given the current initial position and ordered path components
     * @return      An ArrayList of Polygons representing the bounding boxes of the path components in the canvas' coordinate system
     */
    private ArrayList<Polygon> generatePathBounds() {

        int vectorBoundExtension = 5;
        ArrayList<Polygon> boundingBoxes = new ArrayList<Polygon>();
        Vector2D currentPoint = new Vector2D(initialPos);

        for (int v = 0; v < orderedPathVectors.size(); v++) {

            Vector2D currentPathComponent = new Vector2D(orderedPathVectors.get(v));
            Polygon vectorBox = generateVectorBounds(currentPoint, currentPathComponent, vectorBoundExtension);
            boundingBoxes.add(vectorBox);
            currentPoint.add(currentPathComponent);
        }

        return boundingBoxes;
    }

    /**
     * Computes the individual bounding box for a single path component
     * @param initPos       The path component's initial position in the field's coordinate system
     * @param pathComponent The transformation of the initial point to the final point along the path component as the intermediate vector
     * @param boundSize     The distance of a line perpendicular to the vector that intercepts both the path component and the bounding box wall parallel to the vector
     * @return              The bounding box as a polygon in the canvas' coordinate system
     */
    private Polygon generateVectorBounds(Vector2D initPos, Vector2D pathComponent, double boundSize) {

        initPos = convertFieldToCanvas(initPos);
        pathComponent = convertFieldToCanvas(pathComponent);

        double xOffset = fieldDisplay.getWidth() / 2.0;
        double yOffset = fieldDisplay.getHeight() / 2.0;
        Vector2D centerOffset = new Vector2D(xOffset, yOffset);
        pathComponent.sub(centerOffset);

        ArrayList<Double> boundingBoxPoints = new ArrayList<Double>();
        double angle = pathComponent.getTheta() - (Math.PI / 2.0);

        Vector2D refVector = new Vector2D(angle, boundSize, true); // this vector is equal to half the width of the vector and quite useful

        // create the "bottom" two points, the ones perpendicular to the start of the vector
        Vector2D point = Vector2D.add(initPos, refVector);
        boundingBoxPoints.add(point.getComponent(0));
        boundingBoxPoints.add(point.getComponent(1));
        point.sub(Vector2D.scale(refVector, 2.0));
        boundingBoxPoints.add(point.getComponent(0));
        boundingBoxPoints.add(point.getComponent(1));

        initPos.add(pathComponent);

        // create the "top" two points, the ones perpendicular to the end of the vector
        point = Vector2D.sub(initPos, refVector);
        boundingBoxPoints.add(point.getComponent(0));
        boundingBoxPoints.add(point.getComponent(1));
        point.add(Vector2D.scale(refVector, 2.0));
        boundingBoxPoints.add(point.getComponent(0));
        boundingBoxPoints.add(point.getComponent(1));

        // for some weird reason the JavaFX polygon class requires the points as an even list of doubles with the x and the y coordinate alternating ex. (x0, y0, x1, y1,...)
        double[] pointsFormatted = boundingBoxPoints.stream().mapToDouble(a -> a).toArray();

        return (new Polygon(pointsFormatted));
    }

    /**
     * Provides an easy interface to get the points that define the currently defined path
     * @return  An ArrayList of Vectors representing the individual points along the currently defined path in the field's coordinate system
     */
    private ArrayList<Vector2D> generatePathPoints() {

        return generatePathPoints(initialPos, orderedPathVectors);
    }

    /**
     * Calculates the individual points along the path given some initial position and the path's point transformations
     * @param initialPosition   The starting position of the path, or the first point, in the field's coordinate system
     * @param pathComponents    The transformations of the initial point that represent the movement of the robot, in the field's coordinate system (Correct directions and Units in Inches)
     * @return                  An ArrayList of Vectors representing the individual points along the currently defined path in the field's coordinate system
     */
    public static ArrayList<Vector2D> generatePathPoints(Vector2D initialPosition, ArrayList<Vector2D> pathComponents) {

        ArrayList<Vector2D> pathPoints = new ArrayList<Vector2D>();
        pathComponents = new ArrayList<Vector2D>(pathComponents);
        Vector2D currentPoint = new Vector2D(initialPosition);
        pathPoints.add(new Vector2D(currentPoint));

        for (int p = 0; p < pathComponents.size(); p++) {

            currentPoint.add(pathComponents.get(p));
            pathPoints.add(new Vector2D(currentPoint));
        }

        return pathPoints;
    }

    /**
     * Translates the currently defined path given the translation
     * @param mouseClick    currently defined in context of the auto program it was originally written in, used as the "final position" of the translation relative to the previous mouse click
     */
    private void translate(Vector2D mouseClick) {

        int[] pathComponents = getPathCompInRadiusInch(mouseClick, 2);

        if (prevMouseClick == null) {

            if (pathComponents.length > 0) {

                prevMouseClick = mouseClick;
                editSelectCompLabel.setText("Path Selected: TRUE");
            }
        } else {
            Vector2D offset = Vector2D.sub(mouseClick, prevMouseClick);

            if (isPathInField(offset)) {

                initialPos.add(offset);
                pathBounds = generatePathBounds();
                initListener.updateInitPosFields();

                resetPathSpec();

                if (drawRobot) {

                    display.drawRobotInch(getCurrentPathPos());
                }
            } else {

                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setHeaderText("Out of Bounds Error");
                error.setContentText("The requested translation brought the path out of bounds");
                error.showAndWait();
            }

            prevMouseClick = null;
            editSelectCompLabel.setText("Path Selected: FALSE");
        }
    }

    /**
     * Rotates the current path given the current transform vector and transform angle, mainly here as something to automatically have the transform button's change listener to have something to call
     */
    private void rotatePath() {

        if (isPathInField(transformVector, Math.toRadians(transformAngle))) {

            rotatePath(transformVector, Math.toRadians(transformAngle));

            resetPathSpec();
        } else {

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setHeaderText("Out of Bounds Error");
            error.setContentText("The requested rotation brought the path out of bounds");
            error.showAndWait();
        }
    }

    /**
     * Actually takes in a specific pivot point and angle to rotate the current path with
     * @param pivot The point in the field's coordinate system that the path is to pivot around
     * @param angle The angle (Radians) in standard position that you want the path to rotate
     */
    private void rotatePath(Vector2D pivot, double angle) {

        ArrayList<Vector2D> newPathPoints = generatePathPoints();

        for (int v = 0; v < newPathPoints.size(); v++) {

            Vector2D currentPoint = newPathPoints.get(v);
            currentPoint.sub(pivot);
            currentPoint.rotate(angle);
            currentPoint.add(pivot);
        }

        initialPos = new Vector2D(newPathPoints.get(0));
        initListener.updateInitPosFields();

        for (int p = 1; p < newPathPoints.size(); p++) {

            newPathPoints.set(p - 1, Vector2D.sub(newPathPoints.get(p), newPathPoints.get(p - 1)));
        }

        newPathPoints.remove(newPathPoints.size() - 1);

        orderedPathVectors = newPathPoints;
        pathBounds = generatePathBounds();
    }

    /**
     * Reflects the currently defined path given the currently defined transformation angle
     */
    private void reflectPath() {

        if (isPathinField(transformAngle)) {

            reflectPath(transformAngle);

            resetField();
            display.drawReflectionLine();
            redrawPath(pathColors.toArray(new Color[0]));

            if (drawPoint) {

                redrawPoints(pointColors.toArray(new Color[0]));
            }

            if (snapToGrid) {

                display.drawGrid(fieldDisplay, 12, 12, Color.BLACK, new double[] {15, 15, 15, 15});
            }

            specListener.drawRobotChanged();
        } else {

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setHeaderText("Out of Bounds Error");
            error.setContentText("The requested reflection brought the path out of bounds");
            error.showAndWait();
        }
    }

    /**
     *  Given some reflection angle, this method applies the reflection transformation to each coordinate in the currently defined path
     * @param reflectionAngle  The angle the line of reflection makes with the horizontal in standard position (Degrees)
     */
    private void reflectPath(double reflectionAngle) {

       ArrayList<Vector2D> newPathPoints = generatePathPoints();

       for (int p = 0; p < newPathPoints.size(); p++) {

           newPathPoints.set(p, reflectPoint(newPathPoints.get(p), reflectionAngle));
       }

        initialPos = new Vector2D(newPathPoints.get(0));
        initListener.updateInitPosFields();

        for (int p = 1; p < newPathPoints.size(); p++) {

            newPathPoints.set(p - 1, Vector2D.sub(newPathPoints.get(p), newPathPoints.get(p - 1)));
        }

        newPathPoints.remove(newPathPoints.size() - 1);

        orderedPathVectors = newPathPoints;
        pathBounds = generatePathBounds();
    }

    /**
     * Given a point in the field's coordinate system and some angle of reflection, this method calculates the reflected point in the field's coordinate system
     * @param point             The point to be reflected in the field's coordinate system
     * @param reflectionAngle   The angle between a line of reflection intersecting the origin and the horizontal, or the x-axis in standard position
     * @return                  The reflected point in the field's coordinate system
     */
    private static Vector2D reflectPoint(Vector2D point, double reflectionAngle) {

        Vector2D lineRepresentation = new Vector2D(reflectionAngle, 1, false);
        double distance = Vector2D.crossMag(point, lineRepresentation) / lineRepresentation.getMag();

        double toNextPointAngle = normalizeAngle(reflectionAngle + 90);
        Vector2D reflectedPoint = Vector2D.add(point, new Vector2D(toNextPointAngle, distance * 2, false));

        return reflectedPoint;
    }

    public void fieldClicked(MouseEvent mouseEvent) {
        Vector2D mouseClick = new Vector2D(mouseEvent.getSceneX() - fieldDisplay.getLayoutX(), mouseEvent.getSceneY() - fieldDisplay.getLayoutY());
        Vector2D mouseCorrection = new Vector2D(0, -28);
        mouseClick.add(mouseCorrection);
        //fieldGraphics.strokeOval(mouseClick.getComponent(0) - 10, mouseClick.getComponent(1) - 10, 20, 20);
        mouseClick = convertCanvasToField(mouseClick);
        //System.out.println("Mouse pressed at: " + mouseClick);

        if (fieldBounds.contains(mouseClick.toPoint())) {
            switch (pathMode) {
                case "Design": //TODO: add collision detection with actual field elements

                    Vector2D prevPos = getCurrentPathPos();

                    if (pathComponentSnap.equals("Vertical")) {

                        mouseClick.setComponent(0, prevPos.getComponent(0));
                    } else if (pathComponentSnap.equals("Horizontal")) {

                        mouseClick.setComponent(1, prevPos.getComponent(1));
                    }

                    if (snapToGrid) {

                        mouseClick = snapToGridField(mouseClick);
                    }

                    //draw the robot to "simulate" the movement
                    if (drawRobot) {

                        display.drawRobotInch(mouseClick);
                    }

                    // draw and store vector properties
                    orderedPathVectors.add(Vector2D.sub(mouseClick, prevPos));
                    pathBounds.add(generateVectorBounds(prevPos, orderedPathVectors.get(orderedPathVectors.size() - 1), 5));
                    pathColors.add(currentPathColor);
                    display.drawVectorField(fieldDisplay, prevPos, mouseClick, currentPathColor);
                    //display.drawPathBounds();

                    if (drawPoint) {

                        // draw and store point properties
                        display.drawPointField(fieldDisplay, mouseClick, currentPointColor,1.5);
                    }

                    pointColors.add(currentPointColor);
                    //System.out.println("Path Length: " + orderedPathVectors.size());

                    //add the paths and points to have types modified later
                    pathTypeAssociations.put(orderedPathVectors.size() - 1, pathTypes.goToPosition);
                    //pointTypeAssociations.put(orderedPathVectors.size(), pointTypes.Regular);

                    prevMouseClick = null;
                    break;
                case "Edit Components":

                    // what did I press again?
                    int[] pathComponents = getPathCompInRadiusInch(mouseClick, 2);
                    //drawPathBounds();
                    System.out.println("Path Components Clicked: " + Arrays.toString(pathComponents));

                    int[] pathPoints = getPointInRadiusInch(mouseClick, 2);
                    System.out.println("Path Points Clicked: " + Arrays.toString(pathPoints));

                    if (pathPoints.length != 0) {

                        pathComponents = new int[0];
                    }

                    // updating the displays
                    if (pathComponents.length != 0) {

                        currentlySelectedPath = pathComponents[pathComponents.length - 1];
                        editSelectCompLabel.setText("Selected Component: " + currentlySelectedPath);
                        pathTypeDropdown.setDisable(false);

                        pathTypes selectedPathType = pathTypeAssociations.get(currentlySelectedPath);
                        pathTypeDropdown.getSelectionModel().select(selectedPathType);
                    } else {

                        currentlySelectedPath = -1;
                        editSelectCompLabel.setText("Selected Component: NONE");
                        pathTypeDropdown.setDisable(true);
                    }

                    if (pathPoints.length != 0) {

                        currentlySelectedPoint = pathPoints[pathPoints.length - 1];
                        editSelectPointLabel.setText("Selected Point: " + currentlySelectedPoint);

                        //pointTypes selectedPointType = pointTypeAssociations.get(currentlySelectedPoint);
                        //pointTypeDropdown.getSelectionModel().select(selectedPointType); // replace with a label that states the selected point type
                    } else {

                        currentlySelectedPoint = -1;
                        editSelectPointLabel.setText("Selected Point: NONE");
                    }

                    prevMouseClick = null;

                    // doing fun stuff with editing the paths
                    if (currentlySelectedPath != -1) {
                        switch (pathTypeAssociations.get(currentlySelectedPath).displayName()) {
                            case "Go To Position":

                                break;
                            case "Pure Pursuit":

                                break;
                        }
                    }

                    // doing fun stuff with editing points
                    if (currentlySelectedPoint != -1) {
//                        switch (pointTypeAssociations.get(currentlySelectedPoint).displayName()) {
//                            case "Regular":
//
//                                break;
//                            case "Fork - Start":
//
//                                break;
//                            case "Fork - End":
//
//                                break;
//                        }
                    }
                    
                    break;
                case "Rigid Transform":

                    switch(currentTransform) {
                        case "Translate":

                            translate(mouseClick);
                            break;
                        case "Rotate":


                            break;
                        case "Reflect":

                            break;
                    }

                    break;
            }
        }
    }

    /**
     * Overwrites the current field canvas with a blank version of the field image
     */
    private void resetField() {

        fieldGraphics.drawImage(field, 0, 0);
    }

    /**
     * Completely clears the current robot canvas
     */
    private void resetRobotDisplay() {

        robotGraphics.clearRect(0, 0, robotDisplay.getWidth(), robotDisplay.getHeight());
    }

    /**
     * Functional method to redraw the typical field elements given a special element is to be cleared
     */
    public void resetPathSpec() {

        resetField();

        if (snapToGrid) {

            display.drawGrid(fieldDisplay, 12, 12, Color.BLACK, new double[] {15, 15, 15, 15});
        }

        redrawPath(pathColors.toArray(new Color[0]));

        if (drawPoint) {

            redrawPoints(pointColors.toArray(new Color[0]));
        }
    }

    /**
     * Base case of a the more complex isPathInField(Vector2D) method, checks if the current path is within field bounds
     * @return whether the currently defined path is within the currently defined field bounds
     */
    public boolean isPathInField() {

        return isPathInField(new Vector2D(0, 0));
    }

    /**
     * Calculates whether the path violates the field boundaries given a hypothetical reflection
     * @param reflectionAngle   The angle the hypothetical reflection line would make with the horizontal
     * @return                  Whether or not the path is within the field boundaries after the hypothetical reflection
     */
    public boolean isPathinField(double reflectionAngle) {

        ArrayList<Vector2D> pathPoints = generatePathPoints();
        boolean inField = true;

        for (int p = 0; p < pathPoints.size() && inField; p++) {

            Vector2D currentPoint = reflectPoint(pathPoints.get(p), reflectionAngle);

            inField = fieldBounds.contains(currentPoint.toPoint());
        }

        return inField;
    }

    /**
     * Calculates whether the path violates the field boundaries given a hypothetical rotation
     * @param hypotheticalPivot The hypothetical point the function would rotate around
     * @param hypotheticalAngle The hypothetical hypotheticalAngle the function would rotate around the hypotheticalPivot
     * @return      Whether or not the path is within the field boundaries after the hypothetical rotation
     */
    public boolean isPathInField(Vector2D hypotheticalPivot, double hypotheticalAngle) {

        ArrayList<Vector2D> pathPoints = generatePathPoints();
        boolean inField = true;

        for (int v = 0; v < pathPoints.size() && inField; v++) {

            Vector2D currentPoint = pathPoints.get(v);
            currentPoint.sub(hypotheticalPivot);
            currentPoint.rotate(hypotheticalAngle);
            currentPoint.add(hypotheticalPivot);

            inField = fieldBounds.contains(currentPoint.toPoint());
        }

        return inField;
    }

    /**
     * Calculates whether the path violates the field boundaries given a hypothetical translation
     * @param hypotheticalOffset    The hypothetical offset the path would be moved
     * @return                      Whether the path is within the field boundaries after the hypothetical translation
     */
    public boolean isPathInField(Vector2D hypotheticalOffset) {

        Vector2D currentPoint = Vector2D.add(initialPos, hypotheticalOffset);

        if (fieldBounds.contains(currentPoint.toPoint())) {

            boolean inField = true;

            for (int v = 0; v < orderedPathVectors.size() && inField; v++) {

                currentPoint.add(orderedPathVectors.get(v));
                inField = fieldBounds.contains(currentPoint.toPoint());
            }

            return inField;
        } else {

            return false;
        }
    }

    private void redrawPath() {

        redrawPath(new Color[0]);
    }

    private void redrawPath(Color pathColor) {

        redrawPath(Collections.nCopies(orderedPathVectors.size(), pathColor).toArray(new Color[0]));
    }

    private void redrawPath(Color[] pathColors) {

        Vector2D currentPos = new Vector2D(initialPos);

        display.drawPointField(fieldDisplay, initialPos, pointColors.get(0), 1.5);

        for (int v = 0; v < orderedPathVectors.size(); v++) {

            Color pathColor = Color.LAWNGREEN;

            if (v < pathColors.length) {

                pathColor = pathColors[v];
            }

            display.drawVectorField(fieldDisplay, currentPos, Vector2D.add(currentPos, orderedPathVectors.get(v)), pathColor);
            currentPos.add(orderedPathVectors.get(v));
        }
    }

    private void redrawPoints() {

        redrawPoints(new Color[0]);
    }

    private void redrawPoints(Color pointColor) {

        redrawPoints(Collections.nCopies(orderedPathVectors.size() + 1, pointColor).toArray(new Color[0]));
    }

    private void redrawPoints(Color[] pointColors) {

        Vector2D initPoint = new Vector2D(initialPos);

        for (int p = 0; p <= orderedPathVectors.size(); p++) {

            Color pointColor = Color.RED;

            if (p < pointColors.length) {

                pointColor = pointColors[p];
            }

            display.drawPointField(fieldDisplay, initPoint, pointColor, 1.5);

            if (p < orderedPathVectors.size()) {

                initPoint.add(orderedPathVectors.get(p));
            }
        }
    }

    class WindowDrawer {

        /**
         * uses the currently defined transform angle to draw a reflection line when the reflect transformation is selected
         */
        private void drawReflectionLine() {

            double radius = (700.0 / 2.0) * Math.sqrt(2);

            double xOffset = fieldDisplay.getWidth() / 2.0;
            double yOffset = fieldDisplay.getHeight() / 2.0;
            Vector2D centerOffset = new Vector2D(xOffset, yOffset);
            Vector2D initPoint = new Vector2D(transformAngle, radius, false);
            Vector2D termPoint = new Vector2D(transformAngle + 180, radius, false);
            initPoint.flipDimension(1);
            termPoint.flipDimension(1);
            initPoint.add(centerOffset);
            termPoint.add(centerOffset);

            Color prevStroke = (Color) fieldGraphics.getStroke();
            fieldGraphics.setStroke(Color.DARKBLUE);
            fieldGraphics.strokeLine(initPoint.getComponent(0), initPoint.getComponent(1), termPoint.getComponent(0), termPoint.getComponent(1));
            fieldGraphics.setStroke(prevStroke);
        }

        /**
         * Simply uses the currently defined path bounds and draws them on the field display
         */
        private void drawPathBounds() {

            for (int p = 0; p < pathBounds.size(); p++) {

                drawPolygon(pathBounds.get(p), new Vector2D(0, 0));
            }
        }

        /**
         * Draws the robot on the robot canvas at a specific field location
         * @param fieldPos  The location to draw the robot in the field's coordinate system
         */
        private void drawRobotInch(Vector2D fieldPos) { // TODO: maybe make the robot rotate to the orientation of the vector when you create it?

            resetRobotDisplay();
            fieldPos = convertFieldToCanvas(fieldPos);
            fieldPos.sub(new Vector2D(robot.getWidth() / 2.0, robot.getHeight() / 2.0));

            robotGraphics.drawImage(robot, fieldPos.getComponent(0), fieldPos.getComponent(1));
        }

        private void drawVectorField(Canvas c, Vector2D initPoint, Vector2D termPoint, Color arrowColor) {

            initPoint = convertFieldToCanvas(initPoint);
            termPoint = convertFieldToCanvas(termPoint);

            drawVector(c, initPoint, termPoint, arrowColor);
        }

        /**
         *  Draws a vector of some color and length on an arbitrary canvas
         * @param c             The Canvas to be drawn on
         * @param initPoint     The initial point of the vector on the canvas in the canvas' coordinate system
         * @param termPoint     The final point of the vector on the canvas in the canvas' coordinate system
         * @param arrowColor    The color of the vector to be drawn
         */
        private void drawVector(Canvas c, Vector2D initPoint, Vector2D termPoint, Color arrowColor) {

            initPoint = new Vector2D(initPoint);
            termPoint = new Vector2D(termPoint);

            GraphicsContext g = c.getGraphicsContext2D();
            Color prevStroke = (Color) g.getStroke();
            Color prevFill = (Color) g.getFill();
            g.setStroke(arrowColor);
            g.setFill(arrowColor);

            Vector2D pathVector = Vector2D.sub(termPoint, initPoint);
            double angle = pathVector.getTheta();
            double triangleSideMagnitude = 13;

            double[] xPoints = new double[3];
            double[] yPoints = new double[3];

            //tip
            xPoints[1] = termPoint.getComponent(0);
            yPoints[1] = termPoint.getComponent(1);

            // second corner of the triangle
            Vector2D tip1 = Vector2D.add(termPoint, new Vector2D(normalizeAngle(135 + Math.toDegrees(angle)), triangleSideMagnitude, false));
            xPoints[0] = tip1.getComponent(0);
            yPoints[0] = tip1.getComponent(1);

            //third corner of the triangle
            Vector2D tip2 = Vector2D.add(termPoint, new Vector2D(normalizeAngle(Math.toDegrees(angle) - 135), triangleSideMagnitude, false));
            xPoints[2] = tip2.getComponent(0);
            yPoints[2] = tip2.getComponent(1);

            g.strokeLine(initPoint.getComponent(0), initPoint.getComponent(1), xPoints[1], yPoints[1]);
            g.fillPolygon(xPoints, yPoints, 3);

            g.setStroke(prevStroke);
            g.setFill(prevFill);
        }

        /**
         * Draws a point of some color at some location on an arbitrary canvas
         * @param c                 The Canvas to be drawn on
         * @param point             The location of the point to be drawn in the field's coordinate system
         * @param pointColor        The color of the point to be drawn
         * @param pointDiameter     The diameter of the point to be drawn (Inches)
         */
        private void drawPointField(Canvas c, Vector2D point, Color pointColor, double pointDiameter) {

            point = convertFieldToCanvas(point);
            pointDiameter *= pixelsPerInch;

            drawPoint(c, point, pointColor, (int) Math.round(pointDiameter));
        }

        /**
         * Draws a point of some color at some location on an arbitrary canvas
         * @param c                 The Canvas to be drawn on
         * @param point             The location of the point to be drawn in the canvas' coordinate system
         * @param pointColor        The color of the point to be drawn
         * @param pointDiameter     The diameter of the point to be drawn (Pixels)
         */
        private void drawPoint(Canvas c, Vector2D point, Color pointColor, int pointDiameter) { //TODO: clean up code such that these take in only real canvas coordinates and not field coordinates scaled to be pixels, use the new convert methods

            Vector2D drawCorrection = new Vector2D(-(pointDiameter / 2.0), -(pointDiameter / 2.0));
            point.add(drawCorrection);

            GraphicsContext g = c.getGraphicsContext2D();

            Color prevStroke = (Color) g.getStroke();
            double prevStrokeWidth = g.getLineWidth();

            g.setStroke(pointColor);
            g.setLineWidth(3);

            g.strokeOval(point.getComponent(0), point.getComponent(1), pointDiameter, pointDiameter);

            g.setStroke(prevStroke);
            g.setLineWidth(prevStrokeWidth);
        }

        /**
         * Generic method to draw some Image object onto an arbitrary canvas at some location
         * @param c             The Canvas to be drawn on
         * @param cornerPos     The location of the upper left corner of the image on the canvas in the field's coordinate system
         * @param image         The image to be drawn
         */
        private void drawImageInch(Canvas c, Vector2D cornerPos, Image image) {

            cornerPos = convertFieldToCanvas(cornerPos);
            drawImage(c, cornerPos, image);
        }

        /**
         * A generic method to draw an Image at some location on some arbitrary canvas
         * @param c             The Canvas to be drawn on
         * @param cornerPos     The location of the upper left corner of the image on the canvas in the canvas' coordinate system
         * @param image         The image to be drawn
         */
        private void drawImage(Canvas c, Vector2D cornerPos, Image image) {

            if (c.contains(cornerPos.toPoint())) {

                GraphicsContext g = c.getGraphicsContext2D();

                g.drawImage(image, cornerPos.getComponent(0), cornerPos.getComponent(1));
            }
        }

        /**
         * Draws a generic polygon onto the field canvas at some location
         * @param p         The polygon to be drawn sized in pixels
         * @param center    The point the polygon is to be drawn at in the canvas' coordinate system
         */
        private void drawPolygon(Polygon p, Vector2D center) {

            ObservableList<Double> points = p.getPoints();
            double[] xPoints = new double[points.size() / 2];
            double[] yPoints = new double[points.size() / 2];

            for (int q = 0; q < points.size(); q++) {

                if ((q % 2) == 0) {

                    xPoints[q / 2] = points.get(q) + center.getComponent(0);
                } else {

                    yPoints[q / 2] = (points.get(q) + center.getComponent(1));
                }
            }

            Color prevStroke = (Color) fieldGraphics.getStroke();
            fieldGraphics.setStroke(Color.BLACK);

            fieldGraphics.strokePolygon(xPoints, yPoints, points.size() / 2);

            fieldGraphics.setStroke(prevStroke);
        }

        /**
         * Draws a padded grid on an arbitrary canvas
         * @param c             The canvas to be drawn on
         * @param rows          The amount of rows to be in the grid
         * @param columns       The amount of columns to be in the grid
         * @param gridColor     The color of the grid to be drawn
         * @param padding       The padding constants for the grid to be drawn
         */
        private void drawGrid(Canvas c, int rows, int columns, Color gridColor, double[] padding) { // padding is {up, left, down, right}

            double xInterval = (c.getWidth() - padding[1] - padding[3]) / columns;
            double yInterval = (c.getHeight() - padding[0] - padding[2]) / rows;

            GraphicsContext g = c.getGraphicsContext2D();
            Color prevStroke = (Color) g.getStroke();
            g.setStroke(gridColor);

            for (int col = 0; col < (columns + 1); col++) {

                int xPos = (int) (padding[1] + (xInterval * col));
                g.strokeLine(xPos, padding[0], xPos, c.getHeight() - padding[2]);
            }

            for (int row = 0; row < (rows + 1); row++) {

                int yPos = (int) (padding[0] + (xInterval * row));
                g.strokeLine(padding[1], yPos, c.getWidth() - padding[3], yPos);
            }

            g.setStroke(prevStroke);
        }

        private void setPathDesignVisible(boolean show) { // displays or hides the Nodes associated with designing the path (Selecting points, editing position, Defining Curves)
            //path
            setVisible(show, pathText);
            setVisible(show, snapToGridCheckBox);
            setVisible(show, pathColorSetText);
            setVisible(show, currentPathColorPicker);
            setVisible(show, compSnapText);
            setVisible(show, pathComponentSnapDropdown);

            //point
            setVisible(show, pointText);
            setVisible(show, drawPointCheckBox);
            setVisible(show, pointColorSetText);
            setVisible(show, pointColorPicker);
            setVisible(show, drawRobotCheckBox);
        }

        private void setPathEditVisible(boolean show) { // displays or hides the Nodes associated with selecting path components and editing properties (Path type, Speed, Color, etc.)
            //path
            setVisible(show, pathText);
            setVisible(show, editSelectCompLabel);
            setVisible(show, pathTypeSetText);
            setVisible(show, pathTypeDropdown);

            //point
            setVisible(show, pointText);
            setVisible(show, editSelectPointLabel);
            setVisible(show, forkedPathOperationsText);
            setVisible(show, modifyForkBtn);
            setVisible(show, deleteForkBtn);

            if (forkedPaths.size() == 0) {

                show = false;
            }

            setVisible(show, forkViewText);
            setVisible(show, forkTreeView);
        }

        private void setPathRTVisible(boolean show, String transform) { // displays or hides the Nodes associated with transforming the path rigidly (Rotation, Translation, Reflection)

            setVisible(show, pathText);
            setVisible(show, editSelectCompLabel);
            setVisible(show, pathTransformSetText);
            setVisible(show, pathTransformDropdown);
            resetPathSpec();

            switch (transform) {
                case "Translate":

                    showRotate(false);
                    showReflect(false);
                    showTranslate(show);
                    break;
                case "Rotate":

                    showTranslate(false);
                    showReflect(false);
                    showRotate(show);
                    break;
                case "Reflect":

                    if (show) {
                        drawReflectionLine();
                    }

                    showTranslate(false);
                    showRotate(false);
                    showReflect(show);
                    break;
                default:

                    showTranslate(false);
                    showRotate(false);
                    showReflect(false);
                    break;
            }
        }

        private void showTranslate(boolean show) {

            setVisible(show, pivotTransXPosText);
            setVisible(show, pivotTransYPosText);
            setVisible(show, pivotTranslateXField);
            setVisible(show, pivotTranslateYField);
            setVisible(show, transformPathBtn);

            pivotTransXPosText.setText("Translate X");
            pivotTransYPosText.setText("Translate Y");
            transformPathBtn.setText("Translate Path");
        }

        private void showRotate(boolean show) {

            setVisible(show, pivotTransXPosText);
            setVisible(show, pivotTransYPosText);
            setVisible(show, rotRefAngleText);
            setVisible(show, pivotTranslateXField);
            setVisible(show, pivotTranslateYField);
            setVisible(show, rotateReflectAngleField);
            setVisible(show, transformPathBtn);

            pivotTransXPosText.setText("Pivot X Position");
            pivotTransYPosText.setText("Pivot Y Position");
            rotRefAngleText.setText("Rotation Angle");
            transformPathBtn.setText("Rotate Path");
        }

        private void showReflect(boolean show) {

            setVisible(show, rotRefAngleText);
            setVisible(show, rotateReflectAngleField);
            setVisible(show, transformPathBtn);

            rotRefAngleText.setText("Reflection Line Angle");
            transformPathBtn.setText("Reflect Path");
        }
    }

    class programConstructor {

        public boolean outputPath(Path outputFile) {

            return false; // This has to reflect whether the output proceeded correctly
        }

        public boolean importPath(Path inputFile) {

            return true;
        }
    }

    /*---------- Testers ----------*/

    private void vectorDrawTester() {

        display.drawVectorField(fieldDisplay, new Vector2D(0, 0), new Vector2D(36, 36), Color.LAWNGREEN);
        display.drawVectorField(fieldDisplay, new Vector2D(36, 36), new Vector2D(36, -36), Color.LAWNGREEN);

        for (int theta = 0; theta < 360; theta += 12) {

            display.drawVectorField(fieldDisplay, new Vector2D(0, 0), new Vector2D(theta, 36, false), Color.LAWNGREEN);
//            System.out.println("Loop theta: " + theta);
        }
        System.out.println("Finished!");
    }

    private static void reflectionTester() {

        Vector2D initialPosition = new Vector2D(63, -36);
        Vector2D[] path = {
                new Vector2D(-12, 12),
                new Vector2D(0, 12),
                new Vector2D(-36, 0)
        };

        ArrayList<Vector2D> newPath = new ArrayList<Vector2D>();
        newPath.addAll(Arrays.asList(path));

        ArrayList<Vector2D> pathPoints = generatePathPoints(initialPosition, newPath);
        System.out.println(pathPoints);

        for (int p = 0; p < pathPoints.size(); p++) {

            Vector2D currentPoint = reflectPoint(pathPoints.get(p), 90);
            System.out.println(p + ": " + currentPoint);
        }
    }

    /**
     * Creates different tree view structures to test the selection index algorithm
     * @param test  An identifier for which preset tree structure is to be returned
     * @return  A preset TreeItem structure
     */
    private static TreeItem<String> treeViewTester(int test) {

        TreeItem<String> root = new TreeItem<String>();
        root.setExpanded(true);

        switch (test) {
            case 0:

                TreeItem<String> fork1 = new TreeItem<String>("First Fork");
                TreeItem<String> route11 = new TreeItem<String>("Sub Path 11");
                TreeItem<String> route12 = new TreeItem<String>("Sub Path 12");
                TreeItem<String> route13 = new TreeItem<String>("Sub Path 13");
                fork1.getChildren().add(route11);
                fork1.getChildren().add(route12);
                fork1.getChildren().add(route13);

                TreeItem<String> fork2 = new TreeItem<String>("Second Fork");
                TreeItem<String> route21 = new TreeItem<String>("Sub Path 21");
                TreeItem<String> route22 = new TreeItem<String>("Sub Path 22");
                TreeItem<String> route23 = new TreeItem<String>("Sub Path 23");
                fork2.getChildren().add(route21);
                fork2.getChildren().add(route22);
                fork2.getChildren().add(route23);

                // finalize the structure and add it to the dummy root
                root.getChildren().add(fork1);
                root.getChildren().add(fork2);
                break;
            case 1:

                TreeItem<String> node1 = new TreeItem<String>("Node 1");
                    TreeItem<String> node11 = new TreeItem<String>("Node 11");
                    node1.getChildren().add(node11);
                        node11.getChildren().add(new TreeItem<String>("Node 111"));
                        node11.getChildren().add(new TreeItem<String>("Node 112"));
                        node11.getChildren().add(new TreeItem<String>("Node 113"));
                        node11.getChildren().add(new TreeItem<String>("Node 114"));
                        node11.getChildren().add(new TreeItem<String>("Node 115"));
                    node1.getChildren().add(new TreeItem<String>("Node 12"));
                    node1.getChildren().add(new TreeItem<String>("Node 13"));
                    node1.getChildren().add(new TreeItem<String>("Node 14"));
                    node1.getChildren().add(new TreeItem<String>("Node 15"));

                TreeItem<String> node2 = new TreeItem<String>("Node 2");
                    node2.getChildren().add(new TreeItem<String>("Node 21"));
                    node2.getChildren().add(new TreeItem<String>("Node 22"));
                    node2.getChildren().add(new TreeItem<String>("Node 23"));
                    node2.getChildren().add(new TreeItem<String>("Node 24"));
                    node2.getChildren().add(new TreeItem<String>("Node 25"));

                TreeItem<String> node3 = new TreeItem<String>("Node 3");
                    node3.getChildren().add(new TreeItem<String>("Node 31"));
                    node3.getChildren().add(new TreeItem<String>("Node 32"));
                    node3.getChildren().add(new TreeItem<String>("Node 33"));
                    node3.getChildren().add(new TreeItem<String>("Node 34"));
                    node3.getChildren().add(new TreeItem<String>("Node 35"));

                    root.getChildren().add(node1);
                    root.getChildren().add(node2);
                    root.getChildren().add(node3);
                break;
        }

        return root;
    }

    public static void main(String[] args) {

        reflectionTester();
    }
}
