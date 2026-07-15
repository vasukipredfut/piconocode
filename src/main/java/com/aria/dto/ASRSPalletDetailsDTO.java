package com.aria.dto;


import com.aria.model.ASRSPalletDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ASRSPalletDetailsDTO {


    private Long id;

    private String palletNumber;

    private String palletQty;

    private String allocatedQty;

    private String palletType;

    private String deduction;

    private String status;

    private String partCategory;

    private String asrsPalletNumber;

    private String finalASRSQty;

    private String type; 


    public ASRSPalletDetailsDTO(ASRSPalletDetails asrsPalletDetails) {
        this.id = asrsPalletDetails.getId();
        this.palletNumber = asrsPalletDetails.getPalletNumber();
        this.palletQty = asrsPalletDetails.getPalletQty();
        this.allocatedQty = asrsPalletDetails.getAllocatedQty();
        this.palletType = asrsPalletDetails.getPalletType();
        this.deduction = asrsPalletDetails.getDeduction();
        this.asrsPalletNumber = asrsPalletDetails.getAsrsPalletNumber();
        this.finalASRSQty = asrsPalletDetails.getFinalASRSQty();
        this.partCategory=asrsPalletDetails.getPartCategory();
        this.status=asrsPalletDetails.getStatus();
    }
}
