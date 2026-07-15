package com.aria.model;

import com.aria.dto.BlendingAdvicePreProcessDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "blending_advice_preprocess")
public class BlendingAdvicePreProcess {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blending_advice_details_id")
    private Long blendingAdviceDetailsId;

    @Column(name = "cas_group")
    private String casGroup;

    @Column(name = "blending_advice_stock_id ")
    private Long blendingAdviceStockId ;

    @Column
    private String process;

    @OneToMany(mappedBy = "blendingAdvicePreprocessId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BlendingPreProcessBinDetails> preProcessBinDetails;


    public BlendingAdvicePreProcess(BlendingAdvicePreProcessDto blendingAdvicePreProcess) {
        this.id = blendingAdvicePreProcess.getId();
        this.blendingAdviceDetailsId = blendingAdvicePreProcess.getBlendingAdviceDetailsId();
        this.casGroup = blendingAdvicePreProcess.getCasGroup();
        this.process = blendingAdvicePreProcess.getProcess();
        this.blendingAdviceStockId = blendingAdvicePreProcess.getBlendingAdviceStockId();
    }
}
