package com.aria.service;


import com.aria.dao.AwardRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.AwardsDto;
import com.aria.model.Award;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;
import com.aria.utils.AwardUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AwardService {

    @Autowired
    AwardRepository awardRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AwardUtil awardUtil;

    public ResponseEntity<?> saveAward(AwardsDto awardsDto, User loggedUser) {
        Award award = new Award(awardsDto);
        award.setCreated_date(LocalDateTime.now());
        award.setCreatedBy(loggedUser.getId());
        award.setAwardDate(dateUtil.getStringTODateF1(awardsDto.getAwardDate()));
        award.setAwardStatus("Pending");
        awardRepository.save(award);
        return new ResponseEntity<Status>(new Status("200", "Award saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> saveDummyAward(AwardsDto awardsDto, User loggedUser) {
        awardsDto.setAwardValue(awardUtil.getAwardValue(awardsDto.getAwardCategory()));
        Award award = new Award(awardsDto);
        award.setCreated_date(LocalDateTime.now());
        award.setCreatedBy(loggedUser.getId());
        award.setAwardDate(dateUtil.getStringTODateF1(awardsDto.getAwardDate()));
        award.setAwardStatus("Pending");
        awardRepository.save(award);
        return new ResponseEntity<Status>(new Status("200", "Award saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateAward(AwardsDto awardsDto, User loggedUser) {
        Award award = new Award(awardsDto);
        award.setUpdated_date(LocalDateTime.now());
        award.setUpdatedBy(loggedUser.getId());
        award.setAwardDate(dateUtil.getStringTODateF1(awardsDto.getAwardDate()));
        //award.setAwardStatus("InProgress");
        awardRepository.save(award);
        return new ResponseEntity<Status>(new Status("200", "Award updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllAward(Pageable pageable) throws UsernameNotFoundException {
        Page<Award> awards = awardRepository.findAll(pageable);
        return new ResponseEntity<List<AwardsDto>>(awards.toList().stream().map(user -> {
                    AwardsDto award = new AwardsDto(user);
                    award.setAwardDate(dateUtil.getStringTODateF1(user.getAwardDate()));
                    return award;
                }
        ).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> getAwardDetails(String id) throws UsernameNotFoundException {
        Optional<Award> optionalAward = awardRepository.findById(Long.parseLong(id));
        AwardsDto awardsDto = new AwardsDto(optionalAward.get());
        awardsDto.setAwardDate(dateUtil.getStringTODateF1(optionalAward.get().getAwardDate()));
        return new ResponseEntity<AwardsDto>(awardsDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteAward(String id) throws UsernameNotFoundException {
        try {
            Optional<Award> AwardOptional = awardRepository.findById(Long.parseLong(id));
            if (AwardOptional.isPresent()) {
                Award award = AwardOptional.get();
                awardRepository.delete(award);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public ResponseEntity<?> employeeListAllAward() throws UsernameNotFoundException {

        List<User> userList = userRepository.findAllByUserId(awardRepository.findAllByUser());
        return new ResponseEntity<List<AwardsDto>>(userList.stream().map(award -> {
                    AwardsDto awardsDto = new AwardsDto(award, true);
                    awardsDto.setInvocationCount(Long.valueOf(awardRepository.findByCategoryUser("Innovation", award.getId()).size()));
                    awardsDto.setAttendanceCount(Long.valueOf(awardRepository.findByCategoryUser("Attendance", award.getId()).size()));
                    awardsDto.setMaintenanceCount(Long.valueOf(awardRepository.findByCategoryUser("Maintainance", award.getId()).size()));
                    awardsDto.setProductivityCount(Long.valueOf(awardRepository.findByCategoryUser("Productivity", award.getId()).size()));
                    awardsDto.setTrainingCount(Long.valueOf(awardRepository.findByCategoryUser("Training", award.getId()).size()));
                    return awardsDto;
                }
        ).collect(Collectors.toList()), HttpStatus.OK);
    }


    public ResponseEntity<?> getParticularEmployeeAllAwardList(User loggedUser) throws UsernameNotFoundException {
        System.out.println(dateUtil.getPreviousMonthStartDate() + "  =  " + dateUtil.getEndDate());
        List<Award> awards = awardRepository.findByUser(loggedUser.getId(), dateUtil.getPreviousMonthStartDate(), dateUtil.getEndDate());
        return new ResponseEntity<List<AwardsDto>>(awards.stream().map(award -> {
            AwardsDto awardsDto = new AwardsDto();
            awardsDto.setAwardDate(dateUtil.getStringTODate(award.getAwardDate()));
            awardsDto.setInvocationCount(Long.valueOf(awardRepository.findByCategoryUser("Innovation", award.getUser().getId(), award.getAwardDate()).size()));
            awardsDto.setAttendanceCount(Long.valueOf(awardRepository.findByCategoryUser("Attendance", award.getUser().getId(), award.getAwardDate()).size()));
            awardsDto.setMaintenanceCount(Long.valueOf(awardRepository.findByCategoryUser("Maintainance", award.getUser().getId(), award.getAwardDate()).size()));
            awardsDto.setProductivityCount(Long.valueOf(awardRepository.findByCategoryUser("Productivity", award.getUser().getId(), award.getAwardDate()).size()));
            awardsDto.setTrainingCount(Long.valueOf(awardRepository.findByCategoryUser("Training", award.getUser().getId(), award.getAwardDate()).size()));
            return awardsDto;
        }).collect(Collectors.toList()), HttpStatus.OK);
    }


    public ResponseEntity<?> userByListAllAward(User loggedUser) throws UsernameNotFoundException {
        System.out.println(dateUtil.getStartDate() + "  =  " + dateUtil.getEndDate());
        List<Award> awards = awardRepository.findByUser(loggedUser.getId(), dateUtil.getStartDate(), dateUtil.getEndDate());
        return new ResponseEntity<List<AwardsDto>>(awards.stream().map(award -> {
            AwardsDto awardsDto = new AwardsDto(award);
            awardsDto.setAwardDate(dateUtil.getStringTODate(award.getAwardDate()));
            awardsDto.setInvocationCount(Long.valueOf(awardRepository.findByCategoryUser("Innovation", award.getUser().getId(), award.getAwardDate()).size()));
            awardsDto.setAttendanceCount(Long.valueOf(awardRepository.findByCategoryUser("Attendance", award.getUser().getId(), award.getAwardDate()).size()));
            awardsDto.setMaintenanceCount(Long.valueOf(awardRepository.findByCategoryUser("Maintainance", award.getUser().getId(), award.getAwardDate()).size()));
            awardsDto.setProductivityCount(Long.valueOf(awardRepository.findByCategoryUser("Productivity", award.getUser().getId(), award.getAwardDate()).size()));
            awardsDto.setTrainingCount(Long.valueOf(awardRepository.findByCategoryUser("Training", award.getUser().getId(), award.getAwardDate()).size()));
            return awardsDto;
        }).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> getEmployeeDetailByAwardList(String id, String fromDate, String toDate) throws UsernameNotFoundException {
        Award award = awardRepository.findUser(Long.valueOf(id));
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("employeeName", award.getUser().getEmployeename());
        stringObjectMap.put("employeeId", award.getUser().getId());
        stringObjectMap.put("employeeCategory", award.getUser().getEmployeename());
        stringObjectMap.put("designation", award.getUser().getDesignation().getName());
        stringObjectMap.put("branchId", award.getUser().getBranch_id());
        List<Map<String, Object>> mapList = new ArrayList<>();
        if(fromDate != null && toDate != null) {
            List<Date> dateList = dateUtil.getListOfDatesBetweenTwoDate(dateUtil.getStringTODateDDMMYYYY(fromDate), dateUtil.getStringTODateDDMMYYYY(toDate));
            for (Date date : dateList) {
                Map<String, Object> stringObjectMap1 = new HashMap<>();
                stringObjectMap1.put("date", dateUtil.getStringTODateF1(date));
                stringObjectMap1.put("attendanceCount", awardRepository.findByCategoryUser("Attendance", Long.valueOf(id), date).size());
                stringObjectMap1.put("maintenanceCount", awardRepository.findByCategoryUser("Maintainance", Long.valueOf(id), date).size());
                stringObjectMap1.put("productivityCount", awardRepository.findByCategoryUser("Productivity", Long.valueOf(id), date).size());
                stringObjectMap1.put("trainingCount", awardRepository.findByCategoryUser("Training", Long.valueOf(id), date).size());
                stringObjectMap1.put("invocationCount", awardRepository.findByCategoryUser("Innovation", Long.valueOf(id), date).size());
                mapList.add(stringObjectMap1);
            }
        }
        stringObjectMap.put("awardList",mapList);
        return new ResponseEntity<Map<String, Object>>(stringObjectMap, HttpStatus.OK);
    }
}
