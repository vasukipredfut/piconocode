package com.aria.model;

import com.aria.dto.HolidayDetailsDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "holiday_details")
public class HolidayDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "holiday_id")
    private Long holidayId;

    @Column
    @Temporal(TemporalType.DATE)
    private Date date;

    @Column
    private String day;

    @Column(name = "holiday_name")
    private String holidayName;


    public HolidayDetails(HolidayDetailsDto holidayDetails) {
        DateUtil dateUtil = new DateUtil();
        this.id = holidayDetails.getId();
        this.holidayId = holidayDetails.getHolidayId();
        if (holidayDetails.getDate() != null) {
            this.date = dateUtil.getStringTODateWk(holidayDetails.getDate());
        }
        this.day = holidayDetails.getDay();
        this.holidayName = holidayDetails.getHolidayName();
    }
}
