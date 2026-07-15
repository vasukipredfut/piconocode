package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class VehicleRequestAllotmentDto {
    private Long vehicleRequestAllotId;
    private Long vehicleRequestId;
    private Long vehicleId;
    private String  vehicle;
    private Long driverId;
    private String driverName;
    private String departureOn;
    private String arrivalOn;
    private String status;
    private List<VehicleAssignAdditionalResponseDto> additionalDrivers=new ArrayList<>();
    private  VehicleAssignShuffleResponseDto vehicleAssignShuffleResponseDto;

}
