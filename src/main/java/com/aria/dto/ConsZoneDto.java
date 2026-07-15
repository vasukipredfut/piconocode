package com.aria.dto;

import com.aria.model.ConsZone;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;




@NoArgsConstructor
@Getter
@Setter
public class ConsZoneDto {


    private Long id;

   
    private Long mcid;

   
    private String czone;

   
    private String contype;

   
    private String concate;

   
    private Long dept;

   
    private Long branch;


    public ConsZoneDto(ConsZone consZone) {
        this.id = consZone.getId();
        this.mcid = consZone.getId();
        this.czone = consZone.getCzone();
        this.contype = consZone.getContype();
        this.concate = consZone.getConcate();
        this.dept = consZone.getDept();
        this.branch = consZone.getBranch();
    }
}
