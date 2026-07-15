package com.aria.model;

import com.aria.dto.PAttributesDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "pattributes")
public class PAttributes {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Long dept;

    @Column
    private String datatype;

    public PAttributes(PAttributesDto pAttributesDto) {
        this.id = pAttributesDto.getId();
        this.name = pAttributesDto.getAttributesName();
        this.dept = pAttributesDto.getDeptId();
        this.datatype = pAttributesDto.getDatatype();
    }
}
