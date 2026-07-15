package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class VehicleRequestVehiclesDto {
    private Long vehicleId;
    private String  vehicle;
    private Long driverId;
    private String driverName;
    private Long vInfoId;
}
