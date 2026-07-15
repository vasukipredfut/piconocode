
package com.aria.model;

import com.aria.dto.CanteenTokenDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "canteen_token")
public class CanteenToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@Column
	private Date date;

	@Column
	private String groups;
	
	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails employeeCategory;

	@Column(name = "token_for")
	private String tokenFor;

	@OneToOne
	@JoinColumn(name = "shift_id", referencedColumnName = "id")
	private ShiftDetails shift;

	@Column(name = "no_Of_Token")
	private Long noOfToken;

	@Column
	private Long price;

	@Column(name = "token_no")
	private Long tokenNo;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public CanteenToken(CanteenTokenDto canteenToken) {
		this.id = canteenToken.getId();
		if (canteenToken.getUserId() != null) {
			User user = new User();
			user.setId(canteenToken.getUserId());
			this.user = user;
		}
		this.groups = canteenToken.getGroups();
		if (canteenToken.getDeptId() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(canteenToken.getDeptId());
			this.employeeCategory = departmentDetails;
		}
		this.tokenFor = canteenToken.getTokenFor();
		if (canteenToken.getShiftId() != null) {
			ShiftDetails shiftDetails = new ShiftDetails();
			shiftDetails.setId(canteenToken.getShiftId());
			this.shift = shiftDetails;
		}
		this.noOfToken = canteenToken.getNoOfToken();
		this.price = canteenToken.getPrice();
		this.tokenNo = canteenToken.getTokenNo();
		this.createdBy = canteenToken.getCreatedBy();
		this.updatedBy = canteenToken.getUpdatedBy();
		this.created_date = canteenToken.getCreated_date();
		this.updated_date = canteenToken.getUpdated_date();
	}

}