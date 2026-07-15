package com.aria.utils;

import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.*;

import static java.util.Calendar.*;

@Component
public class DateUtil {

    static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    public static LocalDate stringToLocalDate(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate df = null;
        if (date != null) {
            try {
                df = LocalDate.parse(date, dateTimeFormatter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return df;
    }

    public static String localDateToString(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String df = null;
        if (date != null) {
            try {
                df = date.format(dateTimeFormatter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return df;
    }

    public static LocalDateTime stringToLocalDateTime(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime df = null;
        if (date != null) {
            try {
                df = LocalDateTime.parse(date, dateTimeFormatter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return df;
    }

    public static String localDateTimeToString(LocalDateTime date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String df = null;
        if (date != null) {
            try {
                df = date.format(dateTimeFormatter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return df;
    }

    public String getDateToStringDDMMYYYY(Date date) {
        String result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        if (date != null) {
            try {
                result = formatter.format(date);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public Date getFilterDate(String date) {
        Date result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        if (date != null) {
            try {
                result = formatter.parse(date);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public Date getStringTODateDDMMYYYY(String date) {
        Date result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        if (date != null) {
            try {
                result = formatter.parse(date);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public Date getStringTODateF1(String date) {
        Date result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        if (date != null) {
            try {
                result = formatter.parse(date);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public String getStringTODateF1(Date date) {
        String result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        if (date != null) {
            try {
                result = formatter.format(date);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }





    public Date getStringTODateWk(String date) {


        Date result = null;
        if (date != null && date != "") {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            if (date != null) {
                try {
                    result = formatter.parse(date);

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    public Date getStringTODateWkWithStartTime(String date) {

        Date result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        if (date != null) {
            try {
                result = getDateWithStartTime(formatter.parse(date));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public Date getStringTODateWkWithEndTime(String date) {

        Date result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        if (date != null) {
            try {
                result = getDateWithEndTime(formatter.parse(date));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }


    public Date getStringTODateF2(String date) {
        Date result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        try {
            result = formatter.parse(date);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String getStringTODate(Date date) {
        String result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        if (date != null) {
            try {
                result = formatter.format(date);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;

    }

    public String getDateTOStringWk(Date date) {
        String result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        if (date != null) {
            try {
                result = formatter.format(date);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public String getTimeStampToString(String string) {

        String result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("HH:MM:SS");
        try {
            result = formatter.format(string);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public Date getStartDate() {

        Calendar calendar = getCalendarForNow();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        setTimeToBeginningOfDay(calendar);
        return calendar.getTime();
    }


    public Date getStartDate(Date date) {
        Calendar calendar = getCalendarWithoutTime(date);
        return calendar.getTime();
    }


    public Date getEndDate(Date date) {
        Calendar calendar = getCalendarWitEndTime(date);
        return calendar.getTime();
    }

    public Date getPreviousMonthStartDate() {

        Calendar aCalendar = Calendar.getInstance();
        aCalendar.add(MONTH, -1);
        aCalendar.set(DATE, 1);
        return aCalendar.getTime();
    }

    public Date getEndDate() {

        Calendar calendar = getCalendarForNow();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        setTimeToEndofDay(calendar);
        return calendar.getTime();
    }

    private static Calendar getCalendarForNow() {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        return calendar;
    }

    private Date getDateWithStartTime(Date date) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getDateWithEndTime(Date date) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }


    private static void setTimeToBeginningOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private static void setTimeToEndofDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    public String getDateTime(Date date) {
        String start_date = null;
        if (date != null) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            start_date = sdf1.format(date);
        }
        return start_date;
    }


    public Map<String, Long> findDifference(Date date1, Date date2) {
        Map<String, Long> datesValue = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String start_date = sdf1.format(date1);
        String end_date = sdf1.format(date2);
        try {
            Date d1 = sdf.parse(start_date);
            Date d2 = sdf.parse(end_date);

            long difference_In_Time = d2.getTime() - d1.getTime();
/*

            System.out.println(
                    "Difference " + "between two dates is: " + " difference_In_Time   =  " + difference_In_Time);
*/

            long difference_In_Seconds = (difference_In_Time / 1000) % 60;

            datesValue.put("S", difference_In_Seconds);

           /* System.out.println(
                    "Difference " + "between two dates is: " + " difference_In_Seconds   =  " + difference_In_Seconds);
*/
            long difference_In_Minutes = (difference_In_Time / (1000 * 60)) % 60;

            datesValue.put("M", difference_In_Minutes);

         /*   System.out.println(
                    "Difference " + "between two dates is: " + " difference_In_Minutes   =  " + difference_In_Minutes);
*/
            long difference_In_Hours = (difference_In_Time / (1000 * 60 * 60)) % 24;

            if (difference_In_Hours != 0) {

                datesValue.put("H", difference_In_Hours);
            }

            System.out.println(
                    "Difference " + "between two dates is: " + " difference_In_Hours   =  " + difference_In_Hours);

            long difference_In_Years = (difference_In_Time / (1000l * 60 * 60 * 24 * 365));

            if (difference_In_Years != 0) {

                datesValue.put("Y", difference_In_Hours);
            }

            long difference_In_Days = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;

            if (difference_In_Days != 0) {

                datesValue.put("D", difference_In_Days);
            }

         /*   System.out.println(
                    "Difference " + "between two dates is: " + " difference_In_Days   =  " + difference_In_Days);

            System.out.print("Difference " + "between two dates is: ");

            System.out.println(difference_In_Years + " years, " + difference_In_Days + " days, " + difference_In_Hours
                    + " hours, " + difference_In_Minutes + " minutes, " + difference_In_Seconds + " seconds");
      */

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return datesValue;
    }

    public String getTime(Date date) {
        DateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
        String time = formatter.format(new Date());
        System.out.println(time);
        return time;
    }

    public Date getWeekStartDate(Date date) {
        Calendar c = GregorianCalendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return c.getTime();
    }

    public Date getWeekEndDate(Date date) {
        Calendar c = GregorianCalendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.add(DATE, 6);
        return c.getTime();
    }

    public LocalDateTime getDateToLocalDateTime(Date date) {
        Instant dateInstant = date.toInstant();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(dateInstant, ZoneId.of("Asia/Kolkata"));
        return localDateTime;
    }

    public LocalDateTime getStringToLocalDateTime(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDateTime df = null;
        if (date != null) {
            try {
                df = LocalDateTime.parse(date, dateTimeFormatter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return df;
    }

    public String getLocalDateTimeToStringDDMMYYYY(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            return localDateTime.format(dateTimeFormatter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getLocalDateTimeToStringDDMMYYYY1(LocalDateTime date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String df = null;
        if (date != null) {
            try {
                df = date.format(dateTimeFormatter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return df;
    }

    public String getLocalDateTimeToStringDDMMMYYYY(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        try {
            return localDateTime.format(dateTimeFormatter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Date> getListOfDatesBetweenTwoDate(Date startDate, Date endDate) {
        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = getCalendarWithoutTime(startDate);
        Calendar endCalendar = getCalendarWithoutTime(endDate);

        while (calendar.before(endCalendar)) {
            Date result = calendar.getTime();
            datesInRange.add(result);
            calendar.add(DATE, 1);
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

    private static Calendar getCalendarWitEndTime(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar;
    }


    public Date getLocalDateTimeToDate(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZoneId.of("Asia/Kolkata")).toInstant();
        Date date = Date.from(instant);
        return date;
    }

    public Date getDaysBetweenDate(Date date) {
        LocalDate startdate = LocalDate.of(2022, 7, 22);
        LocalDate endDate = LocalDate.of(2022, 7, 24);
        Period period = Period.between(startdate, endDate);
        long daysDiff = Math.abs(period.getDays());
        System.out.println(" The number of days between dates: " + daysDiff);
        return date;

    }

    public String getDateTOStringDDMMMYYYY(Date date) {
        String result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        try {
            result = formatter.format(date);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public Date getStringTODateYYYYMMDD(String date) {
        Date result = null;
        if (date != null && date != "") {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            try {
                result = formatter.parse(date);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public String getDateTOStringYYYYMMDD(Date date) {
        String result = null;
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            try {
                result = formatter.format(date);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public int getDayName(Date date) {
        int i = 0;
        try {
            Format f = new SimpleDateFormat("EEEE");
            String str = f.format(date);
            if (str.equalsIgnoreCase("Sunday")) {
                i = 1;
            } else if (str.equalsIgnoreCase("Monday")) {
                i = 2;
            } else if (str.equalsIgnoreCase("Tuesday")) {
                i = 3;
            } else if (str.equalsIgnoreCase("Wednesday")) {
                i = 4;
            } else if (str.equalsIgnoreCase("Thursday")) {
                i = 5;
            } else if (str.equalsIgnoreCase("Friday")) {
                i = 6;
            } else if (str.equalsIgnoreCase("Saturday")) {
                i = 7;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return i;
    }

    public Date getStringTimeTOTime(String time) {
        Date result = null;
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        try {
            result = dateFormat.parse(time);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String getTimeToStringTime(Date time) {
        String result = null;
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        try {
            result = dateFormat.format(time);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String getDateToStringYYYYMMDD(Date date) {
        String result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (date != null) {
            try {
                result = formatter.format(date);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public Map<String, String> getMonthOfStartAndEndDate(int lastMonths) {
        LocalDate currentDate = LocalDate.now(); // current date
        System.out.println(currentDate);
        // int lastMonths = 3;//number of months
        LocalDate prevDate = null;
        LocalDate start = null;
        LocalDate end = null;
        Map<String, String> stringMap = new HashMap<>();
        for (int i = 0; i < lastMonths; i++) {
            if (prevDate == null) {
                start = currentDate.withDayOfMonth(1); // First day of current month
                end = currentDate.withDayOfMonth(currentDate.lengthOfMonth()); // Last day of current month
                prevDate = currentDate.minusMonths(1); // subtracting one month from current month
            } else {
                start = prevDate.withDayOfMonth(1); // First day of previous month
                end = prevDate.withDayOfMonth(prevDate.lengthOfMonth()); // Last day of previous month
                prevDate = prevDate.minusMonths(1); // subtracting one month from previous month
            }
            stringMap.put(start.getMonth().name(), start + "/" + end);
        }
        return stringMap;
    }

    public Date getDateValue(Date date) {
        String result = null;
        Date result1 = null;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        if (date != null) {
        	
            try {
                result = formatter.format(date);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
            try {
                result1 = formatter1.parse(result);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            
        }
        
        return result1;
    }

    public Date getISTDate() {
        Date date = getIndianDateWithTime();
        return date;
    }

    public Date getISTTomorrowDayDate() {
        Date date = StringDateToDate(getTomorrowDayIndianDate());
        return date;
    }

    public Date getISTYesterDayDate() {
        Date date = StringDateToDate(getPreviousDayIndianDate());
        return date;
    }

    public Date getISTClosingStartDateTime() {
        Date date = StringDateToDate(getPreviousDayIndianDateTime());
        return date;
    }


    public Date StringDateToDate(String StrDate) {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        try {
            dateToReturn = (Date) dateFormat.parse(StrDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateToReturn;
    }



    public Date getIndianDateWithTime() {

        ZonedDateTime currentDateTime = ZonedDateTime.now();
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);
        /*System.out.println("----------------------------");
        System.out.println(" localdateTime = "+indianDateTime.toLocalDateTime());
        System.out.println("----------------------------");
        System.out.println(" indianDateTime instant = "+indianDateTime.toInstant());
        System.out.println("----------------------------");
        System.out.println("--------------DATE--------------");
        System.out.println(" Date instant = "+Date.from(indianDateTime.toInstant()));
        System.out.println("----------------------------");*/
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT);
        String dateTime = formatter.format(indianDateTime);
       /* System.out.println("----------------------------");
        System.out.println(" datetime = "+formatter.format(indianDateTime));
        System.out.println("----------------------------");*/
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        try {
            dateToReturn = (Date) dateFormat.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
      /*  System.out.println("----------------------------");
        System.out.println(" dateToReturn = "+dateToReturn);
        System.out.println("----------------------------");*/
        return dateToReturn;
    }


    public String getIndianDateTime() {

        ZonedDateTime currentDateTime = ZonedDateTime.now();
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT);
        System.out.println("Current time in IST : " + formatter.format(indianDateTime));
        return formatter.format(indianDateTime);
    }


    public String getIndianDateTime(Date date) {
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime indianDateTime = date.toInstant().atZone(indianTimeZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT);
        System.out.println("Current time in IST : " + formatter.format(indianDateTime));
        return formatter.format(indianDateTime);
    }


    public String getPreviousDayIndianDate() {
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);
        ZonedDateTime indianYesterDay = indianDateTime.minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT);
        System.out.println("Current time in IST : " + formatter.format(indianYesterDay));
        return formatter.format(indianYesterDay);
    }


    public String getPreviousDayIndianDateTime() {
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);

        ZonedDateTime indianYesterDay = indianDateTime.minusDays(1);

        int time = 21 - indianYesterDay.getHour();

        int min = 59 - indianYesterDay.getMinute();

        ZonedDateTime indianDatePreviousDay = indianYesterDay.plusHours(time).plusMinutes(min);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT);
        System.out.println("Current time in IST : " + formatter.format(indianDatePreviousDay));
        return formatter.format(indianDatePreviousDay);
    }

    public String getTomorrowDayIndianDate() {
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);
        ZonedDateTime indianYesterDay = indianDateTime.plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT);
        System.out.println("Current time in IST : " + formatter.format(indianYesterDay));
        return formatter.format(indianYesterDay);
    }

    public Date getISTPreviousDate() {
        Date date = StringDateToDate(getPreviousDayIndianDateTime());
        return date;
    }

    public Map<String, Long> getDateData() {
        Map<String, Long> stringLongMap = new HashMap<>();
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);
        Long month = Long.valueOf(indianDateTime.getMonth().getValue());
        Calendar c = Calendar.getInstance();
        int digYear = c.get(YEAR);
        String yrStr = Integer.toString(digYear);
        String yrStrEnd = yrStr.substring(yrStr.length() - 2);
        Long year = Long.valueOf(yrStrEnd);
        stringLongMap.put("y", year);
        stringLongMap.put("m", month);
        return stringLongMap;
    }


    public LocalDateTime getLocalDateTime(LocalDateTime localDateTime) {

        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        LocalDateTime indianDateTime = localDateTime.atZone(indianTimeZone).toLocalDateTime();
        return indianDateTime;
    }


    public LocalDateTime getISTStartLocalDateTime(Date date) {

        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        Instant current = date.toInstant();

        ZonedDateTime currentDateTime = ZonedDateTime.ofInstant(current, indianTimeZone);

        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);

        int time = indianDateTime.getHour();

        int min = indianDateTime.getMinute();

        ZonedDateTime indianDay = indianDateTime.minusHours(time).minusMinutes(min);

        return indianDay.toLocalDateTime();
    }


    public LocalDateTime getISTEndLocalDateTime(Date date) {

        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        Instant current = date.toInstant();

        ZonedDateTime currentDateTime = ZonedDateTime.ofInstant(current, indianTimeZone);

        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);

        int time = 23 - indianDateTime.getHour();

        int min = 59 - indianDateTime.getMinute();

        ZonedDateTime indianDay = indianDateTime.plusHours(time).plusMinutes(min);

        return indianDay.toLocalDateTime();
    }

    public Date getStartMonthDay(Date curdate) {

        //Date curdate = new Date();
        LocalDate localDate = curdate.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        int month = localDate.getMonthValue();
        int year = localDate.getYear();

        String date = null;
        if (month > 9) {
            date = "01-0" + month + "-" + year;
        } else {
            date = "01-" + month + "-" + year;
        }

        Date result = null;
        if (date != null && date != "") {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            if (date != null) {
                try {
                    result = formatter.parse(date);

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;

    }


    public Date getEndMonthDay() {

        Date curdate = new Date();
        LocalDate localDate = curdate.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        int month = localDate.getMonthValue();
        int year = localDate.getYear();
        String date = getSetEndDate(month, year);
        Date result = null;
        if (date != null && date != "") {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            if (date != null) {
                try {
                    result = formatter.parse(date);

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    public String getSetEndDate(int month, int year) {

        String date = null;

        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
            date = "31-" + month + "-" + year;
        } else if (month == 4 || month == 6 || month == 9 || month == 11) {
            date = "31-" + month + "-" + year;
        } else if (month == 2) {
            date = "28-" + month + "-" + year;
        }
        return date;
    }

    public String getSetStartDate(int month, int year) {

        String date = null;

        if (month > 10) {
            date = "01-" + month + "-" + year;
        } else {
            date = "01-0" + month + "-" + year;
        }
        return date;
    }


    public Long getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH) > b.get(MONTH) ||
                (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
            diff--;
        }
        return Long.valueOf(diff);
    }

    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }


    public Date getCalendarEndMonthDate(Date startDay) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDay);

        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DATE, -1);

        Date lastDayOfMonth = calendar.getTime();

        return lastDayOfMonth;

    }


    public int lastDayOfMonth(int Y, int M) {
        return LocalDate.of(Y, M, 1).getMonth().length(Year.of(Y).isLeap());
    }


    public String getLocalDateTimeToDateString(LocalDateTime localDateTime) {
    	String result = null;
    	
    	if(localDateTime != null) {

        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);
        
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        if (date != null) {
            try {
                result = formatter.format(date);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
        return result;
    }


    public int getMonth() {
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);
        return indianDateTime.getMonth().getValue();
    }


    public int getYear() {
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
        ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);
        return indianDateTime.getYear();
    }


    public String getLocalDateTimeToDateTimeString(LocalDateTime localDateTime) {

        String result = null;

        if (localDateTime != null) {

            /*System.out.println("date localDateTime = " + localDateTime);

            Instant instant = localDateTime.atZone(ZoneId.of("Asia/Kolkata")).toInstant();

            Date date = Date.from(instant);
            System.out.println("date sttttt = " + date);
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss ");
            if (date != null) {
                try {
                    result = formatter.format(date);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }*/
            ZoneId indianTimeZone = ZoneId.of("Asia/Kolkata");
            ZonedDateTime currentDateTime = localDateTime.atZone(indianTimeZone);
            ZonedDateTime indianDateTime = currentDateTime.withZoneSameInstant(indianTimeZone);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss ");
            System.out.println("Current time in IST : " + formatter.format(indianDateTime));
            result = formatter.format(indianDateTime);
        }
        return result;
    }


    public int getMonth(Date date) {
        LocalDate lt = date.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        return lt.getMonth().getValue();
    }

    public int getYear(Date date) {
        LocalDate lt = date.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        return lt.getYear();
    }

    public LocalDateTime getNowLocalDateTime() {
        LocalDateTime lt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
       return lt;
    }



    public  Date stringToDateTime(String date) {
        Date result = null;
        if (date != null && date != "") {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            if (date != null) {
                try {
                    result = formatter.parse(date);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }



    public Date getISTStartDateTime(LocalDateTime localDateTime) {
        Date date = StringDateToDate(getPreviousDayIndianDateTime());
        return date;
    }


    public Date getWeeklyDate(int day) {
        if (day == 1) {
            return getSunday();
        } else if (day == 2) {
            return getMonday();
        } else if (day == 3) {
            return getTuesday();
        } else if (day == 4) {
            return getWednesday();
        } else if (day == 5) {
            return getThursday();
        } else if (day == 6) {
            return getFriday();
        } else if (day == 7) {
            return getSaturday();
        }
        return null;
    }

    public Date getSunday() {
        Calendar date1 = Calendar.getInstance();
        date1.setTime(new Date());

        while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            date1.add(Calendar.DATE, 1);
        }
        return date1.getTime();
    }



    public Date getMonday() {
        Calendar date1 = Calendar.getInstance();
        date1.setTime(new Date());

        while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            date1.add(Calendar.DATE, 1);
        }
        return date1.getTime();
    }


    public Date getTuesday() {
        Calendar date1 = Calendar.getInstance();
        date1.setTime(new Date());

        while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) {
            date1.add(Calendar.DATE, 1);
        }
        return date1.getTime();
    }


    public Date getWednesday() {
        Calendar date1 = Calendar.getInstance();
        date1.setTime(new Date());

        while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
            date1.add(Calendar.DATE, 1);
        }
        return date1.getTime();
    }

    public Date getThursday() {
        Calendar date1 = Calendar.getInstance();
        date1.setTime(new Date());

        while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
            date1.add(Calendar.DATE, 1);
        }
        return date1.getTime();
    }


    public Date getFriday() {
        Calendar date1 = Calendar.getInstance();
        date1.setTime(new Date());

        while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            date1.add(Calendar.DATE, 1);
        }
        return date1.getTime();
    }


    public Date getSaturday() {
        Calendar date1 = Calendar.getInstance();
        date1.setTime(new Date());

        while (date1.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            date1.add(Calendar.DATE, 1);
        }
        return date1.getTime();
    }


    public Date getMonthlyDate(int day) {
        Date curdate = new Date();
        LocalDate localDate = curdate.toInstant().atZone(ZoneId.of("Asia/Kolkata")).toLocalDate();
        int month = localDate.getMonthValue();
        int year = localDate.getYear();

        if(month == 12){
            year++;
        }
        String date = null;
        if(month < 10){
            date = day+"-0"+month+"-"+year;
        }else {
            date = day+"-"+month+"-"+year;
        }

        Date result = null;
        if (date != null && date != "") {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            if (date != null) {
                try {
                    result = formatter.parse(date);

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }




    public Date getLocalDateToDate(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date result = null;
        if (date != null) {
            try {
                String dateVal = dateTimeFormatter.format(date);

                result = formatter.parse(dateVal);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public String getDateTOStringDATETIME(Date date) {
        String result = null;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        if (date != null) {
            try {
                result = formatter.format(date);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public String getDateTOStringMMYYYY(LocalDate date) {
        String result = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/YYYY");
        if (date != null) {
            try {
                result = formatter.format(date);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    
    public Date getStringToDateTime(String date){

        Date liveDate= null;
        SimpleDateFormat sdf =
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        if(date != null) {
            try {
                liveDate = sdf.parse(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return liveDate;
    }


    public String getDateToStringDateTime(Date date){

        String liveDate= null;
        DateTimeFormatter sdf =
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        if(date != null) {
            try {
                liveDate = sdf.format((TemporalAccessor) date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return liveDate;
    }
    
    public long getDaysBetween(Date fromDate, Date toDate) {

        LocalDate start = fromDate.toInstant()
                .atZone(ZoneId.of("Asia/Kolkata"))
                .toLocalDate();

        LocalDate end = toDate.toInstant()
                .atZone(ZoneId.of("Asia/Kolkata"))
                .toLocalDate();

        return ChronoUnit.DAYS.between(start, end) + 1; // inclusive
    }
    public String convertToYYYYMMDD(String inputDate) {

        if (inputDate == null || inputDate.isEmpty()) {
            return null;
        }

        Date parsedDate = null;

        String[] formats = {
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "yyyy-MM-dd",
                "dd MMM yyyy"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                parsedDate = sdf.parse(inputDate);
                break;
            } catch (Exception e) {
                // next format try
            }
        }

        if (parsedDate != null) {
            SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd");
            return output.format(parsedDate);
        }

        return null;
    }
}