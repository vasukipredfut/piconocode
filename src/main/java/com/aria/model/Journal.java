
package com.aria.model;

import com.aria.dto.JournalDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "journal")
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String journalName;

    @Column(name = "proid")
    private Long primaryProductId;

    @Column(name = "cate")
    private String productionCategory;

    @Column(name = "type")
    private String processType;

    /* @Column(name = "ba")
     private String excuteInBatch;
*/
    @Column(name = "batchtype")
    private String batchNumber;

    @Column(name = "dept")
    private Long deptId;

    @Column(name = "pvitytype")
    private String productvityType;

    @Column(name = "pridunit")
    private String displayUnit;

    @Column(name = "activity")
    private String activity;

    @Column(name = "dctype")
    private String dataCollectionType;

    @Column(name = "company_Id")
    private Long companyId;

    @Column(name = "active_Status")
    private String activeStatus;


    @Column(name = "batchproxid")
    private Long batchProxId;


    /*@Column(name = "costGroupId")
    private Long costGroupId;
*/

   /*

    @Column(name = "pvityproid")
    private Long pvityProId;

    @Column(name = "batchproid")
    private Long batchProId;

    @Column(name = "no1seedid")
    private Long no1SeedId;

    @Column(name = "no2seedid")
    private Long no2SeedId;

    @Column(name = "batchproxid")
    private Long batchProxId;

    @Column(name = "exportOrder")
    private String exportOrder;

    @Column(name = "filmPrinting")
    private Long filmPrinting;

    @Column(name = "category")
    private String category;

    @Column(name = "input_Output_unTally")
    private int inputOutputUnTally;*/

    /*@Column(name = "P1")
    private int p1;

    @Column(name = "P2")
    private int p2;

    @Column(name = "P3")
    private int p3;*/

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    /*@Column
    private Boolean batchrequired;*/

    @Column
    private Boolean blending;

    @OneToMany(mappedBy = "journalId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Where(clause = "status = true")
    List<JournalProductMapping> productDetails;

    @OneToMany(mappedBy = "journalId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Where(clause = "connect_table = 'machine' AND primary_work_center = 'YES' AND status = true")
    List<JournalMachineMapping> machineDetails;

    @OneToMany(mappedBy = "journalId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Where(clause = "connect_table = 'dwcluster' AND status = true ")
    List<JournalMachineMapping> manWorkDetails;


    @Column(name = "calculationtype")
    private String calculationType;

    @Column(name = "stock_type")
    private String stockType;

    @Column(name = "asrs")
    private Boolean asrs;
    
   

    public Journal(JournalDto journal) {
        this.id = journal.getId();
        this.journalName = journal.getJournalName();
        this.primaryProductId = journal.getPrimaryProductId();
        this.batchProxId = journal.getBatchProxId();
        this.dataCollectionType = journal.getDataCollectionType();
        this.productionCategory = journal.getProductionCategory();
        this.processType = journal.getProcessType();
        //this.excuteInBatch = journal.getExcuteInBatch();
        //this.batchNumber = journal.getBatchNumber();
        this.deptId = journal.getDeptId();
        this.productvityType = journal.getProductvityType();
        this.displayUnit = journal.getDisplayUnit();
        this.activity = journal.getActivity();
        this.companyId = journal.getCompanyId();
        //this.p1 = journal.getP1();
        //this.p2 = journal.getP2();
        //this.p3 = journal.getP3();
        this.activeStatus = journal.getActiveStatus();
        //this.costGroupId = journal.getCostGroupId();
        //this.batchrequired=journal.getBatchrequired();
        //this.productvityProId = journal.getPvityProId();
        //this.batchProId = journal.getBatchProId();
        //this.no1SeedId = journal.getNo1SeedId();
        //this.no2SeedId = journal.getNo2SeedId();
        //this.batchProxId = journal.getBatchProxId();
        //this.exportOrder = journal.getExportOrder();
        //this.filmPrinting = journal.getFilmPrinting();
        //this.category = journal.getCategory();
        //this.inputOutputUnTally = journal.getInputOutputUnTally();

        this.createdBy = journal.getCreatedBy();
        this.updatedBy = journal.getUpdatedBy();
        this.created_date = journal.getCreated_date();
        this.updated_date = journal.getUpdated_date();
        this.blending = journal.getBlending();
        this.batchProxId = journal.getBatchProxId();

        this.calculationType = journal.getCalculationType();
        this.stockType = journal.getStockType();
        this.asrs = journal.getAsrs();
       
    }


}
