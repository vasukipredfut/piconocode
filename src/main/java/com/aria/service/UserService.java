package com.aria.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aria.config.JwtUtils;
import com.aria.dao.BloodGroupRepository;
import com.aria.dao.BranchRepository;
import com.aria.dao.DaCategoryRepository;
import com.aria.dao.EmployeeQualificationMappingRepository;
import com.aria.dao.MachineRepository;
import com.aria.dao.OrganisationRepository;
import com.aria.dao.QualificationRepository;
import com.aria.dao.RoleRepository;
import com.aria.dao.RolesRepository;
import com.aria.dao.ShiftRotationMappingRepository;
import com.aria.dao.ShiftsRepository;
import com.aria.dao.UserDependencyRepository;
import com.aria.dao.UserRepository;
import com.aria.dao.WeekOffRepository;
import com.aria.dto.ListUserDto;
import com.aria.dto.LoginDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.PasswordDto;
import com.aria.dto.ResponseUserDto;
import com.aria.dto.ShiftRotationMappingData;
import com.aria.dto.UserDependencyDto;
import com.aria.dto.UserDropDownDto;
import com.aria.dto.UserDto;
import com.aria.dto.UserList;
import com.aria.dto.UserListDto;
import com.aria.model.BloodGroup;
import com.aria.model.Branch;
import com.aria.model.DaCategory;
import com.aria.model.EmployeeQualificationMapping;
import com.aria.model.ListUser;
import com.aria.model.Machine;
import com.aria.model.Organisation;
import com.aria.model.Qualification;
import com.aria.model.Role;
import com.aria.model.Roles;
import com.aria.model.Shifts;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.model.UserDependency;
import com.aria.model.Weekoff;
import com.aria.utils.DateUtil;

@Service
public class UserService {

	public static final String ESCALATION_TIER_1 = "TIER1";
	public static final String ESCALATION_TIER_2 = "TIER2";

	public static final String DEPARTMENT_ASSISTANT_MANAGER = "Department Assistant Manager";
	public static final String DEPARTMENT_MANAGER = "Department Manager";

	public static final String PLANT_MANAGER = "Plant Manager";
	@Autowired
	PasswordEncoder encoder;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RolesRepository roleRepository;

	@Autowired
	OrganisationRepository organisationRepository;

	@Autowired
	BloodGroupRepository bloodGroupRepository;

	@Autowired
	BranchRepository branchRepository;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	DateUtil dateUtil;

	@Autowired
	WeekOffRepository weekOffRepository;

	@Autowired
	FileStorageService fileStorageService;

	@Autowired
	DaCategoryRepository daCategoryRepository;

	@Autowired
	UserDependencyRepository dependencyRepository;

	@Autowired
	QualificationRepository qualificationRepository;

	@Autowired
	MachineRepository machineRepository;

	@Autowired
	EmployeeQualificationMappingRepository employeeQualificationMappingRepository;

	@Autowired
	ShiftRotationMappingRepository shiftRotationMappingRepository;
	
	@Autowired
	ShiftsRepository shiftRepository;

