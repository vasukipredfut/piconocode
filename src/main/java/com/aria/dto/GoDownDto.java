package com.aria.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.aria.model.GoDown;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoDownDto {

    private Long id;

    private String name;

    private String address;

    private String licenseNo;

    private Long cluster;

    private Long branch;

    private String branchName;

    private Long plant;

    private Long ebService;

    private String ebServiceName;

    private String genSet;

    private Long genSetId;

    private String genSetName;

    private String finale;

    private Date updatedon;

    private String external;

    private Long ledgerId;

    private Long HOSupplierId;

    private Long printOrder;

    private Long firmId;

    private String firmName;

    private String godowntype;

    private String godownCapacity;

   /* private String godownLength;

    private String godownBredth;

    private String godownArea;*/

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private String contractTimePeroid;

    private String createdDate;
    private String fromDate;

    private String toDate;

    private String documentPath;

    public GoDownDto(GoDown goDown) {
        DateUtil dateUtil = new DateUtil();
        this.id = goDown.getId();
        this.name = goDown.getName();
        this.address = goDown.getAddress();
        this.licenseNo = goDown.getLicenseNo();
        this.cluster = goDown.getCluster();
        this.branch = goDown.getBranch();
        this.plant = goDown.getPlant();
        this.ebService = goDown.getEbService();
        this.genSet = goDown.getGenSet();
        this.genSetId = goDown.getGenSetId();
        this.finale = goDown.getFinale();
        this.updatedon = goDown.getUpdatedon();
        this.updatedBy = goDown.getUpdatedBy();
        this.godowntype = goDown.getGodowntype();
        this.ledgerId = goDown.getLedgerId();
        this.HOSupplierId = goDown.getHOSupplierId();
        this.printOrder = goDown.getPrintOrder();
        if (goDown.getOrganisation() != null) {
            this.firmId = goDown.getOrganisation().getId();
            this.firmName = goDown.getOrganisation().getName();
        }
        this.updated_date = goDown.getUpdated_date();
        this.updatedBy = goDown.getUpdatedBy();
        this.created_date = goDown.getCreated_date();
        this.createdBy = goDown.getCreatedBy();
        this.godowntype = goDown.getGodowntype();

        this.contractTimePeroid = goDown.getContractTimePeroid();
        if (goDown.getFromDate() != null) {
            this.fromDate = dateUtil.getDateTOStringWk(goDown.getFromDate());
        }
        if (goDown.getToDate() != null) {
            this.toDate = dateUtil.getDateTOStringWk(goDown.getToDate());
        }
        this.documentPath = goDown.getDocumentPath();
        this.godownCapacity = goDown.getGodownCapacity();
        this.createdDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(goDown.getCreated_date());
    }
}
