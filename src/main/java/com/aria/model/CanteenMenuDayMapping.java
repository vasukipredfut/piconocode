package com.aria.model;

import com.aria.dto.CanteenMenuDayDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "canteen_menu_day")
public class CanteenMenuDayMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_id")
    private Long foodId;

    @OneToOne
    @JoinColumn(name = "day_id", referencedColumnName = "id")
    public Day day;

    public CanteenMenuDayMapping(CanteenMenuDayDto dayDto) {
        this.id = dayDto.getId();
        this.foodId = dayDto.getId();
        if (dayDto.getId() != null) {
            Day day = new Day();
            day.setId(dayDto.getDayId());
            this.day = day;
        }
    }
}
