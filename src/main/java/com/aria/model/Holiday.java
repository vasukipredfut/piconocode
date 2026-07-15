package com.aria.model;

import com.aria.dto.HolidayDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "holiday")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String year;

    @Column(name = "holiday_type")
    private String holidayType;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_By")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "holidayId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<HolidayDetails> holidayDetails;

    public Holiday(HolidayDto holiday) {
        this.id = holiday.getId();
        this.year = holiday.getYear();
        this.holidayType = holiday.getHolidayType();
        this.createdBy = holiday.getCreatedBy();
        this.updatedBy = holiday.getUpdatedBy();
        this.created_date = holiday.getCreated_date();
        this.updated_date = holiday.getUpdated_date();
    }
}
