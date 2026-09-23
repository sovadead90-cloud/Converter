package com.example.fileConversionService.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    public InputStream downloadFile(String objectKey) throws Exception {
        log.info("Запрос на скачивание файла из MinIO: bucket='{}', key='{}'", bucketName, objectKey);
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .build()
        );
    }

    public void uploadFile(String objectKey, byte[] content, String contentType) throws Exception {
        log.info("Загрузка файла в MinIO: bucket='{}', key='{}', size={} байт", bucketName, objectKey, content.length);

        try (InputStream byteArrayInputStream = new ByteArrayInputStream(content)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(byteArrayInputStream, content.length, -1)
                            .contentType(contentType)
                            .build()
            );
        }
        log.info("Файл '{}' успешно загружен в MinIO", objectKey);
    }
}
