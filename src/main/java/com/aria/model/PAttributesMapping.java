package com.aria.model;

import com.aria.dto.PAttributesMappingDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "pattributes_mapping")
public class PAttributesMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="sjid")
    private Long journalId;

    @Column(name = "dept")
    private Long deptId;

    @Column(name = "branch")
    private Long branchId;

    @Column
    private String total;

    @Column
    private String avg;

    @Column
    private String linkwith;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "machine_id")
    private Long machineId;

    @Column(name ="attributes_id")
    private Long attributesId;

    @Column(name ="attributes_name")
    private String attributesName;

    @Column
    private int disporder;

    @Column
    private String datatype;

    @Column
    private String awith;

    @Column
    private Long awithid;

    @Column
    private String awithtable;

    @Column
    private Long dispunit;



    /*public PAttributesMapping(PAttributesMappingDto pAttributesMapping) {
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

    public PAttributesMapping(PAttributesMappingDto pAttributesMapping) {
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
    }
}
