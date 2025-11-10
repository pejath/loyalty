package main.java.com.clubloyalty.client.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.com.clubloyalty.client.MainApp;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ApiClient {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper om = new ObjectMapper();
    private final String base = MainApp.SERVER_URL;

    private HttpRequest.Builder req(String path) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path)).timeout(Duration.ofSeconds(10));
        AuthSession.get().ifPresent(t -> b.header("Authorization", "Bearer " + t));
        return b;
    }

    private HttpRequest.BodyPublisher jsonBody(Object payload) throws Exception {
        return HttpRequest.BodyPublishers.ofString(om.writeValueAsString(payload), StandardCharsets.UTF_8);
    }

    private Map<String, Object> readObject(HttpResponse<String> response) throws Exception {
        return om.readValue(response.body(), Map.class);
    }

    private List<Map<String, Object>> readList(HttpResponse<String> response) throws Exception {
        return om.readValue(response.body(), List.class);
    }

    public String login(String username, String password) throws Exception {
        var payload = Map.of("username", username, "password", password);
        var r = http.send(req("/api/auth/login")
                .header("Content-Type", "application/json")
                .POST(jsonBody(payload))
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException("Login failed: " + r.body());
        return om.readTree(r.body()).get("token").asText();
    }

    public Map<String, Object> profile() throws Exception {
        var r = http.send(req("/api/auth/me").GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public void register(String username, String password, String fullName, String phone) throws Exception {
        var payload = Map.of("username", username, "password", password, "fullName", fullName, "phone", phone);
        var r = http.send(req("/api/auth/register")
                .header("Content-Type", "application/json")
                .POST(jsonBody(payload))
                .build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException("Register failed: " + r.body());
    }

    public Map<String, Object> me() throws Exception {
        var r = http.send(req("/api/members/me").GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public List<Map<String, Object>> rewards() throws Exception {
        var r = http.send(req("/api/rewards").GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readList(r);
    }

    public Map<String, Object> redeem(long rewardId, long memberId) throws Exception {
        var r = http.send(req("/api/rewards/redeem/" + rewardId + "/member/" + memberId)
                .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public Map<String, Object> transactions(Integer page, Integer size, String type, String fromIso, String toIso) throws Exception {
        int p = page == null ? 0 : page;
        int s = size == null ? 20 : size;
        var path = new StringBuilder("/api/transactions?page=" + p + "&size=" + s);
        if (type != null && !type.isBlank())
            path.append("&type=").append(URLEncoder.encode(type, StandardCharsets.UTF_8));
        if (fromIso != null && !fromIso.isBlank())
            path.append("&from=").append(URLEncoder.encode(fromIso, StandardCharsets.UTF_8));
        if (toIso != null && !toIso.isBlank())
            path.append("&to=").append(URLEncoder.encode(toIso, StandardCharsets.UTF_8));
        var r = http.send(req(path.toString()).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public List<Map<String, Object>> notifications() throws Exception {
        var r = http.send(req("/api/notifications").GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readList(r);
    }

    // --- Admin endpoints ---

    public List<Map<String, Object>> adminUsers() throws Exception {
        var r = http.send(req("/api/admin/users").GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readList(r);
    }

    public Map<String, Object> adminCreateUser(Map<String, Object> payload) throws Exception {
        var r = http.send(req("/api/admin/users")
                .header("Content-Type", "application/json")
                .POST(jsonBody(payload)).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public Map<String, Object> adminUpdateUser(long userId, Map<String, Object> payload) throws Exception {
        var r = http.send(req("/api/admin/users/" + userId)
                .header("Content-Type", "application/json")
                .PUT(jsonBody(payload)).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public void adminResetPassword(long userId, String password) throws Exception {
        var r = http.send(req("/api/admin/users/" + userId + "/password")
                .header("Content-Type", "application/json")
                .POST(jsonBody(Map.of("password", password))).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
    }

    public List<Map<String, Object>> adminMembers() throws Exception {
        var r = http.send(req("/api/admin/members").GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readList(r);
    }

    public Map<String, Object> adminUpdateMember(long memberId, Map<String, Object> payload) throws Exception {
        var r = http.send(req("/api/admin/members/" + memberId)
                .header("Content-Type", "application/json")
                .PUT(jsonBody(payload)).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public Map<String, Object> adminAdjustMemberPoints(long memberId, long delta) throws Exception {
        var r = http.send(req("/api/admin/members/" + memberId + "/points")
                .header("Content-Type", "application/json")
                .POST(jsonBody(Map.of("delta", delta))).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public List<Map<String, Object>> adminRewards() throws Exception {
        var r = http.send(req("/api/admin/rewards").GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readList(r);
    }

    public Map<String, Object> adminCreateReward(Map<String, Object> payload) throws Exception {
        var r = http.send(req("/api/admin/rewards")
                .header("Content-Type", "application/json")
                .POST(jsonBody(payload)).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public Map<String, Object> adminUpdateReward(long rewardId, Map<String, Object> payload) throws Exception {
        var r = http.send(req("/api/admin/rewards/" + rewardId)
                .header("Content-Type", "application/json")
                .PUT(jsonBody(payload)).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public Map<String, Object> adminSetRewardStatus(long rewardId, boolean active) throws Exception {
        var r = http.send(req("/api/admin/rewards/" + rewardId + "/status")
                .header("Content-Type", "application/json")
                .method("PATCH", jsonBody(Map.of("active", active))).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public void adminDeleteReward(long rewardId) throws Exception {
        var r = http.send(req("/api/admin/rewards/" + rewardId).DELETE().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
    }

    public Map<String, Object> adminTransactions(int page, int size, Long memberId, String type, String fromIso, String toIso) throws Exception {
        var path = new StringBuilder("/api/admin/transactions?page=" + page + "&size=" + size);
        if (memberId != null) {
            path.append("&memberId=").append(memberId);
        }
        if (type != null && !type.isBlank())
            path.append("&type=").append(URLEncoder.encode(type, StandardCharsets.UTF_8));
        if (fromIso != null && !fromIso.isBlank())
            path.append("&from=").append(URLEncoder.encode(fromIso, StandardCharsets.UTF_8));
        if (toIso != null && !toIso.isBlank())
            path.append("&to=").append(URLEncoder.encode(toIso, StandardCharsets.UTF_8));
        var r = http.send(req(path.toString()).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public List<Map<String, Object>> adminPromotions() throws Exception {
        var r = http.send(req("/api/admin/promotions").GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readList(r);
    }

    public Map<String, Object> adminCreatePromotion(Map<String, Object> payload) throws Exception {
        var r = http.send(req("/api/admin/promotions")
                .header("Content-Type", "application/json")
                .POST(jsonBody(payload)).build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }

    public Map<String, Object> adminReportSummary(String fromIso, String toIso) throws Exception {
        var path = new StringBuilder("/api/admin/reports/summary");
        boolean hasQuery = false;
        if (fromIso != null && !fromIso.isBlank()) {
            path.append(hasQuery ? "&" : "?").append("from=").append(URLEncoder.encode(fromIso, StandardCharsets.UTF_8));
            hasQuery = true;
        }
        if (toIso != null && !toIso.isBlank()) {
            path.append(hasQuery ? "&" : "?").append("to=").append(URLEncoder.encode(toIso, StandardCharsets.UTF_8));
            hasQuery = true;
        }
        var r = http.send(req(path.toString()).GET().build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() != 200) throw new RuntimeException(r.body());
        return readObject(r);
    }
}


