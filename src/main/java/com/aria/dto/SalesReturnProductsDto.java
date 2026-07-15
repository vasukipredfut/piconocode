package com.aria.dto;

import com.aria.model.SalesReturnProducts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class SalesReturnProductsDto {

    private Long id;
    private Long returnId;

    public Long productId;
    private String productName;
    private String productCode;

    private String productType;

    private Long binName;

    private Double lot;

    private Double return_quantity;

    private String return_reason;

    private List<SalesReturnBinDetailsDto> binDetails;

    private String batchNo;
    private Long batchId;

    public SalesReturnProductsDto(SalesReturnProducts salesReturnProduct) {

        this.id = salesReturnProduct.getId();
        this.returnId = salesReturnProduct.getId();
        if (salesReturnProduct.getProduct() != null) {
            this.productId = salesReturnProduct.getProduct().getId();
            this.productName = salesReturnProduct.getProduct().getProductName();
        }
        this.productCode = salesReturnProduct.getProductCode();
        this.productType = salesReturnProduct.getProductType();
        this.lot = salesReturnProduct.getLot();
        this.return_quantity = salesReturnProduct.getReturn_quantity();

        this.batchNo = salesReturnProduct.getBatch_No();
        this.batchId = salesReturnProduct.getBatch_Id();

        this.binDetails = salesReturnProduct.getBinDetails() != null
                ? salesReturnProduct.getBinDetails().stream().map(field -> new SalesReturnBinDetailsDto(field)).collect(Collectors.toList()) : null;

    }
}
