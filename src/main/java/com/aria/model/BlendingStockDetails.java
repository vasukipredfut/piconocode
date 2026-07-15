package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "binstock")
public class BlendingStockDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refid")
    private Long refId;

    @Column(name = "batchno")
    private String batchNo;

    @Column(name = "batchid")
    private Long batchId;

    @Column(name = "binid")
    private String binId;

    @Column(name = "binname")
    private Long binName;

    @Column(name = "lot")
    private Double lot;

    @Column(name = "stkqty")
    private Double  stkQty;

    @Column(name = "sstkqty")
    private Double sStkQty;

    @Column(name = "avgcost")
    private Double avgCost;

    @Column(name = "cost")
    private Double cost;

    @Column(name = "dept")
    private Long dept;

    @Column(name = "branch")
    private Long branch;

    @Column(name = "lockstatus")
    private String lockStatus;

    @Column(name = "ltrnno")
    private String lTrnNo;

    @Column(name = "gid")
    private Long gid;

    @Column(name = "delstatus")
    private String delStatus;

    @Column(name = "bintype")
    private String binType;

    @Column(name = "cid")
    private Long cid;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "purchase_bill_id")
    private Long purchaseBillId;

    @Column(name = "material_Transaction_Id")
    private Long materialTransactionId;

    @Column(name = "category")
    private String category;

    @Column(name = "locktype")
    private String lockType;

    @Column(name = "oil_FFA")
    private Double oilFFA;


}
