package com.aria.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "day_end_closing_branch_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DayEndClosingBranchGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "child_branch_id")
    private Long childBranchId;

    @Column(name = "child_branch_name")
    private String childBranchName;

    @Column(name = "created_date")
    private LocalDateTime createdAt;

}
