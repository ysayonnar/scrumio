package com.example.scrumio.auth;

import com.example.scrumio.entity.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

public final class AuthContext {

    public static final String USER_ID_ATTR = "auth.user_id";
    public static final String ROLE_ATTR = "auth.role";

    private AuthContext() {}

    public static UUID getUserId() {
        return (UUID) getRequest().getAttribute(USER_ID_ATTR);
    }

    public static UserRole getRole() {
        return (UserRole) getRequest().getAttribute(ROLE_ATTR);
    }

    private static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }
}
