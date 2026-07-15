package com.aria.dto;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import com.aria.model.BatchMaster;
import com.aria.model.DpatchBillorderItems;
import com.aria.model.DpatchDespatch;
import com.aria.model.GoGroup;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



@NoArgsConstructor
@Getter
@Setter
@ToString
public class DpatchItemsBatchDto {
	
	private Long id;
	
	private Long dpatch_Items_Ref_Id;
	
	private Long binstock_Ref_Id;
	
	private Long batch_Ref_Id;
	
	private Long bin_Ref_Id;
	
	private Double quanity;
	
	private Long created_By;
	
	private String created_Timestamp;
	
	private String updated_Timestamp;
	
	private Long dpatch_Despatch_Ref_Id;
}
