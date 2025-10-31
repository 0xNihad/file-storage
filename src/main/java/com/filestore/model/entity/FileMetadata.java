package com.filestore.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name  = "files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private String storageKey;

    @Column(unique = true, nullable = false)
    private String shareToken;

    @Column(unique = true, nullable = false)
    private String deleteToken;

    @Column
    private String passwordHash;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer downloadCount = 0;

    @Column
    private Integer maxDownloads;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column
    private String uploaderIp;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isDownloadLimitReached() {
        return maxDownloads != null && downloadCount >= maxDownloads;
    }

    public boolean isPasswordProtected() {
        return passwordHash != null && !passwordHash.isEmpty();
    }
}
