package main.java.com.clubloyalty.client.net;

import java.util.List;
import java.util.Optional;

public class AuthSession {
    private static String token;
    private static String username;
    private static List<String> roles = List.of();
    private static Long memberId;

    public static void set(String t) {
        token = t;
    }

    public static Optional<String> get() {
        return Optional.ofNullable(token);
    }

    public static void setProfile(String user, List<String> r, Long member) {
        username = user;
        roles = r == null ? List.of() : List.copyOf(r);
        memberId = member;
    }

    public static Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public static List<String> getRoles() {
        return roles;
    }

    public static boolean hasRole(String role) {
        return roles.contains(role);
    }

    public static Optional<Long> getMemberId() {
        return Optional.ofNullable(memberId);
    }

    public static void clear() {
        token = null;
        username = null;
        roles = List.of();
        memberId = null;
    }
}
