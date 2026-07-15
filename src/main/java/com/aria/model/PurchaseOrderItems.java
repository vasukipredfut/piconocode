package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.PurchaseOrderItemsDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_Order_Id")
    private Long purchaseOrderId;

    @Column
    public Long ITEM_ID;

    @Column
    public String ITEM_NAME;

    @Column
    public Double ITEM_QTY;

    @Column
    public String UOM;

    @Column
    public String REFFERENCE_NO;

    @Column
    public String PO_NOTES;

    @Column
    public String PO_STATUS;

    @Column
    public Double TOTAL_AMOUNT;

    @Column
    public String CREATED_BY;

    @Column
    public Date CREATED_ON;

    @Column
    public String UPDATED_BY;

    @Column
    public Date UPDATED_ON;

    @Column
    public Double INITIAL_QTY;

    @Column
    public Double FINAL_QTY;

    @Column
    public Double FINAL_ORDER_QTY;

    @Column
    public Double FINAL_PRICE;

    @Column
    public String DESTIN_UOM;

    @Column
    public Long CAMP_ID;

    @Column
    public Long ITEM_GROUP_ID;

    @Column
    public Double CONV_FACTOR;

    @Column
    public Double CLAIM_RATE;

    @Column
    public Double PREMIUM_RATE;

    @Column
    public Double FINAL_ORDER_PRICE;

    @Column
    public String PO_PURPOSE;

    @Column
    public Double BOX_QTY;

    @Column
    public String APPROVED_BY;

    @Column
    public Date APPROVED_ON;

    @Column
    public String REMARKS;

    @Column
    public String YIELD;

    @Column
    public String FFA;

    @Column
    public String MOISTURE;

    @Column
    public Long BROKERAGE_ID;

    @Column
    public String INCLUDE_GST;

    @Column
    public String warranty;

    @Column(name ="warranty_Date" )
    public Date warrantyDate;

    @Column
    public Long spec_id;

    @Column
    public Long spec_type;

    @Column
    public String spec_name;

    @OneToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductMaster productMaster;

    @OneToOne
    @JoinColumn(name = "machine_id", referencedColumnName = "id")
    private Machine machine;

    @OneToOne
    @JoinColumn(name = "expense_id", referencedColumnName = "amc_Id")
    private PurchaseExpense purchaseExpense;

    @Column(name = "freight_charges")
    public Double freightCharges;

    @Column(name = "freight_charges_type")
    public String freightChargesType;

    @Column(name = "packing_forwarding_charges")
    public Double packingAndForwardingCharges;


    @Column
    public Boolean active;


    @Column(name = "po_validity_request")
    private String poValidityRequest;


    @Column(name = "net_approved_qty")
    private Double netApprovedQty;

    @Column(name = "net_approved_price")
    private Double netApprovedPrice;

    @Column
    private String lot ;

    @Column
    private String po_inward_status ;

    @Column(name ="cgst_percentage" )
    public Double cgstPercentage;

    @Column(name ="igst_percentage" )
    public Double igstPercentage;

    @Column(name ="sgst_percentage" )
    public Double sgstPercentage;


    public PurchaseOrderItems(PurchaseOrderItemsDto purchaseOrderItemsDto) {
        DateUtil dateUtil = new DateUtil();
        this.id = purchaseOrderItemsDto.getId();
        this.purchaseOrderId = purchaseOrderItemsDto.getPurchaseOrderId();
        this.ITEM_ID = purchaseOrderItemsDto.getItem_id();
        this.ITEM_NAME = purchaseOrderItemsDto.getItem_name();
        this.ITEM_QTY = purchaseOrderItemsDto.getItem_qty();
        this.UOM = purchaseOrderItemsDto.getUom();
        this.REFFERENCE_NO = purchaseOrderItemsDto.getRefference_no();
        this.PO_NOTES = purchaseOrderItemsDto.getPo_notes();
        this.PO_STATUS = purchaseOrderItemsDto.getPo_status();
        this.TOTAL_AMOUNT = purchaseOrderItemsDto.getTotal_amount();
        this.CREATED_BY = purchaseOrderItemsDto.getCreated_by();
        if (purchaseOrderItemsDto.getCreated_on() != null && purchaseOrderItemsDto.getCreated_on() != "")
            this.CREATED_ON = dateUtil.getStringTODateYYYYMMDD(purchaseOrderItemsDto.getCreated_on());
        this.UPDATED_BY = purchaseOrderItemsDto.getUpdated_by();
        if (purchaseOrderItemsDto.getUpdated_on() != null && purchaseOrderItemsDto.getUpdated_on() != "")
            this.UPDATED_ON = dateUtil.getStringTODateYYYYMMDD(purchaseOrderItemsDto.getUpdated_on());
        this.INITIAL_QTY = purchaseOrderItemsDto.getInitial_qty();
        this.FINAL_QTY = purchaseOrderItemsDto.getFinal_qty();
        this.FINAL_ORDER_QTY = purchaseOrderItemsDto.getFinal_order_qty();
        this.FINAL_PRICE = purchaseOrderItemsDto.getFinal_price();
        this.DESTIN_UOM = purchaseOrderItemsDto.getDestin_uom();
        this.CAMP_ID = purchaseOrderItemsDto.getCamp_id();
        this.ITEM_GROUP_ID = purchaseOrderItemsDto.getItem_group_id();
        this.CONV_FACTOR = purchaseOrderItemsDto.getConv_factor();
        this.CLAIM_RATE = purchaseOrderItemsDto.getClaim_rate();
        this.PREMIUM_RATE = purchaseOrderItemsDto.getPremium_rate();
        this.FINAL_ORDER_PRICE = purchaseOrderItemsDto.getFinal_order_price();
        this.PO_PURPOSE = purchaseOrderItemsDto.getPo_purpose();
        this.BOX_QTY = purchaseOrderItemsDto.getBox_qty();
        this.APPROVED_BY = purchaseOrderItemsDto.getApproved_by();
        if (purchaseOrderItemsDto.getApproved_on() != null && purchaseOrderItemsDto.getApproved_on() != "")
            this.APPROVED_ON = dateUtil.getStringTODateYYYYMMDD(purchaseOrderItemsDto.getApproved_on());
        this.REMARKS = purchaseOrderItemsDto.getRemarks();
        this.YIELD = purchaseOrderItemsDto.getYield();
        this.FFA = purchaseOrderItemsDto.getFfa();
        this.MOISTURE = purchaseOrderItemsDto.getMoisture();
        this.BROKERAGE_ID = purchaseOrderItemsDto.getBrokerage_id();
        this.spec_id = purchaseOrderItemsDto.getSpec_id();
        this.spec_type = purchaseOrderItemsDto.getSpec_type();
        this.spec_name = purchaseOrderItemsDto.getSpec_name();
        this.INCLUDE_GST = purchaseOrderItemsDto.getInclude_gst();

        if (purchaseOrderItemsDto.getProduct_id() != null) {
            ProductMaster productMaster = new ProductMaster();
            productMaster.setId(purchaseOrderItemsDto.getProduct_id());
            this.productMaster = productMaster;
        }

        if (Objects.isNull(purchaseOrderItemsDto.getSpec_type())) {
            ProductMaster productMaster = new ProductMaster();
            productMaster.setId(purchaseOrderItemsDto.getItem_id());
            this.productMaster = productMaster;
        } else if (purchaseOrderItemsDto.getSpec_type() == 28) {
            ProductMaster productMaster = new ProductMaster();
            productMaster.setId(purchaseOrderItemsDto.getItem_id());
            this.productMaster = productMaster;
        } else if (purchaseOrderItemsDto.getExpense_id() != null) {
            PurchaseExpense purchaseExpense = new PurchaseExpense();
            purchaseExpense.setAmc_Id(purchaseOrderItemsDto.getExpense_id());
            this.purchaseExpense = purchaseExpense;
        } else if ((purchaseOrderItemsDto.getSpec_type() == 30 || purchaseOrderItemsDto.getSpec_type() == 31)) {
            PurchaseExpense purchaseExpense = new PurchaseExpense();
            purchaseExpense.setAmc_Id(purchaseOrderItemsDto.getItem_id());
            this.purchaseExpense = purchaseExpense;
        } else if (purchaseOrderItemsDto.getMachine_id() != null) {
            Machine machine = new Machine();
            machine.setId(purchaseOrderItemsDto.getMachine_id());
            this.machine = machine;
        } else if (purchaseOrderItemsDto.getSpec_type() == 29) {
            Machine machine = new Machine();
            machine.setId(purchaseOrderItemsDto.getMachine_id());
            this.machine = machine;
        }

        this.warranty = purchaseOrderItemsDto.getWarranty();
        this.warrantyDate = purchaseOrderItemsDto.getWarrantyDate();
        this.freightCharges = purchaseOrderItemsDto.getFreightCharges();
        this.packingAndForwardingCharges = purchaseOrderItemsDto.getPackingAndForwardingCharges();
        this.active = purchaseOrderItemsDto.getActive();
        this.freightChargesType = purchaseOrderItemsDto.getFreightChargesType();
        this.poValidityRequest = purchaseOrderItemsDto.getPoValidityRequest();
        this.netApprovedPrice = purchaseOrderItemsDto.getNetApprovedPrice();
        this.netApprovedQty = purchaseOrderItemsDto.getNetApprovedQty();
        this.lot = purchaseOrderItemsDto.getLot();
        this.cgstPercentage = purchaseOrderItemsDto.getCgst();
        this.sgstPercentage = purchaseOrderItemsDto.getSgst();
        this.igstPercentage = purchaseOrderItemsDto.getIgst();

    }
}
