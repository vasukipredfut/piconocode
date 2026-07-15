package com.aria.model;

import com.aria.dto.CategoryConstantsDto;
import com.aria.dto.ColorDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Table(name = "category_constants")
@Getter
@Setter
public class CategoryConstants {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Long type;

    public CategoryConstants(CategoryConstantsDto constantsDto) {
        this.id = constantsDto.getId();
        this.name = constantsDto.getName();
        this.type = constantsDto.getType();
    }

}
