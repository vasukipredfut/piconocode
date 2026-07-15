
package com.aria.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.AdminInspectionDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.time.LocalDateTime;
import jakarta.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "admin_inspection")
public class AdminInspection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Date date;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch branch;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@Column(name = "inspection_details_value")
	private String inspectionDetailsValue;

	public AdminInspection(AdminInspectionDto adminInspection) {
		this.id = adminInspection.getId();
		if(adminInspection.getBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(adminInspection.getBranchId());
			this.branch = branch;
		}
		this.createdBy = adminInspection.getCreatedBy();
		this.updatedBy = adminInspection.getUpdatedBy();
		this.created_date = adminInspection.getCreated_date();
		this.updated_date = adminInspection.getUpdated_date();
		ObjectMapper mapper = new ObjectMapper();
		try {
			this.inspectionDetailsValue = mapper.writeValueAsString(adminInspection.getInspectionDetailsValue());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
