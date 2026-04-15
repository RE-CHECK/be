package com.be.recheckbe.global.ocr.service;

import com.be.recheckbe.global.ocr.dto.OcrExtractedData;
import org.springframework.web.multipart.MultipartFile;

public interface OcrService {

  OcrExtractedData extractReceiptData(MultipartFile file);
}
