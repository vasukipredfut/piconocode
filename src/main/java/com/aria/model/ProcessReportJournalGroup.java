package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "prgrpsjid")
public class ProcessReportJournalGroup {
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long prgrptitle;

    @Column
    private Long prid;

    @Column
    private Long prgrpid;

    @Column
    private Long sjid;

    @Column
    private Long createdby;
}
