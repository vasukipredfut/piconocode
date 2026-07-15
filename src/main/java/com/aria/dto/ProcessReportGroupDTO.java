package com.aria.dto;

import com.aria.model.ProcessReportGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProcessReportGroupDTO {

    private Long id;

    private String name;

    private Long prid;

    private String total;

    List<ProcessReportProductGroupDTO> productGroupDTOS ;
    // List<ProcessReportMenuDTO> menuDTOList;

    public ProcessReportGroupDTO(ProcessReportGroup processReportGroup) {
        this.id = processReportGroup.getId();
        this.name = processReportGroup.getName();
        this.prid = processReportGroup.getPrid();
        this.total = processReportGroup.getTotal();
    }
}
