package com.be.recheckbe.global.security;

import com.be.recheckbe.domain.auth.exception.AuthErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {
    AuthErrorCode errorCode = AuthErrorCode.AUTHENTICATION_NOT_FOUND;

    log.warn("인증되지 않은 요청: {} {}", request.getMethod(), request.getRequestURI());

    response.setStatus(errorCode.getStatus().value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    Map<String, Object> body =
        Map.of(
            "success", false,
            "code", errorCode.getStatus().value(),
            "message", errorCode.getMessage(),
            "data", Map.of());

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
