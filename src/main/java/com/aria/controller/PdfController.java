package com.aria.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aria.utils.Htmltopdf;


@RestController
@RequestMapping("/api")
@CrossOrigin
public class PdfController {

    @Autowired
    Htmltopdf htmltopdf;

    @PostMapping("/generatePDF")
    public ResponseEntity<byte[]> generatePDF(@RequestBody Map<String, String> requestData) {
        String base64Html = requestData.get("encodedHTML");  // Get Base64 encoded HTML from the request
        String fileName = requestData.get("fileName");
        // Return the generated PDF as a byte array
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition", "inline; filename="+fileName+".pdf");
        return new ResponseEntity<>(htmltopdf.generatePdfFromBase64(base64Html), headers, HttpStatus.OK);
    }
}
