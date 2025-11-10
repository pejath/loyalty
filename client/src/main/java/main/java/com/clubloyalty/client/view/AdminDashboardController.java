package main.java.com.clubloyalty.client.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import main.java.com.clubloyalty.client.net.ApiClient;
import main.java.com.clubloyalty.client.net.AuthSession;
import main.java.com.clubloyalty.client.util.Alerts;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AdminDashboardController {
    private static final int TX_PAGE_SIZE = 50;
    private final ObservableList<Map<String, Object>> users = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> filteredUsers = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> members = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> rewards = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> transactions = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> promotions = FXCollections.observableArrayList();
    private final ApiClient api = new ApiClient();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());
    private final boolean isAdmin = AuthSession.hasRole("ROLE_ADMIN");
    private final boolean isStaff = AuthSession.hasRole("ROLE_STAFF");
    private final boolean canOperateMembers = isAdmin || isStaff;
    @FXML
    private Label welcomeLbl;
    @FXML
    private TextField userSearchField;
    @FXML
    private Tab usersTab;
    @FXML
    private Tab rewardsTab;
    @FXML
    private Tab promotionsTab;
    @FXML
    private ListView<Map<String, Object>> usersList;
    @FXML
    private ListView<Map<String, Object>> membersList;
    @FXML
    private ListView<Map<String, Object>> rewardsList;
    @FXML
    private ListView<Map<String, Object>> transactionsList;
    @FXML
    private ListView<Map<String, Object>> promotionsList;
    @FXML
    private Button promotionsCreateBtn;
    @FXML
    private ListView<String> reportTypeList;
    @FXML
    private ListView<String> reportRewardsList;
    @FXML
    private DatePicker reportFrom;
    @FXML
    private DatePicker reportTo;
    @FXML
    private Label reportRangeLbl;
    @FXML
    private Label reportMembersLbl;
    @FXML
    private Label reportActiveLbl;
    @FXML
    private Label reportTxLbl;
    @FXML
    private Label reportPointsLbl;
    @FXML
    private Label reportAvgLbl;
    @FXML
    private TextField txMemberFilter;
    @FXML
    private ComboBox<String> txType;
    @FXML
    private DatePicker txFrom;
    @FXML
    private DatePicker txTo;
    @FXML
    private Label txPageLabel;
    @FXML
    private Button txPrevBtn;
    @FXML
    private Button txNextBtn;
    private int txPage = 0;
    private int txTotalPages = 1;

    @FXML
    public void initialize() {
        String suffix = isAdmin ? " (admin)" : (isStaff ? " (staff)" : "");
        welcomeLbl.setText(AuthSession.getUsername().orElse("-") + suffix);

        configureList(usersList, filteredUsers, this::formatUser);
        configureList(membersList, members, this::formatMember);
        configureList(rewardsList, rewards, this::formatReward);
        configureList(transactionsList, transactions, this::formatTransaction);
        configureList(promotionsList, promotions, this::formatPromotion);
        if (txType != null) {
            txType.getItems().setAll("EARN", "REDEEM", "ADJUST");
            txType.setPromptText("All");
        }

        if (!isAdmin) {
            hideTab(usersTab);
            hideTab(rewardsTab);
            if (promotionsCreateBtn != null) {
                promotionsCreateBtn.setDisable(true);
            }
        }

        try {
            if (isAdmin) {
                refreshUsers();
                refreshRewards();
            } else {
                clearList(usersList);
                clearList(rewardsList);
            }
            refreshPromotions();
            refreshMembers();
            refreshTransactions();
            refreshReport();
        } catch (Exception e) {
            Alerts.error("Admin", e.getMessage());
        }
    }

    private void configureList(ListView<Map<String, Object>> list,
                               ObservableList<Map<String, Object>> backing,
                               Function<Map<String, Object>, String> mapper) {
        if (list == null) return;
        list.setItems(backing);
        list.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Map<String, Object>> call(ListView<Map<String, Object>> lv) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Map<String, Object> item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(mapper.apply(item));
                        }
                    }
                };
            }
        });
    }

    @FXML
    public void onRefreshUsers() {
        if (!ensureAdmin("Users")) {
            return;
        }
        try {
            refreshUsers();
        } catch (Exception e) {
            Alerts.error("Users", e.getMessage());
        }
    }

    private void refreshUsers() throws Exception {
        users.setAll(api.adminUsers());
        applyUserFilter();
    }

    @FXML
    public void onCreateUser() {
        if (!ensureAdmin("Users")) {
            return;
        }
    var fields = List.of(
        new FieldSpec("username", "Username", "", false, false),
        new FieldSpec("password", "Password", "", true, false),
        new FieldSpec("roles", "Roles (comma separated)", "ROLE_STAFF", false, false),
        new FieldSpec("fullName", "Full name", "", false, false),
        new FieldSpec("phone", "Phone (digits only)", "", false, false),
        new FieldSpec("tierId", "Tier ID", "1", false, false)
    );
    showForm("Create user", fields).ifPresent(values -> {
      List<String> roles = Arrays.stream(values.get("roles").split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .collect(Collectors.toList());
      if (roles.isEmpty()) roles = List.of("ROLE_STAFF");
      try {
        String phoneVal = values.get("phone").trim();
        if (!phoneVal.matches("\\d+")) {
          Alerts.error("Users", "Phone number must contain digits only");
          return;
        }
        if (phoneVal.length() < 7) {
          Alerts.error("Users", "Phone number must contain at least 7 digits");
          return;
        }
        long tierId = Long.parseLong(values.get("tierId").trim());
        api.adminCreateUser(Map.of(
            "username", values.get("username"),
            "password", values.get("password"),
            "roles", roles,
            "fullName", values.get("fullName").trim(),
            "phone", phoneVal,
            "tierId", tierId
        ));
        refreshUsers();
      } catch (NumberFormatException nfe) {
        Alerts.error("Users", "Tier ID must be a number");
      } catch (Exception e) {
        Alerts.error("Users", e.getMessage());
      }
    });
  }

    @FXML
    public void onSearchUsers() {
        applyUserFilter();
    }

    @FXML
    public void onToggleUser() {
        if (!ensureAdmin("Users")) {
            return;
        }
        var selected = usersList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Users", "Select a user first");
            return;
        }
        try {
            boolean enabled = Boolean.TRUE.equals(selected.get("enabled"));
            api.adminUpdateUser(((Number) selected.get("id")).longValue(), Map.of("enabled", !enabled));
            refreshUsers();
        } catch (Exception e) {
            Alerts.error("Users", e.getMessage());
        }
    }

    @FXML
    public void onResetPassword() {
        if (!ensureAdmin("Users")) {
            return;
        }
        var selected = usersList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Users", "Select a user first");
            return;
        }
        var fields = List.of(new FieldSpec("password", "New password", "", true, false));
        showForm("Reset password", fields).ifPresent(values -> {
            try {
                api.adminResetPassword(((Number) selected.get("id")).longValue(), values.get("password"));
                Alerts.info("Users", "Password updated");
            } catch (Exception e) {
                Alerts.error("Users", e.getMessage());
            }
        });
    }

    @FXML
    public void onRefreshMembers() {
        try {
            refreshMembers();
        } catch (Exception e) {
            Alerts.error("Members", e.getMessage());
        }
    }

    private void refreshMembers() throws Exception {
        members.setAll(api.adminMembers());
    }

    @FXML
    public void onEditMember() {
        var selected = membersList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Members", "Select a member first");
            return;
        }

        var fields = List.of(
                new FieldSpec("fullName", "Full name", String.valueOf(selected.get("fullName")), false, false),
                new FieldSpec("phone", "Phone", String.valueOf(selected.get("phone")), false, false),
                new FieldSpec("tierId", "Tier ID", String.valueOf(selected.get("tierId")), false, false)
        );
        showForm("Edit member", fields).ifPresent(values -> {
            try {
                String phoneVal = values.get("phone").trim();
                if (!phoneVal.matches("\\d+")) {
                    Alerts.error("Members", "Phone number must contain digits only");
                    return;
                }
                long tierId = Long.parseLong(values.get("tierId").trim());
                api.adminUpdateMember(((Number) selected.get("id")).longValue(), Map.of(
                        "fullName", values.get("fullName").trim(),
                        "phone", phoneVal,
                        "tierId", tierId
                ));
                refreshMembers();
            } catch (NumberFormatException nfe) {
                Alerts.error("Members", "Tier ID must be a number");
            } catch (Exception e) {
                Alerts.error("Members", e.getMessage());
            }
        });
    }

    @FXML
    public void onAdjustMemberPoints() {
        var selected = membersList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Members", "Select a member first");
            return;
        }

        var fields = List.of(new FieldSpec(
                "delta",
                "Points delta (use negative to deduct)",
                "100",
                false,
                false));
        showForm("Adjust member points", fields).ifPresent(values -> {
            try {
                long delta = Long.parseLong(values.get("delta"));
                if (delta == 0) {
                    Alerts.info("Members", "Delta must be non-zero");
                    return;
                }
                var response = api.adminAdjustMemberPoints(
                        ((Number) selected.get("id")).longValue(), delta);
                Object balanceObj = response != null ? response.get("balance") : null;
                long balance = balanceObj instanceof Number ? ((Number) balanceObj).longValue() : 0L;
                Alerts.info("Members", "Adjustment applied. New balance: " + balance);
                refreshMembers();
                loadTransactions();
            } catch (NumberFormatException nfe) {
                Alerts.error("Members", "Delta must be a whole number");
            } catch (Exception e) {
                Alerts.error("Members", e.getMessage());
            }
        });
    }

    @FXML
    public void onRefreshRewards() {
        if (!ensureAdmin("Rewards")) {
            return;
        }
        try {
            refreshRewards();
        } catch (Exception e) {
            Alerts.error("Rewards", e.getMessage());
        }
    }

    private void refreshRewards() throws Exception {
        rewards.setAll(api.adminRewards());
    }

    @FXML
    public void onAddReward() {
        if (!ensureAdmin("Rewards")) {
            return;
        }
        var fields = List.of(
                new FieldSpec("title", "Title", "", false, false),
                new FieldSpec("description", "Description", "", false, false),
                new FieldSpec("cost", "Cost (points)", "100", false, false)
        );
        showForm("Create reward", fields).ifPresent(values -> {
            try {
                int cost = Integer.parseInt(values.get("cost"));
                api.adminCreateReward(Map.of(
                        "title", values.get("title"),
                        "description", values.get("description"),
                        "cost", cost
                ));
                refreshRewards();
            } catch (NumberFormatException nfe) {
                Alerts.error("Rewards", "Cost must be a number");
            } catch (Exception e) {
                Alerts.error("Rewards", e.getMessage());
            }
        });
    }

    @FXML
    public void onEditReward() {
        if (!ensureAdmin("Rewards")) {
            return;
        }
        var selected = rewardsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Rewards", "Select a reward first");
            return;
        }

        var fields = List.of(
                new FieldSpec("title", "Title", String.valueOf(selected.get("title")), false, false),
                new FieldSpec("description", "Description", String.valueOf(selected.get("description")), false, false),
                new FieldSpec("cost", "Cost (points)", String.valueOf(selected.get("cost")), false, false)
        );
        showForm("Edit reward", fields).ifPresent(values -> {
            try {
                int cost = Integer.parseInt(values.get("cost"));
                boolean active = Boolean.TRUE.equals(selected.get("active"));
                api.adminUpdateReward(((Number) selected.get("id")).longValue(), Map.of(
                        "title", values.get("title"),
                        "description", values.get("description"),
                        "cost", cost,
                        "active", active
                ));
                refreshRewards();
            } catch (NumberFormatException nfe) {
                Alerts.error("Rewards", "Cost must be a number");
            } catch (Exception e) {
                Alerts.error("Rewards", e.getMessage());
            }
        });
    }

    @FXML
    public void onToggleReward() {
        if (!ensureAdmin("Rewards")) {
            return;
        }
        var selected = rewardsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Rewards", "Select a reward first");
            return;
        }
        try {
            boolean active = Boolean.TRUE.equals(selected.get("active"));
            api.adminSetRewardStatus(((Number) selected.get("id")).longValue(), !active);
            refreshRewards();
        } catch (Exception e) {
            Alerts.error("Rewards", e.getMessage());
        }
    }

    @FXML
    public void onDeleteReward() {
        if (!ensureAdmin("Rewards")) {
            return;
        }
        var selected = rewardsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alerts.info("Rewards", "Select a reward first");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete reward");
        confirm.setHeaderText("Delete " + selected.get("title") + "?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    api.adminDeleteReward(((Number) selected.get("id")).longValue());
                    refreshRewards();
                } catch (Exception e) {
                    Alerts.error("Rewards", e.getMessage());
                }
            }
        });
    }

    @FXML
    public void onRefreshPromotions() {
        try {
            refreshPromotions();
        } catch (Exception e) {
            Alerts.error("Promotions", e.getMessage());
        }
    }

    @FXML
    public void onCreatePromotion() {
        if (!ensureAdmin("Promotions")) {
            return;
        }
        showPromotionDialog().ifPresent(payload -> {
            try {
                api.adminCreatePromotion(payload);
                refreshPromotions();
            } catch (Exception e) {
                Alerts.error("Promotions", e.getMessage());
            }
        });
    }

    private void refreshPromotions() throws Exception {
        if (promotionsList == null) return;
        promotions.setAll(api.adminPromotions());
    }

    @FXML
    public void onRefreshReport() {
        try {
            refreshReport();
        } catch (Exception e) {
            Alerts.error("Reports", e.getMessage());
        }
    }

    private void refreshReport() throws Exception {
        if (reportMembersLbl == null) return;
        LocalDate fromDate = reportFrom == null ? null : reportFrom.getValue();
        LocalDate toDate = reportTo == null ? null : reportTo.getValue();
        String fromIso = toIso(fromDate, true);
        String toIso = toIso(toDate, false);
        var summary = api.adminReportSummary(fromIso, toIso);
        reportMembersLbl.setText(String.valueOf(summary.getOrDefault("totalMembers", 0)));
        reportActiveLbl.setText(String.valueOf(summary.getOrDefault("activeMembers", 0)));
        reportTxLbl.setText(String.valueOf(summary.getOrDefault("totalTransactions", 0)));
        reportPointsLbl.setText(String.valueOf(summary.getOrDefault("totalPointsDelta", 0)));
        Object avg = summary.get("avgTransactionsPerActiveMember");
        reportAvgLbl.setText(avg == null ? "0" : String.format("%.2f", ((Number) avg).doubleValue()));
        reportRangeLbl.setText(buildRangeLabel(summary.get("from"), summary.get("to")));

        var byType = (Map<String, Object>) summary.get("transactionsByType");
        if (reportTypeList != null) {
            if (byType == null || byType.isEmpty()) {
                reportTypeList.getItems().clear();
            } else {
                reportTypeList.getItems().setAll(byType.entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue())
                        .collect(Collectors.toList()));
            }
        }

        var topRewards = (List<Map<String, Object>>) summary.getOrDefault("topRewards", List.of());
        if (reportRewardsList != null) {
            reportRewardsList.getItems().setAll(topRewards.stream()
                    .map(r -> r.get("title") + " - " + r.get("redemptions") + " redeems")
                    .collect(Collectors.toList()));
        }
    }

    @FXML
    public void onRefreshTransactions() {
        txPage = 0;
        loadTransactions();
    }

    @FXML
    public void onTxNext() {
        if (txPage >= txTotalPages - 1) {
            Alerts.info("Transactions", "Already at the last page");
            return;
        }
        txPage++;
        loadTransactions();
    }

    @FXML
    public void onTxPrev() {
        if (txPage <= 0) {
            Alerts.info("Transactions", "Already at the first page");
            return;
        }
        txPage--;
        loadTransactions();
    }

    @FXML
    public void onResetTransactions() {
        if (txMemberFilter != null) {
            txMemberFilter.clear();
        }
        if (txType != null) {
            txType.getSelectionModel().clearSelection();
        }
        if (txFrom != null) {
            txFrom.setValue(null);
        }
        if (txTo != null) {
            txTo.setValue(null);
        }
        txPage = 0;
        loadTransactions();
    }

    private void refreshTransactions() throws Exception {
        loadTransactions();
    }

    private void loadTransactions() {
        Long memberId = null;
        if (txMemberFilter != null && !txMemberFilter.getText().isBlank()) {
            try {
                memberId = Long.parseLong(txMemberFilter.getText().trim());
            } catch (NumberFormatException e) {
                Alerts.error("Transactions", "Member ID must be a number");
                return;
            }
        }

        try {
            String type = txType != null ? txType.getValue() : null;
            if (type != null && !type.isBlank()) {
                type = type.toUpperCase(Locale.ROOT);
            }
            String fromIso = toIso(txFrom, true);
            String toIso = toIso(txTo, false);
            var response = api.adminTransactions(txPage, TX_PAGE_SIZE, memberId, type, fromIso, toIso);
            var items = (List<Map<String, Object>>) response.getOrDefault("items", List.of());
            transactions.setAll(items);
            int currentPage = ((Number) response.getOrDefault("page", 0)).intValue();
            int totalPages = Math.max(1, ((Number) response.getOrDefault("totalPages", 1)).intValue());
            txPage = Math.min(currentPage, totalPages - 1);
            txTotalPages = totalPages;
            txPageLabel.setText(String.format("Page %d of %d", txPage + 1, totalPages));
            updateTxPagingButtons();
        } catch (Exception e) {
            Alerts.error("Transactions", e.getMessage());
        }
    }

    @FXML
    public void onClient() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ui/dashboard.fxml"));
            welcomeLbl.getScene().setRoot(root);
        } catch (Exception e) {
            Alerts.error("Navigation", e.getMessage());
        }
    }

    @FXML
    public void onLogout() {
        try {
            AuthSession.clear();
            Parent root = FXMLLoader.load(getClass().getResource("/ui/login.fxml"));
            welcomeLbl.getScene().setRoot(root);
        } catch (Exception e) {
            Alerts.error("Logout", e.getMessage());
        }
    }

    private Optional<Map<String, String>> showForm(String title, List<FieldSpec> fields) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        Map<String, TextInputControl> inputs = new LinkedHashMap<>();
        int row = 0;
        for (FieldSpec field : fields) {
            Label label = new Label(field.label());
            TextInputControl input = field.password() ? new PasswordField() : new TextField();
            input.setText(field.defaultValue());
            grid.add(label, 0, row);
            grid.add(input, 1, row++);
            inputs.put(field.key(), input);
        }

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            for (FieldSpec field : fields) {
                String value = inputs.get(field.key()).getText().trim();
                if (!field.optional() && value.isEmpty()) {
                    Alerts.info("Validation", field.label() + " is required");
                    event.consume();
                    return;
                }
            }
        });

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                Map<String, String> values = new HashMap<>();
                inputs.forEach((key, input) -> values.put(key, input.getText().trim()));
                return values;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void clearList(ListView<?> list) {
        if (list != null) {
            list.getItems().clear();
        }
    }

    private void hideTab(Tab tab) {
        if (tab != null && tab.getTabPane() != null) {
            tab.getTabPane().getTabs().remove(tab);
        }
    }

    private void updateTxPagingButtons() {
        if (txPrevBtn != null) {
            txPrevBtn.setDisable(txPage <= 0);
        }
        if (txNextBtn != null) {
            txNextBtn.setDisable(txPage >= txTotalPages - 1);
        }
    }

    private boolean ensureAdmin(String feature) {
        if (!isAdmin) {
            Alerts.info(feature, "Available to administrators only");
            return false;
        }
        return true;
    }

    private void applyUserFilter() {
        if (usersList == null) return;
        String query = userSearchField == null ? "" : userSearchField.getText().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            filteredUsers.setAll(users);
        } else {
            filteredUsers.setAll(users.stream()
                    .filter(u -> String.valueOf(u.getOrDefault("username", ""))
                            .toLowerCase(Locale.ROOT).contains(query))
                    .collect(Collectors.toList()));
        }
    }

    private String buildRangeLabel(Object from, Object to) {
        String start = from == null ? "—" : formatDate(from);
        String end = to == null ? "—" : formatDate(to);
        if ("—".equals(start) && "—".equals(end)) {
            return "All time";
        }
        return start + " .. " + end;
    }

    private Optional<Map<String, Object>> showPromotionDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create promotion");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        TextField titleField = new TextField();
        TextField descriptionField = new TextField();
        ComboBox<String> actionBox = new ComboBox<>();
        actionBox.getItems().setAll("AWARD_POINTS", "SEND_NOTIFICATION");
        actionBox.setValue("AWARD_POINTS");

        DatePicker startDate = new DatePicker(LocalDate.now());
        DatePicker endDate = new DatePicker(LocalDate.now().plusDays(1));
        Spinner<Integer> startHour = timeSpinner(0, 23, 9);
        Spinner<Integer> startMinute = timeSpinner(0, 59, 0);
        Spinner<Integer> endHour = timeSpinner(0, 23, 18);
        Spinner<Integer> endMinute = timeSpinner(0, 59, 0);

        TextField pointsField = new TextField("100");
        TextField notifTitleField = new TextField();
        TextArea notifMessageArea = new TextArea();
        notifMessageArea.setPrefRowCount(3);
        notifMessageArea.setWrapText(true);

        int row = 0;
        grid.add(new Label("Title"), 0, row);
        grid.add(titleField, 1, row);
        GridPane.setColumnSpan(titleField, 3);
        row++;

        grid.add(new Label("Description"), 0, row);
        grid.add(descriptionField, 1, row);
        GridPane.setColumnSpan(descriptionField, 3);
        row++;

        grid.add(new Label("Action"), 0, row);
        grid.add(actionBox, 1, row);
        GridPane.setColumnSpan(actionBox, 3);
        row++;

        grid.add(new Label("Start"), 0, row);
        HBox startTimeBox = new HBox(5, startHour, new Label(":"), startMinute);
        grid.add(startDate, 1, row);
        grid.add(startTimeBox, 2, row);
        GridPane.setColumnSpan(startTimeBox, 2);
        row++;

        grid.add(new Label("End"), 0, row);
        HBox endTimeBox = new HBox(5, endHour, new Label(":"), endMinute);
        grid.add(endDate, 1, row);
        grid.add(endTimeBox, 2, row);
        GridPane.setColumnSpan(endTimeBox, 2);
        row++;

        grid.add(new Label("Points amount"), 0, row);
        grid.add(pointsField, 1, row);
        GridPane.setColumnSpan(pointsField, 3);
        row++;

        grid.add(new Label("Notification title"), 0, row);
        grid.add(notifTitleField, 1, row);
        GridPane.setColumnSpan(notifTitleField, 3);
        row++;

        grid.add(new Label("Notification message"), 0, row);
        grid.add(notifMessageArea, 1, row);
        GridPane.setColumnSpan(notifMessageArea, 3);

        dialog.getDialogPane().setContent(grid);

        Runnable toggleFields = () -> {
            boolean award = "AWARD_POINTS".equals(actionBox.getValue());
            pointsField.setDisable(!award);
            notifTitleField.setDisable(award);
            notifMessageArea.setDisable(award);
        };
        actionBox.valueProperty().addListener((obs, oldVal, newVal) -> toggleFields.run());
        toggleFields.run();

        final Instant[] startHolder = new Instant[1];
        final Instant[] endHolder = new Instant[1];

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            String title = titleField.getText().trim();
            String description = descriptionField.getText().trim();
            String action = actionBox.getValue();

            if (title.isEmpty()) {
                Alerts.info("Validation", "Title is required");
                event.consume();
                return;
            }
            if (description.isEmpty()) {
                Alerts.info("Validation", "Description is required");
                event.consume();
                return;
            }
            if (action == null || action.isBlank()) {
                Alerts.info("Validation", "Action is required");
                event.consume();
                return;
            }

            Instant startInstant = composeInstant(startDate, startHour, startMinute);
            Instant endInstant = composeInstant(endDate, endHour, endMinute);
            if (startInstant == null || endInstant == null) {
                Alerts.info("Validation", "Start and end time must be set");
                event.consume();
                return;
            }
            if (!startInstant.isBefore(endInstant)) {
                Alerts.info("Validation", "Start time must be before end time");
                event.consume();
                return;
            }

            if ("AWARD_POINTS".equals(action)) {
                String pointsText = pointsField.getText().trim();
                if (pointsText.isEmpty()) {
                    Alerts.info("Validation", "Points amount is required");
                    event.consume();
                    return;
                }
                try {
                    int points = Integer.parseInt(pointsText);
                    if (points <= 0) {
                        Alerts.info("Validation", "Points amount must be positive");
                        event.consume();
                        return;
                    }
                } catch (NumberFormatException ex) {
                    Alerts.info("Validation", "Points amount must be a number");
                    event.consume();
                    return;
                }
            } else {
                if (notifTitleField.getText().trim().isEmpty() || notifMessageArea.getText().trim().isEmpty()) {
                    Alerts.info("Validation", "Notification title and message are required");
                    event.consume();
                    return;
                }
            }

            startHolder[0] = startInstant;
            endHolder[0] = endInstant;
        });

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("title", titleField.getText().trim());
                payload.put("description", descriptionField.getText().trim());
                String action = actionBox.getValue().toUpperCase(java.util.Locale.ROOT);
                payload.put("actionType", action);
                payload.put("startAt", startHolder[0].toString());
                payload.put("endAt", endHolder[0].toString());
                if ("AWARD_POINTS".equals(action)) {
                    payload.put("pointsAmount", Integer.parseInt(pointsField.getText().trim()));
                } else {
                    payload.put("notificationTitle", notifTitleField.getText().trim());
                    payload.put("notificationMessage", notifMessageArea.getText().trim());
                }
                return payload;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private String formatUser(Map<String, Object> map) {
        String username = String.valueOf(map.get("username"));
        String roles = joinRoles(map.get("roles"));
        String enabled = Boolean.TRUE.equals(map.get("enabled")) ? "enabled" : "disabled";
        Long memberId = map.get("memberId") == null ? null : ((Number) map.get("memberId")).longValue();
        return memberId == null
                ? String.format("%s [%s] (%s)", username, roles, enabled)
                : String.format("%s [%s] (%s) -> member %d", username, roles, enabled, memberId);
    }

    private String formatMember(Map<String, Object> map) {
        String fullName = String.valueOf(map.get("fullName"));
        String phone = String.valueOf(map.get("phone"));
        String tier = String.valueOf(map.get("tier"));
        long points = map.get("points") == null ? 0 : ((Number) map.get("points")).longValue();
        return String.format("%s | %s | %s | %d pts", fullName, phone, tier, points);
    }

    private String formatReward(Map<String, Object> map) {
        String title = String.valueOf(map.get("title"));
        int cost = map.get("cost") == null ? 0 : ((Number) map.get("cost")).intValue();
        boolean active = Boolean.TRUE.equals(map.get("active"));
        return String.format("%s (%d pts) %s", title, cost, active ? "" : "[inactive]");
    }

    private String formatTransaction(Map<String, Object> map) {
        String time = formatDate(map.get("createdAt"));
        long amount = map.get("amount") == null ? 0 : ((Number) map.get("amount")).longValue();
        String type = String.valueOf(map.get("type"));
        String member = String.valueOf(map.get("memberName"));
        String reward = map.get("rewardTitle") == null ? "" : " -> " + map.get("rewardTitle");
        return String.format("%s | %s | %+d | %s%s", time, member, amount, type, reward);
    }

    private String formatPromotion(Map<String, Object> map) {
        String title = String.valueOf(map.get("title"));
        String action = String.valueOf(map.get("actionType"));
        String start = String.valueOf(map.get("startAt"));
        String end = String.valueOf(map.get("endAt"));
        boolean executed = Boolean.TRUE.equals(map.get("executed"));
        return String.format("%s [%s] %s -> %s %s", title, action, start, end, executed ? "(completed)" : "");
    }

    private String joinRoles(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.joining(", "));
        }
        return value == null ? "" : String.valueOf(value);
    }

    private String formatDate(Object value) {
        if (value == null) return "";
        try {
            return dtf.format(Instant.parse(String.valueOf(value)));
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private String toIso(DatePicker picker, boolean startOfDay) {
        if (picker == null) return null;
        return toIso(picker.getValue(), startOfDay);
    }

    private String toIso(LocalDate date, boolean startOfDay) {
        if (date == null) return null;
        var zone = ZoneId.systemDefault();
        if (startOfDay) {
            return date.atStartOfDay(zone).toInstant().toString();
        } else {
            return date.plusDays(1).atStartOfDay(zone).minusSeconds(1).toInstant().toString();
        }
    }

    private Spinner<Integer> timeSpinner(int min, int max, int initial) {
        Spinner<Integer> spinner = new Spinner<>();
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initial, 1));
        spinner.setEditable(true);
        return spinner;
    }

    private Instant composeInstant(DatePicker picker, Spinner<Integer> hourSpinner, Spinner<Integer> minuteSpinner) {
        if (picker == null || picker.getValue() == null) {
            return null;
        }
        LocalDate date = picker.getValue();
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        return date.atTime(LocalTime.of(hour, minute)).atZone(ZoneId.systemDefault()).toInstant();
    }

    private record FieldSpec(String key, String label, String defaultValue, boolean password, boolean optional) {
    }
}


