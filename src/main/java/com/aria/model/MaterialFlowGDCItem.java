package com.aria.model;

import com.aria.dto.OtherBranchIssueItemsDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "material_flow_gdc_item")
public class MaterialFlowGDCItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  material_Flow_Gdc_Item_Id ;

    @Column
    private String material_Flow_Gdc_Number ;

    @Column
    private String  gdc_Item_Name ;
    @Column
    private Long gdc_Item_Id ;
    @Column
    private Double gdc_Item_Quandity ;
    @Column
    private Double gdc_Item_Avgcost ;
    @Column
    private Double gdc_Item_Totalcost ;
    @Column
    private Double gdc_Item_lot ;
    @Column
    private String HSN_CODE ;
    @Column
    private String gdc_Item_UOM;

    public MaterialFlowGDCItem(OtherBranchIssueItemsDTO items) {
        this.gdc_Item_Name = items.getItemName();
        this.gdc_Item_Id = items.getItemId();
        if(items.getAllocatedQuantity() != null) {
            this.gdc_Item_Quandity = Double.valueOf(items.getAllocatedQuantity());
        }
        this.gdc_Item_lot =  Double.valueOf(items.getSelectLot());
    }
}
