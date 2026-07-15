package com.aria.model;

import com.aria.dto.DpEntryStoredProductDto;


import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "direct_purchase_stored_product")
public class DpEntryStoredProduct {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_id")
    private Long entryId;


    @OneToOne
    @JoinColumn(name = "color_id", referencedColumnName = "id")
    private Color color;

    @Column(name = "batch_No")
    private String batchNo;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "package_type")
    private String packageType;

    @Column(name = "per_bag_weight")
    private String perBagWeight;

    @Column(name = "total_quantity")
    private String totalQuantity;

    @Column(name = "item_type")
    private Long itemType;


    @Column(name = "item_id")
    private Long itemId;
    
    
    @Column
    private Double sgst;
    
    @Column
    private Double cgst;
    
    @Column
    private Double igst;
        
    @Column
    private Double billamount;


    @Column(name = "item_name")
    private String itemName;


    @Column
    private Long po_id;

    @Column
    private String po_number;

    @Column
    private Long bill_ref_id;

    @Column
    private String lot;

    @OneToMany(mappedBy = "entryProductId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DpEntryStoredBinDetails> binDetails;

    @Column
    private Date warranty_Date;

    @Column
    private String warranty_certificate;

    @Column(name = "unit_cost", updatable = false)
    private String unitCost;

    @Column(name = "unit_rate", updatable = false)
    private String unitRate;

    @OneToMany(mappedBy = "dpEntryStoredProductId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ConsumptionDetails> consumptionDetails;


    public DpEntryStoredProduct(DpEntryStoredProductDto directPurchase) {
        DateUtil dateUtil = new DateUtil();
        this.id = directPurchase.getId();
        this.entryId = directPurchase.entryId;

        if(directPurchase.getColorId() != null) {
            Color color = new Color();
            color.setId(directPurchase.getColorId());
            this.color = color;
        }
        this.batchNo = directPurchase.getBatchNo();
        this.batchId = directPurchase.getBatchId();
        this.packageType = directPurchase.getPackageType();
        this.perBagWeight = directPurchase.getPerBagWeight();
        this.totalQuantity = directPurchase.getTotalQuantity();
        this.itemId=directPurchase.getItemId();
        this.itemType=directPurchase.getItemType();
        this.sgst=directPurchase.getSgst();
        this.cgst=directPurchase.getCgst();
        this.igst=directPurchase.getIgst();
        this.billamount=directPurchase.getBillamount();
        this.itemName =directPurchase.getItemName();
        this.po_id=directPurchase.getPoId();
        this.po_number=directPurchase.getPoNumber();
        this.bill_ref_id=directPurchase.getBillRefId();
        this.lot = directPurchase.getLot();
        if(directPurchase.getWarranty_Date() != null) {
            this.warranty_Date = dateUtil.getStringTODateWk(directPurchase.getWarranty_Date());
        }
        this.warranty_certificate = directPurchase.getWarranty_certificate();
        this.unitCost = directPurchase.getUnitCost();
        this.unitRate = directPurchase.getUnitRate();
    }
}
