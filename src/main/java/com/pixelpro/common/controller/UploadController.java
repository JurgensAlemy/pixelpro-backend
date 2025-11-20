package com.pixelpro.common.controller;

import com.pixelpro.common.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
@Tag(name = "Admin - File Uploads", description = "Handles image and file uploads for products")
public class UploadController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "Upload one or more product images")
    @PostMapping("/products")
    public ResponseEntity<String> uploadProductImages(
            @RequestParam("file") MultipartFile file
    ) {
        String url = fileStorageService.uploadProductImage(file);

        return ResponseEntity.ok(url);
    }
}
