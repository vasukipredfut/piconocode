package com.aria.dto;

import com.aria.utils.DateUtil;

public class QcInProcessListDTO {

    public String batchno;
    public Long qcId;
    public Long kpiNameId;
    public String kpiName;
    public String shiftDate;
    public Long shiftId;
    public String status;

    public QcInProcessListDTO(QcInProcessList qcProcess) {
        DateUtil dateUtil = new DateUtil();
        this.batchno = qcProcess.getBatchno();
        this.qcId = qcProcess.getQcId();
        this.kpiNameId = qcProcess.getKpiNameId();
        this.kpiName = qcProcess.getKpiName();
        this.shiftId = qcProcess.getShiftId();
        this.status = qcProcess.getStatus();
        this.shiftDate = dateUtil.getDateTOStringWk(qcProcess.getShiftDate());
    }

}
