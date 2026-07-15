package com.aria.dto;

import com.aria.model.Machine;
import com.aria.model.MachineWorkFlow;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PMAttributesDto {
    private Long id;
    private String value;
}
