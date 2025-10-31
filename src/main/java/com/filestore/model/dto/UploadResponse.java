package com.filestore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {

    private String fileId;
    private String fileName;
    private Long fileSize;
    private String shareUrl;
    private String deleteUrl;
    private LocalDateTime expiresAt;
    private Integer maxDownloads;
}
