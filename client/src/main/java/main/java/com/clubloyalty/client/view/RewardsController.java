package main.java.com.clubloyalty.client.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import main.java.com.clubloyalty.client.net.ApiClient;
import main.java.com.clubloyalty.client.util.Alerts;

import java.util.Map;

public class RewardsController {
    private final ApiClient api = new ApiClient();
    @FXML
    private ListView<String> rewardsList;
    private java.util.List<Map<String, Object>> rewards;
    private Map<String, Object> me;

    @FXML
    public void initialize() {
        try {
            me = api.me();
            rewards = api.rewards();
            rewardsList.getItems().clear();
            for (var r : rewards) {
                rewardsList.getItems().add(r.get("id") + ": " + r.get("title") + " (" + r.get("cost") + ")");
            }
        } catch (Exception e) {
            Alerts.error("Rewards", e.getMessage());
        }
    }

    @FXML
    public void onRedeem() {
        int idx = rewardsList.getSelectionModel().getSelectedIndex();
        if (idx < 0) {
            Alerts.info("Redeem", "Пожалуйста, сначала выберите награду");
            return;
        }
        var r = rewards.get(idx);
        try {
            long rid = ((Number) r.get("id")).longValue();
            long mid = ((Number) me.get("id")).longValue();
            var tx = api.redeem(rid, mid);
            Alerts.info("Redeem", "Успешно. Новый баланс: " + tx.get("balance"));
            initialize();
        } catch (Exception e) {
            Alerts.error("Redeem", e.getMessage());
        }
    }

    @FXML
    public void back() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ui/dashboard.fxml"));
            rewardsList.getScene().setRoot(root);
        } catch (Exception e) {
            Alerts.error("UI", e.getMessage());
        }
    }
}
