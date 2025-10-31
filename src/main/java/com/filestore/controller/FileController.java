package com.filestore.controller;

import com.filestore.model.dto.FileInfoResponse;
import com.filestore.model.dto.UploadResponse;
import com.filestore.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expiryHours", required = false) Integer expiryHours,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "maxDownloads", required = false) Integer maxDownloads,
            HttpServletRequest request
    ) {
        String uploaderIp = getClientIp(request);

        UploadResponse response = fileService.uploadFile(file, expiryHours, password, maxDownloads, uploaderIp);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/f/{shareToken}/info")
    public ResponseEntity<FileInfoResponse> getFileInfo(@PathVariable String shareToken) {

        FileInfoResponse response = fileService.getFileInfo(shareToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/f/{shareToken}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String shareToken, @RequestParam(value = "password", required = false) String password) {

        FileService.FileDownloadResult result = fileService.downloadFile(shareToken, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(result.mimeType));
        headers.setContentLength(result.fileSize);
        headers.setContentDispositionFormData("attachment", result.fileName);

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(new InputStreamResource(result.inputStream));
    }

    @DeleteMapping("/delete/{deleteToken}")
    public ResponseEntity<Void> deleteFile(@PathVariable String deleteToken) {
        fileService.deleteFile(deleteToken);
        return ResponseEntity.noContent().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
