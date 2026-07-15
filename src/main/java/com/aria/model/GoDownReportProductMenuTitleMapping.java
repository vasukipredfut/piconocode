package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "go_down_report_product_menu_map")
public class GoDownReportProductMenuTitleMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_name")
    private String menuName;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "menu_id")
    private Long menuId;

    @Column
    private int active;
}
