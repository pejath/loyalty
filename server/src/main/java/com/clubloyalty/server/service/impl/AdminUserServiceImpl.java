package com.clubloyalty.server.service.impl;

import com.clubloyalty.server.domain.Balance;
import com.clubloyalty.server.domain.Member;
import com.clubloyalty.server.domain.Tier;
import com.clubloyalty.server.domain.user.Role;
import com.clubloyalty.server.domain.user.User;
import com.clubloyalty.server.dto.AdminDtos.UserCreateRequest;
import com.clubloyalty.server.dto.AdminDtos.UserSummary;
import com.clubloyalty.server.dto.AdminDtos.UserUpdateRequest;
import com.clubloyalty.server.repo.*;
import com.clubloyalty.server.service.admin.AdminUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminUserServiceImpl implements AdminUserService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final MemberRepository members;
    private final TierRepository tiers;
    private final BalanceRepository balances;
    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(UserRepository users,
                                RoleRepository roles,
                                MemberRepository members,
                                TierRepository tiers,
                                BalanceRepository balances,
                                PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.members = members;
        this.tiers = tiers;
        this.balances = balances;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserSummary> list() {
        return users.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public UserSummary create(UserCreateRequest request) {
        if (users.findByUsername(request.username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        var user = new User();
        user.setUsername(request.username);
        user.setPasswordHash(passwordEncoder.encode(request.password));
        user.setEnabled(request.enabled == null || request.enabled);
        user.getRoles().addAll(resolveRoles(request.roles));
        users.save(user);

        String fullName = (request.fullName == null || request.fullName.isBlank())
                ? request.username
                : request.fullName.trim();
        String phone = sanitizePhone(request.phone);
        Long tierId = resolveTierId(request.tierId);

        createMemberForUser(user, fullName, phone, tierId);

        return toDto(user);
    }

    public UserSummary update(Long userId, UserUpdateRequest request) {
        var user = users.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (request.enabled != null) {
            user.setEnabled(request.enabled);
        }
        if (request.roles != null && !request.roles.isEmpty()) {
            user.getRoles().clear();
            user.getRoles().addAll(resolveRoles(request.roles));
        }
        return toDto(user);
    }

    public void resetPassword(Long userId, String newPassword) {
        var user = users.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    private Set<Role> resolveRoles(List<String> roleCodes) {
        return roleCodes.stream()
                .map(code -> roles.findByCode(code).orElseThrow(() -> new RuntimeException("Role not found: " + code)))
                .collect(Collectors.toSet());
    }

    private void createMemberForUser(User user, String fullName, String phone, Long tierId) {
        if (members.findByPhone(phone).isPresent()) {
            throw new RuntimeException("Phone already in use");
        }
        Tier tier = tiers.findById(tierId).orElseThrow(() -> new RuntimeException("Tier not found"));
        var member = new Member();
        member.setFullName(fullName);
        member.setPhone(phone);
        member.setTier(tier);
        member.setUser(user);
        members.save(member);

        var balance = new Balance(member, 0);
        member.setBalance(balance);
        balances.save(balance);
    }

    private UserSummary toDto(User user) {
        var dto = new UserSummary();
        dto.id = user.getId();
        dto.username = user.getUsername();
        dto.enabled = user.isEnabled();
        dto.roles = user.getRoles().stream().map(Role::getCode).sorted().collect(Collectors.toList());
        dto.memberId = members.findByUserId(user.getId()).map(Member::getId).orElse(null);
        return dto;
    }

    private String sanitizePhone(String raw) {
        String phone = raw == null ? "" : raw.trim();
        if (phone.isEmpty()) {
            phone = generateFallbackPhone();
        } else if (!phone.matches("\\d+")) {
            throw new RuntimeException("Phone number must contain digits only");
        } else if (phone.length() < 7) {
            throw new RuntimeException("Phone number must contain at least 7 digits");
        }
        while (members.findByPhone(phone).isPresent()) {
            phone = generateFallbackPhone();
        }
        return phone;
    }

    private String generateFallbackPhone() {
        String base = String.valueOf(System.currentTimeMillis());
        String suffix = base.substring(Math.max(0, base.length() - 6));
        return "9" + suffix;
    }

    private Long resolveTierId(Long requested) {
        if (requested != null) {
            return requested;
        }
        return tiers.findById(1L)
                .map(Tier::getId)
                .orElseGet(() -> tiers.findAll().stream()
                        .sorted((a, b) -> Long.compare(a.getThreshold(), b.getThreshold()))
                        .findFirst()
                        .map(Tier::getId)
                        .orElseThrow(() -> new RuntimeException("No tiers found")));
    }
}
