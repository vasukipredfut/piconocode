package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "inprocess_audit")
public class InProcessAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String batch_no;

    @Column
    private String entry_type;

    @Column(name = "inward_date")
    private Date inwardDate;

    @Column(name = "product_id")
    private Long productId;

    @Column
    private String status;


    @Column
    private String batch_type;


    @Column(name = "created_By", updatable = false)
    private Long createdBy;

    @Column(name = "updated_By")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

}
