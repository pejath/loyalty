package com.clubloyalty.server.config;

import com.clubloyalty.server.repo.UserRepository;
import com.clubloyalty.server.security.UserDetailsImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class AuthBeans {

    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByUsername(username)
                .map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
