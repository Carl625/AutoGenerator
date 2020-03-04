package AutoPathGenerator;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class WindowController {

    //display variables
    public Canvas fieldDisplay;
    public GraphicsContext graphics;
    public Image field;

    public void initialize() {

        try {
            FileInputStream f = new FileInputStream("../AutoPathGenerator/src/Resources/Images/topviewfield.png");
            field = new Image(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        graphics = fieldDisplay.getGraphicsContext2D();
        resetField();
    }

    public void fieldClicked(MouseEvent mouseEvent) {
    }

    private void resetField() {

        graphics.drawImage(field, 0, 0);
    }
}
