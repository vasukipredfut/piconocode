package com.aria.dto;

import com.aria.model.BinRange;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
public class BinRangeDto {

    private Long id;

    private Long dept;

    private String deptName;

    private Long branch;

    private String branchName;

    private Long slFm;

    private Long slTo;

    private String goAlive;

    private String opBatch;

    private String trnPrefix;

    private Long docketNo;

    private Long hubTrnNo;

    private String hub;

    private Long pocketNo;

    private Long goDownId;

    private String goDownName;
    private String displayName;

    private Long firmId;

    private String firmName;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private String createdDate;

    private LocalDateTime updated_date;


    public BinRangeDto(BinRange binRange) {
        DateUtil dateUtil = new DateUtil();
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
        if (binRange.getGoDown() != null) {
            this.goDownId = binRange.getGoDown().getId();
            this.goDownName = binRange.getGoDown().getName();
        }
        if (binRange.getOrganisation() != null) {
            this.firmId = binRange.getOrganisation().getId();
            this.firmName = binRange.getOrganisation().getName();
        }
        this.updated_date = binRange.getUpdated_date();
        this.updatedBy = binRange.getUpdatedBy();
        this.createdDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(binRange.getCreated_date());
        this.created_date = binRange.getCreated_date();
        this.createdBy = binRange.getCreatedBy();
        this.displayName = binRange.getDisplayName();
    }
}
