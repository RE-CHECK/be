package com.be.recheckbe.global.s3.service;

import com.be.recheckbe.global.s3.enums.PathName;
import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

  String uploadFile(PathName pathName, MultipartFile file);

  void deleteFile(String fileUrl);
}
