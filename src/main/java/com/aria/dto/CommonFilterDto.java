package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonFilterDto {

    private Long branchId;
    private Long deptId;
    private Long batchId;
    private Long productId;
    private String batchNo;
    private Double lot;
    private Long departmentId;
}
