package com.aria.security;

import com.aria.auth.AuthUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** The logged-in user of the current request, as the server sees it (JWT principal), never from the client. */
public final class CurrentUser {

    private CurrentUser() {}

    /** The acting user's id, or null when there is no authenticated {@link AuthUserDetails} principal. */
    public static Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        return principal instanceof AuthUserDetails u ? u.getId() : null;
    }
}
