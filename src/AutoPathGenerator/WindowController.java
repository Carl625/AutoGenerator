package AutoPathGenerator;

import Resources.Vector2D;
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

    public void initialize() {

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
            System.out.println("new Default Stone Dropdown value: " + newlySelected);
        }
    }

    private void allianceDropdownChanged() {

        int selectIndex = allianceDropdown.getSelectionModel().getSelectedIndex();

        if (selectIndex != -1) {

            String newlySelected = (String) allianceDropdown.getItems().get(selectIndex);
            System.out.println("new Alliance Dropdown value: " + newlySelected);
        }
    }

    private void grabDropdownChanged() {

        int selectIndex = initGrabDropdown.getSelectionModel().getSelectedIndex();

        if (selectIndex != -1) {

            String newlySelected = (String) initGrabDropdown.getItems().get(selectIndex);
            System.out.println("new Grab Dropdown value: " + newlySelected);
        }
    }

    private void armDropdownChanged() {

        int selectIndex = initArmDropdown.getSelectionModel().getSelectedIndex();

        if (selectIndex != -1) {

            String newlySelected = (String) initArmDropdown.getItems().get(selectIndex);
            System.out.println("new Arm Dropdown value: " + newlySelected);

            if (newlySelected.equals("Specific")) {

                specArmPosField.setDisable(false);
            } else {

                specArmPosField.setDisable(true);
            }
        }
    }

    // data manipulation/internal processing

    public void fieldClicked(MouseEvent mouseEvent) {
    }

    private void resetField() {

        graphics.drawImage(field, 0, 0);
    }

    private void drawVector(Canvas c, Vector2D initPoint, Vector2D termPoint, Color arrowColor) {

        if (c.contains(initPoint.toPoint()) && c.contains(termPoint.toPoint())) {

            GraphicsContext g = c.getGraphicsContext2D();

            double xDir = (termPoint.getComponent(0) - initPoint.getComponent(0)) / Math.abs(termPoint.getComponent(0) - initPoint.getComponent(0));
            double yDir = (termPoint.getComponent(1) - initPoint.getComponent(1)) / Math.abs(termPoint.getComponent(1) - initPoint.getComponent(1));
            double angle = Vector2D.sub(termPoint, initPoint).getTheta();

            double[] xPoint = {(termPoint.getComponent(0) - ((xDir * 50) / Math.cos(angle))), termPoint.getComponent(0), termPoint.getComponent(0)}; // TODO: finish these so the edges of the triangles are pointing in the direction of the vector
            double[] yPoint = {termPoint.getComponent(1), termPoint.getComponent(1), termPoint.getComponent(1) - (yDir * 50)};

            g.strokeLine(initPoint.getComponent(0), initPoint.getComponent(1), termPoint.getComponent(0), termPoint.getComponent(1));
            //g.fillPolygon(xPoint, yPoint, 3);
        }
    }
}
