package com.be.recheckbe.global.ocr.service;

import org.springframework.web.multipart.MultipartFile;

public interface OcrService {

    int extractPaymentAmount(MultipartFile file);
}