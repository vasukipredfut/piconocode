package com.aria.dto;

import com.aria.model.ProcessReportProductGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
public class ProcessReportProductGroupDTO {

    private Long id;


    private String name;

    List<ProcessReportProductDTO> productGroupDTOS ;
    List<ProcessReportMenuDTO> menuDTOList;

    public ProcessReportProductGroupDTO(ProcessReportProductGroup processReportGroup) {
        this.id = processReportGroup.getId();
        this.name = processReportGroup.getName();
    }
}
