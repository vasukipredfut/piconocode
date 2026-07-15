package com.aria.model;

import com.fasterxml.jackson.annotation.JsonIgnore;


import com.aria.dto.*;
import com.aria.utils.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "employee_details")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column
    @JsonIgnore
    private String password;

    @Column
    private Integer active;

    @Column
    private Long roles_id;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails department;

    @Column
    private Long org_id;

    @Column
    private Long branch_id;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column(name = "batchno")
    private String batchNo;

    @Column
    private String employeename;

    @Column
    private String employeetype;

    @OneToOne
    @JoinColumn(name = "desi_id", referencedColumnName = "id")
    private Designation designation;

    private Long bloodgroupid;

    @Column
    private String gender;

    @Column
    private String qualification;

    @Column
    private String location;

    @Column
    private Long cluster;

    @Column
    private String address;


    @Column(name = "photo")
    private String photo;

    @Column
    private String email;

    @Column
    private String phoneno;

    @Column
    private String emergencycontactno;

    @Column
    private String emergentcontactperson;

    @Column(name = "emergency_contact_person_relationship")
    private String emergencyContactPersonRelationship;

    @Column
    private String maritalstatus;

    @Column
    @Temporal(TemporalType.DATE)
    private Date dateofbirth;

    @Column
    private String bankname;

    @Column
    private String accountno;

    @Column
    private String ifsccode;

    @Column(name = "panno")
    private String panNo;

    @Column
    private String aadharno;

    @Column
    private String uanno;

    @Column
    private String esino;

    @Column
    private String epfno;

    @Column(name = "joiningdate")
    @Temporal(TemporalType.DATE)
    private Date joiningDate;

     /* @Column(name = "actcategory")
    private String actCategory;*/

    @Column
    private String basicsalary;

    @Column
    private String allowance;

    @Column(name = "deamessallowance")
    private String deamessAllowance;

    @Column(name = "houserentallowance")
    private String houseRentAllowance;

    @Column(name = "goodworkallowance")
    private String goodWorkAllowance;

    @Column
    private String fathername;

    @Column
    private Long dacatid;

    @Column
    private Long jobres;

    @Column
    private Long quali;

    @Column
    private Long blood;

    @Column
    private String category;

    @Column
    private String biostn;

    @Column
    private Long bioid;

    @Column
    private String staffid;

    @Column
    private String userlevel;

    @Column
    private String ar;

    @Column
    private String locationpath;
    
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
    private String describe;

    @Column
    @Temporal(TemporalType.DATE)
    private Date weddingday;

    @Column
    private String cstatus;

    @Column
    private String status;

    @Column
    @Temporal(TemporalType.DATE)
    private Date lefton;

    @Column(name = "final")
    private String finals;

    @Column
    private String experience;

    @Column
    private String room;

    @Column
    private String bus;

    @Column
    @Temporal(TemporalType.DATE)
    private Date provisonalstaffjoiningdate;

    @Column
    @Temporal(TemporalType.DATE)
    private Date permenantstaffjoiningdate;

    @Column
    private String memberaddress;

    @Column
    private String uploadaadhar;

    @Column
    private String uploadpan;

    @Column
    private String uploadbankpass;

    @Column
    private String uploadpassportsize;

    @Column
    private String uploadspousebankdetail;

    @Column
    private String uploadspouseaadhardetail;

    @Column
    private Long aadhaarverifiedby;

    @Column
    @Temporal(TemporalType.DATE)
    private Date aadhaarverifiedon;

    @Column
    private String aadhaarfile;

    /*@Column
    private String batch;*/

    @Column(name = "abry")
    private String ABRY;


    @OneToMany(mappedBy = "userid", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<UserDependency> dependencies;


    @OneToMany(mappedBy = "empId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EmployeeQualificationMapping> qualificationMapping;


    
    
    public DepartmentDetails getDepartment() {
		return department;
	}


	public void setDepartment(DepartmentDetails department) {
		this.department = department;
	}


	public Long getBranch_id() {
		return branch_id;
	}


	public void setBranch_id(Long branch_id) {
		this.branch_id = branch_id;
	}


	public User(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public User(UserDto user) {
        DateUtil dateUtil = new DateUtil();
        this.id = user.getId();
        //this.username = user.getEmail();
        this.password = user.getPassword();
        this.active = user.getActive();
        this.roles_id = user.getRoles_id();
        this.org_id=user.getOrgId();
        this.branch_id=user.getBranchId();
        if(user.getDeptId() != null){
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(user.getDeptId());
            this.department = departmentDetails;
        }
        this.locationpath = user.getLocationpath(); 
        this.created_date = user.getCreated_date();
        this.updated_date = user.getUpdated_date();
        this.batchNo = user.getBatchNo();
        //this.batch = user.getBatchNo();
        this.employeename = user.getEmployeeName();
        this.employeetype = user.getEmployeeType();
        if (user.getDesignationId() != null && user.getDesignationId() != 0) {
            Designation designation = new Designation();
            designation.setId(user.getDesignationId());
            this.designation = designation;
        }

        this.bloodgroupid = user.getBloodGroupId();
        this.gender = user.getGender();
        this.qualification = user.getQualification();
        this.location = user.getLocation();
        this.address = user.getAddress();
        this.photo = user.getPhoto();
        this.email = user.getEmail();
        this.phoneno = user.getPhoneNo();
        this.emergencycontactno = user.getEmergencyContactNo();
        this.emergentcontactperson = user.getEmergentContactPerson();
        this.emergencyContactPersonRelationship = user.getEmergencyContactPersonRelationship();
        //this.actCategory = user.getActCategory();
        this.maritalstatus = user.getMaritalStatus();
        if(user.getDateOfBirth() != null) {
            this.dateofbirth = dateUtil.getStringTODateWk(user.getDateOfBirth());
        }
        this.category = user.getEmployeeType();
        this.bankname = user.getBankName();
        this.accountno = user.getAccountNo();
        this.ifsccode = user.getIfsccode();
        this.panNo = user.getPanNo();
        this.aadharno = user.getAadharNo();
        this.uanno = user.getUanNo();
        this.esino = user.getEsiNo();
        this.epfno = user.getEpfNo();
        this.basicsalary = user.getBasicSalary();
        this.allowance = user.getAllowance();
        this.deamessAllowance = user.getDeamessAllowance();
        this.houseRentAllowance = user.getHouseRentAllowance();
        this.goodWorkAllowance = user.getGoodWorkAllowance();
        this.dacatid = user.getDacatid();
        this.aadhaarfile = user.getAadhaarfile();
        this.status = user.getStatus();
        this.bioid=user.getBioId();
    }



    public User(Interview user) {

        this.batchNo = user.getBatchNo();
        this.employeename = user.getCandidateName();
        this.employeetype = user.getEmployeeType();
        this.gender = user.getGender();
        this.qualification = user.getQualification();
        this.location = user.getHomeTown();
        this.phoneno = user.getPhoneNumber();
        if(user.getDateOfBirth() != null) {
            this.dateofbirth = user.getDateOfBirth();
        }
        this.category = user.getEmployeeType();
        this.aadharno = user.getAadharNumber();
    }

}