package com.learning.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final boolean isOtpValidated;

    public CustomUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities, boolean isOtpValidated) {
        super(username, password, authorities);
        this.isOtpValidated = isOtpValidated;
    }

    public CustomUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
                             boolean credentialsNonExpired, boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities, boolean isOtpValidated) {
        super(username,password,enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, authorities);
        this.isOtpValidated = isOtpValidated;
    }

    public boolean isOtpValidated() {
        return isOtpValidated;
    }
}
