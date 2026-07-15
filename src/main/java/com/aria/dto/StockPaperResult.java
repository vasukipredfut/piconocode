package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class StockPaperResult {

    private Long shiftId;
    private String shiftName;
    private String shiftDate;
    private String deptName;
    private String branchName;


    List<StockPaperDto> stockPapers;


}
