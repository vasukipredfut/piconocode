package com.aria.dto;

import com.aria.model.Action;
import com.aria.utils.CustomConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class VehicleRequestDto {
    private Long id;
    private Long tripId;
    private String tripName;
    private String typeofVehicle;
    private String startTime;
    private String travelDistance;
    private Long nofvehicle;
    private String capacity;
    private String reqDepartment;
    private String reqPlant;
    private String requestedBy;
    private String travelDate;
    private String status;

}
