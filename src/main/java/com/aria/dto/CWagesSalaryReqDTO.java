package com.aria.dto;

import com.aria.utils.TransactionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CWagesSalaryReqDTO {


    private String shiftDate;

    private String refTrnNo;

    private Long refTrnId;

    private String refTrnTable;

    private String trnDate;

    private Long shiftId;

    private Long pShift;

    private String expref;

    private Long refid;

    private Double trnQty;

    private Double qty;

    private Double amt;

    private Long createdBy;

    private Long dept;

    private Long branch;
    private Double lot;
    private String type;
    private TransactionType transactionType;

    List<CWagesSalaryEmployeeDTO> employeeDTOList;
    List<CwagesDetailsReqDTO> cwagesDetailsReqDTOList;

}
