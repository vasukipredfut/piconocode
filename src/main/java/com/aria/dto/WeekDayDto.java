package com.aria.dto;

import com.aria.model.WeekDay;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class WeekDayDto {

    private Long id;

    private String weekday;

  

    public WeekDayDto(WeekDay weekDay) {
        this.id = weekDay.getId();
        this.weekday = weekDay.getWeekday();
     
    }
}
