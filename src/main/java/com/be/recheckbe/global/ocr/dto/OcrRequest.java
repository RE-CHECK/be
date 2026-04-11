package com.be.recheckbe.global.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class OcrRequest {

    private String version;
    private String requestId;
    private long timestamp;
    private List<OcrImageRequest> images;
}