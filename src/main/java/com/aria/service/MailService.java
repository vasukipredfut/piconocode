package com.aria.service;

import com.aria.config.ResponseMessage;
import com.aria.dao.PurchaseOrderRepository;
import com.aria.dao.VendorRepository;
import com.aria.model.PurchaseOrder;
import com.aria.model.Vendor;
import com.aria.utils.Constants;
import com.aria.utils.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class MailService {

    @Autowired
    MailUtil mailUtil;

    @Autowired
    PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    EmailNotificationService emailNotificationService;


    public ResponseEntity<ResponseMessage> sentMail(InputStream inputStream, String type, Long typeId, HttpServletRequest request) {


        String fileName = null;
        String subject = null;
        String content = null;

        try {

            content = mailUtil.generatePdf(inputStream, type, request);

            List<InternetAddress> recipientList = new ArrayList<>();
            InternetAddress[] recipients = null;

            if (type.equalsIgnoreCase("PurchaseOrder")) {
                fileName = Constants.purchaseOrderFileName;
                subject = "Testing With PurchaseOrder With Attachment";
                if (typeId != null) {
                    Optional<PurchaseOrder> optional = purchaseOrderRepository.findById(typeId);
                    if (optional.isPresent()) {
                        Vendor vendor = optional.get().getVendor();
                        if (vendor != null && vendor.getEmail() != null) {
                            // recipintList.add(new InternetAddress(vendor.getEmail()));
                            recipientList.add(new InternetAddress("predfuttechnologies@gmail.com"));
                        } else {
                            //throw new IllegalArgumentException("Type is null...");
                            recipientList.add(new InternetAddress("predfuttechnologies@gmail.com"));
                        }
                    }
                }
                recipientList.addAll(getRecipients(Constants.poNotificationAriaMailList));

            } else if (type.equalsIgnoreCase("SupplierEvaluation")) {

                fileName = Constants.supplierEvaluationFileName;
                subject = "Testing With SupplierEvaluation With Attachment";
                if (typeId != null) {
                    Optional<Vendor> optional = vendorRepository.findById(typeId);
                    if (optional.isPresent()) {
                        Vendor vendor = optional.get();
                        if (vendor != null && vendor.getEmail() != null) {
                            // recipintList.add(new InternetAddress(vendor.getEmail()));
                            recipientList.add(new InternetAddress("predfuttechnologies@gmail.com"));
                        } else {
                            //throw new IllegalArgumentException("Type is null...");
                            recipientList.add(new InternetAddress("predfuttechnologies@gmail.com"));
                        }
                    }
                }
                recipientList.addAll(getRecipients(Constants.poNotificationAriaMailList));

            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("Type Is Null..."));
            }

            recipients = recipientList.stream().toArray(InternetAddress[]::new);

            emailNotificationService.sendMailWithAttachmentMultipleRecipients(recipients, subject, content, fileName);


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(e.getMessage()));
        }


        return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage("Mail Sent Successfully......."));

    }


    public List<InternetAddress> getRecipients(String mail) {

        List<InternetAddress> recipientList = new ArrayList<>();

        List<String> mailList = new ArrayList<>(Arrays.asList(mail.split("\\,")));
        try {
            if (!mailList.isEmpty()) {
                for (String mailValue : mailList) {
                    recipientList.add(new InternetAddress(mailValue));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recipientList;

    }
}
