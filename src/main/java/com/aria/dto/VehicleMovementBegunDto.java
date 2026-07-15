package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class VehicleMovementBegunDto {
    private Long vehicleRequestAllotId;
    private String departureTime;
    private String load;
    private String employeeType;
    private List<VehicleRefTyeDto> refTypes;
    private List<VehicleVanBoyDto> vanBoys;

}
