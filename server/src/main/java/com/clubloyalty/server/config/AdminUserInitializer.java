package com.clubloyalty.server.config;

import com.clubloyalty.server.domain.Balance;
import com.clubloyalty.server.domain.Member;
import com.clubloyalty.server.domain.Tier;
import com.clubloyalty.server.domain.user.User;
import com.clubloyalty.server.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

  private final UserRepository users;
  private final RoleRepository roles;
  private final TierRepository tiers;
  private final MemberRepository members;
  private final BalanceRepository balances;
  private final PasswordEncoder passwordEncoder;

  public AdminUserInitializer(UserRepository users,
                              RoleRepository roles,
                              TierRepository tiers,
                              MemberRepository members,
                              BalanceRepository balances,
                              PasswordEncoder passwordEncoder) {
    this.users = users;
    this.roles = roles;
    this.tiers = tiers;
    this.members = members;
    this.balances = balances;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    if (users.findByUsername("main").isPresent()) {
      return;
    }
    var adminRole = roles.findByCode("ROLE_ADMIN")
        .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

    var user = new User();
    user.setUsername("main");
    user.setPasswordHash(passwordEncoder.encode("main"));
    user.setEnabled(true);
    user.getRoles().add(adminRole);
    users.save(user);

    Tier tier = tiers.findById(1L)
        .orElseGet(() -> tiers.findAll().stream()
            .sorted((a, b) -> Long.compare(a.getThreshold(), b.getThreshold()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No tiers found")));
    var member = new Member();
    member.setFullName("Main Admin");
    member.setPhone("9999999999");
    member.setTier(tier);
    member.setUser(user);
    member = members.save(member);

    // reload to ensure managed instance for balance creation
    member = members.findById(member.getId()).orElse(member);

    var balance = new Balance(member, 0);
    member.setBalance(balance);
    balances.save(balance);
  }
}
