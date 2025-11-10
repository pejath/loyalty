package com.clubloyalty.server.domain.user;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String passwordHash;
    private boolean enabled = true;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String u) {
        this.username = u;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String p) {
        this.passwordHash = p;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean e) {
        this.enabled = e;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
