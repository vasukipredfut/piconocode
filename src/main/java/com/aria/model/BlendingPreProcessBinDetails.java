package com.aria.model;

import com.aria.dto.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "blending_advice_preprocess_bin_details")
public class BlendingPreProcessBinDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blending_advice_preprocess_id")
    private Long blendingAdvicePreprocessId;


    @Column(name = "bin_no")
    private Long binNo;

    @Column(name = "batchno")
    private String batchNo;

    @Column(name = "batchid")
    private Long batchId;
    @Column(name = "lot")
    private Double lot;

    public BlendingPreProcessBinDetails(BlendingPreProcessBinDetailsDto binDetails) {
        this.id = binDetails.getId();
        this.blendingAdvicePreprocessId = binDetails.getBlendingAdvicePreprocessId();
        this.binNo = binDetails.getBinNo();
        this.batchNo = binDetails.getBatchNo();
        this.batchId = binDetails.getBatchId();
        this.lot = binDetails.getLot();
    }
}
