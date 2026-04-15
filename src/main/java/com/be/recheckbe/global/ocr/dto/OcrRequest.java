package com.be.recheckbe.global.ocr.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OcrRequest {

  private String version; // V2만 사용
  private String requestId; // UUID
  private long timestamp; // api 호출 시각
  private List<OcrImageRequest> images;
}
