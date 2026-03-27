package com.stockmaster.common.service.impl;

import com.stockmaster.common.exception.BusinessException;
import com.stockmaster.common.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Value("${file.upload.avatar.max-size:2097152}")
    private long avatarMaxSize;

    @Value("${file.upload.image.max-size:5242880}")
    private long imageMaxSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Override
    public String uploadAvatar(MultipartFile file) {
        validateImageFile(file, avatarMaxSize);
        String directory = "avatars/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return uploadFile(file, directory);
    }

    @Override
    public String uploadProductImage(MultipartFile file) {
        validateImageFile(file, imageMaxSize);
        String directory = "products/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return uploadFile(file, directory);
    }

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        try {
            Path uploadDir = Paths.get(uploadPath, directory);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(newFilename);

            file.transferTo(filePath.toFile());

            return "/uploads/" + directory + "/" + newFilename;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }

    private void validateImageFile(MultipartFile file, long maxSize) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小超过限制");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("不支持的文件类型");
        }
    }
}
