package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OtherDeductionReqDTO {


    public String type;
    public Long branch;
    public Long dept;
    public String category;
    public String connectTable;
    public Long month;
    public Long year;
    
    List<OtherDeductionEmpReqDto> empDetails;


}
