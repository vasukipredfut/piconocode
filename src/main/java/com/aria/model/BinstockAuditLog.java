package com.aria.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "binstock_audit_log")
public class BinstockAuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long item_id;

	@Column
	private String trnno;

	@Column
	private String trn_type;

	@Column
	private Long bill_id;

	@Column
	private String batchno;

	@Column
	private Long batch_id;

	@Column
	private Long branch_id;

	@Column
	private Long firm_id;

	@Column
	private Double qty;

	@Column
	private Double avgcost;

	@Column
	private Double cost;

	@Column
	private Double lot;

	@Column
	private Double balance;

	@Column(name = "open_close_balance")
	private Double openOrCloseBalance;

	@Column
	private String naration;

	@Column
	private String entry_type;

	@Column
	@Temporal(TemporalType.DATE)
	private Date accouting_date;

	@Column
	@Temporal(TemporalType.DATE)
	private Date update_on;

	@Column(name = "sal_status")
	private String salStatus;

	@Column(name = "bin_no")
	private Long binNo;
	
	@Column(name = "godown_id")
	private Long godownId;
	
	
	
}