	public ResponseEntity<?> createUser(UserDto userDto, User loggedUser) throws UsernameNotFoundException {

		if (userDto.getEmail() != null && userDto.getDesignationId() != null) {
			if(userDto.getBioId() != null) {
			boolean existsByBioid = userRepository.existsByBioid(userDto.getBioId());
        	if(existsByBioid) {
        		 return new ResponseEntity<Status>(new Status("400", "User Already exists for this bioId :"+ userDto.getBioId() + "!!!"), HttpStatus.BAD_REQUEST);
        	}}
			Optional<User> userOptional = userRepository.findByEmail(userDto.getEmail());
			if (userOptional.isPresent()) {
				return new ResponseEntity<Status>(new Status("400", "User Email Already exists!!!"), HttpStatus.OK);
			} else {
				Optional<User> aadharOptional = userRepository.findByAadharno(userDto.getAadharNo());
				if (aadharOptional.isPresent()) {
					return new ResponseEntity<Status>(new Status("400", "AadharNo Already exists!!!"), HttpStatus.OK);
				} else {

					User userObj = new User(userDto);
					userObj.setPassword(encoder.encode("changeme"));

					userObj.setOrg_id(loggedUser.getOrg_id());
//                    Role userRole = roleRepository.findById(userDto.getDesignationId())
//                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                    userObj.setRoles_id(userRole.getId());
					System.out.println("#####");
					System.out.println("roleID  = " + userDto.getRoles_id());
					System.out.println("#####");
					userObj.setActive(0);
					userObj.setStatus("A");
					userObj.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
					userObj.setJoiningDate(dateUtil.getStringTODateWk(userDto.getJoiningDate()));
					User result = userRepository.save(userObj);
					result.setUsername(result.getId().toString());
					userRepository.save(result);
					updateQualification(userDto, result);
					updateDependency(userDto, result);
					return new ResponseEntity<Status>(new Status("200", "User Created Successfully"), HttpStatus.OK);
				}
			}
		} else {
			return new ResponseEntity<Status>(new Status("400", "User Not Created"), HttpStatus.BAD_REQUEST);
		}

	}

	public ResponseEntity<?> UpdateUser(UserDto userDto, User loggedUser) throws UsernameNotFoundException {
		Optional<User> userout = null ;
		userout = userRepository.findByEmail(userDto.getEmail());
		if(userDto.getBioId() != null) {
		boolean existsByBioid = userRepository.existsByBioidAndIdNot(userDto.getBioId(),userDto.getId());
    	if(existsByBioid) {
    		 return new ResponseEntity<Status>(new Status("400", "User Already exists for this bioId :"+ userDto.getBioId() + "!!!"), HttpStatus.BAD_REQUEST);
    	}}
		if (userout.isPresent()) {
			System.out.println("id "+userout.get().getId().toString());
			System.out.println("id1 "+userDto.getId().toString());
			if (!userout.get().getId().toString().equalsIgnoreCase(userDto.getId().toString())){
				return new ResponseEntity<Status>(new Status("400", "User Email Already exists!!!"), HttpStatus.OK);
			}
		}else {
			userout = userRepository.findByIdValue(userDto.getId());
		}

		if (userout.isPresent()) {
			User userObj = new User(userDto);
			userObj.setPassword(userout.get().getPassword());
			userObj.setUsername(userout.get().getId().toString());
			userObj.setActive(0);
			userObj.setOrg_id(loggedUser.getOrg_id());
//			Role userRole = roleRepository.findById(userDto.getDesignationId())
//					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//			userObj.setRoles_id(userRole.getId());
			userObj.setRoles_id(userDto.getRoles_id());
			if (userDto.getStatus() == null || userDto.getStatus().isEmpty() || userDto.getStatus().equalsIgnoreCase("P")) {
				userObj.setStatus("A");
			}
			userObj.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			userObj.setJoiningDate(dateUtil.getStringTODateWk(userDto.getJoiningDate()));
			User result = userRepository.save(userObj);
			updateQualification(userDto, result);
			updateDependency(userDto, result);
		} else {
			if (userDto.getStatus() != null && userDto.getStatus().equalsIgnoreCase("P")) {
				updateInterviewUser(userDto, loggedUser);
				return new ResponseEntity<Status>(new Status("200", "User Updated Successfully"), HttpStatus.OK);
			} else if (userDto.getStatus() != null && userDto.getStatus().equalsIgnoreCase("A")) {
				Optional<User> inactiveUserOut = userRepository.findByEmailCheck(userDto.getId(), userDto.getEmail());
				if(inactiveUserOut.isPresent()) {
					updateInactiveUser(inactiveUserOut.get(), userDto, loggedUser);
				}else {
					return new ResponseEntity<Status>(new Status("404", "User Record not Found"), HttpStatus.OK);
				}
			} else {
				return new ResponseEntity<Status>(new Status("404", "User Record not Found"), HttpStatus.OK);
			}
		}

		return new ResponseEntity<Status>(new Status("200", "User Updated Successfully"), HttpStatus.OK);
	}

