package com.aria.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.validation.Valid;


import com.aria.dao.*;
import com.aria.model.*;
import com.aria.service.*;
import com.aria.auth.AuthUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.aria.config.JwtUtils;
import com.aria.dto.BranchDto;
import com.aria.dto.DepartmentDto;
import com.aria.dto.JwtResponse;
import com.aria.dto.LoginDto;
import com.aria.dto.PasswordDto;
import com.aria.dto.SearchDto;
import com.aria.dto.ShiftDto;
import com.aria.dto.TokenRefreshRequest;
import com.aria.dto.TokenRefreshResponse;
import com.aria.dto.UserDropDownDto;
import com.aria.dto.UserDto;
import com.aria.utils.TokenRefreshException;


@RestController
@CrossOrigin
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PermissionsRepository permissionsRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    DesignationRepository designationRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Autowired
    OrganizationService organizationService;

    @Autowired
    CommonService commonService;

    @Autowired
    AttendanceRepository attendanceRepository;

    @Autowired
    ShiftAssignRepository shiftAssignRepository;

    @Autowired
    AttendanceService attendanceService;

    private static final LocalTime CUT_OFF_TIME = LocalTime.of(6,10);

    @PostMapping("/api/auth/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDto loginRequest) {

        long startTime = System.currentTimeMillis();
        String username = loginRequest.getUsername();

        logger.info("AUTH_LOGIN_ATTEMPT | username={}", username);

        try {

            // 1️⃣ Authenticate credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            AuthUserDetails userDetails = (AuthUserDetails) authentication.getPrincipal();

            // 2️⃣ Fetch User
            Optional<User> optionalUser = userRepository.findById(userDetails.getId());
            if (!optionalUser.isPresent()) {
                logger.warn("AUTH_LOGIN_FAILED | username={} | reason=USER_NOT_FOUND", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new Status("401", "User not found"));
            }

            User user = optionalUser.get();
            Integer roleId = user.getRoles_id() != null ? user.getRoles_id().intValue() : null;
            Long branchId = user.getBranch_id();
            Long userId = user.getId();
            Date todate = new Date();
            Long shiftId = shiftAssignRepository.findShiftIdByEmpId(userId,todate);

            // 3️⃣ Attendance & Shift Validation
            if (shouldValidateBranch(branchId)) {

                if (shouldValidateAttendance(roleId)) {

                    LocalDate attendanceDate = getAttendanceDate(shiftId);

                    Date todayDate = Date.from(attendanceDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

                    boolean shiftExists =
                            shiftAssignRepository.existsByEmpIdAndShiftDateAndEmpType(userId, todayDate, 0);

                    if (!shiftExists) {

                        logger.warn("AUTH_LOGIN_BLOCKED | userId={} | reason=SHIFT_NOT_ASSIGNED", userId);

                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new Status(
                                        "403",
                                        "Login denied. No shift is assigned for today. Please contact your administrator."
                                ));
                    }

                    LocalDate today = getAttendanceDate(shiftId);

                    boolean attendanceExists =
                            attendanceRepository.existsAttendanceForDate(userId, today);

                    if (!attendanceExists) {

                        logger.warn("AUTH_LOGIN_BLOCKED | userId={} | reason=ATTENDANCE_NOT_MARKED", userId);

                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new Status(
                                        "403",
                                        "Login denied. No biometric check-in has been recorded for today. Please complete your biometric verification to continue."
                                ));
                    }

                    boolean isOutTimeValid = attendanceService.isOutTimeValid(userId, today);

                    if (!isOutTimeValid) {

                        logger.warn("AUTH_LOGIN_BLOCKED | userId={} | reason=SHIFT_ALREADY_COMPLETED", userId);

                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new Status(
                                        "403",
                                        "Login denied. Your shift for today has already been completed."
                                ));
                    }

                } else {
                    logger.info("AUTH_LOGIN_BYPASS_ATTENDANCE | userId={} | reason=ROLE_BYPASS | roleId={}", userId, roleId);
                }
            } else {
                logger.info("AUTH_LOGIN_BYPASS_BRANCH_VALIDATION | userId={} | reason=BRANCH_BYPASS | branchId={}", userId, branchId);
            }

            // 4️⃣ Generate Tokens
            String jwt = jwtUtils.generateJwtToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);

            List<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            List<Permissions> permissions =
                    roleId != null ? permissionsRepository.findByRoleId(roleId) : new ArrayList<>();

            long duration = System.currentTimeMillis() - startTime;

            logger.info("AUTH_LOGIN_SUCCESS | userId={} | username={} | roleId={} | durationMs={}",
                    userId, username, roleId, duration);

            return ResponseEntity.ok(
                    new JwtResponse(
                            jwt,
                            refreshToken.getToken(),
                            userId,
                            username,
                            roles,
                            permissions
                    )
            );

        } catch (BadCredentialsException ex) {

            logger.warn("AUTH_LOGIN_FAILED | username={} | reason=BAD_CREDENTIALS", username);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Status("401", "Invalid username or password"));

        } catch (Exception ex) {

            long duration = System.currentTimeMillis() - startTime;

            logger.error("AUTH_LOGIN_ERROR | username={} | durationMs={} | error={}",
                    username, duration, ex.getMessage(), ex);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Status("500", "Something went wrong"));
        }
    }


    private boolean shouldValidateAttendance(Integer roleId) {
        Set<Integer> bypassRoles = new HashSet<>(Arrays.asList(22, 14, 101, 63));
        return roleId == null || !bypassRoles.contains(roleId);
    }

    private boolean shouldValidateBranch(Long branchId) {
        return branchId != null && !Arrays.asList(8L, 10L).contains(branchId);
    }


    public LocalDate getAttendanceDate(Long shiftId) {
        try {
            LocalTime currentTime = LocalTime.now();
            LocalDate today = LocalDate.now();

            LocalDate attendanceDate = (Long.valueOf(150).equals(shiftId) && currentTime.isBefore(CUT_OFF_TIME))
                    ? today.minusDays(1)
                    : today;

            logger.info("Attendance date calculated. CurrentTime: {}, AttendanceDate: {}", currentTime, attendanceDate);

            return attendanceDate;

        } catch (Exception ex) {
            logger.error("Error while calculating attendance date", ex);
            // fallback to today to avoid system failure
            return LocalDate.now();
        }
    }


    @PostMapping("/api/auth/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new Status("400", "Error: Username is already taken!"));
        }
        User user = new User(signUpRequest);
        user.setPassword(encoder.encode("changeme"));
        Role userRole = roleRepository.findById(signUpRequest.getDesignationId())
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        user.setRoles_id(userRole.getId());
        user.setCreated_date(LocalDateTime.now());
        user.setUpdated_date(LocalDateTime.now());
        userRepository.save(user);
        return ResponseEntity.ok(new Status("200", "User registered successfully!"));
    }

    @PostMapping("/api/auth/refreshToken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {

        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken).map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser).map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                }).orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token was expired. Please make a new signin request!"));
    }

    @RequestMapping(value = "/api/logout", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> logout(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.isPresent() && user.get().getId() > 0) {
            refreshTokenService.delete(user.get().getId());
        } else {
            return ResponseEntity.ok(new Status("403", "User Not Logged in !!!"));
        }

        return ResponseEntity.ok(new Status("200", "User logged out successfully!"));
    }

    @RequestMapping(value = "/api/createUser", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userdto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return userService.createUser(userdto, user.get());
    }

    @RequestMapping(value = "/api/updateUser", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDto userdto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return userService.UpdateUser(userdto, user.get());
    }

    @RequestMapping(value = "/api/allListUser", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> ListUser(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return userService.ListAllUser(pageable);
        } else {
            return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/dropDownUserList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> ListUser(@RequestParam(value = "username", required = false) String username,
                                      @RequestParam(value = "machineId", required = false) Long machineId,
                                      @RequestParam(value = "escalationUserType", required = false) String escalationUserType,
                                      Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.isPresent()) {
            return new ResponseEntity<>(userService.getAllUser(username, machineId, escalationUserType), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<UserDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/allListFilterUser", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> filterMapListUser(@RequestParam(value = "branchId", required = false) Long branchId,
                                               @RequestParam(value = "employeeId", required = false) Long employeeId,
                                               @RequestParam(value = "departmentId", required = false) Long departmentId,
                                               @RequestParam(value = "employeeName", required = false) String employeeName,
                                               @RequestParam(value = "employeeCategory", required = false) String employeeType,
                                               @RequestParam(value = "location", required = false) String location,
                                               @RequestParam(value = "designationId", required = false) Long designationId,
                                               Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return userService.stafflistwfilter(employeeId, branchId, departmentId, employeeName, employeeType,
                    designationId, location);
        } else {
            return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/filterListUser", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> filterListUser(@RequestParam(value = "employeeId", required = false) Long employeeId,
                                            @RequestParam(value = "branchId", required = false) Long branchId,
                                            @RequestParam(value = "departmentId", required = false) Long departmentId,
                                            @RequestParam(value = "employeeName", required = false) String employeeName,
                                            @RequestParam(value = "employeeCategory", required = false) String employeeType,
                                            @RequestParam(value = "designationId", required = false) Long designationId,
                                            @RequestParam(value = "designation", required = false) String designation,
                                            @RequestParam(value = "status", required = false) String active,
                                            @RequestParam(value = "startDate", required = false) String startDate,
                                            @RequestParam(value = "endDate", required = false) String endDate, Pageable pageable,
                                            Authentication authentication) {

        if (pageable == null) {
            pageable = PageRequest.of(0, 500);
        }
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return userService.filterListUser(employeeId, branchId, departmentId, employeeName, employeeType,
                    designationId, designation, active, startDate, endDate, pageable);
        } else {
            return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/stafflistwfilter", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> stafflistwfilter(@RequestParam(value = "employeeId", required = false) Long employeeId,
                                              @RequestParam(value = "branchId", required = false) Long branchId,
                                              @RequestParam(value = "departmentId", required = false) Long departmentId,
                                              @RequestParam(value = "employeeName", required = false) String employeeName,
                                              @RequestParam(value = "employeeCategory", required = false) String employeeType,
                                              @RequestParam(value = "designationId", required = false) Long designationId,
                                              @RequestParam(value = "status", required = false) Integer active,
                                              @RequestParam(value = "shiftId", required = false) Long shiftId,
                                              @RequestParam(value = "sex", required = false) String sex, Pageable pageable,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return userService.stafflistwfilter(employeeId, branchId, departmentId, employeeName, employeeType,
                    designationId, active, shiftId, sex);
        } else {
            return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/userProfile/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> UserProfile(@PathVariable String id, Authentication authentication) {

        return userService.UserProfile(id);
    }

    @RequestMapping(value = "/api/deleteUserDetails/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<?> deleteUserDetails(@PathVariable String id, Authentication authentication) {

        return userService.DeleteUser(id);
    }

    @PostMapping("/api/checkUser")
    public ResponseEntity<?> checkUser(@RequestBody LoginDto loginDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return userService.checkUser(loginDto);
    }

    @RequestMapping(value = "/api/staffList", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> staffList(@RequestBody SearchDto searchDto, Pageable pageable,
                                       Authentication authentication) {

        if (pageable == null) {
            pageable = PageRequest.of(0, 500);
        }

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return userService.ListAllUser(pageable);
        } else {
            return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/dutyList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> dutyList(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        List<String> dutyList = userRepository.findAllByDesignation();
        return new ResponseEntity<List<String>>(dutyList, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/designationList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> designationList(Authentication authentication,
                                             @RequestParam(value = "designation", required = false) String designation) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        List<Designation> dutyList = null;
        if (designation != null) {
            dutyList = designationRepository.findAllByDesignation("%" + designation + "%");
        } else {
            dutyList = designationRepository.findAll();
        }
        return new ResponseEntity<List<Designation>>(dutyList, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/employeeCategoryList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> employeeCategoryList(Authentication authentication,
                                                  @RequestParam(value = "category", required = false) String category) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        List<String> dutyList = userRepository.findAllByCategory();
        return new ResponseEntity<List<String>>(dutyList, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/allRoleList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> allRoleList(Authentication authentication) {

        return roleService.getAllRoles();
    }

    @RequestMapping(value = "/api/qualificationList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> qualificationList(Authentication authentication,
                                               @RequestParam(value = "qualification", required = false) String qualification) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        List<String> qualificationList = null;
        if (qualification != null) {
            qualificationList = userRepository.findAllByQualification(qualification);
        } else {
            qualificationList = userRepository.findAllByQualification();
        }
        return new ResponseEntity<List<String>>(qualificationList, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/headerDetails", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> headerDetails(Pageable pageable, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("userDetails", user.get());
        stringObjectMap.put("branchList", commonService.listAll().getBody());
        stringObjectMap.put("organizationList", commonService.organizationListAll(pageable).getBody());
        return new ResponseEntity<Map<String, Object>>(stringObjectMap, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/changePassword", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<?> changePassword(@RequestBody PasswordDto pwd, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.isPresent()) {
            return userService.changePassword(pwd, user.get());
        } else {
            return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/api/auth/createOrg", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> createOrg(@RequestParam("orgName") String orgName, Authentication authentication) {
        organizationService.save(orgName);
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @RequestMapping(value = "/api/auth/createRole", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> createRole(@RequestParam("roleName") String roleName) {

        roleService.save(roleName);
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @PostMapping("/api/auth/saveBranch")
    public ResponseEntity<?> saveBranch(@RequestBody BranchDto branchDto) {
        commonService.saveBranch(branchDto);
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @PostMapping("/api/auth/saveDepartment")
    public ResponseEntity<?> saveDepartment(@RequestBody DepartmentDto departmentDto, Authentication authentication) {
        commonService.saveDepartment(departmentDto);
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @PostMapping("/api/auth/saveShift")
    public ResponseEntity<?> saveShift(@RequestBody ShiftDto shiftDto, Authentication authentication) {
        commonService.saveShift(shiftDto);
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @PostMapping("/api/auth/savePass")
    public ResponseEntity<?> savePass(Authentication authentication) {
        userService.savePass();
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }

    @RequestMapping(value = "/api/commonStaffFilterUser", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> commonStaffFilterUser(@RequestParam(value = "branchId") Long branchId,
                                                   @RequestParam(value = "dacatId", required = false) Long dacatId,
                                                   @RequestParam(value = "employeeId", required = false) Long employeeId,
                                                   @RequestParam(value = "departmentId", required = false) Long departmentId,
                                                   @RequestParam(value = "designationId", required = false) Long designationId,
                                                   @RequestParam(value = "employeeName", required = false) String employeeName,
                                                   @RequestParam(value = "employeeType", required = false) String employeeType,
                                                   @RequestParam(value = "weekOff", required = false) String weekOff, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return userService.commonStaffList(employeeId, dacatId, branchId, departmentId, employeeName, employeeType,
                    designationId, weekOff);
        } else {
            return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @RequestMapping(value = "/api/auth/updatePass", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> createOrg(@RequestParam("id") Long id, Authentication authentication) {
        userService.updatePass(id);
        return new ResponseEntity<String>("success", HttpStatus.OK);
    }
}
