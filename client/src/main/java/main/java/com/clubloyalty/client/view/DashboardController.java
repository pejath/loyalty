package main.java.com.clubloyalty.client.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import main.java.com.clubloyalty.client.net.ApiClient;
import main.java.com.clubloyalty.client.net.AuthSession;
import main.java.com.clubloyalty.client.util.Alerts;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {
    private final ApiClient api = new ApiClient();
    private final DateTimeFormatter dtf =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    @FXML
    private Label nameLbl;
    @FXML
    private Label tierLbl;
    @FXML
    private Label pointsLbl;
    @FXML
    private ListView<String> rewardsList;
    @FXML
    private Button adminBtn;
    @FXML
    private DatePicker historyFrom;
    @FXML
    private DatePicker historyTo;
    @FXML
    private ComboBox<String> historyType;
    @FXML
    private ListView<String> historyList;
    @FXML
    private ListView<String> notificationsList;
    private Map<String, Object> me;
    private List<Map<String, Object>> rewards;
    private List<Map<String, Object>> history;
    private List<Map<String, Object>> notifications;

    @FXML
    public void initialize() {
        if (historyType != null) {
            historyType.getItems().setAll("EARN", "REDEEM", "ADJUST");
            historyType.setPromptText("All");
        }

        try {
            loadProfile();
            loadRewards();
            refreshHistory();
            refreshNotifications();
            boolean admin = AuthSession.hasRole("ROLE_ADMIN") || AuthSession.hasRole("ROLE_STAFF");
            if (adminBtn != null) {
                adminBtn.setVisible(admin);
                adminBtn.setManaged(admin);
            }
        } catch (Exception e) {
            Alerts.error("Profile", e.getMessage());
        }
    }

    private void loadProfile() throws Exception {
        me = api.me();
        nameLbl.setText(String.valueOf(me.getOrDefault("fullName", "-")));
        tierLbl.setText(String.valueOf(me.getOrDefault("tier", "-")));
        pointsLbl.setText(String.valueOf(me.getOrDefault("points", "0")));
    }

    private void loadRewards() throws Exception {
        rewards = api.rewards();
        rewardsList.getItems().clear();
        for (var r : rewards) {
            String item = r.get("id") + ": " + r.get("title") + " - " + r.get("cost") + " pts";
            rewardsList.getItems().add(item);
        }
    }

    private void refreshHistory() throws Exception {
        if (historyList == null) {
            return;
        }
        String type = historyType.getValue();
        String fromIso = toIso(historyFrom.getValue(), true);
        String toIso = toIso(historyTo.getValue(), false);
        var page = api.transactions(0, 100, type, fromIso, toIso);
        history = (List<Map<String, Object>>) page.getOrDefault("items", List.of());
        historyList
                .getItems()
                .setAll(history.stream().map(this::formatHistory).collect(Collectors.toList()));
    }

    private void refreshNotifications() throws Exception {
        if (notificationsList == null) {
            return;
        }
        notifications = api.notifications();
        notificationsList
                .getItems()
                .setAll(notifications.stream().map(this::formatNotification).collect(Collectors.toList()));
    }

    private String formatHistory(Map<String, Object> map) {
        String time = formatInstant(map.get("createdAt"));
        long amount = map.get("amount") == null ? 0 : ((Number) map.get("amount")).longValue();
        String type = String.valueOf(map.get("type"));
        String reward = map.get("rewardTitle") == null ? "" : " -> " + map.get("rewardTitle");
        return time + " | " + String.format("%+d", amount) + " | " + type + reward;
    }

    private String formatNotification(Map<String, Object> map) {
        String time = formatInstant(map.get("createdAt"));
        String title = String.valueOf(map.get("title"));
        String message = String.valueOf(map.get("message"));
        boolean read = Boolean.TRUE.equals(map.get("read"));
        return (read ? "" : "[new] ") + time + " | " + title + ": " + message;
    }

    private String toIso(LocalDate date, boolean startOfDay) {
        if (date == null) {
            return null;
        }
        var zone = ZoneId.systemDefault();
        if (startOfDay) {
            return date.atStartOfDay(zone).toInstant().toString();
        } else {
            return date.plusDays(1).atStartOfDay(zone).minusSeconds(1).toInstant().toString();
        }
    }

    private String formatInstant(Object value) {
        if (value == null) {
            return "";
        }
        try {
            return dtf.format(Instant.parse(String.valueOf(value)));
        } catch (Exception ex) {
            return String.valueOf(value);
        }
    }

    @FXML
    public void onHistoryFilter() {
        try {
            refreshHistory();
        } catch (Exception e) {
            Alerts.error("History", e.getMessage());
        }
    }

    @FXML
    public void onHistoryExport() {
        if (history == null || history.isEmpty()) {
            Alerts.info("History", "Nothing to export");
            return;
        }
        String joined = history.stream().map(this::formatHistory).collect(Collectors.joining("\n"));
        Alerts.info("History", joined);
    }

    @FXML
    public void onNotificationsRefresh() {
        try {
            refreshNotifications();
        } catch (Exception e) {
            Alerts.error("Notifications", e.getMessage());
        }
    }

    @FXML
    public void onRedeem() {
        try {
            int idx = rewardsList.getSelectionModel().getSelectedIndex();
            if (idx < 0) {
                Alerts.info("Rewards", "Select a reward first");
                return;
            }
            Map<String, Object> reward = rewards.get(idx);
            long rid = ((Number) reward.get("id")).longValue();
            long mid = ((Number) me.get("id")).longValue();
            Map<String, Object> tx = api.redeem(rid, mid);
            Alerts.info("Rewards", "Balance updated: " + tx.get("balance"));
            loadProfile();
            loadRewards();
            refreshHistory();
        } catch (Exception e) {
            Alerts.error("Rewards", e.getMessage());
        }
    }

    @FXML
    public void onRefresh() {
        try {
            loadProfile();
            loadRewards();
            refreshHistory();
            refreshNotifications();
        } catch (Exception e) {
            Alerts.error("Refresh", e.getMessage());
        }
    }

    @FXML
    public void onAdmin() {
        if (!AuthSession.hasRole("ROLE_ADMIN") && !AuthSession.hasRole("ROLE_STAFF")) {
            Alerts.info("Admin", "You do not have permissions");
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ui/admin-dashboard.fxml"));
            nameLbl.getScene().setRoot(root);
        } catch (Exception e) {
            Alerts.error("Admin", e.getMessage());
        }
    }

    @FXML
    public void onLogout() {
        try {
            AuthSession.clear();
            Parent root = FXMLLoader.load(getClass().getResource("/ui/login.fxml"));
            nameLbl.getScene().setRoot(root);
        } catch (Exception e) {
            Alerts.error("Logout", e.getMessage());
        }
    }
}
