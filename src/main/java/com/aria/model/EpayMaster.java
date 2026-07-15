package com.aria.model;

import java.util.Date;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "epaymaster")
public class EpayMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long empcode;
	
	@Column
	private String name;
	
	@Column
	private Integer pleave;
	
	@Column
	private Long trnyear;
	
	@Column
	private String status;
	
	@Column
	private Date jdate;
	
	@Column
	private String natWork;
	
	@Column
	private Long dacatid;
	
	@Column
	private Integer basic;
	
	@Column
	private Integer da;
	
	@Column
	private Integer hra;
	
	@Column
	private Integer tot;
	
	@Column
	private Integer epf;
	
	@Column
	private Integer esi;
	
	@Column
	private Integer tds;
	
	@Column
	private Integer tdeduction;
	
	@Column
	private Integer netsalary;
	
	@Column
	private Integer eesi;
	
	@Column
	private Integer epfbase;
	
	@Column
	private Integer epf1;
	
	@Column
	private Integer epf2;
	
	@Column
	private String sex;
	
	@Column
	private Long esino;
	
	@Column
	private String epfno;
	
	@Column
	private String dispen;
	
	@Column
	private Long bankacno;
	
	@Column
	private String bankcode;
	
	@Column
	private String year;
	
	@Column
	private String month;
	
	@Column
	private Integer texp;
	
	@Column
	private Integer hday;
	
	@Column
	private Integer gleave;
	
	@Column
	private Integer gperm;
	
	@Column
	private Integer fleave;
	
	@Column
	private Integer tpresent;
	
	@Column
	private Integer tabsent;
	
	@Column
	private Integer tnonil;
	
	@Column
	private Integer tleave;
	
	@Column
	private Integer phleave;
	
	@Column
	private Integer totleave;
	
	@Column
	private Integer aleave;
	
	@Column
	private Integer tperm;
	
	@Column
	private Integer tlate;
	
	@Column
	private Integer lsmf;
	
	@Column
	private Integer blsalary;
	
	@Column
	private Integer flsalary;
	
	@Column
	private Integer lsalary;
	
	@Column
	private Integer alsalary;
	
	@Column
	private LocalDateTime updatedon;
	
	@Column
	private Integer tnolate;
	
	@Column
	private Integer tnoperm;
	
	@Column
	private Integer tnoleave;
	
	@Column
	private Long company;
	
	@Column
	private String firm;
	
	@Column
	private String payon;
	
	@Column
	private Date lpaid;
	
	@Column
	private Integer desiper;
	
	@Column
	private Integer depfper;
	
	@Column
	private Integer eesiper;
	
	@Column
	private Integer eepf1per;
	
	@Column
	private Integer eepf2per;
	
	@Column
	private String scode;
	
	@Column
	private Long did;
	
	@Column
	private Long bid;
	
	@Column
	private Long esiceil;
	
	@Column
	private Long bioid;
	
	@Column
	private String fps;
	
	@Column
	private String designation;
	
	@Column
	private String fpmode;
	
	@Column
	private String eexem;
	
	@Column
	private String lstatus;
	
	@Column
	private Integer mwdays;
	
	@Column
	private Integer mldays;
	
	@Column
	private Integer mlpay;
	
	@Column
	private Integer mlsalary;
	
	@Column
	private Integer lincentive;
	
	@Column
	private Integer msdays;
	
	@Column
	private Long desigid;
	
	@Column
	private String usecid;
	
	@Column
	private Integer lnleave;
	
	@Column
	private Long cby;
	
	@Column
	private String flamt;
	
	@Column
	private Date edate;
	
	@Column
	private Integer oda;
	
	@Column
	private Integer obasic;
	
	@Column
	private String chstatus;
	
	@Column
	private Long chby;
	
	@Column
	private LocalDateTime changedon;
	
	@Column
	private Integer oda1;
	
	@Column
	private Integer ohra1;
	
	@Column
	private String  epfexem;
	
	@Column
	private String code;
	
	@Column
	private String gacode;
	
	@Column
	private String esiexem;
	
	@Column
	private String bank_ifsc;
	
	@Column
	private Double perdayamount; 
	
	@Transient
	private String jjDate;
	
	@Transient
	private String eeDate;
	
	@Transient
	private String llDate;
		
}
