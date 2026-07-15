
package com.aria.model;

import com.aria.dto.InternalMaterialIssueDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "internal_material_issue")
public class InternalMaterialIssue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String requestreferance;

	@Column
	private String issuedepartment;

	@Column
	private String issueperson;

	@Column
	private Date issueddate;

	@Column(updatable = false)
	private Long createdBy;

	@Column
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	/*@OneToMany(mappedBy = "itemname", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "active=0")
	private List<RequestItem> requestItemDetails;*/

	public InternalMaterialIssue(InternalMaterialIssueDto internalMaterialIssue) {
		this.id = internalMaterialIssue.getId();
		this.requestreferance = internalMaterialIssue.getRequestreferance();
		this.issuedepartment = internalMaterialIssue.getIssuedepartment();
		this.issueperson = internalMaterialIssue.getIssueperson();
		this.issueddate = internalMaterialIssue.getIssueddate();
		this.createdBy = internalMaterialIssue.getCreatedBy();
		this.updatedBy = internalMaterialIssue.getUpdatedBy();
		this.created_date = internalMaterialIssue.getCreated_date();
		this.updated_date = internalMaterialIssue.getUpdated_date();

	}

}