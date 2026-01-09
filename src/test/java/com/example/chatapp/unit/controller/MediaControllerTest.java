package com.example.chatapp.unit.controller;

import com.example.chatapp.controller.MediaController;
import com.example.chatapp.dto.UploadResponseDTO;
import com.example.chatapp.service.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MediaControllerTest {
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private MediaController mediaController;

    @Test
    public void test_getUploadURl_returnsOkResponse() {
        UploadResponseDTO uploadResponseDTO = new UploadResponseDTO();
        String fileName = "videofile";
        String contentType = "video/mp4";
        when(s3Service.generatePresignedUrl(fileName, contentType)).thenReturn(uploadResponseDTO);

        ResponseEntity<UploadResponseDTO> result = mediaController.getUploadUrl(fileName, contentType);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(uploadResponseDTO, result.getBody());
    }

    @Test
    public void test_getDownloadURl_returnsOkResponse() {
        String url= "testUrl.com";
        String key = "testKey";
        when(s3Service.generateDownloadPresignedUrl(key)).thenReturn(url);

        ResponseEntity<String> result = mediaController.getDownloadUrl(key);

        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(url, result.getBody());
    }


}
