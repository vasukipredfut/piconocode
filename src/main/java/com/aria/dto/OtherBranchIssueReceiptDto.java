package com.aria.dto;


import com.aria.model.WithInBranchReceiveItems;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OtherBranchIssueReceiptDto {

    private Long id;

    private String itemName;

    private Long itemId;

    private Long withinId;

    private String quantity;

    private String packageType;
    
    private String docketType;

    private String packageQuantity;


}
