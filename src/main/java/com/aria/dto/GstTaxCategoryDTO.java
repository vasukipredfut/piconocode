package com.aria.dto;

import com.aria.model.AssignDuty;
import com.aria.model.GstTaxCategory;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class GstTaxCategoryDTO {

	 private Long id;
	    private String hsnCode;
	    private String itemCategory;
	    private Double cgst;
	    private Double sgst;
	    private Double igst;
	    private LocalDateTime createdTimestamp;
	    private Long emasterId;
	    private String category;

	    public GstTaxCategoryDTO(GstTaxCategory gstTaxCategory) {
	        this.id = gstTaxCategory.getId();
	        this.hsnCode = gstTaxCategory.getHsnCode();
	        this.itemCategory = gstTaxCategory.getItemCategory();
	        this.cgst = gstTaxCategory.getCgst();
	        this.sgst = gstTaxCategory.getSgst();
	        this.igst = gstTaxCategory.getIgst();
	        this.createdTimestamp = gstTaxCategory.getCreatedTimestamp();
	        this.emasterId = gstTaxCategory.getEmasterId();
	        this.category = gstTaxCategory.getCategory();
	    }
}
