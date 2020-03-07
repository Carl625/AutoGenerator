package AutoPathGenerator;

import Resources.Vector2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;

public class WindowController {

    /*---------- Path Specification Variables ----------*/

    //display variables
    public Canvas fieldDisplay;
    private GraphicsContext graphics;
    private Image field;

    //Path editing and definition variables
    public ChoiceBox<String> pathModeDropdown;
    private String pathMode;

    public Label pathColorSetText;
    public ColorPicker currentPathColorPicker;
    private Color currentPathColor;

    public Label compSnapText;
    public ChoiceBox<String> pathComponentSnapDropdown;
    private String pathComponentSnap;

    //Point editing and definition variables

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
    private ArrayList<Vector2D> orderedPathVectors;
    private Vector2D initialPos;
    private double pixelsPerInch = (700.0 / (24.0 * 6.0)); // 700x700 pixel image of a 12ft x 12ft field

    public void initialize() {

        // init graphics
        initTabs();

        //init internal variables
        orderedPathVectors = new ArrayList<Vector2D>();
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        graphics = fieldDisplay.getGraphicsContext2D();
        resetField();

        initialPos = new Vector2D(63, -36);

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

        //path editing

        //point editing
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
        autoNameField.setOnAction(event -> autoNameChanged());
        specArmPosField.setOnAction(event -> specArmPosChanged());
        startDragPosField.setOnAction(event -> startDragPosChanged());
        initXField.setOnAction(event -> initPosChanged());
        initYField.setOnAction(event -> initPosChanged());

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
            setPathRTVisible(false);

            switch (pathMode) {
                case "Design":

                    setPathDesignVisible(true);
                    System.out.println(true);
                    break;
                case "Edit Components":

                    setPathEditVisible(true);
//                    resetField();
//                    redrawPath(Color.RED);
                    break;
                case "Rigid Transform":

                    setPathRTVisible(true);
                    break;
            }
        }
    }

    private void pathColorChanged() {

        currentPathColor = currentPathColorPicker.getValue();
    }

    private void pathComponentSnapChanged() {

        int selectIndex = pathComponentSnapDropdown.getSelectionModel().getSelectedIndex();

        if (selectIndex != -1) {

            pathComponentSnap = pathComponentSnapDropdown.getItems().get(selectIndex);
        }
    }

    private void defStoneDropdownChanged() {

        int selectIndex = defStoneDropdown.getSelectionModel().getSelectedIndex();

        if (selectIndex != -1) {

            String newlySelected = defStoneDropdown.getItems().get(selectIndex);
            //System.out.println("new Default Stone Dropdown value: " + newlySelected);
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
                    initXField.setText(initialPos.getComponent(0).toString());
                    initYField.setText(initialPos.getComponent(1).toString());
                }
            } else if (newlySelected.equals("Blue")) {

                if (initialPos.equals(new Vector2D(63, -36))) {

                    initialPos = new Vector2D(-63, -36);
                    initXField.setText(initialPos.getComponent(0).toString());
                    initYField.setText(initialPos.getComponent(1).toString());
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

    }

    /*---------- JavaFX Convenience Methods ----------*/
    public void setVisible(boolean show, Node component) {

        component.setVisible(show);
        component.managedProperty().bind(component.visibleProperty());
    }

    /*---------- Data Manipulation/Internal Processing ----------*/

    public double normalizeAngle(double degrees) {

        while (degrees < 0) {

            degrees += 360;
        }

        while (degrees > 360) {

            degrees -= 360;
        }

        return degrees;
    }

    public void fieldClicked(MouseEvent mouseEvent) {
        Vector2D mouseClick = new Vector2D(mouseEvent.getSceneX() - fieldDisplay.getLayoutX(), mouseEvent.getSceneY() - fieldDisplay.getLayoutY());
        Vector2D mouseCorrection = new Vector2D(0, -28);
        mouseClick.add(mouseCorrection);

        double xOffset = fieldDisplay.getWidth() / 2.0;
        double yOffset = fieldDisplay.getHeight() / 2.0;
        Vector2D centerOffset = new Vector2D(xOffset, yOffset);
        mouseClick.sub(centerOffset);
        mouseClick.flipDimension(1);
        System.out.println("Mouse pressed at: " + mouseClick);

        Vector2D prevPos = getCurrentPathPos();

        if (pathComponentSnap.equals("Vertical")) {

            mouseClick.setComponent(0, prevPos.getComponent(0) * pixelsPerInch);
        } else if (pathComponentSnap.equals("Horizontal")) {

            mouseClick.setComponent(1, prevPos.getComponent(1) * pixelsPerInch);
        }

        orderedPathVectors.add(Vector2D.sub(Vector2D.scaleComp(mouseClick, 1.0 / pixelsPerInch), prevPos));
        drawVector(fieldDisplay, Vector2D.scaleComp(prevPos, pixelsPerInch), mouseClick, currentPathColor);
        System.out.println("Path Length: " + orderedPathVectors.size());
    }

    private void resetField() {

        graphics.drawImage(field, 0, 0);
    }

    private void redrawPath() {

        redrawPath(new Color[0]);
    }

    private void redrawPath(Color pathColor) {

        redrawPath(Collections.nCopies(orderedPathVectors.size(), pathColor).toArray(new Color[0]));
    }

    private void redrawPath(Color[] pathColors) {

        Vector2D currentPos = new Vector2D(initialPos);

        if (pathColors.length >= orderedPathVectors.size()) {

            for (int v = 0; v < orderedPathVectors.size(); v++) {

                drawVectorInch(fieldDisplay, currentPos, Vector2D.add(currentPos, orderedPathVectors.get(v)), pathColors[v]);
                currentPos.add(orderedPathVectors.get(v));
            }
        } else { // if the list is less than the length is defaults to LAWNGREEN for the rest of the path components

            for (int v = 0; v < orderedPathVectors.size(); v++) {

                Color pathColor = Color.LAWNGREEN;

                if (v < pathColors.length) {

                    pathColor = pathColors[v];
                }

                drawVectorInch(fieldDisplay, currentPos, Vector2D.add(currentPos, orderedPathVectors.get(v)), pathColor);
                currentPos.add(orderedPathVectors.get(v));
            }
        }
    }

    public Vector2D getCurrentPathPos() {

        Vector2D currentPos = new Vector2D(initialPos);

        for (int v = 0; v < orderedPathVectors.size(); v++) {

            currentPos.add(orderedPathVectors.get(v));
        }

        return currentPos;
    }

    private void drawVectorInch(Canvas c, Vector2D initPoint, Vector2D termPoint, Color arrowColor) {

        drawVector(c, Vector2D.scaleComp(initPoint, pixelsPerInch), Vector2D.scaleComp(termPoint, pixelsPerInch), arrowColor);
    }

    private void drawVector(Canvas c, Vector2D initPoint, Vector2D termPoint, Color arrowColor) {

        initPoint.flipDimension(1);
        termPoint.flipDimension(1);
        double xOffset = c.getWidth() / 2.0;
        double yOffset = c.getHeight() / 2.0;
        Vector2D centerOffset = new Vector2D(xOffset, yOffset);
        initPoint.add(centerOffset);
        termPoint.add(centerOffset);

        if (c.contains(initPoint.toPoint()) && c.contains(termPoint.toPoint())) {

            GraphicsContext g = c.getGraphicsContext2D();
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
        }
    }

    private void setPathDesignVisible(boolean show) { // displays or hides the Nodes associated with designing the path (Selecting points, editing position, Defining Curves)

        setVisible(show, pathColorSetText);
        setVisible(show, currentPathColorPicker);
        setVisible(show, compSnapText);
        setVisible(show, pathComponentSnapDropdown);
    }

    private void setPathEditVisible(boolean show) { // displays or hides the Nodes associated with selecting path components and editing properties (Path type, Speed, Color, etc.)

    }

    private void setPathRTVisible(boolean show) { // displays or hides the Nodes associated with transforming the path rigidly (Rotation, Translation, Reflection)

    }

    /*---------- Testers ----------*/

    private void vectorDrawTester() {

        drawVectorInch(fieldDisplay, new Vector2D(0, 0), new Vector2D(36, 36), Color.LAWNGREEN);
        drawVectorInch(fieldDisplay, new Vector2D(36, 36), new Vector2D(36, -36), Color.LAWNGREEN);

        for (int theta = 0; theta < 360; theta += 12) {

            drawVectorInch(fieldDisplay, new Vector2D(0, 0), new Vector2D(theta, 36, false), Color.LAWNGREEN);
//            System.out.println("Loop theta: " + theta);
        }
        System.out.println("Finished!");
    }
}
