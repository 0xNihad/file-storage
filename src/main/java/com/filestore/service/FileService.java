package com.filestore.service;

import com.filestore.exception.FileExpiredException;
import com.filestore.exception.FileNotFoundException;
import com.filestore.exception.InvalidPasswordException;
import com.filestore.model.dto.FileInfoResponse;
import com.filestore.model.dto.UploadResponse;
import com.filestore.model.entity.FileMetadata;
import com.filestore.repository.FileMetadataRepository;
import com.filestore.util.FileValidator;
import com.filestore.util.PasswordUtil;
import com.filestore.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final StorageService storageService;
    private final TokenGenerator tokenGenerator;
    private final FileValidator fileValidator;
    private final PasswordUtil passwordUtil;

    @Value("${filestore.file.default-expiry-hours}")
    private Integer defaultExpiryHours;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Transactional
    public UploadResponse uploadFile(
            MultipartFile file,
            Integer expiryHours,
            String password,
            Integer maxDownloads,
            String uploaderIp
    ) {
        fileValidator.validateFile(file);

        String shareToken = tokenGenerator.generateShareToken();
        String deleteToken = tokenGenerator.generateDeleteToken();
        String fileExtension = fileValidator.getFileExtension(file.getOriginalFilename());
        String storageFileName = tokenGenerator.generateStorageFileName(fileExtension);
        String storageKey = "uploads/" + storageFileName;

        storageService.uploadFile(file, storageKey);

        String passwordHash = password != null && !password.isEmpty() ? passwordUtil.hashPassword(password) : null;

        int hoursUntilExpiry = expiryHours != null ? expiryHours : defaultExpiryHours;
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(hoursUntilExpiry);

        FileMetadata metadata = FileMetadata.builder()
                .originalFileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .storageKey(storageKey)
                .shareToken(shareToken)
                .deleteToken(deleteToken)
                .passwordHash(passwordHash)
                .uploadDate(LocalDateTime.now())
                .expiryDate(expiryDate)
                .downloadCount(0)
                .maxDownloads(maxDownloads)
                .isDeleted(false)
                .uploaderIp(uploaderIp)
                .build();

        fileMetadataRepository.save(metadata);

        log.info("File uploaded successfully: {} (shareToken: {})", file.getOriginalFilename(), shareToken);

        return UploadResponse.builder()
                .fileId(metadata.getId().toString())
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .shareUrl(contextPath + "/f/" + shareToken)
                .deleteUrl(contextPath + "/delete/" + deleteToken)
                .expiresAt(expiryDate)
                .maxDownloads(maxDownloads)
                .build();
    }

    public FileInfoResponse getFileInfo(String shareToken) {
        FileMetadata metadata = findFileByShareToken(shareToken);

        return FileInfoResponse.builder()
                .fileName(metadata.getOriginalFileName())
                .fileSize(metadata.getFileSize())
                .mimeType(metadata.getMimeType())
                .uploadDate(metadata.getUploadDate())
                .expiryDate(metadata.getExpiryDate())
                .downloadCount(metadata.getDownloadCount())
                .maxDownloads(metadata.getMaxDownloads())
                .isPasswordProtected(metadata.isPasswordProtected())
                .isExpired(metadata.isExpired())
                .build();
    }

    @Transactional
    public FileDownloadResult downloadFile(String shareToken, String password) {
        FileMetadata metadata = findFileByShareToken(shareToken);

        if (metadata.isExpired()) {
            throw new FileExpiredException("File has expired and is no longer available");
        }

        if (metadata.isDownloadLimitReached()) {
            throw new FileExpiredException("Download limit reached for this file");
        }

        if (metadata.isPasswordProtected()) {
            if (password == null || password.isEmpty()) {
                throw new InvalidPasswordException("Password is required");
            }
            if (!passwordUtil.verifyPassword(password, metadata.getPasswordHash())) {
                throw new InvalidPasswordException("Invalid password");
            }
        }

        metadata.setDownloadCount(metadata.getDownloadCount() + 1);
        fileMetadataRepository.save(metadata);

        InputStream fileStream = storageService.downloadFile(metadata.getStorageKey());

        log.info("File downloaded: {} (shareToken: {}, downloadCount: {})",
                metadata.getOriginalFileName(), shareToken, metadata.getDownloadCount());

        return new FileDownloadResult(
                fileStream,
                metadata.getOriginalFileName(),
                metadata.getMimeType(),
                metadata.getFileSize()
        );
    }

    @Transactional
    public void deleteFile(String deleteToken) {
        FileMetadata metadata = fileMetadataRepository.findByDeleteToken(deleteToken)
                .orElseThrow(() -> new FileNotFoundException("File not found"));

        if (metadata.getIsDeleted()) {
            throw new FileNotFoundException("File has already been deleted");
        }

        storageService.deleteFile(metadata.getStorageKey());

        metadata.setIsDeleted(true);
        fileMetadataRepository.save(metadata);

        log.info("File deleted: {} (deleteToken: {})", metadata.getOriginalFileName(), deleteToken);
    }

    private FileMetadata findFileByShareToken(String shareToken) {
        FileMetadata metadata = fileMetadataRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new FileNotFoundException("File not found"));

        if (metadata.getIsDeleted()) {
            throw new FileNotFoundException("File has been deleted");
        }

        return metadata;
    }

    public static class FileDownloadResult {
        public final InputStream inputStream;
        public final String fileName;
        public final String mimeType;
        public final Long fileSize;

        public FileDownloadResult(InputStream inputStream, String fileName, String mimeType, Long fileSize) {
            this.inputStream = inputStream;
            this.fileName = fileName;
            this.mimeType = mimeType;
            this.fileSize = fileSize;
        }
    }
}
