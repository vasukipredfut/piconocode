package com.aria.utils;



import com.aria.dao.PMScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aria.service.*;







@Component
public class EarmsScheduler {


    @Autowired
    BlendingAdviceService blendingAdviceService;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    LeaveSchedulerService leaveSchedulerService;

    @Autowired
    SalarySchedulerService salarySchedulerService;

    @Autowired
    DeliteStockMoveRequestService deliteStockMoveRequestService;



    @Autowired
    PMScheduleService pmScheduleService;

    @Scheduled(cron = "0 0 0 * * ?")
    // @Scheduled(cron = "0 0/5 * * * ?")
    public void updateBlendingScheduler(){
        System.out.println("blending scheduler :: check Execution Time Start :: " + dateUtil.getISTDate());
        try {
            blendingAdviceService.checkBatchDetails();
        } catch (Exception e) {
            System.out.println("Exception occurred while processing script batch"+ e);
        }
        System.out.println("blending scheduler :: check Execution Time end :: " + dateUtil.getISTDate());
    }


    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron = "0 0/5 * * * ?")
    public void updateLeaveScheduler(){
        System.out.println("Leave scheduler :: check Execution Time Start :: " + dateUtil.getISTDate());
        try {
            leaveSchedulerService.updateLeaveScheduler();
        } catch (Exception e) {
            System.out.println("Exception occurred while processing script batch"+ e);
        }
        System.out.println("Leave scheduler :: check Execution Time end :: " + dateUtil.getISTDate());
    }

    //@Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron = "0 0/5 * * * ?")
    public void updateSalaryScheduler(){
        System.out.println("Salary scheduler :: check Execution Time Start :: " + dateUtil.getISTDate());
        try {
            //salarySchedulerService.saveSalaryScheduler(null,null, null);
        } catch (Exception e) {
            System.out.println("Exception occurred while processing script batch"+ e);
        }
        System.out.println("Salary scheduler :: check Execution Time end :: " + dateUtil.getISTDate());
    }




    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron = "0 0/5 * * * ?")
    public void updateDeliteExpireScheduler(){
        System.out.println("Delite scheduler :: check Execution Time Start :: " + dateUtil.getISTDate());
        try {
            deliteStockMoveRequestService.updateDeliteScheduler();
        } catch (Exception e) {
            System.out.println("Exception occurred while processing script batch"+ e);
        }
        System.out.println("Delite scheduler :: check Execution Time end :: " + dateUtil.getISTDate());
    }



    @Scheduled(cron = "0 0 0 * * ?")
    //@Scheduled(cron = "0 0/5 * * * ?")
    public void updatePMScheduleScheduler(){
        System.out.println("pmschedule scheduler :: check Execution Time Start :: " + dateUtil.getISTDate());
        try {
            pmScheduleService.updatePMScheduleScheduler();
        } catch (Exception e) {
            System.out.println("Exception occurred while processing script batch"+ e);
        }
        System.out.println("pmschedule scheduler :: check Execution Time end :: " + dateUtil.getISTDate());
    }


}
