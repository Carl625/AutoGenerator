package AutoPathGenerator;

import Resources.Vector2D;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WindowController {

    /*---------- Path Specification Variables ----------*/

    //display variables
    public Canvas fieldDisplay; // TODO: make a class that encapsulates all drawing functions, more OOP
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

    public Label pathTypeSetText;
    public ChoiceBox<String> pathTypeDropdown;
    private String pathType;

    public Label pointTypeSetText;
    public ChoiceBox<String> pointTypeDropdown;
    private String pointType;

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
    private double fieldLengthInch = 24.0 * 6.0;
    private double pixelsPerInch = (670.0 / fieldLengthInch); // 700x700 pixel image of a 12ft x 12ft field, 670 of the pixels actually represent the length of the field
    private Polygon fieldBounds = new Polygon(fieldLengthInch / -2.0, fieldLengthInch / -2.0, fieldLengthInch / -2.0, fieldLengthInch / 2.0, fieldLengthInch / 2.0, fieldLengthInch / 2.0, fieldLengthInch / 2.0, fieldLengthInch / -2.0);

    private ArrayList<Vector2D> orderedPathVectors;
    private ArrayList<Polygon> pathBounds;
    private ArrayList<Color> pathColors;
    private ArrayList<Color> pointColors;

    private Vector2D initialPos;
    private Vector2D prevMouseClick;

    public void initialize() {

        //init internal variables
        orderedPathVectors = new ArrayList<Vector2D>();
        pathBounds = new ArrayList<Polygon>();
        pathColors = new ArrayList<Color>();
        pointColors = new ArrayList<Color>();

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

        initialPos = new Vector2D(63, -36);
        pointColors.add(currentPointColor);
        redrawPoints();

        //Path editing and definition initialization
        initPathDef();

        //Point editing and definition initialization
        initPointDef();

        //testers
        //vectorDrawTester();
    }

    private void initPathDef() {

        pathModeDropdown.getItems().clear();
        pathModeDropdown.getItems().add("Design");
        pathModeDropdown.getItems().add("Edit Components");
        pathModeDropdown.getItems().add("Rigid Transform");
        pathModeDropdown.getSelectionModel().select(0);
        pathModeDropdown.setOnAction(event -> pathModeChanged());
        pathMode = pathModeDropdown.getItems().get(0);

        //path design
        snapToGridCheckBox.setSelected(false);
        snapToGridCheckBox.setOnAction(event -> snapToGridChanged());
        snapToGrid = snapToGridCheckBox.isSelected();

        currentPathColorPicker.setValue(Color.LAWNGREEN);
        currentPathColorPicker.setOnAction(event -> pathColorChanged());
        currentPathColor = currentPathColorPicker.getValue();

        pathComponentSnapDropdown.getItems().clear();
        pathComponentSnapDropdown.getItems().add("None");
        pathComponentSnapDropdown.getItems().add("Vertical");
        pathComponentSnapDropdown.getItems().add("Horizontal");
        pathComponentSnapDropdown.getSelectionModel().select(0);
        pathComponentSnapDropdown.setOnAction(event -> pathComponentSnapChanged());

        //point design
        drawPointCheckBox.setSelected(false);
        drawPointCheckBox.setOnAction(event -> drawPointChanged());
        drawPoint = drawPointCheckBox.isSelected();

        pointColorPicker.setValue(Color.RED);
        pointColorPicker.setOnAction(event -> pointColorChanged());
        currentPointColor = pointColorPicker.getValue();

        drawRobotCheckBox.setSelected(false);
        drawRobotCheckBox.setOnAction(event -> drawRobotChanged());
        drawRobot = drawPointCheckBox.isSelected();

        //path editing
        pathTypeDropdown.getItems().clear();
        pathTypeDropdown.getItems().add("Go To Position");
        pathTypeDropdown.getItems().add("Pure Pursuit");
        pathTypeDropdown.getSelectionModel().select(0);
        pathTypeDropdown.setOnAction(event -> pathTypeChanged());
        pathType = pathTypeDropdown.getItems().get(0);

        //point editing
        pointTypeDropdown.getItems().clear();
        pointTypeDropdown.getItems().add("Regular");
        pointTypeDropdown.getItems().add("Forked"); // TODO: add another textfield that either appears or becomes enabled when this is selected to specify what variable it should depend on? Work it out dude
        pointTypeDropdown.getSelectionModel().select(0);
        pointTypeDropdown.setOnAction(event -> pointTypeChanged());
        pointType = pointTypeDropdown.getItems().get(0);

        //path transforms
        pathTransformDropdown.getItems().clear();
        pathTransformDropdown.getItems().add("Translate");
        pathTransformDropdown.getItems().add("Rotate");
        pathTransformDropdown.getItems().add("Reflect");
        pathTransformDropdown.getSelectionModel().select(0);
        pathTransformDropdown.setOnAction(event -> transformChanged());
        currentTransform = pathTransformDropdown.getItems().get(0);

        pivotTranslateXField.textProperty().addListener((o, ov, nv) -> pivotPosChanged());
        pivotTranslateYField.textProperty().addListener((o, ov, nv) -> pivotPosChanged());
        rotateReflectAngleField.textProperty().addListener((o, ov, nv) -> rotationAngleChanged());
//        pivotXField.setOnAction(event -> pivotPosChanged());
//        pivotYField.setOnAction(event -> pivotPosChanged());
//        rotateAngleField.setOnAction(event -> rotationAngleChanged());
        transformVector = new Vector2D(0, 0);
        transformAngle = 0;

        transformPathBtn.setOnAction(event -> transform());

        // general
        clearButton.setOnAction(event -> clearPath());
        undoButton.setOnAction(event -> undo());
        redoButton.setOnAction(event -> redo());
    }

    private void initPointDef() {
    }

    private void initAutoInit() {

        // dropdown initialization
        defStoneDropdown.getItems().clear();
        defStoneDropdown.getItems().add("Left");
        defStoneDropdown.getItems().add("Middle");
        defStoneDropdown.getItems().add("Right");
        defStoneDropdown.getSelectionModel().select(1);
        defStoneDropdown.setOnAction(event -> defStoneDropdownChanged());

        allianceDropdown.getItems().clear();
        allianceDropdown.getItems().add("Red");
        allianceDropdown.getItems().add("Blue");
        allianceDropdown.getSelectionModel().select(0);
        allianceDropdown.setOnAction(event -> allianceDropdownChanged());

        initGrabDropdown.getItems().clear();
        initGrabDropdown.getItems().add("Grab");
        initGrabDropdown.getItems().add("Release");
        initGrabDropdown.getSelectionModel().select(1);
        initGrabDropdown.setOnAction(event -> grabDropdownChanged());

        initArmDropdown.getItems().clear();
        initArmDropdown.getItems().add("In");
        initArmDropdown.getItems().add("Standby");
        initArmDropdown.getItems().add("Intermediate 2");
        initArmDropdown.getItems().add("Drop");
        initArmDropdown.getItems().add("Specific");
        initArmDropdown.getSelectionModel().select(1);
        initArmDropdown.setOnAction(event -> armDropdownChanged());

        // field initialization
        autoNameField.textProperty().addListener((o, ov, nv) -> autoNameChanged());
        specArmPosField.textProperty().addListener((o, ov, nv) -> specArmPosChanged());
        startDragPosField.textProperty().addListener((o, ov, nv) -> startDragPosChanged());
        initXField.textProperty().addListener((o, ov, nv) -> {if (initXField.focusedProperty().getValue()) {
            initPosChanged();
        }});
        initYField.textProperty().addListener((o, ov, nv) -> {if (initYField.focusedProperty().getValue()) {
            initPosChanged();
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

    private void pathModeChanged() {

        int selectIndex = pathModeDropdown.getSelectionModel().getSelectedIndex();

        if (selectIndex != -1) {

            pathMode = pathModeDropdown.getItems().get(selectIndex);
            //System.out.println("new PathMode: " + pathMode);

            setPathDesignVisible(false);
            setPathEditVisible(false);
            setPathRTVisible(false, currentTransform);

            switch (pathMode) {
                case "Design":

                    setPathDesignVisible(true);
                    break;
                case "Edit Components":

                    editSelectCompLabel.setText("Selected Component: NONE");
                    setPathEditVisible(true);
                    break;
                case "Rigid Transform":

                    prevMouseClick = null;
                    editSelectCompLabel.setText("Path Selected: FALSE");
                    setPathRTVisible(true, currentTransform);
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

            drawRobotInch(getCurrentPathPos());
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

        if (newlySelected != -1) {

            pathType = pathTypeDropdown.getItems().get(pathTypeDropdown.getSelectionModel().getSelectedIndex());
        }
    }

    private void pointTypeChanged() {

        int newlySelected = pointTypeDropdown.getSelectionModel().getSelectedIndex();

        if (newlySelected != -1) {

            pointType = pointTypeDropdown.getItems().get(pointTypeDropdown.getSelectionModel().getSelectedIndex());
        }
    }

    private void transformChanged() {

        int newlySelected = pathTransformDropdown.getSelectionModel().getSelectedIndex();

        if (newlySelected != -1) {

            currentTransform = pathTransformDropdown.getItems().get(newlySelected);
            setPathRTVisible(pathMode.equals("Rigid Transform"), currentTransform);
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
            drawReflectionLine();
            redrawPath(pathColors.toArray(new Color[0]));

            if (drawPoint) {

                redrawPoints(pointColors.toArray(new Color[0]));
            }

            if (snapToGrid) {

                drawGrid(fieldDisplay, 12, 12, Color.BLACK, new double[] {15, 15, 15, 15});
            }
        }
    }

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
        double x = initialPos.getComponent(0);
        double y = initialPos.getComponent(1);

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

    private void clearPath() {

        orderedPathVectors.clear();
        pathBounds.clear();
        pathColors.clear();
        pointColors.clear();
        pointColors.add(currentPointColor);

        resetField();
        drawPointField(fieldDisplay, initialPos, pointColors.get(0), 6);

        if (pathMode.equals("Rigid Transform") && currentTransform.equals("Reflect")) {

            drawReflectionLine();
        }

        if (snapToGrid) {

            drawGrid(fieldDisplay, 12, 12, Color.BLACK, new double[] {15, 15, 15, 15});
        }

        resetRobotDisplay();
    }

    private void undo() { // how should I make the undo stack generalized? make them strings for specific actions encoded in a method?

    }

    private void redo() {

    }

    /*---------- JavaFX Convenience Methods ----------*/
    public void setVisible(boolean show, Node component) {

        component.setVisible(show);
        component.managedProperty().bind(component.visibleProperty());
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

        ArrayList<Double> boundingBoxPoints = new ArrayList<>();
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
        updateInitPosFields();

        for (int p = 1; p < newPathPoints.size(); p++) {

            newPathPoints.set(p - 1, Vector2D.sub(newPathPoints.get(p), newPathPoints.get(p - 1)));
        }

        newPathPoints.remove(newPathPoints.size() - 1);

        orderedPathVectors = newPathPoints;
        pathBounds = generatePathBounds();
    }

    private ArrayList<Vector2D> generatePathPoints() {

        return generatePathPoints(initialPos, orderedPathVectors);
    }

    public static ArrayList<Vector2D> generatePathPoints(Vector2D initialPosition, ArrayList<Vector2D> pathComponents) {

        ArrayList<Vector2D> pathPoints = new ArrayList<>();
        pathComponents = new ArrayList<>(pathComponents);
        Vector2D currentPoint = new Vector2D(initialPosition);
        pathPoints.add(new Vector2D(currentPoint));

        for (int p = 0; p < pathComponents.size(); p++) {

            currentPoint.add(pathComponents.get(p));
            pathPoints.add(new Vector2D(currentPoint));
        }

        return pathPoints;
    }

    private void reflectPath() {

        if (isPathinField(transformAngle)) {

            reflectPath(transformAngle);

            resetField();
            drawReflectionLine();
            redrawPath(pathColors.toArray(new Color[0]));

            if (drawPoint) {

                redrawPoints(pointColors.toArray(new Color[0]));
            }

            if (snapToGrid) {

                drawGrid(fieldDisplay, 12, 12, Color.BLACK, new double[] {15, 15, 15, 15});
            }

            drawRobotChanged();
        } else {

            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setHeaderText("Out of Bounds Error");
            error.setContentText("The requested reflection brought the path out of bounds");
            error.showAndWait();
        }
    }

    /**
     *
     * @param reflectionAngle  The angle the line of reflection makes with the horizontal in standard position (Degrees)
     */
    private void reflectPath(double reflectionAngle) {

       ArrayList<Vector2D> newPathPoints = generatePathPoints();

       for (int p = 0; p < newPathPoints.size(); p++) {

           newPathPoints.set(p, reflectPoint(newPathPoints.get(p), reflectionAngle));
       }

        initialPos = new Vector2D(newPathPoints.get(0));
        updateInitPosFields();

        for (int p = 1; p < newPathPoints.size(); p++) {

            newPathPoints.set(p - 1, Vector2D.sub(newPathPoints.get(p), newPathPoints.get(p - 1)));
        }

        newPathPoints.remove(newPathPoints.size() - 1);

        orderedPathVectors = newPathPoints;
        pathBounds = generatePathBounds();
    }

    private static Vector2D reflectPoint(Vector2D point, double reflectionAngle) {

        Vector2D lineRepresentation = new Vector2D(reflectionAngle, 1, false);
        double distance = Vector2D.crossMag(point, lineRepresentation) / lineRepresentation.getMag();

        double toNextPointAngle = normalizeAngle(reflectionAngle + 90);
        Vector2D reflectedPoint = Vector2D.add(point, new Vector2D(toNextPointAngle, distance * 2, false));

        return reflectedPoint;
    }

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

            drawPolygonField(pathBounds.get(p), new Vector2D(0, 0));
        }
    }

    private void drawPolygonField(Polygon p, Vector2D center) {

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

//        System.out.println("Border X points: " + Arrays.toString(xPoints));
//        System.out.println("Border Y points: " + Arrays.toString(yPoints));

        Color prevStroke = (Color) fieldGraphics.getStroke();
        fieldGraphics.setStroke(Color.BLACK);

        fieldGraphics.strokePolygon(xPoints, yPoints, points.size() / 2);

        fieldGraphics.setStroke(prevStroke);
    }

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

    public void fieldClicked(MouseEvent mouseEvent) {
        Vector2D mouseClick = new Vector2D(mouseEvent.getSceneX() - fieldDisplay.getLayoutX(), mouseEvent.getSceneY() - fieldDisplay.getLayoutY());
        Vector2D mouseCorrection = new Vector2D(0, -28);
        mouseClick.add(mouseCorrection);
        //fieldGraphics.strokeOval(mouseClick.getComponent(0) - 10, mouseClick.getComponent(1) - 10, 20, 20);
        mouseClick = convertCanvasToField(mouseClick);
        //System.out.println("Mouse pressed at: " + mouseClick);

        switch (pathMode) {
            case "Design":

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

                    drawRobotInch(mouseClick);
                }

                // draw and store vector properties
                orderedPathVectors.add(Vector2D.sub(mouseClick, prevPos));
                pathBounds.add(generateVectorBounds(prevPos, orderedPathVectors.get(orderedPathVectors.size() - 1), 5));
                pathColors.add(currentPathColor);
                drawVectorField(fieldDisplay, prevPos, mouseClick, currentPathColor);
                //drawPathBounds();

                if (drawPoint) {

                    // draw and store point properties
                    drawPointField(fieldDisplay, mouseClick, currentPointColor,6);
                }

                pointColors.add(currentPointColor);
                //System.out.println("Path Length: " + orderedPathVectors.size());
                prevMouseClick = null;
                break;
            case "Edit Components":

                int[] pathComponents = getPathCompInRadiusInch(mouseClick, 2);
                //drawPathBounds();
                System.out.println("Path Components Clicked: " + Arrays.toString(pathComponents));

                if (pathComponents.length != 0) {

                    editSelectCompLabel.setText("Selected Component: " + pathComponents[pathComponents.length - 1]);
                }

                int[] pathPoints = getPointInRadiusInch(mouseClick, 2);
                System.out.println("Path Points Clicked: " + Arrays.toString(pathPoints));

                if (pathPoints.length != 0) {

                    editSelectPointLabel.setText("Selected Point: " + pathPoints[pathPoints.length - 1]);
                }

                prevMouseClick = null;
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
                updateInitPosFields();

                resetPathSpec();

                if (drawRobot) {

                    drawRobotInch(getCurrentPathPos());
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

    private void resetField() {

        fieldGraphics.drawImage(field, 0, 0);
    }

    private void resetRobotDisplay() {

        robotGraphics.clearRect(0, 0, robotDisplay.getWidth(), robotDisplay.getHeight());
    }

    public void resetPathSpec() {

        resetField();
        redrawPath(pathColors.toArray(new Color[0]));

        if (drawPoint) {

            redrawPoints(pointColors.toArray(new Color[0]));
        }

        if (snapToGrid) {

            drawGrid(fieldDisplay, 12, 12, Color.BLACK, new double[] {15, 15, 15, 15});
        }
    }

    public boolean isPathInField() {

        return isPathInField(new Vector2D(0, 0));
    }

    public boolean isPathinField(double reflectionAngle) {

        ArrayList<Vector2D> pathPoints = generatePathPoints();
        boolean inField = true;

        for (int p = 0; p < pathPoints.size() && inField; p++) {

            Vector2D currentPoint = reflectPoint(pathPoints.get(p), reflectionAngle);

            inField = fieldBounds.contains(currentPoint.toPoint());
        }

        return inField;
    }

    public boolean isPathInField(Vector2D pivot, double angle) {

        ArrayList<Vector2D> pathPoints = generatePathPoints();
        boolean inField = true;

        for (int v = 0; v < pathPoints.size() && inField; v++) {

            Vector2D currentPoint = pathPoints.get(v);
            currentPoint.sub(pivot);
            currentPoint.rotate(angle);
            currentPoint.add(pivot);

            inField = fieldBounds.contains(currentPoint.toPoint());
        }

        return inField;
    }

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

    private void drawRobotInch(Vector2D fieldPos) { // TODO: maybe make the robot rotate to the orientation of the vector when you create it?

        resetRobotDisplay();
        fieldPos = convertFieldToCanvas(fieldPos);
        fieldPos.sub(new Vector2D(robot.getWidth() / 2.0, robot.getHeight() / 2.0));

        robotGraphics.drawImage(robot, fieldPos.getComponent(0), fieldPos.getComponent(1));
    }

    private void redrawPath() {

        redrawPath(new Color[0]);
    }

    private void redrawPath(Color pathColor) {

        redrawPath(Collections.nCopies(orderedPathVectors.size(), pathColor).toArray(new Color[0]));
    }

    private void redrawPath(Color[] pathColors) {

        Vector2D currentPos = new Vector2D(initialPos);

        drawPointField(fieldDisplay, initialPos, pointColors.get(0), 6);

//        if (pathColors.length >= orderedPathVectors.size()) {
//
//            for (int v = 0; v < orderedPathVectors.size(); v++) {
//
//                drawVectorInch(fieldDisplay, currentPos, Vector2D.add(currentPos, orderedPathVectors.get(v)), pathColors[v]);
//                currentPos.add(orderedPathVectors.get(v));
//            }
//        } else { // if the list is less than the length is defaults to LAWNGREEN for the rest of the path components
//
//
//        }

        for (int v = 0; v < orderedPathVectors.size(); v++) {

            Color pathColor = Color.LAWNGREEN;

            if (v < pathColors.length) {

                pathColor = pathColors[v];
            }

            drawVectorField(fieldDisplay, currentPos, Vector2D.add(currentPos, orderedPathVectors.get(v)), pathColor);
            currentPos.add(orderedPathVectors.get(v));
        }
    }

    private void drawVectorField(Canvas c, Vector2D initPoint, Vector2D termPoint, Color arrowColor) {

        initPoint = convertFieldToCanvas(initPoint);
        termPoint = convertFieldToCanvas(termPoint);

        drawVector(c, initPoint, termPoint, arrowColor);
    }

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

            drawPointField(fieldDisplay, initPoint, pointColor, 6);

            if (p < orderedPathVectors.size()) {

                initPoint.add(orderedPathVectors.get(p));
            }
        }
    }

    private void drawPointField(Canvas c, Vector2D point, Color pointColor, int pointDiameter) {

        point = convertFieldToCanvas(point);

        drawPoint(c, point, pointColor, pointDiameter);
    }

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

    private void drawImageInch(Canvas c, Vector2D cornerPos, Image image) {

        cornerPos = convertFieldToCanvas(cornerPos);
        drawImage(c, cornerPos, image);
    }

    private void drawImage(Canvas c, Vector2D cornerPos, Image image) {

        if (c.contains(cornerPos.toPoint())) {

            GraphicsContext g = c.getGraphicsContext2D();

            g.drawImage(image, cornerPos.getComponent(0), cornerPos.getComponent(1));
        }
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

    /*---------- Testers ----------*/

    private void vectorDrawTester() {

        drawVectorField(fieldDisplay, new Vector2D(0, 0), new Vector2D(36, 36), Color.LAWNGREEN);
        drawVectorField(fieldDisplay, new Vector2D(36, 36), new Vector2D(36, -36), Color.LAWNGREEN);

        for (int theta = 0; theta < 360; theta += 12) {

            drawVectorField(fieldDisplay, new Vector2D(0, 0), new Vector2D(theta, 36, false), Color.LAWNGREEN);
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

        ArrayList<Vector2D> newPath = new ArrayList<>();
        newPath.addAll(Arrays.asList(path));

        ArrayList<Vector2D> pathPoints = generatePathPoints(initialPosition, newPath);
        System.out.println(pathPoints);

        for (int p = 0; p < pathPoints.size(); p++) {

            Vector2D currentPoint = reflectPoint(pathPoints.get(p), 90);
            System.out.println(p + ": " + currentPoint);
        }
    }

    public static void main(String[] args) {

        reflectionTester();
    }
}
