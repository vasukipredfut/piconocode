package com.aria.dto;

import com.aria.model.HolidayDetails;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class HolidayDetailsDto {

    private Long id;

    private Long holidayId;

    private String date;

    private String day;

    private String holidayName;


    public HolidayDetailsDto(HolidayDetails holidayDetails) {
        DateUtil dateUtil = new DateUtil();
        this.id = holidayDetails.getId();
        this.holidayId = holidayDetails.getHolidayId();
        if (holidayDetails.getDate() != null) {
            this.date = dateUtil.getDateTOStringWk(holidayDetails.getDate());
        }
        this.day = holidayDetails.getDay();
        this.holidayName = holidayDetails.getHolidayName();
    }
}