	public void updateInterviewUser(UserDto userDto, User loggedUser) {
		User userObj = new User(userDto);
		userObj.setPassword(encoder.encode("changeme"));
		userObj.setActive(0);
		userObj.setOrg_id(userDto.getOrgId());
		/*Role userRole = roleRepository.findById(userDto.getDesignationId())
				.orElseThrow(() -> new RuntimeException("Error: Role is not found."));*/
		userObj.setRoles_id(userDto.getRoles_id());
		userObj.setStatus("A");
		userObj.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		userObj.setJoiningDate(dateUtil.getStringTODateWk(userDto.getJoiningDate()));
		User result = userRepository.save(userObj);
		result.setUsername(result.getId().toString());
		userRepository.save(result);
		updateDependency(userDto, result);
	}



	public void updateInactiveUser(User inactiveUser , UserDto userDto, User loggedUser) {

		User userObj = new User(userDto);
		userObj.setId(inactiveUser.getId());
		userObj.setUsername(inactiveUser.getId().toString());
		userObj.setPassword(encoder.encode("changeme"));
		userObj.setActive(0);
		userObj.setOrg_id(userDto.getOrgId());
		/*Role userRole = roleRepository.findById(userDto.getDesignationId())
				.orElseThrow(() -> new RuntimeException("Error: Role is not found."));*/
		userObj.setRoles_id(userDto.getRoles_id());
		userObj.setStatus("A");
		userObj.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		userObj.setJoiningDate(dateUtil.getStringTODateWk(userDto.getJoiningDate()));
		User result = userRepository.save(userObj);
		updateDependency(userDto, result);
	}

