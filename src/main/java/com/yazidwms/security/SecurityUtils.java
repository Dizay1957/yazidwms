package com.yazidwms.security;

import com.yazidwms.user.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static User currentUserOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.user();
        }
        return null;
    }

    public static Long currentUserIdOrNull() {
        var user = currentUserOrNull();
        return user == null ? null : user.getId();
    }
}
