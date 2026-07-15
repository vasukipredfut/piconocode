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
@Table(name = "return_items")
public class MaterialReturnItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_id")
    private Long returnId;

    @Column(name = "item_Id")
    private Long itemId;

    @Column(name = "item_Name")
    private String itemName;


    @Column(name = "quantity")
    private String quantity;


    @Column
    private String status;


    @Column(name = "return_date")
    @Temporal(TemporalType.DATE)
    private Date returnDate;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column
    private LocalDateTime updated_date;

    public MaterialReturnItems(Tuple itemsDto) {
        this.itemId = Long.valueOf(itemsDto.get("itemId").toString());
        this.itemName = itemsDto.get("itemName").toString();
        this.quantity = itemsDto.get("qty").toString();
        this.status = "Pending";
    }

    public MaterialReturnItems(OtherBranchIssueItems itemsDto) {
        this.itemId = itemsDto.getItemId();
        this.itemName = itemsDto.getItemName();
        this.quantity = itemsDto.getAllocatedQuantity();
        this.status = "Pending";
    }
    public MaterialReturnItems(SendExternalItemsEntity itemsDto) {
        this.itemId = itemsDto.getSendExternalItemId();
        this.itemName = itemsDto.getSendExternalItemName();
        this.quantity = itemsDto.getSendExternalItemQuantity().toString();
        this.status = "Pending";
    }

    public MaterialReturnItems(RequestGodownItem itemsDto) {
        this.itemId = itemsDto.getItemId();
        this.itemName = itemsDto.getItemName();
        this.quantity = itemsDto.getAllocatedQuantity().toString();
        this.status = "Pending";
    }
}
