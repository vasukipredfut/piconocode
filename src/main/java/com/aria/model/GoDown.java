package com.aria.model;

import com.aria.dto.GoDownDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "godown")
public class GoDown {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@Column
	private String address;

	@Column(name = "licenseno")
	private String licenseNo;

	@Column
	private Long cluster;

	@Column
	private Long branch;

	@Column
	private Long plant;

	@Column(name = "ebservice")
	private Long ebService;

	@Column(name = "genset")
	private String genSet;

	@Column(name = "gensetid")
	private Long genSetId;

	@Column(name = "final")
	private String finale;

	@Column
	private Date updatedon;

	@Column(name = "updatedby")
	private Long updatedBy;

	@Column
	private String godowntype;
	@Column
	private String external;

	@Column(name = "ledger_id")
	private Long ledgerId;

	@Column(name = "ho_supplier_id")
	private Long HOSupplierId;

	@Column(name = "print_order")
	private Long printOrder;

	@OneToOne
	@JoinColumn(name = "org_id", referencedColumnName = "id")
	private Organisation organisation;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@Column(name = "contract_Time_Peroid")
	private String contractTimePeroid;

	@Column(name = "from_date")
	@Temporal(TemporalType.DATE)
	private Date fromDate;

	@Column(name = "to_date")
	@Temporal(TemporalType.DATE)
	private Date toDate;

	@Column(name = "document_Path")
	private String documentPath;

	@Column(name = "godown_capacity")
	private String godownCapacity;

	public GoDown(GoDownDto goDown) {
		DateUtil dateUtil = new DateUtil();
		this.id = goDown.getId();
		this.name = goDown.getName();
		this.address = goDown.getAddress();
		this.licenseNo = goDown.getLicenseNo();
		this.cluster = goDown.getCluster();
		this.branch = goDown.getBranch();
		this.plant = goDown.getPlant();
		this.ebService = goDown.getEbService();
		this.genSet = goDown.getGenSet();
		this.genSetId = goDown.getGenSetId();
		this.finale = goDown.getFinale();
		this.godowntype = goDown.getGodowntype();
		if(goDown.getGodowntype() != null && goDown.getGodowntype().equalsIgnoreCase("ExternalGodown")){
			this.external = "Y";
		}
		this.updatedon = goDown.getUpdatedon();
		this.updatedBy = goDown.getUpdatedBy();
		this.ledgerId = goDown.getLedgerId();
		this.HOSupplierId = goDown.getHOSupplierId();
		this.printOrder = goDown.getPrintOrder();
		if (goDown.getFirmId() != null) {
			Organisation organisation = new Organisation();
			organisation.setId(goDown.getFirmId());
			this.organisation = organisation;
		}
		this.updated_date = goDown.getUpdated_date();
		this.updatedBy = goDown.getUpdatedBy();
		this.created_date = goDown.getCreated_date();
		this.createdBy = goDown.getCreatedBy();
		this.contractTimePeroid = goDown.getContractTimePeroid();
		if (goDown.getFromDate() != null) {
			this.fromDate = dateUtil.getStringTODateWk(goDown.getFromDate());
		}
		if (goDown.getToDate() != null) {
			this.toDate = dateUtil.getStringTODateWk(goDown.getToDate());
		}
		this.documentPath = goDown.getDocumentPath();
		this.godownCapacity = goDown.getGodownCapacity();

	}
}
