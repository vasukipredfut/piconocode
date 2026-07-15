package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "blending_request_preprocess_bin_map")
public class BlendingRequestAndPreprocessBinMap {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blending_Detail_Id")
    private Long blendingDetailId;

    /*
    @Column(name = "blending_advice_stock_id")
    private Long blendingAdviceStockId;*/

    @Column(name = "casgroup")
    private String casgroup;

    @Column(name = "type_Id")   // requestId / externalGodownId
    private Long typeId;

    @Column(name = "trn_no")
    private String trnNo;

    @Column(name = "type")
    private String type;

}
