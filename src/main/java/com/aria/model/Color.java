package com.aria.model;

import com.aria.dto.ColorDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Table(name = "color")
@Getter
@Setter
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String value;

    public Color(ColorDto color) {
        this.id = color.getId();
        this.name = color.getName();
        this.value = color.getValue();
    }

}
