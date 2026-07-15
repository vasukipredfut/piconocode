package com.aria.service;

import org.springframework.http.HttpStatus;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;

public class TestService {
    static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    //public static void main(String[] args) {

        /*String date1 = "25-09-2022";
        Date result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        try {
            result = formatter.parse(date1);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println(result);*/


        /*String start_date
                = "10-01-2018 06:10:20";

        // Given end Date
        String end_date
                = "10-01-2018 06:50:50";

        // Function Call
        findDifference(start_date, end_date);*/


        /*{
            List<Long> list = new ArrayList<Long>();
           *//* list.add(5);
            list.add(6);
            list.add(7);
            list.add(10);
            list.add(9);*//*
            int sum = 0;
            for (Long i : list)
                sum += i.intValue();

            System.out.println("sum-> " + sum);
        }*/


       /* Calendar c = GregorianCalendar.getInstance();

        System.out.println("Current week = " + Calendar.DAY_OF_WEEK);

// Set the calendar to monday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        System.out.println("Current week = " + Calendar.DAY_OF_WEEK);

// Print dates of the current week starting on Monday
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = "", endDate = "";

        startDate = df.format(c.getTime());
        c.add(Calendar.DATE, 6);
        endDate = df.format(c.getTime());*/

      /* int i =0;
       int ii =1;
       int iii =2;
       int iiii =3;
       if(iii<=4){
           System.out.println("present");
       }else{
           System.out.println("not present");
       }*/

       /* Double stkQty = Double.valueOf(80);
        Double getStkQty = Double.valueOf(50);
        Double remainingStkQty = Double.valueOf(0);

        remainingStkQty = stkQty - getStkQty;
        System.out.println(remainingStkQty);*/


    //System.out.println("Start Date = " + startDate);
    //System.out.println("End Date = " + endDate);


       /* String oilffa = "2.1";

        System.out.println("oilffa == " + oilffa);
        Double oilFFACheck = new Double(oilffa);
        System.out.println("oilFFACheck == " + oilFFACheck);
        if (oilFFACheck >= 1.5 && oilFFACheck <= 2) {
            System.out.println("Oil_FFA_1.5_to_2.0 == " + oilFFACheck);
            //binStock.setCategory("Oil_FFA_1.5_to_2.0");
        } else if (oilFFACheck > 2) {
            System.out.println("Oil_FFA_2.0 == " + oilFFACheck);
           // binStock.setCategory("Oil_FFA_2.0");
        } else {
            System.out.println( "loc == " + oilFFACheck);
        }*/


        /*Double percentage = 50.0;
        Double total_percentage = 100.0;
        Double actual_per = total_percentage - percentage;
        Double b = 60.0;
        Double c = 60.0;

        Double d = 60.0;
        Double e = 60.0;

        if (actual_per > b) {
            System.out.println("present");
        } else {
            System.out.println(" not present");
        }

        if (Double.compare(e, d) == 0) {
            System.out.println("present");
        }else {
            System.out.println("not present");
        }*/


    //System.out.println(GetUTCdatetimeAsDate());

    //}


    public static Date GetUTCdatetimeAsDate() {
        //note: doesn't check for null
        return StringDateToDate(GetUTCdatetimeAsString());
    }

    public static String GetUTCdatetimeAsString() {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("IST"));
        final String utcTime = sdf.format(new Date());

