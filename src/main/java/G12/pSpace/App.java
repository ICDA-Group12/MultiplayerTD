package G12.pSpace;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Main class for JavaFX application.
 *
 * @version 1.0
 */

public class App extends Application {

    private Pane gameRoot;

    @Override
    public void start(Stage stage) throws Exception {
        _initIU(stage);
    }

    private void _initIU(Stage primaryStage) {
        gameRoot = new Pane();
        Scene scene = new Scene(gameRoot, 800, 600);

        // TODO: Initialize game elements here (towers, enemies, etc.)

        primaryStage.setTitle("Tower Defense Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void _addGameElement(Node element) {
        gameRoot.getChildren().add(element);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
