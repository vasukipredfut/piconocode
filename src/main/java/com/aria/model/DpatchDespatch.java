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

import com.aria.dto.DpatchDispatchDto;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "dpatch_despatch")
public class DpatchDespatch {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "created_by", referencedColumnName = "id")
	private User createdBy;

	@OneToOne
	@JoinColumn(name = "order_ref_id", referencedColumnName = "id")
	private Order dpatch_Bill_Ref_Id;

	@Column(name = "trndate")
	private Date trndate;

	@Column(name = "active_flag")
	private Integer active_Flag;

	@Column(name = "status")
	private String status;

	@Column(name = "trn_no")
	private String trnNo;

	@OneToOne
	@JoinColumn(name = "approved_by", referencedColumnName = "id")
	private User approvedBy;


	@Column(name = "firm_id")
	private Long firmId;

	@Column(name = "branch_id")
	private Long branchId;


	public DpatchDespatch(DpatchDispatchDto dpatchDispatchDto) {
		DateUtil dateUtil = new DateUtil();
		this.active_Flag = 0;

		if (dpatchDispatchDto.getOrderId() != null) {
			Order order = new Order();
			order.setId(dpatchDispatchDto.getOrderId());
			this.dpatch_Bill_Ref_Id = order;
		}
		this.trnNo = dpatchDispatchDto.getTrnNo();
		this.trndate = dateUtil.getStringTODateWk(dpatchDispatchDto.getDeliveryDate());
		this.status = dpatchDispatchDto.getStatus();

	}
}