package com.aria.service;


import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;

import com.aria.utils.DateUtil;
import com.aria.utils.Hashing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShitAllotmentService {

    @Autowired
    ShiftAllotmentRepository shiftAllotmentRepository;

    @Autowired
    ShiftRotationRepository shiftRotationRepository;

    @Autowired
    ShiftsRepository shiftsRepository;

    @Autowired
    EmastDailyRepository emastRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ShiftAllotmentMappingRepository allotmentMappingRepository;

    @Autowired
    DateUtil dateUtil;


    public ResponseEntity<?> saveShift(ShiftAllotmentDto shiftAllotmentDto, User loggedUser) throws NoSuchAlgorithmException {

        List<ShiftAllotment> shiftAllotment = new ArrayList<ShiftAllotment>();
        List<Shifts> shifts = shiftsRepository.findByShifts(shiftAllotmentDto.getShiftName());
        Long shift1id = 0L;
        Long shift2id = 0L;
        Long shift3id = 0L;

        for (Shifts sh : shifts) {
            if(sh.getShiftnumber() != null) {
                if (sh.getShiftnumber() == 1) {
                    shift1id = sh.getId();
                }
                if (sh.getShiftnumber() == 2) {
                    shift2id = sh.getId();
                }
                if (sh.getShiftnumber() == 3) {
                    shift3id = sh.getId();
                }
            }
        }

        for (int i = 0; i < shifts.size(); i++) {
            if (i == 0 && Objects.nonNull(shiftAllotmentDto.getShift1Staffs()) && shiftAllotmentDto.getShift1Staffs().size() > 0) {
                for (UserDto user : shiftAllotmentDto.getShift1Staffs()) {
                    ShiftAllotment shiftexemptAllotment = new ShiftAllotment();
                    shiftexemptAllotment.setBranchId(shiftAllotmentDto.getBranchId());
                    shiftexemptAllotment.setDeptId(shiftAllotmentDto.getDeptID());
                    shiftexemptAllotment.setOrgId(shiftAllotmentDto.getOrgId());
                    shiftexemptAllotment.setShiftId(shift1id);
                    shiftexemptAllotment.setEmpId(user.getId());
                    shiftexemptAllotment.setStaffemployee(shiftAllotmentDto.getStaffemployee());
                    shiftexemptAllotment.setId(Hashing.hash("Employee:" + user.getId(), "shift" + shift1id, shiftAllotmentDto.getStaffemployee()));
                    shiftexemptAllotment.setCreatedBy(loggedUser.getId());
                    shiftexemptAllotment.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    shiftexemptAllotment.setUpdatedBy(loggedUser.getId());
                    shiftexemptAllotment.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    shiftexemptAllotment.setEmp_type(shiftAllotmentDto.getEmp_type());
                    shiftAllotment.add(shiftexemptAllotment);

                }
            }

            if (i == 1 && Objects.nonNull(shiftAllotmentDto.getShift2Staffs()) && shiftAllotmentDto.getShift2Staffs().size() > 0) {
                for (UserDto user : shiftAllotmentDto.getShift2Staffs()) {
                    ShiftAllotment shiftexemptAllotment = new ShiftAllotment();
                    shiftexemptAllotment.setBranchId(shiftAllotmentDto.getBranchId());
                    shiftexemptAllotment.setDeptId(shiftAllotmentDto.getDeptID());
                    shiftexemptAllotment.setOrgId(shiftAllotmentDto.getOrgId());
                    shiftexemptAllotment.setShiftId(shift2id);
                    shiftexemptAllotment.setEmpId(user.getId());
                    shiftexemptAllotment.setStaffemployee(shiftAllotmentDto.getStaffemployee());
                    shiftexemptAllotment.setId(Hashing.hash("Employee:" + user.getId(), "shift" + shift2id, shiftAllotmentDto.getStaffemployee()));

                    shiftexemptAllotment.setCreatedBy(loggedUser.getId());
                    shiftexemptAllotment.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    shiftexemptAllotment.setUpdatedBy(loggedUser.getId());
                    shiftexemptAllotment.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    shiftexemptAllotment.setEmp_type(shiftAllotmentDto.getEmp_type());
                    shiftAllotment.add(shiftexemptAllotment);

                }
            }

            if (i == 2 && Objects.nonNull(shiftAllotmentDto.getShift3Staffs()) && shiftAllotmentDto.getShift3Staffs().size() > 0) {
                for (UserDto user : shiftAllotmentDto.getShift3Staffs()) {
                    ShiftAllotment shiftexemptAllotment = new ShiftAllotment();
                    shiftexemptAllotment.setBranchId(shiftAllotmentDto.getBranchId());
                    shiftexemptAllotment.setDeptId(shiftAllotmentDto.getDeptID());
                    shiftexemptAllotment.setOrgId(shiftAllotmentDto.getOrgId());
                    shiftexemptAllotment.setShiftId(shift3id);
                    shiftexemptAllotment.setEmpId(user.getId());
                    shiftexemptAllotment.setStaffemployee(shiftAllotmentDto.getStaffemployee());
                    shiftexemptAllotment.setId(Hashing.hash("Employee:" + user.getId(), "shift" + shift3id, shiftAllotmentDto.getStaffemployee()));

                    shiftexemptAllotment.setCreatedBy(loggedUser.getId());
                    shiftexemptAllotment.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    shiftexemptAllotment.setUpdatedBy(loggedUser.getId());
                    shiftexemptAllotment.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    shiftexemptAllotment.setEmp_type(shiftAllotmentDto.getEmp_type());
                    shiftAllotment.add(shiftexemptAllotment);

                }
            }

        }

        List<ShiftAllotment> result = shiftAllotmentRepository.saveAll(shiftAllotment);
        return new ResponseEntity<Status>(new Status("200", "shift exemption executed successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAll(String type, String empName, Long deptId, String shiftName, Pageable pageable) throws UsernameNotFoundException {

  
        Page<ShiftAllotmentList> list = null;
        System.out.println(type);
        if (type.equalsIgnoreCase("empstaff") || type.contains("Staff")) {
            list = shiftAllotmentRepository.findAllEmp(empName, deptId, shiftName, 0, pageable);
        } else {
            list = shiftAllotmentRepository.findAllEmast(empName, deptId, shiftName, 1, pageable);
        }
        if (!list.toList().isEmpty()) {
            List<ShiftAllotmentListDto> result = list.toList().stream().map(data -> new ShiftAllotmentListDto(data)).collect(Collectors.toList());
            PageDataDto<ShiftAllotmentListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<ShiftAllotmentListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ShiftAllotmentListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getShiftDetails(String empId, String shiftId, Boolean staff) throws UsernameNotFoundException, NoSuchAlgorithmException {

        String id = Hashing.hash("Employee:" + empId, "shift" + shiftId, staff);

        Optional<ShiftAllotment> optionalShiftAllotment = shiftAllotmentRepository.findById(id);
        ShiftAllocateMappingDto shift = new ShiftAllocateMappingDto(optionalShiftAllotment.get());
        if (optionalShiftAllotment.get().getStaffemployee()) {
            User user = updateEmployeeDetails(shift);
            shift.setEmployeeName(user.getEmployeename());
            shift.setDepartmentName(user.getDepartment().getName());

        } else {
            EmastDaily emastDaily = updateWagesEmployeeDetails(shift);
            shift.setEmployeeName(emastDaily.getName());
//            shift.setDepartmentName(emastDaily.getDepartment().getName());
        }
        return new ResponseEntity<ShiftAllocateMappingDto>(shift, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteShift(String hashId, String shiftId, Boolean staff) throws UsernameNotFoundException {
        try {

            //String id = Hashing.hash("Employee:" + empId, "shift" + shiftId, staff);

            Optional<ShiftAllotment> optionalShiftAllotment = shiftAllotmentRepository.findById(hashId);
            if (optionalShiftAllotment.isPresent()) {
                ShiftAllotment shiftAllotment = optionalShiftAllotment.get();
                shiftAllotmentRepository.delete(shiftAllotment);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public User updateEmployeeDetails(ShiftAllocateMappingDto shift) {

        Optional<User> user = userRepository.findById(shift.getEmpId());
        if (user.isPresent()) {
            return user.get();
        }

        return null;
    }


    public EmastDaily updateWagesEmployeeDetails(ShiftAllocateMappingDto shift) {

        Optional<EmastDaily> emastDaily = emastRepository.findById(shift.getEmpId());
        if (emastDaily.isPresent()) {
            return emastDaily.get();
        }

        return null;
    }


}
