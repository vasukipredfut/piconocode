package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QcInProcessDataDTO {
	public Long id;
    public Long kpiId;
    public String kpiName;

    public Long shiftId;
    public String shiftName;
    public String shiftDate;

    public Long batchId;
    public String batchNo;

    public Long deptId;
    public String deptName;

    public String status;
    
    public Double quantity;
    public Long batchBinNo;
    public String batchProduct;
    public Long batchProductNo;
  

    List<QcProcessKpiListDto> qcInProcessStepList;

    public QcInProcessDataDTO(QcInProcessDataList qcInProcessDataList ) {
        DateUtil dateUtil = new DateUtil();
        this.kpiId = qcInProcessDataList.getKpiId();
        this.kpiName = qcInProcessDataList.getKpiName();
        this.shiftId = qcInProcessDataList.getShiftId();
        this.shiftName = qcInProcessDataList.getShiftName();
        this.shiftDate = dateUtil.getDateTOStringWk(qcInProcessDataList.getShiftDate());
        this.batchId = qcInProcessDataList.getBatchId();
        this.batchNo = qcInProcessDataList.getBatchNo();
        this.deptId = qcInProcessDataList.getDeptId();
        this.deptName = qcInProcessDataList.getDeptName();
        this.status = qcInProcessDataList.getStatus();
        this.quantity=qcInProcessDataList.getQuantity();
        this.batchBinNo=qcInProcessDataList.getBatchBinNo();
        this.batchProduct=qcInProcessDataList.getBatchProduct();
        this.batchProductNo=qcInProcessDataList.getBatchProductNo();
        this.id=qcInProcessDataList.getId();
           }
}
