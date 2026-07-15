package com.aria.service;

import com.aria.dto.*;
import com.aria.dao.*;
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
import java.util.*;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

@Service
public class InterviewService {

    @Autowired
    InterviewRepository interviewRepository;

    @Autowired
    InterviewQualificationRepository interviewQualificationRepository;

    @Autowired
    InterviewerAssignRepository interviewerAssignRepository;

    @Autowired
    InterviewerAssignDetailsRepository interviewerAssignDetailsRepository;

    @Autowired
    ScoreAssessmentRepository scoreAssessmentRepository;

    @Autowired
    TestScoreRepository testScoreRepository;

    @Autowired
    InterviewScoreRepository interviewScoreRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmastDailyRepository emastDailyRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    QualificationRepository qualificationRepository;

    @Autowired
    CandidateQualificationMappingReposiory candidateQualificationMappingReposiory;

    @Autowired
    EmailNotificationService emailNotificationService;


    // interview candidate details

    @Transactional
    public ResponseEntity<?> saveInterview(InterviewDto interviewDto, User loggedUser) {

        Optional<Interview> interviewOptional = interviewRepository.findByAadhar(interviewDto.getAadharNumber());
        if (interviewOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new Status("400", "Error: AadharNumber is already exits!"));
        }
        Interview interview = new Interview(interviewDto);
        interview.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        interview.setCreatedBy(loggedUser.getId());
        interview.setDateOfBirth(dateUtil.getStringTODateF1(interviewDto.getDateOfBirth()));
        interview.setInterviewDate(dateUtil.getStringTODateF1(interviewDto.getInterviewDate()));
        Interview result = interviewRepository.save(interview);
        if (result.getEmail() != null && result.getCandidateName() != null) {
            System.out.println("###########");
            System.out.println("EMAIL = "+result.getEmail() +" ###  NAME = "+result.getCandidateName());
            System.out.println("###########");
            emailNotificationService.sendNotificationMail(result.getEmail(), result.getCandidateName(), dateUtil.getDateTOStringWk(result.getInterviewDate()), result.getInterviewTime());
        }
        updateQualification(interviewDto, result);

