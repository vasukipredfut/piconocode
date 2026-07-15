package com.aria.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmailNotificationService {


    @Autowired
    private Configuration templates;

    @Value("${mail.smtp.host}")
    private String host;

    @Value("${mail.smtp.port}")
    private String port;

    @Value("${mail.smtp.username}")
    private String username;

    @Value("${mail.smtp.password}")
    private String password;

    @Value("${mail.from}")
    private String fromEmail;


    public boolean sendNotificationMail1(String toEmail, String name) {
        System.out.println("###########");
        System.out.println("EMAIL = " + toEmail + " ###  NAME = " + name);
        System.out.println("###########");
        String subject = "Please verify your email";
        String body = "";
        try {
            Template t = templates.getTemplate("interview_notification.ftl");
            Map<String, String> map = new HashMap<>();
            map.put("VERIFICATION_URL", "CHECK MAIL");
            map.put("NAME", name);
            body = FreeMarkerTemplateUtils.processTemplateIntoString(t, map);
        } catch (Exception ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return sendMail(toEmail, subject, body);
    }

    public boolean sendNotificationMail(String toEmail, String name, String date, String time) {
        System.out.println("###########");
        System.out.println("EMAIL = " + toEmail + " ###  NAME = " + name);
        System.out.println("###########");
        String subject = "Interview Notification Mail";
        String body = getContent(name, date, time);
        return sendMail(toEmail, subject, body);
    }

    public String getContent(String name, String date, String time) {
        String text = "Dear Mr." + name + ",\n" +
                "\n<br>" +
                "\n<br>" +
                "Greetings from Aria!\n" +
                "\n<br>" +
                "Your Application is under evaluation.\n" +
                "\n<br>" +
                "You are advised to be present on " + date + " @ " + time + "am for a personal interview.\n" +
                "\n<br>" +
                "\n<br>" +
                "Please bring original mark sheets, certificates, Nationality proofs, 2 Passport Size Photo & Supporting documents if any for the interview.\n" +
                "\n<br>" +
                "\n<br>" +
                "Please bring Xerox copy of mark sheets, certificates, Nationality proofs & Supporting documents if any for the interview.\n" +
                "\n<br>" +
                "\n<br>" +
                "<H1>Venue : V.V.V. & Sons Edible Oils Ltd., Plant-1.,</H1>" +
                "<H5>Near Aanimuthu Pillaiyar kovil street, Virudhunagar </H5>\n" +
                "\n<br>" +
                "\n<br>" +
                "All the Best,\n" +
                "\n<br>" +
                "\n<br>" +
                "Thanking you,\n" + "\n<br>" +
                "\n<br>" +
                "\n<br>" +
                "M.Petchimuthu\n" +
                "Plant Manager\n" +
                "VVV & Sons Edible Oils Ltd.,\n" +
                "(Manufacturer of ARIA)\n" +
                "Virudhunagar\n" +
                "9443745789" +
                "\n<br>";
        return text;
    }


    private boolean sendMail(String toEmail, String subject, String body) {
        return sendMail(toEmail, subject, body, 0);
    }

    private boolean sendMail(String toEmail, String subject, String body, int retryCount) {
        if (retryCount > 3) {
            System.out.println("Exceeded retry limit.");
            return false;
        }

        try {
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.connectiontimeout", "30000");
            props.put("mail.smtp.timeout", "30000");
            props.put("mail.smtp.writetimeout", "30000");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject(subject);
            msg.setContent(body, "text/html");

            Transport.send(msg);
            System.out.println("Email sent successfully!");
            return true;

        } catch (MessagingException ex) {
            if (ex.getCause() instanceof SocketTimeoutException || ex.getCause() instanceof ConnectException) {
                System.out.println("Connection timed out. Retrying...");
                return sendMail(toEmail, subject, body, retryCount + 1);  // Retry logic
            } else {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }



/*



    public void sendEmail(final String userName, final String password, final String host, final String html, final List<String> emails, String subject, String file) throws MessagingException
    {
        System.out.println("User Name: " + userName);
        System.out.println("Password: " + password);
        System.out.println("Host: " + host);

        //Get the session object
        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props,
                new jakarta.mail.Authenticator()
                {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication()
                    {
                        return new PasswordAuthentication(userName, password);
                    }
                });

        if (!emails.isEmpty())
        {
            //Compose the message
            InternetAddress[] address = new InternetAddress[emails.size()];
            for (int i = 0; i < emails.size(); i++)
            {
                address[i] = new InternetAddress(emails.get(i));
            }

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(userName));
            message.setRecipients(Message.RecipientType.TO, address);
            message.setSubject(subject);

            MimeBodyPart messageBodyPart = new MimeBodyPart();

            Multipart multipart = new MimeMultipart();

            messageBodyPart = new MimeBodyPart();
            String fileName = "attachmentName";
            DataSource source = new FileDataSource(file);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);
            message.setContent(html, "text/html; charset=utf-8");
            message.setContent(multipart);
            //send the message
            Transport.send(message);

            System.out.println("message sent successfully...");
        } else
        {
            System.out.println("No Recieptions");
        }

    }


*/

    public boolean sendMailWithAttach(String toEmail, String subject, String content, String file) {

       return sendMailWithAttach( toEmail,  subject,  content,  file, 0);
    }


    public boolean sendMailWithAttach(String toEmail, String subject, String content, String file, int retryCount) {
        System.out.println("###########");
        System.out.println("EMAIL = " + toEmail + " ###  subject = " + subject);
        System.out.println("###########");
        if (retryCount > 3) {
            System.out.println("Exceeded retry limit.");
            return false;
        }

        try {
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.connectiontimeout", "30000");
            props.put("mail.smtp.timeout", "30000");
            props.put("mail.smtp.writetimeout", "30000");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject(subject);

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(content, "text/html");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();

            DataSource source = new FileDataSource(file);

            messageBodyPart.setDataHandler(new DataHandler(source));

            messageBodyPart.setFileName(file);

            multipart.addBodyPart(messageBodyPart);

            msg.setContent(multipart);

            Transport.send(msg);
            return true;
        } catch (Exception ex) {

            if (ex instanceof MessagingException) {
                if (ex.getCause() instanceof SocketTimeoutException || ex.getCause() instanceof ConnectException) {
                    // Connection timeout exception

                    System.out.println("Connection timed out.");
                    return sendMailWithAttach( toEmail,  subject,  content,  file, retryCount+1);
                } else {
                    // Other messaging exceptions
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            } else {
                // Handle other exceptions
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return false;
    }



    public boolean sendMailWithAttachmentMultipleRecipients(InternetAddress[] recipients, String subject, String content, String file) {

        return sendMailWithAttachmentMultipleRecipients( recipients,  subject,  content,  file, 0);
    }


    public boolean sendMailWithAttachmentMultipleRecipients(InternetAddress[] recipients, String subject, String content, String file, int retryCount) {


        if (retryCount > 3) {
            System.out.println("Exceeded retry limit.");
            return false;
        }

        try {
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.connectiontimeout", "30000");
            props.put("mail.smtp.timeout", "30000");
            props.put("mail.smtp.writetimeout", "30000");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.addRecipients(Message.RecipientType.TO, recipients);
            msg.setSubject(subject);

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(content, "text/html");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            messageBodyPart = new MimeBodyPart();

            DataSource source = new FileDataSource(file);

            messageBodyPart.setDataHandler(new DataHandler(source));

            messageBodyPart.setFileName(file);

            multipart.addBodyPart(messageBodyPart);

            msg.setContent(multipart);

            Transport.send(msg);
            return true;
        } catch (Exception ex) {

            if (ex instanceof MessagingException) {
                if (ex.getCause() instanceof SocketTimeoutException || ex.getCause() instanceof ConnectException) {
                    // Connection timeout exception

                    System.out.println("Connection timed out.");
                    return sendMailWithAttachmentMultipleRecipients( recipients,  subject,  content,  file, retryCount+1);
                } else {
                    // Other messaging exceptions
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            } else {
                // Handle other exceptions
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return false;
    }


}
