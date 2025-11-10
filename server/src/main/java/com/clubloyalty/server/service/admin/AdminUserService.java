package com.clubloyalty.server.service.admin;

import com.clubloyalty.server.dto.AdminDtos.UserCreateRequest;
import com.clubloyalty.server.dto.AdminDtos.UserSummary;
import com.clubloyalty.server.dto.AdminDtos.UserUpdateRequest;

import java.util.List;

public interface AdminUserService {
    List<UserSummary> list();

    UserSummary create(UserCreateRequest request);

    UserSummary update(Long userId, UserUpdateRequest request);

    void resetPassword(Long userId, String newPassword);
}
