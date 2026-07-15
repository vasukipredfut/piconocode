package com.aria.model;

import com.aria.dto.FinishedGooseBinsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "fgbins")
public class FinishedGooseBins {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long refid;

    @Column
    private Long binname;

    @Column
    private Long bid;

    @Column
    private String type;

    @Column
    private String  name;


    public FinishedGooseBins(FinishedGooseBinsDto finishedGooseBins) {
        this.id = finishedGooseBins.getId();
        this.refid = finishedGooseBins.getRefid();
        this.binname = finishedGooseBins.getBinname();
        this.bid = finishedGooseBins.getBid();
        this.type = finishedGooseBins.getType();
        this.name = finishedGooseBins.getName();
    }

}
