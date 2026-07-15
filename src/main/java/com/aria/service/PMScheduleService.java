package com.aria.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.dto.PMScheduleList;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dto.PMScheduleDto;
import com.aria.dto.PageDataDto;
import com.aria.utils.DateUtil;

@Service
public class PMScheduleService {

    @Autowired
    PMScheduleRepository pmScheduleRepo;

    @Autowired
    TaskCategoryRepository taskCategoryRepository;

    @Autowired
    TaskGroupMasterRepository taskGroupMasterRepository;

    @Autowired
    PanelRepository panelRepository;

    @Autowired
    MachineRepository machineRepository;

    @Autowired
    ScheduleNameRepository scheduleNameRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    TaskGroupRepository taskGroupRepository;

    @Autowired
    PMScheduleDetailsRepo pmScheduleDetailsRepo;

    public ResponseEntity<?> savePMSchedule(PMScheduleDto reqDto, User user) throws ParseException {
        if (reqDto.getId() == null || reqDto.getId() == 0) {
            PMSchedule newPMSchedule = new PMSchedule();
            generatePMSchedule(reqDto, newPMSchedule, user);
            newPMSchedule.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            newPMSchedule.setCreatedBy(user.getId());
            PMSchedule result = pmScheduleRepo.save(newPMSchedule);
            updateScheduleDetails(result);
            return new ResponseEntity<Status>(new Status("200", "PMSchedule details saved successfully!!!"), HttpStatus.OK);
        } else {
            return updatePMSchedule(reqDto, user);
        }
    }


    public void generatePMSchedule(PMScheduleDto reqDto, PMSchedule newPMSchedule, User user) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        newPMSchedule.setScheduleid(reqDto.getScheduleId());
        Optional<ScheduleName> optionalSchedule = scheduleNameRepository.findById(reqDto.getScheduleId());
        if (optionalSchedule.isPresent()) {
            newPMSchedule.setName(optionalSchedule.get().getName());
        }
        Optional<TaskCategory> optionalTaskCategory = taskCategoryRepository.findById(reqDto.getTaskCategoryId());
        if (optionalTaskCategory.isPresent()) {
            newPMSchedule.setTaskCategory(optionalTaskCategory.get());
        }

        Optional<TaskGroupMaster> optionalGroup = taskGroupMasterRepository.findById(reqDto.getTaskGroupId());
        if (optionalGroup.isPresent())
            newPMSchedule.setTaskGroup(optionalGroup.get());

        Optional<Machine> optionalMachine = machineRepository.findById(reqDto.getMachineId());
        if (optionalMachine.isPresent())
            newPMSchedule.setMachine(optionalMachine.get());

        newPMSchedule.setExternalVendor(reqDto.getExternalVendor());
        newPMSchedule.setRemain(reqDto.getRemain());
        if (reqDto.getLastCompleteDate() != null)
            newPMSchedule.setLastCompletedDate(sdf.parse(reqDto.getLastCompleteDate()));

        if (reqDto.getPanelId() != null) {
            Optional<Panel> optionalPanel = panelRepository.findById(reqDto.getPanelId());
            if (optionalPanel.isPresent())
                newPMSchedule.setPanel(optionalPanel.get());
        }

