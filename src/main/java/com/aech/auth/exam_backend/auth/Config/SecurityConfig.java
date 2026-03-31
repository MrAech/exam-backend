package com.aech.auth.exam_backend.auth.Config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.aech.auth.exam_backend.DTOs.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private JwtAuthFilter jwtAuthFilter;
  private AuthenticationSuccessHandler successHandler;

  @Value("${app.auth.frontend.failure-redirect}")
  private String frontEndFailureUrl;
  @Value("${app.security.public-docs-enabled:false}")
  private boolean publicDocsEnabled;

  public SecurityConfig(JwtAuthFilter jwtAuthFilter, AuthenticationSuccessHandler successHandler) {
    this.jwtAuthFilter = jwtAuthFilter;
    this.successHandler = successHandler;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorizeHttpReq -> {
          authorizeHttpReq.requestMatchers(AppConstants.AUTH_PUBLIC_URLS).permitAll();

          if (publicDocsEnabled) {
            authorizeHttpReq.requestMatchers(AppConstants.DOCS_PUBLIC_URLS).permitAll();
          }

          authorizeHttpReq.anyRequest().authenticated();
        })

        .oauth2Login(oauth2 -> oauth2.successHandler(successHandler)
            .failureHandler((req, res, exc) -> res.sendRedirect(frontEndFailureUrl)))
        .logout(AbstractHttpConfigurer::disable)
        .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {

          res.setStatus(401);
          res.setContentType("application/json");

          String message = e.getMessage();
          String error = (String) req.getAttribute("error");

          if (error != null) {
            message = error;
          }

          var apiError = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access", message,
              req.getRequestURI(), true);
          var objectMapper = new ObjectMapper();
          res.getWriter().write(objectMapper.writeValueAsString(apiError));
        }).accessDeniedHandler((req, res, e) -> {

          res.setStatus(403);
          res.setContentType("application/json");

          String message = e.getMessage();
          String error = (String) req.getAttribute("error");

          if (error != null) {
            message = error;
          }

          var apiError = ApiError.of(HttpStatus.FORBIDDEN.value(), "Forbidden Access", message, req.getRequestURI(),
              true);
          var objectMapper = new ObjectMapper();
          res.getWriter().write(objectMapper.writeValueAsString(apiError));

        })).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();

  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.front-end-url}") String corsUrls) {

    String[] urls = corsUrls.trim().split(",");

    var config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList(urls));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;

  }

}
