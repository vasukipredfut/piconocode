package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class StockLedgerReqDto {

    private String entryType;

    private String type;

    private String batchNo;

}
