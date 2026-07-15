package com.aria.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.aria.dto.BinStockDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Table(name = "binstock")
public class BinStock {

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

    @Column(name = "inwarddate")
    private Date inwarDate;

    @Column(name = "location")
    private String location;

    @Column(name = "camp_name")
    private String campName;

    @Column(name = "seedcolor")
    private String seedcolor;

    @Column(name = "seedmoisture")
    private String seedMoisture;

    @Column(name = "mrnstatus")
    private String mrnStatus;

    @Column(name = "blending_lock")
    private Boolean blendingLock;

    @Column
    @Temporal(TemporalType.DATE)
    private Date adate;



    public BinStock(BinStockDto binStock) {
        this.id = binStock.getId();
        this.refId = binStock.getProductId();
        this.batchNo = binStock.getBatchNo();
        this.batchId = binStock.getBatchId();
        this.binId = binStock.getBinId();
        this.binName = binStock.getBinName();
        this.lot = binStock.getLot();
        this.stkQty = binStock.getStkQty();
        this.sStkQty = binStock.getSStkQty();
        this.avgCost = binStock.getAvgCost();
        this.cost = binStock.getCost();
        this.dept = binStock.getDept();
        this.branch = binStock.getBranch();
        this.lockStatus = binStock.getLockStatus();
        this.lTrnNo = binStock.getLTrnNo();
        this.gid = binStock.getGid();
        this.delStatus = binStock.getDelStatus();
        this.binType = binStock.getBinType();
        this.cid = binStock.getCid();
        this.orderId = binStock.getOrderId();
        this.purchaseBillId = binStock.getPurchaseBillId();
        this.materialTransactionId = binStock.getMaterialTransactionId();
    }

    public BinStock(BinStockDto binStock, BinStock binStockObj) {

        this.refId = binStock.getProductId();
        this.batchNo = binStock.getBatchNo();
        this.batchId = binStockObj.getBatchId();
        this.binId = binStockObj.getBinId();
        this.binName = binStock.getBinName();
        this.lot = binStock.getLot();
        this.stkQty = binStock.getTakenIssued();
        this.sStkQty = binStockObj.getStkQty();
        this.avgCost = binStockObj.getAvgCost();
        this.cost = binStockObj.getCost();
        this.dept = binStock.getDept();
        this.branch = binStock.getBranch();
        this.lockStatus = binStockObj.getLockStatus();
        this.lockType = binStockObj.getLockType();
        this.lTrnNo = binStock.getLTrnNo();
        this.gid = binStock.getGid();
        this.delStatus = binStockObj.getDelStatus();
        this.binType = binStockObj.getBinType();
        this.cid = binStockObj.getCid();
        this.orderId = binStockObj.getOrderId();
        this.purchaseBillId = binStockObj.getPurchaseBillId();
        this.materialTransactionId = binStockObj.getMaterialTransactionId();
        this.category = binStockObj.getCategory();
        this.lockType = binStockObj.getLockType();
        this.oilFFA = binStockObj.getOilFFA();
        this.inwarDate = binStockObj.getInwarDate();
        this.location = binStockObj.getLocation();
        this.campName = binStockObj.getCampName();
        this.seedcolor = binStockObj.getSeedcolor();
        this.seedMoisture = binStockObj.getSeedMoisture();
        this.mrnStatus = binStockObj.getMrnStatus();
        this.blendingLock = binStockObj.getBlendingLock();
        this.adate = binStockObj.getAdate();
    }



    public BinStock(BinStock binStock) {

        this.refId = binStock.getRefId();
        this.batchNo = binStock.getBatchNo();
        this.batchId = binStock.getBatchId();
        this.binId = binStock.getBinId();
        this.binName = binStock.getBinName();
        this.lot = binStock.getLot();
        this.stkQty = binStock.getStkQty();
        this.sStkQty = binStock.getStkQty();
        this.avgCost = binStock.getAvgCost();
        this.cost = binStock.getCost();
        this.dept = binStock.getDept();
        this.branch = binStock.getBranch();
        this.lockStatus = binStock.getLockStatus();
        this.lockType = binStock.getLockType();
        this.lTrnNo = binStock.getLTrnNo();
        this.gid = binStock.getGid();
        this.delStatus = binStock.getDelStatus();
        this.binType = binStock.getBinType();
        this.cid = binStock.getCid();
        this.orderId = binStock.getOrderId();
        this.purchaseBillId = binStock.getPurchaseBillId();
        this.materialTransactionId = binStock.getMaterialTransactionId();
        this.category = binStock.getCategory();
        this.lockType = binStock.getLockType();
        this.oilFFA = binStock.getOilFFA();
        this.inwarDate = binStock.getInwarDate();
        this.location = binStock.getLocation();
        this.campName = binStock.getCampName();
        this.seedcolor = binStock.getSeedcolor();
        this.seedMoisture = binStock.getSeedMoisture();
        this.mrnStatus = binStock.getMrnStatus();
        this.blendingLock = binStock.getBlendingLock();
        this.adate = binStock.getAdate();
    }


    public BinStock(BinStock binStock, BinStockDto binStockDto) {

        this.refId = binStock.getRefId();
        this.batchNo = binStock.getBatchNo();
        this.batchId = binStock.getBatchId();
        this.binId = binStock.getBinId();
        this.binName = binStock.getBinName();
        this.lot = binStock.getLot();
        this.stkQty = binStock.getStkQty();
        this.sStkQty = binStock.getStkQty();
        this.avgCost = binStock.getAvgCost();
        this.cost = binStock.getCost();
        this.dept = binStock.getDept();
        this.branch = binStock.getBranch();
        this.lockStatus = binStock.getLockStatus();
        this.lockType = binStock.getLockType();
        this.lTrnNo = binStock.getLTrnNo();
        this.gid = binStock.getGid();
        this.delStatus = binStock.getDelStatus();
        this.binType = binStock.getBinType();
        this.cid = binStock.getCid();
        this.orderId = binStock.getOrderId();
        this.purchaseBillId = binStock.getPurchaseBillId();
        this.materialTransactionId = binStock.getMaterialTransactionId();
        this.category = binStock.getCategory();
        this.lockType = binStock.getLockType();
        this.oilFFA = binStock.getOilFFA();
        this.inwarDate = binStock.getInwarDate();
        this.location = binStock.getLocation();
        this.campName = binStock.getCampName();
        this.seedcolor = binStock.getSeedcolor();
        this.seedMoisture = binStock.getSeedMoisture();
        this.mrnStatus = binStock.getMrnStatus();
        this.blendingLock = binStock.getBlendingLock();
        this.adate = binStock.getAdate();
    }
}

