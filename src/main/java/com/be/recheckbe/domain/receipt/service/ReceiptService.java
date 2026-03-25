package com.be.recheckbe.domain.receipt.service;

import org.springframework.web.multipart.MultipartFile;

public interface ReceiptService {

    String uploadReceiptImage(Long userId, MultipartFile image);
}
