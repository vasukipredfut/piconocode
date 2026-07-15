package com.aria.dto;

import java.util.Date;
import java.util.List;

import com.aria.model.BinLock;
import com.aria.model.BinStock;

import com.aria.model.GogroupPalletNumberMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinStockDto {

	private Long id;

	private Long productId;

	private String productName;

	private String batchNo;

	private Long batchId;

	private String binId;

	private Long binName;

	private Double lot;

	private Double stkQty;

	private Double sStkQty;

	private Double avgCost;

	private Double cost;

	private Long dept;

	private String deptName;

	private Long branch;

	private String branchName;

	private String lockStatus;

	private String lockType;

	private String lTrnNo;

	private Long gid;

	private String delStatus;

	private String binType;

	private Long cid;

	private Long orderId;

	private Long purchaseBillId;

	private Long materialTransactionId;

	private Date inwardDate;

	private String location;

	private String campName;

	private String seedcolor;

	private String seedMoisture;

	private BinLock binLock;

	private Double takenIssued;

	private List<GogroupPalletNumberMap> palletNumbers;

	public BinStockDto(BinStock binStock) {
		this.id = binStock.getId();
		this.productId = binStock.getRefId();
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
		this.lockType = binStock.getLockType();
		this.lTrnNo = binStock.getLTrnNo();
		this.gid = binStock.getGid();
		this.delStatus = binStock.getDelStatus();
		this.binType = binStock.getBinType();
		this.cid = binStock.getCid();
		this.orderId = binStock.getOrderId();
		this.purchaseBillId = binStock.getPurchaseBillId();
		this.materialTransactionId = binStock.getMaterialTransactionId();
		this.inwardDate = binStock.getInwarDate();
		this.location = binStock.getLocation();
		this.campName = binStock.getCampName();
		this.seedcolor = binStock.getSeedcolor();
		this.seedMoisture = binStock.getSeedMoisture();
		// this.binLock = binStock.getBinLock();
	}
}
