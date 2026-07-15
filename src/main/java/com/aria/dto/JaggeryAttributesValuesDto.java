package com.aria.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.JaggeryAttributesValues;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class JaggeryAttributesValuesDto {

	private Long id;

	private String uniqno;

	private Long color;

	private Long quality;

	private Long thul;

	private Long thulfrom;

	private Long thulto;

	/*
	 * private Long vabus;
	 * 
	 * 
	 * private Double vabqty;
	 */

	public Map<String, String> vabusValue;

	private Long gunny;

	private Long comment;

	public Map<String, String> weightValue;

	public JaggeryAttributesValuesDto(JaggeryAttributesValues attributesValues) {
		this.id = attributesValues.getId();
		this.uniqno = attributesValues.getUniqno();
		this.color = attributesValues.getColor();
		this.quality = attributesValues.getQuality();
		this.thul = attributesValues.getThul();
		this.thulfrom = attributesValues.getThulfrom();
		this.thulto = attributesValues.getThulto();
		// this.vabus = attributesValues.getVabus();
		// this.vabqty = attributesValues.getVabqty();
		this.gunny = attributesValues.getGunny();
		this.comment = attributesValues.getComment();
		ObjectMapper mapper = new ObjectMapper();
		if (attributesValues.getVabusValue() != null) {

			try {
				this.vabusValue = mapper.readValue(attributesValues.getVabusValue(), HashMap.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (attributesValues.getWeightValue() != null) {
			try {
				this.weightValue = mapper.readValue(attributesValues.getWeightValue(), HashMap.class);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
