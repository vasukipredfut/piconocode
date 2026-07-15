
package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "intra_branch_receipt_mappings")
public class IntraBranchReceiptMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "other_branch_issue_id")
	private Long otherBranchIssueId;

	@Column(name = "intra_branch_receipt_id")
	private Long intraBranchReceiptId;

}
