package com.Ishwarjit.Wolf_OVRN_backend.security;

import com.Ishwarjit.Wolf_OVRN_backend.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomOAuth2UserService oAuth2UserService;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CorsConfigurationSource corsConfigurationSource;
        private final String frontendRedirectUrl;

        public SecurityConfig(
                        CustomOAuth2UserService oAuth2UserService,
                        OAuth2SuccessHandler oAuth2SuccessHandler,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        CorsConfigurationSource corsConfigurationSource,
                        @Value("${app.frontend.redirect-url}") String frontendRedirectUrl) {
                this.oAuth2UserService = oAuth2UserService;
                this.oAuth2SuccessHandler = oAuth2SuccessHandler;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.corsConfigurationSource = corsConfigurationSource;
                this.frontendRedirectUrl = frontendRedirectUrl;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                                                .requestMatchers("/error").permitAll()
                                                .requestMatchers("/actuator/health").permitAll()
                                                .requestMatchers("/api/auth/logout", "/api/auth/me", "/api/auth/refresh").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/categories/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/categories/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/products/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/products/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/products/*/images")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/faqs", "/api/faqs/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/faqs").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/faqs/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/faqs/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/feature-carousel").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/feature-carousel/all").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/feature-carousel").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/feature-carousel/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/feature-carousel/*").hasRole("ADMIN")
                                                // Banner texts — public read, admin write
                                                .requestMatchers(HttpMethod.GET, "/api/banner-texts").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/banner-texts/all").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/banner-texts").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/banner-texts/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/top-products").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/top-products").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/top-products/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/top-products/*").hasRole("ADMIN")
                                                // Drop Events
                                                .requestMatchers(HttpMethod.GET, "/api/drops/upcoming", "/api/drops/live", "/api/drops/previous", "/api/drops/*").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/drops").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/drops").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/drops/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/drops/*").hasRole("ADMIN")
                                                // Product Inquiries
                                                .requestMatchers(HttpMethod.POST, "/api/inquiries").authenticated()
                                                .requestMatchers(HttpMethod.GET, "/api/inquiries", "/api/inquiries/grouped").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/inquiries/*/status").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/inquiries/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/orders", "/api/orders/fulfillment").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/orders/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/users/*/role").hasRole("ADMIN")
                                                .requestMatchers("/api/cart", "/api/cart/**").authenticated()
                                                .requestMatchers("/api/payments", "/api/payments/**").authenticated()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth -> oauth
                                                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler)
                                                .failureHandler((request, response, exception) ->
                                                                response.sendRedirect(frontendRedirectUrl + "signin?error=true")))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint((request, response, authException) -> {
                                                        if (request.getRequestURI().startsWith("/api/")) {
                                                                writeJsonError(response,
                                                                                HttpServletResponse.SC_UNAUTHORIZED,
                                                                                "Unauthorized",
                                                                                "Authentication required");
                                                        } else {
                                                                response.sendRedirect("/oauth2/authorization/google");
                                                        }
                                                })
                                                .accessDeniedHandler((request, response, accessDeniedException) ->
                                                                writeJsonError(response,
                                                                                HttpServletResponse.SC_FORBIDDEN,
                                                                                "Forbidden",
                                                                                "Access denied")))
                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        private static void writeJsonError(
                        HttpServletResponse response, int status, String error, String message) throws java.io.IOException {
                response.setStatus(status);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                String body = "{"
                                + "\"success\":false,"
                                + "\"timestamp\":\"" + OffsetDateTime.now() + "\","
                                + "\"status\":" + status + ","
                                + "\"error\":\"" + escapeJson(error) + "\","
                                + "\"message\":\"" + escapeJson(message) + "\""
                                + "}";
                response.getWriter().write(body);
        }

        private static String escapeJson(String value) {
                if (value == null) {
                        return "";
                }
                return value.replace("\\", "\\\\").replace("\"", "\\\"");
        }
}
