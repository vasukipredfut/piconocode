package com.aria.model;

import com.aria.dto.BinDetailsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "bin_details ")
public class BinDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long binid;

	@Column
	private String batchno;

	@Column
	private String trn_no;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column
	private LocalDateTime updated_date;
	
	@Column(name = "allotted_quantity")
	private Double allottedQuantity;

	@Column(name = "bin_stock_id", updatable = false)
	private Long binStockId;

	@Column(name = "receive_bin_stock_id", updatable = false)
	private Long receiveBinStockId;

	public BinDetails(BinDetailsDto binDetails) {
		this.id = binDetails.getId();
		this.binid = binDetails.getBinid();
		this.batchno = binDetails.getBatchno();
		this.updatedBy = binDetails.getUpdatedBy();
		this.updated_date = binDetails.getUpdated_date();
	}
}
