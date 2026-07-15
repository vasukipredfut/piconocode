package com.aria.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.aria.model.DeliteStockMoveRequest;
import com.aria.utils.DateUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliteStockMoveRequestDto {

    private Long id;

    private Long gid;

    private Long fromBranchId;

    private String fromBranchName;

    private Long toBranchId;

    private String toBranchName;

    private Long fromDeptId;

    private String fromDeptName;

    private Long toDeptId;

    private String toDeptName;

    private Long destinationBinNo;

    private String requestDate;

    private Long updatedBy;

    private String completedDate;

    private Long toGid;

    private String kalasalFromDate;

    private String kalasalToDate;

    private Long activatedBy;

    private String activationStatus;

    private String activatedOn;

    private Long materialRequestId;

    private String materialRequestRqId;
    private Long firmId;

    List<DeliteStockMoveRequestBinDto> deliteStockMoveRequestBins;

    public DeliteStockMoveRequestDto(DeliteStockMoveRequest deliteStockMoveRequest) {
        this.id = deliteStockMoveRequest.getId();
        this.gid = deliteStockMoveRequest.getGid();
        this.fromDeptId = deliteStockMoveRequest.getFromDeptId();
        this.fromDeptName = deliteStockMoveRequest.getFromDeptName();
        this.toDeptId = deliteStockMoveRequest.getToDeptId();
        this.toDeptName = deliteStockMoveRequest.getToDeptName();
        this.fromBranchId = deliteStockMoveRequest.getFromBranchId();
        this.fromBranchName = deliteStockMoveRequest.getFromBranchName();
        this.toBranchId = deliteStockMoveRequest.getToBranchId();
        this.toBranchName = deliteStockMoveRequest.getToBranchName();
        this.destinationBinNo = deliteStockMoveRequest.getDestinationBinNo();
        this.requestDate = DateUtil.localDateTimeToString(deliteStockMoveRequest.getRequestDate());
        this.updatedBy = deliteStockMoveRequest.getUpdatedBy();
        this.completedDate = DateUtil.localDateTimeToString(deliteStockMoveRequest.getCompletedDate());
        this.toGid = deliteStockMoveRequest.getToGid();
        this.kalasalFromDate = DateUtil.localDateToString(deliteStockMoveRequest.getKalasalFromDate());
        this.kalasalToDate = DateUtil.localDateToString(deliteStockMoveRequest.getKalasalToDate());
        this.activatedBy = deliteStockMoveRequest.getActivatedBy();
        this.activationStatus = deliteStockMoveRequest.getActivationStatus();
        this.activatedOn = DateUtil.localDateToString(deliteStockMoveRequest.getActivatedOn());
        this.materialRequestId = deliteStockMoveRequest.getMaterialRequestId();
        this.firmId = deliteStockMoveRequest.getFirmId();
        this.materialRequestRqId = "RQ-"+ deliteStockMoveRequest.getMaterialRequestId();
        this.deliteStockMoveRequestBins  = deliteStockMoveRequest.getDeliteStockMoveRequestBins() != null ? deliteStockMoveRequest.getDeliteStockMoveRequestBins().stream().map(obj -> new DeliteStockMoveRequestBinDto(obj)).collect(Collectors.toList()) : null;;
    }
}
