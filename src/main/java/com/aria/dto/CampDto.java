package com.aria.dto;

import com.aria.model.Camp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@NoArgsConstructor
@Getter
@Setter
public class CampDto {

    private Long id;

    private String camp;

    private String place;

    private String state;

    private Long stateId;

    private String updatedOn;

    public CampDto(Camp campmodel) {
        this.id = campmodel.getId();
        this.camp = campmodel.getCamp();
        this.place = campmodel.getPlace();
        this.state = campmodel.getState();
        this.stateId = campmodel.getState_id();
        this.updatedOn = campmodel.getUpdatedOn();
    }
}