        newPMSchedule.setOption(reqDto.getOption());
        newPMSchedule.setRemind(reqDto.getRemind());
        newPMSchedule.setRemaindMe(reqDto.getRemaindMe());
        newPMSchedule.setDescribe(reqDto.getDescribe());
        newPMSchedule.setOntime(reqDto.getOntime());
        newPMSchedule.setDelaymf(reqDto.getDelaymf());
        newPMSchedule.setMailid(reqDto.getMailid());
        newPMSchedule.setValidator(reqDto.getValidator());
        newPMSchedule.setApprover(reqDto.getApprover());
        newPMSchedule.setUnlockres(reqDto.getUnlockres());
        newPMSchedule.setResponsibility1(reqDto.getResponsibility1());
        newPMSchedule.setResponsibility2(reqDto.getResponsibility2());
        newPMSchedule.setTier1(reqDto.getTier1Id());
        newPMSchedule.setTier2(reqDto.getTier2Id());
        newPMSchedule.setTier3(reqDto.getTier3Id());
        newPMSchedule.setTier4(reqDto.getTier4Id());
    }

    private User getUser(Long userId) {
        if (userId != null) {
            Optional<User> optional = userRepository.findById(userId);
            if (optional.isPresent())
                return optional.get();
        }
        return null;
    }

    public ResponseEntity<?> updatePMSchedule(PMScheduleDto reqDtos, User user) {
        try {
            Optional<PMSchedule> optionalPMSchedule = pmScheduleRepo.findById(reqDtos.getId());
            if (optionalPMSchedule.isPresent()) {
                PMSchedule pmSchedule = optionalPMSchedule.get();
                generatePMSchedule(reqDtos, pmSchedule, user);
                pmSchedule.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                pmSchedule.setUpdatedBy(user.getId());
                PMSchedule result = pmScheduleRepo.save(pmSchedule);
                updateScheduleDetails(result);
            }
            return new ResponseEntity<Status>(new Status("200", "PMSchedule details updated successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "PMSchedule details not updated"), HttpStatus.OK);
    }

    public ResponseEntity<?> getPMScheduleDetails(String id) throws UsernameNotFoundException {
        Optional<PMSchedule> optionalPMSchedule = pmScheduleRepo.findById(Long.parseLong(id));
        PMScheduleDto pmScheduleDto = new PMScheduleDto(optionalPMSchedule.get());
        return new ResponseEntity<PMScheduleDto>(pmScheduleDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deletePMSchedule(Long id) {

        Optional<PMSchedule> optionalPMSchedule = pmScheduleRepo.findById(id);
        if (optionalPMSchedule.isPresent()) {
            pmScheduleRepo.delete(optionalPMSchedule.get());
            return new ResponseEntity<Status>(new Status("200", "PMSchedule detail deleted successfully!!!"),
                    HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("400", "PMSchedule details not deleted"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAllPMScheduleDetails(String scheduleName, String taskCategory, String machineName,
                                                      Long machineId, String dateFrom, String dateTo, String taskGroup,
                                                      User user, Pageable pageable) {

        Page<PMScheduleList> list = pmScheduleRepo.getAllPMScheduleList(taskGroup, scheduleName, taskCategory, machineName, machineId,
                dateFrom, dateTo,
                pageable);
        if (!list.toList().isEmpty()) {
            List<PMScheduleDto> result = list.toList().stream().map(data -> {
                PMScheduleDto pmScheduleDto = new PMScheduleDto(data);
                return pmScheduleDto;
            }).collect(Collectors.toList());
            PageDataDto<PMScheduleDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<PMScheduleDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<PMScheduleDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }



    public void updateScheduleDetails(PMSchedule pmSchedule) {

        Optional<PMScheduleDetails> optionalPMScheduleDetails = pmScheduleDetailsRepo.findByPmscheduleId(pmSchedule.getId());
        PMScheduleDetails pmScheduleDetails = null;
        if (optionalPMScheduleDetails.isPresent()) {
            pmScheduleDetails = optionalPMScheduleDetails.get();
        } else {
            pmScheduleDetails = new PMScheduleDetails(pmSchedule);
        }

        Date scheduleDate = getScheduleDate(pmSchedule);

        pmScheduleDetails.setSchedule_date(scheduleDate);

        pmSchedule.setSchedule_date(scheduleDate);

        pmScheduleDetailsRepo.save(pmScheduleDetails);

        pmScheduleRepo.save(pmSchedule);
    }

    private Date getScheduleDate(PMSchedule pmSchedule) {
        System.out.println(LocalDateTime.now());
        LocalDateTime date = null;
        if(pmSchedule.getSchedule_date() == null) {
             date = LocalDateTime.now().plusHours(pmSchedule.getFrequency());
        }else {
            LocalDateTime dateTime = dateUtil.getDateToLocalDateTime(pmSchedule.getSchedule_date());
            date = dateTime.plusHours(pmSchedule.getFrequency());
        }
        System.out.println(date);
        return dateUtil.getLocalDateTimeToDate(date);
    }



    public ResponseEntity<?> listAllPMScheduleDetailsList (String scheduleName, String taskCategory, String machineName,
                                                      Long machineId, String dateFrom, String dateTo, String taskGroup,
                                                      String status, Pageable pageable) {

        Page<PMScheduleList> list = pmScheduleDetailsRepo.getAllPMScheduleDetailsList(taskGroup, scheduleName, taskCategory,
                machineName, machineId, dateFrom, dateTo, status , pageable);
        if (!list.toList().isEmpty()) {
            List<PMScheduleDto> result = list.toList().stream().map(data -> {
                PMScheduleDto pmScheduleDto = new PMScheduleDto(data);
                return pmScheduleDto;
            }).collect(Collectors.toList());
            PageDataDto<PMScheduleDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<PMScheduleDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<PMScheduleDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> amendmentScheduleStatus(Long id) {

        Optional<PMScheduleDetails> optionalPMSchedule = pmScheduleDetailsRepo.findById(id);
        if (optionalPMSchedule.isPresent()) {
           PMScheduleDetails scheduleDetails = optionalPMSchedule.get();
           scheduleDetails.setStatus("Pending");
           pmScheduleDetailsRepo.save(scheduleDetails);
            return new ResponseEntity<Status>(new Status("200", "PMScheduleDetails detail Amendment successfully!!!"),
                    HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("400", "PMSchedule details not Amendment"), HttpStatus.OK);
    }


    public void updatePMScheduleScheduler () {

        Date scheduleDate = dateUtil.getISTDate();

        List<PMSchedule> pmSchedules = pmScheduleRepo.findByScheduleDate(scheduleDate);

        pmSchedules.stream().map(data -> {
            PMSchedule pmSchedule = data;
            updatePmScheduleDetails(pmSchedule);
            return pmSchedule;
        } );
    }


    public void updatePmScheduleDetails(PMSchedule pmSchedule) {


        PMScheduleDetails   pmScheduleDetails = new PMScheduleDetails(pmSchedule);

        Date scheduleDate = getScheduleDate(pmSchedule);

        pmScheduleDetails.setSchedule_date(scheduleDate);

        pmScheduleDetails.setStatus("Pending");

        pmSchedule.setSchedule_date(scheduleDate);

        pmScheduleDetailsRepo.save(pmScheduleDetails);

        pmScheduleRepo.save(pmSchedule);
    }

}
