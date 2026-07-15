package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "blending_receive_bin_map")
public class BlendingRequestReceiveBinMap {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blending_Detail_Id")
    private Long blendingDetailId;

    @Column(name = "casgroup")
    private String casgroup;

    @Column(name = "type_Id")   // issueId / externalGodownId
    private Long typeId;

    @Column(name = "trn_no")
    private String trnNo;

    @Column(name = "type")
    private String type;

    @Column(name = "bin_no")
    private Long BinNo;

    @Column(name = "batch_no")
    private String BatchNo;

    @Column(name = "qty")
    private Double Qty;

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
    private Double lot;


}
