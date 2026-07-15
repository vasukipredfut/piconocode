package com.aria.dto;

import com.aria.model.PurchaseOrderItems;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseOrderItemsDto {

    private Long id;
    private Long purchaseOrderId;

    public Long item_id;

    public Long earms_item_id;

    public String item_name;
    public Double item_qty;
    public String uom;
    public String refference_no;
    public String po_notes;
    public String po_status;
    public Double total_amount;
    public String created_by;
    public String created_on;
    public String updated_by;
    public String updated_on;
    public Double initial_qty;
    public Double final_qty;
    public Double final_order_qty;
    public Double final_price;
    public String destin_uom;
    public Long camp_id;
    public Long item_group_id;
    public Double conv_factor;
    public Double claim_rate;
    public Double premium_rate;
    public Double final_order_price;
    public String po_purpose;
    public Double box_qty;
    public String approved_by;
    public String approved_on;
    public String remarks;
    public String yield;
    public String ffa;
    public String moisture;
    public Long brokerage_id;
    public String include_gst;
    public Double sgst;
    public Double igst;
    public Double cgst;
    public Long product_id;
    public Long machine_id;
    public Long expense_id;
    public String warranty;
    public Long spec_id;
    public Long spec_type;
    public String spec_name;
    public Date warrantyDate;

    public Double freightCharges;
    public Double packingAndForwardingCharges;
    public Boolean active;
    public String freightChargesType;

    private String poValidityRequest;
    private Double netApprovedQty;
    private Double netApprovedPrice;
    private String lot ;

    public PurchaseOrderItemsDto(PurchaseOrderItems purchaseOrderItemsDto) {
        DateUtil dateUtil = new DateUtil();
        this.id = purchaseOrderItemsDto.getId();
        this.purchaseOrderId = purchaseOrderItemsDto.getPurchaseOrderId();
        this.item_id = purchaseOrderItemsDto.getITEM_ID();
        this.item_name = purchaseOrderItemsDto.getITEM_NAME();
        this.item_qty = purchaseOrderItemsDto.getITEM_QTY();
        this.uom = purchaseOrderItemsDto.getUOM();
        this.refference_no = purchaseOrderItemsDto.getREFFERENCE_NO();
        this.po_notes = purchaseOrderItemsDto.getPO_NOTES();
        this.po_status = purchaseOrderItemsDto.getPO_STATUS();
        this.total_amount = purchaseOrderItemsDto.getTOTAL_AMOUNT();
        this.created_by = purchaseOrderItemsDto.getCREATED_BY();
        this.created_on = dateUtil.getDateToStringYYYYMMDD(purchaseOrderItemsDto.getCREATED_ON());
        this.updated_by = purchaseOrderItemsDto.getUPDATED_BY();
        this.updated_on = dateUtil.getDateToStringYYYYMMDD(purchaseOrderItemsDto.getUPDATED_ON());
        this.initial_qty = purchaseOrderItemsDto.getINITIAL_QTY();
        this.final_qty = purchaseOrderItemsDto.getFINAL_QTY();
        this.final_order_qty = purchaseOrderItemsDto.getFINAL_ORDER_QTY();
        this.final_price = purchaseOrderItemsDto.getFINAL_PRICE();
        this.destin_uom = purchaseOrderItemsDto.getDESTIN_UOM();
        this.camp_id = purchaseOrderItemsDto.getCAMP_ID();
        this.item_group_id = purchaseOrderItemsDto.getITEM_GROUP_ID();
        this.conv_factor = purchaseOrderItemsDto.getCONV_FACTOR();
        this.claim_rate = purchaseOrderItemsDto.getCLAIM_RATE();
        this.premium_rate = purchaseOrderItemsDto.getPREMIUM_RATE();
        this.final_order_price = purchaseOrderItemsDto.getFINAL_ORDER_PRICE();
        this.po_purpose = purchaseOrderItemsDto.getPO_PURPOSE();
        this.box_qty = purchaseOrderItemsDto.getBOX_QTY();
        this.approved_by = purchaseOrderItemsDto.getAPPROVED_BY();
        this.approved_on = dateUtil.getDateToStringYYYYMMDD(purchaseOrderItemsDto.getAPPROVED_ON());
        this.remarks = purchaseOrderItemsDto.getREMARKS();
        this.yield = purchaseOrderItemsDto.getYIELD();
        this.ffa = purchaseOrderItemsDto.getFFA();
        this.moisture = purchaseOrderItemsDto.getMOISTURE();
        this.brokerage_id = purchaseOrderItemsDto.getBROKERAGE_ID();
        this.include_gst = purchaseOrderItemsDto.getINCLUDE_GST();
        this.spec_id = purchaseOrderItemsDto.getSpec_id();
        this.spec_name = purchaseOrderItemsDto.getSpec_name();
        this.spec_type = purchaseOrderItemsDto.getSpec_type();
        this.warrantyDate = purchaseOrderItemsDto.getWarrantyDate();
        if (purchaseOrderItemsDto.getProductMaster() != null) {
            this.product_id = purchaseOrderItemsDto.getProductMaster().getId();

        }
        if (purchaseOrderItemsDto.getMachine() != null) {
            this.machine_id = purchaseOrderItemsDto.getMachine().getId();

        }
        if (purchaseOrderItemsDto.getPurchaseExpense() != null) {
            this.expense_id = purchaseOrderItemsDto.getPurchaseExpense().getAmc_Id();

        }
        this.warranty = purchaseOrderItemsDto.getWarranty();
        this.freightCharges = purchaseOrderItemsDto.getFreightCharges();
        this.packingAndForwardingCharges = purchaseOrderItemsDto.getPackingAndForwardingCharges();
        this.active = purchaseOrderItemsDto.getActive();
        this.freightChargesType = purchaseOrderItemsDto.getFreightChargesType();
        this.poValidityRequest = purchaseOrderItemsDto.getPoValidityRequest();
        this.netApprovedPrice = purchaseOrderItemsDto.getNetApprovedPrice();
        this.netApprovedQty = purchaseOrderItemsDto.getNetApprovedQty();
        this.lot = purchaseOrderItemsDto.getLot();
        this.cgst = purchaseOrderItemsDto.getCgstPercentage();
        this.igst = purchaseOrderItemsDto.getIgstPercentage();
        this.sgst = purchaseOrderItemsDto.getSgstPercentage();

    }

}
