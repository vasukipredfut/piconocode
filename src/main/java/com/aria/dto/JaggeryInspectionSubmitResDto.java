package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class JaggeryInspectionSubmitResDto {

	private Long billInwardId;
	private Long vendorId;
	private String vendorName;
	private String deliverBranchName;
	private Double qty;
	private String billNo;
	private String billDate;
	private String jagDate;
    private String unitRate;
    private String place;

	private List<String> lots;
	List<JaggeryAttributesDto> attributesDtoList;

	public JaggeryInspectionSubmitResDto(JaggeryInspectionSubmitRes jaggeryInspectionSubmitRes) {
		if (jaggeryInspectionSubmitRes.getBillInwardId() != null) {
			this.billInwardId = jaggeryInspectionSubmitRes.getBillInwardId();
		}
		this.vendorId = jaggeryInspectionSubmitRes.getVendorId();
		this.vendorName = jaggeryInspectionSubmitRes.getVendorName();
		this.deliverBranchName = jaggeryInspectionSubmitRes.getDeliverBranchName();
		this.qty = jaggeryInspectionSubmitRes.getQty();
		this.billNo = jaggeryInspectionSubmitRes.getBillNo();
		this.place = jaggeryInspectionSubmitRes.getPlace();
	}

}
