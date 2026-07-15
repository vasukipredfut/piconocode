package com.aria.dto;

import com.aria.model.State;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
public class StateDto {

 
    private Long id;

    private String name;

    private Long countryId;

    private String gstStateCode;

    public StateDto(State state) {
        this.id = state.getId();
        this.name = state.getName();
        this.countryId = state.getCountryId();
        this.gstStateCode = state.getGstStateCode();
    }
}
