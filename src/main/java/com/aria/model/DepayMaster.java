package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "depaymaster")
public class DepayMaster {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column
	private String code;
	
	@Column
	private String name;
	
	@Column
	private String fathername;
	
	@Column
	private Date jdate;
	
	@Column
	private String natWork;
	
	@Column
	private String designation;
	
	@Column
	private String scode;
	
	@Column
	private Long did;
	
	@Column
	private String department;
	
	@Column
	private Long bid;
	
	@Column
	private String branch;
	
	@Column
	private Long cid;
	
	@Column
	private String company;
	
	@Column
	private String bank;
	
	@Column
	private String bankacno;
	
	@Column
	private String esino;
	
	@Column
	private String epfno;
	
	@Column
	private Long bioid;
	
	@Column
	private String sex;
	
	@Column
	private Integer cshiftid;
	
	@Column
	private String woff;
	
	@Column
	private String contract;
	
	@Column
	private double basic;
	
	@Column
	private double da;
	
	@Column
	private String nshift;
	
	@Column
	private double allowance;
	
	@Column
	private Long empcode;
	
	@Column
	private String fixshift;
	
	@Column
	private String status;
	
	@Column
	private Integer ltrnweek;
	
	@Column
	private Integer lshiftid;
	
	@Column
	private String fps;
	
	@Column
	private String usecid;
	
	@Column
	private Integer cby;
	
	@Column
	private String edate;
	
	@Column
	private double desiper;
	
	@Column
	private double depfper;
	
	@Column
	private double eesiper;
	
	@Column
	private double eepf1per;
	
	@Column
	private double eepf2per;
	
	@Column
	private double epfbase;
	
	@Column
	private double esiceil;
	
	@Column
	private double desigid;
	
	@Column
	private String firm;
	
	@Column
	private String dispen;
	
	@Column
	private String lefton;
	
	@Column
	private String transferon;
	
	@Column
	private String entrymode;
	
	@Column
	private double oda;
	
	@Column
	private double otot;
	
	@Column
	private double oallow;
	
	@Column
	private String nat_wk1;
	
	@Column
	private String nwoff;
	
	@Column
	private Integer nshiftid;
	
	@Column
	private String owoff;
	
	@Column
	private Integer eshiftid;
	
	@Column
	private String esiexemp;
	
	@Column
	private double tds;
	
	@Column
	private String epfexempted;
	
	@Column
	private String bank_ifsc;
	
}
