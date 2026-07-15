package com.aria.service;

import com.aria.model.Shifts;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.*;

public class TEstEmailService {

    private static final DecimalFormat decfor = new DecimalFormat("0.00");
    private static final DecimalFormat decfor1 = new DecimalFormat("0");

    public static void main(String args[]) {




        /*String json = "{\"id\": 773,\"productName\": \"SESAME SEED1\",\"productType\": \"Production\",\"description\": \"SESAME SEED\",\"departmentId\": 13,\"nikeNameSearch\": \"SESAME SEED\",\"rawMaterial\": \"Oil\",\"contractPurchase\": \"YES\",\"recycle\": \"YES\",\"taskCategory\": \"RING SIZE CHECKING\",\"tcid\": 22,\"mainUnit\": \"Kg\",\"altunit\": \"HOURS\",\"mainUnit1\": \"Bags\",\"altUnit1\": \"ROLL\",\"mainValue\": \"asd\",\"altValue\": \"as\",\"billOfMaterial\": \"ON\",\"byeProduct\": \"ON\",\"grossWeight\": 0,\"packingType\": \"CARTON\",\"inward\": \"maintain\": \"YES\",\"peakMonthAvgQty1\": \"23\",\"peakMonthAvgQty2\": \"23\",\"peakMonthAvgQty3\": \"23\",\"minimumStockLevelQuantity\": \"23\",\"allowProcessIntakeIfNoQcReport\": \"YES\",\"productCategory\": \"SESAME SEED\",\"purchaseResponsibilityPlant\": 6}";
        try {
            HttpHeaders headers = new HttpHeaders();

            headers.add("X-AspNet-Version", "4.0.30319");
            headers.add("X-Powered-By", "4.0.30319");
            headers.add("F", "SaveEarmsItem");
            headers.add("M", "POST");
            headers.add("P", json);
            String url = "https://www.ilovewaste.in/Api/Factory/appealRequest";
            HttpEntity entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = null;
            HttpEntity response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println(response.getBody().toString());


        }catch (Exception e){

        }*/

       /* String t = "6860";
        Double total = Double.valueOf(t);

        System.out.println("total = " + total);

        Double lot = Double.valueOf("74");

        System.out.println("lot = " + lot);

        Double per100 = Double.valueOf("100");

        System.out.println("per100 = " + per100);

        Double qty = total / lot;

        Double qty1 = qty;

        System.out.println("qty = " + qty);

        System.out.println("qty 1 = " + qty1);

        Double s = 0.70;

        Double sin1 = s * 74 ;
         DecimalFormat decfor = new DecimalFormat("00");
        System.out.println("sin1 = " + decfor.format(sin1));*/


       /* double poAmt = 2457802.60;
        System.out.println("poAmt = " + poAmt);
        double valuePercentage = 100;
        System.out.println("valuePercentage == " + valuePercentage);
        double advanceAmt = (valuePercentage / 100) * poAmt;
        System.out.println("AdvanceAmt = " + advanceAmt);
        */

        /*Double lot = 40.0;
        Double packageQty = Double.valueOf(50);
        Double packageQty1 = Double.valueOf(49);

        Double covQty = packageQty / lot;
        System.out.println(covQty);

        String convertValue = decfor.format(covQty);
        System.out.println( "convertValue  == "+convertValue) ;

        List<String> values = new ArrayList<>(Arrays.asList(convertValue.split("\\.")));
        String bagValue = values.get(0);
        System.out.println( "bagValue  == "+bagValue) ;
        Double kgValues = Double.valueOf("0."+values.get(1));
        System.out.println( "kgValues  == "+kgValues) ;
        Double covKGQty =  lot * kgValues ;
        System.out.println( "covKGQty  == "+covKGQty) ;
        String kgValue = decfor1.format(covKGQty);
        System.out.println( "kgValue  == "+kgValue) ;

        int kg = Integer.parseInt(kgValue);
        String finalCovQty = null;
        if(9 >= kg){
            finalCovQty = bagValue+".0"+kgValue;
        }else {
            finalCovQty = bagValue+"."+kgValue;
        }*/


        /*Long l = 241L;

        if(l >= 240){
            System.out.println(l.intValue());
        }else {
            System.out.println("finalCovQty  == " + l.intValue());
        }
*/



        /*Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int month = localDate.getMonthValue();
        int year = localDate.getYear();
        System.out.println("month  == " + month);
        System.out.println("year  == " + year);


        LocalDate currentDate = LocalDate.now();

        // Get the current month
        YearMonth currentMonth = YearMonth.from(currentDate);

        // Get the number of days in the current month
        int daysInMonth = currentMonth.lengthOfMonth();

        System.out.println("daysInMonth  == " + daysInMonth);*/



        /*double gwa=13275;
        double hra = 7350;

        double basic = 	45871;
        double da = 6344;
        double wa = 750;

        double  epf = gwa + basic+ da +wa +2008;

        System.out.println( "  epf === "+epf);

        double checkepf = epf/100 ;

        System.out.println( " checkepf epf === "+checkepf);

        double finalepf = checkepf * 12;

        System.out.println( " detuction epf === "+Math.round(finalepf));*/

       /* int c = 100;
        if(c > 1){
            System.out.println(c);
        }else {
            System.out.println(c);
        }*/

        
        /*System.out.println("LocalDateTime () "+LocalDateTime.now());
        System.out.println("LocalDateTime () "+LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        Instant instant = localDateTime.atZone(ZoneId.of("Asia/Kolkata")).toInstant();
        Date date = Date.from(instant);
        System.out.println("date = " + date);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss ");
        if (date != null) {
            try {
               System.out.println( formatter.format(date));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }*/
        
        

        /*final String username = "smtp@aemprocessing.com";
        final String password = "Processing1.";
        final String host = "authsmtp.aemprocessing.com";
        final String fromEmail = "noreply@aemprocessing.com";*/

       /* final String username = "varalottissvrm@aria.com";
        final String password = "Test@12345";
        final String host = "smtp.gmail.com";
        final String fromEmail = "varalottissvrm@aria.com";

        String text = "Hello  ALAGURAJA, <br>" + "Thank you for applying for the position of <H1> Apperntice <H1> with us. We are glad to inform you that your interview has been scheduled for <Time> on <Date>. <br>" +
                "\n<br>" +
                "Kindly note the interview details: <br>" +
                "\n<br>" +
                "Venue Address (in case of face-to-face interview) <br>" +
                "\n<br>" +
                "Communication Link (in case of remote/virtual interview) <br>" +
                "\n<br>" +
                "Interviewing Person (name and designation) <br>" +
                "\n<br>" +
                "\n<br>" +
                "Please reply to this email if you have any questions or need to reschedule. We look forward to speaking with you. <br> " +
                "\n<br>" +
                " Sincerely, <br> " +
                "\n<br>" +
                "Name <br> " +
                "\n<br>" +
                "Designation and contact details <br> ";

        try {

            Properties props = System.getProperties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.port", 25);
            props.put("mail.smtp.auth", true);
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.socketFactory.port", 25);
            props.put("mail.smtp.socketFactory.fallback", false);
            props.put("mail.smtp.username", username);
            props.put("mail.smtp.password", password);
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");


           Session session = Session.getInstance(props, new Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

           *//* Properties properties = System.getProperties();
            properties.put("mail.smtp.host","smtp.gmail.com");
            properties.put("mail.transport.protocol", "smtp");
            properties.put("mail.smtp.port","465");
            properties.put("mail.smtp.starttls.enable", false);
            properties.put("mail.smtp.auth",true);
            properties.put("mail.smtp.ssl.enable",false);
            Session session = Session.getInstance(properties, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("alagurajaramasamy43@gmail.com", "$4Palaniyammal");
                }
            });*//*



            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress("alagudce@gmail.com"));
            msg.setSubject("test mail");

            msg.setContent(text, "text/html");
            Transport.send(msg, msg.getAllRecipients());

        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
/*
        //percentage calculate

        float totalValue = 10;
        float value = 5;

        float fin = ((value / totalValue) * 100);

        float fd = (fin / 100) * 30;
        System.out.println("fin = " + fin);
        System.out.println("fd = " + fd);


        String s1 = "10/05/2023";
        String s2 = "25/05/2023";
        String s3 = "20/05/2023";
        Date date1 = null;
        Date date2 = null;
        Date date3 = null;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        try {
            date1 = formatter.parse(s1);
            date2 = formatter.parse(s2);
            date3 = formatter.parse(s3);


        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Long diff1 = date3.getTime() - date1.getTime();
        Long difference_In_Days = (diff1 / (1000 * 60 * 60 * 24)) % 365;

        System.out.println(difference_In_Days);
        float totalDates = Integer.valueOf(difference_In_Days.intValue());
        System.out.println(totalDates);

        Long diff2 = date2.getTime() - date3.getTime();
        Long difference_In_Days1 = (diff2 / (1000 * 60 * 60 * 24)) % 365;
        System.out.println(difference_In_Days1);
        float dates = Float.valueOf(difference_In_Days1.intValue());
        System.out.println(dates);

        float fin1 = ((dates / totalDates) * 100);


        System.out.println(fin1);*/


        /*LocalDate currentDate = LocalDate.now();//current date
        System.out.println(currentDate);
        int lastMonths = 12;//number of months
        LocalDate prevDate = null;
        LocalDate start = null;
        LocalDate end = null;

        for (int i = 0; i < lastMonths; i++) {
            if (prevDate == null) {
                start = currentDate.withDayOfMonth(1);//First day of current month
                end = currentDate.withDayOfMonth(currentDate.lengthOfMonth());//Last day of current month
                prevDate = currentDate.minusMonths(1);//subtracting one month from current month
            } else {
                start = prevDate.withDayOfMonth(1);//First day of previous month
                end = prevDate.withDayOfMonth(prevDate.lengthOfMonth());//Last day of previous month
                prevDate = prevDate.minusMonths(1);//subtracting one month from previous month
            }
            System.out.println(start.getMonth().name() + " " + end);
        }
*/



        Double qty = 80.0 ;
        Double qty1 =100.0 ;


        qty = qty - qty1;

        System.out.println("qty == "+qty);







    }


}


