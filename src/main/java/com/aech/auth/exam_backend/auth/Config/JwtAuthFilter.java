package com.aech.auth.exam_backend.auth.Config;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aech.auth.exam_backend.auth.Repos.UserRepo;
import com.aech.auth.exam_backend.auth.Services.Impl.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserRepo userRepo;

  private Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
      throws ServletException, IOException {

    String header = req.getHeader("Authorization");
    logger.info("Authorization header  : {}", header);

    if (header != null && header.startsWith("Bearer ")) {

      String token = header.substring(7);

      try {
        if (!jwtService.isAccessToken(token)) {
          filterChain.doFilter(req, res);
          return;
        }

        Jws<Claims> parse = jwtService.parse(token);

        Claims payload = parse.getPayload();

        String userId = payload.getSubject();
        UUID userUid = UUID.fromString(userId);

        userRepo.findById(userUid).ifPresent(user -> {

          if (user.isEnable()) {
            List<GrantedAuthority> authorities = user.getRoles() == null ? List.of()
                : user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName()))
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
              SecurityContextHolder.getContext().setAuthentication(authentication);
            }
          }
        });
      } catch (ExpiredJwtException e) {
        req.setAttribute("error", "Token Expired");

      } catch (Exception e) {
        req.setAttribute("error", "Invalid Token");
      }

    }

    filterChain.doFilter(req, res);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest req) throws ServletException {
    String uri = req.getRequestURI();
    return "/api/v1/auth/login".equals(uri)
        || "/api/v1/auth/register".equals(uri)
        || "/api/v1/auth/refresh".equals(uri)
        || "/api/v1/auth/logout".equals(uri)
        || uri.startsWith("/oauth2/")
        || uri.startsWith("/login/oauth2/");
  }
}
