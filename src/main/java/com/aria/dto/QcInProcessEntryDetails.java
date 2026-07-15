package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QcInProcessEntryDetails {


    public Long machineId ;
    public String machineName ;
    public String packingBatchNo;
    public Long binName;

    List<QcInProcessEntryAttributesDTO> qcInProcessEntryAttributes;

    public QcInProcessEntryDetails(QcInProcessEntry entry) {
        this.machineId = entry.getMachineId();
        this.machineName = entry.getMachineName();
        this.packingBatchNo=entry.getPackingBatchNo();
        this.binName=entry.getBinName();
    }
}
