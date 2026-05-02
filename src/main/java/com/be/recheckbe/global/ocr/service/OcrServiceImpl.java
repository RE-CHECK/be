package com.be.recheckbe.global.ocr.service;

import com.be.recheckbe.global.exception.CustomException;
import com.be.recheckbe.global.ocr.config.OcrConfig;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import com.be.recheckbe.global.ocr.dto.OcrExtractedData;
import com.be.recheckbe.global.ocr.dto.OcrImageRequest;
import com.be.recheckbe.global.ocr.dto.OcrRequest;
import com.be.recheckbe.global.ocr.dto.OcrResponse;
import com.be.recheckbe.global.ocr.exception.OcrErrorCode;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrServiceImpl implements OcrService {

  private final RestTemplate restTemplate;
  private final OcrConfig ocrConfig;
  private final CircuitBreaker ocrCircuitBreaker;

  @Override
  public OcrExtractedData extractReceiptData(MultipartFile file) {
    String base64Image = encodeToBase64(file);
    String format = extractFormat(file.getOriginalFilename());

    OcrRequest request =
        OcrRequest.builder()
            .version("V2")
            .requestId(UUID.randomUUID().toString())
            .timestamp(System.currentTimeMillis())
            .images(
                List.of(
                    OcrImageRequest.builder()
                        .format(format)
                        .data(base64Image)
                        .name("receipt")
                        .build()))
            .build();

    // ocr호출
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("X-OCR-SECRET", ocrConfig.getSecretKey());

    OcrResponse response;
    try {
      response =
          ocrCircuitBreaker.executeSupplier(
              () -> {
                try {
                  return restTemplate.postForObject(
                      ocrConfig.getApiUrl(), new HttpEntity<>(request, headers), OcrResponse.class);
                } catch (RestClientException e) {
                  log.error("OCR API 요청 실패: {}", e.getMessage());
                  throw new CustomException(OcrErrorCode.OCR_REQUEST_FAILED);
                }
              });
    } catch (CallNotPermittedException e) {
      log.warn("[OCR Circuit] OPEN - 요청 차단");
      throw new CustomException(OcrErrorCode.OCR_CIRCUIT_OPEN);
    }

    return parseReceiptData(response);
  }

  private String encodeToBase64(MultipartFile file) {
    try {
      return Base64.getEncoder().encodeToString(file.getBytes());
    } catch (IOException e) {
      log.error("이미지 Base64 인코딩 실패: {}", e.getMessage());
      throw new CustomException(OcrErrorCode.OCR_IMAGE_ENCODE_FAILED);
    }
  }

  private String extractFormat(String originalFilename) {
    if (originalFilename == null || !originalFilename.contains(".")) {
      return "jpg";
    }
    return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
  }

  private OcrExtractedData parseReceiptData(OcrResponse response) {
    if (response == null || response.getImages() == null || response.getImages().isEmpty()) {
      throw new CustomException(OcrErrorCode.OCR_REQUEST_FAILED);
    }

    OcrResponse.ImageResult imageResult = response.getImages().get(0);

    if (!"SUCCESS".equals(imageResult.getInferResult())) {
      log.warn("OCR 인식 결과: {}", imageResult.getInferResult());
      throw new CustomException(OcrErrorCode.OCR_INFER_FAILED);
    }

    OcrResponse.ReceiptResult result = imageResult.getReceipt().getResult();

    int paymentAmount = parsePaymentAmount(result);
    String storeName = extractText(result.getStoreInfo(), OcrResponse.StoreInfo::getName);
    String cardCompany = extractCardCompany(result);
    String confirmNum = extractConfirmNum(result);

    return OcrExtractedData.builder()
        .storeName(storeName)
        .paymentAmount(paymentAmount)
        .cardCompany(cardCompany)
        .confirmNum(confirmNum)
        .build();
  }

  private int parsePaymentAmount(OcrResponse.ReceiptResult result) {
    String text = null;
    try {
      text = result.getTotalPrice().getPrice().getText();
    } catch (NullPointerException e) {
      // totalPrice 또는 price 필드 없음
    }

    if (text == null || text.isBlank()) {
      log.warn("결제금액 추출 실패: totalPrice.price.text가 null");
      throw new CustomException(OcrErrorCode.OCR_PAYMENT_NOT_FOUND);
    }

    try {
      return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    } catch (NumberFormatException e) {
      log.warn("결제금액 파싱 실패: text={}", text);
      throw new CustomException(OcrErrorCode.OCR_PAYMENT_NOT_FOUND);
    }
  }

  private <T> String extractText(
      T obj, java.util.function.Function<T, OcrResponse.TextField> fieldExtractor) {
    if (obj == null) return null;
    OcrResponse.TextField textField = fieldExtractor.apply(obj);
    if (textField == null) return null;
    return textField.getText();
  }

  private String extractCardCompany(OcrResponse.ReceiptResult result) {
    try {
      return result.getPaymentInfo().getCardInfo().getCompany().getText();
    } catch (NullPointerException e) {
      return null;
    }
  }

  private String extractConfirmNum(OcrResponse.ReceiptResult result) {
    try {
      return result.getPaymentInfo().getConfirmNum().getText();
    } catch (NullPointerException e) {
      return null;
    }
  }
}
