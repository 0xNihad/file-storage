package com.filestore.model.dto;

import lombok.Data;

@Data
public class UploadRequest {

    private Integer expiryHours;
    private String password;
    private Integer maxDownloads;
}