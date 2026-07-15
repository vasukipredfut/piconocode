package com.aria.dto;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.aria.model.ScheduleTraining;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleTrainingDto {

    private Long id;

    private String trainingType;

    private Long branchId;

    private String branchName;

    private String trainingTopic;

    private Long departmentId;

    private String departmentName;

    private Long designationId;

    private String designationName;


    private Long skillId;

    private String skillName;

    private String trainingDate;

    private String lastRegistrationDate;

    private Integer totalSeats;

    private Integer totalRegisteredEmployees;

    private Integer remainingSeats;

    private String status;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private Set<TrainingUserLevelMappingDto> trainingUserLevelMappingDtos;

    private Set<TrainingEmployeeCategoryMappingDto> trainingEmployeeCategoryMappingDtos;

    private List<TrainerDetailDto> trainerDetailDtos;

    private List<MultipleChoiceQuestionsDto> multipleChoiceQuestionDtos;

    private List<OpenEndedQuestionsDto> openEndedQuestionDtos;

    private List<TrainingUserRegistrationMappingDto> trainingUserRegistrationMappingDtos;

    public ScheduleTrainingDto(ScheduleTraining scheduleTraining) {

        this.id = scheduleTraining.getId();


        this.trainingType = scheduleTraining.getTrainingType();


        if (scheduleTraining.getTrainingBranch() != null) {
            this.branchId = scheduleTraining.getTrainingBranch().getId();
            this.branchName = scheduleTraining.getTrainingBranch().getBranchName();
        }

        this.trainingTopic = scheduleTraining.getTrainingTopic();

        if (scheduleTraining.getDepartment() != null) {
            this.departmentId = scheduleTraining.getDepartment().getId();
            this.departmentName = scheduleTraining.getDepartment().getName();
        }

        if (scheduleTraining.getDesignation() != null) {
            this.designationId = scheduleTraining.getDesignation().getId();
            this.designationName = scheduleTraining.getDesignation().getName();
        }

        if (scheduleTraining.getSkills() != null) {
            this.skillId = scheduleTraining.getSkills().getId();
            this.skillName = scheduleTraining.getSkills().getName();
        }

        this.totalSeats = scheduleTraining.getTotalSeats();
        this.totalRegisteredEmployees = scheduleTraining.getTotalRegisteredEmployees();
        this.remainingSeats = scheduleTraining.getRemainingSeats();

        this.trainingUserLevelMappingDtos = scheduleTraining.getTrainingUserLevelMappings() != null ? scheduleTraining
                .getTrainingUserLevelMappings().stream().map(data -> new TrainingUserLevelMappingDto(data))
                .collect(Collectors.toCollection(LinkedHashSet::new)) : null;

        this.trainingEmployeeCategoryMappingDtos = scheduleTraining.getTrainingEmployeeCategoryMappings() != null
                ? scheduleTraining.getTrainingEmployeeCategoryMappings().stream()
                .map(data -> new TrainingEmployeeCategoryMappingDto(data))
                .collect(Collectors.toCollection(LinkedHashSet::new))
                : null;

        this.trainerDetailDtos = scheduleTraining.getTrainerDetails() != null ? scheduleTraining.getTrainerDetails()
                .stream().map(data -> new TrainerDetailDto(data)).collect(Collectors.toList()) : null;

        this.multipleChoiceQuestionDtos = scheduleTraining.getMultipleChoiceQuestions() != null
                ? scheduleTraining.getMultipleChoiceQuestions().stream()
                .map(data -> new MultipleChoiceQuestionsDto(data)).collect(Collectors.toList())
                : null;

        this.openEndedQuestionDtos = scheduleTraining.getOpenEndedQuestions() != null
                ? scheduleTraining.getOpenEndedQuestions().stream().map(data -> new OpenEndedQuestionsDto(data))
                .collect(Collectors.toList())
                : null;

        this.trainingUserRegistrationMappingDtos = scheduleTraining.getTrainingUserRegistrationMapping() != null
                ? scheduleTraining.getTrainingUserRegistrationMapping().stream()
                .map(data -> new TrainingUserRegistrationMappingDto(data)).collect(Collectors.toList())
                : null;

        this.status = scheduleTraining.getStatus();
        this.createdBy = scheduleTraining.getCreatedBy();
        this.updatedBy = scheduleTraining.getUpdatedBy();
        this.created_date = scheduleTraining.getCreated_date();
        this.updated_date = scheduleTraining.getUpdated_date();
    }

}
