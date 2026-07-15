package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dao.LeaveDetailsRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.LeaveDetailsDto;
import com.aria.dto.LeaveRequestReportDto;
import com.aria.utils.DateUtil;

@Service
public class LeaveDetailsService {

    @Autowired
    protected LeaveDetailsRepository leaveRepository;

    @Autowired
    UserRepository userRepository;


    @Autowired
    UserLeaveCountRepo leaveCountRepo;

    @Autowired
    DateUtil dateUtil;
    @Autowired
    WeekOffRepository weekOffRepository;

    public ResponseEntity<?> leaveRequest(LeaveDetailsDto leaveDetailsDto, User loggedUser) {

        try {
            LeaveDetails leaveDetails = new LeaveDetails(leaveDetailsDto);
            leaveDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            leaveDetails.setStatus("Request");
            leaveDetails.setLeaveFrom(dateUtil.getStringTODateWk(leaveDetailsDto.getLeaveFrom()));
            leaveDetails.setLeaveTo(dateUtil.getStringTODateWk(leaveDetailsDto.getLeaveTo()));
            leaveDetails.setDeptId(loggedUser.getDepartment().getId());
            leaveDetails.setBranchId(loggedUser.getBranch_id());
            Organisation organisation = new Organisation();
            organisation.setId(loggedUser.getOrg_id());
            leaveDetails.setOrganisation(organisation);
            leaveDetails.setCreatedBy(loggedUser.getId());
            LeaveDetails leave = leaveRepository.save(leaveDetails);
            return new ResponseEntity<Status>(new Status("200", "leave request sent successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "leave request not sent"), HttpStatus.OK);

    }

    public ResponseEntity<?> updateLeaveRequest(LeaveDetailsDto leaveDetailsDto, User loggedUser) {

        try {
            LeaveDetails leaveDetails = new LeaveDetails(leaveDetailsDto);
            leaveDetails.setLeaveFrom(dateUtil.getStringTODateWk(leaveDetailsDto.getLeaveFrom()));
            leaveDetails.setLeaveTo(dateUtil.getStringTODateWk(leaveDetailsDto.getLeaveTo()));
            leaveDetails.setDeptId(loggedUser.getDepartment().getId());
            Organisation organisation = new Organisation();
            organisation.setId(loggedUser.getOrg_id());
            leaveDetails.setOrganisation(organisation);
            leaveDetails.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            leaveDetails.setUpdatedBy(loggedUser.getId());
            LeaveDetails leave = leaveRepository.save(leaveDetails);
            return new ResponseEntity<Status>(new Status("200", "leave request updated successfully!!!"),
                    HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "leave request not updated"), HttpStatus.OK);

    }


    public ResponseEntity<?> approveOrReject(LeaveDetailsDto leaveDetailsDto, User loggedUser) {
        LeaveDetails leaveDetails = new LeaveDetails(leaveDetailsDto);
        leaveDetails.setLeaveFrom(dateUtil.getStringTODateWk(leaveDetailsDto.getLeaveFrom()));
        leaveDetails.setLeaveTo(dateUtil.getStringTODateWk(leaveDetailsDto.getLeaveTo()));
        leaveDetails.setDeptId(loggedUser.getDepartment().getId());
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        leaveDetails.setOrganisation(organisation);
        leaveDetails.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        leaveDetails.setUpdatedBy(loggedUser.getId());
        LeaveDetails leave = leaveRepository.save(leaveDetails);
        if (leave.getLeaveType().equalsIgnoreCase("LeaveWithWages")) {
            if (leave.getStatus() != null && leave.getStatus().equalsIgnoreCase("Approve")) {
                Optional<UserLeaveCount> userLeaveCount = leaveCountRepo.findById(leave.getUserId());
                if (userLeaveCount.isPresent()) {
                    UserLeaveCount userLeave = userLeaveCount.get();
                    if (userLeave.getRemaining_leave_count() > 0) {
                        int remain = userLeave.getRemaining_leave_count() - leave.getNoOfDays();
                        int rs = Integer.signum(remain);
                        if (rs == 1) {
                            userLeave.setRemaining_leave_count(remain);
                        } else {
                            userLeave.setRemaining_leave_count(0);
                        }

                    }
                    userLeave.setApprovedDate(dateUtil.getISTDate());
                    leaveCountRepo.save(userLeave);
                }
            }
        }
        return new ResponseEntity<Status>(new Status("200", "leave " + leaveDetails.getStatus() + " sent successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAllRequest(Long dept, String employeeName, String designationName, String roleName, String leaveType, String leaveStatus, String fromDate, String toDate, Pageable pageable) throws UsernameNotFoundException {

        List<String> listDesignation = new ArrayList<>();

        Page<LeaveDetailsList> list = null;

        if (designationName != null) {

            if (designationName.equalsIgnoreCase("Dy._General_Manager")) {

                listDesignation.add("Plant_Manager");

                list = leaveRepository.findByIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

            } else if (designationName.equalsIgnoreCase("Plant_Manager")) {

                if (leaveStatus != null) {

                    if (leaveStatus.equalsIgnoreCase("Verify")) {

                        listDesignation.add("Plant_Manager");
                        listDesignation.add("Dy._General_Manager");

                        list = leaveRepository.findByNOTIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

                    } else if (leaveStatus.equalsIgnoreCase("Request")) {

                        listDesignation.add("Department_Manager");

                        list = leaveRepository.findByIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

                    }

                } else {

                    listDesignation.add("Plant_Manager");
                    listDesignation.add("Dy._General_Manager");

                    list = leaveRepository.findByNOTINPlantMan(dept, employeeName, listDesignation, leaveType, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);
                }

            } else if (designationName.equalsIgnoreCase("Department_Manager")) {

                listDesignation.add("Plant_Manager");
                listDesignation.add("Department_Manager");
                listDesignation.add("Dy._General_Manager");

                list = leaveRepository.findByNOTIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

            } else if (designationName.equalsIgnoreCase("Managing_Director")) {

                listDesignation.add("Plant_Manager");
                listDesignation.add("Dy._General_Manager");
                listDesignation.add("Department_Manager");

                list = leaveRepository.findByIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

            } else {
                if (roleName != null) {
                    if (roleName.equalsIgnoreCase("Plant Manager") || roleName.equalsIgnoreCase("plant 1 manager")) {

                        if (leaveStatus != null) {

                            if (leaveStatus.equalsIgnoreCase("Verify")) {

                                listDesignation.add("Plant_Manager");
                                listDesignation.add("Dy._General_Manager");

                                list = leaveRepository.findByNOTIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

                            } else if (leaveStatus.equalsIgnoreCase("Request")) {

                                listDesignation.add("Department_Manager");

                                list = leaveRepository.findByIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

                            }

                        } else {

                            listDesignation.add("Dy._General_Manager");

                            list = leaveRepository.findByNOTIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);
                        }
                    } else if (roleName.equalsIgnoreCase("General Manager")) {

                        listDesignation.add("Plant_Manager");

                        list = leaveRepository.findByIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

                    } else if (roleName.equalsIgnoreCase("Manager")) {

                        listDesignation.add("Plant_Manager");
                        listDesignation.add("Department_Manager");
                        listDesignation.add("Dy._General_Manager");

                        list = leaveRepository.findByNOTIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

                    } else if (roleName.equalsIgnoreCase("Managing Director")) {

                        listDesignation.add("Plant_Manager");
                        listDesignation.add("Dy._General_Manager");

                        list = leaveRepository.findByIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

                    }
                }
            }

        } else {
            if (roleName != null) {
                if (roleName.equalsIgnoreCase("Managing Director")) {

                    listDesignation.add("Plant_Manager");
                    listDesignation.add("Dy._General_Manager");

                    list = leaveRepository.findByIN(dept, employeeName, listDesignation, leaveType, leaveStatus, dateUtil.getStringTODateF1(fromDate), dateUtil.getStringTODateF1(toDate), pageable);

                }
            }
        }


        if (list != null && !list.toList().isEmpty()) {

            List<LeaveDetailsDto> result = list.toList().stream().map(data -> {
                LeaveDetailsDto leaveDetailsDto = new LeaveDetailsDto(data, leaveRepository);
                leaveDetailsDto.setLeaveFrom(dateUtil.getDateTOStringWk(data.getLeaveFrom()));
                leaveDetailsDto.setLeaveTo(dateUtil.getDateTOStringWk(data.getLeaveTo()));
                updateEmployeeDetails(leaveDetailsDto);
                return leaveDetailsDto;
            }).collect(Collectors.toList());
            PageDataDto<LeaveDetailsDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(pageData, HttpStatus.OK);

        } else {

            return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);

        }
    }

    public ResponseEntity<?> leaveRequestListByUserId(Long userId, Pageable pageable) throws UsernameNotFoundException {

        Page<LeaveDetails> list = leaveRepository.findByUserId(userId, pageable);
        if (!list.isEmpty()) {
            List<LeaveDetailsDto> result = list.toList().stream().map(data -> {
                LeaveDetailsDto leaveDetailsDto = new LeaveDetailsDto(data);
                leaveDetailsDto.setLeaveFrom(dateUtil.getDateTOStringWk(data.getLeaveFrom()));
                leaveDetailsDto.setLeaveTo(dateUtil.getDateTOStringWk(data.getLeaveTo()));
                updateEmployeeDetails(leaveDetailsDto);
                return leaveDetailsDto;
            }).collect(Collectors.toList());
            PageDataDto<LeaveDetailsDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> leaveRequestReportByUserId(Long userId) throws UsernameNotFoundException {

        String status = "Approve";
        Integer earnedLeavesCount = leaveCountRepo.getYearnedLeave(userId);
        Integer remainingLeaveLeavesCount = leaveCountRepo.getRemainingLeave(userId);

        Integer leaveWithWagesLeaveCount = leaveRepository.getLeaveCountByUserIdAndLeaveType(userId, "LeaveWithWages", status);
        Integer maternityLeaveCount = leaveRepository.getLeaveCountByUserIdAndLeaveType(userId, "Maternity", status);
        Integer lossOfPayLeaveCount = leaveRepository.getLeaveCountByUserIdAndLeaveType(userId, "LossOfPay", status);
        Integer acceidentLeaveCount = leaveRepository.getLeaveCountByUserIdAndLeaveType(userId, "AcceidentLeave", status);

        LeaveRequestReportDto leaveRequestReportDto = new LeaveRequestReportDto();
        leaveRequestReportDto.setEarnedLeavesCount(earnedLeavesCount);
        leaveRequestReportDto.setLeaveWithWagesCount(leaveWithWagesLeaveCount);
        leaveRequestReportDto.setMaternityCount(maternityLeaveCount);
        leaveRequestReportDto.setLossOfPayCount(lossOfPayLeaveCount);
        leaveRequestReportDto.setAccidentLeaveCount(acceidentLeaveCount);
        leaveRequestReportDto.setRemainingLeaveCount(remainingLeaveLeavesCount);
        Optional<Weekoff> optionalWeekoff = weekOffRepository.findById(userId, 0);
        if (optionalWeekoff.isPresent()) {
            leaveRequestReportDto.setWeekOffDay(optionalWeekoff.get().getWeekOffDay().getWeekday());
        }


        return new ResponseEntity<LeaveRequestReportDto>(leaveRequestReportDto, HttpStatus.OK);
    }

    public ResponseEntity<?> listAllRejectOrApprove(Pageable pageable) throws UsernameNotFoundException {

        Page<LeaveDetails> list = leaveRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<LeaveDetailsDto> result = list.toList().stream().map(data -> {
                LeaveDetailsDto leaveDetailsDto = new LeaveDetailsDto(data);
                leaveDetailsDto.setLeaveFrom(dateUtil.getDateTOStringWk(data.getLeaveFrom()));
                leaveDetailsDto.setLeaveTo(dateUtil.getDateTOStringWk(data.getLeaveTo()));
                updateEmployeeDetails(leaveDetailsDto);
                return leaveDetailsDto;
            }).collect(Collectors.toList());

            return new ResponseEntity<List<LeaveDetailsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<LeaveDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getLeaveDetails(String id) throws UsernameNotFoundException {
        Optional<LeaveDetails> optionalLeaveDetails = leaveRepository.findById(Long.parseLong(id));
        LeaveDetailsDto leaveDetailsDto = new LeaveDetailsDto(optionalLeaveDetails.get());
        leaveDetailsDto.setLeaveFrom(dateUtil.getDateTOStringWk(optionalLeaveDetails.get().getLeaveFrom()));
        leaveDetailsDto.setLeaveTo(dateUtil.getDateTOStringWk(optionalLeaveDetails.get().getLeaveTo()));
        updateEmployeeDetails(leaveDetailsDto);
        return new ResponseEntity<LeaveDetailsDto>(leaveDetailsDto, HttpStatus.OK);
    }


    public ResponseEntity<?> verifyOrApprove(String id, String type, String leaveType, String status, String reasonForRejection, User loggedUser) throws UsernameNotFoundException {
        Optional<LeaveDetails> optionalLeaveDetails = leaveRepository.findById(Long.parseLong(id));
        if (optionalLeaveDetails.isPresent()) {
            LeaveDetails leaveDetails = optionalLeaveDetails.get();
            if (type.equalsIgnoreCase("verify")) {
                leaveDetails.setVerified_by(loggedUser.getId());
                leaveDetails.setVerified_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                leaveDetails.setLeaveType(leaveType);
            } else if (type.equalsIgnoreCase("approve")) {
                leaveDetails.setApproved_by(loggedUser.getId());
                leaveDetails.setApproved__date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                leaveDetails.setLeaveType(leaveType);
            } else if (type.equalsIgnoreCase("reject")) {
                leaveDetails.setRejected_by(loggedUser.getId());
                leaveDetails.setRejected__date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                leaveDetails.setReasonForRejection(reasonForRejection);
            }
            leaveDetails.setStatus(status);
            leaveRepository.save(leaveDetails);
            return new ResponseEntity<Status>(new Status("200", "leave approve OR verify updated successfully!!!"), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("200", "leave request updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> deleteLeave(String id) throws UsernameNotFoundException {
        try {
            Optional<LeaveDetails> optionalLeaveDetails = leaveRepository.findById(Long.parseLong(id));
            if (optionalLeaveDetails.isPresent()) {
                LeaveDetails leaveDetails = optionalLeaveDetails.get();
                leaveRepository.delete(leaveDetails);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateEmployeeDetails(LeaveDetailsDto pageDto) {

        if (pageDto.getApproveBy() != null) {
            Optional<User> user1 = userRepository.findById(pageDto.getApproveBy());
            if (user1.isPresent()) {
                pageDto.setApproveByName(user1.get().getEmployeename());
            }
        }

        if (pageDto.getRejectedBy() != null) {
            Optional<User> user2 = userRepository.findById(pageDto.getRejectedBy());
            if (user2.isPresent()) {
                pageDto.setRejectedByName(user2.get().getEmployeename());
            }
        }

        if (pageDto.getVerifiedBy() != null) {
            Optional<User> user3 = userRepository.findById(pageDto.getVerifiedBy());
            if (user3.isPresent()) {
                pageDto.setVerifiedByName(user3.get().getEmployeename());
            }
        }
    }


    public ResponseEntity<?> leaveMasterRegList(Long branch, Long dept, String employeeName, Long empId, String fromDate, String toDate) throws UsernameNotFoundException {

        Date startDate = null;
        if (fromDate != null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
        }
        Date endDate = null;
        if (toDate != null) {
            endDate = dateUtil.getStringTODateWk(toDate);
        }
        List<Date> dateList = null;
        if (startDate != null && endDate != null) {
            dateList = populateDateList(startDate, endDate);
        }

        List<Long> empIdList = leaveRepository.findBy(branch, dept, empId, startDate, endDate);



        if (!empIdList.isEmpty()) {

            List<LeaveRegisterList> registerLists = new ArrayList<>();

            if (dateList != null && !dateList.isEmpty()) {
                for (Date shiftDate : dateList) {
                    List<LeaveRegisterList> userList = leaveRepository.findBy(branch, dept, empId, shiftDate, empIdList);
                    if (!userList.isEmpty()) {
                        registerLists.addAll(userList);
                    }
                }
            }

            if (registerLists != null && !registerLists.isEmpty()) {


                registerLists.sort(Comparator.comparing(LeaveRegisterList::getShiftDate).reversed());

                List<LeaveRegisterListDTO> result = registerLists.stream().map(data -> {
                    LeaveRegisterListDTO leaveDetailsDto = new LeaveRegisterListDTO(data);
                    return leaveDetailsDto;
                }).collect(Collectors.toList());

                return new ResponseEntity<List<LeaveRegisterListDTO>>(result, HttpStatus.OK);

            } else {
                return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);
            }

        } else {
            return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }


    private List<Date> populateDateList(Date startDate, Date toDate) {

        Date current = startDate;

        List<Date> dateRanges = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        dateRanges.add(current);

        while (current.before(toDate)) {
            calendar = Calendar.getInstance();
            calendar.setTime(current);
            calendar.add(Calendar.DATE, 1);
            current = calendar.getTime();
            dateRanges.add(current);
        }

        return dateRanges;
    }


    public ResponseEntity<?> printLeaveRegisterList(Long branch, Long dept, Long empId,
                                                    String fromDate, String toDate) throws UsernameNotFoundException {

        Date startDate = null;
        if (fromDate != null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
        }
        Date endDate = null;
        if (toDate != null) {
            endDate = dateUtil.getStringTODateWk(toDate);
        }
        List<Date> dateList = null;
        if (startDate != null && endDate != null) {
            dateList = populateDateList(startDate, endDate);
        }

        List<Long> empIdList = leaveRepository.findByNotApp(branch, dept, empId, startDate, endDate);



        if (!empIdList.isEmpty()) {

            List<LeaveRegisterList> registerLists = new ArrayList<>();

            if (dateList != null && !dateList.isEmpty()) {
                for (Date shiftDate : dateList) {
                    List<LeaveRegisterList> userList = leaveRepository.findByNotInApproved(branch, dept, shiftDate, empIdList);
                    if (!userList.isEmpty()) {
                        registerLists.addAll(userList);
                    }
                }
            }

            if (registerLists != null && !registerLists.isEmpty()) {


                registerLists.sort(Comparator.comparing(LeaveRegisterList::getShiftDate).reversed());

                List<LeaveRegisterListDTO> result = registerLists.stream().map(data -> {
                    LeaveRegisterListDTO leaveDetailsDto = new LeaveRegisterListDTO(data);
                    leaveDetailsDto.setStatus("Yet to Approve");
                    return leaveDetailsDto;
                }).collect(Collectors.toList());

                return new ResponseEntity<List<LeaveRegisterListDTO>>(result, HttpStatus.OK);

            } else {
                return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);
            }

        } else {
            return new ResponseEntity<PageDataDto<LeaveDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }



    public ResponseEntity<?> validationLeaveRequest(LeaveDetailsDto leaveDetailsDto, User loggedUser) {

        try {
            LeaveDetails leaveDetails = new LeaveDetails(leaveDetailsDto);
            List<LeaveDetails> list = leaveRepository.findBy(leaveDetails.getUserId(),
                    dateUtil.getStringTODateWk(leaveDetailsDto.getLeaveFrom()),
                    dateUtil.getStringTODateWk(leaveDetailsDto.getLeaveTo()));
            if (list.isEmpty()) {
                return new ResponseEntity<Status>(new Status("200", "create leave request allowed!!!"), HttpStatus.OK);
            } else {
                return new ResponseEntity<Status>(new Status("400", "already exists same days!!!"), HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "leave request not sent"), HttpStatus.OK);

    }
}
