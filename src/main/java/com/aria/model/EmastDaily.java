package com.aria.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "emast_daily")
public class EmastDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @OneToOne
    @JoinColumn(name = "department", referencedColumnName = "id")
    private DepartmentDetails department;

    @Column
    private String gender;

    @Column
    private Long quali;

    @Column
    private Long blood;

    @Column
    private String location;

    @Column
    private String fathername;

    @Column
    private String address;

    @Column
    private String phoneno;

    @Column(name = "esino")
    private String esiNo;

    @Column
    private String epfno;

    @Column
    private String category;

    @Column
    private Long wages;

    @Column
    private String photo;

    @Column
    private Long biostn;

    @Column
    private Long bioid;

    @Column
    private String picturename;

    @Column
    private String picturesize;

    @Column
    private String picturetype;

    @Column
    private String picturewidth;

    @Column
    private String pictureheight;

    @Column
    private String age;

    @Column
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date birthday;

    @Transient
    private Long departmentId;

    @Column
    private String finals;

    @Column
    private Long branch;

    @Column
    private Long cluster;

    @Column
    private Long company;

    @Column
    private String cstatus;

    @Column
    private String status;

    @Column
    private String religion;

    @Column
    private String caste;

    @Column
    private String community;

    @Column
    private Long country;

    @Column
    private Long state;

    @Column
    private Long district;

    @Column
    private Long city;

    @Column
    private String title;

    @Column
    private String mobno;

    @Column(name = "uanno")
    private String uanNo;

    @Column(name = "adharno")
    private String adharNo;

    @Column
    private String bus;

    @Column(name = "dacatid")
    private Long dacatId;

    @Column
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "DD-MM-YYYY")
    private Date lefton;

    @Column
    private String nominee_name;

    @Column
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "DD-MM-YYYY")
    private Date nominee_dob;

    @Column
    private String nominee_relationship;

    @Column
    private String member_address;

    @Column
    private String upload_aadhar;

    @Column
    private String upload_pan;


    @Column
    private String upload_bank_pass;

    @Column
    private String upload_passportsize;

    @Column(name = "upload_Spouse_Bank_Detail")
    private String uploadSpouseBankDetail;

    @Column(name = "upload_Spouse_Aadhar_Detail")
    private String uploadSpouseAadharDetail;

    @Column(name = "aadhaar_Verified_By")
    private Long aadhaarVerifiedBy;

    @Column(name = "aadhaar_Verified_On")
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "DD-MM-YYYY")
    private Date aadhaarVerifiedOn;

    @Column(name = "aadhaar_File")
    private String aadhaarFile;

    @Column
    private int abry;

    @Transient
    List<DailyUserDependency> dailyUserDependency;

    @Column
    private Integer active;

    @Column
    private String qualification;

    @Column
    private String emergencycontactno;

    @Column
    private String emergentcontactperson;

    @Column(name = "emergency_Contact_Person_Relationship")
    private String emergencyContactPersonRelationship;

    @Column
    private String maritalstatus;

    @Column(name = "bankname")
    private String bankName;

    @Column
    private String accountno;

    @Column(name = "panno")
    private String panNo;

    @Column
    private String describe;

    @Column(name = "deamessallowance")
    private String deamessAllowance;

    @Column(name = "houserentallowance")
    private String houseRentAllowance;

    @Column(name = "goodworkallowance")
    private String goodWorkAllowance;

    @Column
    private String basicsalary;

    @Column(name = "joiningdate")
    @Temporal(TemporalType.DATE)
    @JsonFormat(pattern = "dd-MM-yyyy")
    private Date joiningDate;

    @Column(name = "act_category")
    private String actCategory;

    @Column
    private String allowance;

    @Column
    private String ifsccode;

    @Column(name = "batch_no")
    private String batchNo;


    @Column(name = "desigid")
    private Long desiId;

    @Column(name = "designation")
    private String designation;


    public EmastDaily(Interview user) {

        this.batchNo = user.getBatchNo();
        this.name = user.getCandidateName();
        this.gender = user.getGender();
        this.qualification = user.getQualification();
        this.location = user.getHomeTown();
        this.phoneno = user.getPhoneNumber();
        if (user.getDateOfBirth() != null) {
            this.birthday = user.getDateOfBirth();
        }
        this.category = user.getEmployeeType();
        this.adharNo = user.getAadharNumber();
    }

}
