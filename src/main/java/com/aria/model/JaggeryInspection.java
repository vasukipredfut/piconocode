package com.aria.model;

import com.aria.dto.JaggeryInspectionDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "jaggeryinsp")
public class JaggeryInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Temporal(TemporalType.DATE)
    private Date jdate;

    @Column
    private Long inwardat;

    @Column
    private String billtrnno;

    @Column
    private String billno;

    @Column
    @Temporal(TemporalType.DATE)
    private Date billdt;

    @Column
    private Long party;

    @Column
    private String place;

    @Column
    private Double qty;

    @Column
    private Double urate;

    @Column
    private Long branch;

    @Column
    private Long dept;

    @Column
    private Long vby;

    @Column
    private LocalDateTime vdate;

    @Column
    private String verify;

    @Column
    private Long aby;

    @Column
    private LocalDateTime adate;

    @Column
    private String approve;

    @Column
    private Long auby;

    @Column
    private LocalDateTime audate;

    @Column
    private String authenty;

    @Column
    private String uniqno;

    @Column
    private String uniqueses;
    
    @Column
	private String status;
    
    @Column(name = "rejected_By")
   	private Long rejectedBy;

    @Column
    @Temporal(TemporalType.DATE)
    private Date rejectdate;

    @Column(name = "rejected_Description")
   	private String rejectedDescription;

    @Column(name = "updated_By")
   	private Long updatedBy;
    
    @Column
	private LocalDateTime updated_date;


    @Column
    private Long prepareby;

    @Column
    private LocalDateTime preparedate;





    public JaggeryInspection(JaggeryInspectionDto jaggeryInspection) {
        DateUtil dateUtil = new DateUtil();
        this.id = jaggeryInspection.getId();
        this.jdate = dateUtil.getStringTODateWk(jaggeryInspection.getJagDate());
        this.inwardat = jaggeryInspection.getInwardat();
        this.billtrnno = jaggeryInspection.getBilltrnno();
        this.billno = jaggeryInspection.getBillNo();
        this.billdt =dateUtil.getStringTODateWk(jaggeryInspection.getBillDate());
        this.party = jaggeryInspection.getVendorId();
        this.place = jaggeryInspection.getPlace();
        this.qty = jaggeryInspection.getQty();
        this.urate = jaggeryInspection.getUnitRate();
        this.branch = jaggeryInspection.getBranch();
        this.dept = jaggeryInspection.getDept();
        this.status = jaggeryInspection.getStatus();
        this.uniqno = jaggeryInspection.getUniqno();
        this.uniqueses = jaggeryInspection.getUniqueses();
    }

}
