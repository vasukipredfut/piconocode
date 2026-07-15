package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.DwlogDto;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DwlogService {

    public static final Logger logger = LoggerFactory.getLogger(DwlogService.class);

    @Autowired
    private DwlogRepository dwlogRepository;

    @Autowired
    private DailyWagesAttendanceRepository attendanceRepository;

    @Autowired
    private DailyWagesAttendanceTimelineRepository attendanceTimelineRepository;

    @Autowired
    private BiomastRepository biomastRepository;

    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private EmastDailyRepository emastDailyRepository;


    public ResponseEntity<?> updateDwlog(DwlogDto dwlogDto, User loggedUser) {
        Map<String,Object> response = new HashMap<>();
        try {
            logger.info("Processing DwlogDto: {}", dwlogDto);

            if (dwlogDto.getBioId() == null) {
                logger.error("BioId is null");
                response.put("success", "false");
                response.put("status", "400");
                response.put("message", "BioId is required");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            Optional<EmastDaily> user = emastDailyRepository.findByBioIdWithActive(dwlogDto.getBioId());

            if (!user.isPresent()) {
                logger.error("User not found or inactive for BioId: " + dwlogDto.getBioId());
                response.put("success", "false");
                response.put("status", "404");
                response.put("message", "User not found or inactive for BioId: " + dwlogDto.getBioId());
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Optional<Dwlog> dwlogPreviousOptional = Optional.empty();
            dwlogPreviousOptional = dwlogRepository.findByBioId(dwlogDto.getBioId());

            Dwlog dwlog = new Dwlog(dwlogDto);

            dwlog.setDate(dateUtil.getDateValue(dwlog.getAttDateTime()));
            Dwlog result = dwlogRepository.save(dwlog);

            if (dwlogPreviousOptional.isPresent()) {
                long diffMillis = result.getAttDateTime().getTime()
                        - dwlogPreviousOptional.get().getAttDateTime().getTime();

                long diffMinutes = diffMillis / (1000 * 60);

                if (diffMinutes <= 1l) {
                    logger.error("Duplicate Dwlog entry for BioId: " + dwlogDto.getAttendanceDateTime());
                    response.put("success", "false");
                    response.put("status", "409");
                    response.put("message", "Duplicate entry for BioId: " + dwlogDto.getBioId());
                    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
                }
            }

            Optional<Biomast> biomastOptional = Optional.empty();
            if ((result.getSysIp() != null) && (!result.getSysIp().isEmpty())) {
                biomastOptional = biomastRepository.findByMachineIp(result.getSysIp());
            } else {
                logger.error("SysIp is null or empty for Dwlog with BioId: " + dwlogDto.getBioId());
                //continue;
            }
            
            updateAttendanceLog(result, biomastOptional);

            response.put("success", "true");
            response.put("status", "200");
            response.put("message", "Dwlog list saved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<?> getDwlog(Long id) {
        Optional<Dwlog> opt = dwlogRepository.findById(id);
        if (opt.isPresent()) {
            DwlogDto dto = new DwlogDto(opt.get());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    

    public ResponseEntity<?> listDwlogs() {
        List<DwlogDto> list = dwlogRepository.findAll().stream().map(DwlogDto::new).collect(Collectors.toList());
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteDwlog(Long id) {
        try {
            dwlogRepository.deleteById(id);
            return new ResponseEntity<>(new Status("200", "Dwlog deleted successfully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(new Status("304", "Could not delete record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> saveDwLogList(List<DwlogDto> dwlogDtos) {
        logger.info("Saving Dwlog list with size: {}", dwlogDtos != null ? dwlogDtos.size() : 0);
        Map<String, Object> response = new HashMap<>();
        try {
            if (dwlogDtos == null || dwlogDtos.isEmpty()) {
                logger.warn("No Dwlog data provided to save");
                response.put("success", "false");
                response.put("status", "400");
                response.put("message", "No data provided");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            logger.info("Received Dwlog list: {}", dwlogDtos);
            for (DwlogDto dwlogDto : dwlogDtos) {
                logger.info("Processing DwlogDto: {}", dwlogDto);

                if (dwlogDto.getBioId() == null) {
                    logger.error("BioId is null");
                    continue;
                }

                Optional<Dwlog> checkSlno = dwlogRepository.findBySlno(dwlogDto.getSlno());
                if (checkSlno.isPresent()) {
                    logger.warn("Duplicate Slno found: {}", dwlogDto.getSlno());
                    continue;
                }

                Optional<Dwlog> dwlogPreviousOptional = Optional.empty();
                dwlogPreviousOptional = dwlogRepository.findByBioId(dwlogDto.getBioId());

                Dwlog dwlog = new Dwlog(dwlogDto);

                dwlog.setDate(dateUtil.getDateValue(dwlog.getAttDateTime()));
                Dwlog result = dwlogRepository.save(dwlog);

                if (dwlogPreviousOptional.isPresent()) {
                    long diffMillis = result.getAttDateTime().getTime()
                            - dwlogPreviousOptional.get().getAttDateTime().getTime();

                    long diffMinutes = diffMillis / (1000 * 60);

                    if (diffMinutes <= 1l) {
                        logger.error("Duplicate Dwlog entry for BioId: " + dwlogDto.getAttendanceDateTime());
                        continue;
                    }
                }

                Optional<Biomast> biomastOptional = Optional.empty();
                if ((result.getSysIp() != null) && (!result.getSysIp().isEmpty())) {
                    biomastOptional = biomastRepository.findByMachineIp(result.getSysIp());
                } else {
                    logger.error("SysIp is null or empty for Dwlog with BioId: " + dwlogDto.getBioId());
                    continue;
                }



                updateAttendanceLog(result, biomastOptional);

            }
            response.put("success", "true");
            response.put("status", "200");
            response.put("message", "Dwlog list saved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception ex) {
            logger.error("Error saving Dwlog list", ex);
            ex.printStackTrace();
            response.put("success", "false");
            response.put("status", "500");
            response.put("message", "Error saving Dwlog list");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private void updateAttendanceLog(Dwlog dwlog, Optional<Biomast> biomastOptional) {
        // Implement logic to update attendance log based on the Dwlog entity
        // This is a placeholder method and should be implemented according to your application's requirements

        try {
            // Example logic to update attendance log
            logger.info("Updating attendance log for Dwlog with ID: {}", dwlog.getId());
            // Fetch the related attendance log based on the Dwlog details
            // Update the attendance log with the new information from the Dwlog
            // Save the updated attendance log back to the database
            logger.info("Updating attendance details for BioId: " + dwlog.getBioId());
            try {

                DailyWagesAttendance attendance = new DailyWagesAttendance();

                attendance.setDate(dwlog.getDate());

                logger.info("Finding user for BioId: " + dwlog.getBioId());

                Optional<EmastDaily> user = emastDailyRepository.findByBioIdWithActive(dwlog.getBioId());

                if (!user.isPresent()) {
                    logger.error("User not found or inactive for BioId: " + dwlog.getBioId());
                    return;
                }


                Optional<DailyWagesAttendance> attendanceOptional = attendanceRepository.findByUserAndDate(user.get().getId(), dwlog.getDate());

                DailyWagesAttendance resultAttendance = null;
                if (!attendanceOptional.isPresent()) {
                    attendance.setUser(user.get());
                    attendance.setPunchIn(dwlog.getAttDateTime());
                    attendance.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    resultAttendance = attendanceRepository.save(attendance);
                    if (resultAttendance == null) {
                        logger.error("Failed to create Attendance record for BioId: " + dwlog.getBioId());
                        return;
                    }
                } else {
                    resultAttendance = attendanceOptional.get();
                }

                Optional<DailyWagesAttendanceTimeline> attendanceTimelineLatestOptional = Optional.empty();
                if (attendanceOptional.isPresent()) {
                    attendanceTimelineLatestOptional = attendanceTimelineRepository
                            .findLatestByAttendanceId(attendanceOptional.get().getId());
                } else {
                    attendanceTimelineLatestOptional = attendanceTimelineRepository
                            .findLatestByAttendanceId(resultAttendance.getId());
                }

                if (!attendanceTimelineLatestOptional.isPresent()) {

                    DailyWagesAttendanceTimeline attendanceTimeline = new DailyWagesAttendanceTimeline();
                    attendanceTimeline.setAttendanceId(resultAttendance.getId());
                    if (resultAttendance.getUser() != null) {
                        attendanceTimeline.setUserId(resultAttendance.getUser().getId());
                    }
                    attendanceTimeline.setDwlogId(dwlog.getId());
                    if (biomastOptional.isPresent()) {
                        attendanceTimeline.setBiomast(biomastOptional.get());
                    }
                    attendanceTimeline.setAttendanceDate(dwlog.getDate());
                    attendanceTimeline.setInTime(dwlog.getAttDateTime());
                    attendanceTimeline.setAttendanceTime(dwlog.getAttDateTime());
                    attendanceTimeline.setCreatedBy(resultAttendance.getUser().getId());
                    attendanceTimeline.setCreated_date(dateUtil.getISTDate());
                    attendanceTimelineRepository.save(attendanceTimeline);
                    resultAttendance.setPunchIn(dwlog.getAttDateTime());
                    attendanceRepository.save(resultAttendance);
                } else {

                    /*logger.info("Found latest AttendanceTimeline ID: " + attendanceTimelineLatestOptional.get().getId());
                    if (attendanceTimelineLatestOptional.get().getBiomast() == null) {
                        logger.error("Biomast is null for AttendanceTimeline ID: "
                                + attendanceTimelineLatestOptional.get().getId());
                        return;
                    }*/
                    if (attendanceTimelineLatestOptional.get().getOutTime() == null) {
                        // insert new checkout
                        DailyWagesAttendanceTimeline attendanceTimelineold = attendanceTimelineLatestOptional.get();

                        attendanceTimelineold.setOutTime(dwlog.getAttDateTime());
                        attendanceTimelineold.setCreated_date(dateUtil.getISTDate());
                        if (attendanceTimelineLatestOptional.get().getOutTime() != null && attendanceTimelineLatestOptional.get().getInTime() != null) {
                            attendanceTimelineold.setEndposition("L");
                        }

                        attendanceTimelineRepository.save(attendanceTimelineold);
                        resultAttendance.setPunchOut(dwlog.getAttDateTime());
                        attendanceRepository.save(resultAttendance);
                    } else {
                        // insert new checkin
                        DailyWagesAttendanceTimeline attendanceTimeline = new DailyWagesAttendanceTimeline();

                        attendanceTimeline.setAttendanceId(resultAttendance.getId());
                        if (resultAttendance.getUser() != null) {
                            attendanceTimeline.setUserId(resultAttendance.getUser().getId());
                        }
                        attendanceTimeline.setDwlogId(dwlog.getId());
                        if (biomastOptional.isPresent()) {
                            attendanceTimeline.setBiomast(biomastOptional.get());
                        }
                        attendanceTimeline.setAttendanceDate(dwlog.getDate());
                        attendanceTimeline.setAttendanceTime(dwlog.getAttDateTime());
                        attendanceTimeline.setInTime(dwlog.getAttDateTime());
                        attendanceTimeline.setOutTime(null);
                        attendanceTimeline.setCreatedBy(resultAttendance.getUser().getId());
                        attendanceTimeline.setCreated_date(dateUtil.getISTDate());
                        attendanceTimelineRepository.save(attendanceTimeline);
                        resultAttendance.setPunchOut(null);
                        attendanceRepository.save(resultAttendance);

                    }

                }

            } catch (Exception e) {
                logger.error("Error updating attendance details for BioId: " + dwlog.getBioId() + " - "
                        + e.getMessage());
                return;
            }

        } catch (Exception ex) {
            logger.error("Error updating attendance log for Dwlog with ID: {}", dwlog.getId(), ex);
        }

    }


}
