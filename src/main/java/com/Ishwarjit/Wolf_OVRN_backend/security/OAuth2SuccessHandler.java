package com.Ishwarjit.Wolf_OVRN_backend.security;

import com.Ishwarjit.Wolf_OVRN_backend.entity.User;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.repository.UserRepository;
import com.Ishwarjit.Wolf_OVRN_backend.service.CustomOAuth2UserService;
import com.Ishwarjit.Wolf_OVRN_backend.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final String frontendRedirectUrl;
    private final boolean cookieSecure;
    private final String cookieDomain;

    public OAuth2SuccessHandler(
            JwtService jwtService,
            UserRepository userRepository,
            @Value("${app.frontend.redirect-url}") String frontendRedirectUrl,
            @Value("${app.cookie.secure}") boolean cookieSecure,
            @Value("${app.cookie.domain:}") String cookieDomain) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.frontendRedirectUrl = frontendRedirectUrl;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String userIdStr = principal.getAttribute(CustomOAuth2UserService.ATTR_USER_ID);
        if (userIdStr == null) {
            throw new ResourceNotFoundException("Authenticated user is missing app user id");
        }
        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found after OAuth login"));

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Cookie cookie = new Cookie(JwtAuthenticationFilter.COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            cookie.setDomain(cookieDomain);
        }
        cookie.setMaxAge((int) (jwtService.getExpirationMs() / 1000));
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(cookieSecure);
        refreshCookie.setPath("/api/auth/refresh");
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            refreshCookie.setDomain(cookieDomain);
        }
        refreshCookie.setMaxAge((int) (jwtService.getRefreshExpirationMs() / 1000));
        refreshCookie.setAttribute("SameSite", "Lax");
        response.addCookie(refreshCookie);

        // Append ?auth=1 so the frontend knows to call /api/auth/refresh
        // immediately — avoids the race condition where cookies are not yet
        // committed by the time the JS on the landing page runs.
        String redirectTarget = frontendRedirectUrl.endsWith("/")
                ? frontendRedirectUrl + "?auth=1"
                : frontendRedirectUrl + "/?auth=1";
        getRedirectStrategy().sendRedirect(request, response, redirectTarget);
    }
}

