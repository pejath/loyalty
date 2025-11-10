package main.java.com.clubloyalty.client.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import main.java.com.clubloyalty.client.net.ApiClient;
import main.java.com.clubloyalty.client.net.AuthSession;
import main.java.com.clubloyalty.client.util.Alerts;

import java.util.List;
import java.util.Map;

public class LoginController {
  private final ApiClient api = new ApiClient();
  @FXML private TextField username;
  @FXML private PasswordField password;

  @FXML
  public void onLogin() {
    clearErrors();
    boolean invalid = false;
    if (username.getText() == null || username.getText().trim().isEmpty()) {
      markError(username);
      invalid = true;
    }
    if (password.getText() == null || password.getText().isBlank()) {
      markError(password);
      invalid = true;
    }
    if (invalid) {
      Alerts.error("Login", "Please fill in the highlighted fields");
      return;
    }

    try {
      String token = api.login(username.getText().trim(), password.getText());
      AuthSession.set(token);
      Map<String, Object> profile = api.profile();
      List<String> roles = (List<String>) profile.getOrDefault("roles", List.of());
      AuthSession.setProfile(String.valueOf(profile.get("username")), roles,
          profile.get("memberId") == null ? null : ((Number) profile.get("memberId")).longValue());

      String target = AuthSession.hasRole("ROLE_ADMIN") ? "/ui/admin-dashboard.fxml" : "/ui/dashboard.fxml";
      Parent root = FXMLLoader.load(getClass().getResource(target));
      username.getScene().setRoot(root);
    } catch (Exception e) {
      String message = e.getMessage();
      if (message == null || message.isBlank()) {
        message = e.toString();
      }
      Alerts.error("Login", message);
    }
  }

  @FXML
  public void openRegister() {
    try {
      Parent root = FXMLLoader.load(getClass().getResource("/ui/register.fxml"));
      username.getScene().setRoot(root);
    } catch (Exception e) {
      Alerts.error("UI", e.getMessage());
    }
  }

  private void clearErrors() {
    removeError(username);
    removeError(password);
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
