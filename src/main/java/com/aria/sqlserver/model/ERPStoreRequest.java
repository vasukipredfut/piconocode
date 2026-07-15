package com.aria.sqlserver.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "erp_store_request")
public class ERPStoreRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    @Column
    private String PalletID;

    @Column
    private String PalletType;

    @Column
    private String ProductName;

    @Column
    private String ProductId;

    @Column
    private String BatchNo;

    @Column
    private String ExportOrderNo;

    @Column
    private String UOM;

    @Column
    private double MRP;

    @Column
    private Long JournalID;

    @Column
    private Date ShiftDate;

    @Column
    private String ShiftNo;

    @Column
    private String Bestbefore;

    @Column
    private double Quantity;

    @Column
    private double ProductWeight;

    @Column
    private double GrossWeight;

    @Column
    private String PartCategory;

    @Column
    private double NoOfEmptyBottlesPerCottonBox;

    @Column
    private String Reason;

    @Column
    private String RequestStatus;

    @Column
    private Date RequestDateTime;

    @Column
    private Date CompletedDateTime;

    @Column
    private int IsScanned;

    @Column
    private Date ScannedDateTime;

    /*@Column
    private String trnno ;*/
}
