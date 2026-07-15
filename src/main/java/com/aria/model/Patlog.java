package com.aria.model;

import com.aria.dto.PatlogDto;
import com.aria.dto.UnitsDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "patlog")
public class Patlog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String logtype;

	@Column
	private Long logid;

	@Column
	private Long schid;

	@Column
	private Long mcid;

	@Column
	private String batchno;

	@Column
	private Long batchid;

	/*@Column
	private Long tcid;*/

	@Column
	private Long tgid;

	/*@Column
	private Long taskid;*/

	@Column
	private Long atid;

	@Column
	private Long resid;

	@Column
	private String resval;

	@Column
	private int resscore;

	@Column
	private String scoreto;

	@Column
	private Long scoretoid;

	@Column
	private String tno;

	@Column
	@Temporal(TemporalType.DATE)
	private Date entry;

	@Column
	private String restype;

	@Column
	private String dcolor;

	@Column
	private String dunit;

	@Column
	private Long mmcid;

	@Column
	private Long mcauxid;

	@Column
	private String mctable;

	@Column
	private String mcauxtable;

	@Column
	private String mmctable;

	@Column
	private Long shiftid;

	@Column
	private Date shiftdate;

	@Column
	private Long kpi;

	@Column
	private Long proreportdbid;

	@Column
	private String journalflag;


	@Column(name = "no_of_bag")
	private String noOfBag;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;



	public Patlog(PatlogDto model) {
		DateUtil dateUtil = new DateUtil();
		this.id = model.getId();	
		this.logid = model.getLogid();
		this.logtype = model.getLogtype();
		this.schid = model.getSchid();
		this.mcid = model.getMcid();
		this.batchno = model.getBatchno();
		this.batchid = model.getBatchid();
		//this.tcid = model.getTcid();
		this.tgid = model.getTgid();
		//this.taskid = model.getTaskid();
		this.atid = model.getAtid();
		this.resid = model.getResid();
		this.resval = model.getResval();
		this.resscore = model.getResscore();
		this.scoreto = model.getScoreto();
		this.scoretoid = model.getScoretoid();
		this.tno = model.getTno();
		//this.entry = model.getEntry();
		this.restype = model.getRestype();
		this.dcolor = model.getDcolor();
		this.dunit = model.getDunit();
		this.mmcid = model.getMcid();
		this.mcauxid = model.getMcauxid();
		this.mctable = model.getMctable();
		this.mcauxtable = model.getMcauxtable();
		this.mmctable = model.getMctable();
		this.shiftid = model.getShiftid();
        //this.shiftdate = model.getShiftdate()
		this.kpi = model.getKpi();
		this.proreportdbid = model.getProreportdbid();
		this.journalflag = model.getJournalflag();
		this.noOfBag = model.getNoOfBag();
		this.createdBy = model.getCreatedBy();
		this.updatedBy = model.getUpdatedBy();
		this.created_date = model.getCreated_date();
		this.updated_date = model.getUpdated_date();

	}
}
