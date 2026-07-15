package com.aria.dto;

import com.aria.model.City;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CityDto {

    private Long id;

    private String name;

    private String poName;

    private Integer pinCode;
   
    private String district;

    private String state;

    private Long districtId;

    private Long stateId;

    private Long countryId;

    private Long updatedBy;

    private Date updatedOn;

    public CityDto(City city) {
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
    }
}
