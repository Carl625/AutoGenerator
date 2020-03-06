package AutoPathGenerator;

import Resources.Vector2D;
import com.sun.prism.paint.Paint;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

public class WindowController {

    //display variables
    public Canvas fieldDisplay;
    public GraphicsContext graphics;
    public Image field;

    //Checkboxes
    public CheckBox visionInitCheckBox;
    public CheckBox scnBeforeStartCheckBox;
    public CheckBox intakeDeployCheckBox;
    public CheckBox IMUInitCheckBox;
    public CheckBox skystonePosGenCheckBox;

    // drodowns
    public ChoiceBox defStoneDropdown;
    public ChoiceBox allianceDropdown;
    public ChoiceBox initGrabDropdown;
    public ChoiceBox initArmDropdown;

    // text/numerical fields
    public TextField autoNameField;
    public TextField specArmPosField;
    public TextField startDragPosField;

    //internal variables
    private ArrayList<Vector2D> orderedPathVectors;
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

        vectorDrawTester();
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
    }

    private void initAutoInit() {

        defStoneDropdown.getItems().clear();
        defStoneDropdown.getItems().add("Left");
        defStoneDropdown.getItems().add("Middle");
        defStoneDropdown.getItems().add("Right");
        defStoneDropdown.getSelectionModel().select(1);
        defStoneDropdown.setOnAction(new EventHandler() {
            public void handle(Event event) {defStoneDropdownChanged();}
        });

        allianceDropdown.getItems().clear();
        allianceDropdown.getItems().add("Red");
        allianceDropdown.getItems().add("Blue");
        allianceDropdown.getSelectionModel().select(0);
        allianceDropdown.setOnAction(new EventHandler() {
            public void handle(Event event) {allianceDropdownChanged();}
        });

        initGrabDropdown.getItems().clear();
        initGrabDropdown.getItems().add("Grab");
        initGrabDropdown.getItems().add("Release");
        initGrabDropdown.getSelectionModel().select(1);
        initGrabDropdown.setOnAction(new EventHandler() {
            public void handle(Event event) {grabDropdownChanged();}
        });

        initArmDropdown.getItems().clear();
        initArmDropdown.getItems().add("In");
        initArmDropdown.getItems().add("Standby");
        initArmDropdown.getItems().add("Intermediate 2");
        initArmDropdown.getItems().add("Drop");
        initArmDropdown.getItems().add("Specific");
        initArmDropdown.getSelectionModel().select(1);
        initArmDropdown.setOnAction(new EventHandler() {
            public void handle(Event event) {armDropdownChanged();}
        });

        autoNameField.setText("Autonomous X Red");
        specArmPosField.setText("0.22"); // this is the left servos standby position
        specArmPosField.setDisable(true);
    }

    private void initAutoMisc() {


    }

    private void initAutoGen() {


    }

    // info listeners

    private void defStoneDropdownChanged() {

        int selectIndex = defStoneDropdown.getSelectionModel().getSelectedIndex();

        if (selectIndex != -1) {

            String newlySelected = (String) defStoneDropdown.getItems().get(selectIndex);
            //System.out.println("new Default Stone Dropdown value: " + newlySelected);
        }
    }

    private void allianceDropdownChanged() {

        int selectIndex = allianceDropdown.getSelectionModel().getSelectedIndex();

        if (selectIndex != -1) {

            String newlySelected = (String) allianceDropdown.getItems().get(selectIndex);
            //System.out.println("new Alliance Dropdown value: " + newlySelected);
        }
    }

    private void grabDropdownChanged() {

        int selectIndex = initGrabDropdown.getSelectionModel().getSelectedIndex();

        if (selectIndex != -1) {

            String newlySelected = (String) initGrabDropdown.getItems().get(selectIndex);
            //System.out.println("new Grab Dropdown value: " + newlySelected);
        }
    }

    private void armDropdownChanged() {

        int selectIndex = initArmDropdown.getSelectionModel().getSelectedIndex();

        if (selectIndex != -1) {

            String newlySelected = (String) initArmDropdown.getItems().get(selectIndex);
            //System.out.println("new Arm Dropdown value: " + newlySelected);

            if (newlySelected.equals("Specific")) {

                specArmPosField.setDisable(false);
            } else {

                specArmPosField.setDisable(true);
            }
        }
    }

    // data manipulation/internal processing

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


    }

    private void resetField() {

        graphics.drawImage(field, 0, 0);
    }

    private void drawVectorInch(Canvas c, Vector2D initPoint, Vector2D termPoint, Color arrowColor) {

        drawVector(c, Vector2D.scaleComp(initPoint, pixelsPerInch), Vector2D.scaleComp(termPoint, pixelsPerInch), arrowColor);
    }

    private void drawVector(Canvas c, Vector2D initPoint, Vector2D termPoint, Color arrowColor) {

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
            System.out.println(pathVector);
            double angle = pathVector.getTheta();
            System.out.println(Math.toDegrees(angle));
            double triangleSideMagnitude = 10;

            double[] xPoints = new double[3]; //TODO: finish these so the edges of the triangles are pointing in the direction of the vector
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
//            System.out.println(Arrays.toString(xPoints));
//            System.out.println(Arrays.toString(yPoints));
            g.fillPolygon(xPoints, yPoints, 3);
        }
    }

    private void vectorDrawTester() {

        drawVectorInch(fieldDisplay, new Vector2D(0, 0), new Vector2D(36, 36), Color.LAWNGREEN);
        drawVectorInch(fieldDisplay, new Vector2D(36, 36), new Vector2D(36, -36), Color.LAWNGREEN);
    }
}
