package com.aria.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * Allow-list check for files that end up under /uploads/**, which is served publicly from the app's
 * own origin. Without it an uploaded .html / .svg / .js runs as the app (stored XSS) for anyone
 * who opens the link -- the profile photo and document-vault endpoints accepted any file type.
 *
 * <p>The decision uses the file's first bytes (magic number), not its name or the client's
 * Content-Type, and returns the extension the file must be stored with.
 */
public final class UploadGuard {

    public static final Set<String> IMAGES = Set.of("jpg", "png", "webp");
    public static final Set<String> IMAGES_AND_PDF = Set.of("jpg", "png", "webp", "pdf");
    public static final long MAX_BYTES = 10L * 1024 * 1024;

    private UploadGuard() {}

    /**
     * Extension to store the file with ("jpg" / "png" / "webp" / "pdf"), or IllegalArgumentException
     * when it is empty, too large, or not one of {@code allowed}.
     */
    public static String check(MultipartFile file, Set<String> allowed) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("no file");
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("file is larger than 10 MB");
        byte[] head = new byte[12];
        int n;
        try (InputStream in = file.getInputStream()) {
            n = in.readNBytes(head, 0, head.length);
        }
        String kind = sniff(Arrays.copyOf(head, n));
        if (kind == null || !allowed.contains(kind)) {
            throw new IllegalArgumentException("Only " + String.join(", ", allowed).toUpperCase(Locale.ROOT) + " files are accepted");
        }
        return kind;
    }

    static String sniff(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "jpg";
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') return "png";
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') return "webp";
        if (b.length >= 5 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-') return "pdf";
        return null;
    }

    /** Stored file name: caller-chosen safe prefix + the sniffed extension (the client's name is not trusted). */
    public static String storedName(String prefix, String kind) {
        return prefix.replaceAll("[^A-Za-z0-9_-]", "_") + "." + kind;
    }
}
