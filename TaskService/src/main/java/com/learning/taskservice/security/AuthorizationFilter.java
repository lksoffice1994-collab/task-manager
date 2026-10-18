package com.learning.taskservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AuthorizationFilter extends BasicAuthenticationFilter {

    private final Environment environment;

    public AuthorizationFilter(AuthenticationManager authenticationManager, Environment environment) {
        super(authenticationManager);
        this.environment = environment;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String headerName = environment.getProperty("authorization.token.header.name", "Authorization");
        String headerPrefix = environment.getProperty("authorization.token.header.prefix", "Bearer ");

        String authorizationHeader = req.getHeader(headerName);

        // Skip filter if no valid header
        if (authorizationHeader == null || !authorizationHeader.startsWith(headerPrefix)) {
            chain.doFilter(req, res);
            return;
        }

        try {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(authorizationHeader);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            chain.doFilter(req, res);
        } catch (Exception e) {
            chain.doFilter(req, res);
        }

    }

    private UsernamePasswordAuthenticationToken getAuthentication(String authorizationHeader) {
        String headerPrefix = environment.getProperty("authorization.token.header.prefix", "Bearer ");
        String tokenSecret = environment.getProperty("jwt.token.secret");

        if (authorizationHeader == null || !authorizationHeader.startsWith(headerPrefix)) {
            return null;
        }

        String token = authorizationHeader.replace(headerPrefix, "").trim();
        if (tokenSecret == null) return null;

        // Build signing key
        byte[] tokenSecretBytes = Base64.getDecoder().decode(tokenSecret);
        SecretKey secretKey = new SecretKeySpec(tokenSecretBytes, SignatureAlgorithm.HS256.getJcaName());

        // Use the new parser builder
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build();

        Claims claims = parser.parseClaimsJws(token).getBody();
        String userId = claims.getSubject();

        // Extract roles and build authorities
        List<String> roles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority(role));
            }
        }

        if (userId == null) {
            return null;
        }

        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }

}
