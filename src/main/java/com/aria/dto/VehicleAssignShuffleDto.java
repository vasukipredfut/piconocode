package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class VehicleAssignShuffleDto {
    private Long vehicleRequestAllotId;
    private String reason;
    private Long vehicleInfoId;
}
