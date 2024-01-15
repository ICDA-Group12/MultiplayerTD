package G12.main;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ChatApp extends Application {

    private ListView<String> chatListView;
    private TextField messageInput;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX Chat App");

        // Create UI components
        chatListView = new ListView<>();
        messageInput = new TextField();
        Button sendButton = new Button("Send");

        // Handle the send button action
        sendButton.setOnAction(e -> sendMessage());

        // Create layout
        HBox inputBox = new HBox(messageInput, sendButton);
        inputBox.setAlignment(Pos.CENTER);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(chatListView);
        borderPane.setBottom(inputBox);

        // Set up the scene
        Scene scene = new Scene(borderPane, 400, 300);
        primaryStage.setScene(scene);

        // Show the stage
        primaryStage.show();
    }

    private void sendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            chatListView.getItems().add("You: " + message);
            messageInput.clear();
        }
    }
}