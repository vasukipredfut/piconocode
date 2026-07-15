package com.aria.dto;

import com.aria.model.*;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FumigationDto {


    private Long id;
    private String fumigationType;
    private String lastDate;
    private String nextDate;
    private Long godownId;
    private String godownName;
    private Long doneById;
    private String doneByName;
    private Long departmentId;
    private String departmentName;
    private String consumption;
    private String measure;
    private String length;
    private String breadth;
    private String area;
    private Boolean vendorStatus;
    private Long vendorId;
    private String vendorName;
    private String binNo;
    private String bags;
    private String lot;
    private String qty;
    private Long createdBy;
    private LocalDateTime created_date;
    private Long updatedBy;
    private LocalDateTime updated_date;


    public FumigationDto(Fumigation fumigation) {
        DateUtil dateUtil = new DateUtil();
        this.id = fumigation.getId();
        this.fumigationType = fumigation.getFumigationType();
        if (fumigation.getLastDate() != null) {
            this.lastDate = dateUtil.getDateTOStringWk(fumigation.getLastDate());
        }
        if (fumigation.getNextDate() != null) {
            this.nextDate = dateUtil.getDateTOStringWk(fumigation.getNextDate());
        }
        if (fumigation.getGodown() != null) {
            this.godownId = fumigation.getGodown().getId();
            this.godownName = fumigation.getGodown().getName();
        }
        if (fumigation.getDoneBy() != null) {
            this.doneById = fumigation.getDoneBy().getId();
            this.doneByName = fumigation.getDoneBy().getEmployeename();
        }
        if (fumigation.getDepartment() != null) {
            this.departmentId = fumigation.getDepartment().getId();
            this.departmentName = fumigation.getDepartment().getName();
        }
        this.consumption = fumigation.getConsumption();
        this.measure = fumigation.getMeasure();
        this.length = fumigation.getLength();
        this.breadth = fumigation.getBreadth();
        this.area = fumigation.getArea();
        this.vendorStatus = fumigation.getVendorStatus();
        if (fumigation.getVendor() != null) {
            this.vendorId = fumigation.getVendor().getId();
            this.vendorName = fumigation.getVendor().getName();
        }
        this.binNo = fumigation.getBinNo();
        this.bags = fumigation.getBags();
        this.lot = fumigation.getLot();
        this.qty = fumigation.getQty();
        this.updated_date = fumigation.getUpdated_date();
        this.updatedBy = fumigation.getUpdatedBy();
        this.created_date = fumigation.getCreated_date();
        this.createdBy = fumigation.getCreatedBy();
    }
}
