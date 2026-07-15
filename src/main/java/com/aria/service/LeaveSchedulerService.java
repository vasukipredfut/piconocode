package com.aria.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveSchedulerService {


    @Autowired
    InstruLogRepository instruLogRepository;

    @Autowired
    LeaveDetailsRepository leaveDetailsRepository;

    @Autowired
    UserLeaveCountRepo leaveCountRepo;
    
    @Autowired
    DateUtil dateUtil;

    @Autowired
    UserRepository userRepository;

    public void updateLeaveScheduler() {
        System.out.println("###################");

        Date date = dateUtil.getISTDate();
        System.out.println(" IST DATE == " + date);

        System.out.println("###################");
        System.out.println("###################");

        List<InstruLogList> attendanceList = instruLogRepository.findAlls();
        if (!attendanceList.isEmpty() && attendanceList != null) {
            for (InstruLogList attendance : attendanceList) {

                Long count = 0L;
                if (attendance.getCount() >= 240) {
                    count = attendance.getCount() / 20;
                } else if (attendance.getCount() < 240) {
                     count = (attendance.getCount() * 2/3 ) / 20;
                }

                if(count >= 15){
                    count = 15L;
                }

                Optional<User> optional = userRepository.findByBioId(attendance.getBioId());
                if(optional.isPresent()){
                    UserLeaveCount userLeaveCount = new UserLeaveCount();
                    userLeaveCount.setUserId(optional.get().getId());
                    userLeaveCount.setEarn_leave_count(count.intValue());
                    userLeaveCount.setRemaining_leave_count(count.intValue());
                    userLeaveCount.setApprovedDate(date);
                    userLeaveCount.setUpdatedDate(date);
                    leaveCountRepo.save(userLeaveCount);
                }

            }

        }
        System.out.println("###################");
    }

}
