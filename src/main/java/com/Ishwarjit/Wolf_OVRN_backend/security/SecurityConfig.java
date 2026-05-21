package com.Ishwarjit.Wolf_OVRN_backend.security;

import com.Ishwarjit.Wolf_OVRN_backend.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.OffsetDateTime;
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

        public SecurityConfig(
                        CustomOAuth2UserService oAuth2UserService,
                        OAuth2SuccessHandler oAuth2SuccessHandler,
                        JwtAuthenticationFilter jwtAuthenticationFilter,
                        CorsConfigurationSource corsConfigurationSource) {
                this.oAuth2UserService = oAuth2UserService;
                this.oAuth2SuccessHandler = oAuth2SuccessHandler;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.corsConfigurationSource = corsConfigurationSource;
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
                                                .requestMatchers("/api/auth/logout", "/api/auth/me").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/categories",
                                                                "/api/categories/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/products/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/products/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.POST, "/api/products/*/images")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/orders").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/orders/*").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/users/*/role").hasRole("ADMIN")
                                                .requestMatchers("/api/cart", "/api/cart/**").authenticated()
                                                .requestMatchers("/api/payments", "/api/payments/**").authenticated()
                                                .anyRequest().authenticated())
                                .oauth2Login(oauth -> oauth
                                                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler))
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
