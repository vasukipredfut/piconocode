package com.aria.dto;

import java.util.Date;

import com.aria.model.SalesProductFilmOrder;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesProductFilmOrderDto {

	private Long id;

	private Long filmId;

	private String filmName;

	private String location;

	private Long subLocationId;

	private String subLocationName;

	private Long lot;

	private Double mrp;
	private Double perMlPrice;

	private Double orderQty;

	private Double orderQtyKg;

	private Double completedQty;

	private Long stockInPouch;

	private Double pendingQty;

	private String orderDate;
	private String orderDateTime;

	private String packedOn;

	private String mattersToPrint;

	private Long orderedBy;

	private String orderedByName;

	private Long orderBranch;

	private String branchName;

	private String mrnStatus;

	private Date completedDate;

	private String printedBy;

	private String mrnNo;

	private String specialInstructions;

	private Long cancelledBy;

	private String ip;

	private String bestBefore;
	private String altValue;

	public SalesProductFilmOrderDto(SalesProductFilmOrder model) {
		DateUtil dateUtil = new DateUtil();
		this.id = model.getId();
		if (model.getFilmId() != null) {
			this.filmId = model.getFilmId().getId();
			this.filmName = model.getFilmId().getName();
		}
		this.location = model.getLocation();
		this.subLocationId = model.getSubLocationId();
		this.mrp = model.getMrp();
		this.lot = model.getLot();
		this.packedOn = model.getPackedOn();
		this.bestBefore = dateUtil.getDateTOStringMMYYYY(model.getBestBefore());
		this.orderQty = model.getOrderQty();
		this.orderQtyKg = model.getOrderQtyKg();
		this.completedQty = model.getPrintedQty();
		this.stockInPouch = model.getStockInPouch();
		this.pendingQty = model.getBalance();
		this.orderedBy = model.getOrderedBy();
		this.orderDate = dateUtil.getDateTOStringWk(model.getOrderDate());
		this.orderDateTime = dateUtil.getDateTOStringDATETIME(model.getOrderDate());
		this.orderBranch = model.getOrderBranch();
		this.mattersToPrint = model.getMattersToPrint();
		this.specialInstructions = model.getSpecialInstructions();
		this.cancelledBy = model.getCancelledBy();
		this.completedDate = model.getCompletedDate();
		this.printedBy = model.getPrintedBy();
		this.ip = model.getIp();
		this.mrnNo = "RQ - " + model.getMrnNo();
		this.mrnStatus = model.getMrnStatus();
		if(model.getPerMlPrice() != null) {
			this.perMlPrice = model.getPerMlPrice();
		}
	}

}
