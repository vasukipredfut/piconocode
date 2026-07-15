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
@Table(name = "view_accounts_ledger")
public class ViewAccountsLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trnno")
	private String trnno;

	@Column(name = "trn_amount")
	private Double trnAmount;

	@Column(name = "trn_Type")
	private Long trnType;

	@Column(name = "party_id")
	private Long partyId;

	@Column(name = "trn_date")
	@Temporal(TemporalType.DATE)
	private Date trnDate;

	@Column(name = "description")
	private String description;

	@Column(name = "prepared_By")
	private Long preparedBy;

	@Column(name = "verified_By")
	private Long verifiedBy;

	@Column(name = "approved_By")
	private Long approvedBy;

	@Column(name = "authorize_By")
	private Long authorizeBy;

	@Column(name = "branch")
	private Long branch;

	@Column(name = "company")
	private Long company;

	@Column(name = "ref_date")
	@Temporal(TemporalType.DATE)
	private Date refDate;  // bill_date , accountingDate
}
