package G12.main;

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

public class MainMenu extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load the FXML file
            Parent root = FXMLLoader.load(getClass().getResource("/MainMenu.fxml"));


            // Create the Scene using the loaded FXML
            Scene scene = new Scene(root);

            // Set the scene to the stage and display it
            primaryStage.setScene(scene);
            primaryStage.setTitle("My JavaFX Application");
            primaryStage.show();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
