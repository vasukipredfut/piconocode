package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehicleAssignShuffleResponseDto {
    private String reason;
    private Long vehicleInfoId;
    private Long vehicleId;
    private Long driverId;
    private String vehicle;
    private String driverName;
}
