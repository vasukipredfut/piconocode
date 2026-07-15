package com.aria.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import com.aria.dto.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dao.MultipleChoiceQuestionRepository;
import com.aria.dao.OpenEndedQuestionRepository;
import com.aria.dao.ScheduleTrainingRepository;
import com.aria.dao.SkillMatrixGradeLevelRepository;
import com.aria.dao.SkillMatrixRepository;
import com.aria.dao.TraineeAnswerMultipleChoiceQuestionRepository;
import com.aria.dao.TraineeAnswerOpenEndedQuestionRepository;
import com.aria.dao.TraineeExamScoreRepository;
import com.aria.dao.TrainerDetailRepository;
import com.aria.dao.TrainerFeedbackScoreRepository;
import com.aria.dao.TrainingEmployeeCategoryMappingRepository;
import com.aria.dao.TrainingUserLevelMappingRepository;
import com.aria.dao.TrainingUserRegistrationMappingRepository;
import com.aria.dao.UserRepository;
import com.aria.model.DepartmentDetails;
import com.aria.model.Designation;
import com.aria.model.ILowSkillLevelUserCount;
import com.aria.model.IRegisteredUserListScheduleTraining;
import com.aria.model.IScheduleTrainingFilter;
import com.aria.model.ISkillMatrixFilter;
import com.aria.model.ITrainerLastTrainingDetailReport;
import com.aria.model.ITrainerTrainingHistory;
import com.aria.model.MultipleChoiceQuestion;
import com.aria.model.OpenEndedQuestion;
import com.aria.model.ScheduleTraining;
import com.aria.model.SkillGradeLevel;
import com.aria.model.SkillMatrix;
import com.aria.model.SkillMatrixGradeLevel;
import com.aria.model.Status;
import com.aria.model.TraineeAnswerMultipleChoiceQuestion;
import com.aria.model.TraineeAnswerOpenEndedQuestion;
import com.aria.model.TraineeExamScore;
import com.aria.model.TrainerDetail;
import com.aria.model.TrainerFeedbackScore;
import com.aria.model.TrainingEmployeeCategoryMapping;
import com.aria.model.TrainingUserLevelMapping;
import com.aria.model.TrainingUserRegistrationMapping;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class TrainingService {

    @Value("${answer.sheet.upload.path}")
    private String answerSheetUploadPath;

    @Autowired
    SkillMatrixRepository skillMatrixRepository;

    @Autowired
    SkillMatrixGradeLevelRepository skillMatrixGradeLevelRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    ScheduleTrainingRepository scheduleTrainingRepository;

    @Autowired
    TrainerDetailRepository trainerDetailRepository;

    @Autowired
    MultipleChoiceQuestionRepository multipleChoiceQuestionRepository;

    @Autowired
    OpenEndedQuestionRepository openEndedQuestionRepository;

    @Autowired
    TrainingUserRegistrationMappingRepository trainingUserRegistrationMappingRepository;

    @Autowired
    TraineeAnswerMultipleChoiceQuestionRepository traineeAnswerMultipleChoiceQuestionRepository;

    @Autowired
    TraineeAnswerOpenEndedQuestionRepository traineeAnswerOpenEndedQuestionRepository;

    @Autowired
    TraineeExamScoreRepository traineeExamScoreRepository;

    @Autowired
    TrainerFeedbackScoreRepository trainerFeedbackScoreRepository;

    @Autowired
    TrainingUserLevelMappingRepository trainingUserLevelMappingRepository;

    @Autowired
    TrainingEmployeeCategoryMappingRepository trainingEmployeeCategoryMappingRepository;

    @Autowired
    UserRepository userRepository;

    // skill matrix

    public ResponseEntity<?> saveSkillMatrix(SkillMatrixDto skillMatrixDto, User loggedUser) {
        SkillMatrix skillMatrix = new SkillMatrix(skillMatrixDto);

        if (skillMatrixDto.getUserId() != null) {
            User user = new User();
            user.setId(skillMatrixDto.getUserId());
            skillMatrix.setUser(user);
        } else {
            skillMatrix.setUser(loggedUser);
        }
        if (skillMatrixDto.getDepartmentId() != null) {
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(skillMatrixDto.getDepartmentId());
            skillMatrix.setDepartment(departmentDetails);
        } else {
            skillMatrix.setDepartment(loggedUser.getDepartment());
        }
        skillMatrix.setDesignation(loggedUser.getDesignation());
        skillMatrix.setDate(dateUtil.getStringTODateWk(skillMatrixDto.getDate()));
        skillMatrix.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        skillMatrix.setUpdated_date(dateUtil.getISTDate());
        skillMatrix.setCreatedBy(loggedUser.getId());
        SkillMatrix result = skillMatrixRepository.save(skillMatrix);
        if (!skillMatrixDto.getSkillMatrixGradeLevelDtos().isEmpty()) {
            for (SkillMatrixGradeLevelDto skillMatrixGradeLevelDto : skillMatrixDto.getSkillMatrixGradeLevelDtos()) {
                SkillMatrixGradeLevel skillMatrixGradeLevel = new SkillMatrixGradeLevel(skillMatrixGradeLevelDto);
                skillMatrixGradeLevel.setSkillMatrixId(result.getId());
                skillMatrixGradeLevelRepository.save(skillMatrixGradeLevel);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "SkillMatrix saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateSkillMatrix(SkillMatrixDto skillMatrixDto, User loggedUser) {
        SkillMatrix skillMatrix = new SkillMatrix(skillMatrixDto);
        skillMatrix.setDate(dateUtil.getStringTODateWk(skillMatrixDto.getDate()));
        skillMatrix.setUpdated_date(dateUtil.getISTDate());
        skillMatrix.setUpdatedBy(loggedUser.getId());
        if (skillMatrixDto.getUserId() != null) {
            User user = new User();
            user.setId(skillMatrixDto.getUserId());
            skillMatrix.setUser(user);
        } else {
            skillMatrix.setUser(loggedUser);
        }
        if (skillMatrixDto.getDepartmentId() != null) {
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(skillMatrixDto.getDepartmentId());
            skillMatrix.setDepartment(departmentDetails);
        } else {
            skillMatrix.setDepartment(loggedUser.getDepartment());
        }
        SkillMatrix result = skillMatrixRepository.save(skillMatrix);
        if (!skillMatrixDto.getSkillMatrixGradeLevelDtos().isEmpty()) {
            for (SkillMatrixGradeLevelDto skillMatrixGradeLevelDto : skillMatrixDto.getSkillMatrixGradeLevelDtos()) {
                SkillMatrixGradeLevel skillDetailsGradeLevel = new SkillMatrixGradeLevel(skillMatrixGradeLevelDto);
                skillDetailsGradeLevel.setSkillMatrixId(result.getId());
                skillMatrixGradeLevelRepository.save(skillDetailsGradeLevel);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "SkillMatrix updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllSkillMatrix() throws UsernameNotFoundException {
        List<SkillMatrix> skillMatrixList = skillMatrixRepository.findAll();
        return new ResponseEntity<List<SkillMatrixDto>>(skillMatrixList.stream().map(data -> {
            SkillMatrixDto skillMatrixDto = new SkillMatrixDto(data);
            skillMatrixDto.setDate(dateUtil.getStringTODate(data.getDate()));
            return skillMatrixDto;
        }).collect(Collectors.toList()), HttpStatus.OK);
    }


    public ResponseEntity<?> filterListSkillMatrix(User user, Long branchId, Long departmentId, Long designationId,
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

        Page<SkillMatrixList> list = skillMatrixRepository.skillMatrixfilterList(user.getId(), designationId, departmentId, startDate, endDate, pageable);

        if (!list.toList().isEmpty()) {
            List<SkillMatrixListDto> result = list.toList().stream().map(data -> {
                SkillMatrixListDto compentencyMatrixDto = new SkillMatrixListDto(data);
                return compentencyMatrixDto;
            }).collect(Collectors.toList());
            PageDataDto<SkillMatrixListDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<SkillMatrixListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SkillMatrixListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> allSkillMatrixFilter(User user, Long branchId, Long departmentId, Long designationId,
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

        Page<SkillMatrixList> list = skillMatrixRepository.skillMatrixfilterList(user.getId(), designationId, departmentId, startDate, endDate, pageable);

        if (!list.toList().isEmpty()) {
            List<SkillMatrixListDto> result = list.toList().stream().map(data -> {
                SkillMatrixListDto compentencyMatrixDto = new SkillMatrixListDto(data);
                return compentencyMatrixDto;
            }).collect(Collectors.toList());
            PageDataDto<SkillMatrixListDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<SkillMatrixListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SkillMatrixListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


	/*public ResponseEntity<?> filterListSkillMatrix(User user, Long branchId, Long departmentId, Long designationId,
												   String dateFrom, String dateTo, Boolean isFilterForOnlyLoggedInUser) throws UsernameNotFoundException {

		HashMap<String, Date> dateHashMap = dateDynamic(dateFrom, dateTo);
		List<ISkillMatrixFilter> iSkillMatrixFilterList = new ArrayList<>();
		if (isFilterForOnlyLoggedInUser) {
			iSkillMatrixFilterList = skillMatrixRepository.filterListSkillMatrix(user.getId(), null,
					dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"));
		} else {
			if (branchId == null) {
				if (user.getBranch_id()!= null) {
					branchId = user.getBranch_id();
				} else {
					return new ResponseEntity<Status>(
							new Status("400", "Invalid. branchId is null for the loggedin user"),
							HttpStatus.BAD_REQUEST);
				}
			}
			iSkillMatrixFilterList = skillMatrixRepository.filterListSkillMatrixAllUser(null, branchId, departmentId,
					designationId, dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"));
		}

		return new ResponseEntity<List<SkillMatrixFilterDto>>(iSkillMatrixFilterList.stream().map(data -> {
			SkillMatrixFilterDto skillMatrixFilterDto = new SkillMatrixFilterDto(data);
			skillMatrixFilterDto.setDate(dateUtil.getDateToStringDDMMYYYY(data.getDate()));
			if (data.getUpdatedDate() != null) {
				skillMatrixFilterDto.setUpdatedDate(dateUtil.getLocalDateTimeToStringDDMMYYYY(data.getUpdatedDate()));
			}
			return skillMatrixFilterDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}*/

    public ResponseEntity<?> getSkillMatrix(String id) throws UsernameNotFoundException {
        Optional<SkillMatrix> skillMatrixOptional = skillMatrixRepository.findById(Long.parseLong(id));
        if (skillMatrixOptional.isPresent()) {
            SkillMatrixDto skillMatrixDto = new SkillMatrixDto(skillMatrixOptional.get());
            skillMatrixDto.setDate(dateUtil.getDateToStringDDMMYYYY(skillMatrixOptional.get().getDate()));
            return new ResponseEntity<SkillMatrixDto>(skillMatrixDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getSkillMatrixList(Optional<User> userOptional, Long branchId, Long departmentId,
                                                Long designationId, String date, Boolean isFilterForOnlyLoggedInUser) throws UsernameNotFoundException {

        Date dateFilter = null;
        List<SkillMatrix> skillMatrixList = null;

        if (date != null && !date.isEmpty()) {
            dateFilter = dateUtil.getStringTODateF1(date);
        }

        if (isFilterForOnlyLoggedInUser) {
            skillMatrixList = skillMatrixRepository.getSkillMatrixList(userOptional.get().getId(), branchId, departmentId, designationId, dateFilter);
        } else {
            skillMatrixList = skillMatrixRepository.getSkillMatrixList(null, branchId, departmentId, designationId, dateFilter);
        }

        List<SkillMatrixDto> skillMatrixDtos = new ArrayList<>();
        if (!skillMatrixList.isEmpty()) {
            for (SkillMatrix skillMatrix : skillMatrixList) {
                SkillMatrixDto skillMatrixDto = new SkillMatrixDto(skillMatrix);
                skillMatrixDto.setDate(dateUtil.getDateToStringDDMMYYYY(skillMatrix.getDate()));
                skillMatrixDtos.add(skillMatrixDto);
            }
            return new ResponseEntity<List<SkillMatrixDto>>(skillMatrixDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(skillMatrixDtos, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getSkillMatrixListWithSingleSkill(Long branchId, Long departmentId, Long designationId,
                                                               Long skillId) {

        List<SkillMatrix> skillMatrixList = null;
        skillMatrixList = skillMatrixRepository.getSkillMatrixListWithSingleSkill(branchId, departmentId, designationId,
                skillId);

        List<SkillMatrixDto> skillMatrixDtos = new ArrayList<>();
        if (!skillMatrixList.isEmpty()) {
            for (SkillMatrix skillMatrix : skillMatrixList) {
                SkillMatrixDto skillMatrixDto = new SkillMatrixDto(skillMatrix);
                skillMatrixDto.setDate(dateUtil.getDateToStringDDMMYYYY(skillMatrix.getDate()));
                skillMatrixDtos.add(skillMatrixDto);
            }
            return new ResponseEntity<List<SkillMatrixDto>>(skillMatrixDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(skillMatrixDtos, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> deleteSkillMatrix(String id) throws UsernameNotFoundException {
        try {
            Optional<SkillMatrix> skillMatrixOptional = skillMatrixRepository.findById(Long.parseLong(id));
            if (skillMatrixOptional.isPresent()) {
                SkillMatrix skillMatrix = skillMatrixOptional.get();
                skillMatrixRepository.delete(skillMatrix);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> getLowSkillLevelUserCount(Long branchId, Long departmentId, Long designationId,
                                                       String dateTo) {

        Date dateFilter = null;
        if (dateTo != null && !dateTo.isEmpty()) {
            dateFilter = dateUtil.getStringTODateF1(dateTo);
        }

        List<ILowSkillLevelUserCount> skillMatrixAverageGradeLevelList = skillMatrixGradeLevelRepository
                .getLowSkillLevelUserCount(branchId, departmentId, designationId, dateFilter);

        return new ResponseEntity<List<LowSkillLevelUserCountDto>>(
                skillMatrixAverageGradeLevelList.stream().map(data -> {
                    LowSkillLevelUserCountDto lowSkillLevelUserCountDto = new LowSkillLevelUserCountDto(data);
                    return lowSkillLevelUserCountDto;
                }).collect(Collectors.toList()), HttpStatus.OK);
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

    // ScheduleTraining

    @Transactional
    public ResponseEntity<?> saveScheduleTraining(ScheduleTrainingDto scheduleTrainingDto, User loggedUser) {

        ScheduleTraining scheduleTraining = new ScheduleTraining(scheduleTrainingDto);
        scheduleTraining.setTrainingDate(dateUtil.getStringTODateWk(scheduleTrainingDto.getTrainingDate()));
        scheduleTraining.setLastRegistrationDate(dateUtil.getStringTODateWk(scheduleTrainingDto.getLastRegistrationDate()));

        if (scheduleTraining.getTrainingDate() != null && scheduleTraining.getLastRegistrationDate() != null) {
            if (scheduleTraining.getLastRegistrationDate().equals(scheduleTraining.getTrainingDate())
                    || scheduleTraining.getLastRegistrationDate().after(scheduleTraining.getTrainingDate())) {
                return new ResponseEntity<Status>(
                        new Status("400", "ScheduleTraining LastRegistrationDate should be less than TrainingDate !!!"),
                        HttpStatus.BAD_REQUEST);
            }
        }

        scheduleTraining.setStatus("Pending");
        scheduleTraining.setRemainingSeats(scheduleTraining.getTotalSeats());
        if (scheduleTrainingDto.getId() == null) {
            scheduleTraining.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            scheduleTraining.setCreatedBy(loggedUser.getId());
        } else {
            scheduleTraining.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            scheduleTraining.setUpdatedBy(loggedUser.getId());
        }
        ScheduleTraining result = scheduleTrainingRepository.save(scheduleTraining);
        ScheduleTrainingDto scheduleTrainingDto2 = new ScheduleTrainingDto(result);
        scheduleTrainingDto2.setTrainingDate(dateUtil.getDateTOStringWk(result.getTrainingDate()));
        scheduleTrainingDto2
                .setLastRegistrationDate(dateUtil.getDateTOStringWk(result.getLastRegistrationDate()));
        if (scheduleTrainingDto.getTrainerDetailDtos() != null
                && !scheduleTrainingDto.getTrainerDetailDtos().isEmpty()) {
            for (TrainerDetailDto trainerDetailDto : scheduleTrainingDto.getTrainerDetailDtos()) {
                TrainerDetail trainerDetail = new TrainerDetail(trainerDetailDto);
                ScheduleTraining scheduleTraining1 = new ScheduleTraining();
                scheduleTraining1.setId(result.getId());
                trainerDetail.setScheduleTraining(scheduleTraining1);
                TrainerDetail result2 = trainerDetailRepository.save(trainerDetail);
            }
        }

        // save in TrainingUserLevelMapping
        if (scheduleTrainingDto.getTrainingUserLevelMappingDtos() != null
                && !scheduleTrainingDto.getTrainingUserLevelMappingDtos().isEmpty()) {
            for (TrainingUserLevelMappingDto trainingUserLevelMappingDto : scheduleTrainingDto.getTrainingUserLevelMappingDtos()) {
                TrainingUserLevelMapping trainingUserLevelMapping = new TrainingUserLevelMapping(trainingUserLevelMappingDto);
                trainingUserLevelMapping.setScheduleTrainingId(result.getId());
                trainingUserLevelMappingRepository.save(trainingUserLevelMapping);
            }
        }

        // save in TrainingEmployeeCategoryMapping
        if (scheduleTrainingDto.getTrainingEmployeeCategoryMappingDtos() != null
                && !scheduleTrainingDto.getTrainingEmployeeCategoryMappingDtos().isEmpty()) {
            for (TrainingEmployeeCategoryMappingDto trainingEmployeeCategoryMappingDto : scheduleTrainingDto
                    .getTrainingEmployeeCategoryMappingDtos()) {
                TrainingEmployeeCategoryMapping trainingEmployeeCategoryMapping = new TrainingEmployeeCategoryMapping(
                        trainingEmployeeCategoryMappingDto);
                trainingEmployeeCategoryMapping.setScheduleTrainingId(result.getId());
                trainingEmployeeCategoryMappingRepository.save(trainingEmployeeCategoryMapping);
            }
        }

        if (scheduleTrainingDto.getMultipleChoiceQuestionDtos() != null
                && !scheduleTrainingDto.getMultipleChoiceQuestionDtos().isEmpty()) {
            for (MultipleChoiceQuestionsDto multipleChoiceQuestionsDto : scheduleTrainingDto
                    .getMultipleChoiceQuestionDtos()) {
                MultipleChoiceQuestion multipleChoiceQuestion = new MultipleChoiceQuestion(multipleChoiceQuestionsDto);
                multipleChoiceQuestion.setScheduleTrainingId(result.getId());
                multipleChoiceQuestionRepository.save(multipleChoiceQuestion);
            }
        }
        if (scheduleTrainingDto.getOpenEndedQuestionDtos() != null
                && !scheduleTrainingDto.getOpenEndedQuestionDtos().isEmpty()) {
            for (OpenEndedQuestionsDto openEndedQuestionsDto : scheduleTrainingDto.getOpenEndedQuestionDtos()) {
                OpenEndedQuestion openEndedQuestion = new OpenEndedQuestion(openEndedQuestionsDto);
                openEndedQuestion.setScheduleTrainingId(result.getId());
                openEndedQuestionRepository.save(openEndedQuestion);
            }
        }
        return new ResponseEntity<ScheduleTrainingDto>(scheduleTrainingDto2, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> updateScheduleTraining(ScheduleTrainingDto scheduleTrainingDto, User loggedUser) {

        Optional<ScheduleTraining> scheduleTrainingOptional = scheduleTrainingRepository
                .findById(scheduleTrainingDto.getId());

        if (!scheduleTrainingOptional.isPresent()) {
            return new ResponseEntity<Status>(new Status("400", "ScheduleTraining is not present!!!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!scheduleTrainingOptional.get().getStatus().equals("Pending")) {
            return new ResponseEntity<Status>(
                    new Status("400", "ScheduleTraining can be updated only when Pending status!!!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!scheduleTrainingOptional.get().getTrainingType().equals(scheduleTrainingDto.getTrainingType())) {
            return new ResponseEntity<Status>(new Status("400", "ScheduleTraining Training Type can not be updated!!!"),
                    HttpStatus.BAD_REQUEST);
        }

        ScheduleTraining scheduleTraining = new ScheduleTraining(scheduleTrainingDto);
        scheduleTraining.setTrainingDate(dateUtil.getStringTODateWk(scheduleTrainingDto.getTrainingDate()));
        scheduleTraining
                .setLastRegistrationDate(dateUtil.getStringTODateWk(scheduleTrainingDto.getLastRegistrationDate()));
        scheduleTraining.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        if (scheduleTraining.getTrainingDate() != null && scheduleTraining.getLastRegistrationDate() != null) {
            if (scheduleTraining.getLastRegistrationDate().equals(scheduleTraining.getTrainingDate())
                    || scheduleTraining.getLastRegistrationDate().after(scheduleTraining.getTrainingDate())) {
                return new ResponseEntity<Status>(
                        new Status("400", "ScheduleTraining LastRegistrationDate should be less than TrainingDate !!!"),
                        HttpStatus.BAD_REQUEST);
            }
        }

        scheduleTraining.setUpdatedBy(loggedUser.getId());
        ScheduleTraining result = scheduleTrainingRepository.save(scheduleTraining);
        if (scheduleTrainingDto.getTrainerDetailDtos() != null
                && !scheduleTrainingDto.getTrainerDetailDtos().isEmpty()) {
            for (TrainerDetailDto trainerDetailDto : scheduleTrainingDto.getTrainerDetailDtos()) {
                TrainerDetail trainerDetail = new TrainerDetail(trainerDetailDto);
                ScheduleTraining scheduleTraining1 = new ScheduleTraining();
                scheduleTraining1.setId(result.getId());
                trainerDetail.setScheduleTraining(scheduleTraining1);
                TrainerDetail result2 = trainerDetailRepository.save(trainerDetail);
            }
        }

        // save in TrainingUserLevelMapping
        if (scheduleTrainingDto.getTrainingUserLevelMappingDtos() != null
                && !scheduleTrainingDto.getTrainingUserLevelMappingDtos().isEmpty()) {

            Set<Long> idsSet = trainingUserLevelMappingRepository
                    .findAllByScheduleTrainingId(scheduleTrainingDto.getId());

            Set<Long> deletedIds = new LinkedHashSet<>();
            Set<Long> invalidIds = new LinkedHashSet<>();

            for (TrainingUserLevelMappingDto trainingUserLevelMappingDto : scheduleTrainingDto
                    .getTrainingUserLevelMappingDtos()) {
                if (trainingUserLevelMappingDto.getId() == null) {
                    break;
                }
                boolean matched = false;
                for (Long id : idsSet) {
                    if (id.equals(trainingUserLevelMappingDto.getId())) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    invalidIds.add(trainingUserLevelMappingDto.getId());
                }
            }

            if (!invalidIds.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                invalidIds.forEach(data -> stringBuilder.append(data + ", "));
                throw new RuntimeException("400: Invalid trainingUserLevelMappingDto Ids: " + stringBuilder.toString());
            }

            for (Long id : idsSet) {
                boolean matched = false;
                for (TrainingUserLevelMappingDto trainingUserLevelMappingDto : scheduleTrainingDto
                        .getTrainingUserLevelMappingDtos()) {
                    if (id.equals(trainingUserLevelMappingDto.getId())) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    deletedIds.add(id);
                }
            }

            trainingUserLevelMappingRepository.deleteAllByIdsIn(deletedIds);

            for (TrainingUserLevelMappingDto trainingUserLevelMappingDto : scheduleTrainingDto
                    .getTrainingUserLevelMappingDtos()) {
                TrainingUserLevelMapping trainingUserLevelMapping = new TrainingUserLevelMapping(
                        trainingUserLevelMappingDto);
                trainingUserLevelMapping.setScheduleTrainingId(result.getId());
                trainingUserLevelMappingRepository.save(trainingUserLevelMapping);
            }
        }

        // save in TrainingEmployeeCategoryMapping
        if (scheduleTrainingDto.getTrainingEmployeeCategoryMappingDtos() != null
                && !scheduleTrainingDto.getTrainingEmployeeCategoryMappingDtos().isEmpty()) {

            Set<Long> idsSet = trainingEmployeeCategoryMappingRepository
                    .findAllByScheduleTrainingId(scheduleTrainingDto.getId());

            Set<Long> deletedIds = new LinkedHashSet<>();
            Set<Long> invalidIds = new LinkedHashSet<>();

            for (TrainingEmployeeCategoryMappingDto trainingEmployeeCategoryMappingDto : scheduleTrainingDto
                    .getTrainingEmployeeCategoryMappingDtos()) {
                if (trainingEmployeeCategoryMappingDto.getId() == null) {
                    break;
                }
                boolean matched = false;
                for (Long id : idsSet) {
                    if (id.equals(trainingEmployeeCategoryMappingDto.getId())) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    invalidIds.add(trainingEmployeeCategoryMappingDto.getId());
                }
            }

            if (!invalidIds.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                invalidIds.forEach(data -> stringBuilder.append(data + ", "));
                throw new RuntimeException(
                        "400: Invalid trainingEmployeeCategoryMappingDto Ids: " + stringBuilder.toString());
            }

            for (Long id : idsSet) {
                boolean matched = false;
                for (TrainingEmployeeCategoryMappingDto trainingEmployeeCategoryMappingDto : scheduleTrainingDto
                        .getTrainingEmployeeCategoryMappingDtos()) {
                    if (id.equals(trainingEmployeeCategoryMappingDto.getId())) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    deletedIds.add(id);
                }
            }

            trainingEmployeeCategoryMappingRepository.deleteAllByIdsIn(deletedIds);

            for (TrainingEmployeeCategoryMappingDto trainingEmployeeCategoryMappingDto : scheduleTrainingDto
                    .getTrainingEmployeeCategoryMappingDtos()) {
                TrainingEmployeeCategoryMapping trainingEmployeeCategoryMapping = new TrainingEmployeeCategoryMapping(
                        trainingEmployeeCategoryMappingDto);
                trainingEmployeeCategoryMapping.setScheduleTrainingId(result.getId());
                trainingEmployeeCategoryMappingRepository.save(trainingEmployeeCategoryMapping);
            }
        }

        if (scheduleTrainingDto.getMultipleChoiceQuestionDtos() != null
                && !scheduleTrainingDto.getMultipleChoiceQuestionDtos().isEmpty()) {
            for (MultipleChoiceQuestionsDto multipleChoiceQuestionsDto : scheduleTrainingDto
                    .getMultipleChoiceQuestionDtos()) {
                MultipleChoiceQuestion multipleChoiceQuestion = new MultipleChoiceQuestion(multipleChoiceQuestionsDto);
                multipleChoiceQuestion.setScheduleTrainingId(result.getId());
                multipleChoiceQuestionRepository.save(multipleChoiceQuestion);
            }
        }

        if (scheduleTrainingDto.getOpenEndedQuestionDtos() != null
                && !scheduleTrainingDto.getOpenEndedQuestionDtos().isEmpty()) {
            for (OpenEndedQuestionsDto openEndedQuestionsDto : scheduleTrainingDto.getOpenEndedQuestionDtos()) {
                OpenEndedQuestion openEndedQuestion = new OpenEndedQuestion(openEndedQuestionsDto);
                openEndedQuestion.setScheduleTrainingId(result.getId());
                openEndedQuestionRepository.save(openEndedQuestion);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "ScheduleTraining updated successfully!!!"), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteScheduleTraining(Long id) {

        Optional<ScheduleTraining> scheduleTrainingOptional = scheduleTrainingRepository.findById(id);
        if (!scheduleTrainingOptional.isPresent()) {
            return new ResponseEntity<Status>(new Status("400", "ScheduleTraining does not exist"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!scheduleTrainingOptional.get().getStatus().equals("Pending")) {
            return new ResponseEntity<Status>(new Status("400", "ScheduleTraining status must be Pending for delete"),
                    HttpStatus.BAD_REQUEST);
        }
        try {
            scheduleTrainingRepository.delete(scheduleTrainingOptional.get());
        } catch (Exception ex) {
            throw new RuntimeException("304, Could not Delete Record", ex);
        }
        return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
    }

    public ResponseEntity<?> getFilterListScheduleTraining(Long branchId, String trainingType, Long departmentId,
                                                           Long designationId, String trainingTopic, String fromDate, String toDate, Pageable pageable)
            throws UsernameNotFoundException {

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
        Page<IScheduleTrainingFilter> list = null;
        try {
            list = scheduleTrainingRepository.getFilterListScheduleTraining(branchId, trainingType, departmentId, designationId, trainingTopic, startDate, endDate, pageable);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!list.toList().isEmpty()) {
            List<ScheduleTrainingFilterDto> result = list.toList().stream().map(data -> new ScheduleTrainingFilterDto(data)).collect(Collectors.toList());
            PageDataDto<ScheduleTrainingFilterDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<ScheduleTrainingFilterDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ScheduleTrainingFilterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getLastTrainingDetailReportOfTrainer(Long userId) {

//		Integer trainingCount = trainerDetailRepository.getTrainingCount(userId, Arrays.asList("Pending", "Scheduled"));

        Integer trainingCount = trainerDetailRepository.getTrainingCount(userId);

//		ITrainerLastTrainingDetailReport iTrainerLastTrainingDetailReport = trainerDetailRepository
//				.getLastTrainingDetail(userId, Arrays.asList("Pending", "Scheduled"));

        ITrainerLastTrainingDetailReport iTrainerLastTrainingDetailReport = trainerDetailRepository
                .getLastTrainingDetail(userId);

        if (iTrainerLastTrainingDetailReport != null) {
            TrainerLastTrainingDetailReportDto trainerLastTrainingDetailReportDto = new TrainerLastTrainingDetailReportDto(
                    iTrainerLastTrainingDetailReport);
            trainerLastTrainingDetailReportDto.setTrainingCount(trainingCount);
            trainerLastTrainingDetailReportDto.setLastTrainingDate(
                    dateUtil.getDateToStringDDMMYYYY(iTrainerLastTrainingDetailReport.getLastTrainingDate()));
            return new ResponseEntity<>(trainerLastTrainingDetailReportDto, HttpStatus.OK);
        }

        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    public ResponseEntity<?> getScheduleTraining(Long id) {
        Optional<ScheduleTraining> scheduleTrainingOptional = scheduleTrainingRepository.findById(id);
        ScheduleTrainingDto scheduleTrainingDto = null;
        if (scheduleTrainingOptional.isPresent()) {
            scheduleTrainingDto = new ScheduleTrainingDto(scheduleTrainingOptional.get());

            if (scheduleTrainingOptional.get().getTrainingDate() != null) {
                scheduleTrainingDto.setTrainingDate(
                        dateUtil.getDateToStringDDMMYYYY(scheduleTrainingOptional.get().getTrainingDate()));
            }
            if (scheduleTrainingOptional.get().getLastRegistrationDate() != null) {
                scheduleTrainingDto.setLastRegistrationDate(
                        dateUtil.getDateToStringDDMMYYYY(scheduleTrainingOptional.get().getLastRegistrationDate()));
            }

//			for (TrainerDetail trainerDetail : scheduleTrainingOptional.get().getTrainerDetails()) {
//				for (TrainerDetailDto trainerDetailDto : scheduleTrainingDto.getTrainerDetailDtos()) {
//					if (trainerDetail.getId() == trainerDetailDto.getId()) {
//						if (trainerDetail.getLastScheduleTraining() != null) {
//							trainerDetailDto.setLastTrainingOn(dateUtil.getDateToStringDDMMYYYY(
//									trainerDetail.getLastScheduleTraining().getTrainingDate()));
//						}
//					}
//				}
//			}
            return new ResponseEntity<ScheduleTrainingDto>(scheduleTrainingDto, HttpStatus.OK);
        }

        return new ResponseEntity<ScheduleTrainingDto>(new ScheduleTrainingDto(), HttpStatus.OK);
    }

    public ResponseEntity<?> getTrainerTrainingHistory(Long userId) {


        List<ITrainerTrainingHistory> iTrainerTrainingHistoryList = trainerDetailRepository
                .getTrainerTrainingHistory(userId);

        List<TrainerTrainingHistoryDto> trainerTrainingHistoryDtos = new ArrayList<>();
        if (!iTrainerTrainingHistoryList.isEmpty()) {
            for (ITrainerTrainingHistory iTrainerTrainingHistory : iTrainerTrainingHistoryList) {

                TrainerTrainingHistoryDto trainerTrainingHistoryDto = new TrainerTrainingHistoryDto(
                        iTrainerTrainingHistory);
                trainerTrainingHistoryDto
                        .setTrainingDate(dateUtil.getDateToStringDDMMYYYY(iTrainerTrainingHistory.getTrainingDate()));
                trainerTrainingHistoryDtos.add(trainerTrainingHistoryDto);
            }
        }

        return new ResponseEntity<List<TrainerTrainingHistoryDto>>(trainerTrainingHistoryDtos, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> approveScheduleTraining(Long scheduleTrainingId, Optional<User> userOptional) {

        Optional<ScheduleTraining> scheduleTrainingOptional = scheduleTrainingRepository.findById(scheduleTrainingId);

        if (!scheduleTrainingOptional.isPresent()) {
            return new ResponseEntity<Status>(new Status("400", "ScheduleTraining is not present!!!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (scheduleTrainingOptional.get().getStatus() == null) {
            return new ResponseEntity<Status>(new Status("400", "ScheduleTraining status is null!!!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!scheduleTrainingOptional.get().getStatus().equals("Pending")) {
            return new ResponseEntity<Status>(
                    new Status("400", "ScheduleTraining status must be Pending before approval!!!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (scheduleTrainingOptional.get().getTrainingBranch().getId() == null) {
            return new ResponseEntity<Status>(new Status("400", "TrainingBranch id is null"), HttpStatus.BAD_REQUEST);
        }

        // save in TrainingUserRegistrationMapping based on skill level
        if (scheduleTrainingOptional.get().getTrainingType().equals("Departmental Training")) {
            List<Long> idList = skillMatrixGradeLevelRepository.getLowSkillLevelUserIdList(
                    scheduleTrainingOptional.get().getTrainingBranch().getId(),
                    scheduleTrainingOptional.get().getDepartment().getId(),
                    scheduleTrainingOptional.get().getDesignation().getId(),
                    scheduleTrainingOptional.get().getSkills().getId());

            for (Long id : idList) {
                TrainingUserRegistrationMapping trainingUserRegistrationMapping = new TrainingUserRegistrationMapping();
                User user = new User();
                user.setId(id);
                trainingUserRegistrationMapping.setUser(user);
                trainingUserRegistrationMapping.setScheduleTraining(scheduleTrainingOptional.get());
                trainingUserRegistrationMapping.setRegistrationStatus("Pending");
                trainingUserRegistrationMapping.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                trainingUserRegistrationMapping.setCreatedBy(userOptional.get().getId());
                trainingUserRegistrationMappingRepository.save(trainingUserRegistrationMapping);
            }
        }

        // save in TrainingUserRegistrationMapping based on TrainingUserLevelMapping &
        // TrainingEmployeeCategoryMapping
        else if (scheduleTrainingOptional.get().getTrainingType()
                .equalsIgnoreCase("Promotional Training")
                || scheduleTrainingOptional.get().getTrainingType()
                .equalsIgnoreCase("Industrial Training")) {

            if (scheduleTrainingOptional.get().getTrainingUserLevelMappings() != null
                    && scheduleTrainingOptional.get().getTrainingUserLevelMappings().isEmpty()) {
                return new ResponseEntity<Status>(new Status("400", "TrainingUserLevelMappings List is empty"),
                        HttpStatus.BAD_REQUEST);
            }

            if (scheduleTrainingOptional.get().getTrainingEmployeeCategoryMappings() != null
                    && scheduleTrainingOptional.get().getTrainingEmployeeCategoryMappings().isEmpty()) {
                return new ResponseEntity<Status>(new Status("400", "TrainingEmployeeCategoryMappings List is empty"),
                        HttpStatus.BAD_REQUEST);
            }

            Set<String> userLevelSet = new LinkedHashSet<>();
            Set<String> categorySet = new LinkedHashSet<>();
            scheduleTrainingOptional.get().getTrainingUserLevelMappings().forEach(data -> {
                if (data.getUserLevel() != null) {
                    userLevelSet.add(data.getUserLevel());
                }
            });

            scheduleTrainingOptional.get().getTrainingEmployeeCategoryMappings().forEach(data -> {
                if (data.getEmployeeCategory() != null) {
                    categorySet.add(data.getEmployeeCategory());
                }
            });

            Set<Long> userIdSet = userRepository.findByUserLevelInANDEmployeeCategoryIn(
                    scheduleTrainingOptional.get().getTrainingBranch().getId(), userLevelSet, categorySet);

            for (Long id : userIdSet) {
                TrainingUserRegistrationMapping trainingUserRegistrationMapping = new TrainingUserRegistrationMapping();
                User user = new User();
                user.setId(id);
                trainingUserRegistrationMapping.setUser(user);
                trainingUserRegistrationMapping.setScheduleTraining(scheduleTrainingOptional.get());
                trainingUserRegistrationMapping.setRegistrationStatus("Pending");
                trainingUserRegistrationMapping.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                trainingUserRegistrationMapping.setCreatedBy(userOptional.get().getId());
                trainingUserRegistrationMappingRepository.save(trainingUserRegistrationMapping);
            }
        } else {
            return new ResponseEntity<Status>(new Status("400", "Invalid TrainingType"), HttpStatus.BAD_REQUEST);
        }

        scheduleTrainingOptional.get().setStatus("Scheduled");
        scheduleTrainingOptional.get().setUpdatedBy(userOptional.get().getId());
        scheduleTrainingOptional.get().setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        scheduleTrainingRepository.save(scheduleTrainingOptional.get());
        return new ResponseEntity<Status>(new Status("200", "ScheduleTraining approved successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> changeScheduleTrainingStatus(Long scheduleTrainingId, String status) {
        Optional<ScheduleTraining> scheduleTrainingOptional = scheduleTrainingRepository.findById(scheduleTrainingId);

        if (!(status.equals("InProgress") || (status.equals("Completed. Release Pending") || status.equals("Released")
                || status.equals("Evaluation & Feedback Pending") || status.equals("All Completed")))) {
            return new ResponseEntity<Status>(new Status("400",
                    "ScheduleTraining requested status must be either \"InProgress\" or \"Completed. Release Pending\" or \"Released\" or \"Evaluation & Feedback Pending\" or \"All Completed\""),
                    HttpStatus.BAD_REQUEST);
        } else if (status.equals("InProgress")) {
            if (scheduleTrainingOptional.isPresent() && scheduleTrainingOptional.get().getStatus() != null
                    && scheduleTrainingOptional.get().getStatus().equals("Scheduled")) {
                scheduleTrainingOptional.get().setStatus("InProgress");
            } else {
                return new ResponseEntity<Status>(new Status("400",
                        "ScheduleTraining status must be as \"Scheduled\" to change the status as \"InProgress\""),
                        HttpStatus.BAD_REQUEST);
            }
        } else if (status.equals("Completed. Release Pending")) {
            if (scheduleTrainingOptional.isPresent() && scheduleTrainingOptional.get().getStatus() != null
                    && scheduleTrainingOptional.get().getStatus().equals("InProgress")) {
                scheduleTrainingOptional.get().setStatus("Completed. Release Pending");
            } else {
                return new ResponseEntity<Status>(new Status("400",
                        "ScheduleTraining status must be as \"InProgress\" to change the status as \"Completed. Release Pending\""),
                        HttpStatus.BAD_REQUEST);
            }
        } else if (status.equals("Released")) {
            if (scheduleTrainingOptional.isPresent() && scheduleTrainingOptional.get().getStatus() != null
                    && scheduleTrainingOptional.get().getStatus().equals("Completed. Release Pending")) {
                scheduleTrainingOptional.get().setStatus("Released");
            } else {
                return new ResponseEntity<Status>(new Status("400",
                        "ScheduleTraining status must be as \"Completed. Release Pending\" to change the status as \"Released\""),
                        HttpStatus.BAD_REQUEST);
            }
        } else if (status.equals("Evaluation & Feedback Pending")) {
            if (scheduleTrainingOptional.isPresent() && scheduleTrainingOptional.get().getStatus() != null
                    && scheduleTrainingOptional.get().getStatus().equals("Released")) {
                scheduleTrainingOptional.get().setStatus("Evaluation & Feedback Pending");
            } else {
                return new ResponseEntity<Status>(new Status("400",
                        "ScheduleTraining status must be as \"Released\" to change the status as \"Evaluation & Feedback Pending\""),
                        HttpStatus.BAD_REQUEST);
            }
        }

//		else if (status.equals("All completed")) {
//			if (scheduleTrainingOptional.isPresent() && scheduleTrainingOptional.get().getStatus() != null
//					&& scheduleTrainingOptional.get().getStatus().equals("Evaluation & Feedback Pending")) {
//				scheduleTrainingOptional.get().setStatus("All completed");
//			} else {
//				return new ResponseEntity<Status>(new Status("400",
//						"ScheduleTraining status must be as \"Evaluation & Feedback Pending\" to change the status as \"All completed\""),
//						HttpStatus.BAD_REQUEST);
//			}
//		}

        scheduleTrainingRepository.save(scheduleTrainingOptional.get());
        return new ResponseEntity<Status>(new Status("200", "ScheduleTraining status is changed successfully!!!"),
                HttpStatus.OK);
    }

    // Training Details

    // Training Register

    public ResponseEntity<?> getFilterListScheduleTrainingForRegistration(String trainingType, String trainingTopic,
                                                                          String dateFrom, String dateTo, Pageable pageable, User loggedUser) throws UsernameNotFoundException {

        Long userId = null, branchId = null, departmentId = null, designationId = null;

        userId = loggedUser.getId();

        if (loggedUser.getBranch_id() != null) {
            branchId = loggedUser.getBranch_id();
        }
        if (loggedUser.getDepartment() != null) {
            departmentId = loggedUser.getDepartment().getId();
        }
        if (loggedUser.getDesignation() != null) {
            designationId = loggedUser.getDesignation().getId();
        }

        HashMap<String, Date> dateHashMap = dateDynamic(dateFrom, dateTo);

        Page<ScheduleTrainingList> list = scheduleTrainingRepository
                .getFilterListScheduleTrainingForRegistration(branchId, trainingType, trainingTopic,
                        dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"), pageable);

        if (!list.toList().isEmpty()) {
            List<ScheduleTrainingListDto> result = list.toList().stream().map(data -> new ScheduleTrainingListDto(data)).collect(Collectors.toList());
            PageDataDto<ScheduleTrainingListDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<ScheduleTrainingListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ScheduleTrainingListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveTrainingRegistration(Long scheduleTrainingId, User loggedUser) {

        Optional<ScheduleTraining> scheduleTraining2Optional = scheduleTrainingRepository.findById(scheduleTrainingId);

        if (scheduleTraining2Optional.isPresent() && scheduleTraining2Optional.get().getStatus() != null
                && scheduleTraining2Optional.get().getStatus().equalsIgnoreCase("Scheduled")) {

            if (scheduleTraining2Optional.get().getRemainingSeats() != null
                    && scheduleTraining2Optional.get().getRemainingSeats() >= 1) {
                scheduleTraining2Optional.get()
                        .setRemainingSeats(scheduleTraining2Optional.get().getRemainingSeats() - 1);
            } else {
                return new ResponseEntity<Status>(new Status("400", "No remaining seats"), HttpStatus.BAD_REQUEST);
            }

            Optional<TrainingUserRegistrationMapping> trainingUserRegistrationMapping = trainingUserRegistrationMappingRepository
                    .findByScheduleTrainingIdANDUserId(scheduleTraining2Optional.get().getId(), loggedUser.getId());

            if (!trainingUserRegistrationMapping.isPresent()) {
                return new ResponseEntity<Status>(new Status("400",
                        "Invalid: userId is not present in TrainingUserRegistrationMapping for the requested scheduleTrainingId"),
                        HttpStatus.BAD_REQUEST);
            }


            trainingUserRegistrationMapping.get()
                    .setRegistrationDate(dateUtil.getLocalDateTimeToDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
            trainingUserRegistrationMapping.get().setRegistrationStatus("Registered");
            trainingUserRegistrationMapping.get().setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            trainingUserRegistrationMapping.get().setUpdatedBy(loggedUser.getId());
            trainingUserRegistrationMappingRepository.save(trainingUserRegistrationMapping.get());

            if (scheduleTraining2Optional.get().getTotalRegisteredEmployees() != null) {
                scheduleTraining2Optional.get()
                        .setTotalRegisteredEmployees(scheduleTraining2Optional.get().getTotalRegisteredEmployees() + 1);
            } else {
                scheduleTraining2Optional.get().setTotalRegisteredEmployees(1);
            }

            scheduleTrainingRepository.save(scheduleTraining2Optional.get());

            // For only Promotional Training Type
            if (scheduleTraining2Optional.get().getTrainingType() != null
                    && scheduleTraining2Optional.get().getTrainingType() != null
                    && scheduleTraining2Optional.get().getTrainingType()
                    .equalsIgnoreCase("Promotional Training")) {
                TraineeExamScore traineeExamScore = new TraineeExamScore();
                traineeExamScore.setTrainingUserRegistrationMapping(trainingUserRegistrationMapping.get());
                traineeExamScore.setScheduleTrainingId(scheduleTrainingId);
                traineeExamScore.setEvaluationStatus("Evaluation Pending");

                traineeExamScoreRepository.save(traineeExamScore);
            }

            return new ResponseEntity<Status>(new Status("200", "TrainingRegistration saved successfully!!!"),
                    HttpStatus.OK);
        } else if (!scheduleTraining2Optional.isPresent()) {
            return new ResponseEntity<Status>(
                    new Status("400", "ScheduleTraining with id value: " + scheduleTrainingId + " is not present"),
                    HttpStatus.BAD_REQUEST);
        } else if (scheduleTraining2Optional.get().getStatus() == null) {
            return new ResponseEntity<Status>(new Status("500", "ScheduleTraining status is null"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } else if (!scheduleTraining2Optional.get().getStatus().equalsIgnoreCase("Scheduled")) {
            return new ResponseEntity<Status>(
                    new Status("400", "Invalid: ScheduleTraining status must be Scheduled to be updated"),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Status>(new Status("500", "TrainingRegistration is not saved successfully"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<?> getRegisteredUserList(Long scheduleTrainingId, User loggedInuser) {

        List<IRegisteredUserListScheduleTraining> iRegisteredUserListScheduleTrainingList = trainingUserRegistrationMappingRepository
                .findByScheduleTrainingId(scheduleTrainingId);

        return new ResponseEntity<List<RegisteredUserListScheduleTrainingDto>>(
                iRegisteredUserListScheduleTrainingList.stream().map(data -> {
                    RegisteredUserListScheduleTrainingDto registeredUserListScheduleTrainingDto = new RegisteredUserListScheduleTrainingDto(
                            data);

                    registeredUserListScheduleTrainingDto
                            .setRegistrationDate(dateUtil.getDateToStringDDMMYYYY(data.getRegistrationDate()));

                    return registeredUserListScheduleTrainingDto;
                }).collect(Collectors.toList()), HttpStatus.OK);
    }

    // Exam

    @Transactional
    public ResponseEntity<?> saveTraineeAnswerSheet(SubmitExamDto submitExamDto, User loggedInUser) {

        Optional<TrainingUserRegistrationMapping> trainingUserRegistrationMappingOptional = Optional.empty();

        if (submitExamDto.getTraineeAnswerMultipleChoiceQuestionDtos() != null
                & !submitExamDto.getTraineeAnswerMultipleChoiceQuestionDtos().isEmpty()
                && submitExamDto.getTraineeAnswerOpenEndedQuestionDtos() != null
                & !submitExamDto.getTraineeAnswerOpenEndedQuestionDtos().isEmpty()) {

            if (submitExamDto.getScheduleTrainingId() != null) {
                trainingUserRegistrationMappingOptional = trainingUserRegistrationMappingRepository
                        .findByScheduleTrainingIdANDUserId(submitExamDto.getScheduleTrainingId(), loggedInUser.getId());
            } else {
                return new ResponseEntity<Status>(new Status("400", "Invalid: ScheduleTrainingId is null"),
                        HttpStatus.BAD_REQUEST);
            }

            if (!trainingUserRegistrationMappingOptional.isPresent()) {
                return new ResponseEntity<Status>(
                        new Status("500", "Invalid: trainingUserRegistrationMappingId is null"),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            TraineeExamScore traineeExamScore = new TraineeExamScore();
            traineeExamScore.setTrainingUserRegistrationMapping(trainingUserRegistrationMappingOptional.get());
            traineeExamScore.setScheduleTrainingId(submitExamDto.getScheduleTrainingId());
            traineeExamScore.setEvaluationStatus("Evaluation Pending");

            TraineeExamScore result = traineeExamScoreRepository.save(traineeExamScore);

            List<TraineeAnswerMultipleChoiceQuestion> traineeAnswerMultipleChoiceQuestionList = submitExamDto
                    .getTraineeAnswerMultipleChoiceQuestionDtos().stream().map(data -> {
//						return new TraineeAnswerMultipleChoiceQuestion(data).setTraineeScore(result);

                        TraineeAnswerMultipleChoiceQuestion traineeAnswerMultipleChoiceQuestion = new TraineeAnswerMultipleChoiceQuestion(
                                data);
                        traineeAnswerMultipleChoiceQuestion.setTestExamScoreId(result.getId());
                        traineeAnswerMultipleChoiceQuestion
                                .setSubmittedDate(dateUtil.getLocalDateTimeToDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
                        return traineeAnswerMultipleChoiceQuestion;

                    }).collect(Collectors.toList());

            traineeAnswerMultipleChoiceQuestionRepository.saveAll(traineeAnswerMultipleChoiceQuestionList);

            List<TraineeAnswerOpenEndedQuestion> traineeAnswerOpenEndedQuestionList = submitExamDto
                    .getTraineeAnswerOpenEndedQuestionDtos().stream().map(data -> {
//						return new TraineeAnswerOpenEndedQuestion(data);

                        TraineeAnswerOpenEndedQuestion traineeAnswerOpenEndedQuestion = new TraineeAnswerOpenEndedQuestion(
                                data);
                        traineeAnswerOpenEndedQuestion.setTestExamScoreId(result.getId());
                        traineeAnswerOpenEndedQuestion
                                .setSubmittedDate(dateUtil.getLocalDateTimeToDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
                        return traineeAnswerOpenEndedQuestion;
                    }).collect(Collectors.toList());

            traineeAnswerOpenEndedQuestionRepository.saveAll(traineeAnswerOpenEndedQuestionList);
            return new ResponseEntity<Status>(new Status("200", "Answer sheet is saved successfully"), HttpStatus.OK);
        } else if (submitExamDto.getTraineeAnswerMultipleChoiceQuestionDtos() == null
                || submitExamDto.getTraineeAnswerMultipleChoiceQuestionDtos().isEmpty()) {
            return new ResponseEntity<Status>(
                    new Status("400", "Invalid: TraineeAnswerMultipleChoiceQuestionList is null or empty"),
                    HttpStatus.BAD_REQUEST);
        } else if (submitExamDto.getTraineeAnswerOpenEndedQuestionDtos() == null
                || !submitExamDto.getTraineeAnswerOpenEndedQuestionDtos().isEmpty()) {
            return new ResponseEntity<Status>(
                    new Status("400", "Invalid: TraineeAnswerOpenEndedQuestionList is null or empty"),
                    HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Status>(new Status("500", "Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Trainer Feedback score

    public ResponseEntity<?> getTrainerFeedbackScoreList(List<Long> trainerIdsList, User loggedInUser) {

        if (trainerIdsList != null && !trainerIdsList.isEmpty()) {
            List<TrainerFeedbackScore> trainerFeedbackScoreList = trainerFeedbackScoreRepository
                    .getTrainerFeedbackScoreListByTraineeIdtrainerDetailIn(trainerIdsList, loggedInUser.getId());

            DateUtil dateUtil = new DateUtil();
            List<TrainerFeedbackScoreDto> trainerFeedbackScoreDtos = trainerFeedbackScoreList.stream().map(data -> {
                return new TrainerFeedbackScoreDto(data, dateUtil);
            }).collect(Collectors.toList());

            return new ResponseEntity<List<TrainerFeedbackScoreDto>>(trainerFeedbackScoreDtos, HttpStatus.OK);
        }

        return new ResponseEntity<Status>(new Status("400", "Invalid: trainerIdsList is null or empty"),
                HttpStatus.BAD_REQUEST);
    }

    @Transactional
    public ResponseEntity<?> saveTrainerFeedbackScore(List<TrainerFeedbackScoreDto> trainerFeedbackScoreDtos,
                                                      User loggedInUser) {

        if (trainerFeedbackScoreDtos == null || trainerFeedbackScoreDtos.isEmpty()) {
            return new ResponseEntity<Status>(new Status("400", "trainerFeedbackScoreDtos is either null or empty"),
                    HttpStatus.OK);
        }

        Optional<TrainingUserRegistrationMapping> trainingUserRegistrationMappingOptional = trainingUserRegistrationMappingRepository
                .findByScheduleTrainingIdANDUserId(trainerFeedbackScoreDtos.get(0).getScheduleTrainingId(),
                        trainerFeedbackScoreDtos.get(0).getTraineeId());

        for (TrainerFeedbackScoreDto trainerFeedbackScoreDto : trainerFeedbackScoreDtos) {

            Optional<TrainerDetail> trainerDetailOptional = trainerDetailRepository.findByScheduleTrainingIdUserId(
                    trainerFeedbackScoreDto.getScheduleTrainingId(), trainerFeedbackScoreDto.getTrainerId());

            TrainerFeedbackScore trainerFeedbackScore = new TrainerFeedbackScore(trainerFeedbackScoreDto);

            TrainingUserRegistrationMapping trainingUserRegistrationMapping = new TrainingUserRegistrationMapping();
            trainingUserRegistrationMapping.setId(trainingUserRegistrationMappingOptional.get().getId());
            trainerFeedbackScore.setTrainingUserRegistrationMapping(trainingUserRegistrationMapping);

            TrainerDetail trainerDetail = new TrainerDetail();
            trainerDetail.setId(trainerDetailOptional.get().getId());
            trainerFeedbackScore.setTrainerDetail(trainerDetail);

            trainerFeedbackScore.setSubmittedDate(dateUtil.getLocalDateTimeToDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
            trainerFeedbackScore.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            trainerFeedbackScore.setCreatedBy(loggedInUser.getId());
            trainerFeedbackScoreRepository.save(trainerFeedbackScore);

            if (trainerDetailOptional.isPresent()) {
                if (trainerDetailOptional.get().getAverageScore() != null) {
                    trainerDetailOptional.get().setAverageScore(Float.sum(trainerDetailOptional.get().getAverageScore(),
                            trainerFeedbackScoreDto.getAverageScore()) / 2);
                } else {
                    trainerDetailOptional.get().setAverageScore(trainerFeedbackScoreDto.getAverageScore());
                }
                trainerDetailRepository.save(trainerDetailOptional.get());
            } else {
                throw new RuntimeException("Error:400, Invalid: TrainerDetail is not present with given id: "
                        + trainerFeedbackScoreDto.getId());
            }
        }

        int traineesCount = trainingUserRegistrationMappingRepository
                .countByScheduleTrainingId(trainerFeedbackScoreDtos.get(0).getScheduleTrainingId());
        int trainerCount = trainerDetailRepository
                .countByScheduleTrainingId(trainerFeedbackScoreDtos.get(0).getScheduleTrainingId());
        int feedbackCount = trainerFeedbackScoreRepository
                .countByScheduleTrainingId(trainerFeedbackScoreDtos.get(0).getScheduleTrainingId());

        if ((traineesCount * trainerCount) == feedbackCount) {
            Optional<ScheduleTraining> scheduleTrainingOptional = scheduleTrainingRepository
                    .findById(trainerFeedbackScoreDtos.get(0).getScheduleTrainingId());

            if (scheduleTrainingOptional.get().getStatus().equals("Evaluation & Feedback Pending")) {
                scheduleTrainingOptional.get().setStatus("Feedback Completed & Evaluation Pending");
                scheduleTrainingOptional.get().setUpdatedBy(loggedInUser.getId());
                scheduleTrainingOptional.get().setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                scheduleTrainingRepository.save(scheduleTrainingOptional.get());

                return new ResponseEntity<Status>(new Status("200",
                        "TrainerFeedbackScore saved successfully & Training status is changed as \"Feedback Completed & Evaluation Pending\"!!!"),
                        HttpStatus.OK);
            } else if (scheduleTrainingOptional.get().getStatus().equals("Evaluation Completed & Feedback Pending")) {
                scheduleTrainingOptional.get().setStatus("All Completed");
                scheduleTrainingOptional.get().setUpdatedBy(loggedInUser.getId());
                scheduleTrainingOptional.get().setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                scheduleTrainingRepository.save(scheduleTrainingOptional.get());

                return new ResponseEntity<Status>(new Status("200",
                        "TrainerFeedbackScore saved successfully & Training status is changed as \"All Completed\" !!!"),
                        HttpStatus.OK);
            }
        }

        return new ResponseEntity<Status>(new Status("200", "TrainerFeedbackScore saved successfully !!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> getTrainerFeedbackScore(Long traineeId, User loggedInUser) {

        List<TrainerFeedbackScore> trainerFeedbackScores = trainerFeedbackScoreRepository.findByTraineeId(traineeId);

        List<TrainerFeedbackScoreDto> trainerFeedbackScoreDtos = trainerFeedbackScores.stream().map(data -> {
            return new TrainerFeedbackScoreDto(data, dateUtil);
        }).collect(Collectors.toList());

        return new ResponseEntity<List<TrainerFeedbackScoreDto>>(trainerFeedbackScoreDtos, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteRegistration(Long scheduleTrainingId, Optional<User> userOptional)
            throws UsernameNotFoundException {

        if (scheduleTrainingId == null || !userOptional.isPresent()) {
            return new ResponseEntity<Status>(new Status("400", "scheduleTrainingId or userOptional is null"),
                    HttpStatus.BAD_REQUEST);
        }

        Optional<ScheduleTraining> scheduleTrainingOptional = scheduleTrainingRepository.findById(scheduleTrainingId);
        if (scheduleTrainingOptional.isPresent() && (scheduleTrainingOptional.get().getStatus().equals("Released")
                || scheduleTrainingOptional.get().getStatus().equals("Evaluation & Feedback Pending")
                || scheduleTrainingOptional.get().getStatus().equals("Evaluation Completed & Feedback Pending")
                || scheduleTrainingOptional.get().getStatus().equals("Feedback Completed & Evaluation Pending")
                || scheduleTrainingOptional.get().getStatus().equals("All Completed"))) {
            return new ResponseEntity<Status>(
                    new Status("400", "Registration can be deleted only before Training status of \"Released\""),
                    HttpStatus.BAD_REQUEST);
        }

        Optional<TrainingUserRegistrationMapping> trainingUserRegistrationMappingOptional = trainingUserRegistrationMappingRepository
                .findByScheduleTrainingIdANDUserId(scheduleTrainingId, userOptional.get().getId());
        if (trainingUserRegistrationMappingOptional.isPresent()) {
            try {
                trainingUserRegistrationMappingRepository.delete(trainingUserRegistrationMappingOptional.get());
            } catch (Exception ex) {
                throw new RuntimeException("CODE: 304, Could not Delete Record", ex);
            }

            if (scheduleTrainingOptional.get().getTotalRegisteredEmployees() != null
                    && scheduleTrainingOptional.get().getTotalRegisteredEmployees() > 0) {
                scheduleTrainingOptional.get()
                        .setTotalRegisteredEmployees(scheduleTrainingOptional.get().getTotalRegisteredEmployees() - 1);
            } else {
                scheduleTrainingOptional.get().setTotalRegisteredEmployees(0);
            }

            if (scheduleTrainingOptional.get().getRemainingSeats() != null) {
                scheduleTrainingOptional.get()
                        .setRemainingSeats(scheduleTrainingOptional.get().getRemainingSeats() + 1);
            } else {
                scheduleTrainingOptional.get().setRemainingSeats(1);
            }

            scheduleTrainingRepository.save(scheduleTrainingOptional.get());

            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        }

        throw new RuntimeException("ERROR:400, trainingUserRegistrationMappingOptional is not present");
    }

    // Evaluation

    public ResponseEntity<?> getFilterListScheduleTrainingForEvaluation(String trainingType, Long departmentId,
                                                                        Long designationId, String trainingTopic, String dateFrom, String dateTo, Pageable pageable,
                                                                        User loggedInUser) throws UsernameNotFoundException {

        Long branchId = loggedInUser.getBranch_id();
        Long userId = loggedInUser.getId();

        HashMap<String, Date> dateHashMap = dateDynamic(dateFrom, dateTo);

        if (userId == null || branchId == null) {
            return new ResponseEntity<Status>(new Status("500", "Error. LoggedIn user's userId or branchId is null"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

		Page<IScheduleTrainingFilter> list = scheduleTrainingRepository.getFilterListScheduleTrainingForEvaluation( branchId, departmentId, designationId, trainingType, trainingTopic, dateHashMap.get("dateFrom"), dateHashMap.get("dateTo"), pageable);

        if (!list.toList().isEmpty()) {
            List<ScheduleTrainingFilterDto> result = list.toList().stream().map(data -> new ScheduleTrainingFilterDto(data)).collect(Collectors.toList());
            PageDataDto<ScheduleTrainingFilterDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<ScheduleTrainingFilterDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ScheduleTrainingFilterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getTraineeExamScoreList(Long scheduleTrainingId, User loggedInUser) {

        LinkedHashSet<TraineeExamScore> traineeExamScoreSet = traineeExamScoreRepository
                .getTraineeExamScoreListByScheduleTrainingId(scheduleTrainingId, null);

        return ResponseEntity.ok().body((traineeExamScoreSet.stream().map(data -> {
            TraineeExamScoreDto traineeExamScoreDto = new TraineeExamScoreDto(data);
            traineeExamScoreDto.setEvaluationDate(dateUtil.getDateToStringDDMMYYYY(data.getEvaluationDate()));
            return traineeExamScoreDto;
        }).collect(Collectors.toCollection(LinkedHashSet::new))));
    }

    @Transactional
    public ResponseEntity<?> saveTraineeExamScore(String traineeExamScoreDtoString, MultipartFile[] answerSheets,
                                                  User loggedInUser) {

        ObjectMapper objectMapper = new ObjectMapper();
        List<TraineeExamScoreDto> traineeExamScoreDtos = new ArrayList<>();

        try {
            traineeExamScoreDtos = Arrays
                    .asList(objectMapper.readValue(traineeExamScoreDtoString, TraineeExamScoreDto[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("500, Internal server error", e);
        }

        int multipartFileIndex = 0;
        for (TraineeExamScoreDto traineeExamScoreDto : traineeExamScoreDtos) {

            Optional<TraineeExamScore> traineeExamScoreListOptional = traineeExamScoreRepository
                    .findById(traineeExamScoreDto.getId());

            if (!traineeExamScoreListOptional.isPresent()) {
                return new ResponseEntity<Status>(new Status("400", "TraineeExamScore is not present !!!"),
                        HttpStatus.BAD_REQUEST);
            } else if (traineeExamScoreListOptional.get().getEvaluationStatus().equals("Evaluation Completed")) {
                return new ResponseEntity<Status>(
                        new Status("400", "TraineeExamScore is already submitted by another trainer!!!"),
                        HttpStatus.BAD_REQUEST);
            }

            Optional<ScheduleTraining> scheduleTrainingOptional = scheduleTrainingRepository
                    .findById(traineeExamScoreDto.getScheduleTrainingId());

            Optional<TrainerDetail> trainerDetailOptional = trainerDetailRepository
                    .findByScheduleTrainingIdUserId(traineeExamScoreDto.getScheduleTrainingId(), loggedInUser.getId());

            if (!trainerDetailOptional.isPresent()) {
                return new ResponseEntity<Status>(
                        new Status("400", "trainerDetail is not present for scheduleTrainingId & loggedInUserId !!!"),
                        HttpStatus.BAD_REQUEST);
            }

            Optional<TrainingUserRegistrationMapping> trainingUserRegistrationMappingOptional = trainingUserRegistrationMappingRepository
                    .findByScheduleTrainingIdANDUserId(traineeExamScoreDto.getScheduleTrainingId(),
                            traineeExamScoreDto.getTraineeId());

            if (!trainingUserRegistrationMappingOptional.isPresent()) {
                return new ResponseEntity<Status>(new Status("400",
                        "trainingUserRegistrationMappingOptional is not present for scheduleTrainingId & trainee userId !!!"),
                        HttpStatus.BAD_REQUEST);
            }

            if (scheduleTrainingOptional.get().getTrainingType()
                    .equalsIgnoreCase("Departmental Training")) {

                TraineeExamScore traineeExamScore = new TraineeExamScore(traineeExamScoreDto);
                traineeExamScore.setTrainerDetail(trainerDetailOptional.get());
                traineeExamScore.setTrainingUserRegistrationMapping(trainingUserRegistrationMappingOptional.get());
                traineeExamScore.setEvaluationDate(dateUtil.getLocalDateTimeToDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
                traineeExamScore.setEvaluationStatus("Evaluation Completed");
                traineeExamScore.setCreatedBy(loggedInUser.getId());
                traineeExamScore.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                TraineeExamScore result1 = traineeExamScoreRepository.save(traineeExamScore);

                final TrainingUserRegistrationMapping trainingUserRegistrationMapping1 = trainingUserRegistrationMappingOptional
                        .get();

                List<TraineeAnswerMultipleChoiceQuestion> traineeAnswerMultipleChoiceQuestions = traineeExamScoreDto
                        .getTraineeAnswerMultipleChoiceQuestionDtos().stream().map(data -> {
                            TraineeAnswerMultipleChoiceQuestion traineeAnswerMultipleChoiceQuestion = new TraineeAnswerMultipleChoiceQuestion(
                                    data);
                            traineeAnswerMultipleChoiceQuestion.setTrainee(trainingUserRegistrationMapping1);
                            traineeAnswerMultipleChoiceQuestion
                                    .setEvaluationDate(dateUtil.getLocalDateTimeToDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
                            traineeAnswerMultipleChoiceQuestion.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                            traineeAnswerMultipleChoiceQuestion.setUpdatedBy(loggedInUser.getId());
                            return traineeAnswerMultipleChoiceQuestion;
                        }).collect(Collectors.toList());

                traineeAnswerMultipleChoiceQuestionRepository.saveAll(traineeAnswerMultipleChoiceQuestions);

                List<TraineeAnswerOpenEndedQuestion> traineeAnswerOpenEndedQuestions = traineeExamScoreDto
                        .getTraineeAnswerOpenEndedQuestionDtos().stream().map(data -> {
                            TraineeAnswerOpenEndedQuestion traineeAnswerOpenEndedQuestion = new TraineeAnswerOpenEndedQuestion(
                                    data);
                            traineeAnswerOpenEndedQuestion.setTrainee(trainingUserRegistrationMapping1);
                            traineeAnswerOpenEndedQuestion
                                    .setEvaluationDate(dateUtil.getLocalDateTimeToDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
                            traineeAnswerOpenEndedQuestion.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                            traineeAnswerOpenEndedQuestion.setUpdatedBy(loggedInUser.getId());
                            return traineeAnswerOpenEndedQuestion;
                        }).collect(Collectors.toList());

                traineeAnswerOpenEndedQuestionRepository.saveAll(traineeAnswerOpenEndedQuestions);

                // save skill matrix based on exam final score
                Long skillGradeLevel = null;
                if (traineeExamScoreDto.getTotalScore() != null) {
                    if (traineeExamScoreDto.getTotalScore() >= 80) {
                        skillGradeLevel = 1L;
                    }
                    if (traineeExamScoreDto.getTotalScore() >= 60 && traineeExamScoreDto.getTotalScore() < 80) {
                        skillGradeLevel = 2L;
                    }
                    if (traineeExamScoreDto.getTotalScore() >= 50 && traineeExamScoreDto.getTotalScore() < 60) {
                        skillGradeLevel = 3L;
                    }
                    if (traineeExamScoreDto.getTotalScore() < 50) {
                        skillGradeLevel = 4L;
                    }
                }

                final Long skillGradeLevelId = skillGradeLevel;

                //scheduleTrainingOptional = scheduleTrainingRepository.findById(traineeExamScoreDto.getScheduleTrainingId());

                List<SkillMatrix> skillMatrixList = skillMatrixRepository.getSkillMatrixList(
                        trainingUserRegistrationMappingOptional.get().getUser().getId(),
                        scheduleTrainingOptional.get().getTrainingBranch().getId(),
                        scheduleTrainingOptional.get().getDepartment().getId(),
                        scheduleTrainingOptional.get().getDesignation().getId(), null);

                SkillMatrix skillMatrix2 = new SkillMatrix();

                if (!skillMatrixList.isEmpty()) {
                    List<SkillMatrixGradeLevel> skillMatrixGradeLevelList1 = skillMatrixList.get(0)
                            .getSkillMatrixGradeLevels();

                    TraineeExamScore traineeExamScore1 = new TraineeExamScore();
                    traineeExamScore1.setId(result1.getId());
                    User user = new User();
                    user.setId(trainingUserRegistrationMappingOptional.get().getUser().getId());
                    skillMatrix2.setUser(user);
                    DepartmentDetails departmentDetails = new DepartmentDetails();
                    departmentDetails.setId(scheduleTrainingOptional.get().getDepartment().getId());
                    Designation designation = new Designation();
                    designation.setId(scheduleTrainingOptional.get().getDesignation().getId());
                    skillMatrix2.setDepartment(departmentDetails);
                    skillMatrix2.setDesignation(designation);
                    skillMatrix2.setDate(dateUtil.getLocalDateTimeToDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
                    skillMatrix2.setTraineeExamScore(traineeExamScore1);
                    skillMatrix2.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    skillMatrix2.setCreatedBy(loggedInUser.getId());
                    SkillMatrix result = skillMatrixRepository.save(skillMatrix2);

                    final Optional<ScheduleTraining> scheduleTrainingOptionalFinal = scheduleTrainingOptional;
                    List<SkillMatrixGradeLevel> skillMatrixGradeLevelList2 = skillMatrixGradeLevelList1.stream()
                            .map(data -> {
                                SkillMatrixGradeLevel skillMatrixGradeLevel = new SkillMatrixGradeLevel();

                                SkillGradeLevel skillGradeLevel2 = new SkillGradeLevel();

                                if (data.getSkills().getId()
                                        .equals(scheduleTrainingOptionalFinal.get().getSkills().getId())) {
                                    skillGradeLevel2.setId(skillGradeLevelId);
                                    skillMatrixGradeLevel.setSkillGradeLevel(skillGradeLevel2);
                                } else {
                                    skillMatrixGradeLevel.setSkillGradeLevel(data.getSkillGradeLevel());
                                }

                                skillMatrixGradeLevel.setSkillMatrixId(result.getId());
                                skillMatrixGradeLevel.setSkills(data.getSkills());
                                skillMatrixGradeLevel.setCreatedBy(loggedInUser.getId());
                                skillMatrixGradeLevel.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                                return skillMatrixGradeLevel;
                            }).collect(Collectors.toList());

                    if (!skillMatrixGradeLevelList2.isEmpty()) {
//				for (SkillMatrixGradeLevel skillMatrixGradeLevel : skillMatrixGradeLevelList2) {
//					skillMatrixGradeLevel.setSkillMatrixId(result2.getId());
//					skillMatrixGradeLevelRepository.save(skillMatrixGradeLevel);
//				}

                        skillMatrixGradeLevelRepository.saveAll(skillMatrixGradeLevelList2);
                    }
                } else {
                    throw new RuntimeException("Error:500, skillMatrixList is empty");
                }
            } else if (scheduleTrainingOptional.get().getTrainingType()
                    .equalsIgnoreCase("Promotional Training")) {

                Path answerSheetFilePath = null;
                StringBuilder filename = new StringBuilder();
                String contentType = null;
                String fullFileName = null;
                if (traineeExamScoreDto.getIsAnswerSheetAttached()) {
                    // upload offline exam answer sheet

                    if (answerSheets[multipartFileIndex] == null) {
                        return new ResponseEntity<Status>(new Status("400", "answerSheetMultipart is null !!!"),
                                HttpStatus.BAD_REQUEST);
                    }

                    if (answerSheets[multipartFileIndex].getContentType() == null) {
                        contentType = answerSheets[multipartFileIndex].getContentType();
                        return new ResponseEntity<Status>(
                                new Status("400", "answerSheetMultipart Content Type is null !!!"),
                                HttpStatus.BAD_REQUEST);
                    }

                    if (!answerSheets[multipartFileIndex].getContentType().equals("application/pdf")) {
                        return new ResponseEntity<Status>(
                                new Status("400", "answerSheetMultipart content type is not pdf !!!"),
                                HttpStatus.BAD_REQUEST);
                    }

                    if (answerSheets[multipartFileIndex].getOriginalFilename() == null) {
                        return new ResponseEntity<Status>(
                                new Status("400", "answerSheetMultipart file name is null !!!"),
                                HttpStatus.BAD_REQUEST);
                    }

                    if (answerSheets[multipartFileIndex].getOriginalFilename().contains("..")) {
                        return new ResponseEntity<Status>(new Status("400", "Invalid file name !!!"),
                                HttpStatus.BAD_REQUEST);
                    }

                    Path root = Paths.get(answerSheetUploadPath);

                    if (!Files.exists(root)) {
                        try {
                            Files.createDirectories(root);
                        } catch (IOException e) {
                            throw new RuntimeException("500: Exception when creating answer sheet upload directory", e);
                        }
                    }

                    DateTimeFormatter customFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss");
                    if (answerSheets[multipartFileIndex].getOriginalFilename() != null) {
                        filename.append(StringUtils.cleanPath(answerSheets[multipartFileIndex].getOriginalFilename()));
                    }
                    fullFileName = traineeExamScoreDto.getId() + "_" + LocalDateTime.now(ZoneId.of("Asia/Kolkata")).format(customFormat) + "_"
                            + filename.toString();
                    answerSheetFilePath = root.resolve(fullFileName);

                    try {
                        Files.copy(answerSheets[multipartFileIndex].getInputStream(), answerSheetFilePath,
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException("500: Exception when copying answer sheet to upload directory", e);
                    }
                    // uploading offline exam answer sheet is optional
                    multipartFileIndex++;
                }

                TraineeExamScore traineeExamScore = new TraineeExamScore(traineeExamScoreDto);
                traineeExamScore.setTrainerDetail(trainerDetailOptional.get());
                traineeExamScore.setTrainingUserRegistrationMapping(trainingUserRegistrationMappingOptional.get());
                traineeExamScore.setAnswerSheetUploadPath(answerSheetFilePath.toString());
                traineeExamScore.setAnswerSheetFileName(fullFileName);
                traineeExamScore.setAnswerSheetFileType(contentType);
                traineeExamScore.setEvaluationDate(dateUtil.getLocalDateTimeToDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata"))));
                traineeExamScore.setEvaluationStatus("Evaluation Completed");
                traineeExamScore.setCreatedBy(loggedInUser.getId());
                traineeExamScore.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                traineeExamScoreRepository.save(traineeExamScore);
            } else {
                return new ResponseEntity<Status>(
                        new Status("400",
                                "TraineeExamScore submission is not applicable for the requested Training Type !!!"),
                        HttpStatus.BAD_REQUEST);
            }

            List<TraineeExamScore> traineeExamScoreList = traineeExamScoreRepository
                    .findAllTrainingExamScoreWithEvaluationPendingStatus(traineeExamScoreDto.getScheduleTrainingId(),
                            PageRequest.of(0, 1));

            if (traineeExamScoreList.isEmpty()) {
                if (scheduleTrainingOptional.get().getStatus().equals("Evaluation & Feedback Pending")) {
                    //Optional<ScheduleTraining> scheduleTrainingOptional = scheduleTrainingRepository.findById(traineeExamScoreDto.getScheduleTrainingId());
                    scheduleTrainingOptional.get().setStatus("Evaluation Completed & Feedback Pending");
                    scheduleTrainingOptional.get().setUpdatedBy(loggedInUser.getId());
                    scheduleTrainingOptional.get().setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    scheduleTrainingRepository.save(scheduleTrainingOptional.get());

                    return new ResponseEntity<Status>(new Status("200",
                            "TraineeExamScore saved successfully & Training status is changed as Evaluation Completed & Feedback Pending!!!"),
                            HttpStatus.OK);
                } else if (scheduleTrainingOptional.get().getStatus()
                        .equals("Feedback Completed & Evaluation Pending")) {
                    scheduleTrainingOptional.get().setStatus("All Completed");
                    scheduleTrainingOptional.get().setUpdatedBy(loggedInUser.getId());
                    scheduleTrainingOptional.get().setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    scheduleTrainingRepository.save(scheduleTrainingOptional.get());

                    return new ResponseEntity<Status>(new Status("200",
                            "TraineeExamScore saved successfully & Training status is changed as All Completed !!!"),
                            HttpStatus.OK);
                }
            }
        }

        return new ResponseEntity<Status>(new Status("200", "TraineeExamScore saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> getEligibleTraineesCount(Long branchId, List<String> userLevelList,
                                                      List<String> categoryList) {

        Set<String> userLevelSet = null;
        Set<String> categorySet = null;

        if (userLevelList != null && !userLevelList.isEmpty()) {
            userLevelSet = userLevelList.stream().map(data -> {
                return data;
            }).collect(Collectors.toCollection(LinkedHashSet::new));
        }

        if (categoryList != null && !categoryList.isEmpty()) {
            categorySet = categoryList.stream().map(data -> {
                return data;
            }).collect(Collectors.toCollection(LinkedHashSet::new));
        }

        Set<Long> userIdSet = userRepository.findByUserLevelInANDEmployeeCategoryIn(branchId, userLevelSet,
                categorySet);

        return new ResponseEntity<Integer>(userIdSet.size(), HttpStatus.OK);
    }

    public ResponseEntity<?> getTraineeExamScore(Long scheduleTrainingId, User loggedInUser) {
        LinkedHashSet<TraineeExamScore> traineeExamScoreSet = traineeExamScoreRepository
                .getTraineeExamScoreListByScheduleTrainingId(scheduleTrainingId, loggedInUser.getId());

//		return new ResponseEntity<LinkedHashSet<TraineeExamScoreDto>>(traineeExamScoreSet.stream().map(data -> {
//			TraineeExamScoreDto traineeExamScoreDto = new TraineeExamScoreDto(data);
//			traineeExamScoreDto.setEvaluationDate(dateUtil.getDateToStringDDMMYYYY(data.getEvaluationDate()));
//			return traineeExamScoreDto;
//		}).collect(Collectors.toCollection(LinkedHashSet::new)), HttpStatus.OK);

        return ResponseEntity.ok().body((traineeExamScoreSet.stream().map(data -> {
            TraineeExamScoreDto traineeExamScoreDto = new TraineeExamScoreDto(data);
            traineeExamScoreDto.setEvaluationDate(dateUtil.getDateToStringDDMMYYYY(data.getEvaluationDate()));
            return traineeExamScoreDto;
        }).collect(Collectors.toCollection(LinkedHashSet::new))));
    }

    public ResponseEntity<byte[]> downloadAnswerSheet(Long traineeExamScoreId, String fileName, User user,
                                                      HttpServletRequest httpServletRequest) {

        byte[] filecontent = null;
        File file = new File(answerSheetUploadPath + File.separator + fileName);
        String fileAbsolutePath = file.getAbsolutePath();

        try {
            filecontent = FileUtils.readFileToByteArray(new File(fileAbsolutePath));
        } catch (IOException e) {
            throw new RuntimeException("IOException", e);
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").body(filecontent);
    }

    public ResponseEntity<byte[]> viewAnswerSheet(Long traineeExamScoreId, String fileName, User user,
                                                  HttpServletRequest httpServletRequest) {

        byte[] filecontent = null;
        File file = new File(answerSheetUploadPath + File.separator + fileName);
        String fileAbsolutePath = file.getAbsolutePath();

        try {
            filecontent = FileUtils.readFileToByteArray(new File(fileAbsolutePath));
        } catch (IOException e) {
            throw new RuntimeException("IOException", e);
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"").body(filecontent);
    }

    public ResponseEntity<?> testFileUploadApi(String traineeExamScoreDtoString, MultipartFile answerSheetMultipart,
                                               User user) {

        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder filename = new StringBuilder();

        try {
            MyTestDto myTestDto = objectMapper.readValue(traineeExamScoreDtoString, MyTestDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("500, Internal server error", e);
        }

        if (answerSheetMultipart == null) {
            return new ResponseEntity<Status>(new Status("400", "answerSheetMultipart is null !!!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (answerSheetMultipart.getContentType() == null) {
            return new ResponseEntity<Status>(new Status("400", "answerSheetMultipart Content Type is null !!!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!answerSheetMultipart.getContentType().equals("application/pdf")) {
            return new ResponseEntity<Status>(new Status("400", "answerSheetMultipart content type is not pdf !!!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (answerSheetMultipart.getOriginalFilename() == null) {
            return new ResponseEntity<Status>(new Status("400", "answerSheetMultipart file name is null !!!"),
                    HttpStatus.BAD_REQUEST);
        }

        if (answerSheetMultipart.getOriginalFilename().contains("..")) {
            return new ResponseEntity<Status>(new Status("400", "Invalid file name !!!"), HttpStatus.BAD_REQUEST);
        }

        Path root = Paths.get(answerSheetUploadPath);

        if (!Files.exists(root)) {
            try {
                Files.createDirectories(root);
            } catch (IOException e) {
                throw new RuntimeException("500: Exception when creating answer sheet upload directory", e);
            }
        }

        DateTimeFormatter customFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm_ss");

        if (answerSheetMultipart.getOriginalFilename() != null) {
            filename.append(StringUtils.cleanPath(answerSheetMultipart.getOriginalFilename()));
        }

        Path a1 = root.resolve("1_" + LocalDateTime.now(ZoneId.of("Asia/Kolkata")).format(customFormat) + "_" + filename.toString());
        String a1String = a1.toString();
        Path a1Absolute = a1.toAbsolutePath();
        String a1AbsoluteString = a1Absolute.toString();

        try {
            Files.copy(answerSheetMultipart.getInputStream(),
                    root.resolve("1_" + LocalDateTime.now(ZoneId.of("Asia/Kolkata")).format(customFormat) + "_" + filename.toString()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("500: Exception when copying answer sheet to upload directory", e);
        }

        return null;
    }

    public ResponseEntity<Resource> testDownloadApi(String fileName, User user, HttpServletRequest httpServletRequest) {

        Resource resource = null;
        try {
            Path root = Paths.get(answerSheetUploadPath);
//			Path rootAbsolute = root.toAbsolutePath();
            Path filePath = root.resolve(fileName).normalize();
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException ex) {
            throw new RuntimeException("MalformedURLException", ex);
        }

        String contentType = null;
        try {
            contentType = httpServletRequest.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            throw new RuntimeException("400: File not found " + fileName, ex);
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        if (resource.exists()) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            throw new RuntimeException("400: File not found " + fileName);
        }
    }
}
