package com.aria.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.config.JwtUtils;
import com.aria.dao.PermissionsRepository;
import com.aria.dao.ShiftAssignRepository;
import com.aria.dao.ShiftsRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.AttendanceDto;
import com.aria.dto.AttendanceTimelineDto;
import com.aria.dto.InstruLogDto;
import com.aria.dto.JwtResponse;
import com.aria.dto.LoginDto;
import com.aria.dto.SearchDto;
import com.aria.dto.updateReasonDto;
import com.aria.model.Permissions;
import com.aria.model.RefreshToken;
import com.aria.model.ShiftAssign;
import com.aria.model.Shifts;
import com.aria.model.User;
import com.aria.service.AttendanceService;
import com.aria.service.RefreshTokenService;
import com.aria.service.UserDetailsImpl;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AttendanceController {

    @Autowired
    AttendanceService attendanceService;

    @Autowired
    ShiftAssignRepository shiftAssignRepository;

    @Autowired
    ShiftsRepository shiftsRepository;

    @Autowired
    PermissionsRepository permissionsRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/saveInstruLog")
    public ResponseEntity<?> saveInstruLog(Authentication authentication, @RequestBody InstruLogDto instruLog) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.save(instruLog);
    }

    @GetMapping("/userAttendanceList")
    public ResponseEntity<?> userAttendanceList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.userAttanceListAll(user.get(), pageable);
    }

    @GetMapping("/adminAttendanceList")
    public ResponseEntity<?> adminAttendanceList(@RequestParam(value = "employeeId", required = false) Long employeeId,
                                                 @RequestParam(value = "month", required = false) String month,
                                                 @RequestParam(value = "fromDate", required = false) String fromDate,
                                                 @RequestParam(value = "toDate", required = false) String toDate,

                                                 Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.adminAttendanceList(employeeId, month, fromDate, toDate, user.get(), pageable);
    }

    @PostMapping("/auth/attendancePunch")
    public ResponseEntity<?> attendancePunch(@RequestBody AttendanceDto attendanceDto) {

        return attendanceService.updatePunch(attendanceDto);
    }

    @PutMapping("/updateOverDueToBreakTimeReason")
    public ResponseEntity<?> updateBreakTime(@RequestBody AttendanceDto attendanceDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.updateOverDueToBreakTime(attendanceDto);
    }

    @PutMapping("/updateOverTimeReason")
    public ResponseEntity<?> updateOverTimeReason(@RequestBody AttendanceDto attendanceDto,
                                                  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.updateOverTime(attendanceDto);
    }

    @PostMapping("/getAwardAttendanceUserDetails")
    public ResponseEntity<?> getAwardAttendanceUserDetails(@RequestBody SearchDto searchDto,
                                                           Authentication authentication) {

        return attendanceService.getAwardAttendanceUserDetails(searchDto);
    }

    @GetMapping("/getAttendanceInfo")
    public ResponseEntity<?> getAttendanceInfo(@RequestParam(value = "id", required = false) String id,
                                               @RequestParam(value = "date", required = false) String date, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.getAttendanceInfo(user.get(), id, date);
    }

    @PostMapping("/saveAttendanceUser")
    public ResponseEntity<?> saveAttendanceUser(@RequestBody AttendanceDto attendanceDto,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.saveAttendanceUser(attendanceDto, user.get());
    }

    @PutMapping("/updateAttendanceUser")
    public ResponseEntity<?> updateAttendanceUser(@RequestBody AttendanceDto attendanceDto,
                                                  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.updateAttendanceUser(attendanceDto, user.get());
    }

    @PutMapping("/attendanceOutTimeReason")
    public ResponseEntity<?> attendanceOutTimeReason(@RequestBody AttendanceTimelineDto attendanceTimelineDto,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.attendanceOutTimeReason(attendanceTimelineDto, user.get());
    }

    @PutMapping("/attendanceOverBreakTimeReason")
    public ResponseEntity<?> updateoverDueToBreakTimeReason(@RequestBody AttendanceDto attendanceDto,
                                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.overDueToBreakTimeReason(attendanceDto, user.get());
    }

    @PutMapping("/attendanceOverTimeReason")
    public ResponseEntity<?> updateoverTimeReason(@RequestBody AttendanceDto attendanceDto,
                                                  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return attendanceService.overTimeReason(attendanceDto, user.get());
    }

    @GetMapping("/getAttendanceUserList")
    public ResponseEntity<?> getAttendanceUser(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attendanceService.listAllAttendance();
        } else {
            return new ResponseEntity<List<AttendanceDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getBillAttendanceList")
    public ResponseEntity<?> getAttendanceList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attendanceService.listAllAttendance();
        } else {
            return new ResponseEntity<List<AttendanceDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getAttendanceOverBreakTimeReason")
    public ResponseEntity<?> overDueToBreakTimeReason(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attendanceService.listAlloverDueToBreakTimeReason();
        } else {
            return new ResponseEntity<List<AttendanceDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getAttendanceOverTimeReason")
    public ResponseEntity<?> overTimeReason(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return attendanceService.listAlloverTimeReason();
        } else {
            return new ResponseEntity<List<AttendanceDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getAttendanceUserDetails/{id}")
    public ResponseEntity<?> getAttendanceUser(@PathVariable String id, Authentication authentication) {

        return attendanceService.getAttendance(id);
    }

    @DeleteMapping("/deleteAttendanceUserDetails/{id}")
    public ResponseEntity<?> deleteAttendanceUser(@PathVariable String id, Authentication authentication) {

        return attendanceService.deleteAttendance(id);
    }

    @GetMapping("/getListAllEmployees")
    public ResponseEntity<?> getListOfAllEmployees(
            @RequestParam(value = "employeename", required = false) String employeename,
            @RequestParam(value = "fromdate", required = false) String fromdate,
            @RequestParam(value = "todate", required = false) String todate) {
        return attendanceService.getEmployeesShiftDetails(employeename, fromdate, todate);
    }

    @PostMapping("/auth/Usersignin")
    public ResponseEntity<?> authenticateUserforattendence(@Valid @RequestBody LoginDto loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long empId = userDetails.getId();
        LocalDate date = LocalDate.now();
        Long roleid = null;

        Optional<User> user = userRepository.findById(empId);
        if (user.isPresent()) {
            roleid = user.get().getRoles_id();
        }
//		System.out.println(roleid);

        if (!Integer.valueOf(22).equals(roleid)) {

            // SHIFT VALIDATION
            ShiftAssign shiftassign = shiftAssignRepository.getShifts(empId, date);
            LocalTime inTime = LocalTime.parse(shiftassign.getShifts().getIntime());
            LocalTime outTime = LocalTime.parse(shiftassign.getShifts().getOuttime());
            Long activeShiftId = null;
            LocalTime now = LocalTime.now();

            if (isWithinShift(now, inTime, outTime) && 154 == shiftassign.getShifts().getId().longValue()) {
                activeShiftId = 154l;
                System.out.println(activeShiftId);
            } else if (isWithinShift(now, inTime, outTime) && 158 == shiftassign.getShifts().getId().longValue()) {
                activeShiftId = 158l;
                System.out.println(activeShiftId);
            } else {
                activeShiftId = 150l;
            }

            System.out.println(date);
            System.out.println(shiftassign.getShifts().getId().longValue());
            Long shiftid = shiftassign.getShifts().getId().longValue();

            System.out.println(inTime);
            System.out.println(outTime);
            System.out.println(now);
            if (!shiftid.equals(activeShiftId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Shift timing mismatch. Please contact admin.");
            }
        }

        String jwt = jwtUtils.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
                .collect(Collectors.toList());

        Optional<User> userDetailsById = userRepository.findById(userDetails.getId());
        List<Permissions> permissions = null;
        if (userDetailsById.get().getRoles_id() != null) {
            int roleId = userDetailsById.get().getRoles_id().intValue();
            permissions = permissionsRepository.findByRoleId(roleId).stream().collect(Collectors.toList());
        } else {
            permissions = new ArrayList<>();
        }

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
                userDetails.getUsername(), roles, permissions));

    }

    public boolean isWithinShift(LocalTime now, LocalTime in, LocalTime out) {

        // Normal shift
        if (in.isBefore(out)) {
            return !now.isBefore(in) && !now.isAfter(out);
        }

        // Night shift (cross day)
        return !now.isBefore(in) || !now.isAfter(out);
    }

    @GetMapping("/getEmployeesDetails")
    public ResponseEntity<?> getEmployeesList(@RequestParam(value = "emptype", required = false) String emptype,
                                              @RequestParam(value = "fromdate", required = false) String fromdate,
                                              @RequestParam(value = "todate", required = false) String todate, @RequestParam(value = "empName", required = false) String empName,@RequestParam(value = "dept", required = false) Long dept, Pageable pageable) {
        return attendanceService.getEmployeeShiftsdetails(emptype, fromdate, todate, empName,dept, pageable);
    }

    @GetMapping("/getemployeelogdetails")
    public ResponseEntity<?> getEmployeeLogDetail(@RequestParam(value = "emptype", required = false) String emptype,
                                                  @RequestParam(value = "fromdate", required = true) String fromdate,
                                                  @RequestParam(value = "employeename", required = false) String employeename) {
        return attendanceService.getEmployeelogdetails(emptype, fromdate, employeename);
    }

    @GetMapping("/getEmployeesAllLogDetails")
    public ResponseEntity<?> getEmployeesAllLogDetails(@RequestParam(value = "emptype", required = false) String emptype,
                                                       @RequestParam(value = "employeename", required = false) String employeename,
                                                       @RequestParam(value = "fromdate", required = false) String fromdate,
                                                       @RequestParam(value = "todate", required = false) String todate) {
        return attendanceService.getemployeeallLogdetails(emptype, employeename, fromdate, todate);
    }

    @GetMapping("/getattendanceOverView")
    public ResponseEntity<?> getattendanceOverview(
            @RequestParam(value = "emptype", required = false) String emptype,
            @RequestParam(value = "employeename", required = false) String employeename,
            @RequestParam(value = "fromdate", required = false) String fromdate) {
        return attendanceService.getempolyeeOverview(emptype, employeename, fromdate);
    }

    @PutMapping("/updateReason")
    public ResponseEntity<?> updateReason(@RequestBody updateReasonDto request, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return attendanceService.updateReason(request, user.get());
    }


    @PutMapping("/updateInstruLog")
    public ResponseEntity<?> updateInstruLog(@RequestBody InstruLogDto request, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return attendanceService.updateInstruLog(request, user.get());
    }



}


