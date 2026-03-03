package com.example.scrumio.auth;

import com.example.scrumio.web.exception.ServiceUnavailableException;
import com.example.scrumio.web.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthClient authClient;

    public AuthInterceptor(AuthClient authClient) {
        this.authClient = authClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        boolean required = handlerMethod.hasMethodAnnotation(RequireAuth.class)
                || handlerMethod.getBeanType().isAnnotationPresent(RequireAuth.class);

        if (!required) {
            return true;
        }

        try {
            String cookieHeader = request.getCookies() == null ? ""
                    : Arrays.stream(request.getCookies())
                            .map(c -> c.getName() + "=" + c.getValue())
                            .collect(Collectors.joining("; "));
            AuthValidationResponse auth = authClient.authenticate(cookieHeader);
            request.setAttribute(AuthContext.USER_ID_ATTR, auth.userId());
            request.setAttribute(AuthContext.ROLE_ATTR, auth.role());
            return true;
        } catch (HttpClientErrorException _) {
            throw new UnauthorizedException("Unauthorized");
        } catch (Exception _) {
            throw new ServiceUnavailableException("Authentication service is unavailable");
        }
    }
}
