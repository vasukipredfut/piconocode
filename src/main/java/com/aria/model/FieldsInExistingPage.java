package com.aria.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.aria.dto.FieldsInExistingPageDto;
import com.aria.dto.FoodItemDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "fields_details")
public class FieldsInExistingPage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "field_name")
	private String fieldName;

	@Column(name = "field_size")
	private String fieldSize;

	@Column(name = "field_data_type")
	private String fieldDataType;

	@Column(name = "possible_data_type")
	private String possibleDataType;

	@Column(name = "field_type")
	private String fieldType;

	@Column(name = "database_table")
	private String databaseTable;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_By")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@Column(name = "page_Id")
    private Long pageId;

	@Column(name = "field_Position")
    private Long fieldPosition;

	@Column(name = "new_Db_Table")
    private String newDbTable;

	public FieldsInExistingPage(FieldsInExistingPageDto fields) {
		this.id = fields.getId();
		this.fieldName = fields.getFieldName();
		this.fieldSize = fields.getFieldSize();
		this.fieldDataType = fields.getFieldDataType();
		this.possibleDataType = fields.getPossibleDataType();
		this.fieldType = fields.getFieldDataType();
		this.databaseTable = fields.getDatabaseTable();
		this.newDbTable = fields.getNewDbTable();
		this.fieldPosition = fields.getFieldPosition();
		this.createdBy = fields.getCreatedBy();
		this.updatedBy = fields.getUpdatedBy();
		this.created_date = fields.getCreated_date();
		this.updated_date = fields.getUpdated_date();
	}

	
	

	
}
