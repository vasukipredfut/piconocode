package com.aria.dto;

import java.util.Date;

import com.aria.model.BinStock;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public interface StockByBranch {

    Long getBranch();
    String getBranchName();
    String getDepartmentName();
    Long getProductId();
    Long getDepartmentId();
    String getProductName();
    Double getStock();
    Double getLot();
}
