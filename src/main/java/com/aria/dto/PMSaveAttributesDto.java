package com.aria.dto;

import com.aria.model.Machine;
import com.aria.model.MachineWorkFlow;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PMSaveAttributesDto {
    private Long machineId;
    private Long detailedReasonId;
    private Long taskCategory;
    private Long taskGroup;
    private Long taskId;
    private String timeSlab;
    private String productionShift;
    private List<PMAttributesDto> attributes;

    public PMSaveAttributesDto(Machine field, DateUtil dateUtil) {
    }
}
