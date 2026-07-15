package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;



import com.aria.model.User;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;




@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    private Long id;

    private String username;

    private String password;

    private  Long orgId;

    private  Long branchId;

    private  String branchName;

    private  Long deptId;

    private  String deptName;

    private int active;

    private Long roles_id;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private String batchNo;

    private String employeeName;

    private String employeeType;

    private Long designationId;

    private Long dacatid;

    private String designationName;

    private String gender;

    private String qualification;

    private String location;

    private String address;

    private Long bloodGroupId;

    private String bloodGroupName;
    
    private String locationpath;
    
    private String pictureBae64;

    private String photo;

    private MultipartFile photoFile;

    private String email;

    private String phoneNo;

    private String emergencyContactNo;

    private String emergentContactPerson;

    private String emergencyContactPersonRelationship;

    private String bankName;

    private String accountNo;

    private String ifsccode;

    private String panNo;

    private String aadharNo;

    private String aadhaarfile;

    private String uanNo;

    private String esiNo;

    private String epfNo;

    private String maritalStatus;

    private String dateOfBirth;

    private String joiningDate;

    private String actCategory;

    private String basicSalary;

    private String allowance;

    private String deamessAllowance;

    private String houseRentAllowance;

    private String goodWorkAllowance;

    private String cstatus;

    private String status;
    
    private Long bioId;

    private List<String> qualificationList;

    //private String relationShip;

    List<UserDependencyDto> dependencies;




    public UserDto(User user) {
        DateUtil dateUtil = new DateUtil();
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        //this.active = user.getActive();
        this.roles_id=user.getRoles_id();
        this.orgId=user.getOrg_id();
        this.branchId=user.getBranch_id();
        if(user.getDepartment() != null){
            this.deptId = user.getDepartment().getId();
            this.deptName = user.getDepartment().getName();
        }
        this.locationpath = user.getLocationpath();
        
        this.created_date = user.getCreated_date();
        this.updated_date = user.getUpdated_date();
        this.batchNo = user.getBatchNo();
        this.employeeName = user.getEmployeename();
        if(user.getEmployeetype() != null){
            this.employeeType = user.getEmployeetype();
        }else {
            this.employeeType = user.getCategory();
        }
        if(user.getDesignation() != null) {
            this.designationId = user.getDesignation().getId();
            this.designationName = user.getDesignation().getName();
        }
        this.gender = user.getGender();
        this.qualification = user.getQualification();
        this.location = user.getLocation();
        this.address = user.getAddress();
	    this.bloodGroupId=user.getBloodgroupid();
        this.photo = user.getPhoto();
        this.email = user.getEmail();
        this.phoneNo = user.getPhoneno();
        this.emergencyContactNo = user.getEmergencycontactno();
        this.emergentContactPerson = user.getEmergentcontactperson();
        this.emergencyContactPersonRelationship = user.getEmergencyContactPersonRelationship();
        this.aadhaarfile = user.getAadhaarfile();
        //this.actCategory = user.getActCategory();
        this.maritalStatus = user.getMaritalstatus();
        this.bankName = user.getBankname();
        this.accountNo = user.getAccountno();
        this.ifsccode = user.getIfsccode();
        this.panNo = user.getPanNo();
        this.aadharNo = user.getAadharno();
        this.uanNo = user.getUanno();
        this.esiNo = user.getEsino();
        this.epfNo = user.getEpfno();
        this.basicSalary = user.getBasicsalary();
        this.allowance = user.getAllowance();
        this.deamessAllowance = user.getDeamessAllowance();
        this.houseRentAllowance = user.getHouseRentAllowance();
        this.goodWorkAllowance = user.getGoodWorkAllowance();
        if(user.getDateofbirth() != null) {
            this.dateOfBirth = dateUtil.getDateTOStringWk(user.getDateofbirth());
        }
        this.cstatus = user.getCstatus();
        this.status = user.getStatus();
        this.dependencies = user.getDependencies() != null ? user.getDependencies().stream().map(obj -> new UserDependencyDto(obj)).collect(Collectors.toList()) : null;
        this.bioId=user.getBioid();
        
    }

    public UserDto(User user, Boolean status) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.roles_id= user.getRoles_id();
        this.employeeName = user.getEmployeename();
        this.employeeType = user.getEmployeetype();
        if(user.getDesignation() != null) {
            this.designationId = user.getDesignation().getId();
            this.designationName = user.getDesignation().getName();
        }
        this.gender = user.getGender();
        this.qualification = user.getQualification();
        this.location = user.getLocation();
        this.address = user.getAddress();
        this.photo = user.getPhoto();
        this.email = user.getEmail();
        this.bioId=user.getBioid();
    }
}
