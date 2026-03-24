package com.be.recheckbe.domain.auth.service;

import com.be.recheckbe.domain.auth.dto.RegisterRequest;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    void checkUsername(String username);

    void register(RegisterRequest request, MultipartFile studentCardImage);
}