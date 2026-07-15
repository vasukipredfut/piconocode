package com.aria.dto;

import java.util.Date;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class transactionDTO {

	private Long refTrnId;
	private Long refId;
	private String trnNo;
	private String trnName;
	private Long issueBranchId;
	private Long requestBranchId;
	private Long issueDeptId;
	private Long requestDeptId;
	private String refIdProductName;
	private String trnDate;
	private String qty;



	public transactionDTO(TransactionList transaction) {
		DateUtil dateUtil= new DateUtil();
		this.refTrnId = transaction.getRefTrnId();
		this.trnNo = transaction.getTrnNo();
		this.trnName = transaction.getTrnName();
		this.issueBranchId = transaction.getIssueBranchId();
		this.requestBranchId = transaction.getRequestBranchId();
		this.issueDeptId = transaction.getIssueDeptId();
		this.requestDeptId = transaction.getRequestDeptId();
		this.refIdProductName = transaction.getRefIdProductName();
		this.refId = transaction.getRefId();
		if(transaction.getTrnDate() !=null ) {
			this.trnDate = dateUtil.getDateTOStringWk(transaction.getTrnDate());
		}
		this.qty = transaction.getQty();

	}

}
