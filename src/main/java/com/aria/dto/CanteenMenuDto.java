package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.CanteenMenu;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CanteenMenuDto {

    private Long id;

    private Long dayId;

    private String dayName;

    private String fromDate;

    private String toDate;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<MenuItemsDto> dinnerItems;
    private List<MenuItemsDto> breakfastItems;
    private List<MenuItemsDto> lunchItems;
    private List<MenuItemsDto> beverageItems;
    private List<MenuItemsDto> snacksItems;
    private List<CanteenMenuDayDto> days;
    private List<Long> dayIds;

    public CanteenMenuDto(CanteenMenu canteenMenu, Boolean status) {
        this.id = canteenMenu.getId();
        this.createdBy = canteenMenu.getCreatedBy();
        this.updatedBy = canteenMenu.getUpdatedBy();
        this.created_date = canteenMenu.getCreated_date();
        if (canteenMenu.getUpdated_date() != null) {
            this.updated_date = canteenMenu.getUpdated_date();
        } else {
            this.updated_date = canteenMenu.getCreated_date();
        }
        if (status) {
            this.dinnerItems = canteenMenu.getDinnerItems() != null ? canteenMenu.getDinnerItems().stream()
                    .map(field -> new MenuItemsDto(field)).collect(Collectors.toList()) : null;
            this.breakfastItems = canteenMenu.getBreakfastItems() != null ? canteenMenu.getBreakfastItems().stream()
                    .map(field -> new MenuItemsDto(field)).collect(Collectors.toList()) : null;
            this.lunchItems = canteenMenu.getLunchItems() != null ? canteenMenu.getLunchItems().stream()
                    .map(field -> new MenuItemsDto(field)).collect(Collectors.toList()) : null;
            this.beverageItems = canteenMenu.getBeverageItems() != null ? canteenMenu.getBeverageItems().stream()
                    .map(field -> new MenuItemsDto(field)).collect(Collectors.toList()) : null;
            this.snacksItems = canteenMenu.getSnacksItems() != null ? canteenMenu.getSnacksItems().stream()
                    .map(field -> new MenuItemsDto(field)).collect(Collectors.toList()) : null;
            this.days = canteenMenu.getDays() != null ? canteenMenu.getDays().stream()
                    .map(field -> new CanteenMenuDayDto(field)).collect(Collectors.toList()) : null;
        }
    }

}
