package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import com.aria.dto.KpiDto;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "kpi")
public class Kpi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priid;

    @Column
    private Long id;

    @Column
    private Long refid;

    @Column
    private Long tgid;

    @Column
    private Long dispord;

    @Column
    private String skip;

    @Column
    private String active;

    
    public Kpi(KpiDto model){
    	 this.id = model.getId();
         this.priid = model.getPriid();
         this.refid = model.getRefid();
         this.tgid = model.getTgid();
         this.dispord = model.getDispord();
         this.skip = model.getSkip();
         if(model.getActive() == null) {
             this.active = "Y";
         }
    }


}
