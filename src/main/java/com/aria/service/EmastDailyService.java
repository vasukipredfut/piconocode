package com.aria.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class EmastDailyService {

    @Autowired
    private EpayMasterRepository epayMasterRepository;

    @Autowired
    private DepayMasterRepository depayMasterRepository;

    @Autowired
    private EmastDailyRepository emastDailyRepository;

    @Autowired
    private DailyUserDependencyRepository dailyUserDependencyRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BloodGroupRepository bloodGroupRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private WeekOffRepository weekOffRepository;

    @Autowired
    ShiftRotationMappingRepository shiftRotationMappingRepository;

    public EpayMasterAndDepayMasterDto getAllSalaryEMployeeStructureList(String employeeType, Pageable pageable) {
        EpayMasterAndDepayMasterDto ans = new EpayMasterAndDepayMasterDto();

        if (employeeType.equalsIgnoreCase("staff")) {
            Page<EpayMaster> epayMaster = epayMasterRepository.findAll(pageable);
            if (!epayMaster.toList().isEmpty()) {
                List<EpayMaster> epay = epayMaster.toList();
                List<EpayMasterDto> epayDto = new ArrayList<EpayMasterDto>();
                for (int i = 0; i < epay.size(); i++) {
                    EpayMasterDto e = new EpayMasterDto();
                    e.setAlsalary(epay.get(i).getAlsalary());
                    e.setBlsalary(epay.get(i).getBlsalary());
                    e.setEmpcode(epay.get(i).getEmpcode());
                    e.setFlsalary(epay.get(i).getFlsalary());
                    e.setLsalary(epay.get(i).getLsalary());
                    e.setName(epay.get(i).getName());
                    e.setNetsalary(epay.get(i).getNetsalary());
                    e.setPerdayamount(epay.get(i).getPerdayamount());
                    e.setTdeduction(epay.get(i).getTdeduction());
                    epayDto.add(e);
                }
                ans.setStaff(epayDto);
            }
        } else if (employeeType.equalsIgnoreCase("employee")) {
            Page<DepayMaster> page = depayMasterRepository.findAll(pageable);
            if (!page.toList().isEmpty()) {
                List<DepayMaster> depay = page.toList();
                System.out.println("SIZE : " + depay.size());
                List<DepayMasterDto> depayDto = depay.stream().map(epm -> {
                    DepayMasterDto e = new DepayMasterDto();
                    e.setBasic(epm.getBasic());
                    e.setCode(epm.getCode());
                    e.setEpfbase(epm.getEpfbase());
                    e.setEsiceil(epm.getEsiceil());
                    e.setName(epm.getName());
                    return e;
                }).collect(Collectors.toList());
                ans.setWagesEmployee(depayDto);
            }
        }
        return ans;
    }

    public ResponseEntity<?> createUser(EmastDaily userDto) throws UsernameNotFoundException {
        if (userDto.getPhoneno() != null) {
        	if(userDto.getBioid() != null) {
        	boolean existsByBioid = emastDailyRepository.existsByBioid(userDto.getBioid());
        	if(existsByBioid) {
        		 return new ResponseEntity<Status>(new Status("400", "User Already exists for this bioId :"+ userDto.getBioid() + "!!!"), HttpStatus.BAD_REQUEST);
        	}}
            Optional<EmastDaily> userOptional = emastDailyRepository.findByPhoneno(userDto.getPhoneno());
            if (userOptional.isPresent())
                return new ResponseEntity<Status>(new Status("400", "User Already exists!!!"), HttpStatus.OK);
            else {
                DepartmentDetails department = new DepartmentDetails();
                department.setId(userDto.getDepartmentId());
                userDto.setDepartment(department);
                EmastDaily result = emastDailyRepository.save(userDto);
                if (result != null) {
                    updateDependency(userDto, result);
                    return new ResponseEntity<Status>(new Status("200", "User Created Successfully"), HttpStatus.OK);
                }
                return new ResponseEntity<Status>(new Status("500", "User NOt Created Successfully"), HttpStatus.INTERNAL_SERVER_ERROR);

            }
        } else {
            return new ResponseEntity<Status>(new Status("400", "User Not Created"), HttpStatus.BAD_REQUEST);
        }

    }

    public void updateDependency(EmastDaily userDto, EmastDaily user) {
        if (!userDto.getDailyUserDependency().isEmpty()) {
            for (DailyUserDependency userDependency : userDto.getDailyUserDependency()) {
                userDependency.setUserid(user.getId());
                dailyUserDependencyRepository.save(userDependency);
            }
        }
    }

    public ResponseEntity<?> allEmastEmployee(Pageable page) {
        Page<EmastDaily> employee = emastDailyRepository.findAll(page);
        if (!employee.toList().isEmpty()) {
            List<EmastDailyDto> employees = employee.toList().stream().map(emp -> {
                EmastDailyDto emastDailyDto = new EmastDailyDto(emp);
                return emastDailyDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<Object>(employees, HttpStatus.OK);
        }
        return new ResponseEntity<Object>("NO user Exist...", HttpStatus.OK);
    }


    public ResponseEntity<?> filterListUser(Long employeeId, Long branchId, Long departmentId, String employeeName, String status, Long designationId,
                                            String designation, String startDate, String endDate, Pageable pageable)
            throws UsernameNotFoundException {

        Date startDateLocalDate = null, endDateLocalDate = null;

        if (startDate != null && endDate != null) {
            startDateLocalDate = dateUtil.getStringTODateWk(startDate);
            endDateLocalDate = dateUtil.getStringTODateWk(endDate);
        }

        Page<ListUser> userList = emastDailyRepository.emastFilterList(employeeId, branchId, departmentId, employeeName, status, designationId, designation , startDateLocalDate, endDateLocalDate , pageable);

        List<ListUserDto> userListDto = userList.stream().map(user -> {
            ListUserDto userDto = new ListUserDto(user);
            return userDto;
        }).collect(Collectors.toList());

        return new ResponseEntity<>(new PageDataDto(userListDto, userList), HttpStatus.OK);
    }

    public EmastSingleUserResponse singleEmastEmployee(Long employeeId) {
        Optional<EmastDaily> employee = emastDailyRepository.findById(employeeId);
        if (employee.isPresent()) {
            EmastDaily e = employee.get();
            List<DailyUserDependency> dailyUsers = dailyUserDependencyRepository.findByUserid(e.getId());
            EmastSingleUserResponse response = new EmastSingleUserResponse();
            e.setDailyUserDependency(dailyUsers);
            response.setEmast(e);
            if (e.getBranch() != null) {
                Optional<Branch> branch = branchRepository.findById(e.getBranch());
                if (branch.isPresent())
                    response.setBranch(branch.get().getBranchName());
            }
            if (e.getBlood() != null) {
                Optional<BloodGroup> blood = bloodGroupRepository.findById(e.getBlood());
                if (blood.isPresent())
                    response.setBlood(blood.get().getName());
            }
            return response;
        }
        return null;
    }

/*
	public ResponseEntity<?> UserProfile(Long employeeId) throws UsernameNotFoundException {
		Optional<EmastDaily> userOptional = emastDailyRepository.findById(employeeId);
		List<DailyUserDependency> userDependency = dailyUserDependencyRepository.findByUserid(userOptional.get().getId());
		System.out.println(userDependency.size());
		Branch branch = null;
		if (userOptional.get().getBranch() != null) {
			Optional<Branch> branchOptional = branchRepository.findById(userOptional.get().getBranch());
			branch = branchOptional.get();
		}
		Organisation org = null;
		if (userOptional.get().getDepartment() != null && userOptional.get().getDepartment().getOrganisation() != null) {
			Optional<Organisation> organisation = organisationRepository.findById(userOptional.get().getDepartment().getOrganisation().getId());
			org = organisation.get();
		}
		BloodGroup bloodGroup = null;
		if (userOptional.get().getBlood() != null) {
			Optional<BloodGroup> bloodgroupOptional = bloodGroupRepository.findById(userOptional.get().getBlood());
			bloodGroup = bloodgroupOptional.get();
		}

		ResponseUserDto responseUserDto = new ResponseUserDto(userOptional.get(), userDependency, branch, org, bloodGroup);
		Map<String, String> userDetails = responseUserDto.getUserDetails();
		responseUserDto.setUserDetails(userDetails);
		return new ResponseEntity<ResponseUserDto>(responseUserDto, HttpStatus.OK);
	}*/

    public Boolean deleteEmast(Long employeeId) {
        Optional<EmastDaily> eMast = emastDailyRepository.findById(employeeId);
        if (eMast.isPresent()) {
            EmastDaily e = eMast.get();

            e.setStatus("1");
            if (emastDailyRepository.save(e) != null)
                return true;
        }
        return false;
    }

    public ResponseEntity<?> updateUser(EmastDaily userDto) throws UsernameNotFoundException {
        Optional<EmastDaily> userOptional = emastDailyRepository.findById(userDto.getId());
        if(userDto.getBioid() != null) {
    	boolean existsByBioid = emastDailyRepository.existsByBioidAndIdNot(userDto.getBioid(),userDto.getId());
    	if(existsByBioid) {
    		 return new ResponseEntity<Status>(new Status("400", "User Already exists for this bioId :"+ userDto.getBioid() + "!!!"), HttpStatus.BAD_REQUEST);
    	}
        }
        if (userOptional.isPresent()) {
            DepartmentDetails department = new DepartmentDetails();
            department.setId(userDto.getDepartmentId());
            // userDto.setStatus("0"); when update time status sent from front-end
            // userDto.setDepartment(department);
            System.out.println(userDto.getCategory());
            EmastDaily result = emastDailyRepository.saveAndFlush(userDto);
            if (result != null) {
                return new ResponseEntity<Object>(new Status("200", "User Updated Successfully"), HttpStatus.OK);
            }
        } else
            return new ResponseEntity<Object>(new Status("404", "User Record not Found"), HttpStatus.OK);

        return new ResponseEntity<Object>(new Status("500", "User NOt Created Successfully"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }


    public ResponseEntity<?> commonEmastStaffFilter(Long employeeId, Long branchId, Long departmentId, String employeeName,
                                                    String employeeType, String weekOff, Long dataCatId)
            throws UsernameNotFoundException {
        List<UserList> userList = null;

        System.out.println( "dataCatId  == "+dataCatId);

        if (dataCatId == 1) {
            userList = emastDailyRepository.commonEmastStaffFilterByDaCat(employeeId, branchId, departmentId, employeeName, employeeType);
        } else {
            userList = emastDailyRepository.commonEmastStaffFilter(employeeId, branchId, departmentId, employeeName, employeeType, dataCatId);
        }
        List<UserListDto> result = userList.stream().map(user -> {
            UserListDto userListDto = new UserListDto(user);
            if (weekOff != null && weekOff.equalsIgnoreCase("weekOff")) {
                Optional<Weekoff> optionalWeekOff = weekOffRepository.findById(user.getId());
                if (optionalWeekOff.isPresent()) {
                    userListDto.setCurrentWeekOff(optionalWeekOff.get().getWeekOffDay().getWeekday());
                }
            }
            ShiftRotationMappingData shiftRotation = shiftRotationMappingRepository.findByShiftNameANdCreatedDate(userListDto.getId(), 1);
            if (shiftRotation != null) {
                if (shiftRotation.getShiftName() != null)
                    userListDto.setLastRotateShiftName(shiftRotation.getShiftName());

                if (shiftRotation.getCreatedDate() != null)
                    userListDto.setLastShiftRotateDate(dateUtil.getDateTOStringWk(shiftRotation.getCreatedDate()));
            }
            return userListDto;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<UserListDto>>(result, HttpStatus.OK);
    }

    public ResponseEntity<?> allContractLabour(String name) {
        List<EmastDaily> employee = emastDailyRepository.findByName(name);
        if (!employee.isEmpty()) {
            List<ContractLabourDropDownDto> employees = employee.stream().map(emp -> {
                ContractLabourDropDownDto emastDailyDto = new ContractLabourDropDownDto(emp.getId(), emp.getName());
                return emastDailyDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<Object>(employees, HttpStatus.OK);
        }
        return new ResponseEntity<Object>("NO user Exist...", HttpStatus.OK);
    }

    public ResponseEntity<?> findAllByQualification(String qualification) {
        List<String> qualificationList = emastDailyRepository.findAllByQualification(qualification);
        return new ResponseEntity<List<String>>(qualificationList, HttpStatus.OK);
    }
}
