package com.aria.auth;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Substrate-native {@link UserDetailsService} — replaces the legacy
 * {@code UserDetailsServiceImpl}/{@code UserRepository}/{@code User} chain with
 * a single JdbcTemplate read against {@code employee_details} (the login table).
 * Same query the legacy repository used:
 *   SELECT ... FROM employee_details WHERE username=? AND (status='' OR status='A')
 *
 * {@code @Primary} so it wins during the transition while the legacy bean still
 * exists; the legacy chain is deleted once nothing references it.
 */
@Service
@Primary
public class AuthUserDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbc;

    public AuthUserDetailsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Map<String, Object> u = jdbc.queryForMap(
                    "SELECT id, username, password, roles_id FROM employee_details " +
                    "WHERE username = ? AND (status = '' OR status = 'A')",
                    username);
            Long id = ((Number) u.get("id")).longValue();
            Object rid = u.get("roles_id");
            Long roleId = rid == null ? null : ((Number) rid).longValue();
            return new AuthUserDetails(id, (String) u.get("username"), (String) u.get("password"), roleId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }
    }
}
