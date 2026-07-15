package com.aria.dto;


import com.aria.model.ShiftDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShiftDto {

    private Long id;

    private Long orgId;

    private String shiftName;

    public ShiftDto(ShiftDetails shiftDetails) {
        this.id = shiftDetails.getId();
        if (shiftDetails.getOrganisation() != null) {
            this.orgId = shiftDetails.getOrganisation().getId();
        }
        this.shiftName = shiftDetails.getShiftName();
    }
}
