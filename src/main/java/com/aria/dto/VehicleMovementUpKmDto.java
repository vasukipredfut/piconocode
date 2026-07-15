package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehicleMovementUpKmDto {
    private Long vehicleRequestAllotId;
    private Long lastReading;
    private Long currentReading;
    private Long distance;
}
