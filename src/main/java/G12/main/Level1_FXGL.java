package G12.main;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

public class Level1_FXGL extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        // Configure your game settings here
    }
    @Override
    protected void initUI() {
        super.initUI();
        try {
            // Load the FXML file
            Parent root = FXMLLoader.load(getClass().getResource("/Level1.fxml"));

            // Add it to the FXGL game scene
            FXGL.getGameScene().addUINode(root);
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
