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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DutyAllotmentService {

    @Autowired
    DutyAllotmentRepository dutyAllotmentRepository;

    @Autowired
    AssignDutyRepository assignDutyRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmastDailyRepository emastDailyRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    WcShiftsRepository wcShiftsRepository;

    @Autowired
    LuZoneRepository luZoneRepository;
    
    @Autowired
    AttendanceRepository attendanceRepository;
    
    @Autowired
    private DailyWagesAttendanceRepository wagesAttendanceRepository;

    public ResponseEntity<?> saveDutyAllotment(DutyAllotmentDto dutyAllotmentDto, User loggedUser) {
    	Date shiftDate = dateUtil.getStringTODateWk(dutyAllotmentDto.getShiftDate());

    	for (AssignDutyDto assignDutyDto : dutyAllotmentDto.getAssignDuties()) {
    		if(dutyAllotmentDto.getCategory().equalsIgnoreCase("staff") || dutyAllotmentDto.getCategory().equalsIgnoreCase("staffApprentice")) {
    	    if (!attendanceRepository.existsAttendanceForShiftDate(
    	            assignDutyDto.getStaffId(),
    	            shiftDate)) {
    	    	return new ResponseEntity<Status>(new Status("400", "Attendance not marked for user ID: "
    	   	                        + assignDutyDto.getStaffId()), HttpStatus.BAD_REQUEST);
			      
    	     }
    		}
    		else {
    			 if (!wagesAttendanceRepository.existsAttendanceForShiftDate(
    	    	            assignDutyDto.getStaffId(),
    	    	            shiftDate)) {
    	    	    	return new ResponseEntity<Status>(new Status("400", "Attendance not marked for wages ID: "
    	    	   	                        + assignDutyDto.getStaffId()), HttpStatus.BAD_REQUEST);
    				      
    	    	     }
    		}
    	}
    	DutyAllotment dutyAllotment = new DutyAllotment(dutyAllotmentDto);
        dutyAllotment.setShiftDate(dateUtil.getStringTODateWk(dutyAllotmentDto.getShiftDate()));
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        dutyAllotment.setOrganisation(organisation);
        DepartmentDetails department = new DepartmentDetails();
        department.setId(dutyAllotmentDto.getDeptId());
        dutyAllotment.setDepartmentDetails(department);
        dutyAllotment.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        dutyAllotment.setCreatedBy(loggedUser.getId());
        DutyAllotment result = dutyAllotmentRepository.save(dutyAllotment);
        for (AssignDutyDto assignDutyDto : dutyAllotmentDto.getAssignDuties()) {
            AssignDuty assignDuty = new AssignDuty(assignDutyDto);
            assignDuty.setAllotmentId(result.getId());
            assignDuty.setAssignedDate(result.getShiftDate());
            Branch branch = new Branch();
            branch.setId(dutyAllotmentDto.getBranchId());
            assignDuty.setAssignBranch(branch);
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(dutyAllotmentDto.getDeptId());
            assignDuty.setAssignDepartment(departmentDetails);
            assignDuty.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            assignDuty.setCreatedBy(loggedUser.getId());
            assignDutyRepository.save(assignDuty);
        }
        //updateWcshift(dutyAllotmentDto, dutyAllotmentDto.getAssignDuties());
        return new ResponseEntity<Status>(new Status("200", "duty allotment save successfully!!!"), HttpStatus.OK);
    }


    public void updateWcshift(DutyAllotmentDto dutyAllotmentDto, List<AssignDutyDto> assignDutyDtos){
        if(!assignDutyDtos.isEmpty()) {
            for (AssignDutyDto assignDuty : assignDutyDtos) {
                Wcshift wcshift = new Wcshift();
                wcshift.setShiftDate(dateUtil.getStringTODateWk(dutyAllotmentDto.getShiftDate()));
                Optional<LuZone> luZoneOptional = luZoneRepository.findByName(assignDuty.getAssignDuty(), dutyAllotmentDto.getBranchId());
                if(luZoneOptional.isPresent()){
                    wcshift.setBranch(dutyAllotmentDto.getBranchId());
                    wcshift.setDept(dutyAllotmentDto.getDeptId());
                    wcshift.setWcId(luZoneOptional.get().getWcid());
                    wcshift.setWcTable(luZoneOptional.get().getWctable());
                }
               //wcShiftsRepository.save(wcshift);
            }
        }
    }

    public ResponseEntity<?> updateDutyAllotment(DutyAllotmentDto dutyAllotmentDto, User loggedUser) {
        DutyAllotment dutyAllotment = new DutyAllotment(dutyAllotmentDto);
        dutyAllotment.setShiftDate(dateUtil.getStringTODateWk(dutyAllotmentDto.getShiftDate()));
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        dutyAllotment.setOrganisation(organisation);
        DepartmentDetails department = new DepartmentDetails();
        department.setId(dutyAllotmentDto.getDeptId());
        dutyAllotment.setDepartmentDetails(department);
        dutyAllotment.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        dutyAllotment.setCreatedBy(loggedUser.getId());
        DutyAllotment result = dutyAllotmentRepository.save(dutyAllotment);
        for (AssignDutyDto assignDutyDto : dutyAllotmentDto.getAssignDuties()) {
            Optional<AssignDuty> optionalAssignDuty = assignDutyRepository.findByAllotmentIdANDUserId(result.getId(), assignDutyDto.getStaffId());
            if (optionalAssignDuty.isPresent()) {
                AssignDuty assignDuty = optionalAssignDuty.get();
                assignDuty.setAllotmentId(result.getId());
                assignDuty.setAssignedDate(result.getShiftDate());
                assignDuty.setStaff_id(assignDutyDto.getStaffId());
                Branch branch = new Branch();
                branch.setId(dutyAllotmentDto.getBranchId());
                assignDuty.setAssignBranch(branch);
                DepartmentDetails departmentDetails = new DepartmentDetails();
                departmentDetails.setId(dutyAllotmentDto.getDeptId());
                assignDuty.setAssignDepartment(departmentDetails);
                assignDuty.setUpdatedBy(loggedUser.getId());
                assignDuty.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                assignDuty.setCurrentDuty(assignDutyDto.getCurrentDuty());
                assignDuty.setAssignDuty(assignDutyDto.getAssignDuty());
                assignDuty.setAssign_duty_id(assignDutyDto.getAssign_duty_id());
                assignDutyRepository.save(assignDuty);
            } else {
                AssignDuty assignDuty = new AssignDuty(assignDutyDto);
                assignDuty.setAllotmentId(result.getId());
                assignDuty.setCreatedBy(loggedUser.getId());
                assignDuty.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                Branch branch = new Branch();
                branch.setId(dutyAllotmentDto.getBranchId());
                assignDuty.setAssignBranch(branch);
                DepartmentDetails departmentDetails = new DepartmentDetails();
                departmentDetails.setId(dutyAllotmentDto.getDeptId());
                assignDuty.setAssignDepartment(departmentDetails);
                if(assignDutyDto.getEmp_type() == 0) {
                    Optional<User> userOptional = userRepository.findById(assignDutyDto.getStaffId());
                    if (userOptional.isPresent()) {
                        assignDuty.setCurrentDuty(userOptional.get().getDesignation().getName());
                    }
                }
                assignDutyRepository.save(assignDuty);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "duty allotment save successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> getAssignDuty(Long allotmentId, User loggedUser) throws UsernameNotFoundException {
        Optional<DutyAllotment> optionalDutyAllotment = dutyAllotmentRepository.findById(allotmentId);
        if (optionalDutyAllotment.isPresent()) {
            DutyAllotmentDto allotmentDto = new DutyAllotmentDto(optionalDutyAllotment.get());
            if (allotmentDto.getDeptId() == null) {
                allotmentDto.setDeptId(loggedUser.getDepartment().getId());
                allotmentDto.setDeptName(loggedUser.getDepartment().getName());
            }
            if (allotmentDto.getBranchId() == null) {
                Optional<Branch> branch = branchRepository.findById(loggedUser.getBranch_id());
                allotmentDto.setBranchId(branch.get().getId());
                allotmentDto.setBranchName(branch.get().getBranchName());
            }
            List<AssignDuty> list = assignDutyRepository.findAllBy(allotmentDto.getId(), allotmentDto.getBranchId(), allotmentDto.getDeptId());
            if (!list.isEmpty()) {
                allotmentDto.setAssignDuties(list.stream().map(data -> {
                    AssignDutyDto assignDutyDto = new AssignDutyDto(data);
                    updateAssigDutyUSer(assignDutyDto);
                    return assignDutyDto;
                }).collect(Collectors.toList()));
            } else {
                allotmentDto.setAssignDuties(new ArrayList<>());
            }
            return new ResponseEntity<DutyAllotmentDto>(allotmentDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<DutyAllotmentDto>(HttpStatus.NOT_FOUND);
        }
    }


    public void updateAssigDutyUSer(AssignDutyDto assignDutyDto) {
        if (assignDutyDto.getStaffId() != null) {
            if(assignDutyDto.getEmp_type() == 0){
                Optional<User> userOptional = userRepository.findById(assignDutyDto.getStaffId());
                if(userOptional.isPresent()){
                    assignDutyDto.setStaffName(userOptional.get().getEmployeename());
                    assignDutyDto.setGender(userOptional.get().getGender());
                }
            }else {
                Optional<EmastDaily> userOptional = emastDailyRepository.findById(assignDutyDto.getStaffId());
                if(userOptional.isPresent()){
                    assignDutyDto.setStaffName(userOptional.get().getName());
                    assignDutyDto.setGender(userOptional.get().getGender());
                }
            }

        }
    }

    public ResponseEntity<?> getAssignDutyFilterList(String type, User loggedUser, Long branchId, Long deptId, String employeeName, String shiftName, String fromDate, String toDate, Pageable pageable) throws UsernameNotFoundException {

        Date startDate = null;
        Date endDate = null;

        if (fromDate != null && toDate != null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
            endDate = dateUtil.getStringTODateWk(toDate);
        } else if (fromDate != null && toDate == null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
        } else if (fromDate == null && toDate != null) {
            endDate = dateUtil.getStringTODateWk(toDate);
        }
        Page<IAssignDutyFilterListResponse> list = null;
        if(type != null && type.equalsIgnoreCase("staff")) {
             list = dutyAllotmentRepository.getDutyAllotmentFilterListByEmp(branchId, deptId, employeeName, shiftName, startDate, endDate, pageable);
        }else {
            list = dutyAllotmentRepository.getDutyAllotmentFilterListByEmast(branchId, deptId, employeeName, shiftName, startDate, endDate, pageable);
        }
        if (!list.isEmpty()) {
            List<AssignDutyFilterListResponseDTO> result = list.toList().stream().map(iAssignDutyFilterListResponse -> new AssignDutyFilterListResponseDTO(iAssignDutyFilterListResponse)).collect(Collectors.toList());
            PageDataDto<AssignDutyFilterListResponseDTO> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<AssignDutyFilterListResponseDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<AssignDutyFilterListResponseDTO>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> deleteAssignDuty(Long allotmentId, Long staffId) throws UsernameNotFoundException {
        Optional<AssignDuty> assignDuty = assignDutyRepository.findByAllotmentIdANDUserId(allotmentId, staffId);
        try {
            if (assignDuty.isPresent()) {
                AssignDuty assignDuty1 = assignDuty.get();
                assignDutyRepository.delete(assignDuty1);
                List<AssignDuty> assignDuties = assignDutyRepository.findAllByAllotmentId(allotmentId);
                if (assignDuties.isEmpty()) {
                    Optional<DutyAllotment> optionalDutyAllotment = dutyAllotmentRepository.findById(allotmentId);
                    if (optionalDutyAllotment.isPresent()) {
                        dutyAllotmentRepository.delete(optionalDutyAllotment.get());
                    }
                }
            } else {
                return new ResponseEntity<AssignDuty>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
}
