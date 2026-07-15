package com.aria.dto;

import com.aria.model.GoDownList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoDownListDto {

    private Long goDownId;

    private String goDownName;

    private String goDownAddress;

    private String branchName;

    private String goDownType;


    public GoDownListDto(GoDownList goDownList) {
        this.goDownId = goDownList.getGoDownId();
        this.goDownName = goDownList.getGoDownName();
        this.goDownAddress = goDownList.getGoDownAddress();
        this.branchName = goDownList.getBranchName();
        this.goDownType = goDownList.getGoDownType();
    }
}
