package com.learning.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
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
import java.security.spec.KeySpec;
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
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {

        String authorizationHeader = req.getHeader(environment.getProperty("authorization.token.header.name", "Authorization"));
        String headerPrefix = environment.getProperty("authorization.token.header.prefix", "Bearer ");
        if (authorizationHeader == null
                || !authorizationHeader.startsWith(headerPrefix)) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest req) {
        // Use the actual property names set in your application.properties
        String headerName = environment.getProperty("authorization.token.header.name", "Authorization");
        String headerPrefix = environment.getProperty("authorization.token.header.prefix", "Bearer ");
        String tokenSecret = environment.getProperty("jwt.token.secret");

        String authorizationHeader = req.getHeader(headerName);

        if (authorizationHeader == null || !authorizationHeader.startsWith(headerPrefix)) {
            return null;
        }

        String token = authorizationHeader.replace(headerPrefix, "").trim();

        if(tokenSecret == null) return null;

        byte[] tokenSecretBytes = Base64.getDecoder().decode(tokenSecret);
        SecretKey secretKey = new SecretKeySpec (tokenSecretBytes, SignatureAlgorithm.HS256.getJcaName());

        JwtParser parser = Jwts.parser()
                .setSigningKey(secretKey);

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
