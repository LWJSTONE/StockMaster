package com.stockmaster.common.controller;

import com.stockmaster.common.dto.ApiResponse;
import com.stockmaster.common.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadAvatar(file);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return ApiResponse.success(result);
    }

    @PostMapping("/product")
    public ApiResponse<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadProductImage(file);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return ApiResponse.success(result);
    }

    @PostMapping("/general")
    public ApiResponse<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "general") String type) {
        String url = fileUploadService.uploadFile(file, type);
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return ApiResponse.success(result);
    }
}
