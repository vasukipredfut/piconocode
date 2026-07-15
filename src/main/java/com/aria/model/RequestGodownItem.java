package com.aria.model;

import java.util.List;

import jakarta.persistence.*;

import com.aria.dto.RequestGodownItemDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "request_items_godown")
public class RequestGodownItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "godown_external_id")
	private Long goDownExternalId;

	@Column(name = "item_id")
	private Long itemId;

	@Column(name = "item_Name")
	private String itemName;

	@Column(name = "select_Lot")
	private String selectLot;

	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails issueDepartment;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch issueBranch;

	@Column(name = "quantity")
	private String quantity;

	@Column(name = "stock_Quantity")
	private String stockQuantity;

	@Column(name = "allocated_Quantity")
	private String allocatedQuantity;

	@Transient
	private List<BinStock> binStock;

	@Column
	private String material_return;


	@Column(name = "sal_status")
	private String salStatus;

	public RequestGodownItem(RequestGodownItemDto requestGodownItemDto) {
		this.id = requestGodownItemDto.getId();
		this.goDownExternalId = requestGodownItemDto.getGoDownExternalId();
		this.itemId = requestGodownItemDto.getItemId();
		this.itemName = requestGodownItemDto.getItemName();
		this.selectLot = requestGodownItemDto.getSelectLot();
		this.quantity = requestGodownItemDto.getQuantity();
		this.stockQuantity = requestGodownItemDto.getStockQuantity();
		this.allocatedQuantity = requestGodownItemDto.getAllocatedQuantity();
		this.material_return = requestGodownItemDto.getMaterialReturn();
	}

	public RequestGodownItem(RequestGodownItem requestGodownItem, RequestGodownItemDto requestGodownItemDto) {
		this.id = requestGodownItem.getId();
		this.goDownExternalId = requestGodownItem.getGoDownExternalId();
		this.itemId = requestGodownItem.getItemId();
		this.itemName = requestGodownItem.getItemName();
		this.selectLot = requestGodownItem.getSelectLot();
		this.quantity = requestGodownItem.getQuantity();
		this.stockQuantity = requestGodownItem.getStockQuantity();
		this.allocatedQuantity = requestGodownItem.getAllocatedQuantity();
		this.material_return = requestGodownItemDto.getMaterialReturn();
	}

}
