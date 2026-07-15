package com.aria.model;

import com.aria.dto.ConsZoneDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Table(name = "conszone")
@Getter
@Setter
public class ConsZone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long mcid;

    @Column
    private String czone;

    @Column
    private String contype;

    @Column
    private String concate;

    @Column
    private Long dept;

    @Column
    private Long branch;

    public ConsZone(ConsZoneDto consZone) {
        this.id = consZone.getId();
        this.mcid = consZone.getId();
        this.czone = consZone.getCzone();
        this.contype = consZone.getContype();
        this.concate = consZone.getConcate();
        this.dept = consZone.getDept();
        this.branch = consZone.getBranch();
    }
}
