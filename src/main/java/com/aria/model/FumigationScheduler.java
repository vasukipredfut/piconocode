package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "fumigation_scheduler")
public class FumigationScheduler {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fumigation_type")
    private String fumigationType;

    @Column(name = "last_date")
    @Temporal(TemporalType.DATE)
    private Date lastDate;

    @Column(name = "next_date")
    @Temporal(TemporalType.DATE)
    private Date nextDate;

    @OneToOne
    @JoinColumn(name = "godown_id", referencedColumnName = "id")
    private GoDown godown;
}
