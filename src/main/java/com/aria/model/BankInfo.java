package com.aria.model;

import com.aria.dto.BankInfoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "bankinfo")
public class BankInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private Long vid;

    @Column
    private String vtable;

    @Column
    private String name;

    @Column
    private String accno;

    @Column
    private String ifsc;

    @Column
    private Long bankid;

    @Column
    private String branch;

    @Column
    private String bank;

    @Column
    private String usertime;

    @Column
    private String status;

    @Column
    private LocalDateTime lastmodified;

    public BankInfo(BankInfoDTO bankInfo) {
        this.id = bankInfo.getId();
        this.vid = bankInfo.getVendorId();
        this.vtable = bankInfo.getConnectTable();
        this.name = bankInfo.getVendorName();
        this.accno = bankInfo.getAccountNo();
        this.ifsc = bankInfo.getIfscCode();
        this.bankid = bankInfo.getBankId();
        this.branch = bankInfo.getBranch();
        this.bank = bankInfo.getBank();
        this.usertime = bankInfo.getUsertime();
        this.status = bankInfo.getStatus();

    }
}
