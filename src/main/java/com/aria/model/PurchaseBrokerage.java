package com.aria.model;

import com.aria.dto.PurchaseBrokerageDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ho_purchase_brokerage")
public class PurchaseBrokerage {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long brokerage_id;

    @Column
    private Long broker_id;

    @Column
    private String brokerage_amt;

    @Column
    private String ref_no;

    @Column
    private String ref_date;

    @Column
    private String created_by_id;

    @Column
    private String created_on;

    @Column
    private Long comp_id;

    @Column
    private Long comp_br_id;

    @Column
    private String status;

    @Column
    private String type;

    @Column
    private Date from_date;

    @Column
    private Date to_date;

    @Column
    private String total_bags;

    @Column
    private Long bank_acc_id;

    @Column
    private String camp_name;

    @Column
    private String supplier_name;

    @Column
    private Double gst_amt;

    @Column
    private Double inv_amt;

    @Column
    private Double tds_amt;

    @Column
    private Double net_amt;

    @Column
    private String city;

    @Column
    private String authorized_by;

    @Column
    private String authorized_on;

    @Column
    private String remarks;

    @Column
    private Double tcs_amt;

    @Column
    private Double tcs_per;

    @Column
    private Double cgst_amt;

    @Column
    private Double sgst_amt;

    @Column
    private  Double igst_amt;


    public PurchaseBrokerage(PurchaseBrokerageDto purchaseBrokerage) {
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
            this.from_date = dateUtil.getStringTODateWk(purchaseBrokerage.getFrom_date());
        }
        if(purchaseBrokerage.getTo_date() != null) {
            this.to_date = dateUtil.getStringTODateWk(purchaseBrokerage.getTo_date());
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
