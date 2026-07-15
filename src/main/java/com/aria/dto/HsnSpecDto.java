package com.aria.dto;

import com.aria.model.Machine;
import com.aria.model.ProductMaster;
import com.aria.model.PurchaseExpense;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HsnSpecDto {


    private Long id;
    private Long typeId;
    private String name;
    private String hsnCode;
    private Long amc_machine_ref_Id;
    private String amc_machine_ref_Name;

    public HsnSpecDto(ProductMaster rs, Long typeId) {
        this.id = rs.getId();
        this.typeId = typeId;
        this.name = rs.getProductName();
        this.hsnCode = rs.getHsnRefId();
    }

    public HsnSpecDto(Machine rs, Long typeId) {
        this.id = rs.getId();
        this.typeId = typeId;
        this.name = rs.getName();
        this.hsnCode = rs.getHsnRefId();
    }

    public HsnSpecDto(PurchaseExpense rs, Long typeId) {
        this.id = rs.getAmc_Id();
        this.typeId = typeId;
        this.name = rs.getAmc_Expense_Name();
        this.hsnCode = rs.getHsn_Ref_Id();
        this.amc_machine_ref_Id = rs.getAmc_Machine_Ref_Id();
    }
}
