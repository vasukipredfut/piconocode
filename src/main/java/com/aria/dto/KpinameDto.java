package com.aria.dto;


import com.aria.model.Kpiname;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KpinameDto {

    private Long id;

    private String name;

    private Long fieldId;

    private String formNo;

    private String type;

    private Long proId;
    
    private String productName;

    private String atType;

    private int pendingReportFlag;

    private String processType;

    public KpinameDto(Kpiname kpiname) {
        this.id = kpiname.getId();
        this.name = kpiname.getName();
        this.fieldId = kpiname.getFieldId();
        this.formNo = kpiname.getFormNo();
        this.type = kpiname.getType();

        if (kpiname.getProduct() != null) {
			this.productName = kpiname.getProduct().getProductName();
			this.proId = kpiname.getProduct().getId();
		}
        this.atType = kpiname.getAtType();
        this.pendingReportFlag = kpiname.getPendingReportFlag();
        this.processType = kpiname.getProcessType();
    }
}
