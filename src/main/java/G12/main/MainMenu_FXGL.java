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

public class MainMenu_FXGL extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        // Configure game settings here
        settings.setTitle("Fortify & Fight");
        settings.setVersion("0.1");
        settings.setWidth(800);
        settings.setHeight(600);
    }
    @Override
    protected void initUI() {
        super.initUI();
        loadScene("MainMenu.fxml");
    }

    //Method to load a scene from an FXML file.
    private void loadScene(String fxmlFileName) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/" + fxmlFileName));
            Parent root = fxmlLoader.load();
            FXGL.getGameScene().clearUINodes();
            FXGL.getGameScene().addUINode(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
