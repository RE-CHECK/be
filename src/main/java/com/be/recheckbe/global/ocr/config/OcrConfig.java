package com.be.recheckbe.global.ocr.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3_000); // Naver server tcp 연결 타임아웃 3초
    factory.setReadTimeout(5_000); // 읽기 타임아웃 5초 (평균 응답시간: 2.41초)
    return new RestTemplate(factory);
  }
}
