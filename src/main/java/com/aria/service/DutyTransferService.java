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
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DutyTransferService {

    @Autowired
    DutyTransferRepository dutyTransferRepository;

    @Autowired
    DutyTransferAssignDutyRepository dutyTransferAssignDutyRepository;

    @Autowired
    AssignDutyRepository assignDutyRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DutyAllotmentRepository dutyAllotmentRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    EmastDailyRepository emastDailyRepository;

    public ResponseEntity<?> saveDuty(DutyTransferDto dutyTransferDto, User loggedUser) {
        DutyTransfer dutyTransfer = new DutyTransfer(dutyTransferDto);
        dutyTransfer.setShiftDate(dateUtil.getStringTODateWk(dutyTransferDto.getShiftDate()));
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        dutyTransfer.setOrganisation(organisation);
        dutyTransfer.setDepartmentDetails(loggedUser.getDepartment());
        dutyTransfer.setBranchId(loggedUser.getBranch_id());
        dutyTransfer.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        dutyTransfer.setCreatedBy(loggedUser.getId());
        DutyTransfer result = dutyTransferRepository.save(dutyTransfer);
        for (DutyTransferAssignDutyDto assignDutyDto : dutyTransferDto.getAssignDutyDetails()) {
            DutyTransferAssignDuty dutyTransferAssignDuty = new DutyTransferAssignDuty(assignDutyDto);
            dutyTransferAssignDuty.setTransferId(result.getId());
            dutyTransferAssignDuty.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            dutyTransferAssignDuty.setCreatedBy(loggedUser.getId());
            dutyTransferAssignDutyRepository.save(dutyTransferAssignDuty);
        }
        saveDutyAllotment(result, dutyTransferDto, loggedUser);
        return new ResponseEntity<Status>(new Status("200", "duty transfer save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateDuty(DutyTransferDto dutyTransferDto, User loggedUser) {
        DutyTransfer dutyTransfer = new DutyTransfer(dutyTransferDto);
        dutyTransfer.setShiftDate(dateUtil.getStringTODateWk(dutyTransferDto.getShiftDate()));
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        dutyTransfer.setOrganisation(organisation);
        dutyTransfer.setDepartmentDetails(loggedUser.getDepartment());
        dutyTransfer.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        dutyTransfer.setUpdatedBy(loggedUser.getId());
        DutyTransfer result = dutyTransferRepository.save(dutyTransfer);
        for (DutyTransferAssignDutyDto assignDutyDto : dutyTransferDto.getAssignDutyDetails()) {
            Optional<DutyTransferAssignDuty> optional = dutyTransferAssignDutyRepository
                    .findByTransferIdANDUserId(result.getId(), assignDutyDto.getStaffId());
            if (optional.isPresent()) {
                DutyTransferAssignDuty dutyTransferAssignDuty = new DutyTransferAssignDuty(assignDutyDto);
                dutyTransferAssignDuty.setId(optional.get().getId());
                dutyTransferAssignDuty.setTransferId(result.getId());
                dutyTransferAssignDuty.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                dutyTransferAssignDuty.setUpdatedBy(loggedUser.getId());
                dutyTransferAssignDutyRepository.save(dutyTransferAssignDuty);

                /*
                 * Optional<AssignDuty> assignDutyOptional =
                 * assignDutyRepository.findByTransferIdANDUserId(result.getId(),
                 * assignDutyDto.getStaffId()); if (assignDutyOptional.isPresent()) { AssignDuty
                 * assignDuty = assignDutyOptional.get(); Optional<User> userOptional =
                 * userRepository.findById(dutyTransferAssignDuty.getStaff().getId()); if
                 * (userOptional.isPresent()) {
                 * assignDuty.setCurrentDuty(userOptional.get().getDesignation().getName()); }
                 * assignDuty.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                 * assignDuty.setUpdatedBy(loggedUser.getId());
                 * assignDutyRepository.save(assignDuty); } else { AssignDuty assignDuty = new
                 * AssignDuty(); assignDuty.setTransferId(result.getId()); User user = new
                 * User(); user.setId(dutyTransferAssignDuty.getStaff().getId());
                 * assignDuty.setStaff(user); assignDuty.setCreatedBy(loggedUser.getId());
                 * assignDuty.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))); Optional<User> userOptional
                 * = userRepository.findById(dutyTransferAssignDuty.getStaff().getId()); if
                 * (userOptional.isPresent()) {
                 * assignDuty.setCurrentDuty(userOptional.get().getDesignation().getName()); }
                 * assignDutyRepository.save(assignDuty); }
                 */
            } else {
                DutyTransferAssignDuty dutyTransferAssignDuty = new DutyTransferAssignDuty(assignDutyDto);
                dutyTransferAssignDuty.setTransferId(result.getId());
                dutyTransferAssignDuty.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                dutyTransferAssignDuty.setCreatedBy(loggedUser.getId());
                dutyTransferAssignDutyRepository.save(dutyTransferAssignDuty);

                /*
                 * AssignDuty assignDuty = new AssignDuty();
                 * assignDuty.setTransferId(result.getId()); User user = new User();
                 * user.setId(dutyTransferAssignDuty.getStaff().getId());
                 * assignDuty.setStaff(user); Optional<User> userOptional =
                 * userRepository.findById(dutyTransferAssignDuty.getStaff().getId()); if
                 * (userOptional.isPresent()) {
                 * assignDuty.setCurrentDuty(userOptional.get().getDesignation().getName()); }
                 * assignDuty.setCreatedBy(loggedUser.getId());
                 * assignDuty.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                 * assignDutyRepository.save(assignDuty);
                 */
            }
        }
        updateDutyAllotment(dutyTransfer, dutyTransferDto, loggedUser);
        return new ResponseEntity<Status>(new Status("200", "duty transfer  updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAll() throws UsernameNotFoundException {

        List<DutyTransfer> list = dutyTransferRepository.findAll();
        if (!list.isEmpty()) {
            List<DutyTransferDto> result = list.stream().map(data -> {
                DutyTransferDto transferDto = new DutyTransferDto(data);
                transferDto.setAssignedDate(dateUtil.getStringTODate(data.getShiftDate()));
                return transferDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<DutyTransferDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DutyTransferDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getDutyTransferAssignDutyFilterList(Long branchId, Long deptId, String type, String employeeName,
                                                                 String assignedBranchName, String fromDepartmentName, String toDepartmentName, String fromDate,
                                                                 String toDate, Pageable pageable) throws UsernameNotFoundException {

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
        Page<DutyTransferAssignDutyList> list = null;
        if (type.equalsIgnoreCase("staff")) {
            list = dutyTransferAssignDutyRepository.getDutyTransferAssignDutyFilterListEmp(branchId, deptId, employeeName,
                    assignedBranchName, fromDepartmentName, toDepartmentName, startDate, endDate, pageable);
        } else {
            list = dutyTransferAssignDutyRepository.getDutyTransferAssignDutyFilterListEmast(branchId, deptId, employeeName,
                    assignedBranchName, fromDepartmentName, toDepartmentName, startDate, endDate, pageable);
        }
        if (!list.isEmpty()) {
            List<DutyTransferAssignDutyDto> result = list.toList().stream()
                    .map(data -> new DutyTransferAssignDutyDto(data)).collect(Collectors.toList());
            PageDataDto<DutyTransferAssignDutyDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<DutyTransferAssignDutyDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<DutyTransferAssignDutyDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getDutyDetails(String id) throws UsernameNotFoundException {
        Optional<DutyTransfer> optionalDutyAllotment = dutyTransferRepository.findById(Long.parseLong(id));
        DutyTransferDto dutyTransferDto = new DutyTransferDto(optionalDutyAllotment.get());
        updateDutyTransferDto(dutyTransferDto);
        dutyTransferDto.setAssignedDate(dateUtil.getDateToStringDDMMYYYY(optionalDutyAllotment.get().getShiftDate()));
        return new ResponseEntity<DutyTransferDto>(dutyTransferDto, HttpStatus.OK);
    }

    public void updateDutyTransferDto(DutyTransferDto dutyTransferDto) {
        if (!dutyTransferDto.getAssignDutyDetails().isEmpty()) {
            List<DutyTransferAssignDutyDto> list = dutyTransferDto.getAssignDutyDetails();
            for (DutyTransferAssignDutyDto dutyTransferAssignDutyDto : list) {
                if (dutyTransferAssignDutyDto.getEmp_type() == 0) {
                    Optional<User> userOptional = userRepository.findById(dutyTransferAssignDutyDto.getStaffId());
                    if (userOptional.isPresent()) {
                        dutyTransferAssignDutyDto.setEmployeeName(userOptional.get().getEmployeename());
                    }
                } else {
                    Optional<EmastDaily> userOptional = emastDailyRepository
                            .findById(dutyTransferAssignDutyDto.getStaffId());
                    if (userOptional.isPresent()) {
                        dutyTransferAssignDutyDto.setEmployeeName(userOptional.get().getName());
                    }
                }
            }
            dutyTransferDto.setAssignDutyDetails(list);
        }
    }

    public ResponseEntity<?> getDutyTransferFilterDetails(Long dutyTransferId, Long toBranchId, Long toDepartmentId)
            throws UsernameNotFoundException {
        List<DutyTransfer> list = new ArrayList<>();
        if (!list.isEmpty()) {
            List<DutyTransferDto> result = list.stream().map(data -> {
                DutyTransferDto dutyTransferDto = new DutyTransferDto(data);
                dutyTransferDto.setAssignedDate(
                        dateUtil.getDateToStringDDMMYYYY(dateUtil.getLocalDateTimeToDate(data.getCreated_date())));
                dutyTransferDto.setShiftDate(dateUtil.getDateToStringDDMMYYYY(data.getShiftDate()));
                return dutyTransferDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<DutyTransferDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DutyTransferDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> deleteDuty(String id, String staffId) throws UsernameNotFoundException {
        try {
            Optional<DutyTransfer> optionalDutyAllotment = dutyTransferRepository.findById(Long.parseLong(id));
            if (optionalDutyAllotment.isPresent()) {
                DutyTransfer dutyTransfer = optionalDutyAllotment.get();
                Optional<DutyTransferAssignDuty> duties = dutyTransferAssignDutyRepository
                        .findByTransferIdANDUserId(dutyTransfer.getId(), Long.valueOf(staffId));
                if (duties.isPresent()) {
                    dutyTransferAssignDutyRepository.delete(duties.get());
                }
                List<DutyTransferAssignDuty> dutyTransferAssignDutyList = dutyTransferAssignDutyRepository
                        .findAllByTransferId(dutyTransfer.getId());
                if (dutyTransferAssignDutyList.isEmpty()) {
                    dutyTransferRepository.delete(dutyTransfer);
                }
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> getDutyAllotmentTransferFilterDetails(Long allotmentId, Long toBranchId,
                                                                   Long toDepartmentId) throws UsernameNotFoundException {
        List<AssignDuty> list = assignDutyRepository.findAllBy(allotmentId, toBranchId, toDepartmentId);
        if (!list.isEmpty()) {
            List<AssignDutyDto> result = list.stream().map(data -> {
                AssignDutyDto assignDutyDto = new AssignDutyDto(data);
                return assignDutyDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<AssignDutyDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<AssignDutyDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public void updateEmployeeDetails(DutyTransferDto duty) {

        /*
         * Optional<User> user = userRepository.findById(duty.getCreatedBy()); if
         * (user.isPresent()) { //duty.setCreatedByName(user.get().getEmployeeName());
         * //duty.setUpdatedByName(user.get().getEmployeeName()); } if
         * (duty.getUpdatedBy() != null) { Optional<User> user1 =
         * userRepository.findById(duty.getUpdatedBy()); if (user1.isPresent()) {
         * //duty.setUpdatedByName(user.get().getEmployeeName()); } }
         */
    }

    public void updateEmployeeDetails(DutyTransferAssignDutyDto dutyTransferAssignDutyDto) {

        /*
         * Optional<User> user =
         * userRepository.findById(dutyTransferAssignDutyDto.getCreatedBy()); if
         * (user.isPresent()) { // duty.setCreatedByName(user.get().getEmployeeName());
         * // duty.setUpdatedByName(user.get().getEmployeeName()); } if
         * (dutyTransferAssignDutyDto.getUpdatedBy() != null) { Optional<User> user1 =
         * userRepository.findById(dutyTransferAssignDutyDto.getUpdatedBy()); if
         * (user1.isPresent()) { // duty.setUpdatedByName(user.get().getEmployeeName());
         * } }
         */
    }

    private HashMap<String, Date> dateDynamic(String dateFrom, String dateTo) {
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

    public void saveDutyAllotment(DutyTransfer dutyTransfer, DutyTransferDto dutyTransferDto, User loggedUser) {
        DutyAllotment dutyAllotment = new DutyAllotment(dutyTransfer);
        dutyAllotment.setShiftDate(dateUtil.getStringTODateWk(dutyTransferDto.getShiftDate()));
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        dutyAllotment.setOrganisation(organisation);
        dutyAllotment.setTransferId(dutyTransfer.getId());
        // dutyAllotment.setDepartmentDetails(loggedUser.getDepartment());
        dutyAllotment.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        dutyAllotment.setCreatedBy(loggedUser.getId());
        DutyAllotment result = dutyAllotmentRepository.save(dutyAllotment);
        for (DutyTransferAssignDutyDto assignDutyDto : dutyTransferDto.getAssignDutyDetails()) {
            AssignDuty assignDuty = new AssignDuty();
            assignDuty.setAllotmentId(result.getId());
            DepartmentDetails department = new DepartmentDetails();
            department.setId(assignDutyDto.getAssignDeptId());
            assignDuty.setAssignDepartment(department);
            Branch branch = new Branch();
            branch.setId(assignDutyDto.getAssignBranchId());
            assignDuty.setAssignBranch(branch);
            assignDuty.setStaff_id(assignDutyDto.getStaffId());
            assignDuty.setCreatedBy(loggedUser.getId());
            assignDuty.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            assignDuty.setEmp_type(assignDutyDto.getEmp_type());
            if(assignDutyDto.getEmp_type() == 1) {

                Optional<EmastDaily> userOptional = emastDailyRepository.findById(assignDutyDto.getStaffId());
                if (userOptional.isPresent()) {
                    if (userOptional.get().getDesignation() != null)
                        assignDuty.setCurrentDuty(userOptional.get().getDesignation());
                }
            } else {
                Optional<User> userOptional = userRepository.findById(assignDutyDto.getStaffId());
                if (userOptional.isPresent()) {
                    if (userOptional.get().getDesignation() != null)
                        assignDuty.setCurrentDuty(userOptional.get().getDesignation().getName());
                }

            }
            assignDutyRepository.save(assignDuty);
        }
    }

    public void updateDutyAllotment(DutyTransfer dutyTransfer, DutyTransferDto dutyTransferDto, User loggedUser) {

        Optional<DutyAllotment> optionalDutyAllotment = dutyAllotmentRepository.findByTransferId(dutyTransfer.getId());
        if (optionalDutyAllotment.isPresent()) {
            DutyAllotment dutyAllotment = optionalDutyAllotment.get();
            dutyAllotment.setShiftDate(dateUtil.getStringTODateWk(dutyTransferDto.getShiftDate()));
            Organisation organisation = new Organisation();
            organisation.setId(loggedUser.getOrg_id());
            dutyAllotment.setOrganisation(organisation);
            // dutyAllotment.setDepartmentDetails(loggedUser.getDepartment());
            dutyAllotment.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            dutyAllotment.setUpdatedBy(loggedUser.getId());
            DutyAllotment result = dutyAllotmentRepository.save(dutyAllotment);
            for (DutyTransferAssignDutyDto assignDutyDto : dutyTransferDto.getAssignDutyDetails()) {
                Optional<AssignDuty> optionalAssignDuty = assignDutyRepository
                        .findByAllotmentIdANDUserId(result.getId(), assignDutyDto.getStaffId());
                if (optionalAssignDuty.isPresent()) {
                    AssignDuty assignDuty = optionalAssignDuty.get();
                    assignDuty.setAllotmentId(result.getId());
                    // User user = new User();
                    // user.setId(assignDutyDto.getStaffId());
                    assignDuty.setStaff_id(assignDutyDto.getStaffId());
                    DepartmentDetails department = new DepartmentDetails();
                    department.setId(assignDutyDto.getAssignDeptId());
                    assignDuty.setAssignDepartment(department);
                    Branch branch = new Branch();
                    branch.setId(assignDutyDto.getAssignBranchId());
                    assignDuty.setAssignBranch(branch);
                    assignDuty.setUpdatedBy(loggedUser.getId());
                    assignDuty.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    assignDuty.setEmp_type(assignDutyDto.getEmp_type());
                    Optional<User> userOptional = userRepository.findById(assignDutyDto.getStaffId());
                    if (userOptional.isPresent()) {
                        assignDuty.setCurrentDuty(userOptional.get().getDesignation().getName());
                    }
                    assignDutyRepository.save(assignDuty);
                } else {
                    AssignDuty assignDuty = new AssignDuty();
                    assignDuty.setAllotmentId(result.getId());
                    // User user = new User();
                    // user.setId(assignDutyDto.getStaffId());
                    assignDuty.setStaff_id(assignDutyDto.getStaffId());
                    DepartmentDetails department = new DepartmentDetails();
                    department.setId(assignDutyDto.getAssignDeptId());
                    assignDuty.setAssignDepartment(department);
                    Branch branch = new Branch();
                    branch.setId(assignDutyDto.getAssignBranchId());
                    assignDuty.setAssignBranch(branch);
                    assignDuty.setCreatedBy(loggedUser.getId());
                    assignDuty.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    assignDuty.setEmp_type(assignDutyDto.getEmp_type());
                    Optional<User> userOptional = userRepository.findById(assignDutyDto.getStaffId());
                    if (userOptional.isPresent()) {
                        assignDuty.setCurrentDuty(userOptional.get().getDesignation().getName());
                    }
                    assignDutyRepository.save(assignDuty);
                }
            }
        }
    }


    public ResponseEntity<?> validationDutyAllotOrTransferList(String shiftDate, String staffType) throws UsernameNotFoundException {
        List<DutyAssignUserList> list = new ArrayList<>();
        if (staffType.equalsIgnoreCase("staff")) {
            list = assignDutyRepository.findAllByStaff(dateUtil.getStringTODateWk(shiftDate));
        } else {
            list = assignDutyRepository.findAllByWages(dateUtil.getStringTODateWk(shiftDate));
        }
        if (!list.isEmpty()) {
            List<DutyAssignUserListDTO> result = list.stream().map(data -> {
                DutyAssignUserListDTO assignDutyDto = new DutyAssignUserListDTO(data);
                return assignDutyDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<DutyAssignUserListDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DutyAssignUserListDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}
