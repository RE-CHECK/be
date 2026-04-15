package com.be.recheckbe.global.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OcrImageRequest {

    private String format; // V2만 사용
    private String name;
    private String data; // Base64 인코딩된 이미지 데이터
}