package com.aria.model;



import jakarta.persistence.*;

import com.aria.dto.VendorDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ledger")
public class Vendor {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
    
    @Column
    private String type;

    @Column
    private String taxtype;

    @Column
    private String address;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String country;

    @Column
    private String tinno;

    @Column
    private String phone;

    @Column
    private String mobile;

    @Column
    private String email;
    
    @Column
    private String contact;

    @Column
    private String zone;

    @Column
    private String lfinal;

    @Column
    private String chess;

    @Column(name="gst")
    private String gstNumber;

    @Column
    private Integer companylink;

    @Column
    private Integer headplant;

    @Column
    private Long stateid;

    @Column
    private Long partygroupid;

    @Column
    private Long cityid;

    @Column
    private Long countryid;

    @Column
    private Integer activeflag;

    @Column
    private Integer packing_raw_material;

    @Column
    private Integer godownflag;

    @Column
    private String pincode;

    @Column
    private String district;

    @Column
    private String camp;


    @Column(name = "ho_status")
    private String hoStatus;

    @Column(name = "send_ft_to_ho_object")
    private String send_ft_to_ho_object;


    @Column(name = "sister_Concern_Group_Id")
    private Long sisterConcernGroupId;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @OneToMany(mappedBy = "vid", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Where(clause = "vtable='ledger' AND status='A'")
    private List<BankInfo> bankInfo;

    public Vendor(VendorDto vendorDto) {
        this.id = vendorDto.getId();
        this.name = vendorDto.getName();
        this.type = vendorDto.getType();
        this.taxtype = vendorDto.getTaxtype();
        this.address = vendorDto.getAddress();
        this.city = vendorDto.getCity();
        this.state = vendorDto.getState();
        this.country = vendorDto.getCountry();
        this.tinno = vendorDto.getTinno();
        this.phone = vendorDto.getPhone();
        this.mobile = vendorDto.getMobile();
        this.email = vendorDto.getEmail();
        this.contact = vendorDto.getContact();
        this.zone = vendorDto.getZone();
        this.lfinal = vendorDto.getLfinal();
        this.chess = vendorDto.getChess();
        this.gstNumber = vendorDto.getGstNumber();
        this.companylink = vendorDto.getCompanylink();
        this.headplant = vendorDto.getHeadplant();
        this.stateid = vendorDto.getStateid();
        this.partygroupid = vendorDto.getPartygroupid();
        this.cityid = vendorDto.getCityid();
        this.countryid = vendorDto.getCountryid();
        this.activeflag = vendorDto.getActiveflag();
        this.packing_raw_material = vendorDto.getPacking_raw_material();
        this.godownflag = vendorDto.getGodownflag();
        this.camp = vendorDto.getCamp();
        this.district = vendorDto.getDistrict();
        this.pincode = vendorDto.getPincode();
        this.sisterConcernGroupId = vendorDto.getSisterConcernGroupId();
    }
}
