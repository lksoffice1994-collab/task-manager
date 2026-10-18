package com.learning.taskservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final Environment environment;

    @Autowired
    public WebSecurityConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Disable CSRF since we’re stateless (no sessions)
        http.csrf(csrf -> csrf.disable());

        // Use a dummy AuthenticationManager (since we’re only doing authorization)
        AuthenticationManager dummyAuthenticationManager = authentication -> authentication;

        // Custom authorization filter that validates JWT and sets Authentication
        AuthorizationFilter authorizationFilter =
                new AuthorizationFilter(dummyAuthenticationManager, environment);

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilter(authorizationFilter)
                .authorizeHttpRequests(authz -> authz
                        // restrict as needed — you can change permitAll to authenticated() if required
                        .requestMatchers("/**").permitAll()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> null; // disables the default in-memory user
    }

}
