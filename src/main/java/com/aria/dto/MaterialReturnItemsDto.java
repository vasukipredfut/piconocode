package com.aria.dto;

import com.aria.model.IssueItems;
import com.aria.model.MaterialReturnItems;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class MaterialReturnItemsDto {

    private Long id;


    private Long returnId;


    private Long itemId;

    private String itemName;
    private String qty;
    private String returnDate;
    private String status;

    public MaterialReturnItemsDto(MaterialReturnItems materialReturnItems) {
        DateUtil dateUtil = new DateUtil();
        this.id = materialReturnItems.getId();
        this.returnId = materialReturnItems.getReturnId();
        this.itemId = materialReturnItems.getItemId();
        this.itemName = materialReturnItems.getItemName();
        this.qty = materialReturnItems.getQuantity();
        this.returnDate = dateUtil.getDateTOStringWk(materialReturnItems.getReturnDate());
        this.status = materialReturnItems.getStatus();
    }

    public MaterialReturnItemsDto(IssueItems materialReturnItems) {
        this.id = materialReturnItems.getId();
        //this.returnId = materialReturnItems.getReturnId();
        this.itemId = materialReturnItems.getItemId();
        this.itemName = materialReturnItems.getItemName();
    }
}
