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
@Table(name = "daily_wages_attendance_timeline")
public class DailyWagesAttendanceTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long attendanceId;

    @Column
    private Long userId;

    @Column
    private Long dwlogId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "biomast_id", referencedColumnName = "sno")
    private Biomast biomast;

    @Column
    private Date inTime;

    @Column
    private Date outTime;

    @Column
    private Date attendanceTime;

    @Column
    private Date attendanceDate;

    @Transient
    @Column
    private String duration;

    @Transient
    @Column
    private Boolean isReasonNeeded;

    @Column
    private String reason;

    @Column
    private String endposition;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private Date created_date;

    @Column
    private Date updated_date;
}
