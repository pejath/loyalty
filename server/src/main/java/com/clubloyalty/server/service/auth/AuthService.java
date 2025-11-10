package com.clubloyalty.server.service.auth;

import com.clubloyalty.server.dto.AuthDtos.ProfileResponse;
import com.clubloyalty.server.dto.AuthDtos.RegisterRequest;

public interface AuthService {
    String login(String username, String password);

    void register(RegisterRequest req);

    ProfileResponse profile(String username);
}