/*
    private static final String SMTP_HOST_NAME = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String emailMsgTxt = "Test Message Contents";
    private static final String emailSubjectTxt = "A test from gmail";
    private static final String emailFromAddress = "itdesk@aria.com";
    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    private static final String[] sendTo = {"alagudce@gmail.com"};
    private static final String sendTo1 = "alagudce@gmail.com";


    public static void main(String args[]) throws Exception {

        *//*Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        new TEstEmailService().sendSSLMessage(sendTo, emailSubjectTxt,
                emailMsgTxt, emailFromAddress);*//*

        sendPlainTextEmail(SMTP_HOST_NAME, SMTP_PORT, "alagurajaramasamy43@gmail.com","$4Palaniyammal",sendTo1,emailSubjectTxt, emailMsgTxt);
        System.out.println("Sucessfully Sent mail to All Users");
    }

    public void sendSSLMessage(String recipients[], String subject,
                               String message, String from) throws MessagingException {
        boolean debug = true;

        Properties props = new Properties();

        props.put("mail.smtp.host", SMTP_HOST_NAME);


        props.put("mail.smtp.auth", "true");
        //props.put("mail.smtp.ssl.enable",true);
        props.put("mail.debug", "true");
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.port", SMTP_PORT);
        props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getDefaultInstance(props,
                new jakarta.mail.Authenticator() {

                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("@aria.com", "");
                    }
                });

        session.setDebug(debug);

        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);

        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients(Message.RecipientType.TO, addressTo);

// Setting the Subject and Content Type
        msg.setSubject(subject);
        msg.setContent(message, "text/plain");
        Transport.send(msg);
    }


    public static void sendPlainTextEmail(String host, String port,
                                          final String userName, final String password, String toAddress,
                                          String subject, String message) throws AddressException,
            MessagingException {

        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        properties.put("mail.smtp.starttls.enable", "true");
// *** BEGIN CHANGE
        properties.put("mail.smtp.user", userName);

        // creates a new session, no Authenticator (will connect() later)
        Session session = Session.getDefaultInstance(properties);
// *** END CHANGE

        // creates a new e-mail message
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        // set plain text message
        msg.setText(message);

// *** BEGIN CHANGE
        // sends the e-mail
        Transport t = session.getTransport("smtp");
        t.connect(userName, password);
        t.sendMessage(msg, msg.getAllRecipients());
        t.close();
// *** END CHANGE

    }*/

//}
