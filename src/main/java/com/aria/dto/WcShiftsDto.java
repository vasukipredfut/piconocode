package com.aria.dto;

import com.aria.model.Wcshift;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class WcShiftsDto {

    private Long id;

    private Date shiftDate;

    private String pShift;

    private Long pShiftId;

    private Long wcId;

    private String wcTable;

    private String shrs;

    private String begin;

    private String end;

    private String strbegin;

    private Long journalId;

    private Long proId;

    private String batchNo;

    private Long batchId;

    private String bStatus;

    private String eStatus;

    private String trnNo;

    private Long dept;

    private Long branch;

    private Long bby;

    private String bon;

    private Long eby;

    private String eon;

    private Long jEntryBy;

    private String jEntryOn;

    private String jEntry;

    private String aStatus;

    private Long acBy;

    private String acon;

    private String reversedBy;

    private Date reversedOn;

    private String imageLocation;

    private Long salesOrderId;

    private Double MRP;

    public WcShiftsDto(Wcshift wcShiftsDto) {
        this.id = wcShiftsDto.getId();
        this.shiftDate = wcShiftsDto.getShiftDate();
        this.pShift = wcShiftsDto.getPShift();
        this.pShiftId = wcShiftsDto.getPShiftId();
        this.wcId = wcShiftsDto.getWcId();
        this.wcTable = wcShiftsDto.getWcTable();
        this.shrs = wcShiftsDto.getShrs();
        this.begin = wcShiftsDto.getBegin();
        this.strbegin = wcShiftsDto.getStrbegin();
        this.journalId = wcShiftsDto.getJournalId();
        this.proId = wcShiftsDto.getProId();
        this.batchNo = wcShiftsDto.getBatchNo();
        this.batchId = wcShiftsDto.getBatchId();
        this.bStatus = wcShiftsDto.getBStatus();
        this.eStatus = wcShiftsDto.getEStatus();
        this.trnNo = wcShiftsDto.getTrnNo();
        this.dept = wcShiftsDto.getDept();
        this.branch = wcShiftsDto.getBranch();
        this.bby = wcShiftsDto.getBby();
        this.bon = wcShiftsDto.getBon();
        this.eby = wcShiftsDto.getEby();
        this.eon = wcShiftsDto.getEon();
        this.jEntryBy = wcShiftsDto.getJEntryBy();
        this.jEntryOn = wcShiftsDto.getJEntryOn();
        this.jEntry = wcShiftsDto.getJEntry();
        this.aStatus = wcShiftsDto.getAStatus();
        this.acBy = wcShiftsDto.getAcBy();
        this.acon = wcShiftsDto.getAcon();
        this.reversedBy = wcShiftsDto.getReversedBy();
        this.reversedOn = wcShiftsDto.getReversedOn();
        this.imageLocation = wcShiftsDto.getImageLocation();
        this.salesOrderId = wcShiftsDto.getSalesOrderId();
        this.MRP = wcShiftsDto.getMRP();
    }
}
