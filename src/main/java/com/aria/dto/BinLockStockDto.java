package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinLockStockDto {

    public  Long id;

    public Long refId;

    public String batchNo;

    public Long batchId;

    public String binId;

    public Long binName;

    public Double lot;

    public Double stkQty;

    

    public Double avgCost;

    public Double cost;

    public Long dept;

    public Long branch;

    public String lockStatus;

    public String lTrnNo;

    public Long gid;

    public String delStatus;

    public String binType;

    public Long cid;

    public Long orderId;

    public Long purchaseBillId;

    public Long materialTransactionId;

    public String category;

    public String lockType;

    public Double oilFFA;

    public String inwarDate;

    public String location;

    public String seedcolor;

    public String seedMoisture;

    public String camp;

    public String place;

    public String state;

    public String lorrySerialNo;

    public BinLockStockDto(BinLockStock binLock) {
        DateUtil dateUtil = new DateUtil();
        this.id = binLock.getId();
        this.refId = binLock.getRefId();
        this.batchNo = binLock.getBatchNo();
        this.batchId = binLock.getBatchId();
        this.binId = binLock.getBinId();
        this.binName = binLock.getBinName();
        this.lot = binLock.getLot();
        this.stkQty = binLock.getStkQty();
        this.avgCost = binLock.getAvgCost();
        this.cost = binLock.getCost();
        this.dept = binLock.getDept();
        this.branch = binLock.getBranch();
        this.lockStatus = binLock.getLockStatus();
        this.lTrnNo = binLock.getlTrnNo();
        this.gid = binLock.getGid();
        this.delStatus = binLock.getDelStatus();
        this.binType = binLock.getBinType();
        this.cid = binLock.getCid();
        this.orderId = binLock.getOrderId();
        this.purchaseBillId = binLock.getPurchaseBillId();
        this.materialTransactionId = binLock.getMaterialTransactionId();
        this.category = binLock.getCategory();
        this.lockType = binLock.getLockType();
        this.oilFFA = binLock.getOilFFA();
        if(binLock.getInwarDate() != null) {
            this.inwarDate = dateUtil.getDateTOStringWk(binLock.getInwarDate());
        }
        this.location = binLock.getLocation();
        this.seedcolor = binLock.getSeedcolor();
        this.seedMoisture = binLock.getSeedMoisture();
        this.camp = binLock.getCamp();
        this.place = binLock.getPlace();
        this.state = binLock.getState();
        this.lorrySerialNo = binLock.getLorrySerialNo();
    }
}
