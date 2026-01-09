package com.example.chatapp.controller;

import com.example.chatapp.dto.UploadResponseDTO;
import com.example.chatapp.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {
    private final S3Service s3Service;

    public MediaController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload-url")
    public ResponseEntity<UploadResponseDTO> getUploadUrl(@RequestParam String fileName,
                                                          @RequestParam String contentType) {
        UploadResponseDTO response = s3Service.generatePresignedUrl(fileName, contentType);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<String> getDownloadUrl(String key) {
        String url = s3Service.generateDownloadPresignedUrl(key);
        return ResponseEntity.ok(url);
    }
}
