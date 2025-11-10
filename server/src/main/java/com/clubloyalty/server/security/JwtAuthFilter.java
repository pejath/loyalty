package com.clubloyalty.server.security;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtProvider jwt;
    @Autowired
    private UserDetailsService uds;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String t = h.substring(7);
            try {
                String username = jwt.parse(t).getBody().getSubject();
                UserDetails ud = uds.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken a = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(a);
            } catch (JwtException e) {/* ignore */}
        }
        chain.doFilter(req, res);
    }
}
