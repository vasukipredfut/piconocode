package com.aria.dto;

import com.aria.model.DailyUserDependency;
import com.aria.model.EmastDaily;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class EmastListDto {

    private Long id;
   
    private String name;

    private String gender;

    private Long quali;

    private Long blood;

    private String location;

    private String fathername;

    private String address;
   
    private String phoneno;

    private String esino;

    private String epfno;

    private String category;

    private Long wages;

    private String photo;

    private Long biostn;

    private Long bioid;

    private String picturename;

    private String picturesize;

    private String picturetype;

    private String picturewidth;
   
    private String pictureheight;

    private String age;


    private Date birthday;


    private Long departmentId;

    private String finals;
   
    private Long branch;

    private Long cluster;

    private Long company;
   
    private String cstatus;

    private String status;

    private String religion;

    private String caste;

    private String community;

    private Long country;

    private Long state;

    private Long district;

    private Long city;

    private String title;

    private String mobno;
   
    private String uanno;

    private String adharno;

    private String bus;
   
    private Long dacatId;

    private String lefton;
   
    private String nominee_name;

    private String nominee_dob;
   
    private String nominee_relationship;

   
    private String member_address;

   
    private String upload_aadhar;

   
    private String upload_pan;

    private String upload_bank_pass;

    private String upload_passportsize;

    private String uploadSpouseBankDetail;

    private String uploadSpouseAadharDetail;
   
    private Long aadhaarVerifiedBy;

    private Date aadhaarVerifiedOn;
   
    private String aadhaarFile;

    private int abry;

    List<DailyUserDependency> dailyUserDependency;


    public EmastListDto(EmastDaily emastDaily) {
        this.id = emastDaily.getId();
        this.name = emastDaily.getName();
        this.gender = emastDaily.getGender();
        this.quali = emastDaily.getQuali();
        this.blood = emastDaily.getBlood();
        this.location = emastDaily.getLocation();
        this.fathername = emastDaily.getFathername();
        this.address = emastDaily.getAddress();
        this.phoneno = emastDaily.getPhoneno();
        this.esino = emastDaily.getEsiNo();
        this.epfno = emastDaily.getEpfno();
        this.category = emastDaily.getCategory();
        this.wages = emastDaily.getWages();
        this.photo = emastDaily.getPhoto();
        this.biostn = emastDaily.getBiostn();
        this.bioid = emastDaily.getBioid();
        this.picturename = emastDaily.getPicturename();
        this.picturesize = emastDaily.getPicturesize();
        this.picturetype = emastDaily.getPicturetype();
        this.picturewidth = emastDaily.getPicturewidth();
        this.pictureheight = emastDaily.getPictureheight();
        this.age = emastDaily.getAge();
        this.birthday = emastDaily.getBirthday();
        this.departmentId = emastDaily.getDepartmentId();
        this.finals = emastDaily.getFinals();
        this.branch = emastDaily.getBranch();
        this.cluster = emastDaily.getCluster();
        this.company = emastDaily.getCompany();
        this.cstatus = emastDaily.getCstatus();
        this.status = emastDaily.getStatus();
        this.religion = emastDaily.getReligion();
        this.caste = emastDaily.getCaste();
        this.community = emastDaily.getCommunity();
        this.country = emastDaily.getCountry();
        this.state = emastDaily.getState();
        this.district = emastDaily.getDistrict();
        this.city = emastDaily.getCity();
        this.title = emastDaily.getTitle();
        this.mobno = emastDaily.getMobno();
        this.uanno = emastDaily.getUanNo();
        this.adharno = emastDaily.getAdharNo();
        this.bus = emastDaily.getBus();
        this.dacatId = emastDaily.getDacatId();
        this.lefton = emastDaily.getBus();
        this.nominee_name = emastDaily.getNominee_name();
        this.nominee_dob = emastDaily.getNominee_name();
        this.nominee_relationship = emastDaily.getNominee_relationship();
        this.member_address = emastDaily.getMember_address();
        this.upload_aadhar = emastDaily.getUpload_aadhar();
        this.upload_pan = emastDaily.getUpload_pan();
        this.upload_bank_pass = emastDaily.getUpload_bank_pass();
        this.upload_passportsize = emastDaily.getUpload_passportsize();
        this.uploadSpouseBankDetail = emastDaily.getUploadSpouseBankDetail();
        this.uploadSpouseAadharDetail = emastDaily.getUploadSpouseAadharDetail();
        this.aadhaarVerifiedBy = emastDaily.getAadhaarVerifiedBy();
        this.aadhaarVerifiedOn = emastDaily.getAadhaarVerifiedOn();
        this.aadhaarFile = emastDaily.getAadhaarFile();
        this.abry = emastDaily.getAbry();
        this.dailyUserDependency = emastDaily.getDailyUserDependency();
    }
}
