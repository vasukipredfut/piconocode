package com.aria.dto;

import com.aria.model.FumigationScheduler;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FumigationSchedulerDto {


    private Long id;
    private String fumigationType;
    private String lastDate;
    private String nextDate;
    private Long godownId;
    private String godownName;

    public FumigationSchedulerDto(FumigationScheduler fumigation) {
        DateUtil dateUtil = new DateUtil();
        this.id = fumigation.getId();
        this.fumigationType = fumigation.getFumigationType();
        if (fumigation.getLastDate() != null) {
            this.lastDate = dateUtil.getDateTOStringWk(fumigation.getLastDate());
        }
        if (fumigation.getNextDate() != null) {
            this.nextDate = dateUtil.getDateTOStringWk(fumigation.getNextDate());
        }
        if (fumigation.getGodown() != null) {
            this.godownId = fumigation.getGodown().getId();
            this.godownName = fumigation.getGodown().getName();
        }
    }
}
