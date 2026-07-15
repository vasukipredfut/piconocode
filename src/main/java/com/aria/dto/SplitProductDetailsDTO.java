package com.aria.dto;

import com.aria.model.SplitProductDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
public class SplitProductDetailsDTO {


    private Long id;
    
    private Long  company;

    private Long branch;
    
    private Long  department;
    
    private Long  binno;

    private Long  godown_id;

    
    private Long  split_trn_id;

    private String  split_type;

    private String  bsk_batch_no;
    
    private Long  bsk_batch_id;

    private Long  product_id;

    private String  product_name;

    private double  lot;

    private String  packing_factor;

    
    private double  quantity;
    private double  selectedQty;

    private Date production_date;

    private Long  hub;

    private Long  active_flag;

    private String  status;
    
    private String  remarks;

    private String  description;

    private Date  inventory_date;

    private double order_quantity;

    private double  balance_quantity;

    private Long  binstock_id;
    
    private String  batch_no;

    
    private Long  batch_id;
    private String palletNo;

    public SplitProductDetailsDTO(SplitProductDetails productDetails) {
        this.id = productDetails.getId();
        this.company = productDetails.getCompany();
        this.branch = productDetails.getBranch();
        this.department = productDetails.getDepartment();
        this.binno = productDetails.getBinno();
        this.godown_id = productDetails.getGodown_id();
        this.split_trn_id = productDetails.getSplit_trn_id();
        this.split_type = productDetails.getSplit_type();
        this.bsk_batch_no = productDetails.getBsk_batch_no();
        this.bsk_batch_id = productDetails.getBsk_batch_id();
        this.product_id = productDetails.getProduct_id();
        this.product_name = productDetails.getProduct_name();
        this.lot = productDetails.getLot();
        this.packing_factor = productDetails.getPacking_factor();
        this.quantity = productDetails.getQuantity();
        this.selectedQty = productDetails.getSelected_qty();
        this.production_date = productDetails.getProduction_date();
        this.hub = productDetails.getHub();
        this.active_flag = productDetails.getActive_flag();
        this.status = productDetails.getStatus();
        this.remarks = productDetails.getRemarks();
        this.description = productDetails.getDescription();
        this.inventory_date = productDetails.getInventory_date();
        this.order_quantity = productDetails.getOrder_quantity();
        this.balance_quantity = productDetails.getBalance_quantity();
        this.binstock_id = productDetails.getBinstock_id();
        this.batch_no = productDetails.getBatch_no();
        this.batch_id = productDetails.getBatch_id();
        this.palletNo=productDetails.getPalletNumber();
    }
}
