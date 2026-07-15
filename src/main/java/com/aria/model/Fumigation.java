package com.aria.model;

import com.aria.dto.FumigationDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "fumigation")
public class Fumigation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fumigation_type")
    private String fumigationType;

    @Column(name = "last_Date")
    @Temporal(TemporalType.DATE)
    private Date lastDate;

    @Column(name = "next_Date")
    @Temporal(TemporalType.DATE)
    private Date nextDate;

    @OneToOne
    @JoinColumn(name = "godown_id", referencedColumnName = "id")
    private GoDown godown;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User doneBy;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails department;

    @Column
    private String consumption;

    @Column
    private String measure;

    @Column
    private String length;

    @Column
    private String breadth;

    @Column
    private String area;

    @Column(name = "vendor_status")
    private Boolean vendorStatus;

    @OneToOne
    @JoinColumn(name = "vendor_id", referencedColumnName = "id")
    private Vendor vendor;

    @Column(name = "bin_no")
    private String binNo;

    @Column
    private String bags;

    @Column
    private String lot;

    @Column
    private String qty;


    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column(name = "updated_by", updatable = false)
    private Long updatedBy;

    @Column
    private LocalDateTime updated_date;

    public Fumigation(FumigationDto fumigation) {
        DateUtil dateUtil = new DateUtil();
        this.id = fumigation.getId();
        this.fumigationType = fumigation.getFumigationType();
        if (fumigation.getLastDate() != null) {
            this.lastDate = dateUtil.getStringTODateWk(fumigation.getLastDate());
        }
        if (fumigation.getNextDate() != null) {
            this.nextDate = dateUtil.getStringTODateWk(fumigation.getNextDate());
        }
        if (fumigation.getGodownId() != null) {
            GoDown goDown = new GoDown();
            godown.setId(fumigation.getGodownId());
            this.godown = goDown;
        }
        if (fumigation.getDoneById() != null) {
            User user = new User();
            user.setId(fumigation.getDoneById());
            this.doneBy = user;
        }
        if (fumigation.getDepartmentId() != null) {
            DepartmentDetails department = new DepartmentDetails();
            department.setId(fumigation.getDoneById());
            this.department = department;
        }
        this.consumption = fumigation.getConsumption();
        this.measure = fumigation.getMeasure();
        this.length = fumigation.getLength();
        this.breadth = fumigation.getBreadth();
        this.area = fumigation.getArea();
        this.vendorStatus = fumigation.getVendorStatus();
        if (fumigation.getVendorId() != null) {
            Vendor vendor = new Vendor();
            vendor.setId(fumigation.getVendorId());
            this.vendor = vendor;
        }
        this.binNo = fumigation.getBinNo();
        this.bags = fumigation.getBags();
        this.lot = fumigation.getLot();
        this.qty = fumigation.getQty();
    }
}
