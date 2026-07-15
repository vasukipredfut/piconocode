package com.aria.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import org.springframework.web.multipart.MultipartFile;

import com.aria.dao.UserRepository;
import com.aria.dto.BillInwardDto;
import com.aria.dto.ScheduleTrainingDto;
import com.aria.dto.SkillMatrixDto;
import com.aria.dto.SubmitExamDto;
import com.aria.dto.TrainerFeedbackScoreDto;
import com.aria.model.User;
import com.aria.service.TrainingService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class TrainingController {

//	private static String answerSheetFilePath = "/answer_sheet/";

	@Autowired
	TrainingService trainingService;

	@Autowired
	UserRepository userRepository;

	// skill matrix

	@PostMapping("/saveSkillMatrix")
	public ResponseEntity<?> saveSkillMatrix(@RequestBody SkillMatrixDto skillMatrixDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.saveSkillMatrix(skillMatrixDto, user.get());
	}

	@PutMapping("/updateSkillMatrix")
	public ResponseEntity<?> updateSkillMatrix(@RequestBody SkillMatrixDto skillMatrixDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.updateSkillMatrix(skillMatrixDto, user.get());
	}

	@GetMapping("/listAllSkillMatrix")
	public ResponseEntity<?> listAllSkillMatrix(Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
		if (userOptional.isPresent()) {
			return trainingService.listAllSkillMatrix();
		} else {
			return new ResponseEntity<List<SkillMatrixDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/filterListSkillMatrix")
	public ResponseEntity<?> filterListSkillMatrix(@RequestParam(value = "branchId", required = false) Long branchId,
			@RequestParam(value = "departmentId", required = false) Long departmentId,
			@RequestParam(value = "designationId", required = false) Long designationId,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo,
			Pageable pageable,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
		if (userOptional.isPresent()) {
			return trainingService.filterListSkillMatrix(userOptional.get(), branchId, departmentId, designationId,
					dateFrom, dateTo,pageable);
		} else {
			return new ResponseEntity<List<SkillMatrixDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/allSkillMatrixFilter")
	public ResponseEntity<?> allSkillMatrixFilter(@RequestParam(value = "branchId", required = false) Long branchId,
												   @RequestParam(value = "departmentId", required = false) Long departmentId,
												   @RequestParam(value = "designationId", required = false) Long designationId,
												   @RequestParam(value = "dateFrom", required = false) String dateFrom,
												   @RequestParam(value = "dateTo", required = false) String dateTo,
												   Pageable pageable,
												   Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
		if (userOptional.isPresent()) {
			return trainingService.allSkillMatrixFilter(userOptional.get(), branchId, departmentId, designationId,
					dateFrom, dateTo, pageable);
		} else {
			return new ResponseEntity<List<SkillMatrixDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getSkillMatrixList/{branchId}/{departmentId}/{designationId}")
	public ResponseEntity<?> getSkillMatrixList(@PathVariable(value = "branchId") Long branchId,
			@PathVariable(value = "departmentId") Long departmentId,
			@PathVariable(value = "designationId") Long designationId,
			@RequestParam(value = "date", required = false) String date,
			@RequestParam(value = "isFilterForOnlyLoggedInUser", required = true) Boolean isFilterForOnlyLoggedInUser,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
		if (userOptional.isPresent()) {
			return trainingService.getSkillMatrixList(userOptional, branchId, departmentId, designationId, date,
					isFilterForOnlyLoggedInUser);
		} else {
			return new ResponseEntity<List<SkillMatrixDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getSkillMatrixListWithSingleSkill/{branchId}/{departmentId}/{designationId}/{skillId}")
	public ResponseEntity<?> getSkillMatrixListWithSingleSkill(@PathVariable(value = "branchId") Long branchId,
			@PathVariable(value = "departmentId") Long departmentId,
			@PathVariable(value = "designationId") Long designationId, @PathVariable(value = "skillId") Long skillId,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
		if (userOptional.isPresent()) {
			return trainingService.getSkillMatrixListWithSingleSkill(branchId, departmentId, designationId, skillId);
		} else {
			return new ResponseEntity<List<SkillMatrixDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getLowSkillLevelUserCount/{branchId}/{departmentId}/{designationId}")
	public ResponseEntity<?> getLowSkillLevelUserCount(@PathVariable(value = "branchId") Long branchId,
			@PathVariable(value = "departmentId") Long departmentId,
			@PathVariable(value = "designationId") Long designationId,
			@RequestParam(value = "dateTo", required = false) String dateTo, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
		if (userOptional.isPresent()) {
			return trainingService.getLowSkillLevelUserCount(branchId, departmentId, designationId, dateTo);
		} else {
			return new ResponseEntity<List<SkillMatrixDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getSkillMatrix/{id}")
	public ResponseEntity<?> getSkillMatrix(@PathVariable String id, Authentication authentication) {
		return trainingService.getSkillMatrix(id);
	}

	@DeleteMapping("/deleteSkillMatrix/{id}")
	public ResponseEntity<?> deleteSkillMatrix(@PathVariable String id, Authentication authentication) {
		return trainingService.deleteSkillMatrix(id);
	}

	// Schedule Training

	@PostMapping("/saveScheduleTraining")
	public ResponseEntity<?> saveScheduleTraining(@RequestBody ScheduleTrainingDto scheduleTrainingDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.saveScheduleTraining(scheduleTrainingDto, user.get());
	}

	@PutMapping("/updateScheduleTraining")
	public ResponseEntity<?> updateScheduleTraining(@RequestBody ScheduleTrainingDto scheduleTrainingDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.updateScheduleTraining(scheduleTrainingDto, user.get());
	}

	@DeleteMapping("/deleteScheduleTraining/{id}")
	public ResponseEntity<?> deleteScheduleTraining(@PathVariable Long id, Authentication authentication) {
		return trainingService.deleteScheduleTraining(id);
	}

	@GetMapping("/getFilterListScheduleTraining")
	public ResponseEntity<?> getFilterListScheduleTraining(
			@RequestParam(value = "branchId", required = true) Long branchId,
			@RequestParam(value = "trainingType", required = false) String trainingType,
			@RequestParam(value = "departmentId", required = false) Long departmentId,
			@RequestParam(value = "designationId", required = false) Long designationId,
			@RequestParam(value = "trainingTopic", required = false) String trainingTopic,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo, Pageable pageable,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return trainingService.getFilterListScheduleTraining(branchId, trainingType, departmentId, designationId,
					trainingTopic, dateFrom, dateTo, pageable);
		} else {
			return new ResponseEntity<List<ScheduleTrainingDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getScheduleTraining/{id}")
	public ResponseEntity<?> getScheduleTraining(@PathVariable Long id, Authentication authentication) {

		return trainingService.getScheduleTraining(id);
	}

	@GetMapping("/getTrainerTrainingHistory/{userId}")
	public ResponseEntity<?> getTrainerTrainingHistory(@PathVariable(value = "userId") Long userId,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return trainingService.getTrainerTrainingHistory(userId);
		} else {
			return new ResponseEntity<List<ScheduleTrainingDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getLastTrainingDetailReportOfTrainer/{userId}")
	public ResponseEntity<?> getLastTrainingDetailReportOfTrainer(@PathVariable Long userId,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return trainingService.getLastTrainingDetailReportOfTrainer(userId);
		} else {
			return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/approveScheduleTraining/{scheduleTrainingId}")
	public ResponseEntity<?> approveScheduleTraining(@PathVariable Long scheduleTrainingId,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
		if (userOptional.get() != null) {
			return trainingService.approveScheduleTraining(scheduleTrainingId, userOptional);
		} else {
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	@GetMapping("/getEligibleTraineesCount")
	public ResponseEntity<?> getEligibleTraineesCount(@RequestParam(value = "branchId", required = true) Long branchId,
			@RequestParam(value = "userLevels", required = false) String userLevels,
			@RequestParam(value = "categories", required = false) String categories, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());
		List<String> userLevelList = null;
		List<String> categoryList = null;
		if (userOptional.get() != null) {
			if (userLevels != null && !userLevels.isEmpty()) {
				userLevelList = Arrays.asList(userLevels.split(","));
			}
			if (categories != null && !categories.isEmpty()) {
				categoryList = Arrays.asList(categories.split(","));
			}

			return trainingService.getEligibleTraineesCount(branchId, userLevelList, categoryList);
		} else {
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	@GetMapping("/changeScheduleTrainingStatus/{scheduleTrainingId}")
	public ResponseEntity<?> changeScheduleTrainingStatus(@PathVariable Long scheduleTrainingId,
			@RequestParam(value = "status", required = true) String status, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return trainingService.changeScheduleTrainingStatus(scheduleTrainingId, status);
		} else {
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	// Training Details
	// Training Registration

	@GetMapping("/getFilterListScheduleTrainingForRegistration")
	public ResponseEntity<?> getFilterListScheduleTrainingForRegistration(
			@RequestParam(value = "trainingType", required = false) String trainingTypeId,
			@RequestParam(value = "trainingTopic", required = false) String trainingTopic,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo, Pageable pageable,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return trainingService.getFilterListScheduleTrainingForRegistration(trainingTypeId, trainingTopic, dateFrom,
					dateTo, pageable, user.get());
		} else {
			return new ResponseEntity<List<ScheduleTrainingDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getRegisteredUserList/{scheduleTrainingId}")
	public ResponseEntity<?> getRegisteredUserList(@PathVariable(value = "scheduleTrainingId") Long scheduleTrainingId,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return trainingService.getRegisteredUserList(scheduleTrainingId, user.get());
		} else {
			return new ResponseEntity<List<ScheduleTrainingDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@PostMapping("/saveTrainingRegistration")
	public ResponseEntity<?> saveTrainingRegistration(
			@RequestParam(value = "scheduleTrainingId", required = true) Long scheduleTrainingId,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.saveTrainingRegistration(scheduleTrainingId, user.get());
	}

	@PostMapping("/saveTraineeAnswerSheet")
	public ResponseEntity<?> saveTraineeAnswerSheet(@RequestBody SubmitExamDto submitExamDto,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.saveTraineeAnswerSheet(submitExamDto, user.get());
	}

	@GetMapping("/getTraineeExamScore/{scheduleTrainingId}")
	public ResponseEntity<?> getTraineeExamScore(@PathVariable(value = "scheduleTrainingId") Long scheduleTrainingId,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.getTraineeExamScore(scheduleTrainingId, user.get());
	}

	@GetMapping("/getTrainerFeedbackScoreList")
	public ResponseEntity<?> getTrainerFeedbackScoreList(
			@RequestParam(value = "trainerIds", required = true) String trainerIds, Authentication authentication) {

		List<String> trainerIdsList1 = Arrays.asList(trainerIds.split(","));

		List<Long> trainerIdsList2 = trainerIdsList1.stream().map(data -> {
			return Long.valueOf(data);
		}).collect(Collectors.toList());

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.getTrainerFeedbackScoreList(trainerIdsList2, user.get());
	}

	@PostMapping("/saveTrainerFeedbackScore")
	public ResponseEntity<?> saveTrainerFeedbackScore(
			@RequestBody List<TrainerFeedbackScoreDto> trainerFeedbackScoreDtos, Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.saveTrainerFeedbackScore(trainerFeedbackScoreDtos, user.get());
	}

	@PostMapping("/getTrainerFeedbackScore/{traineeId}")
	public ResponseEntity<?> getTrainerFeedbackScore(@PathVariable(value = "traineeId", required = true) Long traineeId,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.getTrainerFeedbackScore(traineeId, user.get());
	}

	@DeleteMapping("/deleteRegistration")
	public ResponseEntity<?> deleteRegistration(
			@RequestParam(value = "scheduleTrainingId", required = true) Long scheduleTrainingId,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> userOptional = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.deleteRegistration(scheduleTrainingId, userOptional);
	}

	// Evaluation

	@GetMapping("/getFilterListScheduleTrainingForEvaluation")
	public ResponseEntity<?> getFilterListScheduleTrainingForEvaluation(
			@RequestParam(value = "trainingType", required = false) String trainingType,
			@RequestParam(value = "departmentId", required = false) Long departmentId,
			@RequestParam(value = "designationId", required = false) Long designationId,
			@RequestParam(value = "trainingTopic", required = false) String trainingTopic,
			@RequestParam(value = "dateFrom", required = false) String dateFrom,
			@RequestParam(value = "dateTo", required = false) String dateTo, Pageable pageable,
			Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
		if (user.get() != null) {
			return trainingService.getFilterListScheduleTrainingForEvaluation(trainingType, departmentId,
					designationId, trainingTopic, dateFrom, dateTo, pageable, user.get());
		} else {
			return new ResponseEntity<List<ScheduleTrainingDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	@GetMapping("/getTraineeExamScoreList/{scheduleTrainingId}")
	public ResponseEntity<?> getTraineeExamScoreList(
			@PathVariable(value = "scheduleTrainingId") Long scheduleTrainingId, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.getTraineeExamScoreList(scheduleTrainingId, user.get());
	}



	@PostMapping("/saveTraineeExamScore")
	public ResponseEntity<?> saveTraineeExamScore(@RequestParam("traineeExamScoreDto") String traineeExamScoreDtoString,
			@RequestParam(value = "answerSheet", required = false) MultipartFile[] answerSheets,
			Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.saveTraineeExamScore(traineeExamScoreDtoString, answerSheets, user.get());
	}

	@GetMapping(value = "/downloadAnswerSheet/{traineeExamScoreId}/{fileName}")
	public ResponseEntity<?> downloadAnswerSheet(@PathVariable("traineeExamScoreId") Long traineeExamScoreId,
			@PathVariable("fileName") String fileName, Authentication authentication,
			HttpServletRequest httpServletRequest) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.downloadAnswerSheet(traineeExamScoreId, fileName, user.get(), httpServletRequest);
	}

	@GetMapping(value = "/viewAnswerSheet/{traineeExamScoreId}/{fileName}")
	public ResponseEntity<?> viewAnswerSheet(@PathVariable("traineeExamScoreId") Long traineeExamScoreId,
			@PathVariable("fileName") String fileName, Authentication authentication,
			HttpServletRequest httpServletRequest) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.viewAnswerSheet(traineeExamScoreId, fileName, user.get(), httpServletRequest);
	}

	@PostMapping(value = "/testFileUploadApi")
	public ResponseEntity<?> testFileUploadApi(@RequestParam("traineeExamScoreDto") String traineeExamScoreDtoString,
			@RequestParam("answerSheet") MultipartFile answerSheet, Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.testFileUploadApi(traineeExamScoreDtoString, answerSheet, user.get());
	}

	@GetMapping(value = "/testDownloadApi/{fileName}")
	public ResponseEntity<?> downloadApi(@PathVariable String fileName, Authentication authentication,
			HttpServletRequest httpServletRequest) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

		return trainingService.testDownloadApi(fileName, user.get(), httpServletRequest);
	}
}
