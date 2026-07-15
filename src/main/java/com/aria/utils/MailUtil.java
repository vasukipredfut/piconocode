package com.aria.utils;

import com.aria.service.EmailNotificationService;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.xhtmlrenderer.pdf.ITextRenderer;


import jakarta.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.stream.Collectors;


@Component
public class MailUtil {

    @Autowired
    EmailNotificationService emailNotificationService;

    public static String TYPE = "text/html";



    public static boolean hasHTMLFormat(MultipartFile file) {

        if (!TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }


    public String generatePdf(InputStream inputStream, String type, HttpServletRequest request ) {

        String content = null;
        String xhtmlContent = null;

        String fileName = null;
        if(type.equalsIgnoreCase("PurchaseOrder")){
            fileName = Constants.purchaseOrderFileName;
        }else if(type.equalsIgnoreCase("SupplierEvaluation")){
            fileName = Constants.supplierEvaluationFileName;
        }else {
            throw new IllegalArgumentException("Type is null...");
        }

        try {
            // Read the InputStream into a String
            content = new BufferedReader(new InputStreamReader(inputStream))
                        .lines().collect(Collectors.joining("\n"));

            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("HTML content is empty or null.");
            }

            // Now proceed with converting the HTML content to PDF
            ITextRenderer renderer = new ITextRenderer();

            // Convert HTML to XHTML (Make sure this function is working correctly)
             xhtmlContent = htmlToXhtml(content);

            // Set the XHTML content
            renderer.setDocumentFromString(xhtmlContent);

            // Layout and create the PDF
            renderer.layout();
            FileOutputStream fos = new FileOutputStream(fileName);
            renderer.createPDF(fos);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


        return content;
    }


    public String htmlToXhtml(String htmlContent) {

        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("HTML content cannot be null or empty.");
        }

        // Use Jsoup to parse the HTML content
        Document document = Jsoup.parse(htmlContent);

        // Set the document to output in XML syntax (which is similar to XHTML)
        document.outputSettings()
                .syntax(Document.OutputSettings.Syntax.xml)  // Set the syntax to XML
                .prettyPrint(true);  // Optional, formats the output

        // Return the content as an XHTML-compliant string
        return document.html();
    }



}
