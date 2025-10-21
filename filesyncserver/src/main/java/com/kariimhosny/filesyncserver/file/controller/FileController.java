package com.kariimhosny.filesyncserver.file.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kariimhosny.filesyncserver.common.dto.ApiResponse;
import com.kariimhosny.filesyncserver.file.dto.FileMetadataDTO;
import com.kariimhosny.filesyncserver.file.service.contracts.IFileService;

@RestController
@RequestMapping("api/file")
public class FileController {

    private final IFileService fileService;

    public FileController(IFileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("path")
    public ResponseEntity<ApiResponse<String>> postMethodName(Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Authentication Object: 1234 ");
        System.out.println(username);
        return ResponseEntity.ok(ApiResponse.success(username, "Username retrieved successfully"));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestPart("metadata") FileMetadataDTO metadata) {

        fileService.receiveFile(file, metadata);

        return ResponseEntity.ok(ApiResponse.success(null, "File Uploaded Successfuly"));
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> donwloadFile(@PathVariable("id") Integer fileId) {
        Resource resource = fileService.sendFile(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // binary file
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
