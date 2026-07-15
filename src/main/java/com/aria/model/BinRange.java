package com.aria.model;

import com.aria.dto.BinRangeDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "binrange")
public class BinRange {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long dept;

    @Column
    private Long branch;

    @Column(name = "slfm")
    private Long slFm;

    @Column(name = "slto")
    private Long slTo;

    @Column(name = "goalive")
    private String goAlive;


    @Column(name = "opbatch")
    private String opBatch;

    @Column(name = "trnprefix")
    private String trnPrefix;

    @Column(name = "docketno")
    private Long docketNo;

    @Column(name = "hubtrnno")
    private Long hubTrnNo;

    @Column(name = "hub")
    private String hub;

    @Column(name = "pocketno")
    private Long pocketNo;

    @OneToOne
    @JoinColumn(name = "godown_id", referencedColumnName = "id")
    private GoDown goDown;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by", updatable = false)
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column(name = "display_name")
    private String displayName;

    public BinRange(BinRangeDto binRange) {
        this.id = binRange.getId();
        this.dept = binRange.getDept();
        this.branch = binRange.getBranch();
        this.slFm = binRange.getSlFm();
        this.slTo = binRange.getSlTo();
        this.goAlive = binRange.getGoAlive();
        this.opBatch = binRange.getOpBatch();
        this.trnPrefix = binRange.getTrnPrefix();
        this.docketNo = binRange.getDocketNo();
        this.hubTrnNo = binRange.getHubTrnNo();
        this.hub = binRange.getHub();
        this.pocketNo = binRange.getPocketNo();
        if (binRange.getGoDownId() != null) {
            GoDown goDown = new GoDown();
            goDown.setId(binRange.getGoDownId());
            this.goDown = goDown;
        }
        if (binRange.getFirmId() != null) {
            Organisation organisation = new Organisation();
            organisation.setId(binRange.getFirmId());
            this.organisation = organisation;
        }
        this.updated_date = binRange.getUpdated_date();
        this.updatedBy = binRange.getUpdatedBy();
        this.created_date = binRange.getCreated_date();
        this.createdBy = binRange.getCreatedBy();
        this.displayName = binRange.getDisplayName();
    }
}