	public ResponseEntity<?> ListUser() throws UsernameNotFoundException {

		List<User> userout = userRepository.findAll();

		return new ResponseEntity<List<ResponseUserDto>>(userout.stream().map(user -> {

			ResponseUserDto responseUserDto = new ResponseUserDto(user);
			Map<String, Object> userDetails = responseUserDto.getUserDetails();
			if (user.getJoiningDate() != null) {
				userDetails.put("joiningDate", dateUtil.getDateTOStringWk(user.getJoiningDate()));
			} else {
				userDetails.put("joiningDate", "");
			}

			List<String> qualificationList = new ArrayList<>();
			List<EmployeeQualificationMapping> employeeQualificationMappingList = employeeQualificationMappingRepository
					.findByEmpId(user.getId());
			if (!employeeQualificationMappingList.isEmpty()) {
				for (EmployeeQualificationMapping employeeQualificationMapping : employeeQualificationMappingList) {
					Optional<Qualification> qualification = qualificationRepository
							.findById(employeeQualificationMapping.getQualificationId());
					if (qualification.isPresent()) {
						qualificationList.add(qualification.get().getName());
					}
				}
			}
			userDetails.put("qualificationList", qualificationList);

			responseUserDto.setUserDetails(userDetails);
			return responseUserDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}

	public ResponseEntity<?> ListAllUser(Pageable pageable) throws UsernameNotFoundException {
		Page<User> list = userRepository.findAll(pageable);

		if (!list.toList().isEmpty()) {
			List<UserDto> result = list.toList().stream().map(user -> {
				UserDto userDto = new UserDto(user);
				if (user.getJoiningDate() != null) {
					userDto.setJoiningDate(dateUtil.getDateTOStringWk(user.getJoiningDate()));
				}
				return userDto;
			}).collect(Collectors.toList());
			PageDataDto<UserDto> pageData = new PageDataDto<>(result, list);
			return new ResponseEntity<PageDataDto<UserDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<UserDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	public List<UserDropDownDto> getAllUser(String username, Long machineId, String escalationUserType) {
		List<User> users = new ArrayList<>();
		if (machineId != null && escalationUserType != null && escalationUserType.equalsIgnoreCase(ESCALATION_TIER_1)) {
			getTierOneUserByMachine(username, machineId, users);
		} else if (machineId != null && escalationUserType != null
				&& escalationUserType.equalsIgnoreCase(ESCALATION_TIER_2)) {
			getTierTwoUserByMachine(username, machineId, users);
		} else if (machineId != null) {
			Optional<Machine> machineOptional = machineRepository.findById(machineId);
			machineOptional.ifPresent(machine -> {
				if (machine.getBranch() != null) {
					List<User> deptUser = userRepository.findAllByBranchAndDepartment(machine.getBranch().getId(),
							machine.getDepartmentDetails().getId());
					users.addAll(filterByUser(username, deptUser));
				}

			});
		} else {
			List<User> UserByName = userRepository.findAllByEmployeeName(username);
			users.addAll(UserByName);
		}

		List<UserDropDownDto> result = new ArrayList<>();
		if (!users.isEmpty()) {
			result = users.stream().map(user -> new UserDropDownDto(user.getId(), user.getEmployeename()))
					.collect(Collectors.toList());
		}
		return result;
	}

	private void getTierTwoUserByMachine(String username, Long machineId, List<User> users) {
		Optional<Machine> machineOptional = machineRepository.findById(machineId);

		machineOptional.ifPresent(machine -> {
			Optional<Roles> roleOpt = roleRepository.findByNameIgnoreCase(PLANT_MANAGER);
			roleOpt.ifPresent(role -> {
				List<User> deptUser = userRepository.findAllByBranchAndRole(machine.getBranch().getId(), role.getId());
				users.addAll(filterByUser(username, deptUser));
			});

		});

	}

	private void getTierOneUserByMachine(String username, Long machineId, List<User> users) {
		Optional<Machine> machineOptional = machineRepository.findById(machineId);
		List<String> roleNames = new ArrayList<>();
		roleNames.add(DEPARTMENT_MANAGER);
		roleNames.add(DEPARTMENT_ASSISTANT_MANAGER);
		machineOptional.ifPresent( machine -> {
			List<Long> roleIds = roleRepository.findByIds(roleNames);

				List<User> deptUser = userRepository.findAllByBranchAndDepartmentAndRoleIds(machine.getBranch().getId(),
						machine.getDepartmentDetails().getId(), roleIds);
				users.addAll(filterByUser(username, deptUser));

		});
	}

	private List<User> filterByUser(String username, List<User> deptUser) {
		if (username != null) {
			return deptUser.stream()
					.filter(user -> user.getEmployeename() != null
							&& user.getEmployeename().equalsIgnoreCase(username.toLowerCase()))
					.collect(Collectors.toList());
		}
		return deptUser;
	}

	public ResponseEntity<?> UserProfile(String id) throws UsernameNotFoundException {
		Optional<User> userOptional = userRepository.findById(Long.parseLong(id));
		List<UserDependency> userDependency = dependencyRepository.findAllByUserId(userOptional.get().getId());
		System.out.println(userDependency.size());
		Branch branch = null;
		if (userOptional.get().getBranch_id() != null) {
			Optional<Branch> branchOptional = branchRepository.findById(userOptional.get().getBranch_id());
			if(branchOptional.isPresent()) {
			branch = branchOptional.get();
			}
		}
		Organisation org = null;
		if (userOptional.get().getOrg_id() != null) {
			Optional<Organisation> organisation = organisationRepository.findById(userOptional.get().getOrg_id());
			org = organisation.get();
		}
		Roles role = null;
		if (userOptional.get().getRoles_id() != null) {
			Optional<Roles> roleOptional = roleRepository.findById(userOptional.get().getRoles_id());
			if(roleOptional.isPresent()) {
			role = roleOptional.get();
			}
		}
		BloodGroup bloodGroup = null;
		if (userOptional.get().getBloodgroupid() != null) {
			Optional<BloodGroup> bloodgroupOptional = bloodGroupRepository
					.findById(userOptional.get().getBloodgroupid());
			if(bloodgroupOptional.isPresent()) {
			bloodGroup = bloodgroupOptional.get();
			}
		}
		DaCategory daCategory = null;
		if (userOptional.get().getDacatid() != null && userOptional.get().getDacatid() != 0) {
			System.out.println(userOptional.get().getDacatid());
			Optional<DaCategory> daCategoryOptional = daCategoryRepository.findById(userOptional.get().getDacatid());
			if(daCategoryOptional.isPresent() ) {
			daCategory = daCategoryOptional.get();
			}
		}

		ResponseUserDto responseUserDto = new ResponseUserDto(userOptional.get(), userDependency, branch, org, role,
				bloodGroup, daCategory);
		Map<String, Object> userDetails = responseUserDto.getUserDetails();
		if (userOptional.get().getJoiningDate() != null) {
			userDetails.put("joiningDate", dateUtil.getDateTOStringWk(userOptional.get().getJoiningDate()));
		} else {
			userDetails.put("joiningDate", "");
		}

		List<String> qualificationList = new ArrayList<>();
		List<EmployeeQualificationMapping> employeeQualificationMappingList = employeeQualificationMappingRepository
				.findByEmpId(userOptional.get().getId());
		if (!employeeQualificationMappingList.isEmpty()) {
			for (EmployeeQualificationMapping employeeQualificationMapping : employeeQualificationMappingList) {
				Optional<Qualification> qualification = qualificationRepository
						.findById(employeeQualificationMapping.getQualificationId());
				if (qualification.isPresent()) {
					qualificationList.add(qualification.get().getName());
				}
			}
		}
		userDetails.put("qualificationList", qualificationList);
		Date todayDate = java.sql.Date.valueOf(LocalDate.now());
		  Shifts shift = shiftRepository.findTodayShift(userOptional.get().getId(),todayDate);
		  if (shift != null) {
			    userDetails.put("shiftId", shift.getId());
			    userDetails.put("shiftNumber", shift.getShiftnumber());
			    userDetails.put("shiftName", shift.getShiftName());
			    userDetails.put("shiftStartTime", shift.getIntime());
			    userDetails.put("shiftEndTime", shift.getOuttime());
			} else {
			    userDetails.put("shiftId", null);
			    userDetails.put("shiftNumber", null);
			    userDetails.put("shiftName", "");
			    userDetails.put("shiftStartTime", null);
			    userDetails.put("shiftEndTime", null);
			}

		updateAdhar(userDetails, userOptional.get());
		responseUserDto.setUserDetails(userDetails);
		return new ResponseEntity<ResponseUserDto>(responseUserDto, HttpStatus.OK);
	}

	public ResponseEntity<?> DeleteUser(String userId) throws UsernameNotFoundException {

		try {
			Optional<User> userOptional = userRepository.findById(Long.parseLong(userId));
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				userRepository.delete(user);
			}
			return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
		}
	}

	public ResponseEntity<?> checkUser(LoginDto loginDto) throws UsernameNotFoundException {

		Optional<User> userOptional = userRepository.findByUsername(loginDto.getUsername());
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			String pwd = user.getPassword();
			System.out.println(pwd);
			String pwd1 = encoder.encode(loginDto.getPassword().trim());
			System.out.println(pwd1);
			return new ResponseEntity<Status>(new Status("200", "user valid"), HttpStatus.OK);
		} else {
			return new ResponseEntity<Status>(new Status("400", "user not valid "), HttpStatus.OK);
		}
	}

	public void updateDependency(UserDto userDto, User user) {
		if (!userDto.getDependencies().isEmpty()) {
			for (UserDependencyDto userDependencyDto : userDto.getDependencies()) {
				UserDependency userDependency = new UserDependency(userDependencyDto);
				userDependency.setUserid(user.getId());
				dependencyRepository.save(userDependency);
			}
		}
	}

	public void updateQualification(UserDto userDto, User user) {
		
		List<EmployeeQualificationMapping> employeeQualificationMappings = employeeQualificationMappingRepository.findByEmpId(user.getId());
		
		if (!employeeQualificationMappings.isEmpty()) {
			employeeQualificationMappingRepository.deleteAll(employeeQualificationMappings);
		}
		
		if (userDto.getQualificationList() != null && !userDto.getQualificationList().isEmpty()) {
			for (String qualification : userDto.getQualificationList()) {
				Qualification resultQuali = null;
				Optional<Qualification> optionalQualification = qualificationRepository.findByName(qualification);
				if (optionalQualification.isPresent()) {
					resultQuali = optionalQualification.get();

				} else {
					Qualification qualification1 = new Qualification(qualification);
					resultQuali = qualificationRepository.save(qualification1);
				}
				EmployeeQualificationMapping qualificationMapping = new EmployeeQualificationMapping();
				qualificationMapping.setQualificationId(resultQuali.getId());
				qualificationMapping.setEmpId(user.getId());
				employeeQualificationMappingRepository.save(qualificationMapping);
			}
		}
	}

	public String getToken(User user) {
		String token = jwtUtils.generateTokenFromUsername1(user.getUsername());
		return token;
	}

	public String verifyToken(String token, HttpServletRequest request) {
		String result = null;
		if (!jwtUtils.validateJwtToken(token,request)) {
			result = "access token isValid";
		} else {
			result = "access token expired";
		}
		return result;
	}

	public String updateStatus(String token,HttpServletRequest request) {
		String result = null;
		String username = jwtUtils.getUserNameFromJwtToken(token);
		if (jwtUtils.validateJwtToken(token,request)) {
			Optional<User> user = userRepository.findByUsername(username);
			if (user.isPresent()) {
				User user1 = user.get();
				user1.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				userRepository.save(user1);
				result = "verified successfully!";
			}
		} else {
			result = "token expired";
		}
		return result;
	}

	public String setForgetPassword(String token, String password, String retypePassword, HttpServletRequest request) {
		String result = null;
		String username = jwtUtils.getUserNameFromJwtToken(token);
		if (jwtUtils.validateJwtToken(token,request)) {
			Optional<User> user = userRepository.findByUsername(username);
			if (user.isPresent()) {
				User user1 = user.get();
				user1.setPassword(encoder.encode(password));
				userRepository.save(user1);
				result = "password updated successfully";
			}
		} else {
			result = "token expired";
		}
		return result;
	}

	public ResponseEntity<?> filterListUser(Long employeeId, Long branchId, Long departmentId, String employeeName,
			String employeeType, Long designationId, String designation, String status, String startDate,
			String endDate, Pageable pageable) throws UsernameNotFoundException {

		Date startDateLocalDate = null, endDateLocalDate = null;

		if (startDate != null && endDate != null) {
			startDateLocalDate = dateUtil.getStringTODateWk(startDate);
			endDateLocalDate = dateUtil.getStringTODateWk(endDate);
		}

		Page<ListUser> userList = userRepository.filterListUserData(employeeId, branchId, departmentId, employeeName,
				employeeType, designationId, designation, status, startDateLocalDate, endDateLocalDate, pageable);

		List<ListUserDto> userListDto = userList.stream().map(user -> {
			ListUserDto userDto = new ListUserDto(user);
			return userDto;
		}).collect(Collectors.toList());

		return new ResponseEntity<>(new PageDataDto(userListDto, userList), HttpStatus.OK);
	}

	public ResponseEntity<?> stafflistwfilter(Long employeeId, Long branchId, Long departmentId, String employeeName,
			String employeeType, Long designationId, Integer active, Long shiftId, String sex)
			throws UsernameNotFoundException {

		Date now = dateUtil.getISTDate();

		List<ListUser> userList = userRepository.stafflistwfilter(employeeId, branchId, departmentId, employeeName,
				employeeType, designationId, active, shiftId, sex, now);

		return new ResponseEntity<List<ListUserDto>>(userList.stream().map(user -> {
			ListUserDto userDto = new ListUserDto(user);
			return userDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}

	public ResponseEntity<?> stafflistwfilter(Long employeeId, Long branchId, Long departmentId, String employeeName,
			String employeeType, Long designationId, String location) throws UsernameNotFoundException {
		List<ListUser> userList = userRepository.stafflistfilter(employeeId, branchId, departmentId, employeeName,
				employeeType, designationId, location);

		return new ResponseEntity<List<ListUserDto>>(userList.stream().map(user -> {
			ListUserDto userDto = new ListUserDto(user);
			return userDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}

	public void savePass() {
		List<User> users = userRepository.findAll();
		for (User user : users) {
			user.setPassword(encoder.encode("predfut"));
			userRepository.save(user);
		}
	}

	public ResponseEntity<?> commonStaffList(Long employeeId, Long dacatId, Long branchId, Long departmentId,
			String employeeName, String employeeType, Long designationId, String weekOff)
			throws UsernameNotFoundException {
		List<UserList> userList;
		if(dacatId==6) {
		userList = userRepository.commonStaffFilter(employeeId, dacatId, branchId, departmentId,
				employeeName, employeeType, designationId);
		}
		else {
			userList = userRepository.commonStaffFilterList(employeeId, dacatId, branchId, departmentId,
					employeeName, employeeType, designationId);
		}
		List<UserListDto> result = userList.stream().map(user -> {
			UserListDto userListDto = new UserListDto(user);
			if (weekOff != null && weekOff.equalsIgnoreCase("weekOff")) {
				Optional<Weekoff> optionalWeekOff = weekOffRepository.findById(user.getId());
				if (optionalWeekOff.isPresent()) {
					userListDto.setCurrentWeekOff(optionalWeekOff.get().getWeekOffDay().getWeekday());
				}
			}

			ShiftRotationMappingData shiftRotation = shiftRotationMappingRepository
					.findByShiftNameANdCreatedDate(userListDto.getId(), 0);
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

	public void updateAdhar(Map<String, Object> stringObjectMap, User userDto) {

		if (userDto.getAadhaarfile() != null) {
			String pictureBase64String = null;
			try {
				pictureBase64String = fileStorageService.downloadFileAsBase64(userDto.getAadhaarfile());
			} catch (IOException e) {
				System.out.println("No file found" + e.getMessage());
			}
			stringObjectMap.put("aadharBae64", pictureBase64String);

		}

		if (userDto.getPhoto() != null) {

			String pictureBase64String = null;
			try {
				pictureBase64String = fileStorageService.downloadFileAsBase64(userDto.getPhoto());
			} catch (IOException e) {
				System.out.println("No file found" + e.getMessage());
			}
			stringObjectMap.put("pictureBae64", pictureBase64String);

		}

	}

	public ResponseEntity<?> changePassword(PasswordDto passwordDto, User loggedUser) {
		Optional<User> user = userRepository.findById(loggedUser.getId());
		if (user.isPresent()) {
			User updateUser = user.get();
			if (passwordDto.getOldPassWord() != null && passwordDto.getNewPassWord() != null) {
				String oldPwd = passwordDto.getOldPassWord();
				String pwd = passwordDto.getNewPassWord();
				Boolean status = encoder.matches(oldPwd, updateUser.getPassword());
				if (status) {
					updateUser.setPassword(encoder.encode(pwd));
					userRepository.save(updateUser);
					return new ResponseEntity<Status>(new Status("200", "password updated successfully!!!"), HttpStatus.OK);
				} else {
					return new ResponseEntity<Status>(new Status("403", "Old Password Not Match!!!"), HttpStatus.OK);
				}
			} else {
				return new ResponseEntity<Status>(new Status("204", "OLD OR NEW Password Missing!!!"), HttpStatus.OK);
			}
		} else {
			return new ResponseEntity<Status>(new Status("404", "User Not Match!!!"), HttpStatus.OK);
		}
	}

	public void updatePass(Long id) {
		Optional<User> userOptional = userRepository.findById(id);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			user.setPassword(encoder.encode("changeme"));
			userRepository.save(user);
		}
	}

}
