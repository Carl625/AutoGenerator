package AutoPathGenerator;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;

public class ForkDialog<T> extends Dialog<T> {

    public static enum dialogType {
        createFork,
        modifyFork,
        deleteFork
    }

    private dialogType type;

    public void initialize(dialogType type) {

        this.type = type;

        visualSetup();
        behaviorSetup();
    }

    private void visualSetup() {

        this.getDialogPane();
    }

    private void behaviorSetup() {

        
    }
}
