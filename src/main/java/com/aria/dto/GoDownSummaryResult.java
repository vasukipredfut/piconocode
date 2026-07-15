package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GoDownSummaryResult {


    public Long godownId;
    public String godownName;

    List<GoDownSummaryProductDTO> productDTOList;
 
    public GoDownSummaryResult(GoDownSummaryList goDownSummaryList) {
        this.godownId = goDownSummaryList.getGodownId();
        this.godownName = goDownSummaryList.getGodownName();
    }
}
