package com.learning.config.security;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.learning.entity.RoleEntity;
import com.learning.model.LoginRequestModel;
import com.learning.model.RoleDto;
import com.learning.model.UserDto;
import com.learning.service.UserService;
import com.learning.utils.UserServiceUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	ObjectMapper objectMapper = new ObjectMapper();

	private final UserService userService;
	private final Environment environment;

	public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, Environment environment) {
		super(authenticationManager);
		this.userService = userService;
		this.environment = environment;
	}

	@Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Customizable error handling for OTP, locked, etc.
        String errorCode = "AUTH_FAILED";
        String errorMsg = "Authentication failed. " + failed.getMessage();
        if (failed.getClass().getSimpleName().equals("OtpNotVerifiedException")) {
            errorCode = "OTP_NOT_VERIFIED";
            errorMsg = failed.getMessage();
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), java.util.Map.of(
                "code", errorCode,
                "message", errorMsg
        ));
    }


	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {
		try {

			LoginRequestModel creds = objectMapper.readValue(req.getInputStream(), LoginRequestModel.class);

			return getAuthenticationManager().authenticate(
					new UsernamePasswordAuthenticationToken(creds.getUsernameOrEmail(), creds.getPassword(), new ArrayList<>()));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req, 
			HttpServletResponse res, FilterChain chain,
			Authentication auth) throws IOException, ServletException {

		String userName = ((User)auth.getPrincipal()).getUsername();

		UserDto userDto = null;
		if (UserServiceUtils.isEmail(userName)) {
			userDto = userService.findUserByEmail(userName);
		} else {
			userDto = userService.findUserByUserId(userName);
		}

		if(Objects.isNull(userDto)) {
			throw new UsernameNotFoundException(userName);
		}

		String tokenSecret = environment.getProperty("jwt.token.secret");
		byte[] tokenSecretBytes = Base64.getDecoder().decode(tokenSecret);
		SecretKey secretKey = new SecretKeySpec (tokenSecretBytes, SignatureAlgorithm.HS256.getJcaName());

		for(RoleDto roleDto : userDto.getRoles()) {
			
		}

        // Collect role names
        List<String> roleNames = new ArrayList<>();
        if (userDto.getRoles() != null) {
            for (RoleDto roleDto : userDto.getRoles()) {
                roleNames.add(roleDto.getRoleName());
            }
        }

        Instant now = Instant.now();
        String token = Jwts.builder()
                .setSubject(userDto.getUserId())
                .claim("roles", roleNames)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(Long.parseLong(environment.getProperty("jwt.expiration")))))
                .signWith(SignatureAlgorithm.HS256, secretKey).compact();

        res.addHeader("Authorization", token);
        res.addHeader("userId", userDto.getUserId());
		res.addHeader("role", ((RoleDto)(userDto.getRoles().toArray()[0])).getRoleName());
    }
}
