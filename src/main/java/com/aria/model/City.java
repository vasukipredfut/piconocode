package com.aria.model;

import com.aria.dto.CityDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "city")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(name = "poname")
    private String poName;

    @Column(name = "pincode")
    private Integer pinCode;

    @Column
    private String district;

    @Column
    private String state;

    @Column(name = "districtid")
    private Long districtId;

    @Column(name = "stateid")
    private Long stateId;

    @Column(name = "countryid")
    private Long countryId;

    @Column(name = "updatedby")
    private Long updatedBy;

    @Column(name = "updatedon")
    private Date updatedOn;

    public City(CityDto city) {
        this.id = city.getId();
        this.name = city.getName();
        this.poName = city.getPoName();
        this.pinCode = city.getPinCode();
        this.district = city.getDistrict();
        this.state = city.getState();
        this.districtId = city.getDistrictId();
        this.stateId = city.getStateId();
        this.countryId = city.getCountryId();
        this.updatedBy = city.getUpdatedBy();
        this.updatedOn = city.getUpdatedOn();
        this.pinCode = city.getPinCode();
    }
}
