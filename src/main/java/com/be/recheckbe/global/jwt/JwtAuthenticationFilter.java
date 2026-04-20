package com.be.recheckbe.global.jwt;

import com.be.recheckbe.domain.auth.exception.AuthErrorCode;
import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.security.CustomUserDetailService;
import com.be.recheckbe.global.security.CustomUserDetails;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtProvider jwtProvider;
  private final CustomUserDetailService customUserDetailService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      String token = resolveToken(request);

      if (token != null) {
        try {
          if (jwtProvider.validateToken(token)) {
            setAuthentication(token);
          }
        } catch (CustomException e) {
          if (e.getErrorCode() == AuthErrorCode.JWT_TOKEN_EXPIRED) {
            if (!handleExpiredToken(token, response)) {
              return;
            }
          } else {
            throw e;
          }
        }
      }
    } catch (CustomException | JwtException | IllegalArgumentException e) {
      log.error("JWT 검증 실패: {}", e.getMessage());
      SecurityContextHolder.clearContext();
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
      return;
    }
    filterChain.doFilter(request, response);
  }

  private void setAuthentication(String token) {
    Long userId = jwtProvider.extractUserId(token);
    CustomUserDetails userDetails = (CustomUserDetails) customUserDetailService.loadUserById(userId);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
    log.debug("SecurityContext에 '{}' 인증 정보를 저장했습니다.", userId);
  }

  /**
   * 만료된 Access Token 수신 시 DB에 저장된 Refresh Token을 검증하여 새 Access Token을 발급한다.
   * @return 재발급 성공 여부
   */
  private boolean handleExpiredToken(String expiredToken, HttpServletResponse response)
      throws IOException {
    try {
      Long userId = jwtProvider.extractUserIdFromExpiredToken(expiredToken);
      CustomUserDetails userDetails =
          (CustomUserDetails) customUserDetailService.loadUserById(userId);
      String storedRefreshToken = userDetails.getUser().getRefreshToken();

      if (storedRefreshToken == null) {
        log.warn("Refresh Token이 존재하지 않습니다. userId={}", userId);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Refresh token not found");
        return false;
      }

      jwtProvider.validateToken(storedRefreshToken);

      String newAccessToken = jwtProvider.createAccessToken(userId);
      response.setHeader(AUTHORIZATION_HEADER, BEARER_PREFIX + newAccessToken);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);

      log.debug("Access Token 재발급 완료. userId={}", userId);
      return true;

    } catch (CustomException e) {
      log.warn("Refresh Token 만료 또는 유효하지 않음: {}", e.getMessage());
      SecurityContextHolder.clearContext();
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Refresh token expired");
      return false;
    } catch (Exception e) {
      log.error("Access Token 재발급 실패: {}", e.getMessage());
      SecurityContextHolder.clearContext();
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token reissue failed");
      return false;
    }
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }
}
