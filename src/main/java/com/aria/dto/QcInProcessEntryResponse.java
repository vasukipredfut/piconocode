package com.aria.dto;

import com.aria.model.JournalMachineMapping;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class QcInProcessEntryResponse {

    String kpiName;
    String testedBy;
    String isoNo;
    String kpiNameId;
    String productName;
    String plant;
    String shiftDate;
    String shiftName;
    String iso;
    String batchNo;

    String mainMachineName;
    Long mainMachineId;

    List<QcInProcessEntryDetails> qcInProcessEntryDetails;
    List<QcInProcessEntryAttributesDTO> qcInProcessEntryAttributesList;



    public QcInProcessEntryResponse(QcInProcessEntry entry) {
        DateUtil dateUtil = new DateUtil();
        this.kpiName = entry.getKpiName();
        this.isoNo = entry.getIsoNo();
        this.productName = entry.getProductName();
        this.plant = entry.getPlant();
        this.shiftDate = dateUtil.getDateTOStringWk(entry.getShiftDate());
        this.shiftName = entry.getShiftName();
        this.iso = entry.getKpiName();
        this.testedBy = entry.getTestedBy();
    }
}
