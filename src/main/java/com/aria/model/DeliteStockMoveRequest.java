package com.aria.model;

import com.aria.dto.DeliteStockMoveRequestDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "delitestock_moverequest")
public class DeliteStockMoveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gid")
    private Long gid;

    @Column(name = "from_branch_id")
    private Long fromBranchId;

    @Column(name = "from_branch_name")
    private String fromBranchName;

    @Column(name = "to_branch_id")
    private Long toBranchId;

    @Column(name = "to_branch_name")
    private String toBranchName;

    @Column(name = "from_dept_id")
    private Long fromDeptId;

    @Column(name = "from_dept_name")
    private String fromDeptName;

    @Column(name = "to_dept_id")
    private Long toDeptId;

    @Column(name = "to_dept_name")
    private String toDeptName;

    @Column(name = "destination_bin_no")
    private Long destinationBinNo;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "to_gid")
    private Long toGid;

    @Column(name = "kalasal_from_date")
    private LocalDate kalasalFromDate;

    @Column(name = "kalasal_to_date")
    private LocalDate kalasalToDate;

    @Column(name = "activated_by")
    private Long activatedBy;

    @Column(name = "activation_status")
    private String activationStatus;

    @Column(name = "activated_on")
    private LocalDate activatedOn;

    @OneToMany(mappedBy = "delitestockMoverequestId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public List<DeliteStockMoveRequestBin> deliteStockMoveRequestBins;

    @Column(name = "material_request_id")
    private Long materialRequestId;
    @Column(name = "firm_Id", updatable = false)
    private Long firmId;

    public DeliteStockMoveRequest(DeliteStockMoveRequestDto deliteStockMoveRequestDto) {
        this.id = deliteStockMoveRequestDto.getId();
        this.gid = deliteStockMoveRequestDto.getGid();
        this.fromBranchId = deliteStockMoveRequestDto.getFromBranchId();
        this.fromBranchName = deliteStockMoveRequestDto.getFromBranchName();
        this.toBranchId = deliteStockMoveRequestDto.getToBranchId();
        this.toBranchName = deliteStockMoveRequestDto.getToBranchName();
        this.fromDeptId = deliteStockMoveRequestDto.getFromDeptId();
        this.fromDeptName = deliteStockMoveRequestDto.getFromDeptName();
        this.toDeptId = deliteStockMoveRequestDto.getToDeptId();
        this.toDeptName = deliteStockMoveRequestDto.getToDeptName();
        this.destinationBinNo = deliteStockMoveRequestDto.getDestinationBinNo();
        this.requestDate = DateUtil.stringToLocalDateTime(deliteStockMoveRequestDto.getRequestDate());
        this.updatedBy = deliteStockMoveRequestDto.getUpdatedBy();
        this.completedDate = DateUtil.stringToLocalDateTime(deliteStockMoveRequestDto.getCompletedDate());
        this.toGid = deliteStockMoveRequestDto.getToGid();
        this.kalasalFromDate = DateUtil.stringToLocalDate(deliteStockMoveRequestDto.getKalasalFromDate());
        this.kalasalToDate = DateUtil.stringToLocalDate(deliteStockMoveRequestDto.getKalasalToDate());
        this.activatedBy = deliteStockMoveRequestDto.getActivatedBy();
        this.activationStatus = deliteStockMoveRequestDto.getActivationStatus();
        this.activatedOn = DateUtil.stringToLocalDate(deliteStockMoveRequestDto.getActivatedOn());
        this.materialRequestId = deliteStockMoveRequestDto.getMaterialRequestId();
        this.firmId = deliteStockMoveRequestDto.getFirmId();
    }
}