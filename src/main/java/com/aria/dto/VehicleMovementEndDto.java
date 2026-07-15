package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class VehicleMovementEndDto {
    private Long vehicleRequestAllotId;
    private String arrivalTime;
    private String load;
    private String arrivalPlant;
    private String approxDate;
    private String shift;
    private String shiftSlab;
    private List<VehicleRefTyeDto> refTypes;

}
