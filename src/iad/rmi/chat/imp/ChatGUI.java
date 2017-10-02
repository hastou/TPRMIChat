package iad.rmi.chat.imp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;

public class ChatGUI extends Application {

    private ChatLayout chatLayout;
    private FXMLLoader loader;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Platform.setImplicitExit(false);
        loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("ChatGUI.fxml"));
        final BorderPane root;
        try {
            root = (BorderPane) loader.load();
            chatLayout = loader.getController();
            // Création de la scène.
            final Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);

            primaryStage.setTitle("Chat");
            primaryStage.setScene(scene);

            primaryStage.setOnCloseRequest(e -> Platform.exit());
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        chatLayout.stop();
        System.exit(0);
    }
}
