package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class BinLockRequestDto {

    private Long id;

    private String binNo;

    private Boolean locked;

    private String lockType;
    
    private String lockDate;

    private String lockStatus;

    private Long lockedEmployeeId;

    private String lockedEmployeeName;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;


}
