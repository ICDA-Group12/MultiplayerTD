package G12.main;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

/**
 * Main class for JavaFX application.
 *
 * @version 1.0
 */

public class App extends Application {

    public static void main(String[] args) throws InterruptedException {
        Space inbox = new SequentialSpace();
        inbox.put("Hello World!");
        Object[] tuple = inbox.get(new FormalField(String.class));
        System.out.println(tuple[0]);
        launch(args);
    }

    private int counter = 0;
    private Button button = new Button();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        this.button.setText("Im a counter! Click ME!!!");
        this.button.setOnAction(this::handleClick);
        StackPane root = new StackPane();
        root.getChildren().add(this.button);
        Scene scene = new Scene(root, 300, 250);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleClick(ActionEvent event) {
        this.counter++;
        this.button.setText("" + this.counter);
    }

    private void handleKey(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            this.counter++;
        } else if (event.getCode() == KeyCode.DOWN) {
            this.counter--;
        } else {
            return;
        }
        this.button.setText("" + this.counter);
    }

}