package com.aria.dto;

import com.aria.model.Districts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
public class DistrictsDto {


    private Long id;

    private String name;
    
    private String state;

    private Long stateId;

    private Long countryId;

    public DistrictsDto(Districts districts) {
        this.id = districts.getId();
        this.name = districts.getName();
        this.state = districts.getState();
        this.stateId = districts.getStateId();
        this.countryId = districts.getCountryId();
    }
}
