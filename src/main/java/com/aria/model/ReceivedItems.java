
package com.aria.model;

import com.aria.dto.ReceivedItemsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ReceivedItems")
public class ReceivedItems {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String itemname;

	@Column
	private String containertype;

	@Column
	private String quantity;

	@OneToOne
	@JoinColumn(name = "org_id", referencedColumnName = "id")
	private Organisation organisation;

	@Column
	private int active;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public ReceivedItems(ReceivedItemsDto receivedItemsDetails) {
		this.id = receivedItemsDetails.getId();
		this.itemname = receivedItemsDetails.getItemname();
		this.containertype = receivedItemsDetails.getContainertype();
		this.quantity = receivedItemsDetails.getQuantity();
		this.active = receivedItemsDetails.getActive();
		this.createdBy = receivedItemsDetails.getCreatedBy();
		this.updatedBy = receivedItemsDetails.getUpdatedBy();
		this.created_date = receivedItemsDetails.getCreated_date();
		this.updated_date = receivedItemsDetails.getUpdated_date();
	}
}
