package com.aria.dto;

import com.aria.model.JaggeryInspection;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JaggeryInspectionListDto {




    private Long id;
    private String uniqno;
    private Long vendorId;
    private String vendorName;
    private Double qty;
    private String billNo;
    private String billDate;
    private String jagDate;
    private String status;


    public JaggeryInspectionListDto(JaggeryInspectionList jaggeryInspection) {
        DateUtil dateUtil = new DateUtil();
        this.id = jaggeryInspection.getId();
        this.uniqno = jaggeryInspection.getUniqno();
        this.vendorId = jaggeryInspection.getParty();
        this.vendorName = jaggeryInspection.getVendorName();
        this.qty = jaggeryInspection.getQty();
        this.status = jaggeryInspection.getStatus();
        this.billNo = jaggeryInspection.getBillno();
        this.billDate = dateUtil.getDateTOStringWk(jaggeryInspection.getBilldt());
        this.jagDate = dateUtil.getDateTOStringWk(jaggeryInspection.getJdate());
    }
}
