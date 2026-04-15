package com.be.recheckbe.global.ocr.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Getter
@Configuration
public class OcrConfig {

  @Value("${naver.ocr.secret-key}")
  private String secretKey;

  @Value("${naver.ocr.api-url}")
  private String apiUrl;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }
}
