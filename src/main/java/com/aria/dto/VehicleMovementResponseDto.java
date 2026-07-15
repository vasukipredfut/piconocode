package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VehicleMovementResponseDto {
    private Long vehicleRequestAllotId;
    private String arrivalPlant;
    private String scheduleType;
    private String vehicleNumber;
    private String tripName;
    private String scheduleDep;
    private String scheduleArr;
    private String actualDep;
    private String actualDepBy;
    private String actualArr;
    private String actualArrBy;
    private String tripDuration;
    private String kmTravel;
    private String delay;
    private String delayReason;
    private Long meterReading;
}
