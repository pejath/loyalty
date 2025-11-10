package com.clubloyalty.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {
    private final Key key;
    private final long expiration;

    public JwtProvider(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration:86400}") long exp) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = exp * 1000;
    }

    public String generate(String username, java.util.Collection<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);
        return Jwts.builder().setSubject(username).claim("roles", roles).setIssuedAt(now).setExpiration(exp).signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