        if (result.getId() > 0) {
            /*InterviewerAssign interviewerAssign = new InterviewerAssign();
            interviewerAssign.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            interviewerAssign.setCreatedBy(loggedUser.getId());
            if (result.getId() != null) {
                Interview interview1 = new Interview();
                interview1.setId(result.getId());
                interviewerAssign.setInterview(interview1);
            }
            if (result.getBranch() != null) {
                Branch branch = new Branch();
                branch.setId(result.getBranch().getId());
                interviewerAssign.setPlace(branch);
            }

            interviewerAssignRepository.save(interviewerAssign);*/

            ScoreAssessment scoreAssessment = new ScoreAssessment();
            scoreAssessment.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            scoreAssessment.setCreatedBy(loggedUser.getId());
            scoreAssessment.setStatus("Pending");
            scoreAssessment.setInterview_score(0);
            scoreAssessment.setIndustrial_score(0);
            scoreAssessment.setFood_saftey_score(0);
            scoreAssessment.setOverall_score(0);
            if (result.getId() != null) {
                Interview interview1 = new Interview();
                interview1.setId(result.getId());
                scoreAssessment.setInterview(interview1);
            }
            /*if (interviewerAssign.getId() != null) {
                InterviewerAssign interviewerAss = new InterviewerAssign();
                interviewerAss.setId(result.getId());
                scoreAssessment.setInterviewerAssign(interviewerAssign);
            }*/

            scoreAssessmentRepository.save(scoreAssessment);
        }
        for (InterviewQualificationDto interviewQualificationDto : interviewDto.getQualifications()) {
            InterviewQualification interviewQualification = new InterviewQualification(interviewQualificationDto);
            interviewQualification.setInterviewId(result.getId());
            interviewQualification
                    .setExperienceFrom(dateUtil.getStringTODateF1(interviewQualificationDto.getExperienceFrom()));
            interviewQualification
                    .setExperienceTo(dateUtil.getStringTODateF1(interviewQualificationDto.getExperienceTo()));
            interview.setInterviewDate(dateUtil.getStringTODateF1(interviewDto.getInterviewDate()));
            interviewQualificationRepository.save(interviewQualification);
        }
        return new ResponseEntity<Status>(new Status("200", "interview details save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateInterview(InterviewDto interviewDto, User loggedUser) {
        Interview interview = new Interview(interviewDto);
        interview.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        interview.setUpdatedBy(loggedUser.getId());
        interview.setDateOfBirth(dateUtil.getStringTODateF1(interviewDto.getDateOfBirth()));
        interview.setInterviewDate(dateUtil.getStringTODateF1(interviewDto.getInterviewDate()));
        Interview result = interviewRepository.save(interview);
        updateQualification(interviewDto, result);
        if (interviewDto.getStatus() != null && interviewDto.getStatus().equalsIgnoreCase("hired")) {
            updateEmployee(interviewDto, result);
        }
        for (InterviewQualificationDto interviewQualificationDto : interviewDto.getQualifications()) {
            InterviewQualification interviewQualification = new InterviewQualification(interviewQualificationDto);
            interviewQualification.setInterviewId(result.getId());
            interviewQualification
                    .setExperienceFrom(dateUtil.getStringTODateF1(interviewQualificationDto.getExperienceFrom()));
            interviewQualification
                    .setExperienceTo(dateUtil.getStringTODateF1(interviewQualificationDto.getExperienceTo()));
            interviewQualificationRepository.save(interviewQualification);
        }
        return new ResponseEntity<Status>(new Status("200", "interview details updated  successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> listAllInterview(Pageable pageable) throws UsernameNotFoundException {
        Page<Interview> list = interviewRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<InterviewDto> result = list.toList().stream().map(data -> {
                InterviewDto interviewDto = new InterviewDto(data);
                interviewDto.setDateOfBirth(dateUtil.getStringTODate(data.getDateOfBirth()));
                interviewDto.setInterviewDate(dateUtil.getStringTODate(data.getInterviewDate()));

                List<InterviewQualification> qualification = interviewQualificationRepository
                        .findAllByInterviewId(interviewDto.getId());
                List<InterviewQualificationDto> interviewQualificationDtos = qualification.stream().map(data1 -> {

                    InterviewQualificationDto interview = new InterviewQualificationDto(data1);
                    interview.setExperienceFrom(dateUtil.getDateToStringDDMMYYYY(data1.getExperienceFrom()));
                    interview.setExperienceTo(dateUtil.getDateToStringDDMMYYYY(data1.getExperienceTo()));
                    return interview;
                }).collect(Collectors.toList());

                interviewDto.setQualifications(interviewQualificationDtos);
                return interviewDto;

            }).collect(Collectors.toList());
            PageDataDto<InterviewDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<InterviewDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<InterviewDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> interviewfilterList(String candidateName, String employeeType, String interviewPosition,
                                                 String status, Long branchId, /* String interviewTime, */
                                                 String dateFrom, String dateTo, String adhaar, String place, Pageable pageable)
            throws UsernameNotFoundException {
        Page<Interview> list = interviewRepository.interviewfilterList(candidateName, employeeType, interviewPosition,
                branchId, status, dateUtil.getStringTODateF1(dateFrom), dateUtil.getStringTODateF1(dateTo), adhaar, place, pageable);
        if (!list.toList().isEmpty()) {
            List<InterviewDto> result = list.toList().stream().map(data -> {
                InterviewDto interviewDto = new InterviewDto(data);
                interviewDto.setDateOfBirth(dateUtil.getDateToStringDDMMYYYY(data.getDateOfBirth()));
                interviewDto.setInterviewDate(dateUtil.getDateToStringDDMMYYYY(data.getInterviewDate()));

                List<InterviewQualification> qualification = interviewQualificationRepository
                        .findAllByInterviewId(interviewDto.getId());
                List<InterviewQualificationDto> interviewQualificationDtos = qualification.stream().map(data1 -> {

                    InterviewQualificationDto interview = new InterviewQualificationDto(data1);
                    interview.setExperienceFrom(dateUtil.getDateToStringDDMMYYYY(data1.getExperienceFrom()));
                    interview.setExperienceTo(dateUtil.getDateToStringDDMMYYYY(data1.getExperienceTo()));
                    return interview;
                }).collect(Collectors.toList());

                interviewDto.setQualifications(interviewQualificationDtos);
                return interviewDto;

            }).collect(Collectors.toList());
            PageDataDto<InterviewDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<InterviewDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<InterviewDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getInterviewDetails(String id) throws UsernameNotFoundException {
        Optional<Interview> optionalInterview = interviewRepository.findById(Long.parseLong(id));
        if (optionalInterview.isPresent()) {
            InterviewDto interviewDto = new InterviewDto(optionalInterview.get());
            interviewDto.setDateOfBirth(dateUtil.getDateToStringDDMMYYYY(optionalInterview.get().getDateOfBirth()));
            interviewDto.setInterviewDate(dateUtil.getDateToStringDDMMYYYY(optionalInterview.get().getInterviewDate()));

            List<InterviewQualification> qualification = interviewQualificationRepository
                    .findAllByInterviewId(interviewDto.getId());
            List<InterviewQualificationDto> interviewQualificationDtos = qualification.stream().map(data1 -> {
                InterviewQualificationDto interview = new InterviewQualificationDto(data1);
                interview.setExperienceFrom(dateUtil.getDateToStringDDMMYYYY(data1.getExperienceFrom()));
                interview.setExperienceTo(dateUtil.getDateToStringDDMMYYYY(data1.getExperienceTo()));
                return interview;
            }).collect(Collectors.toList());

            interviewDto.setQualifications(interviewQualificationDtos);
            List<QualificationDto> qualificationDtos = new ArrayList<>();
            List<CandidateQualificationMapping> candidateQualificationMappingList = candidateQualificationMappingReposiory.findByEmpId(optionalInterview.get().getId());
            if (!candidateQualificationMappingList.isEmpty()) {
                for (CandidateQualificationMapping candidateQualificationMapping : candidateQualificationMappingList) {
                    Optional<Qualification> qualificationOptional = qualificationRepository.findById(candidateQualificationMapping.getQualificationId());
                    if (qualificationOptional.isPresent()) {
                        QualificationDto qualificationDto = new QualificationDto(qualificationOptional.get().getName(), candidateQualificationMapping.getMarks());
                        qualificationDtos.add(qualificationDto);
                    }
                }
            }
            interviewDto.setQualificationList(qualificationDtos);
            return new ResponseEntity<InterviewDto>(interviewDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteInterviewDetails(String id) throws UsernameNotFoundException {
        try {

//            Optional<InterviewerAssign> optionalInterviewerAssign = interviewerAssignRepository
//                    .findByInterviewId(Long.parseLong(id));
//            if (optionalInterviewerAssign.isPresent()) {
//                InterviewerAssign interviewerAssign = optionalInterviewerAssign.get();
//                interviewerAssignRepository.delete(interviewerAssign);
//            }

            Optional<ScoreAssessment> opt = scoreAssessmentRepository.findByInterviewId(Long.parseLong(id));
            if (opt.isPresent()) {
                ScoreAssessment scoreAssessment = opt.get();
                scoreAssessmentRepository.delete(scoreAssessment);
            }

            Optional<Interview> optionalInterview = interviewRepository.findById(Long.parseLong(id));
            if (optionalInterview.isPresent()) {
                Interview interview = optionalInterview.get();
                interviewRepository.delete(interview);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


// interviewer assign

    public ResponseEntity<?> saveInterviewerAssign(List<InterviewerAssignDto> interviewerAssignDtos, User loggedUser) {
        List<InterviewerAssign> interviewAssignList = new LinkedList<>();
        for (InterviewerAssignDto interviewerAssignDto : interviewerAssignDtos) {
            InterviewerAssign interviewerAssign = new InterviewerAssign(interviewerAssignDto);
            interviewerAssign.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            interviewerAssign.setCreatedBy(loggedUser.getId());
            interviewAssignList.add(interviewerAssign);
        }

        List<InterviewerAssign> result = interviewerAssignRepository.saveAll(interviewAssignList);
        if (result.size() > 0) {
            return new ResponseEntity<Status>(new Status("200", "InterviewerAssign save successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("304", "InterviewerAssign not save successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateInterviewerAssign(List<InterviewerAssignDto> interviewerAssignDtos, User loggedUser) {
        List<InterviewerAssign> interviewAssignList = new LinkedList<>();
        for (InterviewerAssignDto interviewerAssignDto : interviewerAssignDtos) {
            InterviewerAssign interviewerAssign = new InterviewerAssign(interviewerAssignDto);
            interviewerAssign.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            interviewerAssign.setUpdatedBy(loggedUser.getId());
            interviewAssignList.add(interviewerAssign);
        }

        List<InterviewerAssign> result = interviewerAssignRepository.saveAll(interviewAssignList);

        /*if (result.size() > 0) {
            for (InterviewerAssign interviewerAssign : result) {
                Optional<ScoreAssessment> sa = scoreAssessmentRepository.findById(interviewerAssign.getId());
                if (sa.isPresent()) {
                    InterviewerAssign interviewerAssign1 = new InterviewerAssign();
                    interviewerAssign1.setId(interviewerAssign.getId());
                    sa.get().setInterviewerAssign(interviewerAssign1);
                    scoreAssessmentRepository.save(sa.get());
                    ;
                }
            }
        }*/
        return new ResponseEntity<Status>(new Status("200", "InterviewerAssign updated  successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> listAllInterviewerAssign(Pageable pageable) throws UsernameNotFoundException {
        Page<InterviewerAssign> list = interviewerAssignRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<InterviewerAssignDto> result = list.toList().stream().map(data -> {
                InterviewerAssignDto interviewerAssignDto = new InterviewerAssignDto(data);
                interviewerAssignDto.setInterviewDate(dateUtil.getDateToStringDDMMYYYY(data.getInterview().getInterviewDate()));
                return interviewerAssignDto;
            }).collect(Collectors.toList());
            PageDataDto<InterviewerAssignDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<InterviewerAssignDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<InterviewerAssignDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> interviewerAssignFilterList(String branch, String candidateName, String interviewerName,
                                                         String examinerName, String fromDate, String toDate, Pageable pageable) throws UsernameNotFoundException {
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
        Page<InterviewerAssignList> list = interviewerAssignRepository.interviewerAssignFilterList(branch, candidateName, interviewerName, examinerName, startDate, endDate, pageable);
        if (!list.toList().isEmpty()) {
            List<InterviewerAssignListDto> result = list.toList().stream().map(data -> new InterviewerAssignListDto(data)).collect(Collectors.toList());
            PageDataDto<InterviewerAssignListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<InterviewerAssignListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<InterviewerAssignListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getInterviewerAssignDetails(String id) throws UsernameNotFoundException {
        Optional<InterviewerAssign> optionalInterviewerAssign = interviewerAssignRepository
                .findById(Long.parseLong(id));
        if (optionalInterviewerAssign.isPresent()) {
            InterviewerAssignDto interviewerAssignDto = new InterviewerAssignDto(optionalInterviewerAssign.get());
            interviewerAssignDto.setInterviewDate(dateUtil
                    .getDateToStringDDMMYYYY(optionalInterviewerAssign.get().getInterview().getInterviewDate()));
            return new ResponseEntity<InterviewerAssignDto>(interviewerAssignDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteInterviewerAssignDetails(String id) throws UsernameNotFoundException {
        try {
//            Optional<ScoreAssessment> opt = scoreAssessmentRepository.findByInterviewAssignId(Long.parseLong(id));
//            if (opt.isPresent()) {
//                ScoreAssessment scoreAssessment = opt.get();
//                scoreAssessmentRepository.delete(scoreAssessment);
//            }
//            Optional<InterviewerAssign> optionalInterviewerAssign = interviewerAssignRepository
//                    .findById(Long.parseLong(id));
//            if (optionalInterviewerAssign.isPresent()) {
//                InterviewerAssign interviewerAssign = optionalInterviewerAssign.get();
//                interviewerAssignRepository.delete(interviewerAssign);
//            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    // score assessment
    public ResponseEntity<?> saveScoreAssessment(ScoreAssessmentDto scoreAssessmentDto, User loggedUser) {
        ScoreAssessment scoreAssessment = new ScoreAssessment(scoreAssessmentDto);
        scoreAssessment.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        scoreAssessment.setCreatedBy(loggedUser.getId());
        scoreAssessment.setStatus("Pending");
        scoreAssessmentRepository.save(scoreAssessment);
        return new ResponseEntity<Status>(new Status("200", "ScoreAssessment saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateScoreAssessment(List<ScoreAssessmentDto> scoreAssessmentDtos, User loggedUser) {
        List<ScoreAssessment> scoreAssessmentList = new LinkedList<>();
        for (ScoreAssessmentDto scoreAssessmentDto : scoreAssessmentDtos) {
            ScoreAssessment scoreAssessment = new ScoreAssessment(scoreAssessmentDto);
            scoreAssessment.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            scoreAssessment.setUpdatedBy(loggedUser.getId());
            scoreAssessmentList.add(scoreAssessment);
            ScoreAssessment result = scoreAssessmentRepository.save(scoreAssessment);
            updateScoreAssessmentStatus(result, loggedUser);
        }


        return new ResponseEntity<Status>(new Status("200", "scoreAssessment updated successfully!!!"), HttpStatus.OK);
    }

    public void updateScoreAssessmentStatus(ScoreAssessment scoreAssessment, User loggedUser) {
//        if (scoreAssessment.getInterviewerAssign() != null && scoreAssessment.getInterviewerAssign().getInterview() != null) {
        if (scoreAssessment.getInterview() != null) {
            Optional<Interview> interviewOptional = interviewRepository.findById(scoreAssessment.getInterview().getId());
            if (interviewOptional.isPresent()) {
                Interview interview = interviewOptional.get();
                interview.setStatus(scoreAssessment.getStatus());
                interview.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                interview.setUpdatedBy(loggedUser.getId());
                interviewRepository.save(interview);
            }
        }

    }


    public ResponseEntity<?> scoreAssessmentFilterList(String candidateName,
                                                       String fromDate, String toDate, Pageable pageable) throws UsernameNotFoundException {

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
        Page<ScoreAssessmentList> list = scoreAssessmentRepository.scoreAssessmentFilterList(candidateName, startDate, endDate, pageable);
        if (!list.toList().isEmpty()) {
            List<ScoreAssessmentListDto> result = list.toList().stream().map(data -> new ScoreAssessmentListDto(data)).collect(Collectors.toList());
            PageDataDto<ScoreAssessmentListDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<ScoreAssessmentListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ScoreAssessmentListDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getscoreAssessmentDetails(String id) throws UsernameNotFoundException {
        Optional<ScoreAssessment> optionalScoreAssessment = scoreAssessmentRepository.findById(Long.parseLong(id));
        ScoreAssessmentDto scoreAssessmentDto = new ScoreAssessmentDto(optionalScoreAssessment.get());
        scoreAssessmentDto.setInterviewDate(
                dateUtil.getDateToStringDDMMYYYY(optionalScoreAssessment.get().getInterview().getInterviewDate()));
        return new ResponseEntity<ScoreAssessmentDto>(scoreAssessmentDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteScoreAssessment(String id) throws UsernameNotFoundException {
        try {
            Optional<ScoreAssessment> scoreAssessmentOptional = scoreAssessmentRepository.findById(Long.parseLong(id));
            if (scoreAssessmentOptional.isPresent()) {
                ScoreAssessment scoreAssessment = scoreAssessmentOptional.get();
                scoreAssessmentRepository.delete(scoreAssessment);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateEmployee(InterviewDto interviewDto, Interview interview) {
        System.out.println("#######");
        System.out.println("#######");
        System.out.println("#######");
        if(interviewDto.getEmpType() == 0) {
            Optional<User> userOptional = userRepository.findBy(interview.getAadharNumber(), interview.getPhoneNumber(), interview.getCandidateName());
            if (!userOptional.isPresent()) {
                System.out.println("save");
                User user = new User(interview);
                user.setStatus("P");
                user.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                userRepository.save(user);
                System.out.println("success");
            } else {
                System.out.println("not success");
            }
        }else {
            Optional<EmastDaily> userOptional = emastDailyRepository.findBy(interview.getAadharNumber(), interview.getPhoneNumber(), interview.getCandidateName());
            if (!userOptional.isPresent()) {
                System.out.println("save EmastDaily");
                EmastDaily user = new EmastDaily(interview);
                user.setStatus("P");
                emastDailyRepository.save(user);
                System.out.println("success EmastDaily");
            } else {
                System.out.println("not success EmastDaily");
            }
        }
    }

    public void updateQualification(InterviewDto userDto, Interview user) {
        List<CandidateQualificationMapping> candidateQualificationMappings = candidateQualificationMappingReposiory.findByEmpId(user.getId());
        if (!candidateQualificationMappings.isEmpty()) {
            candidateQualificationMappingReposiory.deleteAll(candidateQualificationMappings);
        }
        if (!userDto.getQualificationList().isEmpty()) {
            for (QualificationDto qualification : userDto.getQualificationList()) {
                Qualification resultQuali = null;
                Optional<Qualification> optionalQualification = qualificationRepository.findByName(qualification.getQualification());
                if (optionalQualification.isPresent()) {
                    resultQuali = optionalQualification.get();

                } else {
                    Qualification qualification1 = new Qualification(qualification.getQualification());
                    resultQuali = qualificationRepository.save(qualification1);
                }
                CandidateQualificationMapping qualificationMapping = new CandidateQualificationMapping();
                qualificationMapping.setQualificationId(resultQuali.getId());
                qualificationMapping.setMarks(qualification.getMarks());
                qualificationMapping.setCandidateId(user.getId());
                candidateQualificationMappingReposiory.save(qualificationMapping);
            }
        }
    }

}
