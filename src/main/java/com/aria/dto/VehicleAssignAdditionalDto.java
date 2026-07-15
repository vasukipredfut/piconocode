package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class VehicleAssignAdditionalDto {
    private Long vehicleRequestAllotId;
    private String driverStatus;
    private List<Long> driverIds;
}
