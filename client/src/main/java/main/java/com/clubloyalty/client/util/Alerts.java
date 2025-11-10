package main.java.com.clubloyalty.client.util;

import javafx.scene.control.Alert;

public class Alerts {
    public static void info(String title, String message) {
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void error(String title, String message) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        String content = (message == null || message.isBlank())
                ? "Something went wrong. Please try again."
                : message;
        alert.setContentText(content);
        alert.showAndWait();
    }
}
