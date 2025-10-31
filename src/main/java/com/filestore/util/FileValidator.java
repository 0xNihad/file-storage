package com.filestore.util;

import com.filestore.exception.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class FileValidator {

    @Value("${filestore.file.max-file-size-bytes}")
    private Long maxFileSize;

    @Value("${filestore.file.allowed-extensions:}")
    private List<String> allowedExtensions;

    public void validateFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileSizeLimitExceededException(
                    String.format("File size (%d bytes) exceeds maximum allowed size (%d bytes)",
                            file.getSize(), maxFileSize)
            );
        }

        if (!allowedExtensions.isEmpty()) {
            String extension = getFileExtension(file.getOriginalFilename());
            if (!allowedExtensions.contains(extension.toLowerCase())) {
                throw new IllegalArgumentException(String.format("File type '.%s' is not allowed. Allowed types: %s",
                        extension, String.join(", ", allowedExtensions))
                );
            }
        }
    }

    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotIndex + 1);
    }

    public String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
