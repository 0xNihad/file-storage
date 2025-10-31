package com.filestore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoResponse {

    private String fileName;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime uploadDate;
    private LocalDateTime expiryDate;
    private Integer downloadCount;
    private Integer maxDownloads;
    private Boolean isPasswordProtected;
    private Boolean isExpired;
}
