package com.aria.document;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Map;

/**
 * Document vault upload.
 *
 *  POST /api/m0/document-vault/upload
 *  multipart fields:
 *    file        — the file
 *    doc_no      — unique no (auto if blank)
 *    doc_category, doc_type, title, fy, period, tags, access_level
 *
 * Returns the metadata row inserted into m0_document_vault.
 */
@RestController
@RequestMapping("/api/m0/document-vault")
public class DocumentUploadController {

    private final JdbcTemplate jdbc;
    private final String storageDir;

    public DocumentUploadController(JdbcTemplate jdbc,
                                     @org.springframework.beans.factory.annotation.Value("${earms.document-storage:/tmp/earms-docs}") String storageDir) {
        this.jdbc = jdbc;
        this.storageDir = storageDir;
        new File(storageDir).mkdirs();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam(required = false) String doc_no,
                                       @RequestParam(required = false) String doc_category,
                                       @RequestParam(required = false) String doc_type,
                                       @RequestParam(required = false) String title,
                                       @RequestParam(required = false) String fy,
                                       @RequestParam(required = false) String period,
                                       @RequestParam(required = false) String tags,
                                       @RequestParam(required = false) String access_level) {
        try {
            if (file == null || file.isEmpty())
                return Map.of("ok", false, "error", "no file");

            // /uploads/** is public on the app's own origin: only images / PDF, stored with the
            // sniffed extension. The client's file name is kept only as the display title.
            String kind = com.aria.utils.UploadGuard.check(file, com.aria.utils.UploadGuard.IMAGES_AND_PDF);
            String safeName = (file.getOriginalFilename() == null ? "file"
                : file.getOriginalFilename().replaceAll("[^A-Za-z0-9._-]", "_"));
            String dn = doc_no == null || doc_no.isBlank()
                ? "DOC-" + System.currentTimeMillis() : doc_no.replaceAll("[^A-Za-z0-9_-]", "_");
            String storedName = com.aria.utils.UploadGuard.storedName(dn, kind);
            File target = new File(storageDir, storedName);
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(file.getBytes());
            }

            byte[] hashBytes = MessageDigest.getInstance("SHA-256").digest(file.getBytes());
            String sha256 = HexFormat.of().formatHex(hashBytes);

            jdbc.update(
                "INSERT INTO m0_document_vault (tenant_id, doc_no, doc_category, doc_type, title, " +
                "document_url, document_size_kb, document_hash, issued_at, fy, period, tags, access_level, archived) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                com.aria.security.TenantContext.get(),
                dn,
                doc_category == null ? "POLICY" : doc_category,
                doc_type == null ? "MISC" : doc_type,
                title == null ? safeName : title,
                "/uploads/" + storedName,
                (int) (file.getSize() / 1024),
                sha256,
                LocalDate.now().toString(),
                fy, period, tags,
                access_level == null ? "INTERNAL" : access_level);

            return Map.of("ok", true, "doc_no", dn, "url", "/uploads/" + storedName,
                          "size_kb", file.getSize() / 1024, "sha256", sha256);
        } catch (IllegalArgumentException bad) {
            return Map.of("ok", false, "error", bad.getMessage());
        } catch (Exception e) {
            return Map.of("ok", false, "error", e.getMessage());
        }
    }
}
