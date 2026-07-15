package com.aria.model;

import com.aria.dto.CwagesReqDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.csv.CSVRecord;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cwages")
public class Cwages {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String codeno;

    @Column
    private String sno;

    @Column
    private Long salstruid;

    @Column
    private String designation;

    @Column
    private Long trnyear;

    @Column
    private String name;

    @Column
    private Double amount;

    @Column
    private String journal;

    @Column
    private String mvmt;

    @Column
    private String tport;

    @Column
    private String usecid;

    @Column
    private Long type;

    @Column
    private Long slab;

    @Column
    private String activity;

    public Cwages(CwagesReqDto cwages) {
        this.id = cwages.getId();
        this.codeno = cwages.getCodeno();
        this.sno = cwages.getSno();
        this.salstruid = cwages.getSalstruid();
        this.trnyear = cwages.getTrnyear();
        this.name = cwages.getName();
        this.amount = cwages.getAmount();
        this.journal = cwages.getJournal();
        this.mvmt = cwages.getMvmt();
        this.tport = cwages.getTport();
        this.usecid = cwages.getUsecid();
        this.type = cwages.getType();
        this.slab = cwages.getSlab();
        this.activity = cwages.getActivity();
    }


    public Cwages(CSVRecord csvRecord) {

        if (csvRecord.get("ID") != null && !csvRecord.get("ID").trim().isEmpty()) {
            this.id = Long.valueOf(csvRecord.get("ID"));
        }
        if (csvRecord.get("TRN_YEAR") != null && !csvRecord.get("TRN_YEAR").trim().isEmpty()) {
            this.trnyear = Long.valueOf(csvRecord.get("TRN_YEAR"));
        }
        if (csvRecord.get("DESIGNATION") != null && !csvRecord.get("DESIGNATION").trim().isEmpty()) {
            this.designation = csvRecord.get("DESIGNATION");
        }
        if (csvRecord.get("SALSTRUCTUREID") != null && !csvRecord.get("SALSTRUCTUREID").trim().isEmpty()) {
            this.salstruid = Long.valueOf(csvRecord.get("SALSTRUCTUREID"));
        }
        if (csvRecord.get("NAME") != null && !csvRecord.get("NAME").trim().isEmpty()) {
            this.name = csvRecord.get("NAME");
        }
        if (csvRecord.get("AMOUNT") != null && !csvRecord.get("AMOUNT").trim().isEmpty()) {
            this.amount = Double.valueOf(csvRecord.get("AMOUNT"));
        }
        if (csvRecord.get("JOURNAL") != null && !csvRecord.get("JOURNAL").trim().isEmpty()) {
            this.journal = csvRecord.get("JOURNAL");
        }
        if (csvRecord.get("MATERIALMOVEMENT") != null && !csvRecord.get("MATERIALMOVEMENT").trim().isEmpty()) {
            this.mvmt = csvRecord.get("MATERIALMOVEMENT");
        }
        if (csvRecord.get("TRANSPORT") != null && !csvRecord.get("TRANSPORT").trim().isEmpty()) {
            this.tport = csvRecord.get("TRANSPORT");
        }
        if (csvRecord.get("TYPE") != null && !csvRecord.get("TYPE").trim().isEmpty()) {
            this.type = Long.valueOf(csvRecord.get("TYPE"));
        }
        if (csvRecord.get("SLAB") != null && !csvRecord.get("SLAB").trim().isEmpty()) {
            this.slab = Long.valueOf(csvRecord.get("SLAB"));
        }
        if (csvRecord.get("ACTIVITY") != null && !csvRecord.get("ACTIVITY").trim().isEmpty()) {
            this.activity = csvRecord.get("ACTIVITY");
        }else {
            this.activity = "U";
        }
    }
}
