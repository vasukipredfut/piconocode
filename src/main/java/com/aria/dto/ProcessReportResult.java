package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProcessReportResult {


    List<ProcessReportGroupDTO> tableDataList;
    private Company firmDetails;
    private Object plantDetails;
}
