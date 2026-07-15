package com.aria.dto;



import com.aria.model.Vendor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class VendorDto {

    private Long id;

    private String name;

    private String type;

    private String taxtype;

    private String address;

    private String city;

    private String state;

    private String country;

    private String tinno;

    private String phone;

    private String mobile;

    private String email;

    private String contact;

    private String zone;
   
    private String lfinal;

    private String chess;

    private String gstNumber;

    private Integer companylink;

    private Integer headplant;
   
    private Long stateid;

    private Long partygroupid;

    private String partygroupName;

    private Long cityid;

    private Long countryid;

    private Integer activeflag;

    private Integer packing_raw_material;

    private Integer godownflag;
    private String pincode;
    private String district;
    private String camp;

    private Long sisterConcernGroupId;

    List<BankInfoDTO> bankInfo;




    public VendorDto(Vendor  vendorDto) {
        this.id = vendorDto.getId();

        //this.name = StringEscapeUtils.unescapeHtml4(vendorDto.getName());

        byte[] bytes =  vendorDto.getName().getBytes(Charset.forName("Windows-1252"));;

        // If you know that 8D should be 8E, you can manually correct it (or vice versa)
        for (int i = 0; i < bytes.length; i++) {
            System.out.println(bytes[i]);
            if (bytes[i] == (byte) 0x8D) {
                bytes[i] = (byte) 0x8E; // Replace 8D with 8E if necessary
            }
        }

        // Decode the bytes correctly using UTF-8
        String decodedText = new String(bytes, StandardCharsets.UTF_8);
        this.name = decodedText;
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
        this.pincode = vendorDto.getPincode();
        this.district = vendorDto.getDistrict();
        this.camp = vendorDto.getCamp();
        this.sisterConcernGroupId = vendorDto.getSisterConcernGroupId();

        this.bankInfo = vendorDto.getBankInfo() != null ?
                vendorDto.getBankInfo().stream().map(data -> new BankInfoDTO(data)).collect(Collectors.toList()) : null;
    }
}
