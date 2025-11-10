package com.clubloyalty.server.web;

import com.clubloyalty.server.dto.AdminDtos.PasswordResetRequest;
import com.clubloyalty.server.dto.AdminDtos.UserCreateRequest;
import com.clubloyalty.server.dto.AdminDtos.UserSummary;
import com.clubloyalty.server.dto.AdminDtos.UserUpdateRequest;
import com.clubloyalty.server.dto.AuthDtos.SimpleResponse;
import com.clubloyalty.server.service.admin.AdminUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService users;

    public AdminUserController(AdminUserService users) {
        this.users = users;
    }

    @GetMapping
    public List<UserSummary> list() {
        return users.list();
    }

    @PostMapping
    public UserSummary create(@RequestBody @Validated UserCreateRequest request) {
        return users.create(request);
    }

    @PutMapping("/{id}")
    public UserSummary update(@PathVariable Long id, @RequestBody @Validated UserUpdateRequest request) {
        return users.update(id, request);
    }

    @PostMapping("/{id}/password")
    public SimpleResponse resetPassword(@PathVariable Long id, @RequestBody @Validated PasswordResetRequest request) {
        users.resetPassword(id, request.password);
        return new SimpleResponse("OK");
    }
}
