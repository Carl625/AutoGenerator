package AutoPathGenerator;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class WindowController {

    //display variables
    public Canvas fieldDisplay;
    public GraphicsContext graphics;
    public Image field;
    public ChoiceBox defStone;
    public ChoiceBox alliance;

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

        defStone.getItems().clear();
        defStone.getItems().add("Left");
        defStone.getItems().add("Middle");
        defStone.getItems().add("Right");
        defStone.getSelectionModel().select(1);

        alliance.getItems().clear();
        alliance.getItems().add("Red");
        alliance.getItems().add("Blue");
        alliance.getSelectionModel().select(0);
    }

    private void initAutoMisc() {


    }

    private void initAutoGen() {


    }

    public void fieldClicked(MouseEvent mouseEvent) {
    }

    private void resetField() {

        graphics.drawImage(field, 0, 0);
    }
}
