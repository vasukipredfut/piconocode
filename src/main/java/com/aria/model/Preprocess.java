package com.aria.model;

import com.aria.dto.PShiftsDto;
import com.aria.dto.PreprocessDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "preprocess")
public class Preprocess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long priid;

    @Column
    private String name;

    @Column
    private Long sjid;

    @Column
    private Long refid;

    @Column
    private Long id;

    public Preprocess(PreprocessDto preprocess) {
        this.id = preprocess.getId();
        this.name = preprocess.getName();
        this.sjid = preprocess.getSjid();
        this.refid=preprocess.getProductId();
        this.priid=preprocess.getPriid();
    }
}
