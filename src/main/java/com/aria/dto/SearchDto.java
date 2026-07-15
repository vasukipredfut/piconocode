package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchDto {

    private String category;

    private String sex;

    private Long branchId;

    private Long deptId;

    private Long shiftId;



    private String month;

    private String empId;


    // verify status

    private Long id;

    private String verifyType;


}
