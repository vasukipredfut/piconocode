package com.aria.model;

import com.aria.dto.KpinameDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "kpiname")
public class Kpiname {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(name = "ffieldid")
    private Long fieldId;

    @Column(name = "formno")
    private String formNo;

    @Column(name = "type")
    private String type;

    @OneToOne
	@JoinColumn(name = "proid", referencedColumnName = "id")
	private ProductMaster product;

    @Column(name = "attype")
    private String atType;

    @Column(name = "pendingreportflag")
    private int pendingReportFlag;

    @Column(name = "process_type")
    private String processType;



    public Kpiname(KpinameDto kpiname) {
        this.id = kpiname.getId();
        this.name = kpiname.getName();
        this.fieldId = kpiname.getFieldId();
        this.formNo = kpiname.getFormNo();
        this.type = kpiname.getType();
        //this.proId = kpiname.getProId();
        if(kpiname.getProId() != null){
			ProductMaster tn = new ProductMaster();
            tn.setId(kpiname.getProId());
            this.product = tn;
        }
        this.atType = kpiname.getAtType();
        this.pendingReportFlag = kpiname.getPendingReportFlag();
        this.processType = kpiname.getProcessType();
    }
}
