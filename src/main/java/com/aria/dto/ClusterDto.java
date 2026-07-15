package com.aria.dto;


import com.aria.model.Cluster;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
public class ClusterDto {

    private Long id;

    private String name;

    private Long departmentId;

    private String departmentName;

    private Long orgId;

    private String orgName;

    private Long branchId;

    private String branchName;

    private String purpose;

    private String workinst;

    private String production;

    private String utilisation;
   
    private String responsibility;

    private String finals;

    private String oldname;

    private String trnprefix;
   
    private String cprefix;

    private String pv;

    public ClusterDto(Cluster cluster) {
        this.id = cluster.getId();
        this.name = cluster.getName();
        this.departmentId = cluster.getDepartment().getId();
        this.departmentName = cluster.getDepartment().getName();
        this.orgId = cluster.getOrganisation().getId();
        this.orgName = cluster.getOrganisation().getName();
        this.branchId = cluster.getBranch().getId();
        this.branchName = cluster.getBranch().getBranchName();
        this.purpose = cluster.getPurpose();
        this.workinst = cluster.getWorkinst();
        this.production = cluster.getProduction();
        this.utilisation = cluster.getUtilisation();
        this.responsibility = cluster.getResponsibility();
        this.finals = cluster.getFinals();
        this.oldname = cluster.getOldname();
        this.trnprefix = cluster.getTrnprefix();
        this.cprefix = cluster.getCprefix();
        this.pv = cluster.getPv();
    }
}
