package G12.main;

import G12.main.entities.PlayerType;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class OurController{


    private App parentController;
    private Stage stage;

    public void setParentController(App gui) {
        this.parentController = gui;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void updateView(String msg, PlayerType playerID) {
        parentController.chatBox.getItems().add(playerID + ": " + msg);
        parentController.chatBox.scrollTo(parentController.chatBox.getItems().size()-1);
    }

    public void close() {
        stage.close();
    }

}