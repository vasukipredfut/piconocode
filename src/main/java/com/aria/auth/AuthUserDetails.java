package com.aria.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Substrate-native Spring Security principal — replaces the legacy
 * {@code com.aria.service.UserDetailsImpl}, which was welded to the fat
 * {@code com.aria.model.User} entity (coupled to ~50 business tables). This
 * carries only what security needs: id, username, password, and the single
 * role authority. No JPA, no legacy dependency.
 */
public final class AuthUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthUserDetails(Long id, String username, String password, Long roleId) {
        this.id = id;
        this.username = username;
        this.password = password;
        // The legacy build() added exactly one authority: the role id as a string.
        this.authorities = List.of(new SimpleGrantedAuthority(String.valueOf(roleId)));
    }

    /** Extra accessor used across the substrate to identify the acting user. */
    public Long getId() { return id; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
