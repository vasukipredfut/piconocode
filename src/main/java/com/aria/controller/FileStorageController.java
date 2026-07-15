package com.aria.controller;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.aria.service.FileStorageService;
import com.jcraft.jsch.JSchException;

@RestController
@RequestMapping("/api")
public class FileStorageController {

    @Autowired
    FileStorageService fileStorageService;

    private RestTemplate restTemplate;

    public FileStorageController(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    @PostMapping(value = "/file", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,

                                        @RequestParam("attachmentType") String attachmentType) throws IOException, JSchException {

        if (file != null) {
            return fileStorageService.storeDocuments(file, attachmentType);
        } else {
            return new ResponseEntity<String>("Not a valid file", HttpStatus.OK);
        }
    }

    @GetMapping(value = "/file")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> downloadFile(@RequestParam("fileName") String fileName, HttpServletResponse response)
            throws IOException {
        File file = fileStorageService.downloadFile(fileName, response);
        if (file != null) {
            response.setHeader("Content-Type", "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            return new ResponseEntity<>(new InputStreamResource(FileUtils.openInputStream(file)), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
