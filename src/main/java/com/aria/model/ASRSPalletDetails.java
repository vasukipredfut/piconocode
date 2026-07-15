package com.aria.model;

import com.aria.dto.ASRSPalletDetailsDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "asrs_pallet")
public class ASRSPalletDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long asrsProductionId;

    @Column
    private String palletNumber;

    @Column
    private String palletQty;

    @Column
    private String allocatedQty;

    @Column
    private String palletType;


    @Column
    private String deduction;


    @Column
    private String asrsPalletNumber;


    @Column
    private String finalASRSQty;
//    
    @Column
    private String partCategory;

    @Column
    private String status;



    public ASRSPalletDetails(ASRSPalletDetailsDTO asrsPalletDetails){
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
