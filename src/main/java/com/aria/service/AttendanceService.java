package com.aria.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dao.AttendanceRepository;
import com.aria.dao.AttendanceTimelineRepository;
import com.aria.dao.BiomastRepository;
import com.aria.dao.CommonDao;
import com.aria.dao.DailyWagesAttendanceRepository;
import com.aria.dao.DailyWagesAttendanceTimelineRepository;
import com.aria.dao.DwlogRepository;
import com.aria.dao.FingerPrintRepository;
import com.aria.dao.InstruLogRepository;
import com.aria.dao.LeaveDetailsRepository;
import com.aria.dao.ShiftAssignRepository;
import com.aria.dao.TrainingUserRegistrationMappingRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.AttendanceDto;
import com.aria.dto.AttendanceTimelineDto;
import com.aria.dto.AttendanceTimelineList;
import com.aria.dto.AttendanceTimelineListDto;
import com.aria.dto.AttendanceTimelineProjection;
import com.aria.dto.AttendanceTimelineResDto;
import com.aria.dto.EmployeeDailyLogDto;
import com.aria.dto.EmployeeDailyLogList;
import com.aria.dto.EmployeeOverViewDto;
import com.aria.dto.EmployeeOverViewList;
import com.aria.dto.EmployeeShiftDto;
import com.aria.dto.EmployeeShiftProjection;
import com.aria.dto.EmployeeShiftResponseDto;
import com.aria.dto.InstruLogDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.ReasonDto;
import com.aria.dto.SearchDto;
import com.aria.dto.UserAttendanceReport;
import com.aria.dto.updateReasonDto;
import com.aria.model.Attendance;
import com.aria.model.AttendanceTimeline;
import com.aria.model.Biomast;
import com.aria.model.DailyWagesAttendanceTimeline;
import com.aria.model.FingerPrint;
import com.aria.model.InstruLog;
import com.aria.model.LeaveDetails;
import com.aria.model.ShiftAssign;
import com.aria.model.Status;
import com.aria.model.TrainingUserRegistrationMapping;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class AttendanceService {

    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(AttendanceService.class);

    @Autowired
    protected AttendanceRepository attendanceRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FingerPrintRepository fingerPrintRepository;

    @Autowired
    InstruLogRepository instruLogRepository;

    @Autowired
    CommonDao commonDao;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    LeaveDetailsRepository leaveDetailsRepository;

    @Autowired
    ShiftAssignRepository shiftAssignRepository;

    @Autowired
    BiomastRepository biomastRepository;

    @Autowired
    TrainingUserRegistrationMappingRepository trainingUserRegistrationMappingRepository;

    @Autowired
    AttendanceTimelineRepository attendanceTimelineRepository;

    @Autowired
    private DwlogRepository dwlogRepository;

    @Autowired
    private DailyWagesAttendanceRepository wagesAttendanceRepository;

    @Autowired
    private DailyWagesAttendanceTimelineRepository wagesAttendanceTimelineRepository;


    public ResponseEntity<?> save(Date fromDate, Date toDate, Long userId) {
        List<Date> dates = getDatesBetweenDays(fromDate, toDate);
        List<Attendance> newInstruLogList = new LinkedList<>();
        for (Date date : dates) {
            Attendance newInstruLog = new Attendance();
            newInstruLog.setDate(date);
            newInstruLog.setAtype("onDuty");
            if (userId != null) {
                User user = new User();
                user.setId(userId);
                newInstruLog.setUser(user);
            }
            newInstruLogList.add(newInstruLog);
        }
        attendanceRepository.saveAll(newInstruLogList);
        return new ResponseEntity<Status>(new Status("200", "Attendance updated successfully!!!"), HttpStatus.OK);
    }

    // @Transactional
    public ResponseEntity<?> save(InstruLogDto instruLogDto) {

        logger.info("Saving InstruLog: {}", instruLogDto);
        try {

            if (instruLogDto.getBioId() == null) {
                logger.error("BioId is null");
                return new ResponseEntity<Status>(new Status("400", "BioId is null"), HttpStatus.BAD_REQUEST);
            }

            Optional<InstruLog> instruLogPreviousOptional = Optional.empty();
            instruLogPreviousOptional = instruLogRepository.findLatestByBioId(instruLogDto.getBioId());

            InstruLog instruLog = new InstruLog(instruLogDto);

            instruLog.setAttendanceTime(dateUtil.getISTDate());
            instruLog.setDate(instruLog.getAttendanceTime());
            instruLog.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            InstruLog result = instruLogRepository.save(instruLog);

            if (instruLogPreviousOptional.isPresent()) {
                if (((((result.getAttendanceTime().getTime()
                        - instruLogPreviousOptional.get().getAttendanceTime().getTime()) / 1000) * 60) <= 1L)) {
                    throw new RuntimeException(
                            "400, consecutive instrulog interval of an user must be greater than 1 min");
                }
            }

            Optional<Biomast> biomastOptional = Optional.empty();
            if ((result.getSysIp() != null) && (!result.getSysIp().isEmpty())) {
                biomastOptional = biomastRepository.findByMachineIp(result.getSysIp());
            } else {
                throw new RuntimeException("400, SysIp is null");
            }

            if (!biomastOptional.isPresent()) {
                throw new RuntimeException("400, biomastOptional is null");
            }

            updateAttendance(result, biomastOptional);

            if (biomastOptional.get().getLocation().equals("P1-TrainingHall")) {
                updateTrainingAttendance(result, biomastOptional);
            }

            return new ResponseEntity<Status>(new Status("200", "Attendance updated successfully!!!"), HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error in saving InstruLog: ", e);
            return new ResponseEntity<Status>(new Status("400", e.getMessage()), HttpStatus.BAD_REQUEST);
        }

    }

    public void updateAttendance(InstruLog attendanceDto, Optional<Biomast> biomastOptional) {

        Attendance attendance = new Attendance();

        // attendance.setDate(dateUtil.getLocalDateTimeToDate(
        // dateUtil.getDateToLocalDateTime(attendanceDto.getDate()).with(LocalTime.MIN)));

        attendance.setDate(attendanceDto.getDate());

        Optional<User> user = userRepository.findByBioIdWithActive(attendanceDto.getBioId());

        if (!user.isPresent()) {
            throw new RuntimeException("Bio Id Not Match!...");
        }

        if (biomastOptional.get().getLocation().equals("P1-GATE")) {

            Optional<User> userOptional = userRepository.checkAttendance(user.get().getId(), user.get().getBranch_id(),
                    user.get().getDepartment().getId(), attendanceDto.getDate());

            if (!userOptional.isPresent()) {
                throw new RuntimeException("Shift not assigned!...");
            }
        }

        if (biomastOptional.get().getLocation().equals("P1-TrainingHall")) {
            String status = "Registered";
            Optional<TrainingUserRegistrationMapping> trainingUserRegistrationMappingOptional = trainingUserRegistrationMappingRepository
                    .findByUserIdANDDateANDStatus(user.get().getId(), attendanceDto.getDate(), status);

            if (!trainingUserRegistrationMappingOptional.isPresent()) {
                throw new RuntimeException("Not registered for this training!...");
            }
        }

        Optional<Attendance> attendanceOptional = attendanceRepository.findByUserAndDate(user.get().getId(),
                attendanceDto.getDate());

        Attendance resultAttendance = null;
        if (!attendanceOptional.isPresent()) {
            attendance.setUser(user.get());
            attendance.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            resultAttendance = attendanceRepository.save(attendance);
            if (resultAttendance == null) {
                throw new RuntimeException("resultAttendance is null!...");
            }
        } else {
            resultAttendance = attendanceOptional.get();
        }

        Optional<AttendanceTimeline> attendanceTimelineLatestOptional = Optional.empty();
        if (attendanceOptional.isPresent()) {
            attendanceTimelineLatestOptional = attendanceTimelineRepository
                    .findLatestByAttendanceId(attendanceOptional.get().getId());
        } else {
            attendanceTimelineLatestOptional = attendanceTimelineRepository
                    .findLatestByAttendanceId(resultAttendance.getId());
        }

        if (!attendanceTimelineLatestOptional.isPresent()) {

            AttendanceTimeline attendanceTimeline = new AttendanceTimeline();
            attendanceTimeline.setAttendanceId(resultAttendance.getId());
            if (resultAttendance.getUser() != null) {
                attendanceTimeline.setUserId(resultAttendance.getUser().getId());
            }
            attendanceTimeline.setInstrulogId(attendanceDto.getId());
            if (biomastOptional.isPresent()) {
                attendanceTimeline.setBiomast(biomastOptional.get());
            }
            attendanceTimeline.setAttendanceDate(attendanceDto.getDate());
            attendanceTimeline.setInTime(attendanceDto.getAttendanceTime());
            attendanceTimeline.setAttendanceTime(attendanceDto.getAttendanceTime());
            // attendanceTimeline.setDuration(null);
            // attendanceTimeline.setIsReasonNeeded(null);
            // attendanceTimeline.setReason(null);
            attendanceTimeline.setCreatedBy(resultAttendance.getUser().getId());
            attendanceTimeline.setCreated_date(dateUtil.getISTDate());
            attendanceTimelineRepository.save(attendanceTimeline);
        } else {

            if (attendanceTimelineLatestOptional.get().getBiomast() == null) {
                throw new RuntimeException("Biomast is null!...");
            }

            if (attendanceTimelineLatestOptional.get().getOutTime() == null) {
                // insert new checkout
                AttendanceTimeline attendanceTimeline = new AttendanceTimeline();
                attendanceTimeline.setAttendanceId(resultAttendance.getId());
                if (resultAttendance.getUser() != null) {
                    attendanceTimeline.setUserId(resultAttendance.getUser().getId());
                }
                attendanceTimeline.setInstrulogId(attendanceDto.getId());
                if (biomastOptional.isPresent()) {
                    attendanceTimeline.setBiomast(biomastOptional.get());
                }
                attendanceTimeline.setAttendanceDate(attendanceDto.getDate());
                attendanceTimeline.setAttendanceTime(attendanceDto.getAttendanceTime());
                attendanceTimeline.setOutTime(attendanceDto.getAttendanceTime());
                attendanceTimeline.setCreatedBy(resultAttendance.getUser().getId());
                attendanceTimeline.setCreated_date(dateUtil.getISTDate());
                attendanceTimelineRepository.save(attendanceTimeline);
            }

            // insert new checkin
            else {
                AttendanceTimeline attendanceTimeline = new AttendanceTimeline();
                attendanceTimeline.setAttendanceId(resultAttendance.getId());
                if (resultAttendance.getUser() != null) {
                    attendanceTimeline.setUserId(resultAttendance.getUser().getId());
                }
                attendanceTimeline.setInstrulogId(attendanceDto.getId());
                if (biomastOptional.isPresent()) {
                    attendanceTimeline.setBiomast(biomastOptional.get());
                }
                attendanceTimeline.setAttendanceDate(attendanceDto.getDate());
                attendanceTimeline.setAttendanceTime(attendanceDto.getAttendanceTime());
                attendanceTimeline.setInTime(attendanceDto.getAttendanceTime());
                attendanceTimeline.setCreatedBy(resultAttendance.getUser().getId());
                attendanceTimeline.setCreated_date(dateUtil.getISTDate());
                attendanceTimelineRepository.save(attendanceTimeline);
            }
        }
    }

    public void updateTrainingAttendance(InstruLog instruLog, Optional<Biomast> biomastOptional) {
        Optional<User> userOptional = userRepository.findByBioId(instruLog.getBioId());

        if (!userOptional.isPresent()) {
            throw new RuntimeException("Bio Id Not Matched!...");
        }

        if (instruLog.getDate() == null) {
            throw new RuntimeException("instruLog's Date is null");
        }

        String status = "Registered";
        // Optional<TrainingUserRegistrationMapping>
        // trainingUserRegistrationMappingOptional =
        // trainingUserRegistrationMappingRepository
        // .findByUserIdANDDateANDStatus(userOptional.get().getId(),
        // instruLog.getDate(), status);

        // Date instrulogDate = dateUtil
        // .getLocalDateTimeToDate(dateUtil.getDateToLocalDateTime(instruLog.getDate()).with(LocalTime.MIN));

        Date instrulogDate = instruLog.getDate();

        Optional<TrainingUserRegistrationMapping> trainingUserRegistrationMappingOptional = trainingUserRegistrationMappingRepository
                .findByUserIdANDDateANDStatus(userOptional.get().getId(), instrulogDate, status);

        if (trainingUserRegistrationMappingOptional.isPresent()) {
            if (trainingUserRegistrationMappingOptional.get().getAttendanceStatus() == null) {
                trainingUserRegistrationMappingOptional.get().setAttendanceStatus("Present");
                trainingUserRegistrationMappingRepository.save(trainingUserRegistrationMappingOptional.get());
            } else if (!trainingUserRegistrationMappingOptional.get().getAttendanceStatus().equals("Present")) {
                trainingUserRegistrationMappingOptional.get().setAttendanceStatus("Present");
                trainingUserRegistrationMappingRepository.save(trainingUserRegistrationMappingOptional.get());
            }
        } else {
            throw new RuntimeException("Not registered for this training");
        }
    }

    public ResponseEntity<?> adminAttendanceList(Long empId, String month, String fromDate, String toDate,
                                                 User loggedUser, Pageable pageable) {
        List<Map<String, Object>> mapList = new LinkedList<>();
        Page<User> userList = null;
        if (empId != null) {
            userList = userRepository.findById(empId, pageable);
        } else {
            userList = userRepository.findAll(pageable);
        }
        if (userList != null) {
            for (User user : userList.toList()) {
                Map<String, Object> stringObjectMap = new LinkedHashMap<>();
                stringObjectMap.put("id", user.getId());
                stringObjectMap.put("employeeName", user.getEmployeename());
                List<Date> dateList = null;
                if (fromDate != null && toDate != null) {
                    Date startDate = dateUtil.getStringTODateDDMMYYYY(fromDate);
                    Date endDate = dateUtil.getStringTODateDDMMYYYY(toDate);
                    dateList = dateUtil.getListOfDatesBetweenTwoDate(startDate, endDate);
                } else {
                    Date startDate = dateUtil.getStartDate();
                    Date endDate = dateUtil.getEndDate();
                    dateList = dateUtil.getListOfDatesBetweenTwoDate(startDate, endDate);
                }
                List<Map<String, String>> dates = new ArrayList<>();
                for (Date date : dateList) {
                    Date checkCurrentDate = dateUtil.getISTDate();
                    int intValue = date.compareTo(checkCurrentDate);
                    if (intValue == 1) {
                        break;
                    }
                    Map<String, String> stringMap = new HashMap<>();
                    Optional<Attendance> attendance = attendanceRepository.findByUserAndDate(user.getId(), date);
                    if (attendance.isPresent()) {
                        stringMap.put("date", dateUtil.getStringTODate(attendance.get().getDate()));
                        if (attendance.get().getAtype().equalsIgnoreCase("onDuty")) {
                            stringMap.put("type", "OD");
                        } else {
                            stringMap.put("type", "P");
                        }
                    } else {
                        Optional<LeaveDetails> leaveDetails = leaveDetailsRepository.checkLeave(user.getId(), date);
                        if (leaveDetails.isPresent()) {
                            stringMap.put("date", dateUtil.getStringTODate(date));
                            stringMap.put("type", "L");
                        } else {
                            List<ShiftAssign> shiftAssign = shiftAssignRepository.checkWeekOff(user.getId(),
                                    user.getBranch_id(), user.getDepartment().getId(), date);
                            if (!shiftAssign.isEmpty()) {
                                stringMap.put("date", dateUtil.getStringTODate(date));
                                int i = dateUtil.getDayName(date);
                                if (i == shiftAssign.get(0).getWeek_day_id()) {
                                    stringMap.put("type", "WK");
                                }
                            }
                        }
                    }
                    dates.add(stringMap);
                }
                stringObjectMap.put("attendanceList", dates);
                mapList.add(stringObjectMap);
            }
        }
        return new ResponseEntity<List<Map<String, Object>>>(mapList, HttpStatus.OK);
    }

    public ResponseEntity<?> userAttanceListAll(User loggedUser, Pageable pageable) throws UsernameNotFoundException {

        Page<Attendance> attendancesPage = attendanceRepository.findAll(pageable);
        List<UserAttendanceReport> userAttendanceReports = new ArrayList<>();

        // HashMap<String, Object> attendanceReport = new HashMap<>();

        // List<AttendanceDto> attendanceDtos = new ArrayList<>();
        //
        // for (Attendance attendance : attendancesPage.toList()) {
        // AttendanceDto attendanceDto = new AttendanceDto(attendance);
        // attendanceDtos.add(attendanceDto);
        // }

        for (Attendance attendance : attendancesPage.toList()) {
            HashMap<String, Object> attendanceReport = new HashMap<>();
            // for(AttendanceTimeline attendanceTimeline : attendanceTimelines) {

            int productionHours = 0, OD = 0, outDuration = 0;

            Boolean punchInCalculated = Boolean.FALSE;
            Date punchIn = null, punchOut = null;
            List<AttendanceTimeline> attendanceTimelines = attendanceTimelineRepository
                    .findByAttendanceIdSortByAttendanceTime(attendance.getId());

            if (attendanceTimelines != null && !attendanceTimelines.isEmpty()) {
                for (int i = 0; i <= (attendanceTimelines.size() - 1); i++) {

                    // calculate punchIn time
                    if (!punchInCalculated && attendanceTimelines.get(i).getInTime() != null
                            && attendanceTimelines.get(i).getBiomast().getLocation().equals("P1-GATE")) {
                        punchIn = attendanceTimelines.get(i).getInTime();
                        punchInCalculated = Boolean.TRUE;
                    }

                    // calculate production hours, OD & out duration
                    if (attendanceTimelines.get(i).getOutTime() != null) {
                        long j = ((attendanceTimelines.get(i).getOutTime().getTime())
                                - (attendanceTimelines.get(i - 1).getInTime().getTime()));

                        Date durartion = new Date(j);
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String durartion1 = sdf.format(durartion);
                        attendanceTimelines.get(i).setDuration(durartion1);

                        // calculate production hours
                        if (attendanceTimelines.get(i).getBiomast().getLocation().equals("P1-GATE")) {
                            attendanceTimelines.get(i).setIsReasonNeeded(Boolean.FALSE);
                            productionHours += j;
                        }

                        // check reason & calculate OD & out duration

                        long minutes = (j / 1000) / 60;

                        if (minutes > 5) {
                            // attendanceDto.getAttendanceTimelineDtos().get(i).setIsReasonNeeded(Boolean.TRUE);

                            if (attendanceTimelines.get(i).getBiomast().getLocation().equals("P1-TrainingHall")) {
                                String attendanceStatus = "Present";
                                Optional<TrainingUserRegistrationMapping> trainingUserRegistrationMappingOptional = trainingUserRegistrationMappingRepository
                                        .findByUserIdANDDateANDAttandanceStatus(attendance.getUser().getId(),
                                                attendance.getDate(), attendanceStatus);
                                if (!trainingUserRegistrationMappingOptional.isPresent()) {
                                    attendanceTimelines.get(i).setIsReasonNeeded(Boolean.TRUE);
                                    outDuration += j;
                                } else {
                                    attendanceTimelines.get(i).setIsReasonNeeded(Boolean.FALSE);
                                    OD += j;
                                }
                            }
                        }
                    }

                    // calculate out duration
                    if (i >= 2) {
                        long j = 0;
                        if (attendanceTimelines.get(i).getInTime() != null) {
                            j = attendanceTimelines.get(i).getInTime().getTime()
                                    - attendanceTimelines.get(i - 1).getOutTime().getTime();
                            outDuration += j;
                        }

                        Date durartion = new Date(j);
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                        String durartion1 = sdf.format(durartion);
                        attendanceTimelines.get(i).setDuration(durartion1);
                    }
                }
            }

            // punchOut
            Optional<AttendanceTimeline> attendanceTimelineOptional = attendanceTimelineRepository
                    .findLatestByAttendanceIdAndLocation(attendance.getId(), "P1-GATE");
            if (attendanceTimelineOptional.isPresent()) {
                punchOut = attendanceTimelineOptional.get().getOutTime();
            }

            AttendanceDto attendanceDto = new AttendanceDto(attendance);
            attendanceDto.setDate(dateUtil.getDateToStringDDMMYYYY(attendance.getDate()));

            // LocalDateTime date =
            // LocalDateTime.ofInstant(Instant.ofEpochMilli(productionHours),
            // ZoneId.systemDefault());
            // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd
            // HH:mm:ss");
            // String formatDateTime1 = date.format(formatter);
            // String formatDateTime2 =
            // dateUtil.getDateToLocalDateTime(productionHours1).format(formatter);

            Date productionHours1 = new Date(productionHours);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String productionHours2 = sdf.format(productionHours1);

            Date OD1 = new Date(OD);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String OD2 = sdf.format(OD1);

            Date outDuration1 = new Date(outDuration);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String outDuration2 = sdf.format(outDuration1);

            //// Date productionHours1 = new Date(productionHours);
            // SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            //// sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
            // String punchIn1 = sdf1.format(punchIn);
            //
            // SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            //// sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
            // String punchOut1 = sdf2.format(punchOut);

            attendanceReport.put("actualProductionHours", productionHours2);
            attendanceReport.put("OD", OD2);
            attendanceReport.put("productionHours", "08:00:00");
            attendanceReport.put("outDuration", outDuration2);
            attendanceReport.put("punchIn", punchIn);
            attendanceReport.put("punchOut", punchOut);

            UserAttendanceReport userAttendanceReport = new UserAttendanceReport();

            List<AttendanceTimelineDto> attendanceTimelineDtos = attendanceTimelines.stream().map(data -> {
                AttendanceTimelineDto attendanceTimelineDto = new AttendanceTimelineDto(data);
                return attendanceTimelineDto;
            }).collect(Collectors.toList());

            attendanceDto.setAttendanceTimelineDtos(attendanceTimelineDtos);

            userAttendanceReport.setAttendanceDto(attendanceDto);
            userAttendanceReport.setAttendanceReport(attendanceReport);
            userAttendanceReports.add(userAttendanceReport);
        }

        return new ResponseEntity<List<UserAttendanceReport>>(userAttendanceReports, HttpStatus.OK);
    }

    public ResponseEntity<?> updatePunch(AttendanceDto attendanceDto) {

        Optional<FingerPrint> optional = fingerPrintRepository.findBySecureKey(attendanceDto.getSecureKey());
        if (optional.isPresent()) {
            Optional<Attendance> optionalAttendance = attendanceRepository.findByUserAndDate(optional.get().getUserId(),
                    dateUtil.getISTDate());
            if (optionalAttendance.isPresent()) {
                Attendance attendance = optionalAttendance.get();
                if (attendance.getPunchin() == null) {
                    attendance.setPunchin(dateUtil.getISTDate());
                    attendance.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    attendanceRepository.save(attendance);
                } else if (attendance.getBreakin() == null) {
                    attendance.setBreakin(dateUtil.getISTDate());
                    attendance.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    attendanceRepository.save(attendance);
                } else if (attendance.getBreakout() == null) {
                    attendance.setBreakout(dateUtil.getISTDate());
                    attendance.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    attendanceRepository.save(attendance);
                } else {
                    attendance.setBreak(getStringValues(attendance.getBreakin(), attendance.getBreakout()));
                    attendance.setPunchout(dateUtil.getISTDate());
                    attendance.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    attendance.setOverTime(getStringOverTimeValues(attendance.getPunchin(), attendance.getPunchout()));
                    attendanceRepository.save(attendance);
                }

            } else {
                Attendance attendance = new Attendance();
                User user = new User();
                user.setId(optional.get().getUserId());
                attendance.setUser(user);
                attendance.setDate(dateUtil.getISTDate());
                attendance.setProduction("8");
                attendance.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                attendance.setPunchin(dateUtil.getISTDate());
                attendanceRepository.save(attendance);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "Attendance updated successfully!!!"), HttpStatus.OK);
    }

    public String getStringValues(Date d1, Date d2) {
        String dateValues = null;
        Map<String, Long> stringLongMap = dateUtil.findDifference(d1, d2);

        if (stringLongMap.containsKey("H")) {

            dateValues = stringLongMap.get("H") + ":" + stringLongMap.get("M") + "Hrs";
        } else {
            dateValues = "00:" + stringLongMap.get("M") + "Hrs";
        }

        return dateValues;
    }

    public String getStringOverTimeValues(Date d1, Date d2) {
        Map<String, Long> stringLongMap = dateUtil.findDifference(d1, d2);
        Long hour = stringLongMap.get("H");
        Long minutes = stringLongMap.get("M");
        long h = 8;
        long r;
        if (hour > 8) {
            r = h - hour;
            return r + ":" + minutes;
        } else if (hour == 8 && minutes > 30) {
            r = minutes;
            return "0:" + minutes;
        }
        return "0:00";
    }

    /*
     * public ResponseEntity<?> getAttendanceInfo(User loggedUser, String id, String
     * date) throws UsernameNotFoundException { Optional<Attendance>
     * optionalAttendance = null; if (date != null && id != null) {
     * optionalAttendance = attendanceRepository.findByUserAndDate(Long.valueOf(id),
     * dateUtil.getStringTODateF1(date)); } else if (date != null) {
     * optionalAttendance =
     * attendanceRepository.findByUserAndDate(loggedUser.getId(),
     * dateUtil.getStringTODateF1(date)); } else { optionalAttendance =
     * attendanceRepository.findByUserAndDate(loggedUser.getId(),
     * dateUtil.getISTDate()); } AttendanceDto attendanceDto = new
     * AttendanceDto(optionalAttendance.get());
     * attendanceDto.setDate(dateUtil.getStringTODate(optionalAttendance.get().
     * getDate()));
     * attendanceDto.setDate(dateUtil.getStringTODate(optionalAttendance.get().
     * getDate()));
     * attendanceDto.setPunchIn(dateUtil.getTime(optionalAttendance.get().getPunchin
     * ())); attendanceDto.setPunchOut(dateUtil.getTime(optionalAttendance.get().
     * getPunchout()));
     * attendanceDto.setBreakIn(dateUtil.getTime(optionalAttendance.get().getBreakin
     * ())); attendanceDto.setBreakOut(dateUtil.getTime(optionalAttendance.get().
     * getBreakout())); if (optionalAttendance.get().getPunchin() != null) {
     * attendanceDto.setPunchInAt(optionalAttendance.get().getPunchin()); } if
     * (optionalAttendance.get().getPunchout() != null) {
     * attendanceDto.setPunchOutAt(optionalAttendance.get().getPunchout()); }
     * attendanceDto.setStatistic(getStatistic(attendanceDto.getUserId())); return
     * new ResponseEntity<AttendanceDto>(attendanceDto, HttpStatus.OK); }
     */

    public ResponseEntity<?> getAttendanceInfo(User loggedUser, String id, String date)
            throws UsernameNotFoundException {
        Optional<Attendance> optionalAttendance = null;
        List<InstruLog> inStruLogList = null;
        if (date != null && id != null) {
            // optionalAttendance = attendanceRepository.findByUserAndDate(Long.valueOf(id),
            // dateUtil.getStringTODateF1(date));
            Optional<User> userOptional = userRepository.findById(Long.valueOf(id));
            if (userOptional.isPresent())
                inStruLogList = instruLogRepository.findAllByUser(userOptional.get().getBioid(),
                        dateUtil.getStringTODateF1(date));
        } else if (date != null) {
            // optionalAttendance =
            // attendanceRepository.findByUserAndDate(loggedUser.getId(),
            // dateUtil.getStringTODateF1(date));
            inStruLogList = instruLogRepository.findAllByUser(loggedUser.getBioid(), dateUtil.getStringTODateWk(date));
        } else {
            inStruLogList = instruLogRepository.findAllByUser(loggedUser.getBioid(), dateUtil.getISTDate());
            // optionalAttendance =
            // attendanceRepository.findByUserAndDate(loggedUser.getId(),
            // dateUtil.getISTDate());
        }
        AttendanceDto attendanceDto = new AttendanceDto();
        if (!inStruLogList.isEmpty()) {

            Optional<User> user = userRepository.findByBioId(inStruLogList.get(0).getBioId());
            if (user.isPresent()) {
                attendanceDto.setUserName(user.get().getEmployeename());
                attendanceDto.setUserId(user.get().getId());
            }
            attendanceDto.setDate(dateUtil.getStringTODate(inStruLogList.get(0).getDate()));
            int iValue = inStruLogList.size();
            Date breakIn = null;
            Date breakOut = null;
            Date punchIn = null;
            Date punchOut = null;
            for (int isValue = 0; isValue <= iValue - 1; isValue++) {

                if (isValue == 0) {
                    if (inStruLogList.get(0).getAttendanceTime() != null) {
                        attendanceDto.setPunchIn(dateUtil.getTime(inStruLogList.get(0).getAttendanceTime()));
                        attendanceDto.setPunchInAt(inStruLogList.get(0).getAttendanceTime());
                        punchIn = inStruLogList.get(0).getAttendanceTime();
                    }

                } else if (isValue == 1) {
                    if (inStruLogList.get(1).getAttendanceTime() != null) {
                        attendanceDto.setBreakIn(dateUtil.getTime(inStruLogList.get(1).getAttendanceTime()));
                        breakIn = inStruLogList.get(1).getAttendanceTime();
                    }
                } else if (isValue == 2) {
                    if (inStruLogList.get(2).getAttendanceTime() != null) {
                        attendanceDto.setBreakOut(dateUtil.getTime(inStruLogList.get(2).getAttendanceTime()));
                        breakOut = inStruLogList.get(2).getAttendanceTime();
                    }
                } else if (isValue == 3) {
                    if (inStruLogList.get(3).getAttendanceTime() != null) {
                        attendanceDto.setPunchOut(dateUtil.getTime(inStruLogList.get(3).getAttendanceTime()));
                        attendanceDto.setPunchOutAt(inStruLogList.get(3).getAttendanceTime());
                        punchOut = inStruLogList.get(3).getAttendanceTime();
                    }

                }
            }
            attendanceDto.setProduction("8 Hrs");
            attendanceDto.setBreak(getStringValues(breakIn, breakOut));
            attendanceDto.setOverTime(getStringOverTimeValues(punchIn, punchOut));

            attendanceDto.setStatistic(getStatistic(inStruLogList.get(0).getBioId()));
        }
        return new ResponseEntity<AttendanceDto>(attendanceDto, HttpStatus.OK);
    }

    public Map<String, Object> getStatistic(Long bioId) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        // Optional<Attendance> attendance =
        // attendanceRepository.findByUserAndDate(userId, dateUtil.getISTDate());
        List<InstruLog> inStruLogList = instruLogRepository.findAllByUser(bioId, dateUtil.getISTDate());
        if (!inStruLogList.isEmpty()) {
            int iValue = inStruLogList.size();
            Date punchIn = null;
            Date punchOut = null;
            for (int isValue = 0; isValue <= iValue - 1; isValue++) {
                if (isValue == 0) {
                    punchIn = inStruLogList.get(0).getAttendanceTime();
                } else if (isValue == 3) {
                    punchOut = inStruLogList.get(3).getAttendanceTime();
                }
            }

            if (punchOut != null) {
                stringObjectMap.put("today", getTodayWorkTime(punchIn, punchOut));
            } else {
                stringObjectMap.put("today", getTodayWorkTime(punchIn, dateUtil.getISTDate()));
            }
        }

        List<Long> week = new ArrayList<>();
        /*
         * List<Attendance> weekAttendanceList =
         * attendanceRepository.findAllDatesByUser(userId, dateUtil.getStartDate(),
         * dateUtil.getEndDate());
         */
        List<Map<String, Object>> mapList = commonDao.getLogWithDate(bioId, dateUtil.getStartDate(),
                dateUtil.getEndDate());

        for (Map<String, Object> instrulogmap : mapList) {
            List<InstruLog> instruLogs = instruLogRepository.findAllByUser(bioId,
                    dateUtil.getStringTODateYYYYMMDD(instrulogmap.get("date").toString()));
            if (!instruLogs.isEmpty()) {
                int iValue = instruLogs.size();
                Date punchIn = null;
                Date punchOut = null;
                for (int isValue = 0; isValue <= iValue - 1; isValue++) {
                    if (isValue == 0) {
                        punchIn = inStruLogList.get(0).getAttendanceTime();
                    } else if (isValue == 3) {
                        punchOut = inStruLogList.get(3).getAttendanceTime();
                    }
                }

                if (punchIn != null && punchOut != null) {
                    week.add(getPerDayWorkTime(punchIn, punchOut));
                }
            }

        }
        int weekSum = 0;
        for (Long i : week) {
            weekSum += i.intValue();
        }
        stringObjectMap.put("week", weekSum + " / 48");
        List<Long> month = new ArrayList<>();
        /*
         * List<Attendance> attendanceList =
         * attendanceRepository.findAllDatesByUser(userId, dateUtil.getStartDate(),
         * dateUtil.getEndDate()); for (Attendance attendance1 : attendanceList) { if
         * (attendance1.getPunchin() != null && attendance1.getPunchout() != null) {
         * month.add(getPerDayWorkTime(attendance.get().getPunchin(),
         * attendance.get().getPunchout())); } }
         */
        List<Map<String, Object>> mapMonthList = commonDao.getLogWithDate(bioId, dateUtil.getStartDate(),
                dateUtil.getEndDate());

        for (Map<String, Object> instrulogMonthMap : mapMonthList) {
            List<InstruLog> instruLogs = instruLogRepository.findAllByUser(bioId,
                    dateUtil.getStringTODateYYYYMMDD(instrulogMonthMap.get("date").toString()));
            if (!instruLogs.isEmpty()) {
                int iValue = instruLogs.size();
                Date punchIn = null;
                Date punchOut = null;
                for (int isValue = 0; isValue <= iValue - 1; isValue++) {
                    if (isValue == 0) {
                        punchIn = inStruLogList.get(0).getAttendanceTime();
                    } else if (isValue == 3) {
                        punchOut = inStruLogList.get(3).getAttendanceTime();
                    }
                }

                if (punchIn != null && punchOut != null) {
                    // week.add(getPerDayWorkTime(punchIn, punchOut));
                    month.add(getPerDayWorkTime(punchIn, punchOut));
                }
            }

        }
        int monthSum = 0;
        for (Long i : month) {
            monthSum += i.intValue();
        }
        stringObjectMap.put("month", monthSum + " / 120");
        // stringObjectMap.put("overTime",attendanceRepository.sumOfOverTimeUser(userId,
        // dateUtil.getStartDate(), dateUtil.getEndDate()));
        stringObjectMap.put("overTime", "0");
        return stringObjectMap;
    }

    public String getTodayWorkTime(Date d1, Date d2) {
        String dateValues = null;
        System.out.println(d1);
        System.out.println(d2);
        Map<String, Long> stringLongMap = dateUtil.findDifference(d1, d2);

        if (stringLongMap.containsKey("H")) {

            dateValues = stringLongMap.get("H") + " / 8";
        } else {
            dateValues = 0 + " / 8";
        }
        return dateValues;
    }

    public Long getPerDayWorkTime(Date d1, Date d2) {
        Map<String, Long> stringLongMap = dateUtil.findDifference(d1, d2);

        if (stringLongMap.containsKey("H")) {

            return stringLongMap.get("H");
        } else {
            return Long.valueOf(0);
        }
    }

    public ResponseEntity<?> userListAll(User loggedUser) throws UsernameNotFoundException {

        // List<InstruLog> list =
        // instruLogRepository.findAllByUser(loggedUser.getBioid());
        List<Attendance> list = attendanceRepository.findAllByUser(loggedUser.getBioid());
        if (!list.isEmpty()) {
            List<AttendanceDto> result = list.stream().map(data -> {
                AttendanceDto attendanceDto = new AttendanceDto(data);
                attendanceDto.setDate(dateUtil.getStringTODate(data.getDate()));
                attendanceDto.setPunchIn(dateUtil.getTime(data.getPunchin()));
                attendanceDto.setPunchOut(dateUtil.getTime(data.getPunchout()));
                attendanceDto.setBreakIn(dateUtil.getTime(data.getBreakin()));
                attendanceDto.setBreakOut(dateUtil.getTime(data.getBreakout()));
                return attendanceDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<AttendanceDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<AttendanceDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getAwardAttendanceUserDetails(SearchDto searchDto) throws UsernameNotFoundException {
        List<Map<String, Object>> mapList = new ArrayList<>();
        String sDate1 = "01 " + searchDto.getMonth();
        String sDate2 = "30 " + searchDto.getMonth();
        Date date1 = dateUtil.getStringTODateF2(sDate1);
        Date date2 = dateUtil.getStringTODateF2(sDate2);
        List<Attendance> attendanceList = attendanceRepository.findAllDatesByUser(Long.parseLong(searchDto.getEmpId()),
                date1, date2);
        if (!attendanceList.isEmpty()) {
            attendanceList.stream().map(attendance -> {
                AttendanceDto attendanceDto = new AttendanceDto(attendance);
                Map<String, Object> stringObjectMap = new HashMap<>();
                stringObjectMap.put("Date", dateUtil.getDateTOStringWk(attendance.getDate()));
                stringObjectMap.put("AllotedShift", "Shift1");
                stringObjectMap.put("ShiftInTime", "6.00 AM");
                stringObjectMap.put("ShiftOutTime", "2.00 PM");
                stringObjectMap.put("EmployeeIntime", dateUtil.getTime(attendance.getPunchin()));
                stringObjectMap.put("EmployeeOuttime", dateUtil.getTime(attendance.getPunchout()));
                mapList.add(stringObjectMap);
                return attendanceDto;
            }).collect(Collectors.toList());
        }
        return new ResponseEntity<List<Map<String, Object>>>(mapList, HttpStatus.OK);
    }

    public ResponseEntity<?> updateOverDueToBreakTime(AttendanceDto attendanceDto) {

        Optional<Attendance> optionalAttendance = attendanceRepository.findById(attendanceDto.getId());
        if (optionalAttendance.isPresent()) {
            Attendance attendance = optionalAttendance.get();
            attendance.setOverDueToBreakTimeReason(attendanceDto.getOverDueToBreakTimeReason());
            attendance.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            attendanceRepository.save(attendance);
            return new ResponseEntity<Status>(
                    new Status("200", "attendance  OverDueToBreakTimeReason updated successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(
                    new Status("400", "attendance OverDueToBreakTimeReason not updated successfully!!!"),
                    HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateOverTime(AttendanceDto attendanceDto) {

        Optional<Attendance> optionalAttendance = attendanceRepository.findById(attendanceDto.getId());
        if (optionalAttendance.isPresent()) {
            Attendance attendance = optionalAttendance.get();
            attendance.setOverTimeReason(attendanceDto.getOverTimeReason());
            attendance.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            attendanceRepository.save(attendance);
            return new ResponseEntity<Status>(new Status("200", "attendance  OverTimeReason updated successfully!!!"),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(
                    new Status("400", "attendance OverTimeReason not updated successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveAttendanceUser(AttendanceDto attendanceDto, User loggedUser) {
        Attendance attendance = new Attendance(attendanceDto);
        attendance.setDate(dateUtil.getStringTODateF1(attendanceDto.getDate()));

        attendance.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        attendance.setCreatedBy(loggedUser.getId());
        attendanceRepository.save(attendance);
        return new ResponseEntity<Status>(new Status("200", "Attendance saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateAttendanceUser(AttendanceDto attendanceDto, User loggedUser) {
        Attendance attendance = new Attendance(attendanceDto);
        attendance.setDate(dateUtil.getStringTODateF1(attendanceDto.getDate()));
        attendance.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        attendance.setUpdatedBy(loggedUser.getId());

        attendanceRepository.save(attendance);
        return new ResponseEntity<Status>(new Status("200", "Attendance updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllAttendance() throws UsernameNotFoundException {
        List<Attendance> attendanceList = attendanceRepository.findAll();
        return new ResponseEntity<List<AttendanceDto>>(attendanceList.stream().map(user -> {
            AttendanceDto attendanceDto = new AttendanceDto(user);
            updateEmployeeDetails(attendanceDto);
            return attendanceDto;
        }).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> overDueToBreakTimeReason(AttendanceDto attendanceDto, User loggedUser) {
        Attendance attendance = new Attendance(attendanceDto);
        /*
         * attendance.setBreakin((attendanceDto.getBreakin()));
         * attendance.setBreakout((attendanceDto.getBreakout()));
         */

        attendance.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        attendance.setUpdatedBy(loggedUser.getId());
        attendanceRepository.save(attendance);
        return new ResponseEntity<Status>(
                new Status("200", "attendance " + attendance.getStatus() + " sent successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> attendanceOutTimeReason(AttendanceTimelineDto attendanceTimelineDto, User loggedUser) {
        AttendanceTimeline attendanceTimeline = new AttendanceTimeline(attendanceTimelineDto);

        attendanceTimeline
                .setAttendanceDate(dateUtil.getStringTODateDDMMYYYY(attendanceTimelineDto.getAttendanceDate()));
        attendanceTimeline.setAttendanceTime(attendanceTimelineDto.getAttendanceTime());
        attendanceTimeline.setUpdated_date(dateUtil.getISTDate());
        attendanceTimeline.setUpdatedBy(loggedUser.getId());
        attendanceTimelineRepository.save(attendanceTimeline);
        return new ResponseEntity<Status>(new Status("200", "attendanceTimeline updated successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> listAlloverDueToBreakTimeReason() throws UsernameNotFoundException {

        List<Attendance> list = attendanceRepository.findAll();
        if (!list.isEmpty()) {
            List<AttendanceDto> result = list.stream().map(data -> {
                AttendanceDto attendanceDto = new AttendanceDto(data);
                /*
                 * attendanceDto.setBreakin((data.getBreakin()));
                 * attendanceDto.setBreakout((data.getBreakout()));
                 */

                updateEmployeeDetails(attendanceDto);
                return attendanceDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<AttendanceDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<AttendanceDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> overTimeReason(AttendanceDto attendanceDto, User loggedUser) {
        Attendance attendance = new Attendance(attendanceDto);
        attendance.setOverTime((attendanceDto.getOverTime()));

        attendance.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        attendance.setUpdatedBy(loggedUser.getId());
        attendanceRepository.save(attendance);
        return new ResponseEntity<Status>(
                new Status("200", "attendance " + attendance.getStatus() + " sent successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAlloverTimeReason() throws UsernameNotFoundException {

        List<Attendance> list = attendanceRepository.findAll();
        if (!list.isEmpty()) {
            List<AttendanceDto> result = list.stream().map(data -> {
                AttendanceDto attendanceDto = new AttendanceDto(data);
                attendanceDto.setOverTime((data.getOverTime()));

                updateEmployeeDetails(attendanceDto);
                return attendanceDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<AttendanceDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<AttendanceDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getAttendance(String id) throws UsernameNotFoundException {
        Optional<Attendance> optionalAttendance = attendanceRepository.findById(Long.parseLong(id));
        AttendanceDto attendanceDto = new AttendanceDto(optionalAttendance.get());

        return new ResponseEntity<AttendanceDto>(attendanceDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteAttendance(String id) throws UsernameNotFoundException {
        try {
            Optional<Attendance> attendanceOptional = attendanceRepository.findById(Long.parseLong(id));
            if (attendanceOptional.isPresent()) {
                Attendance attendance = attendanceOptional.get();
                attendanceRepository.delete(attendance);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateEmployeeDetails(AttendanceDto attendanceDto) {

        if (attendanceDto.getUpdatedBy() != null) {
            Optional<User> user1 = userRepository.findById(attendanceDto.getUpdatedBy());
            if (attendanceDto.getStatus().equalsIgnoreCase("Present")) {
                if (user1.isPresent()) {
                    attendanceDto.setUserId(user1.get().getId());
                    attendanceDto.setUserName(user1.get().getEmployeename());
                }
            }
            if (attendanceDto.getStatus().equalsIgnoreCase("Absent")) {
                attendanceDto.setUserId(user1.get().getId());
                attendanceDto.setUserName(user1.get().getEmployeename());
            }
        }
    }

    public List<Date> getDatesBetweenDays(Date startDate, Date endDate) {
        List<Date> datesInRange = new ArrayList<>();
        Calendar calendar = getCalendarWithoutTime(startDate);
        Calendar endCalendar = getCalendarWithoutTime(endDate);

        while (calendar.before(endCalendar)) {
            Date result = calendar.getTime();
            datesInRange.add(result);
            calendar.add(Calendar.DATE, 1);
        }

        return datesInRange;
    }

    private static Calendar getCalendarWithoutTime(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }



    public ResponseEntity<?> saveInstruLogList(List<InstruLogDto> instruLogList) {

        logger.info("Bulk InstruLog processing started | TotalRecords={}", instruLogList.size());

        int successCount = 0;
        int skippedCount = 0;

        for (InstruLogDto dto : instruLogList) {

            Long bioId = dto.getBioId();

            if (bioId == null) {
                logger.warn("Skipped record - BioId is null");
                skippedCount++;
                continue;
            }

            try {

                Date timeChecking = dateUtil.getStringToDateTime(dto.getAttendanceTime());

                // Duplicate check BEFORE save
                Optional<InstruLog> previousOptional =
                        instruLogRepository.findLatestByBioId(bioId, timeChecking);

                if (previousOptional.isPresent()) {

                    logger.warn("Date checking attdenceTime={} | alreadyInsertTime={} ", dto.getAttendanceTime(), previousOptional.get().getAttendanceTime().toString());
                    long diffMinutes =
                            (timeChecking.getTime()
                                    - previousOptional.get().getAttendanceTime().getTime())
                                    / (1000 * 60);
                    logger.warn("Date checking attdenceTime={} | alreadyInsertTime={} | DiffMinutes={}", dto.getAttendanceTime(), previousOptional.get().getAttendanceTime().toString(), diffMinutes);
                    if (diffMinutes <= 1) {
                        logger.warn("Duplicate entry skipped | BioId={} | DiffMinutes={}",  bioId, diffMinutes);
                        skippedCount++;
                        continue;
                    }
                }

                // Save InstruLog
                InstruLog instruLog = new InstruLog(dto);
                instruLog.setDate(instruLog.getAttendanceTime());
                instruLog.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                InstruLog savedLog = instruLogRepository.save(instruLog);

                if (savedLog.getSysIp() == null || savedLog.getSysIp().trim().isEmpty()) {
                    logger.warn("SysIp missing | BioId={}", bioId);
                    skippedCount++;
                    continue;
                }

                Optional<Biomast> biomastOptional =
                        biomastRepository.findByMachineIp(savedLog.getSysIp());

                if (!biomastOptional.isPresent()) {
                    logger.warn("Biomast not found | IP={} | BioId={}", savedLog.getSysIp(), bioId);
                    //skippedCount++;
                    //continue;
                }

                updateAttendanceDetails(savedLog, biomastOptional, null);

                if (biomastOptional.isPresent() && ( "P1-TrainingHall".equals(biomastOptional.get().getLocation())) ) {
                    updateTrainingAttendanceDetails(savedLog, biomastOptional);
                }

                successCount++;

            } catch (Exception e) {
                logger.error("Error processing BioId={} | Error={}", bioId, e.getMessage(), e);
                skippedCount++;
            }
        }

        logger.info("Bulk InstruLog processing completed | Success={} | Skipped={}",
                successCount, skippedCount);

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("success", true);
        response.put("status", "200");
        response.put("message", "Processing completed");
        response.put("processed", successCount);
        response.put("skipped", skippedCount);

        return new ResponseEntity<Object>(response, HttpStatus.OK);
    }


    public ResponseEntity<?> updateInstruLog(InstruLogDto dto, User updatedBy) {

        Long bioId = dto.getBioId();

        if (bioId == null) {
            logger.warn("Update failed - BioId is null");
            return buildResponse("BioId is null", HttpStatus.BAD_REQUEST, false);
        }

        logger.info("InstruLog update started | BioId={}", bioId);

        try {
            Date timeChecking = dateUtil.getStringToDateTime(dto.getAttendanceTime());
            // 1️⃣ Duplicate check BEFORE save
            Optional<InstruLog> previousOptional =
                    instruLogRepository.findLatestByBioId(bioId);

            if (previousOptional.isPresent()) {

                long diffMinutes =
                        (timeChecking.getTime()
                                - previousOptional.get().getAttendanceTime().getTime())
                                / (1000 * 60);

                if (diffMinutes <= 1) {
                    logger.warn("Duplicate InstruLog skipped | BioId={} | DiffMinutes={}",
                            bioId, diffMinutes);

                    return buildResponse("Duplicate entry within 1 minute",
                            HttpStatus.CONFLICT, false);
                }
            }

            // 2️⃣ Save InstruLog
            InstruLog instruLog = new InstruLog(dto);
            instruLog.setDate(instruLog.getAttendanceTime());
            instruLog.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            InstruLog savedLog = instruLogRepository.save(instruLog);

            logger.info("InstruLog saved successfully | BioId={} | LogId={}",
                    bioId, savedLog.getId());

            // 3️⃣ Biomast validation
            if (savedLog.getSysIp() == null || savedLog.getSysIp().trim().isEmpty()) {
                logger.warn("SysIp missing | BioId={}", bioId);
                // return buildResponse("SysIp missing", HttpStatus.BAD_REQUEST, false);
            }

            Optional<Biomast> biomastOptional =
                    biomastRepository.findByMachineIp(savedLog.getSysIp());

            if (!biomastOptional.isPresent()) {
                logger.warn("Biomast not found | IP={} | BioId={}",
                        savedLog.getSysIp(), bioId);

                //return buildResponse("Machine not registered", HttpStatus.BAD_REQUEST, false);
            }

            // 4️⃣ Update attendance
            updateAttendanceDetails(savedLog, biomastOptional, updatedBy);

            // 5️⃣ Training attendance
            if ("P1-TrainingHall".equals(biomastOptional.get().getLocation())) {
                updateTrainingAttendanceDetails(savedLog, biomastOptional);
            }

            logger.info("InstruLog update completed | BioId={}", bioId);

            return buildResponse("InstruLog saved successfully",
                    HttpStatus.OK, true);

        } catch (Exception e) {

            logger.error("InstruLog update failed | BioId={} | Error={}",
                    bioId, e.getMessage(), e);

            return buildResponse("Internal server error",
                    HttpStatus.INTERNAL_SERVER_ERROR, false);
        }
    }


    private ResponseEntity<Map<String, Object>> buildResponse(
            String message,
            HttpStatus status,
            boolean success) {

        Map<String, Object> response = new HashMap<String, Object>();
        response.put("message", message);
        response.put("status", status.value());
        response.put("success", success);

        return new ResponseEntity<Map<String, Object>>(response, status);
    }

    public void updateAttendanceDetails(InstruLog log,
                                        Optional<Biomast> biomastOptional,
                                        User updatedBy) {

        Long bioId = log.getBioId();
        logger.info("Attendance update started | BioId={}", bioId);

        try {

            Optional<User> userOptional = userRepository.findByBioIdWithActive(bioId);

            if (!userOptional.isPresent()) {
                logger.warn("Attendance skipped - User not found | BioId={}", bioId);
                return;
            }

            User user = userOptional.get();

            String location = null;
            if (biomastOptional.isPresent()) {
                location = biomastOptional.get().getLocation();
            }

            // Gate validation
            if ("P1-GATE".equals(location)) {

                Optional<User> allowed =
                        userRepository.checkAttendance(
                                user.getId(),
                                user.getBranch_id(),
                                user.getDepartment().getId(),
                                log.getDate());

                if (!allowed.isPresent()) {
                    logger.warn("Gate attendance denied | BioId={}", bioId);
                    return;
                }
            }

            // Training validation
            if ("P1-TrainingHall".equals(location)) {

                Optional<TrainingUserRegistrationMapping> registered =
                        trainingUserRegistrationMappingRepository
                                .findByUserIdANDDateANDStatus(
                                        user.getId(),
                                        log.getDate(),
                                        "Registered");

                if (!registered.isPresent()) {
                    logger.warn("Training not registered | BioId={}", bioId);
                    return;
                }
            }

            Optional<Attendance> attendanceOptional =
                    attendanceRepository.findByUserAndDate(user.getId(), log.getDate());

            Attendance attendance;

            if (!attendanceOptional.isPresent()) {

                attendance = new Attendance();
                attendance.setUser(user);
                attendance.setDate(log.getDate());
                attendance.setPunchin(log.getAttendanceTime());
                attendance.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

                if (updatedBy != null) {
                    attendance.setCreatedBy(updatedBy.getId());
                } else {
                    attendance.setCreatedBy(user.getId());
                }

                attendance = attendanceRepository.save(attendance);

            } else {
                attendance = attendanceOptional.get();
            }

            processTimeline(attendance, log, biomastOptional);

            logger.info("Attendance update completed | BioId={}", bioId);

        } catch (Exception e) {
            logger.error("Attendance update failed | BioId={} | Error={}",
                    bioId, e.getMessage(), e);
        }
    }

    private void processTimeline(Attendance attendance,
                                 InstruLog log,
                                 Optional<Biomast> biomastOptional) {

        Long attendanceId = attendance.getId();

        Optional<AttendanceTimeline> latestOptional =
                attendanceTimelineRepository.findLatestByAttendanceId(attendanceId);

        // 🔹 FIRST ENTRY (Check-in)
        if (!latestOptional.isPresent()) {

            AttendanceTimeline timeline = new AttendanceTimeline();
            timeline.setAttendanceId(attendanceId);
            timeline.setUserId(attendance.getUser().getId());
            timeline.setInstrulogId(log.getId());

            if (biomastOptional.isPresent()) {
                timeline.setBiomast(biomastOptional.get());
            }

            timeline.setAttendanceDate(log.getDate());
            timeline.setInTime(log.getAttendanceTime());
            timeline.setOutTime(null);
            timeline.setAttendanceTime(log.getAttendanceTime());
            timeline.setCreatedBy(attendance.getUser().getId());
            timeline.setCreated_date(dateUtil.getISTDate());

            attendanceTimelineRepository.save(timeline);

            return;
        }

        AttendanceTimeline latest = latestOptional.get();

        // 🔹 CHECK-OUT (if last record has no outTime)
        if (latest.getOutTime() == null) {

            latest.setOutTime(log.getAttendanceTime());
            latest.setCreated_date(dateUtil.getISTDate());

            if (latest.getInTime() != null) {
                latest.setEndposition("L");
            }

            attendanceTimelineRepository.save(latest);

            attendance.setPunchout(log.getAttendanceTime());
            attendanceRepository.save(attendance);

        } else {

            // 🔹 NEW CHECK-IN
            AttendanceTimeline timeline = new AttendanceTimeline();

            timeline.setAttendanceId(attendanceId);
            timeline.setUserId(attendance.getUser().getId());
            timeline.setInstrulogId(log.getId());

            if (biomastOptional.isPresent()) {
                timeline.setBiomast(biomastOptional.get());
            }

            timeline.setAttendanceDate(log.getDate());
            timeline.setAttendanceTime(log.getAttendanceTime());
            timeline.setInTime(log.getAttendanceTime());
            timeline.setOutTime(null);
            timeline.setCreatedBy(attendance.getUser().getId());
            timeline.setCreated_date(dateUtil.getISTDate());

            attendanceTimelineRepository.save(timeline);

            attendance.setPunchout(null);
            attendanceRepository.save(attendance);
        }
    }



    /*

	// new code
	public void updateAttendanceDetails(InstruLog attendanceDto, Optional<Biomast> biomastOptional, User updatedBy) {

        logger.info("Updating attendance details for BioId: " + attendanceDto.getBioId());
		try {

			Attendance attendance = new Attendance();

			attendance.setDate(attendanceDto.getDate());

			Optional<User> user = userRepository.findByBioIdWithActive(attendanceDto.getBioId());

			if (!user.isPresent()) {
				logger.error("User not found or inactive for BioId: " + attendanceDto.getBioId());
				return;
			}

			if (biomastOptional.isPresent() && biomastOptional.get().getLocation().equals("P1-GATE")) {

				Optional<User> userOptional = userRepository.checkAttendance(user.get().getId(),
						user.get().getBranch_id(), user.get().getDepartment().getId(), attendanceDto.getDate());

				if (!userOptional.isPresent()) {
					logger.error("Attendance not allowed for BioId: " + attendanceDto.getBioId());
					return;
				}
			}

			if (biomastOptional.isPresent() && biomastOptional.get().getLocation().equals("P1-TrainingHall")) {
				String status = "Registered";
				Optional<TrainingUserRegistrationMapping> trainingUserRegistrationMappingOptional = trainingUserRegistrationMappingRepository
						.findByUserIdANDDateANDStatus(user.get().getId(), attendanceDto.getDate(), status);

				if (!trainingUserRegistrationMappingOptional.isPresent()) {
					logger.error("Training not registered for BioId: " + attendanceDto.getBioId());
					return;
				}
			}

			Optional<Attendance> attendanceOptional = attendanceRepository.findByUserAndDate(user.get().getId(),
					attendanceDto.getDate());

			Attendance resultAttendance = null;
			if (!attendanceOptional.isPresent()) {
				attendance.setUser(user.get());
                attendance.setPunchin(attendanceDto.getAttendanceTime());
				attendance.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				if(updatedBy != null) {
					attendance.setCreatedBy(updatedBy.getId());
				} else {
					attendance.setCreatedBy(user.get().getId());
				}
				resultAttendance = attendanceRepository.save(attendance);
				if (resultAttendance == null) {
					logger.error("Failed to create Attendance record for BioId: " + attendanceDto.getBioId());
					return;
				}
			} else {
				resultAttendance = attendanceOptional.get();
			}

			Optional<AttendanceTimeline> attendanceTimelineLatestOptional = Optional.empty();
			if (attendanceOptional.isPresent()) {
				attendanceTimelineLatestOptional = attendanceTimelineRepository
						.findLatestByAttendanceId(attendanceOptional.get().getId());
			} else {
				attendanceTimelineLatestOptional = attendanceTimelineRepository
						.findLatestByAttendanceId(resultAttendance.getId());
			}

			if (!attendanceTimelineLatestOptional.isPresent()) {

				AttendanceTimeline attendanceTimeline = new AttendanceTimeline();
				attendanceTimeline.setAttendanceId(resultAttendance.getId());
				if (resultAttendance.getUser() != null) {
					attendanceTimeline.setUserId(resultAttendance.getUser().getId());
				}
				attendanceTimeline.setInstrulogId(attendanceDto.getId());
				if (biomastOptional.isPresent()) {
					attendanceTimeline.setBiomast(biomastOptional.get());
				}
				attendanceTimeline.setAttendanceDate(attendanceDto.getDate());
				attendanceTimeline.setInTime(attendanceDto.getAttendanceTime());
				attendanceTimeline.setOutTime(null);
				attendanceTimeline.setAttendanceTime(attendanceDto.getAttendanceTime());
				attendanceTimeline.setCreatedBy(resultAttendance.getUser().getId());
				attendanceTimeline.setCreated_date(dateUtil.getISTDate());
				attendanceTimelineRepository.save(attendanceTimeline);
			} else {

				*//*logger.info("Found latest AttendanceTimeline ID: " + attendanceTimelineLatestOptional.get().getId());
				if (attendanceTimelineLatestOptional.get().getBiomast() == null) {
					logger.error("Biomast is null for AttendanceTimeline ID: "
							+ attendanceTimelineLatestOptional.get().getId());
					return;
				}*//*
				if (attendanceTimelineLatestOptional.get().getOutTime() == null) {
					// insert new checkout
					AttendanceTimeline attendanceTimelineold = attendanceTimelineLatestOptional.get();

					attendanceTimelineold.setOutTime(attendanceDto.getAttendanceTime());
					attendanceTimelineold.setCreated_date(dateUtil.getISTDate());
					if(attendanceTimelineLatestOptional.get().getOutTime() != null && attendanceTimelineLatestOptional.get().getInTime() != null) {
						attendanceTimelineold.setEndposition("L");
					}

					attendanceTimelineRepository.save(attendanceTimelineold);
                    resultAttendance.setPunchout(attendanceDto.getAttendanceTime());
                    attendanceRepository.save(resultAttendance);
				} else {
					  // insert new checkin
					AttendanceTimeline attendanceTimeline = new AttendanceTimeline();

					attendanceTimeline.setAttendanceId(resultAttendance.getId());
					if (resultAttendance.getUser() != null) {
						attendanceTimeline.setUserId(resultAttendance.getUser().getId());
					}
					attendanceTimeline.setInstrulogId(attendanceDto.getId());
					if (biomastOptional.isPresent()) {
						attendanceTimeline.setBiomast(biomastOptional.get());
					}
					attendanceTimeline.setAttendanceDate(attendanceDto.getDate());
					attendanceTimeline.setAttendanceTime(attendanceDto.getAttendanceTime());
					attendanceTimeline.setInTime(attendanceDto.getAttendanceTime());
					attendanceTimeline.setOutTime(null);
					attendanceTimeline.setCreatedBy(resultAttendance.getUser().getId());
					attendanceTimeline.setCreated_date(dateUtil.getISTDate());
					attendanceTimelineRepository.save(attendanceTimeline);
                    resultAttendance.setPunchout(null);
                    attendanceRepository.save(resultAttendance);
                }
			}

		} catch (Exception e) {
			logger.error("Error updating attendance details for BioId: " + attendanceDto.getBioId() + " - "
					+ e.getMessage());
			//return;
		}
	}
*/

    public void updateTrainingAttendanceDetails(InstruLog instruLog, Optional<Biomast> biomastOptional) {
        Optional<User> userOptional = userRepository.findByBioId(instruLog.getBioId());

        if (!userOptional.isPresent()) {
            logger.error("User not found for BioId: " + instruLog.getBioId());
            return;
        }

        if (instruLog.getDate() == null) {
            logger.error("InstruLog date is null for BioId: " + instruLog.getBioId());
            return;
        }

        String status = "Registered";
        Date instrulogDate = instruLog.getDate();

        Optional<TrainingUserRegistrationMapping> trainingUserRegistrationMappingOptional = trainingUserRegistrationMappingRepository
                .findByUserIdANDDateANDStatus(userOptional.get().getId(), instrulogDate, status);

        if (trainingUserRegistrationMappingOptional.isPresent()) {
            if (trainingUserRegistrationMappingOptional.get().getAttendanceStatus() == null) {
                trainingUserRegistrationMappingOptional.get().setAttendanceStatus("Present");
                trainingUserRegistrationMappingRepository.save(trainingUserRegistrationMappingOptional.get());
            } else if (!trainingUserRegistrationMappingOptional.get().getAttendanceStatus().equals("Present")) {
                trainingUserRegistrationMappingOptional.get().setAttendanceStatus("Present");
                trainingUserRegistrationMappingRepository.save(trainingUserRegistrationMappingOptional.get());
            }
        } else {
            logger.error("No training registration found for User ID: " + userOptional.get().getId() + " on date: "
                    + instrulogDate);
            return;
        }
    }

    public ResponseEntity<?> getEmployeesShiftDetails(String employeename, String fromdate, String todate) {

        LocalDate start;
        LocalDate end;

        try {
            start = LocalDate.parse(fromdate);
            end = LocalDate.parse(todate);
        } catch (DateTimeParseException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid date format. Expected yyyy-MM-dd");
        }

        if (start.isAfter(end)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("fromDate must be before or equal to toDate");
        }
        List<AttendanceTimelineProjection> row = attendanceTimelineRepository
                .findByEmployeenameAndfromdateAndTodate();
        List<AttendanceTimelineResDto> list = row.stream().map(data -> {
            AttendanceTimelineResDto dto = new AttendanceTimelineResDto(data);
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // new list api
//	public ResponseEntity<?> getEmployeeShifts(String fromDate, String toDate) {
//		
//		LocalDate start = LocalDate.parse(fromDate);
//		LocalDate end = LocalDate.parse(toDate);
//
//		List<EmployeeShiftProjection> rows = shiftAssignRepository.findAllListForStaff(start, end);
//
//		Map<String, List<EmployeeShiftDto>> result = rows.stream()
//				.collect(Collectors.groupingBy(EmployeeShiftProjection::getEmployeeName, Collectors
//						.mapping(r -> new EmployeeShiftDto(r.getShiftDate(), r.getShiftName()), Collectors.toList())));
//
//		if (result.isEmpty()) {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No records found");
//		}
//
//		return ResponseEntity.ok(result);
//	}
//	
//	public ResponseEntity<?> getEmployeeShiftsdetails(String emptype, String fromDate, String toDate,Pageable pageable) {
//
//	    LocalDate start;
//	    LocalDate end;
//
//	    try {
//	        start = LocalDate.parse(fromDate);
//	        end = LocalDate.parse(toDate);
//	    } catch (DateTimeParseException e) {
//	        return ResponseEntity
//	                .status(HttpStatus.BAD_REQUEST)
//	                .body("Invalid date format. Expected yyyy-MM-dd");
//	    }
//
//	    if (start.isAfter(end)) {
//	        return ResponseEntity
//	                .status(HttpStatus.BAD_REQUEST)
//	                .body("fromDate must be before or equal to toDate");
//	    }
//	    Page<EmployeeShiftProjection> rows = null;
//        if("Staff".equalsIgnoreCase(emptype)) {
//        	    rows = shiftAssignRepository.findAllListForStaff(start, end,pageable);
//        }else {
//        	    rows = shiftAssignRepository.findAllListForWages(start, end,pageable);
//        }
//	    
//
////	    Map<String, List<EmployeeShiftDto>> result =
////	            rows.stream()
////	                .collect(Collectors.groupingBy(
////	                    EmployeeShiftProjection::getEmployeeName,
////	                    Collectors.mapping(
////	                        r -> new EmployeeShiftDto(
////	                                r.getShiftDate(),
////	                                r.getShiftName()
////	                        ),
////	                        Collectors.toList()
////	                    )
////	                ));
//        
//        Map<String, List<EmployeeShiftDto>> employeeMap =
//        	    rows.stream()
//        	        .collect(Collectors.groupingBy(
//        	            EmployeeShiftProjection::getEmployeeName,
//        	            LinkedHashMap::new,
//        	            Collectors.mapping(
//        	                r -> new EmployeeShiftDto(
//        	                        r.getShiftDate(),
//        	                        r.getShiftName()
//        	                ),
//        	                Collectors.toList()
//        	            )
//        	        ));
//
//        	List<EmployeeShiftResponseDto> response =
//        	    employeeMap.entrySet()
//        	        .stream()
//        	        .map(e -> new EmployeeShiftResponseDto(e.getKey(), e.getValue()))
//        	        .collect(Collectors.toList());
//
//        	return ResponseEntity.ok(response);
//
//
    ////	    if (result.isEmpty()) {
    ////	        return ResponseEntity
    ////	                .status(HttpStatus.NOT_FOUND)
    ////	                .body("No records found");
    ////	    }
    ////
    ////	    return ResponseEntity.ok(result);
//	}

    public ResponseEntity<?> getEmployeeShiftsdetails(
            String emptype,
            String fromDate,
            String toDate,
            String empName,
            Long dept, Pageable pageable) {

        LocalDate start;
        LocalDate end;

        try {
            start = LocalDate.parse(fromDate);
            end = LocalDate.parse(toDate);
        } catch (DateTimeParseException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid date format. Expected yyyy-MM-dd");
        }

        if (start.isAfter(end)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("fromDate must be before or equal to toDate");
        }

        List<EmployeeShiftProjection> list;
        Page<Long> empIdPage;

        if ("Staff".equalsIgnoreCase(emptype)) {
            empIdPage = shiftAssignRepository.findAllEmployee(start, end,empName,dept, pageable);
            List<Long> empIds = empIdPage.getContent();
            list =
                    shiftAssignRepository.findAllListForStaff(start, end, empIds);
        } else {
            empIdPage = shiftAssignRepository.findAllWages(start, end,empName,dept, pageable);
            List<Long> empIds = empIdPage.getContent();
            list = shiftAssignRepository.findAllListForWages(start, end, empIds);
        }

        if (list.isEmpty()) {
            return new ResponseEntity<>(
                    new PageDataDto<EmployeeShiftResponseDto>(),
                    HttpStatus.OK
            );
        }

        //  Grouping inside current page
        Map<Long, List<EmployeeShiftProjection>> employeeMap =
                list.stream()
                        .collect(Collectors.groupingBy(
                                EmployeeShiftProjection::getEmpId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        //Build response with bioId
        List<EmployeeShiftResponseDto> result =
                employeeMap.values().stream()
                        .map(empList -> {

                            EmployeeShiftProjection first = empList.get(0);

                            List<EmployeeShiftDto> shifts = empList.stream()
                                    .map(r -> new EmployeeShiftDto(
                                            r.getShiftDate(),
                                            r.getShiftName(),
                                            r.getStartTime(),
                                            r.getEndTime()
                                    ))
                                    .collect(Collectors.toList());

                            return new EmployeeShiftResponseDto(
                                    first.getEmployeeName(),
                                    first.getBioId(),
                                    shifts
                            );
                        })
                        .collect(Collectors.toList());

        Page<EmployeeShiftResponseDto> finalPage =
                new PageImpl<>(result, pageable, empIdPage.getTotalElements());

        PageDataDto<EmployeeShiftResponseDto> pageData =
                new PageDataDto<>(result, finalPage);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }


    public ResponseEntity<?> getEmployeelogdetails(String emptype, String fromdate, String employeename) {

        LocalDate start;

        try {
            start = LocalDate.parse(fromdate);
        } catch (DateTimeParseException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid date format. Expected yyyy-MM-dd");
        }
        List<EmployeeDailyLogList> rows = null;
        if("Staff".equalsIgnoreCase(emptype)) {
            rows = instruLogRepository.findListofEmployeesByFromdate(start, employeename);
        }else {
            rows = dwlogRepository.findListofWagesByFromdate(start, employeename);
        }

        List<EmployeeDailyLogDto> dtoList = rows.stream()
                .map(data -> {
                    EmployeeDailyLogDto dto = new EmployeeDailyLogDto(data);
                    return dto;
                })
                .collect(Collectors.toList());
        if (dtoList.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No records found");
        }

        return ResponseEntity.ok(dtoList);
    }

    public ResponseEntity<?> getemployeeallLogdetails(String emptype ,String employeename,String fromdate,String todate){
        LocalDate start;
        LocalDate end;

        try {
            start = LocalDate.parse(fromdate);
            end = LocalDate.parse(todate);
        } catch (DateTimeParseException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid date format. Expected yyyy-MM-dd");
        }

        if (start.isAfter(end)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("fromDate must be before or equal to toDate");
        }
        List<AttendanceTimelineList> row = null;
        if("Staff".equalsIgnoreCase(emptype)) {
            row = attendanceTimelineRepository
                    .findListByEmployeenameandShiftdate(employeename,start,end);
        }else {
            row = wagesAttendanceTimelineRepository.findListByWagesnameandShiftdate(employeename,start,end);
        }
        List<AttendanceTimelineListDto> list = row.stream().map(data -> {
            AttendanceTimelineListDto dto = new AttendanceTimelineListDto(data);
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    public ResponseEntity<?> getempolyeeOverview(String emptype,String employeename,String fromdate){
        LocalDate start;
        try {
            start = LocalDate.parse(fromdate);
        } catch (DateTimeParseException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid date format. Expected yyyy-MM-dd");
        }
        List<EmployeeOverViewList> row = null;
        if("Staff".equalsIgnoreCase(emptype)) {
            row = attendanceTimelineRepository.findLogDetails(start, employeename);
        }else {
            row = wagesAttendanceTimelineRepository.findLogDetails(start, employeename);
        }
        if (row == null || row.isEmpty()) {
            return new ResponseEntity<>(new Status("400", "Record Not Exists"), HttpStatus.NOT_FOUND);
        }
        EmployeeOverViewList first = row.get(0);
        EmployeeOverViewList last  = row.get(row.size() - 1);
        List<ReasonDto> list = row.stream().map(data -> {

            ReasonDto dto = new ReasonDto();

            if(data.getIntime() != null && data.getOuttime() != null) {
                dto.setId(data.getId());
                if(data.getReason() == null) {
                    dto.setReason(null);
                }else {
                    dto.setReason(data.getReason());
                }
                dto.setIntime(data.getIntime());
                dto.setOuttime(data.getOuttime());
                dto.setTotalworkingdays(data.getTotalworkingdays());
            }

            return dto;
        }).collect(Collectors.toList());

        EmployeeOverViewDto response = new EmployeeOverViewDto();
        response.setIntime(first.getIntime());
        response.setOutime(last.getOuttime());
        response.setAttendancedate(first.getAttendancedate());
        response.setReason(list);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> updateReason(updateReasonDto request, User loggedUser) {

        if("Staff".equalsIgnoreCase(request.getEmptype())) {

            Optional<AttendanceTimeline> optional =
                    attendanceTimelineRepository.findById(request.getId());

            // ❌ If record not found
            if (!optional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Attendance record not found");
            }

            // ✅ If record exists
            AttendanceTimeline attendance = optional.get();
            attendance.setReason(request.getReason());
            attendance.setUpdatedBy(loggedUser.getId());
            attendance.setUpdated_date(dateUtil.getISTDate());

            attendanceTimelineRepository.save(attendance);
        }else {
            Optional<DailyWagesAttendanceTimeline> optional =
                    wagesAttendanceTimelineRepository.findById(request.getId());
            // ❌ If record not found
            if (!optional.isPresent()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Attendance record not found");
            }

            // ✅ If record exists
            DailyWagesAttendanceTimeline attendance = optional.get();
            attendance.setReason(request.getReason());
            attendance.setUpdatedBy(loggedUser.getId());
            attendance.setUpdated_date(dateUtil.getISTDate());

            wagesAttendanceTimelineRepository.save(attendance);
        }

        return ResponseEntity.ok("Reason updated successfully");
    }




    public boolean isOutTimeValid(Long userId, LocalDate today){

        boolean isOutTimeValid = attendanceRepository.existsByUserIdAndAttendanceDateAndOutTimeIsNull(userId, today);

        try {


        }catch (Exception e) {
            logger.error("Error checking out time for user ID: " + userId + " on date: " + today + " - " + e.getMessage());
            return false;
        }
        return isOutTimeValid;
    }






}
