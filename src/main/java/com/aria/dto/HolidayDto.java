package com.aria.dto;


import com.aria.model.Holiday;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class HolidayDto {

    private Long id;

    private String year;

    private String holidayType;

    private int noOfHoliday;

    private Long createdBy;

    private String createdByName;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<HolidayDetailsDto> holidayDetails;

    public HolidayDto(Holiday holiday, Boolean status) {
        this.id = holiday.getId();
        this.year = holiday.getYear();
        this.holidayType = holiday.getHolidayType();
        this.createdBy = holiday.getCreatedBy();
        this.updatedBy = holiday.getUpdatedBy();
        this.created_date = holiday.getCreated_date();
        this.updated_date = holiday.getUpdated_date();
        if (status) {
            this.holidayDetails = holiday.getHolidayDetails() != null
                    ? holiday.getHolidayDetails().stream().map(field -> new HolidayDetailsDto(field)).collect(Collectors.toList()) : null;
        } else {
            this.noOfHoliday = holiday.getHolidayDetails() != null
                    ? holiday.getHolidayDetails().stream().map(field -> new HolidayDetailsDto(field)).collect(Collectors.toList()).size() : 0;
        }
    }
}
