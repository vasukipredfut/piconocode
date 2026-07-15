package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BinProduct {
    String productName;
    String batchNo;
    String inwardDate;
    Double quantity;

}
