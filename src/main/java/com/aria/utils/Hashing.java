package com.aria.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.text.StyledEditorKit.BoldAction;

public class Hashing {

	
    public static String hash(String field1, String field2, Boolean field3) throws NoSuchAlgorithmException {
        String data = field1 + field2 + field3; // Concatenate the three fields
        MessageDigest digest = MessageDigest.getInstance("SHA-256"); // Use SHA-256 hashing algorithm
        byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8)); // Hash the concatenated string
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b)); // Convert the hash bytes to hexadecimal format
        }
        return sb.toString(); // Return the hash as a string
    }

}
