package com.clubloyalty.server.service.impl;

import com.clubloyalty.server.domain.Balance;
import com.clubloyalty.server.domain.Member;
import com.clubloyalty.server.domain.Tier;
import com.clubloyalty.server.domain.user.Role;
import com.clubloyalty.server.domain.user.User;
import com.clubloyalty.server.dto.AuthDtos.ProfileResponse;
import com.clubloyalty.server.dto.AuthDtos.RegisterRequest;
import com.clubloyalty.server.repo.*;
import com.clubloyalty.server.security.JwtProvider;
import com.clubloyalty.server.service.auth.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final TierRepository tiers;
    private final MemberRepository members;
    private final BalanceRepository balances;
    private final PasswordEncoder pe;
    private final JwtProvider jwt;

    public AuthServiceImpl(UserRepository u, RoleRepository r, TierRepository t, MemberRepository m, BalanceRepository b, PasswordEncoder pe, JwtProvider j) {
        this.users = u;
        this.roles = r;
        this.tiers = t;
        this.members = m;
        this.balances = b;
        this.pe = pe;
        this.jwt = j;
    }

    @Transactional(readOnly = true)
    public String login(String username, String password) {
        var u = users.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        if (!u.isEnabled()) throw new RuntimeException("Account disabled");
        if (!pe.matches(password, u.getPasswordHash())) throw new RuntimeException("Bad credentials");
        var roleCodes = u.getRoles().stream().map(Role::getCode).toList();
        return jwt.generate(u.getUsername(), roleCodes);
    }

    @Transactional
    public void register(RegisterRequest req) {
        if (users.findByUsername(req.username).isPresent()) throw new RuntimeException("Username already exists");
        if (req.phone == null || !req.phone.trim().matches("\\d{7,}")) {
            throw new RuntimeException("Phone number must contain at least 7 digits");
        }

        var user = new User();
        user.setUsername(req.username);
        user.setPasswordHash(pe.encode(req.password));
        user.setEnabled(true);
        var roleGuest = roles.findByCode("ROLE_GUEST").orElseThrow(() -> new RuntimeException("ROLE_GUEST not found"));
        user.getRoles().add(roleGuest);
        users.save(user);

        Tier tier = tiers.findById(1L).orElseGet(() -> tiers.findAll().stream().sorted((a, b) -> Long.compare(a.getThreshold(), b.getThreshold())).findFirst().orElseThrow(() -> new RuntimeException("No tiers")));
        var member = new Member();
        member.setFullName(req.fullName);
        member.setPhone(req.phone);
        member.setTier(tier);
        member.setUser(user);
        members.save(member);

        var bal = new Balance(member, 0);
        member.setBalance(bal);
        balances.save(bal);
    }

    @Transactional(readOnly = true)
    public ProfileResponse profile(String username) {
        var user = users.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        var rolesList = user.getRoles().stream().map(Role::getCode).sorted().toList();
        Long memberId = members.findByUserId(user.getId()).map(Member::getId).orElse(null);
        return new ProfileResponse(user.getUsername(), rolesList, memberId);
    }
}
