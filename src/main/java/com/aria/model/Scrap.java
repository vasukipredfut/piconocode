
package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;

import com.aria.dto.ScrapDto;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "scrap")
public class Scrap {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "scrap_Item")
	private String scrapItem;

	@Column(name = "stored_On")
	private Date storedOn;

	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails fromDepartment;

	@Column(name = "from_Process")
	private String fromProcess;

	@Column(name = "scrap_Type")
	private String scrapType;

	@Column(name = "select_Storage_Bin")
	private String selectStorageBin;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public Scrap(ScrapDto scrap) {
		this.id = scrap.getId();
		this.scrapItem = scrap.getScrapItem();
		if (scrap.getDeptId() != null) {
			DepartmentDetails fromDepartment = new DepartmentDetails();
			fromDepartment.setId(scrap.getDeptId());
			this.fromDepartment = fromDepartment;
		}
		this.fromProcess = scrap.getFromProcess();
		this.scrapType = scrap.getScrapType();
		this.selectStorageBin = scrap.getSelectStorageBin();
		this.createdBy = scrap.getCreatedBy();
		this.updatedBy = scrap.getUpdatedBy();
		this.created_date = scrap.getCreated_date();
		this.updated_date = scrap.getUpdated_date();
	}
}