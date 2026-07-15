package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class GoGroupListDto {

    private Long companyId;

    private String companyName;

    private Long branchId;

    private String branchName;

    private Long godownId;

    private String godownName;

    private String binCode;

    private Long binId;

    private String binType;

    private String capacity;

    private String packages;
    
    private String display;
    private String palletType;
    private String palletNumber;

    public GoGroupListDto(GoGroupList goGroupList) {
        this.companyId = goGroupList.getCompanyId();
        this.companyName = goGroupList.getCompanyName();
        this.branchId = goGroupList.getBranchId();
        this.branchName = goGroupList.getBranchName();
        this.godownId = goGroupList.getGodownId();
        this.godownName = goGroupList.getGodownName();
        this.binCode = goGroupList.getBinCode();
        this.binId = goGroupList.getBinId();
        this.binType = goGroupList.getBinType();
        this.capacity = goGroupList.getCapacity();
        this.packages = goGroupList.getPackages();
        this.display=goGroupList.getDisplay();
        this.palletType=goGroupList.getPalletType();
        this.palletNumber=goGroupList.getPalletNumber();
    }
}
