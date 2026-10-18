package com.learning.config.security;

import com.learning.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity // to override default spring security configs
public class WebSecurityConfig {

    private final Environment environment;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public WebSecurityConfig(Environment environment, UserService userService, PasswordEncoder passwordEncoder) {
        this.environment = environment;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        // csrf is needed when we we save session in user service
        http.csrf(csrf -> csrf.disable());

        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder);

        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        AuthenticationFilter authenticationFilter =
                new AuthenticationFilter(authenticationManager, userService, environment);

        authenticationFilter.setFilterProcessesUrl("/user/login");

        AuthorizationFilter authorizationFilter =
                new AuthorizationFilter(authenticationManager, environment);

        http.authorizeHttpRequests(authZ ->
                authZ
                    .requestMatchers(new AntPathRequestMatcher("/user")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/user/*")).permitAll()
                    .requestMatchers(new AntPathRequestMatcher("/user/otp/**")).permitAll()
                )
                .addFilter(authorizationFilter)
                .addFilter(authenticationFilter)
                .authenticationManager(authenticationManager)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.headers(headers ->
                headers.frameOptions(frameOptionsConfig -> frameOptionsConfig.sameOrigin()));

        return http.build();
    }

}

/*
package com.learning.config.security;

import com.learning.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final Environment environment;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public WebSecurityConfig(Environment environment,
                             UserService userService,
                             PasswordEncoder passwordEncoder) {
        this.environment = environment;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 🔹 Disable CSRF since we are stateless (JWT)
        http.csrf(csrf -> csrf.disable());

        // 🔹 Build custom AuthenticationManager
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder);
        AuthenticationManager authenticationManager = authBuilder.build();

        // 🔹 Custom filters
        AuthenticationFilter authenticationFilter =
                new AuthenticationFilter(authenticationManager, userService, environment);
        authenticationFilter.setFilterProcessesUrl("/user/login");

        AuthorizationFilter authorizationFilter =
                new AuthorizationFilter(authenticationManager, environment);

        // 🔹 Configure request authorization
        http.authorizeHttpRequests(authz -> authz
                .requestMatchers(
                        new AntPathRequestMatcher("/user/**")
                ).permitAll()
                .anyRequest().authenticated()
        );

        // 🔹 Register filters (order matters!)
        http.addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(authenticationFilter, AuthorizationFilter.class);

        // 🔹 Set AuthenticationManager
        http.authenticationManager(authenticationManager);

        // 🔹 Stateless session
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 🔹 Allow H2-console or same-origin iframes if needed
        http.headers(headers ->
                headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }
}
*/
