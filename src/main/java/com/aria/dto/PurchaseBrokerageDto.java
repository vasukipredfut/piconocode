package com.aria.dto;

import com.aria.model.PurchaseBrokerage;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseBrokerageDto {

    private Long id;
    private Long brokerage_id;
    private Long broker_id;
    private String brokerage_amt;
    private String ref_no;
    private String ref_date;
    private String created_by_id;
    private String created_on;
    private Long comp_id;
    private  Long comp_br_id;
    private String status;
    private  String type;
    private  String from_date;
    private  String to_date;
    private String total_bags;
    private Long bank_acc_id;
    private String camp_name;
    private  String supplier_name;
    private Double gst_amt;
    private Double inv_amt;
    private Double tds_amt;
    private Double net_amt;
    private String city;
    private String authorized_by;
    private String authorized_on;
    private String remarks;
    private Double tcs_amt;
    private Double tcs_per;
    private Double cgst_amt;
    private Double sgst_amt;
    private  Double igst_amt;

    public PurchaseBrokerageDto(PurchaseBrokerage purchaseBrokerage) {
        DateUtil dateUtil = new DateUtil();
        this.id = purchaseBrokerage.getId();
        this.brokerage_id = purchaseBrokerage.getBrokerage_id();
        this.broker_id = purchaseBrokerage.getBroker_id();
        this.brokerage_amt = purchaseBrokerage.getBrokerage_amt();
        this.ref_no = purchaseBrokerage.getRef_no();
        this.ref_date = purchaseBrokerage.getRef_date();
        this.created_by_id = purchaseBrokerage.getCreated_by_id();
        this.created_on = purchaseBrokerage.getCreated_on();
        this.comp_id = purchaseBrokerage.getComp_id();
        this.comp_br_id = purchaseBrokerage.getComp_br_id();
        this.status = purchaseBrokerage.getStatus();
        this.type = purchaseBrokerage.getType();
        if(purchaseBrokerage.getFrom_date() != null) {
            this.from_date = dateUtil.getDateTOStringWk(purchaseBrokerage.getFrom_date());
        }
        if(purchaseBrokerage.getTo_date() != null) {
            this.to_date = dateUtil.getDateTOStringWk(purchaseBrokerage.getTo_date());
        }
        this.total_bags = purchaseBrokerage.getTotal_bags();
        this.bank_acc_id = purchaseBrokerage.getBank_acc_id();
        this.camp_name = purchaseBrokerage.getCamp_name();
        this.supplier_name = purchaseBrokerage.getSupplier_name();
        this.gst_amt = purchaseBrokerage.getGst_amt();
        this.inv_amt = purchaseBrokerage.getInv_amt();
        this.tds_amt = purchaseBrokerage.getTds_amt();
        this.net_amt = purchaseBrokerage.getNet_amt();
        this.city = purchaseBrokerage.getCity();
        this.authorized_by = purchaseBrokerage.getAuthorized_by();
        this.authorized_on = purchaseBrokerage.getAuthorized_on();
        this.remarks = purchaseBrokerage.getRemarks();
        this.tcs_amt = purchaseBrokerage.getTcs_amt();
        this.tcs_per = purchaseBrokerage.getTcs_per();
        this.cgst_amt = purchaseBrokerage.getCgst_amt();
        this.sgst_amt = purchaseBrokerage.getSgst_amt();
        this.igst_amt = purchaseBrokerage.getIgst_amt();
    }
}
