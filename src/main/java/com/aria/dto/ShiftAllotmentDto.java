package com.aria.dto;


import com.aria.model.ShiftAllotment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ShiftAllotmentDto {


    private Long deptID;

    private Long branchId;

    private String shiftName;

    private Long orgId;

    private Long createdBy;

    private Long updatedBy;

    private Boolean staffemployee;
    
    private List<UserDto> shift1Staffs;

    private List<UserDto> shift2Staffs;

    private List<UserDto> shift3Staffs;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private int emp_type;


}
