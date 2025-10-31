package com.filestore.service;

import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@Slf4j
public class StorageService {

    @Value("${filestore.storage.minio.endpoint}")
    private String minioEndpoint;

    @Value("${filestore.storage.minio.access-key}")
    private String accessKey;

    @Value("${filestore.storage.minio.secret-key}")
    private String secretKey;

    @Value("${filestore.storage.minio.bucket-name}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(minioEndpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );

                log.info("Created MinIO bucket {}", bucketName);
            } else {
                log.info("MinIO bucket already exists {}", bucketName);
            }

        } catch (Exception e) {
            log.error("Error while initializing MinIO service", e);
            throw new RuntimeException("Failed to initialize storage service" ,e);
        }
    }

    public String uploadFile(MultipartFile file, String storageKey) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("Uploaded file {}", storageKey);
            return storageKey;

        } catch (Exception e) {
            log.error("Error while uploading file to MinIO service", e);
            throw new RuntimeException("Error while uploading file to MinIO service" ,e);
        }
    }

    public InputStream downloadFile(String storageKey) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
        } catch (Exception e) {
            log.info("Error while downloading file from MinIO service", e);
            throw new RuntimeException("Error while downloading file from MinIO service" ,e);
        }
    }

    public void deleteFile(String storageKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
        } catch (Exception e) {
            log.info("Error while deleting file from MinIO service", e);
            throw new RuntimeException("Error while deleting file from MinIO service" ,e);
        }
    }

    public boolean fileExists(String storageKey) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
