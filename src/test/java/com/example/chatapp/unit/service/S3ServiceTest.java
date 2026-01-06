package com.example.chatapp.unit.service;

import com.example.chatapp.service.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {

    @Mock
    private S3Presigner presigner;

    @InjectMocks
    private S3Service s3Service;

    @Test
    public void test_generatePresignedUrl_returnsPresignedUrl() throws Exception {
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");

        // Mock the presigner response
        PresignedPutObjectRequest mockResponse = mock(PresignedPutObjectRequest.class);
        when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockResponse);
        when(mockResponse.url()).thenReturn(new URL("https://test-bucket.s3.eu-west-1.amazonaws.com/test"));

        String result = s3Service.generatePresignedUrl("test.jpg", "image/jpeg");

        assertEquals("https://test-bucket.s3.eu-west-1.amazonaws.com/test", result);
    }
}
