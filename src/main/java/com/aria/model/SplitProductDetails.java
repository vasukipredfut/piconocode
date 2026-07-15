package com.aria.model;

import com.aria.dto.SplitProductDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "split_product_details")
public class SplitProductDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long  company;

    @Column
    private Long branch;

    @Column
    private Long  department;

    @Column
    private Long  binno;

    @Column
    private Long  godown_id;


    @Column
    private Long  split_trn_id;

    @Column
    private String  split_type;

    @Column
    private String  bsk_batch_no;

    @Column
    private Long  bsk_batch_id;

    @Column
    private Long  product_id;

    @Column
    private String  product_name;

    @Column
    private double  lot;

    @Column
    private String  packing_factor;

    @Column
    private double  quantity;

    @Column
    private double  selected_qty;

    @Column
    @Temporal(TemporalType.DATE)
    private Date production_date;

    @Column
    private Long  hub;

    @Column
    private Long  active_flag;

    @Column
    private String  status;

    @Column
    private String  remarks;

    @Column
    private String  description;

    @Column
    @Temporal(TemporalType.DATE)
    private Date  inventory_date;

    @Column
    private double order_quantity;


    @Column
    private double  balance_quantity;

    @Column
    private Long  binstock_id;

    @Column
    private String  batch_no;

    @Column
    private Long  batch_id;

    @Column
    private String  palletNumber;

    public SplitProductDetails(SplitProductDto splitProduct , SplitTransaction splitTrn) {


        this.branch = splitTrn.getBid();
        this.department = splitTrn.getDid();
        this.split_trn_id = splitTrn.getId();
        this.product_id = splitProduct.getProductId();
        this.product_name = splitProduct.getProductName();
        this.quantity = splitProduct.getProductQty();
        this.selected_qty = splitProduct.getSelectedQty();
        this.bsk_batch_id = splitProduct.getBsk_batch_id();
        this.bsk_batch_no = splitProduct.getBsk_batch_no();
        this.lot = splitProduct.getLot();
        this.palletNumber = splitProduct.getPalletNumber();

    }
}
