package com.aria.utils;

import com.aria.dao.DayEndStockClosingSummaryRepository;
import com.aria.model.DayEndStockClosingSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DayEndUtils {

    Logger logger = LoggerFactory.getLogger(DayEndUtils.class);

    @Autowired
    DayEndStockClosingSummaryRepository dayEndStockClosingSummaryRepository;


    public Date getAccountingDate(Long branch, Date currentDate) {
        Date acountingDate = null;
        TimeZone istTimeZone = TimeZone.getTimeZone("Asia/Kolkata");
        Optional<DayEndStockClosingSummary> dayEndStockClosingSummary = dayEndStockClosingSummaryRepository.findByCompletedDateAndBranch(branch, currentDate);
        if (dayEndStockClosingSummary.isPresent()) {
            System.out.println("=======Day end closing =======");
            Calendar calendar = GregorianCalendar.getInstance(istTimeZone);
            calendar.setTime(currentDate);
            calendar.add(Calendar.DATE, 1);
            acountingDate = calendar.getTime();
            System.out.println(" accounting is upcomming date == " + acountingDate);
        } else {
            Optional<DayEndStockClosingSummary> checkDate = dayEndStockClosingSummaryRepository.checkLastDate(branch);
            if (checkDate.isPresent()) {
                Calendar calendar = GregorianCalendar.getInstance(istTimeZone);
                calendar.setTime(checkDate.get().getAdate());
                calendar.add(Calendar.DATE, 1);
                acountingDate = calendar.getTime();
                System.out.println(" accounting is last close date == " + acountingDate);
            } else {
                System.out.println(" accounting is current date == " + currentDate);
                acountingDate = currentDate;
            }
        }
        System.out.println(" accounting == " + currentDate);
        return acountingDate;
    }


    public boolean checkAccountingStatus(Long branch, Date currentDate) {

        Boolean status = false;

        Optional<DayEndStockClosingSummary> dayEndStockClosingSummary = dayEndStockClosingSummaryRepository.findByCompletedDateAndBranch(branch, currentDate);
        if (dayEndStockClosingSummary.isPresent()) {
            status = true;
        }
        return status;
    }
}
