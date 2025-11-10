package main.java.com.clubloyalty.client.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import main.java.com.clubloyalty.client.net.ApiClient;
import main.java.com.clubloyalty.client.util.Alerts;

public class RegisterController {
  private final ApiClient api = new ApiClient();

  @FXML private TextField username;
  @FXML private PasswordField password;
  @FXML private TextField fullName;
  @FXML private TextField phone;

  @FXML
  public void onRegister() {
    try {
      String usernameVal = username.getText().trim();
      String passwordVal = password.getText();
      String fullNameVal = fullName.getText().trim();
      String phoneVal = phone.getText().trim();

      clearErrors();

      boolean invalid = false;
      if (usernameVal.isEmpty()) {
        markError(username);
        invalid = true;
      }
      if (passwordVal.isBlank()) {
        markError(password);
        invalid = true;
      }
      if (fullNameVal.isEmpty()) {
        markError(fullName);
        invalid = true;
      }
      if (phoneVal.isEmpty()) {
        markError(phone);
        invalid = true;
      } else if (!phoneVal.matches("\\d+")) {
        markError(phone);
        Alerts.error("Registration", "Phone number must contain digits only");
        return;
      } else if (phoneVal.length() < 7) {
        markError(phone);
        Alerts.error("Registration", "Phone number must contain at least 7 digits");
        return;
      }

      if (invalid) {
        Alerts.error("Registration", "Please fill in all highlighted fields");
        return;
      }

      api.register(usernameVal, passwordVal, fullNameVal, phoneVal);
      Alerts.info("Registration", "Registration successful! You can now sign in.");
      Parent root = FXMLLoader.load(getClass().getResource("/ui/login.fxml"));
      username.getScene().setRoot(root);
    } catch (Exception e) {
      Alerts.error("Registration", e.getMessage());
    }
  }

  @FXML
  public void back() {
    try {
      Parent root = FXMLLoader.load(getClass().getResource("/ui/login.fxml"));
      fullName.getScene().setRoot(root);
    } catch (Exception e) {
      Alerts.error("UI", e.getMessage());
    }
  }

  private void clearErrors() {
    removeError(username);
    removeError(password);
    removeError(fullName);
    removeError(phone);
  }

  private void markError(Control control) {
    if (!control.getStyleClass().contains("input-error")) {
      control.getStyleClass().add("input-error");
    }
  }

  private void removeError(Control control) {
    control.getStyleClass().remove("input-error");
  }
}
