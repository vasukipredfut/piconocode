package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class InwardAuditDTO {

	private Long id;

	private String batchNo;

	private String entryType;

	private Date inwardDate;

	private Long productId;

	private String status;

	private String batch_type;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private String roqComment;

}
