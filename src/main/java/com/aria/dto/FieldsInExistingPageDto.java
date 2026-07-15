package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.FieldsInExistingPage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FieldsInExistingPageDto {

	private Long id;

	private String fieldName;

	private String fieldSize;

	private String fieldDataType;

	private String possibleDataType;

	private String fieldType;

	private String databaseTable;
	
	private Long fieldPosition;
	
	private String newDbTable;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public FieldsInExistingPageDto(FieldsInExistingPage fieldsByPageBuilder) {
		this.fieldName = fieldsByPageBuilder.getFieldName();
		this.fieldSize = fieldsByPageBuilder.getFieldSize();
		this.fieldDataType = fieldsByPageBuilder.getFieldDataType();
		this.possibleDataType = fieldsByPageBuilder.getPossibleDataType();
		this.fieldType = fieldsByPageBuilder.getFieldType();
		this.databaseTable = fieldsByPageBuilder.getDatabaseTable();
		this.newDbTable = fieldsByPageBuilder.getNewDbTable();
		this.fieldPosition = fieldsByPageBuilder.getFieldPosition();
		this.id=fieldsByPageBuilder.getId();
		this.createdBy = fieldsByPageBuilder.getCreatedBy();
		this.updatedBy = fieldsByPageBuilder.getUpdatedBy();
		this.created_date = fieldsByPageBuilder.getCreated_date();
		this.updated_date = fieldsByPageBuilder.getUpdated_date();
	}

	public FieldsInExistingPageDto(String dt, String fieldName, String tableName) {
		String fieldDataType = "String";
		if (dt.equalsIgnoreCase("Integer") || dt.equalsIgnoreCase("int") || dt.equalsIgnoreCase("Long")) {
			fieldDataType = "Number";
		}
		this.fieldDataType = fieldDataType;
		this.fieldName = fieldName;
		this.fieldSize = "Medium";
		this.fieldType = dt;
		this.databaseTable = tableName;
	}

//	public static class FieldsByPageBuilder {
//
//		private Long createdBy;
//
//		private Long updatedBy;
//
//		private LocalDateTime created_date;
//
//		private LocalDateTime updated_date;
//
//		private Long id;
//
//		private String fieldName;
//
//		private String fieldSize;
//
//		private String fieldDataType;
//
//		private String possibleDataType;
//
//		private String fieldType;
//
//		private String databaseTable;
//
//		public FieldsByPageBuilder id(Long id) {
//			this.id = id;
//			return this;
//		}
//
//		public FieldsByPageBuilder createdBy(Long createdBy) {
//			this.createdBy = createdBy;
//			return this;
//		}
//
//		public FieldsByPageBuilder updatedBy(Long updatedBy) {
//			this.updatedBy = updatedBy;
//			return this;
//		}
//
//		public FieldsByPageBuilder created_date(LocalDateTime created_date) {
//			this.created_date = created_date;
//			return this;
//		}
//
//		public FieldsByPageBuilder updated_date(LocalDateTime updated_date) {
//			this.updated_date = updated_date;
//			return this;
//		}
//
//		public FieldsByPageBuilder(String dt, String fieldName, String tableName) {
//			String fieldDataType = "String";
//			if (dt.equalsIgnoreCase("Integer") || dt.equalsIgnoreCase("int") || dt.equalsIgnoreCase("Long")) {
//				fieldDataType = "Number";
//			}
////		fields.setFieldDataType(fieldDataType);
////		fields.setFieldName(a.getName());
////		fields.setFieldSize("Medium");
////		fields.setFieldType(dt);
////		fields.setDatabaseTable(entityClass.getAnnotation(Table.class).name());
//			this.fieldName = fieldName;
//			this.fieldSize = "Medium";
//			this.fieldDataType = fieldDataType;
//			this.possibleDataType = null;
//			this.fieldType = dt;
//			this.databaseTable = tableName;
//		}
//
//		public FieldsByPageDto build() {
//			FieldsByPageDto fbp = new FieldsByPageDto(this);
//			return fbp;
//		}
//	}

}
