package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "employee_leave")
public class UserLeaveCount {

    @Id
    @GenericGenerator(name = "elementKey", strategy = "assigned")
    @GeneratedValue(generator = "elementKey")
    @Column(name = "emp_id")
    private Long userId;

    @Column
    private int earn_leave_count;

    @Column
    private int remaining_leave_count;

    @Column(name = "updated_date")
    @Temporal(TemporalType.DATE)
    private Date updatedDate;
    @Column(name = "approve_date")
    @Temporal(TemporalType.DATE)
    private Date approvedDate;

}
