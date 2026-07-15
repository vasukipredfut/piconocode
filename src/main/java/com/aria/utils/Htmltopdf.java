package com.aria.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings;
import org.springframework.stereotype.Component;
import org.jsoup.nodes.Entities;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class Htmltopdf {
	
	public byte[] generatePdfFromBase64(String base64HtmlContent) {

        try {
            // Step 1: Decode Base64 to HTML string
            byte[] decodedHtmlBytes = Base64.getDecoder().decode(base64HtmlContent);
            String htmlContent = new String(decodedHtmlBytes, StandardCharsets.UTF_8);

            // Step 2: Use JSoup to parse and clean up the HTML
            Document jsoupDoc = Jsoup.parse(htmlContent, "UTF-8");

            // Step 3: Convert the HTML to valid XHTML
            OutputSettings settings = new OutputSettings();
            settings.syntax(OutputSettings.Syntax.xml); // Ensure XHTML syntax
            settings.escapeMode(Entities.EscapeMode.xhtml); // Escape mode for XHTML
            jsoupDoc.outputSettings(settings);
            String xhtmlContent = jsoupDoc.html(); // Get the XHTML content

            // Step 4: Pass the XHTML content to Flying Saucer for PDF conversion
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(xhtmlContent);
            renderer.layout();

            // Step 5: Create the PDF in a ByteArrayOutputStream
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                renderer.createPDF(outputStream);
                renderer.finishPDF();
                return outputStream.toByteArray(); // Return PDF as byte array
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String inputFile = "/Users/arunprasadranganathan/test.html";
        String outputFile = "/Users/arunprasadranganathan/output.pdf";

        try {
            // Step 1: Use JSoup to parse and clean up the HTML file
            File htmlFile = new File(inputFile);
            Document jsoupDoc = Jsoup.parse(htmlFile, "UTF-8");

            // Step 2: Convert the HTML to valid XHTML
            OutputSettings settings = new OutputSettings();
            settings.syntax(OutputSettings.Syntax.xml); // Ensure XHTML syntax
            settings.escapeMode(Entities.EscapeMode.xhtml); // Escape mode for XHTML
            jsoupDoc.outputSettings(settings);

            String xhtmlContent = jsoupDoc.html(); // Get the XHTML content

            // Step 3: Pass the XHTML content to Flying Saucer for PDF conversion
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(xhtmlContent);

            renderer.layout();

            // Step 4: Output the PDF
            try (OutputStream os = new FileOutputStream(outputFile)) {
                renderer.createPDF(os);
            }

            System.out.println("PDF generated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
