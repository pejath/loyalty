package com.clubloyalty.server.security;

import com.clubloyalty.server.domain.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
    private final User u;

    public UserDetailsImpl(User u) {
        this.u = u;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return u.getRoles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.getCode().replace("ROLE_", ""))).collect(Collectors.toSet());
    }

    public String getPassword() {
        return u.getPasswordHash();
    }

    public String getUsername() {
        return u.getUsername();
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return u.isEnabled();
    }
}
