package com.aria.dto;

import com.aria.model.PAttributesMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class PAttributesMappingDto {


    /*private Long id;

    private Long sjid;

    private Long dept;

    private Long branch;

    private Long colid;

    private String coltable;

    private String coltype;

    private String datatype;

    private String total;

    private String avg;

    private String awith;

    private Long awithid;

    private String awithtable;

    private String gavg;

    private Long wcid;

    private String wctable;

    private String linkwith;

    private String dispunit;

    private String preset;

    private Long dentryid;

    private String dentrytable;

    private int disporder;

    private String ewhen;*/

    private Long id;

    private Long journalId;

    private Long deptId;

    private Long branchId;

    private String total;

    private String avg;

    private String linkwith;

    private Long productId;

    private Long machineId;

    private Long attributesId;

    private String attributesName;

    private int disporder;

    private String datatype;

    private String awith;

    private Long awithid;

    private String awithtable;

    List<DataSetDto> dataSetList;

    /*public PAttributesMappingDto(PAttributesMapping pAttributesMapping) {
        this.id = pAttributesMapping.getId();
        this.sjid = pAttributesMapping.getSjid();
        this.dept = pAttributesMapping.getDept();
        this.branch = pAttributesMapping.getBranch();
        this.colid = pAttributesMapping.getColid();
        this.coltable = pAttributesMapping.getColtable();
        this.coltype = pAttributesMapping.getColtype();
        this.datatype = pAttributesMapping.getDatatype();
        this.total = pAttributesMapping.getTotal();
        this.avg = pAttributesMapping.getAvg();
        this.awith = pAttributesMapping.getAwith();
        this.awithid = pAttributesMapping.getAwithid();
        this.awithtable = pAttributesMapping.getAwithtable();
        this.gavg = pAttributesMapping.getGavg();
        this.wcid = pAttributesMapping.getWcid();
        this.wctable = pAttributesMapping.getWctable();
        this.linkwith = pAttributesMapping.getLinkwith();
        this.dispunit = pAttributesMapping.getDispunit();
        this.preset = pAttributesMapping.getPreset();
        this.dentryid = pAttributesMapping.getDentryid();
        this.dentrytable = pAttributesMapping.getDentrytable();
        this.disporder = pAttributesMapping.getDisporder();
        this.ewhen = pAttributesMapping.getEwhen();
    }*/

    public PAttributesMappingDto(PAttributesMapping pAttributesMapping) {
        this.id = pAttributesMapping.getId();
        this.journalId = pAttributesMapping.getJournalId();
        this.deptId = pAttributesMapping.getDeptId();
        this.branchId = pAttributesMapping.getBranchId();
        this.total = pAttributesMapping.getTotal();
        this.avg = pAttributesMapping.getAvg();
        this.linkwith = pAttributesMapping.getLinkwith();
        this.productId = pAttributesMapping.getProductId();
        this.machineId = pAttributesMapping.getMachineId();
        this.attributesId = pAttributesMapping.getAttributesId();
        this.attributesName = pAttributesMapping.getAttributesName();
        this.disporder = pAttributesMapping.getDisporder();
        this.awith = pAttributesMapping.getAwith();
        this.awithid = pAttributesMapping.getAwithid();
        this.awithtable = pAttributesMapping.getAwithtable();
    }
}
