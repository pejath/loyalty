package main.java.com.clubloyalty.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.Properties;

public class MainApp extends Application {
    public static String SERVER_URL;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Properties props = new Properties();
        try (InputStream is = MainApp.class.getResourceAsStream("/app.properties")) {
            props.load(is);
        }
        SERVER_URL = props.getProperty("SERVER_URL", "http://localhost:8080");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        var theme = MainApp.class.getResource("/ui/theme.css");
        if (theme != null) {
            scene.getStylesheets().add(theme.toExternalForm());
        }
        stage.setTitle("Loyalty Client");
        stage.setScene(scene);
        stage.setMinWidth(1024);
        stage.setMinHeight(720);
        stage.show();
    }
}
