package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QcInProcessDataDetailsDTO {

    public Long kpiId;
    public String kpiName;

    public Long shiftId;
    public String shiftName;
    public String shiftDate;

    public Long batchId;
    public String batchNo;

    public Long deptId;
    public String deptName;

    public String machineNme;
    public Long machineId;

    public Long binName;
    public String binId;
    
    public String packingBatchNo;
   public Long batchProductNo;

    private List<AttributesListDto> attributeFieldsProcessType;

    public QcInProcessDataDetailsDTO(QcInProcessDataList qcInProcessDataList ) {
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
        this.machineNme = qcInProcessDataList.getMachineNme();
        this.machineId = qcInProcessDataList.getMachineId();
        this.binName = qcInProcessDataList.getBinName();
        this.binId = qcInProcessDataList.getBinId();
        this.packingBatchNo=qcInProcessDataList.getPackingBatchNo();
        this.batchProductNo=qcInProcessDataList.getBatchProductNo();
        
    }
}
