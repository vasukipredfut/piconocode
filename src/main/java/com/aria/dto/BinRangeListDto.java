package com.aria.dto;

import com.aria.model.BinRangeList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinRangeListDto {

    private Long binRangeId;

    private String departmentName;

    private String binNo;

    private String firmName;

    private String goDownName;


    public BinRangeListDto(BinRangeList binRangeList) {
        this.binRangeId = binRangeList.getBinRangeId();
        this.departmentName = binRangeList.getDepartmentName();
        this.firmName = binRangeList.getFirmName();
        this.goDownName = binRangeList.getGoDownName();
        this.binNo = (binRangeList.getSlFm()==null?"":binRangeList.getSlFm().toString())+"-"+(binRangeList.getSlTo()==null?"":binRangeList.getSlTo().toString());
    }
}
