package com.filestore.repository;

import com.filestore.model.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    Optional<FileMetadata> findByShareToken(String shareToken);

    Optional<FileMetadata> findByDeleteToken(String deleteToken);

    @Query("SELECT f FROM FileMetadata f WHERE f.expiryDate < :now AND f.isDeleted = false")
    List<FileMetadata> findByExpiredFiles(LocalDateTime now);

    @Query("SELECT f FROM FileMetadata f WHERE f.uploaderIp = :ip AND f.uploadDate > :since")
    List<FileMetadata> findRecentFilesByIp(String ip, LocalDateTime since);

    long countByisDeletedFalse();

    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileMetadata f WHERE f.isDeleted = false")
    Long calculateTotalStorageUsed();
}
