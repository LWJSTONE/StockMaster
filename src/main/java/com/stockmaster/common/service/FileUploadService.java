package com.stockmaster.common.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {

    String uploadAvatar(MultipartFile file);

    String uploadProductImage(MultipartFile file);

    String uploadFile(MultipartFile file, String directory);
}
