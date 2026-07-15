package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "qtyrefid")
public class QtyRefid {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long gggparent;

    @Column
    private Long ggparent;

    @Column
    private Long gparent;

    @Column
    private Long parent;

    @Column
    private Long refId;

    @Column
    private String pf;

    @Column
    private Long createdBy;
}
