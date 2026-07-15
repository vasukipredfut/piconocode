package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.ScheduleTrainingDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "schedule_training")
public class ScheduleTraining {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_Type")
    private String trainingType;

    @Column(name = "training_Topic")
    private String trainingTopic;

    @OneToOne
    @JoinColumn(name = "training_branch_id", referencedColumnName = "id", nullable = false)
    private Branch trainingBranch;

    @OneToOne
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    private DepartmentDetails department;

    @OneToOne
    @JoinColumn(name = "designation_id", referencedColumnName = "id")
    private Designation designation;

    @OneToOne
    @JoinColumn(name = "skill_id", referencedColumnName = "id")
    private Skills skills;

    @OneToMany(mappedBy = "scheduleTrainingId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TrainingUserLevelMapping> trainingUserLevelMappings;

    @OneToMany(mappedBy = "scheduleTrainingId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TrainingEmployeeCategoryMapping> trainingEmployeeCategoryMappings;

    @Column(name = "training_Date")
    private Date trainingDate;

    @Column(name = "last_Registration_Date")
    private Date lastRegistrationDate;

    @Column(name = "total_Seats")
    private Integer totalSeats;

    @Column(name = "total_Registered_Employees")
    private Integer totalRegisteredEmployees;

    @Column(name = "remaining_Seats")
    private Integer remainingSeats;

    @Column
    private String status;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "scheduleTraining", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TrainerDetail> trainerDetails;

    @OneToMany(mappedBy = "scheduleTrainingId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MultipleChoiceQuestion> multipleChoiceQuestions;

    @OneToMany(mappedBy = "scheduleTrainingId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OpenEndedQuestion> openEndedQuestions;

    @OneToMany(mappedBy = "scheduleTraining", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<TrainingUserRegistrationMapping> trainingUserRegistrationMapping;

    public ScheduleTraining(ScheduleTrainingDto scheduleTrainingDto) {
        id = scheduleTrainingDto.getId();
        this.trainingType = scheduleTrainingDto.getTrainingType();
        this.trainingTopic = scheduleTrainingDto.getTrainingTopic();
        if (scheduleTrainingDto.getBranchId() != null) {
            Branch branch = new Branch();
            branch.setId(scheduleTrainingDto.getBranchId());
            this.trainingBranch = branch;
        }
        if (scheduleTrainingDto.getDepartmentId() != null) {
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(scheduleTrainingDto.getDepartmentId());
            this.department = departmentDetails;
        }
        if (scheduleTrainingDto.getDesignationId() != null) {
            Designation designation = new Designation();
            designation.setId(scheduleTrainingDto.getDesignationId());
            this.designation = designation;
        }
        if (scheduleTrainingDto.getSkillId() != null) {
            Skills skills = new Skills();
			skills.setId(scheduleTrainingDto.getSkillId());
            this.skills = skills;
        }

        this.totalRegisteredEmployees = scheduleTrainingDto.getTotalRegisteredEmployees();
        this.totalSeats = scheduleTrainingDto.getTotalSeats();
        this.remainingSeats = scheduleTrainingDto.getRemainingSeats();
        this.status = scheduleTrainingDto.getStatus();
        this.createdBy = scheduleTrainingDto.getCreatedBy();
        this.updatedBy = scheduleTrainingDto.getUpdatedBy();
        this.created_date = scheduleTrainingDto.getCreated_date();
        this.updated_date = scheduleTrainingDto.getUpdated_date();
    }

}
