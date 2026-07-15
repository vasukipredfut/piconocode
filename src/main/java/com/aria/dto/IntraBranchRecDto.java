package com.aria.dto;


import com.aria.model.Branch;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class IntraBranchRecDto {

	private Long id;
	private String docketNo;
	private String hupId;
	private String issueBranch;
	private String dispatchOn;
	private Long dispatchBy;
	private String status;
	private Long createdBy;
	private Long updatedBy;
	private LocalDateTime created_date;
	private LocalDateTime updated_date;
	private String dispatchByName;
	private String deliveryStaffName;


}
