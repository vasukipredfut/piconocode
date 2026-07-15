package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProcessReportProductDTO {

    public Long productId;

    public String productName;

    public List<Double> lots;

    public List<ProcessReportProductLotWithStock> lotWithStocks ;

    public ProcessReportProductDTO(ProcessReportProductList product) {
        this.productId = product.getProductId();
        this.productName = product.getProductName();
    }
}
