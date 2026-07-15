package com.aria.model;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.DpatchItemsBatchDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@NoArgsConstructor
@Table(name = "dpatch_items_batch")
@Getter
@Setter
@ToString
public class DpatchItemsBatch {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	@JoinColumn(name = " dpatch_Items_Ref_Id", referencedColumnName = "id")
	private DpatchBillorderItems dpatch_Items_Ref_Id;
	
	@OneToOne
	@JoinColumn(name = " binstock_Ref_Id", referencedColumnName = "id")
	private BinStock binstock_Ref_Id;
	
	@OneToOne
	@JoinColumn(name = " batch_Ref_Id", referencedColumnName = "id")
	private BatchMaster batch_Ref_Id;
	
	@OneToOne
	@JoinColumn(name = " bin_Ref_Id", referencedColumnName = "binprimaryId")
	private GoGroup bin_Ref_Id;
	
	@Column
	private Double quanity;
	
	@Column
	private Long created_By;
	
	@Column
	private Date created_Timestamp;
	
	@Column
	private Date updated_Timestamp;
	
	@OneToOne
	@JoinColumn(name = " dpatch_Despatch_Ref_Id", referencedColumnName = "id")
	private DpatchDespatch dpatch_Despatch_Ref_Id;
	
	
	public DpatchItemsBatch(DpatchItemsBatchDto order) {
		DpatchBillorderItems dpatch=new DpatchBillorderItems();
		dpatch.setId(order.getDpatch_Items_Ref_Id());
		
		DpatchDespatch despatch =new DpatchDespatch();
		despatch.setId(order.getDpatch_Despatch_Ref_Id());
		
		BatchMaster batchMaster=new BatchMaster();
		batchMaster.setId(order.getBatch_Ref_Id());
		
		GoGroup goGroup=new GoGroup();
		goGroup.setId(order.getBin_Ref_Id());
		
		BinStock bin=new BinStock();
		bin.setId(order.getBinstock_Ref_Id());
		this.binstock_Ref_Id=bin;
		
		this.quanity=order.getQuanity();
		
		this.created_By=order.getCreated_By();
		
		java.util.Date date=new java.util.Date();
        Date sqlDate = new Date(date.getTime());
        this.created_Timestamp=sqlDate;   
	}	
}