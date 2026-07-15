package com.aria.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.GstTaxCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class HsnMasterDto {

	private Long id;
	private String hsnCode;
	private String itemCategory;
	private String category;
	private List<Map<Long, String>> mapItems;
	private List<Map<Long, String>> deleteItems;
	private double cgst;
	private double igst;
	private double sgst;

	public HsnMasterDto(GstTaxCategory gstTaxCategory, boolean check) {
		this.id = gstTaxCategory.getId();
		this.hsnCode = gstTaxCategory.getHsnCode();
		this.itemCategory = gstTaxCategory.getItemCategory();
		this.cgst = gstTaxCategory.getCgst();
		this.sgst = gstTaxCategory.getSgst();
		this.igst = gstTaxCategory.getIgst();
		this.category = gstTaxCategory.getCategory();
		if (check) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				if (gstTaxCategory.getItemListMap() != null) {
					this.mapItems = mapper.readValue(gstTaxCategory.getItemListMap(),
							new TypeReference<List<Map<Long, String>>>() {
							});
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
