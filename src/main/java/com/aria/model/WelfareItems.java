
package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.WelfareItemsDto;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "welfare_items ")
public class WelfareItems {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;



	@Column(name = "welfare_category")
	private String welfareCategory;

	@Column
	private Date date;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@Column(name = "welfare_Items_Value")
    private String welfareItemsValue;	

	public WelfareItems(WelfareItemsDto welfareItems) {
		this.id = welfareItems.getId();
		this.welfareCategory = welfareItems.getWelfareCategory();
		this.createdBy = welfareItems.getCreatedBy();
		this.updatedBy = welfareItems.getUpdatedBy();
		this.created_date = welfareItems.getCreated_date();
		this.updated_date = welfareItems.getUpdated_date();
		ObjectMapper mapper = new ObjectMapper();
        try {
            this.welfareItemsValue = mapper.writeValueAsString(welfareItems.getWelfareItemsValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	}

