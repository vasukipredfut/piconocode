package com.aria.dto;

import com.aria.model.ProcessReportTitle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProcessReportMenuDTO {

    public Long id;
    public String title;
    public String dispName;
    public String trnType;
    List<ProcessReportProductDTO> productStock ;
    public ProcessReportMenuDTO(ProcessReportTitle processReportTitle) {
        this.id = processReportTitle.getId();
        this.title = processReportTitle.getPrgrptitle();
        this.dispName = processReportTitle.getDisp_name();
        this.trnType = processReportTitle.getTrntype();
    }
}
