package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;

import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShiftRotationService {

    @Autowired
    ShiftRotationRepository shiftRotationRepository;

    @Autowired
    ShiftRotationMappingRepository rotationMappingRepository;

    @Autowired
    ShiftAssignRepository shiftAssignRepository;

    @Autowired
    WeekOffRepository weekOffRepository;

    @Autowired
    WeekDayRepository weekDayRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShiftsRepository shiftRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    ShiftAllotmentRepository shiftAllotmentRepository;

    @Autowired
    EmastDailyRepository emastDailyRepository;

    @Autowired
    AssignDutyRepository assignDutyRepository;

    @Autowired
    LeaveDetailsRepository leaveDetailsRepository;


    public ResponseEntity<?> saveShiftRotation(ShiftRotationDto shiftRotationDto, User loggedUser) {
        ShiftRotation shiftRotation = new ShiftRotation(shiftRotationDto);
        shiftRotation.setCreated_date(dateUtil.getLocalDateTime(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
        shiftRotation.setCreatedBy(loggedUser.getId());
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        shiftRotation.setOrganisation(organisation);
        shiftRotation.getBranch().setId(shiftRotationDto.getBranchId());

        if (shiftRotationDto.getShiftName() != null && shiftRotationDto.getBranchId() > 0) {

            // List<Shifts> shifts = shiftRepository.findByShifts(shiftRotationDto.getShiftName(),shiftRotationDto.getBranchId());

            List<Shifts> shifts = shiftRepository.findByShifts(shiftRotationDto.getShiftName());

            if (shifts != null && shifts.size() > 0) {
                for (Shifts shift : shifts) {
                    if (shift.getShiftnumber() == 1) {
                        shiftRotation.setShift1_id(shift.getId());
                    }
                    if (shift.getShiftnumber() == 2) {
                        shiftRotation.setShift2_id(shift.getId());
                    }
                    if (shift.getShiftnumber() == 3) {
                        shiftRotation.setShift3_id(shift.getId());
                    }
                }
            }
            int sizeofemployees = 0;

            if (shiftRotationDto.getShift1Staffs() != null && !shiftRotationDto.getShift1Staffs().isEmpty()) {
                sizeofemployees = sizeofemployees + shiftRotationDto.getShift1Staffs().size();
                //*********************** update function call is not required, as if the employee exempted on particular shift, ***********************
                //*********************** he automatically falls in next shift available, we should not throw error which is not correct ***********************
                //     update(shiftRotation.getShift1_id(), shiftRotationDto.getShift1Staffs());
            }
            if (shiftRotationDto.getShift2Staffs() != null && !shiftRotationDto.getShift2Staffs().isEmpty()) {
                sizeofemployees = sizeofemployees + shiftRotationDto.getShift2Staffs().size();
                //    update(shiftRotation.getShift2_id(), shiftRotationDto.getShift1Staffs());
            }
            if (shiftRotationDto.getShift3Staffs() != null && !shiftRotationDto.getShift3Staffs().isEmpty()) {
                sizeofemployees = sizeofemployees + shiftRotationDto.getShift3Staffs().size();
                //     update(shiftRotation.getShift3_id(), shiftRotationDto.getShift1Staffs());
            }

            shiftRotation.setNoOfEmployees(String.valueOf(sizeofemployees));
            ShiftRotation result = shiftRotationRepository.save(shiftRotation);
            updateRotation(result, shiftRotationDto);
            updateShiftRotationMapping(result.getId(), shiftRotationDto, shifts, loggedUser);
            return new ResponseEntity<Status>(new Status("200", "shift rotation save successfully!!!"), HttpStatus.OK);

        } else {
            return new ResponseEntity<Status>(new Status("500", "shift rotation failure!!!"), HttpStatus.OK);

        }
    }


    /* To Store RotationId for each Staff ensuring they have been assigned Shift Rotation part of which Rotation*/

    public void updateRotation(ShiftRotation rotation, ShiftRotationDto shiftRotationDto) {
        List<ShiftRotationMapping> shiftRotationMappinglist = new ArrayList<ShiftRotationMapping>();

        if (shiftRotationDto.getShift1Staffs() != null && !shiftRotationDto.getShift1Staffs().isEmpty()) {

            for (ShiftRotationMappingDto shiftRotationMappingDto : shiftRotationDto.getShift1Staffs()) {
                ShiftRotationMapping shiftRotationMapping = new ShiftRotationMapping(shiftRotationMappingDto);
                shiftRotationMapping.setShiftRotationId(rotation.getId());
                shiftRotationMapping.setShift("Shift1Staffs");
                shiftRotationMappinglist.add(shiftRotationMapping);
            }


        }
        if (shiftRotationDto.getShift2Staffs() != null && !shiftRotationDto.getShift2Staffs().isEmpty()) {
            for (ShiftRotationMappingDto shiftRotationMappingDto : shiftRotationDto.getShift2Staffs()) {
                ShiftRotationMapping shiftRotationMapping = new ShiftRotationMapping(shiftRotationMappingDto);
                shiftRotationMapping.setShiftRotationId(rotation.getId());
                shiftRotationMapping.setShift("Shift2Staffs");
                shiftRotationMappinglist.add(shiftRotationMapping);
            }
        }
        if (shiftRotationDto.getShift3Staffs() != null && !shiftRotationDto.getShift3Staffs().isEmpty()) {
            for (ShiftRotationMappingDto shiftRotationMappingDto : shiftRotationDto.getShift3Staffs()) {
                ShiftRotationMapping shiftRotationMapping = new ShiftRotationMapping(shiftRotationMappingDto);
                shiftRotationMapping.setShiftRotationId(rotation.getId());
                shiftRotationMapping.setShift("Shift3Staffs");
                shiftRotationMappinglist.add(shiftRotationMapping);
            }
        }
        rotationMappingRepository.saveAll(shiftRotationMappinglist);

    }

    public ResponseEntity<Status> update(Long shiftId, List<ShiftRotationMappingDto> rotationMappingDtos) {
        for (ShiftRotationMappingDto shiftMap : rotationMappingDtos) {
            List<ShiftAllotment> shiftAllotmentList = shiftAllotmentRepository.findAll(shiftId, shiftMap.getEmpId());
            if (!shiftAllotmentList.isEmpty()) {
                return new ResponseEntity<Status>(new Status("400", shiftMap.getEmployeeName() + " This User Exempted for " + shiftId + " this shift!!!"), HttpStatus.OK);
            }
        }
        return null;
    }


    public ResponseEntity<?> updateShiftRotation(ShiftRotationDto shiftRotationDto, User loggedUser) {
        ShiftRotation shiftRotation = new ShiftRotation(shiftRotationDto);
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        shiftRotation.setOrganisation(organisation);
        shiftRotation.setFromDate(dateUtil.getStringTODateWk(shiftRotationDto.getFromDate()));
        shiftRotation.setToDate(dateUtil.getStringTODateWk(shiftRotationDto.getToDate()));
        shiftRotation.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        shiftRotation.setUpdatedBy(loggedUser.getId());
        ShiftRotation result = shiftRotationRepository.save(shiftRotation);
        return new ResponseEntity<Status>(new Status("200", "shift rotation updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAll() throws UsernameNotFoundException {

        List<ShiftRotation> list = shiftRotationRepository.findAll();
        if (!list.isEmpty()) {
            List<ShiftRotationDto> result = list.stream().map(data -> {
                ShiftRotationDto shiftRotationDto = new ShiftRotationDto(data);
                if (data.getFromDate() != null) {
                    shiftRotationDto.setFromDate(dateUtil.getStringTODate(data.getFromDate()));
                }
                if (data.getToDate() != null) {
                    shiftRotationDto.setToDate(dateUtil.getStringTODate(data.getToDate()));
                }
                updateEmployeeDetails(shiftRotationDto);
                updateStaff(shiftRotationDto);
                return shiftRotationDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<ShiftRotationDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ShiftRotationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> filterShiftRotationList(Long branchId, String type, Long departmentId, String employeeType,
                                                     String dateFrom, String dateTo, Pageable pageable) throws UsernameNotFoundException {

        HashMap<String, LocalDateTime> dateHashMap = dateDynamic(dateFrom, dateTo);


        Page<ListShiftRotation> list = null;
        if (type.equalsIgnoreCase("staff")) {
            list = shiftRotationRepository.filterShiftRotationEmpList(branchId, departmentId,
                    employeeType, dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"), 0, pageable);
        } else {
            list = shiftRotationRepository.filterShiftRotationEmastDailyList(branchId, departmentId,
                    employeeType, dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"), 1, pageable);
        }
        if (!list.toList().isEmpty()) {
            List<ListShiftRotationDto> result = list.toList().stream().map(data -> {
                ListShiftRotationDto shiftRotationDto = new ListShiftRotationDto(data);
                return shiftRotationDto;
            }).collect(Collectors.toList());
            PageDataDto pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> ViewShifts(String employeeCategory, int emp_type, Long branchId, Long departmentId, String dateFrom, String dateTo, String shiftName) throws UsernameNotFoundException {

        Date fromDate = null;
        Date toDate = null;
        if (dateFrom != null) {
            fromDate = dateUtil.getStringTODateWk(dateFrom);
        }
        if (dateTo != null) {
            toDate = dateUtil.getStringTODateWk(dateTo);
        }

        List<ViewShiftsDto> viewShifts = new ArrayList<ViewShiftsDto>();
        System.out.println(" fromDate = " + fromDate);
        System.out.println(" toDate = " + toDate);
        System.out.println(" shiftName = " + shiftName);
        System.out.println(" departmentId = " + departmentId);
        System.out.println(" branchId = " + branchId);


        if (emp_type == 0) {


            List<ShiftAssignUserList> assignList = shiftAssignRepository.viewUserlist(employeeCategory , branchId, departmentId, fromDate, toDate, shiftName);

            if (!assignList.isEmpty())

                for (ShiftAssignUserList shiftAssign : assignList) {


                    Map<String, String> dateMap = getLeaveDate(shiftAssign.getEmpId());

                    ViewShiftsDto viewShift = new ViewShiftsDto(shiftAssign);


                    List<ShiftAssign> shiftAssignList = shiftAssignRepository.viewfilterlist(emp_type, viewShift.getEmpId(), branchId, departmentId, fromDate, toDate, shiftName);


                    List<ShiftPeriod> shiftsData = new ArrayList<ShiftPeriod>();

                    for (ShiftAssign assign : shiftAssignList) {

                        String shiftDate = dateUtil.getDateTOStringWk(assign.getShift_date());
                        ShiftPeriod shiftPeriod = new ShiftPeriod();
                        shiftPeriod.setDate(shiftDate);

                        if (dateMap.containsKey(shiftDate)) {
                            if (assign.getShift_value() != null && assign.getShift_value().equalsIgnoreCase("W")) {
                                shiftPeriod.setShift(assign.getShift_value());
                                shiftPeriod.setShift_details(assign.getShifts());
                            } else {
                                shiftPeriod.setShift("L");
                                shiftPeriod.setShift_details(assign.getShifts());
                            }
                        } else {
                            if (assign.getShift_value() != null && assign.getShift_value().equalsIgnoreCase("W")) {
                                shiftPeriod.setShift(assign.getShift_value());
                            } else {
                                shiftPeriod.setShift(assign.getShifts().getShiftName());
                            }
                            shiftPeriod.setShift_details(assign.getShifts());
                        }
                        shiftsData.add(shiftPeriod);
                    }
                    viewShift.setShifts(shiftsData);
                    viewShifts.add(viewShift);
                }
        } else if (emp_type == 1) {


            List<ShiftAssignUserList> assignList = shiftAssignRepository.viewEmastUserlist(employeeCategory, branchId, departmentId, fromDate, toDate, shiftName);
            if (!assignList.isEmpty())
                for (ShiftAssignUserList shiftAssign : assignList) {

                    ViewShiftsDto viewShift = new ViewShiftsDto(shiftAssign);

                    List<ShiftAssign> shiftAssignList = shiftAssignRepository.viewfilterlist(emp_type, viewShift.getEmpId(), branchId, departmentId, fromDate, toDate, shiftName);

                    List<ShiftPeriod> shiftsData = new ArrayList<ShiftPeriod>();
                    for (ShiftAssign assign : shiftAssignList) {
                        String shiftDate = dateUtil.getDateTOStringWk(assign.getShift_date());
                        ShiftPeriod shiftPeriod = new ShiftPeriod();
                        shiftPeriod.setDate(shiftDate);
                        shiftPeriod.setShift(assign.getShifts().getShiftName());
                        if (assign.getShift_value() != null && assign.getShift_value().equalsIgnoreCase("W")) {
                            shiftPeriod.setShift("W");
                        } else {
                            shiftPeriod.setShift(assign.getShifts().getShiftName());
                        }
                        shiftPeriod.setShift_details(assign.getShifts());
                        shiftsData.add(shiftPeriod);
                    }
                    viewShift.setShifts(shiftsData);
                    viewShifts.add(viewShift);

                }
        }

        return new ResponseEntity<List<ViewShiftsDto>>(viewShifts, HttpStatus.OK);
    }

    private List<Integer> shiftOff(Shifts shift) {
        List<Integer> shiftList = new ArrayList<Integer>();
        if (shift.getFri() != null && !shift.getFri().equals("ON")) {
            shiftList.add(6);
        }
        if (shift.getSun() != null && !shift.getSun().equals("ON")) {
            shiftList.add(1);
        }
        if (shift.getMon() != null && !shift.getMon().equals("ON")) {
            shiftList.add(2);
        }
        if (shift.getTue() != null && !shift.getTue().equals("ON")) {
            shiftList.add(3);
        }
        if (shift.getWed() != null && !shift.getWed().equals("ON")) {
            shiftList.add(4);
        }
        if (shift.getThu() != null && !shift.getThu().equals("ON")) {
            shiftList.add(5);
        }
        if (shift.getSat() != null && !shift.getSat().equals("ON")) {
            shiftList.add(7);
        }
        return shiftList;

    }

    private List<String> iterateBetweenDates(Date start, Date end) {
        Date current = start;
        List<String> shiftDates = new ArrayList<>();

        shiftDates.add(dateUtil.getDateToStringDDMMYYYY(current));

        while (current.before(end)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(current);
            calendar.add(Calendar.DATE, 1);
            current = calendar.getTime();
            shiftDates.add(dateUtil.getDateToStringDDMMYYYY(current));
        }
        return shiftDates;
    }

    public ResponseEntity<?> getShiftDetails(String id) throws UsernameNotFoundException {
        Optional<ShiftRotation> optionalShiftRotation = shiftRotationRepository.findById(Long.parseLong(id));
        ShiftRotationDto shift = new ShiftRotationDto(optionalShiftRotation.get());
        // shift.setFromDate(dateUtil.getStringTODate(optionalShiftRotation.get().getFromDate()));
        shift.setToDate(dateUtil.getStringTODate(optionalShiftRotation.get().getToDate()));
        updateStaff(shift);
        updateEmployeeDetails(shift);
        return new ResponseEntity<ShiftRotationDto>(shift, HttpStatus.OK);
    }

    public ResponseEntity<?> getShiftRotationViewDetails(String id) throws UsernameNotFoundException {
        Optional<ShiftRotation> optionalShiftRotation = shiftRotationRepository.findById(Long.parseLong(id));
        ShiftRotationDto shift = new ShiftRotationDto(optionalShiftRotation.get());

        updateStaffView(shift);
        updateEmployeeDetails(shift);
        return new ResponseEntity<ShiftRotationDto>(shift, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteShift(String id) throws UsernameNotFoundException {
        try {
            Optional<ShiftRotation> optionalShiftRotation = shiftRotationRepository.findById(Long.parseLong(id));
            if (optionalShiftRotation.isPresent()) {
                ShiftRotation shiftRotation = optionalShiftRotation.get();
                List<ShiftAssign> shiftAssignList = shiftAssignRepository.findByRotationId(shiftRotation.getId());
                if (shiftAssignList != null && !shiftAssignList.isEmpty()) {
                    shiftAssignRepository.deleteAll(shiftAssignList);
                }
                shiftRotationRepository.delete(shiftRotation);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateEmployeeDetails(ShiftRotationDto shift) {

        Optional<User> user = userRepository.findById(shift.getCreatedBy());
        if (user.isPresent()) {
            // shift.setCreatedByName(user.get().getEmployeeName());
            // shift.setUpdatedByName(user.get().getEmployeeName());
        }
        if (shift.getUpdatedBy() != null) {
            Optional<User> user1 = userRepository.findById(shift.getUpdatedBy());
            if (user1.isPresent()) {
                // shift.setUpdatedByName(user.get().getEmployeeName());
            }
        }
    }

    public void updateStaff(ShiftRotationDto shift) {
        if (shift.getShift1_id() != null) {
            Optional<Shifts> shiftDetails = shiftRepository.findById(shift.getShift1_id());
            if (shiftDetails.get().getShiftType() != null) {
                shift.setShiftName(shiftDetails.get().getShiftType());
            }
        }
        if (shift.getShift1_id() != null) {
            Long shiftId = shift.getShift1_id();
            List<ShiftRotationMappingList> shiftRotationMappingLists = shiftAssignRepository.getEmployeeList(shiftId, shift.getId());
            List<ShiftRotationMappingDto> result = shiftRotationMappingLists.stream()
                    .map(data -> new ShiftRotationMappingDto(data)).collect(Collectors.toList());
            shift.setShift1Staffs(result);
        }
        if (shift.getShift2_id() != null) {
            Long shiftId = shift.getShift2_id();
            List<ShiftRotationMappingList> shiftRotationMappingLists = shiftAssignRepository.getEmployeeList(shiftId, shift.getId());
            List<ShiftRotationMappingDto> result = shiftRotationMappingLists.stream()
                    .map(data -> new ShiftRotationMappingDto(data)).collect(Collectors.toList());
            shift.setShift2Staffs(result);
        }
        if (shift.getShift3_id() != null) {
            Long shiftId = shift.getShift3_id();
            List<ShiftRotationMappingList> shiftRotationMappingLists = shiftAssignRepository.getEmployeeList(shiftId, shift.getId());
            List<ShiftRotationMappingDto> result = shiftRotationMappingLists.stream()
                    .map(data -> new ShiftRotationMappingDto(data)).collect(Collectors.toList());
            shift.setShift3Staffs(result);
        }
    }


    public void updateStaffView(ShiftRotationDto shift) {
        if (shift.getShift1_id() != null) {
            Optional<Shifts> shiftDetails = shiftRepository.findById(shift.getShift1_id());
            if (shiftDetails.get().getShiftType() != null) {
                shift.setShiftName(shiftDetails.get().getShiftType());
            }
        }

        List<ShiftRotationMapping> shiftRotationMappingLists1 = rotationMappingRepository.findBy(shift.getId(), "Shift1Staffs");
        if (shiftRotationMappingLists1 != null && !shiftRotationMappingLists1.isEmpty()) {
            List<ShiftRotationMappingDto> result = shiftRotationMappingLists1.stream().map(data -> new ShiftRotationMappingDto(data)).collect(Collectors.toList());
            shift.setShift1Staffs(result);
        }

        List<ShiftRotationMapping> shiftRotationMappingLists2 = rotationMappingRepository.findBy(shift.getId(), "Shift2Staffs");
        if (shiftRotationMappingLists2 != null && !shiftRotationMappingLists2.isEmpty()) {
            List<ShiftRotationMappingDto> result = shiftRotationMappingLists2.stream().map(data -> new ShiftRotationMappingDto(data)).collect(Collectors.toList());
            shift.setShift2Staffs(result);
        }

        List<ShiftRotationMapping> shiftRotationMappingLists3 = rotationMappingRepository.findBy(shift.getId(), "Shift3Staffs");
        if (shiftRotationMappingLists3 != null && !shiftRotationMappingLists3.isEmpty()) {
            List<ShiftRotationMappingDto> result = shiftRotationMappingLists3.stream().map(data -> new ShiftRotationMappingDto(data)).collect(Collectors.toList());
            shift.setShift3Staffs(result);
        }


    }


    public void updateShiftRotationMapping(Long id, ShiftRotationDto shiftrotation, List<Shifts> shifts,
                                           User loggeduser) {

        if (id > 0 && shiftrotation != null) {

            Date fromDate = dateUtil.getStringTODateWk(shiftrotation.getFromDate());
            Date toDate = dateUtil.getStringTODateWk(shiftrotation.getToDate());

            Map<String, List<String>> weeklyDatebyassign = new HashMap<String, List<String>>();
            Map<Integer, Shifts> shiftmapping = new HashMap<Integer, Shifts>();

            for (Shifts shift : shifts) {
                shiftmapping.put(shift.getShiftnumber(), shift);
            }

            if (shiftrotation.getFromDate() != null && shiftrotation.getToDate() != null) {

                if (shiftrotation.getShift1Staffs() != null && !shiftrotation.getShift1Staffs().isEmpty()) {

                    shiftassign(shiftrotation.getShift1Staffs(), fromDate, toDate, weeklyDatebyassign, shiftrotation, shifts.size(), shiftmapping, id, 1, loggeduser);
                }

                if (shifts.size() > 1) {

                    if (shiftrotation.getShift2Staffs() != null && !shiftrotation.getShift2Staffs().isEmpty()) {

                        shiftassign(shiftrotation.getShift2Staffs(), fromDate, toDate, weeklyDatebyassign, shiftrotation, shifts.size(), shiftmapping, id, 2, loggeduser);
                    }
                    if (shifts.size() > 2)
                        if (shiftrotation.getShift3Staffs() != null && !shiftrotation.getShift3Staffs().isEmpty()) {

                            shiftassign(shiftrotation.getShift3Staffs(), fromDate, toDate, weeklyDatebyassign, shiftrotation, shifts.size(), shiftmapping, id, 3, loggeduser);
                        }
                }
            }

        }
    }


    private void shiftassign(List<ShiftRotationMappingDto> staffs, Date fromDate, Date toDate,
                             Map<String, List<String>> weeklyDatebyassign, ShiftRotationDto shiftrotation, int shiftsize,
                             Map<Integer, Shifts> shifts, Long id, Integer shiftType, User loggeduser) {

        System.out.println("########");
        System.out.println("shift type = " + shiftType);
        System.out.println("########");
        System.out.println(shifts);


        List<Long> employeesstaff = new ArrayList<Long>();

        List<Long> employeesdaily = new ArrayList<Long>();

        List<Long> shiftids = new ArrayList<Long>();


        for (Map.Entry<Integer, Shifts> entry : shifts.entrySet()) {
            shiftids.add(entry.getValue().getId());
        }


        for (ShiftRotationMappingDto staff : staffs) {

            if (staff.getEmp_type() == 0) {
                employeesstaff.add(staff.getEmpId());

            } else {
                employeesdaily.add(staff.getEmpId());

            }
        }

        Map<Long, User> usermap = new HashMap<Long, User>();
        Map<Long, Weekoff> Weekoffmap = new HashMap<Long, Weekoff>();

        if (employeesstaff.size() > 0) {
            List<User> userslist = userRepository.findAllById(employeesstaff);

            for (User usrdata : userslist) {
                usermap.put(usrdata.getId(), usrdata);
            }


            List<Weekoff> weekofflist = weekOffRepository.findByEmployees(employeesstaff, 0);

            for (Weekoff weekoffData : weekofflist) {
                Weekoffmap.put(weekoffData.getEmpId(), weekoffData);
            }
        }


        Map<Long, EmastDaily> dailyusermap = new HashMap<Long, EmastDaily>();

        Map<Long, Weekoff> Weekoffdailymap = new HashMap<Long, Weekoff>();


        if (employeesdaily.size() > 0) {
            List<EmastDaily> usersdailylist = emastDailyRepository.findAllById(employeesdaily);

            for (EmastDaily usrdata : usersdailylist) {
                dailyusermap.put(usrdata.getId(), usrdata);
            }

            List<Weekoff> weekoffdailylist = weekOffRepository.findByEmployees(employeesdaily, 1);

            for (Weekoff weekoffData : weekoffdailylist) {
                Weekoffdailymap.put(weekoffData.getEmpId(), weekoffData);
            }

        }

        for (ShiftRotationMappingDto staff : staffs) {

            // **************** Employee type to identify Staff or Daily Wages
            if (staff.getEmp_type() == 0) {
                List<ShiftAssign> assign = new ArrayList<ShiftAssign>();
                User employee = usermap.get(staff.getEmpId());


                if (employee != null) {

                    List<Long> shiftskip = new ArrayList<Long>();
                    List<ShiftAllotment> shiftAllotmentList = shiftAllotmentRepository.findAllShift(shiftids, employee.getId(), 0);
                    for (ShiftAllotment shiftAllotment : shiftAllotmentList) {
                        shiftskip.add(shiftAllotment.getShiftId());
                    }


                    // Add Category based check here

                    if (staff.getWeekOff() > 0) {
                        List<String> dateRanges = new ArrayList<String>();
                        Weekoff weekOffexisting = Weekoffmap.get(staff.getEmpId());
                        if (weekOffexisting != null) {

                            // if (toDate.after(weekOffexisting.getShiftassigndate())) {

                            // if (Objects.isNull(weeklyDatebyassign.get(dateUtil.getDateToStringDDMMYYYY(weekOffexisting.getShiftassigndate()) + " - " + staff.getWeekOff()))) {

                            if (Objects.isNull(weeklyDatebyassign.get(dateUtil.getDateToStringDDMMYYYY(fromDate) + " - " + staff.getWeekOff()))) {

                                //dateRanges = shiftSplit(weekOffexisting.getShiftassigndate(), toDate, staff.getWeekOff());

                                dateRanges = shiftSplit(fromDate, toDate, staff.getWeekOff());

                                weeklyDatebyassign.put(dateUtil.getDateToStringDDMMYYYY(fromDate) + " - " + staff.getWeekOff(), dateRanges);

                            } else {
                                dateRanges = weeklyDatebyassign.get(dateUtil.getDateToStringDDMMYYYY(fromDate) + " - " + staff.getWeekOff());
                            }
                            //}

                        } else {

                            //Calendar cal = Calendar.getInstance();
                            //cal.add(Calendar.DATE, 1);


                            if (Objects.isNull(weeklyDatebyassign.get(dateUtil.getStringTODateF1(fromDate) + " - " + staff.getWeekOff()))) {

                                dateRanges = shiftSplit(fromDate, toDate, staff.getWeekOff());
                                weeklyDatebyassign.put(dateUtil.getStringTODateF1(fromDate) + " - " + staff.getWeekOff(), dateRanges);

                            } else {

                                dateRanges = weeklyDatebyassign.get(dateUtil.getStringTODateF1(fromDate) + " - " + staff.getWeekOff());

                            }
                        }

                        if (dateRanges.size() > 0) {

                            int counter = shiftType - 1;


                            for (Long skipId : shiftskip) {
                                System.out.println("skipId ShiftId == " + skipId);
                            }


                            for (String dateRange : dateRanges) {


                                System.out.println(" dateRange " + dateRange);

                                counter = counter + 1;

                                Date fromShiftDate = null;
                                Date toShiftDate = null;
                                List<Date> dateList = new ArrayList<>();
                                String[] datesplit = dateRange.split("-");

                                if (datesplit.length > 0) {

                                    fromShiftDate = dateUtil.getStringTODateDDMMYYYY(datesplit[0].trim());
                                    toShiftDate = dateUtil.getStringTODateDDMMYYYY(datesplit[1].trim());
                                    dateList = shiftSplit(fromShiftDate, toShiftDate);

                                }


                                List<ShiftAssign> assigns = new ArrayList<>();

                                for (Date date : dateList) {


                                    String shiftValue = null;
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);

                                    if (calendar.get(Calendar.DAY_OF_WEEK) == staff.getWeekOff()) {
                                        shiftValue = "W";
                                    } else {
                                        shiftValue = "S";
                                    }

                                    ShiftAssign shiftallocate = null;
                                    Optional<ShiftAssign> optionalShiftAssign = shiftAssignRepository.findBy(staff.getEmpId(), date, 0);
                                    if (optionalShiftAssign.isPresent()) {
                                        System.out.println("Present data " + optionalShiftAssign.get() + " === " + date);
                                        shiftallocate = optionalShiftAssign.get();
                                        shiftallocate.setUpdatedBy(loggeduser.getId());
                                        shiftallocate.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

                                    } else {
                                        System.out.println("Not Present data  === " + date);

                                        shiftallocate = new ShiftAssign();
                                        shiftallocate.setCreatedBy(loggeduser.getId());
                                        shiftallocate.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                                    }


                                    shiftallocate.setBranch_id(employee.getBranch_id());
                                    shiftallocate.setDepartment_id(employee.getDepartment().getId());
                                    if (employee.getDesignation() != null) {
                                        shiftallocate.setDesignation_id(employee.getDesignation().getId());
                                    }
                                    shiftallocate.setEmp_id(employee.getId());
                                    Organisation organisation = new Organisation();
                                    organisation.setId(employee.getOrg_id());
                                    shiftallocate.setOrganisation(organisation);
                                    shiftallocate.setWeek_day_id(staff.getWeekOff());
                                    shiftallocate.setEmp_type(0);

                                    shiftallocate.setRotation_id(id);

                                    System.out.println("Shift Date : " + date);

                                    shiftallocate.setWeek_day_id(staff.getWeekOff());
                                    shiftallocate.setShift_date(date);
                                    shiftallocate.setShift_value(shiftValue);

                                    assigns.add(shiftallocate);

                                }


                                if (shiftskip.size() > 0) {
                                    int count = 0;

                                    long shiftId;
                                    long skipshiftId;

                                    while (count < shiftskip.size()) {

                                        shiftId = shifts.get(counter).getId();
                                        skipshiftId = shiftskip.get(count);

                                        System.out.println("staff shiftId = " + shiftId);
                                        System.out.println("skip shiftId = " + skipshiftId);

                                        if (shiftId == skipshiftId) {
                                            System.out.println("equalsIgnoreCase = " + shiftId + " == " + skipshiftId);
                                            counter = counter + 1;
                                            if (counter > shifts.size()) {
                                                counter = 0;
                                            }
                                        }
                                        count++;
                                    }
                                }

                                if (counter == 0) {
                                    counter = 1;
                                }


                                for (ShiftAssign shiftAssign : assigns) {

                                    System.out.println("########");
                                    System.out.println("staff = " + employee.getId());
                                    System.out.println("shiftallocate = " + shifts.get(counter).getId() + "  NAME = " + shifts.get(counter).getShiftName());
                                    System.out.println("########");
                                    shiftAssign.setShifts(shifts.get(counter));
                                    System.out.println("########");
                                    System.out.println("AssignShift == " + shifts.get(counter).getShiftName());
                                    System.out.println("########");

                                    shiftAssign.setShifts(shifts.get(counter));
                                    assign.add(shiftAssign);
                                }


                                if (shiftsize < 2) {
                                    counter = 0;
                                }
                                if (shiftsize < 3 && counter == 2) {
                                    counter = 0;
                                }
                                if (shiftsize > 2 && counter == 3) {
                                    counter = 0;
                                }

                            }
                        }

                        if (assign != null && assign.size() > 0) {
                            try {
                                shiftAssignRepository.saveAll(assign);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                    }

                    Weekoff weekOff = new Weekoff();
                    weekOff.setEmpId(employee.getId());
                    weekOff.setShiftassigndate(toDate);
                    WeekDay weekday = new WeekDay();
                    weekday.setId(staff.getWeekOff());
                    weekOff.setWeekOffDay(weekday);
                    weekOff.setCreated_by(loggeduser.getId());
                    weekOff.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    weekOff.setEmp_type(0);
                    weekOffRepository.save(weekOff);
                }
            } else {
                List<ShiftAssign> assign = new ArrayList<ShiftAssign>();
                EmastDaily employee = dailyusermap.get(staff.getEmpId());

                List<Long> shiftskip = new ArrayList<Long>();
                List<ShiftAllotment> shiftAllotmentList = shiftAllotmentRepository.findAllShift(shiftids, employee.getId(), 1);
                for (ShiftAllotment shiftAllotment : shiftAllotmentList) {
                    shiftskip.add(shiftAllotment.getShiftId());
                }

                if (employee != null) {

                    // Add Category based check here

                    if (staff.getWeekOff() > 0) {
                        List<String> dateRanges = new ArrayList<String>();
                        Weekoff weekOffexisting = Weekoffdailymap.get(staff.getEmpId());
                        if (weekOffexisting != null && weekOffexisting.getShiftassigndate() != null) {
                            // if (toDate.after(weekOffexisting.getShiftassigndate())) {
                            if (Objects.isNull(weeklyDatebyassign.get(dateUtil.getDateToStringDDMMYYYY(fromDate) + " - " + staff.getWeekOff()))) {

                                dateRanges = shiftSplit(fromDate, toDate, staff.getWeekOff());
                                weeklyDatebyassign.put(dateUtil.getDateToStringDDMMYYYY(fromDate) + " - " + staff.getWeekOff(), dateRanges);

                            } else {
                                dateRanges = weeklyDatebyassign.get(dateUtil.getDateToStringDDMMYYYY(fromDate) + " - " + staff.getWeekOff());
                            }
                            // }

                        } else {
                            //Calendar cal = Calendar.getInstance();
                            //cal.add(Calendar.DATE, 1);
                            if (Objects.isNull(weeklyDatebyassign.get(dateUtil.getStringTODateF1(fromDate) + " - " + staff.getWeekOff()))) {

                                dateRanges = shiftSplit(fromDate, toDate, staff.getWeekOff());
                                weeklyDatebyassign.put(dateUtil.getStringTODateF1(fromDate) + " - " + staff.getWeekOff(), dateRanges);

                            } else {
                                dateRanges = weeklyDatebyassign.get(dateUtil.getStringTODateF1(fromDate) + " - " + staff.getWeekOff());
                            }
                        }
                        

                        if (dateRanges.size() > 0) {


                            int counter = shiftType - 1;


                            for (String dateRange : dateRanges) {

                                counter = counter + 1;


                                Date fromShiftDate = null;
                                Date toShiftDate = null;
                                List<Date> dateList = new ArrayList<>();
                                String[] datesplit = dateRange.split("-");

                                if (datesplit.length > 0) {

                                    fromShiftDate = dateUtil.getStringTODateDDMMYYYY(datesplit[0].trim());
                                    toShiftDate = dateUtil.getStringTODateDDMMYYYY(datesplit[1].trim());
                                    dateList = shiftSplit(fromShiftDate, toShiftDate);

                                }

                                List<ShiftAssign> assigns = new ArrayList<>();

                                for (Date date : dateList) {

                                    String shiftValue = null;
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(date);

                                    if (calendar.get(Calendar.DAY_OF_WEEK) == staff.getWeekOff()) {
                                        shiftValue = "W";
                                    } else {
                                        shiftValue = "S";
                                    }

                                    System.out.println("########");
                                    System.out.println("staff = " + staff.getEmpId());
                                    System.out.println("########");
                                    ShiftAssign shiftallocate = null;
                                    Optional<ShiftAssign> optionalShiftAssign = shiftAssignRepository.findBy(staff.getEmpId(), date, 0);
                                    if (optionalShiftAssign.isPresent()) {
                                        shiftallocate = optionalShiftAssign.get();
                                        shiftallocate.setUpdatedBy(loggeduser.getId());
                                        shiftallocate.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                                    } else {
                                        shiftallocate = new ShiftAssign();
                                        shiftallocate.setCreatedBy(loggeduser.getId());
                                        shiftallocate.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                                    }


                                    shiftallocate.setBranch_id(employee.getBranch());
                                    if (employee.getDepartment() != null) {
                                        shiftallocate.setDepartment_id(employee.getDepartment().getId());
                                    } else {
                                        shiftallocate.setDepartment_id(shiftrotation.getDeptID());
                                    }
                                    shiftallocate.setEmp_id(employee.getId());
                                    Organisation organisation = new Organisation();
                                    organisation.setId(employee.getCompany());
                                    shiftallocate.setOrganisation(organisation);
                                    shiftallocate.setWeek_day_id(staff.getWeekOff());

                                    shiftallocate.setEmp_type(1);

                                    shiftallocate.setRotation_id(id);

                                    shiftallocate.setWeek_day_id(staff.getWeekOff());
                                    shiftallocate.setShift_date(date);
                                    shiftallocate.setShift_value(shiftValue);

                                    assigns.add(shiftallocate);
                                }


                                if (shiftskip.size() > 0) {
                                    int count = 0;

                                    long shiftId;
                                    long skipshiftId;

                                    while (count < shiftskip.size()) {

                                        shiftId = shifts.get(counter).getId();
                                        skipshiftId = shiftskip.get(count);

                                        System.out.println("staff shiftId = " + shiftId);
                                        System.out.println("skip shiftId = " + skipshiftId);

                                        if (shiftId == skipshiftId) {
                                            System.out.println("equalsIgnoreCase = " + shiftId + " == " + skipshiftId);
                                            counter = counter + 1;
                                            if (counter > shifts.size()) {
                                                counter = 0;
                                            }
                                        }
                                        count++;
                                    }
                                }

                                if (counter == 0) {
                                    counter = 1;
                                }


                                for (ShiftAssign shiftAssign : assigns) {

                                    shiftAssign.setShifts(shifts.get(counter));

                                    System.out.println("########");
                                    System.out.println("staff = " + employee.getId());
                                    System.out.println("shiftallocate = " + shifts.get(counter).getId() + "  NAME = " + shifts.get(counter).getShiftName());
                                    System.out.println("########");
                                    shiftAssign.setShifts(shifts.get(counter));
                                    System.out.println("########");
                                    System.out.println("AssignShift == " + shifts.get(counter).getShiftName());
                                    System.out.println("########");

                                    assign.add(shiftAssign);
                                }


                                if (shiftsize < 2) {
                                    counter = 0;
                                }
                                if (shiftsize < 3 && counter == 2) {
                                    counter = 0;
                                }
                                if (shiftsize > 2 && counter == 3) {
                                    counter = 0;
                                }


                            }
                        }

                        if (assign != null && assign.size() > 0) {
                            System.out.println("########");
                            System.out.println(" assign " + assign);
                            System.out.println("########");
                            try {
                                shiftAssignRepository.saveAll(assign);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }

                    }

                    Weekoff weekOff = new Weekoff();
                    weekOff.setEmpId(employee.getId());
                    weekOff.setShiftassigndate(toDate);
                    WeekDay weekday = new WeekDay();
                    weekday.setId(staff.getWeekOff());
                    weekOff.setWeekOffDay(weekday);
                    weekOff.setCreated_by(loggeduser.getId());
                    weekOff.setEmp_type(1);
                    weekOff.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    weekOffRepository.save(weekOff);
                }
            }
        }
    }

    private List<String> shiftSplit(Date startDate, Date toDate, Long weekOff) {

        Date current = startDate;
        List<String> dateRanges = new ArrayList<String>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current);
        Date weekStart = calendar.getTime();
        Date weekEnd = current;
        while ( current.before(toDate)) {
            
        	calendar = Calendar.getInstance();
            calendar.setTime(current);
              
            calendar.add(Calendar.DATE, 1);
            
            System.out.println(calendar.get(Calendar.DAY_OF_WEEK));
            System.out.println(calendar.getTime());
                     
            if (calendar.get(Calendar.DAY_OF_WEEK) == weekOff) {
                weekEnd = calendar.getTime();
                String range = dateUtil.getStringTODateF1(weekStart) + " - " + dateUtil.getStringTODateF1(weekEnd);
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
            String range = dateUtil.getStringTODateF1(weekStart) + " - " + dateUtil.getStringTODateF1(weekEnd);
            if (!dateRanges.get(sizeofarray).equals(range)) {
                dateRanges.add(range);
            }

        } else {
        	String range = dateUtil.getStringTODateF1(weekStart) + " - " + dateUtil.getStringTODateF1(weekEnd);
        	dateRanges.add(range);
        }
        
        System.out.println(dateRanges.stream().toString());

        return dateRanges;
    }
    
    
    
    


    private List<Date> shiftSplit(Date startDate, Date toDate) {

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

    
    
    
    
    

    private HashMap<String, LocalDateTime> dateDynamic(String dateFrom, String dateTo) {
        LocalDateTime dateFromLocalDate = null, dateToLocalDate = null;
        if ((dateFrom != null && !dateFrom.isEmpty()) && (dateTo != null && !dateTo.isEmpty())) {
            dateFromLocalDate = dateUtil.getISTStartLocalDateTime(dateUtil.getStringTODateWk(dateFrom));
            dateToLocalDate = dateUtil.getISTEndLocalDateTime(dateUtil.getStringTODateWk(dateTo));
        } else if ((dateFrom == null || dateFrom.isEmpty()) && ((dateTo != null && !dateTo.isEmpty()))) {
            dateToLocalDate = dateUtil.getISTEndLocalDateTime(dateUtil.getStringTODateWk(dateTo));
        } else if ((dateFrom != null && !dateFrom.isEmpty()) && (dateTo == null || dateTo.isEmpty())) {
            dateFromLocalDate = dateUtil.getISTStartLocalDateTime(dateUtil.getStringTODateWk(dateFrom));
        }
        HashMap<String, LocalDateTime> dateHashMap = new HashMap<>();
        dateHashMap.put("dateFrom", dateFromLocalDate);
        dateHashMap.put("dateTo", dateToLocalDate);
        return dateHashMap;
    }


    private HashMap<String, Date> dateDynamic1(String dateFrom, String dateTo) {
        Date dateFromLocalDate = null, dateToLocalDate = null;
        if ((dateFrom != null && !dateFrom.isEmpty()) && (dateTo != null && !dateTo.isEmpty())) {
            dateFromLocalDate = dateUtil
                    .getLocalDateTimeToDate(dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateF1(dateFrom)));
            dateToLocalDate = dateUtil
                    .getLocalDateTimeToDate(dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateF1(dateTo)));
        } else if ((dateFrom == null || dateFrom.isEmpty()) && ((dateTo != null && !dateTo.isEmpty()))) {
            dateToLocalDate = dateUtil
                    .getLocalDateTimeToDate(dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateF1(dateTo)));
        } else if ((dateFrom != null && !dateFrom.isEmpty()) && (dateTo == null || dateTo.isEmpty())) {
            dateFromLocalDate = dateUtil
                    .getLocalDateTimeToDate(dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateF1(dateFrom)));
        }
        HashMap<String, Date> dateHashMap = new HashMap<>();
        dateHashMap.put("dateFrom", dateFromLocalDate);
        dateHashMap.put("dateTo", dateToLocalDate);
        return dateHashMap;
    }

    public ResponseEntity<?> filterShiftRotationUserList(Long branchId, Long departmentId, String employeeCategory, int empType,
                                                         String shiftType, Long shiftId, String shiftDate, String gender) throws UsernameNotFoundException {

        Date date = null;
        if (shiftDate != null) {
            date = dateUtil.getStringTODateWk(shiftDate);
        }
        List<UserList> userList = null;

        if (empType == 0) {
            userList = shiftRotationRepository.filterShiftRotationUserList(employeeCategory, branchId, departmentId, shiftType, shiftId, date, gender);
        } else {
            userList = shiftRotationRepository.filterShiftRotationEmastUserList(employeeCategory, branchId, departmentId, shiftType, shiftId, date, gender);
        }
        List<UserListDto> result = userList.stream().map(user -> {
            UserListDto userListDto = new UserListDto(user);
            Optional<Weekoff> optionalWeekOff = null;
            Optional<AssignDuty> assignDuty = null;
            if (empType == 0) {
                optionalWeekOff = weekOffRepository.findById(user.getId(), 0);
            } else {
                optionalWeekOff = weekOffRepository.findById(user.getId(), 1);
            }
            if (optionalWeekOff.isPresent()) {
                userListDto.setCurrentWeekOff(optionalWeekOff.get().getWeekOffDay().getWeekday());
            }
            if (empType == 0) {
                assignDuty = assignDutyRepository.findByAssignDuty(user.getId(), 0);
            } else {
                assignDuty = assignDutyRepository.findByAssignDuty(user.getId(), 1);
            }
            if (assignDuty.isPresent()) {
                userListDto.setCurrentDuty(assignDuty.get().getAssignDuty());
            }
            return userListDto;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<UserListDto>>(result, HttpStatus.OK);
    }

    public Map<String, String> getLeaveDate(Long userId) {
        List<String> dates = new ArrayList<>();
        List<LeaveDetails> list = leaveDetailsRepository.findBy(userId);
        Map<String, String> dateMap = new HashMap<>();
        for (LeaveDetails leaveDetails : list) {
            List<String> datesList = iterateBetweenDates(leaveDetails.getLeaveFrom(), leaveDetails.getLeaveTo());
            for (String dateString : datesList) {
                dateMap.put(dateString, dateString);
            }
        }
        return dateMap;
    }


    public ResponseEntity<?> updateParticularShifts(ShiftPeriodDTO shiftPeriodDTO, User loggedUser) {


        Optional<ShiftAssign> shiftAssign = shiftAssignRepository.findBy(shiftPeriodDTO.getEmpId(), dateUtil.getStringTODateWk(shiftPeriodDTO.getShiftDate()), shiftPeriodDTO.getEmpType());

        if (shiftAssign.isPresent()) {
            ShiftAssign shiftRotation = shiftAssign.get();
            shiftRotation.setUpdated_date(dateUtil.getLocalDateTime(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
            shiftRotation.setUpdatedBy(loggedUser.getId());
            Shifts shifts = new Shifts();
            shifts.setId(shiftPeriodDTO.getShiftId());
            shiftRotation.setShifts(shifts);
            shiftAssignRepository.save(shiftRotation);
            return new ResponseEntity<Status>(new Status("200", "updated successfully!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Not Updated!!"), HttpStatus.OK);
        }

    }


    public ResponseEntity<?> shiftRestrictStaffList(String fromDate, Long shiftId, int empType) {
        List<UserList> shiftAssign = null;

        if (empType == 0) {
            shiftAssign = shiftAssignRepository.shiftRestrictStaffList(dateUtil.getStringTODateWk(fromDate), shiftId);
        } else {
            shiftAssign = shiftAssignRepository.shiftRestrictEmastStaffList(dateUtil.getStringTODateWk(fromDate), shiftId);
        }

        if (!shiftAssign.isEmpty()) {
            List<UserListDto> result = shiftAssign.stream().map(user -> {
                UserListDto userListDto = new UserListDto(user);
                return userListDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<UserListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<UserListDto>>(new ArrayList<>(), HttpStatus.OK);
        }

    }
}
