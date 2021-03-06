package com.vgrokh.controller;

import com.vgrokh.service.RequestValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.vgrokh.service.FileStorageService;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/zip")
public class RequestHandlingController {

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private RequestValidationService requestValidationService;


    @PostMapping("/file/upload")
    public FileUploadResponse singleFileUpload(@RequestParam MultipartFile file){
        if (requestValidationService.isIncomingFileValid(file)) {
            String fileName = fileStorageService.storeFile(file);
            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/download")
                    .path(fileName)
                    .toUriString();
            String contentType = file.getContentType();
            return new FileUploadResponse(fileName, contentType, url);
        }
        return new FileUploadResponse();
    }
}
