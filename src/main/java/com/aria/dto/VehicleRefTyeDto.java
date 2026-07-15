package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehicleRefTyeDto {
    private String refType;
    private String refValue;

    public VehicleRefTyeDto(String refType, String refValue) {
        this.refType = refType;
        this.refValue = refValue;
    }
}