        return utcTime;
    }

    public static Date StringDateToDate(String StrDate) {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);

        try {
            dateToReturn = (Date) dateFormat.parse(StrDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateToReturn;
    }


    public static void findDifference(String start_date, String end_date) {


        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        System.out.println(new Date());
        try {

            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);


            long difference_In_Time = d2.getTime() - d1.getTime();

            System.out.println("Difference " + "between two dates is: " + " difference_In_Time   =  " + difference_In_Time);

            long difference_In_Seconds = (difference_In_Time / 1000) % 60;

            System.out.println("Difference " + "between two dates is: " + " difference_In_Seconds   =  " + difference_In_Seconds);

            long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;

            System.out.println("Difference " + "between two dates is: " + " difference_In_Minutes   =  " + difference_In_Minutes);


            long difference_In_Hours = (difference_In_Time / (1000 * 60 * 60)) % 24;

            System.out.println("Difference " + "between two dates is: " + " difference_In_Hours   =  " + difference_In_Hours);

            long difference_In_Years = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));

            System.out.println("Difference " + "between two dates is: " + " difference_In_Years   =  " + difference_In_Years);

            long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;

            System.out.println("Difference " + "between two dates is: " + " difference_In_Days   =  " + difference_In_Days);

            System.out.print("Difference " + "between two dates is: ");

            System.out.println(difference_In_Years + " years, " + difference_In_Days + " days, " + difference_In_Hours + " hours, " + difference_In_Minutes + " minutes, " + difference_In_Seconds + " seconds");
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    public List<Date> getDatesBetweenUsingJava7(Date startDate, Date endDate) {
        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = getCalendarWithoutTime(startDate);
        Calendar endCalendar = getCalendarWithoutTime(endDate);

        while (calendar.before(endCalendar)) {
            Date result = calendar.getTime();
            datesInRange.add(result);
            calendar.add(Calendar.DATE, 1);
        }

        return datesInRange;
    }

    private static Calendar getCalendarWithoutTime(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }




    /*public static void main(String[] args) {

        // Current date and time using now()
        ZonedDateTime currentDateTime = ZonedDateTime.now();

        // Creating two timezone zoneid objects using ZoneId.of() method.
        ZoneId losAngelesTimeZone = ZoneId.of("America/Los_Angeles");
        ZoneId dubaiTimeZone = ZoneId.of("Asia/Dubai");
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");

        // Converting Current timezone time to Kolkata time
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);

        // Converting Current timezone time to Log Angeles time
        ZonedDateTime losAngelesDateTime = currentDateTime.withZoneSameInstant(losAngelesTimeZone);

        // Converting Current timezone time to Dubai time
        ZonedDateTime dubaiDateTime = currentDateTime.withZoneSameInstant(dubaiTimeZone);

        // Datetime formatting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT);

        // Print all 3 dates
        System.out.println("Current time in IST : " + formatter.format(indianDateTime));
        System.out.println("Current time in IST : " + formatter.format(currentDateTime));
        System.out.println("Los Angeles time now : " + formatter.format(losAngelesDateTime));
        System.out.println("Dubai time now : " + formatter.format(dubaiDateTime));

        // getting the diff b/w two los angeles and dubai times.
        //printDurationBetweenTwoDates(losAngelesDateTime, dubaiDateTime);

    }*/

    private static void printDurationBetweenTwoDates(ZonedDateTime sfoDateTime, ZonedDateTime dubaiDateTime) {
        Duration d = Duration.between(sfoDateTime, dubaiDateTime);
        long days = d.get(ChronoUnit.SECONDS);
        System.out.println("Time Difference between los angeles and dubai : " + days / (60 * 60) + " Hours " + (days % (60 * 60)) / 60 + " Minites");

    }



    /*public static void main(String[] args) {

        // Current date and time using now()
        ZonedDateTime currentDateTime = ZonedDateTime.now();

        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");

        // Converting Current timezone time to Kolkata time
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);

        // Datetime formatting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT);

        ZonedDateTime indianDateTime1 = indianDateTime.minusDays(1);

        int time = 21 - indianDateTime1.getHour() ;

        int min = 59 - indianDateTime1.getMinute() ;

        int sec = 999 - indianDateTime1.getSecond() ;

        ZonedDateTime indianDateTime2 =  indianDateTime1.plusHours(time).plusMinutes(min);



        //ZonedDateTime indianDateTime2 = indianDateTime.minusDays(1).minusHours(indianDateTime1.getHour());

        // Print all 3 dates
        System.out.println("Current time in IST 1: " + formatter.format(indianDateTime));
        System.out.println("Current time in IST 2: " + formatter.format(indianDateTime1));
        System.out.println("Current time in IST 3: " + formatter.format(indianDateTime2));
        System.out.println("Current time in IST 4: " + time);
        System.out.println("Current time in IST 5: " + min);
        System.out.println("Current time in IST 5: " + sec);
        //System.out.println("Current time in IST 6: " + indianDateTime2);


        // getting the diff b/w two los angeles and dubai times.
        //printDurationBetweenTwoDates(losAngelesDateTime, dubaiDateTime);
        String productType = null;
        Long id = (Long) 5654L;
        Long sjid = (Long) 563L;
        Long sjid1 = (Long)  564L;
        Long sjid2 = (Long) 565L;




    }*/


   /* public static void main(String args[]){


        ZonedDateTime currentDateTime = ZonedDateTime.now();
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);
        Instant instant = indianDateTime.toInstant();
        Date date =  Date.from(instant);

        System.out.println();







    }*/


    public static void main(String args[]) {


        /*double poQty = 100.0;
        double fixedQty = 2;
        if (  fixedQty > 0 && (poQty > fixedQty) ) {
            System.out.println("PoClosed");
        }else {
            System.out.println("open");
        }


        double stkQty = 6.24;
        double qty = 34567.72;
        if (  stkQty > qty ) {
            System.out.println("greater");
        }else if (  stkQty == qty ) {
            System.out.println("open");
        }else{
            System.out.println("lesser");
        }*/

/*
        Date startDate = null;

        Date toDate = null;
        int weekOff = 7;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

        try {
            startDate = formatter.parse("01-09-2024");
            toDate = formatter.parse("30-09-2024");

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        *//*Calendar endcalendar = Calendar.getInstance();
        endcalendar.setTime(toDate);
        endcalendar.add(Calendar.DATE, 1);
        toDate = endcalendar.getTime();*//*

        Date current = startDate;
        List<String> dateRanges = new ArrayList<String>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        Date weekStart = calendar.getTime();
        Date weekEnd = current;
        while (current.before(toDate)) {

            calendar = Calendar.getInstance();
            calendar.setTime(current);

            calendar.add(Calendar.DATE, 1);
            current = calendar.getTime();

            System.out.println(calendar.get(Calendar.DAY_OF_WEEK));
            System.out.println(calendar.getTime());

            if (calendar.get(Calendar.DAY_OF_WEEK) == weekOff) {

                weekEnd = calendar.getTime();

                String date1 = null;
                String date2 = null;

                SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");

                try {
                    date1 = formatter.format(weekStart);
                    date2 = formatter.format(weekEnd);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


                String range = date1 + " - " + date2;
                dateRanges.add(range);

                calendar.add(Calendar.DATE, 1);
                current = calendar.getTime();
                weekStart = calendar.getTime();
                weekEnd = calendar.getTime();

            } else {
                current = calendar.getTime();
                weekEnd = current;
            }


        }


        int sizeofarray = dateRanges.size() - 1;
        if (dateRanges.size() > 0) {
            String date1 = null;
            String date2 = null;

            SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");

            try {
                date1 = formatter.format(weekStart);
                date2 = formatter.format(weekEnd);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String range = date1 + " - " + date2;
            if (!dateRanges.get(sizeofarray).equals(range)) {
                dateRanges.add(range);
            }

        } else {
            String date1 = null;
            String date2 = null;

            SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy");

            try {
                date1 = formatter.format(weekStart);
                date2 = formatter.format(weekEnd);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String range = date1 + " - " + date2;
            dateRanges.add(range);
        }

        System.out.println(dateRanges);*/

        Long pShiftId1 = 1L;

        Date shiftDate = new Date();

        Integer shiftNumber = Math.toIntExact(pShiftId1);

        int dateNumber = Integer.parseInt(
                shiftDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("ddMMyyyy")));

        int finalShiftValue = dateNumber-1 * 3;

        System.out.println("final_shift_value == "+finalShiftValue);
        int finalShiftValue1 = finalShiftValue + shiftNumber;
        System.out.println("finalShiftValue1 == "+finalShiftValue1);

        String shiftNo = "T-" + finalShiftValue;
        System.out.println("shiftNo == "+shiftNo);

    }


}
