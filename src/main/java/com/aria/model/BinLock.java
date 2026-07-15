package com.aria.model;


import com.aria.dto.BinLockRequestDto;
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
@Table(name = "binlock")
public class BinLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bin_no")
    private String binNo;

    @Column(name = "lock_type")
    private String lockType;

    @Column(name = "lock_status")
    private String lockStatus;

    @Column(name = "lock_date")
    private Date lockDate;

    @OneToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private User lockedEmployee;

    @OneToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductMaster product;

    @Column(name = "batchno")
    private String batchNo;

    @Column(name = "batchid")
    private Long batchId;

    @Column(name = "lorry_serial_no")
    private String lorrySerialNo;

    @Column(name = "inward_date")
    private Date inwardDate;

    @Column
    private String state;

    @Column
    private Double quantity;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    public BinLock(BinLockRequestDto binLock) {
        this.id = binLock.getId();
        this.binNo = binLock.getBinNo();
        this.lockType = binLock.getLockType();
        this.lockStatus = binLock.getLockStatus();
        if (binLock.getLockedEmployeeId() != null) {
            User user = new User();
            user.setId(binLock.getLockedEmployeeId());
            this.lockedEmployee = user;
        }
        this.created_date = binLock.getCreated_date();
        this.updated_date = binLock.getUpdated_date();
    }
}
