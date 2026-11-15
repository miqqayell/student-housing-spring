package com.example.refactored_student_housing.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * URL-level authorization lives here; the record-level rule ("only the owning
 * advisor or an admin may change this record") is enforced by
 * {@code @PreAuthorize} on the service layer, which is why method security is
 * enabled below. Both the MVC pages and the Web API therefore go through the
 * exact same check.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // Origin of the separate Client project (webpack dev server). Week 10.
    private static final String CLIENT_ORIGIN = "http://localhost:9000";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS using the corsConfigurationSource bean below.
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        // CSRF is disabled ONLY for the unit-creation endpoint so the separate
                        // Client project (no Spring session / no CSRF token) can POST to it.
                        .ignoringRequestMatchers(
                                AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/units")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        // Order matters: admin-only routes are declared FIRST so a more
                        // permissive pattern below (e.g. /students/{id}) cannot match them.
                        .requestMatchers("/units/add", "/units/*/delete", "/units/*/edit").hasRole("ADMIN")
                        // Week 12: admin-only bulk CSV import (must be before /students/{id}).
                        .requestMatchers("/students/upload").hasRole("ADMIN")

                        .requestMatchers("/", "/home", "/campus-life", "/login", "/register",
                                          // Week 11: bundled frontend assets (webpack output)
                                          "/built/**", "/css/**", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET, "/students", "/students/{id}", "/units", "/units/{id}",
                                          "/api/students/{id}").permitAll()
                        // Public search endpoint consumed by the Client project.
                        .requestMatchers(HttpMethod.GET, "/api/students").permitAll()
                        // permitAll is used here so the separate Client project can test the
                        // "Add" feature without authenticating. (Week 10 client-testing endpoint.)
                        .requestMatchers(HttpMethod.POST, "/api/units").permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login").defaultSuccessUrl("/home", false).permitAll()
                )
                .logout(logout -> logout.logoutSuccessUrl("/home").permitAll());
        return http.build();
    }

    /**
     * CORS: allow only the Client application's origin (the webpack dev server).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(CLIENT_ORIGIN));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        // The Location header of a 201 response is only readable cross-origin
        // when it is explicitly exposed.
        config.setExposedHeaders(List.of("Location"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
