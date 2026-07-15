package com.aria.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.MedicalCheckupDto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "medical_checkup")
public class MedicalCheckup {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails departmentDetails;

    @OneToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User staff;

    @Column(name = "staff_category")
    private String staffCategory;

    @Column(name = "checkup_date")
    @Temporal(TemporalType.DATE)
    private Date checkupDate;

    @Column
    private String status;

    @Column(name = "checkup_type")
    private String checkupType;


    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column(name = "proof_report")
    private String proofReport;

    @Column(name = "type_date")
    private Date typeDate;

    @Column(name = "vaccination_name")
    private String vaccinationName;

    @Column(name = "medical_check_up_value")
    private String medicalCheckUpValue;
    


    public MedicalCheckup(MedicalCheckupDto medicalCheckup) {
        DateUtil dateUtil = new DateUtil();
        this.id = medicalCheckup.getId();
        if(medicalCheckup.getOrgId() != null){
            Organisation organisation = new Organisation();
            organisation.setId(medicalCheckup.getOrgId());
            this.organisation = organisation;
        }
        if(medicalCheckup.getDeptId() != null){
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(medicalCheckup.getDeptId());
            this.departmentDetails = departmentDetails;
        }
        if(medicalCheckup.getBranchId() != null){
            Branch branch = new Branch();
            branch.setId(medicalCheckup.getBranchId());
            this.branch = branch;
        }
        if(medicalCheckup.getStaffId() != null){
            User user = new User();
            user.setId(medicalCheckup.getStaffId());
            this.staff = user;
        }

        this.staffCategory = medicalCheckup.getStaffCategory();
        this.checkupType = medicalCheckup.getCheckupType();
        this.createdBy = medicalCheckup.getCreatedBy();
        this.updatedBy = medicalCheckup.getUpdatedBy();
        this.created_date = medicalCheckup.getCreated_date();
        this.updated_date = medicalCheckup.getUpdated_date(); 
        this.proofReport = medicalCheckup.getProofReport();
        this.vaccinationName = medicalCheckup.getVaccinationName();
        this.typeDate = dateUtil.getStringTODateWk(medicalCheckup.getTypeDate());
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.medicalCheckUpValue = mapper.writeValueAsString(medicalCheckup.getMedicalCheckUpValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
