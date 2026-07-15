package com.aria.model;

import com.aria.dto.BlendingPreprocessRequestList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "blending_preprocess_request_receive_bin_map")
public class BlendingPreprocessRequestAndReceiveBinMap {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blending_Detail_Id")
    private Long blendingDetailId;

    @Column(name = "blending_advice_stock_id")
    private Long blendingAdviceStockId;

    @Column(name = "casgroup")
    private String casgroup;

    @Column(name = "trn_no")
    private String trnNo;

    @Column(name = "bin_no")
    private Long binNo;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "qty")
    private Double qty;

    @Column(name = "status")
    private String status;

    @Column(name = "branch")
    private Long branch;

    @Column(name = "dept")
    private Long dept;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "lot")
    private double lot;

    public BlendingPreprocessRequestAndReceiveBinMap(BlendingPreprocessRequestList requestList) {
        this.blendingDetailId = requestList.getBlendingdetailsId();
        this.blendingAdviceStockId = requestList.getBlendingAdviceStockId();
        this.itemId = requestList.getProductId();
        this.casgroup = requestList.getCasgroup();
        this.batchNo = requestList.getBatchNo();
        this.batchId = requestList.getBatchId();
        this.binNo = requestList.getBinNo();
        this.lot = requestList.getLot();
    }

}
