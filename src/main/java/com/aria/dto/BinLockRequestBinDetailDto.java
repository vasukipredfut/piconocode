package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class BinLockRequestBinDetailDto {

    private Long productId;

    private String productName;

    private String batchNo;

    private Long batchId;

    private String lorrySerialNo;

    private String inwardDate;

    private Long campId;

    private String campName;

    private Long state;

    private String quantity;

}
