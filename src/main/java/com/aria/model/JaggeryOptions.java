package com.aria.model;

import com.aria.dto.JaggeryOptionsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "jaggoptions")
public class JaggeryOptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attribute_Id")
    private Long attributeId;

    @Column
    private String name;

    @Column
    private String range;

    public JaggeryOptions(JaggeryOptionsDto jaggeryOptions) {
        this.id = jaggeryOptions.getId();
        this.attributeId = jaggeryOptions.getAttributeId();
        this.name = jaggeryOptions.getName();
        this.range = jaggeryOptions.getRange();
    }
}
