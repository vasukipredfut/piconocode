package com.aria.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.LabelPricesDto;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "labelPrices")
public class LabelPrices {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "labelproductid")
	private Long productId;

	@Column(name = "labelLocationId")
	private Long locationId;

	@Column(name = "price")
	private double price;

	@Column
	private Double per_ml_price;

	@Column(name = "labelproductgroupid")
	private Long productGroupId;

	@Column(name = "status")
	private String status;

	@Column(name = "fromdate")
	private Date fromDate;

	@Column(name = "entereddate")
	private Date enteredDate;

	@Column(name = "ip")
	private String ip;

	@OneToOne
	@JoinColumn(name = "updatedby", referencedColumnName = "id")
	private User updatedBy;

	public LabelPrices(LabelPricesDto dto) {

		DateUtil dateUtil = new DateUtil();
		this.id = dto.getId();
		this.productId = dto.getProductId();
		this.locationId = dto.getLocationId();
		this.price = dto.getPrice();
		this.per_ml_price = dto.getPermlprice();
		this.productGroupId = dto.getProductGroupId();
		this.status = dto.getStatus();
		this.fromDate = dateUtil.getStringTODateWk(dto.getFromDate());
		this.enteredDate = dateUtil.getStringTODateWk(dto.getEnteredDate());
		this.ip = dto.getIp();
	}

}
