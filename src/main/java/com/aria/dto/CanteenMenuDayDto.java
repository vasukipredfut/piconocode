package com.aria.dto;

import com.aria.model.CanteenMenuDayMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class CanteenMenuDayDto {


    private Long id;

    private Long foodId;

    public Long dayId;

    public String dayName;

    public CanteenMenuDayDto(CanteenMenuDayMapping dayDto) {
        this.id = dayDto.getId();
        this.foodId = dayDto.getFoodId();
        if (dayDto.getDay() != null) {
            this.dayId = dayDto.getDay().getId();
            this.dayName = dayDto.getDay().getDayName();
        }
    }
}
