package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class StockPaperReqDto {

    private Long branchId;
    private Long deptId;
    private List<StockPaperProducts> productsWithLot;
    private Long shiftId;
    private String shiftDate;

}
