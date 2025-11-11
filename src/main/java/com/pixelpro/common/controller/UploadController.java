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

import java.util.List;

@RestController
@RequestMapping("/api/admin/uploads")
@RequiredArgsConstructor
@Tag(name = "Admin - File Uploads", description = "Handles image and file uploads for products")
public class UploadController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "Upload one or more product images")
    @PostMapping("/products")
    public ResponseEntity<List<String>> uploadProductImages(
            @RequestParam("files") List<MultipartFile> files
    ) {
        List<String> urls = files.stream()
                .map(fileStorageService::uploadProductImage)
                .toList();

        return ResponseEntity.ok(urls);
    }
}
