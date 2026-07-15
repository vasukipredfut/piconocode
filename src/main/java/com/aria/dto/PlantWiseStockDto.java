package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlantWiseStockDto {





    private Long dept;
    private String deptName;

    private Long branch;
    private String branchName;

    private Double qty;

    public PlantWiseStockDto(PlantWiseStock plantWiseStock) {
        this.dept = plantWiseStock.getDept();
        this.deptName = plantWiseStock.getDeptName();
        this.branch = plantWiseStock.getBranch();
        this.branchName = plantWiseStock.getBranchName();
        this.qty = plantWiseStock.getQty();
    }
}
